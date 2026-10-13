package com.shashwat.stopify.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.shashwat.stopify.data.SpotifyTokenManager
import com.shashwat.stopify.data.SpotifyWebClient

@Composable
fun LoginScreen(
    client: SpotifyWebClient,
    initialTab: Int = 0,
    canDismiss: Boolean = true,
    onDismiss: () -> Unit = {},
    onLoginSuccess: (String) -> Unit = {},
    onSkip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(initialTab) } // 0 = Web, 1 = cookies.txt
    var cookieInputText by remember { mutableStateOf(client.customCookies ?: "") }
    var statusMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var webViewReloadKey by remember { mutableIntStateOf(0) }
    var hasSessionInWeb by remember { mutableStateOf(SpotifyWebClient.hasValidSpDc(client.customCookies)) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val text = stream.bufferedReader().use { it.readText() }
                    cookieInputText = text
                    statusMessage = "Loaded ${text.length} bytes from file."
                    isSuccess = true
                }
            } catch (e: Exception) {
                statusMessage = "Error reading file: ${e.localizedMessage}"
                isSuccess = false
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── HEADER ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canDismiss) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF242424))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(38.dp))
                }

                // Slider Pill Toggle: "Web" | "cookies.txt"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF242424))
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(horizontal = 20.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Web",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.75f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(horizontal = 20.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "cookies.txt",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                // Action Icon
                if (selectedTab == 0) {
                    IconButton(
                        onClick = { webViewReloadKey++ },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF242424))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Reload",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            cookieInputText = ""
                            statusMessage = "Cleared input."
                            isSuccess = false
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF242424))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Clear",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── BODY ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (selectedTab == 0) {
                    // Full Screen WebView
                    val effectiveCookies = if (SpotifyWebClient.hasValidSpDc(cookieInputText)) {
                        cookieInputText
                    } else {
                        client.customCookies ?: ""
                    }
                    key(webViewReloadKey) {
                        SpotifyLoginWebView(
                            initialCookies = effectiveCookies,
                            onCookiesFound = { cookies ->
                                if (SpotifyWebClient.hasValidSpDc(cookies)) {
                                    val prefs = context.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE)
                                    prefs.edit().putString("spotify_cookies", cookies).apply()
                                    client.updateCookies(cookies)
                                    onLoginSuccess(cookies)
                                    hasSessionInWeb = true
                                    statusMessage = "Session detected! Cookies saved."
                                    isSuccess = true
                                }
                            },
                            onTokenFound = { token ->
                                val prefs = context.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE)
                                prefs.edit().putString("spotify_access_token", token).apply()
                                client.setAccessToken(token)
                                hasSessionInWeb = true
                                statusMessage = "Live Bearer Token captured!"
                                isSuccess = true
                            },
                            onClientTokenFound = { ct ->
                                val prefs = context.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE)
                                prefs.edit().putString("spotify_client_token", ct).apply()
                                client.setClientToken(ct)
                            }
                        )
                    }
                } else {
                    // Full Screen cookies.txt Mode
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { filePickerLauncher.launch("text/*") },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF282828),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Pick File", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrEmpty()) {
                                        cookieInputText = clip
                                        statusMessage = "Pasted from clipboard."
                                        isSuccess = true
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF282828),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Paste Clip", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val hasSpDc = SpotifyWebClient.hasValidSpDc(cookieInputText) ||
                                    (!cookieInputText.contains("=") && !cookieInputText.contains("\t") && cookieInputText.trim().length >= 20)
                            val hasBearer = cookieInputText.contains("BQ")

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (hasSpDc) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color(0xFF242424)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (hasSpDc) MaterialTheme.colorScheme.primary else Color.Gray)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (hasSpDc) "sp_dc detected" else "sp_dc missing",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (hasSpDc) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (hasBearer) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color(0xFF242424)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (hasBearer) MaterialTheme.colorScheme.primary else Color.Gray)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (hasBearer) "Bearer Token detected" else "Bearer Token optional",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (hasBearer) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = cookieInputText,
                            onValueChange = { cookieInputText = it },
                            placeholder = {
                                Text(
                                    text = "# Netscape HTTP Cookie File\n.spotify.com\tTRUE\t/\tTRUE\t...\tsp_dc\t...",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White.copy(alpha = 0.35f)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0xFF333333),
                                focusedContainerColor = Color(0xFF1A1A1A),
                                unfocusedContainerColor = Color(0xFF1A1A1A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    }
                }
            }

            // ── FOOTER ──────────────────────────────────────────
            Surface(
                color = Color(0xFF1A1A1A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (statusMessage.isNotEmpty()) {
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSuccess) MaterialTheme.colorScheme.primary else Color(0xFFFF5252),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (selectedTab == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (hasSessionInWeb) MaterialTheme.colorScheme.primary else Color(0xFFE8622C))
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (hasSessionInWeb) "Session Active" else "Log in to Spotify",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    val cm = CookieManager.getInstance()
                                    val c1 = cm.getCookie("https://open.spotify.com") ?: ""
                                    val c2 = cm.getCookie("https://accounts.spotify.com") ?: ""
                                    val combined = "$c1; $c2"
                                    if (SpotifyWebClient.hasValidSpDc(combined)) {
                                        val prefs = context.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE)
                                        prefs.edit().putString("spotify_cookies", combined).apply()
                                        client.updateCookies(combined)
                                        onLoginSuccess(combined)
                                        SpotifyTokenManager.refreshAccessTokenInBackground(context, client, showLoadingModal = false)
                                        hasSessionInWeb = true
                                        statusMessage = "Session saved! Ready to stream."
                                        isSuccess = true
                                    } else {
                                        statusMessage = "No active login detected yet. Please complete sign-in."
                                        isSuccess = false
                                    }
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save Session", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val prefs = context.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE)
                                    prefs.edit().remove("spotify_cookies").remove("spotify_access_token").apply()
                                    client.updateCookies("")
                                    cookieInputText = ""
                                    onLoginSuccess("")
                                    statusMessage = "Credentials reset to defaults."
                                    isSuccess = true
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                                modifier = Modifier.weight(0.4f)
                            ) {
                                Text("Reset", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = {
                                    if (cookieInputText.isNotBlank()) {
                                        val prefs = context.getSharedPreferences("stopify_prefs", Context.MODE_PRIVATE)
                                        val tokenRegex = Regex("""(BQ[A-Za-z0-9_-]{80,})""")
                                        val match = tokenRegex.find(cookieInputText)
                                        if (match != null) {
                                            val token = match.value
                                            prefs.edit().putString("spotify_access_token", token).apply()
                                            client.setAccessToken(token)
                                        }

                                        val parsed = SpotifyWebClient.parseCookieInput(cookieInputText)
                                        if (SpotifyWebClient.hasValidSpDc(parsed)) {
                                            prefs.edit().putString("spotify_cookies", parsed).apply()
                                            client.updateCookies(parsed)
                                            onLoginSuccess(parsed)
                                            val cm = CookieManager.getInstance()
                                            for (pair in parsed.split(";")) {
                                                val p = pair.trim()
                                                if (p.isNotEmpty()) {
                                                    cm.setCookie("https://open.spotify.com", p)
                                                    cm.setCookie("https://accounts.spotify.com", p)
                                                    cm.setCookie(".spotify.com", p)
                                                }
                                            }
                                            cm.flush()
                                            SpotifyTokenManager.refreshAccessTokenInBackground(context, client, showLoadingModal = false)
                                            statusMessage = if (match != null) {
                                                "Access token and cookies saved!"
                                            } else {
                                                "Cookies saved! Session active."
                                            }
                                            isSuccess = true
                                        } else if (match != null) {
                                            statusMessage = "Access token saved!"
                                            isSuccess = true
                                        } else {
                                            statusMessage = "Invalid input: valid sp_dc cookie not found."
                                            isSuccess = false
                                        }
                                    } else {
                                        statusMessage = "Please paste or import cookies first."
                                        isSuccess = false
                                    }
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(0.6f)
                            ) {
                                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save & Apply", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (onSkip != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = onSkip,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = "Skip for now",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SpotifyLoginWebView(
    initialCookies: String = "",
    onCookiesFound: (String) -> Unit,
    onTokenFound: (String) -> Unit,
    onClientTokenFound: (String) -> Unit = {}
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.parseColor("#121212"))

                val defaultUa = settings.userAgentString ?: ""
                settings.userAgentString = if (defaultUa.contains("Android")) {
                    defaultUa.replace("; wv", "").replace(Regex("""Version/\d+\.\d+\s*"""), "")
                } else {
                    "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
                }

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                settings.allowContentAccess = true
                settings.allowFileAccess = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.setSupportMultipleWindows(false)
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                val hasValidSession = SpotifyWebClient.hasValidSpDc(initialCookies)
                if (hasValidSession) {
                    for (pair in initialCookies.split(";")) {
                        val p = pair.trim()
                        if (p.isNotEmpty()) {
                            cookieManager.setCookie("https://open.spotify.com", p)
                            cookieManager.setCookie("https://accounts.spotify.com", p)
                            cookieManager.setCookie(".spotify.com", p)
                        }
                    }
                    cookieManager.flush()
                } else {
                    cookieManager.removeAllCookies(null)
                    cookieManager.flush()
                }

                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                        request?.grant(request.resources)
                    }

                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                        android.util.Log.d("SpotifyLoginWebView", "JS: ${consoleMessage?.message()} (${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})")
                        return true
                    }
                }

                class StopifyBridge {
                    @android.webkit.JavascriptInterface
                    fun onToken(token: String?) {
                        if (!token.isNullOrEmpty() && token.startsWith("BQ")) {
                            post { onTokenFound(token) }
                        }
                    }

                    @android.webkit.JavascriptInterface
                    fun onClientToken(token: String?) {
                        if (!token.isNullOrEmpty()) {
                            post { onClientTokenFound(token) }
                        }
                    }
                }
                addJavascriptInterface(StopifyBridge(), "StopifyBridge")

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        // Polyfill requestStorageAccess so reCAPTCHA Enterprise does not get 'Permission denied'
                        view?.evaluateJavascript(
                            """
                            (function() {
                                try {
                                    if (window.document) {
                                        document.requestStorageAccess = function() { return Promise.resolve(); };
                                        document.hasStorageAccess = function() { return Promise.resolve(true); };
                                    }
                                } catch(e) {}
                            })();
                            """.trimIndent(),
                            null
                        )
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val reqUrl = request?.url?.toString() ?: return false
                        android.util.Log.d("SpotifyLoginWebView", "shouldOverrideUrlLoading: $reqUrl")
                        if (reqUrl.startsWith("intent:") || reqUrl.startsWith("spotify:")) {
                            return true
                        }
                        return false
                    }

                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        super.onReceivedError(view, request, error)
                        android.util.Log.e("SpotifyLoginWebView", "onReceivedError: ${error?.errorCode} ${error?.description} for ${request?.url}")
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val reqUrl = request?.url?.toString() ?: ""
                        val headers = request?.requestHeaders

                        val isApiEndpoint = reqUrl.contains("api-partner.spotify.com") ||
                                reqUrl.contains("spclient.spotify.com") ||
                                reqUrl.contains("gae2-spclient.spotify.com")

                        if (isApiEndpoint && headers != null) {
                            val auth = headers["authorization"] ?: headers["Authorization"]
                            if (auth != null && auth.startsWith("Bearer BQ")) {
                                val token = auth.removePrefix("Bearer ").trim()
                                view?.post { onTokenFound(token) }
                            }

                            val ct = headers["client-token"] ?: headers["Client-Token"]
                            if (!ct.isNullOrEmpty()) {
                                view?.post { onClientTokenFound(ct) }
                            }
                        }

                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val cookies = cookieManager.getCookie("https://open.spotify.com") ?: ""
                        if (SpotifyWebClient.hasValidSpDc(cookies)) {
                            onCookiesFound(cookies)
                        }

                        if (url?.contains("open.spotify.com") == true) {
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

                if (hasValidSession) {
                    loadUrl("https://open.spotify.com/")
                } else {
                    loadUrl("https://accounts.spotify.com/en/login?continue=https%3A%2F%2Fopen.spotify.com%2F")
                }
            }
        }
    )
}
