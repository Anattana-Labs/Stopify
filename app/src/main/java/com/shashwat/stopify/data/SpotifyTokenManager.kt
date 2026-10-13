package com.shashwat.stopify.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Headless Background Token Manager for Spotify
 * Automatically fetches and refreshes Bearer access tokens in the background
 * without interrupting the user.
 */
object SpotifyTokenManager {
    private const val TAG = "SpotifyTokenManager"

    private var headlessWebView: WebView? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var isRefreshing = false

    private val _isRefreshingState = MutableStateFlow(false)
    val isRefreshingState: StateFlow<Boolean> = _isRefreshingState.asStateFlow()

    private val pendingCallbacks = mutableListOf<(String?) -> Unit>()

    @SuppressLint("SetJavaScriptEnabled")
    fun refreshAccessTokenInBackground(
        context: Context,
        client: SpotifyWebClient,
        showLoadingModal: Boolean = false,
        onComplete: ((String?) -> Unit)? = null
    ) {
        mainHandler.post {
            if (onComplete != null) {
                pendingCallbacks.add(onComplete)
            }

            if (isRefreshing) {
                Log.d(TAG, "Token refresh already in progress, registered listener")
                if (showLoadingModal) {
                    _isRefreshingState.value = true
                }
                return@post
            }

            val appContext = context.applicationContext
            val prefs = appContext.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE)
            val cookies = client.cookieHeaderString.ifEmpty { prefs.getString("spotify_cookies", "") ?: "" }

            if (!SpotifyWebClient.hasValidSpDc(cookies)) {
                Log.d(TAG, "No valid sp_dc session cookies to refresh, skipping background refresh")
                val callbacks = ArrayList(pendingCallbacks)
                pendingCallbacks.clear()
                callbacks.forEach { it(null) }
                return@post
            }

            isRefreshing = true
            _isRefreshingState.value = showLoadingModal

            val webView = headlessWebView ?: WebView(appContext).also {
                it.settings.javaScriptEnabled = true
                it.settings.domStorageEnabled = true
                it.settings.userAgentString = SpotifyWebClient.USER_AGENT

                class StopifyBridge {
                    @JavascriptInterface
                    fun onToken(token: String?) {
                        if (!token.isNullOrEmpty() && token.startsWith("BQ")) {
                            Log.d(TAG, "Captured token via JS bridge: ${token.take(20)}...")
                            notifyTokenFound(appContext, client, token)
                        }
                    }

                    @JavascriptInterface
                    fun onClientToken(ct: String?) {
                        if (!ct.isNullOrEmpty()) {
                            prefs.edit().putString("spotify_client_token", ct).apply()
                            client.setClientToken(ct)
                        }
                    }

                    @JavascriptInterface
                    fun onDeviceId(devId: String?) {
                        if (!devId.isNullOrEmpty()) {
                            Log.d(TAG, "Captured Spotify Web Player active cluster device_id: $devId")
                            prefs.edit().putString("spotify_active_device_id", devId).apply()
                            client.webPlayerDeviceId = devId
                        }
                    }
                }
                it.addJavascriptInterface(StopifyBridge(), "StopifyBridge")
                headlessWebView = it
            }

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true)

            // Seed active session cookies
            val cookiesToSeed = cookies
            for (pair in cookiesToSeed.split(";")) {
                val p = pair.trim()
                if (p.isNotEmpty()) {
                    cookieManager.setCookie("https://open.spotify.com", p)
                    cookieManager.setCookie("https://accounts.spotify.com", p)
                    cookieManager.setCookie(".spotify.com", p)
                }
            }
            cookieManager.flush()

            webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    // Spoof Page Visibility so Spotify's Web Playback SDK connects to Dealer & registers cluster device even off-screen
                    view?.evaluateJavascript(
                        """
                        (function(){
                          try {
                            var vis = function(){ return 'visible'; };
                            var no  = function(){ return false; };
                            Object.defineProperty(document,'visibilityState',{configurable:true,get:vis});
                            Object.defineProperty(document,'hidden',{configurable:true,get:no});
                            Object.defineProperty(document,'webkitVisibilityState',{configurable:true,get:vis});
                            Object.defineProperty(document,'webkitHidden',{configurable:true,get:no});
                            document.hasFocus = function(){ return true; };
                            ['visibilitychange','webkitvisibilitychange','blur','pagehide'].forEach(function(t){
                              window.addEventListener(t, function(e){ e.stopImmediatePropagation(); }, true);
                              document.addEventListener(t, function(e){ e.stopImmediatePropagation(); }, true);
                            });
                          } catch(e){}
                        })();
                        """.trimIndent(),
                        null
                    )
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val reqUrl = request?.url?.toString() ?: ""
                    val headers = request?.requestHeaders

                    if (headers != null) {
                        val auth = headers["authorization"] ?: headers["Authorization"]
                        if (auth != null && auth.startsWith("Bearer BQ")) {
                            val token = auth.removePrefix("Bearer ").trim()
                            Log.d(TAG, "Captured token via HTTP intercept: ${token.take(20)}...")
                            mainHandler.post {
                                notifyTokenFound(appContext, client, token)
                            }
                        }

                        val ct = headers["client-token"] ?: headers["Client-Token"]
                        if (!ct.isNullOrEmpty()) {
                            prefs.edit().putString("spotify_client_token", ct).apply()
                            client.setClientToken(ct)
                        }
                    }

                    return super.shouldInterceptRequest(view, request)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    
                    // Capture live cookies from open.spotify.com and accounts.spotify.com
                    val currentCookies = cookieManager.getCookie("https://open.spotify.com") ?: ""
                    if (SpotifyWebClient.hasValidSpDc(currentCookies)) {
                        Log.d(TAG, "Captured updated session cookies from WebView")
                        prefs.edit().putString("spotify_cookies", currentCookies).apply()
                        client.updateCookies(currentCookies)
                    }

                    if (url != null && url.contains("spotify.com")) {
                        // Inject bootstrap hook to capture web player device ID and tokens
                        view?.evaluateJavascript(
                            """
                            (function(){
                              if (window.__stopifyHooked) return;
                              window.__stopifyHooked = true;
                              var oriFetch = window.fetch.bind(window);
                              window.fetch = function(){
                                try {
                                  var url = arguments[0], opts = arguments[1] || {};
                                  var us = (typeof url === 'string') ? url : (url && url.url) || '';
                                  var hs = opts.headers || {};
                                  function hv(n){ try { return hs.get ? hs.get(n) : (hs[n] || hs[n.toLowerCase()]); } catch(e){ return null; } }
                                  if (us.indexOf('/track-playback/v1/devices') > -1 && opts.body) {
                                    try {
                                      var b = JSON.parse(opts.body);
                                      if (b && b.device && b.device.device_id) {
                                        window.StopifyBridge.onDeviceId(b.device.device_id);
                                      }
                                    } catch(e){}
                                  }
                                  var ct = hv('Client-Token') || hv('client-token');
                                  if (ct) window.StopifyBridge.onClientToken(ct);
                                  var au = hv('Authorization') || hv('authorization');
                                  if (au && au.indexOf('Bearer') === 0) window.StopifyBridge.onToken(au.replace('Bearer ', '').trim());
                                } catch(e){}
                                return oriFetch.apply(this, arguments);
                              };
                            })();
                            """.trimIndent(),
                            null
                        )

                        view?.evaluateJavascript(
                            """
                            (function() {
                                try {
                                    fetch('https://open.spotify.com/get_access_token?reason=transport&productType=web_player')
                                        .then(function(r) { return r.json(); })
                                        .then(function(d) {
                                            if (d && d.accessToken) {
                                                window.StopifyBridge.onToken(d.accessToken);
                                            }
                                        })
                                        .catch(function(e) {});
                                } catch(e) {}
                            })();
                            """.trimIndent(),
                            null
                        )
                    }
                }
            }

            // Safety timeout after 10 seconds
            val timeoutRunnable = Runnable {
                if (isRefreshing) {
                    Log.w(TAG, "Background token refresh timed out")
                    isRefreshing = false
                    _isRefreshingState.value = false
                    val callbacks = ArrayList(pendingCallbacks)
                    pendingCallbacks.clear()
                    callbacks.forEach { it(null) }
                }
            }
            mainHandler.postDelayed(timeoutRunnable, 10000)

            webView.loadUrl("https://open.spotify.com")
        }
    }

    private fun notifyTokenFound(context: Context, client: SpotifyWebClient, token: String) {
        val wasRefreshing = isRefreshing
        isRefreshing = false
        _isRefreshingState.value = false

        client.setAccessToken(token)
        val prefs = context.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("spotify_access_token", token).apply()

        Log.d(TAG, "Persisted new Spotify Bearer Access Token successfully")

        if (wasRefreshing || pendingCallbacks.isNotEmpty()) {
            val callbacks = ArrayList(pendingCallbacks)
            pendingCallbacks.clear()
            callbacks.forEach { it(token) }
        }
    }

    fun sendWebPlayerPlayCommand(uri: String) {
        mainHandler.post {
            headlessWebView?.evaluateJavascript(
                """
                (function(){
                  try {
                    var devId = window.StopifyBridge ? null : null;
                    // Try playing directly via Web Player DOM play/pause or fetch if ready
                    if (window.__spotuiPlay) {
                      window.__spotuiPlay('$uri');
                    }
                  } catch(e){}
                })();
                """.trimIndent(),
                null
            )
        }
    }

    suspend fun awaitTokenRefresh(context: Context, client: SpotifyWebClient): String? {
        val deferred = CompletableDeferred<String?>()
        refreshAccessTokenInBackground(context, client, showLoadingModal = true) { token ->
            deferred.complete(token)
        }
        return withTimeoutOrNull(20000) { deferred.await() }
    }
}
