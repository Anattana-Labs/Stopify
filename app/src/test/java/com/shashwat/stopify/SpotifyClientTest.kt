package com.shashwat.stopify

import org.junit.Test
import java.io.File
import java.net.URL

class SpotifyClientTest {

    @Test
    fun testCookieParsing() {
        // Test Netscape cookies.txt format
        val netscape = """
            # Netscape HTTP Cookie File
            .spotify.com	TRUE	/	FALSE	1780000000	sp_dc	AQBe8fsMFl...
            .spotify.com	TRUE	/	FALSE	1780000000	sp_key	f164460a...
        """.trimIndent()
        val parsedNetscape = com.shashwat.stopify.data.SpotifyWebClient.parseCookieInput(netscape)
        org.junit.Assert.assertTrue(parsedNetscape.contains("sp_dc=AQBe8fsMFl..."))
        org.junit.Assert.assertTrue(parsedNetscape.contains("sp_key=f164460a..."))
    }

    @Test
    fun testTokenFetchFromCookies() {
        val spDc = "AQBe8fsMFlG6CliFEY2OcaX6JoMM7PceOn-NpEEgpZzzNUDEmnVeR1beSeurfqH2I5SZX9JDXorfKFFvDGZkJ37QkX72k1sVWnnN0JzpFOGR0OGfBpj6kVgatFLPfi8xDDNQ4G8v2VtZOCWt0HK_o3ru6muOgoKMsPlUNQFjt4oFJf5uckaNl33QQ5_OoTv3GIq2Xg55UjR2vKYm9VY"
        val cookie = "sp_dc=$spDc; sp_key=f164460a-a740-48da-9169-4ba4b8fbb4d0; sp_t=9167488b53cb6529d696cc87a46a8235"
        
        val url = URL("https://open.spotify.com/get_access_token?reason=transport&productType=web_player")
        val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            setRequestProperty("Cookie", cookie)
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Referer", "https://open.spotify.com/")
            setRequestProperty("Origin", "https://open.spotify.com")
            setRequestProperty("Sec-Fetch-Dest", "empty")
            setRequestProperty("Sec-Fetch-Mode", "cors")
            setRequestProperty("Sec-Fetch-Site", "same-origin")
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = java.io.BufferedReader(java.io.InputStreamReader(stream)).readText()
        println("=== TEST TOKEN FETCH ===")
        println("Response Code: $code")
        println("Response Body: $body")
    }

    @Test
    fun testPathfinderSearchV2() {
        val token = "BQARZ95Iz0OaCr6e2qQ43IgRlW7qaSBFMI2HRGefAE4d_K0ofgm2PFmDwkyURQKEUB_a2VHtszL1tfREbf8rT_JxCx6pjzEJheauEDY6V86O48tFJphhxc7dhHJyGwkw-SMHeyN0hQxXv0nbPwvrP1pLinMtKUZcJitkl9uh-D0zcE9WCwGXEKzsGK8OqJDogNQwmSnY0kZ2wmJcov7KQjrlZ6s659qvL5pGrLJZa7VkvmOG_PhtrVVnY_6LqrKU5-ntE6z1fuZT6Usf6M-jMsNGxNinf_O7ziyOSqWQYZ2be2la9n9qpCrvKSFCpUxVF5r_I9q3VyTMYf3kAad1wB5_71yW_MwlLBGoB1jRnYukCTFUOn-zwpvgPZ9PjJHIPmUFq0nwbNVWyzfBT1OS"
        val clientToken = "AAF2cbcSyqztb/skX/G1OLT1DpFy8vcibKMaAWSkIZOoJhd/a8J3rOIkeb4iUpJ7ERkfl+k1e7rkkKUd+eG1bkgkiJB6XoDssmOrlqTJIrLz3AYqnNWCnW/a/jeKahNYfo9eULzpyJlwr6AcSKbqjD1458O2AXyDaQb6jTJinGnQMKbxZZy0fp98bOSDSr1PGDiYECkEqvrlQ9cSa3bROqwlkY3+Yi/Bw7G00xzB2DB3e7f7lcqV/G7DKjXuKdpJQelaXdghaMXLylMPVmrBzfYxkc3I3zR5VEje5Eqx71oXLLUFJ2CyZZsYZlE3aEqymjcDnHKofKpSZcxdaH2JKF+pVS/fmkc2757/aZJxOPLTFgK+iIVnZzHqy3XjlsDV9GYH/XpBJwrXfQ=="

        val url = URL("https://api-partner.spotify.com/pathfinder/v2/query")
        val payload = """
        {
          "variables": {
            "query": "not enough",
            "limit": 50,
            "offset": 0,
            "numberOfTopResults": 50,
            "includeArtistHasConcertsField": false,
            "includeAudiobooks": true,
            "includeAuthors": true,
            "includePreReleases": true,
            "includeAlbumPreReleases": false,
            "includeEpisodeContentRatingsV2": true,
            "isPrefix": null,
            "sectionFilters": ["GENERIC", "VIDEO_CONTENT"]
          },
          "operationName": "searchTopResultsList",
          "extensions": {
            "persistedQuery": {
              "version": 1,
              "sha256Hash": "dd78eaff943eba629ed70ee25517b9cea0dcaa41193e2592ae2727660b21892c"
            }
          }
        }
        """.trimIndent()

        val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("authorization", "Bearer $token")
            setRequestProperty("client-token", clientToken)
            setRequestProperty("content-type", "application/json;charset=UTF-8")
            setRequestProperty("app-platform", "WebPlayer")
            setRequestProperty("spotify-app-version", "1.3.5.316.gd873ee9db135")
            setRequestProperty("origin", "https://open.spotify.com")
            setRequestProperty("referer", "https://open.spotify.com/")
            setRequestProperty("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/155.0.0.0 Safari/537.36")
        }

        java.io.OutputStreamWriter(conn.outputStream).use { it.write(payload) }
        val responseCode = conn.responseCode
        val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
        val responseBody = java.io.BufferedReader(java.io.InputStreamReader(stream)).readText()
        println("Pathfinder search response code: $responseCode")
        
        val json = org.json.JSONObject(responseBody)
        val searchV2 = json.getJSONObject("data").getJSONObject("searchV2")
        println("searchV2 keys: ${searchV2.keys().asSequence().toList()}")
        val topResults = searchV2.getJSONObject("topResultsV2").getJSONArray("itemsV2")
        println("topResults itemsV2 count: ${topResults.length()}")
        var trackCount = 0
        for (i in 0 until topResults.length()) {
            val itemObj = topResults.getJSONObject(i).getJSONObject("item")
            if (itemObj.optString("__typename") == "TrackResponseWrapper") {
                trackCount++
                val trackData = itemObj.getJSONObject("data")
                println("Track $trackCount: ${trackData.getString("name")} by ${trackData.getJSONObject("artists").getJSONArray("items").getJSONObject(0).getJSONObject("profile").getString("name")} id=${trackData.getString("id")}")
            }
        }
        val hexId = base62ToHex("1caCUoqSxqsZDN6Ackcswv")
        println("Track hex ID: $hexId")

        val mUrl = URL("https://spclient.wg.spotify.com/metadata/4/track/$hexId?market=from_token")
        val mConn = (mUrl.openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("authorization", "Bearer $token")
            setRequestProperty("client-token", clientToken)
            setRequestProperty("app-platform", "WebPlayer")
            setRequestProperty("spotify-app-version", "1.3.5.316.gd873ee9db135")
            setRequestProperty("origin", "https://open.spotify.com")
            setRequestProperty("referer", "https://open.spotify.com/")
        }
        val bytes = mConn.inputStream.readBytes()
        println("Metadata bytes length: ${bytes.size}")
        val hexStr = bytes.joinToString("") { "%02x".format(it) }
        println("Metadata full hex:\n$hexStr")

        // In Spotify Track proto, field 12 is file (repeated AudioFile, tag = 0x62 = (12 << 3 | 2))
        // AudioFile has field 1 (file_id, 20 bytes, tag = 0x0a 0x14 = 0a14) and field 2 (format enum, tag = 0x10)
        // Let us look for 62 followed by len then 0a14
        val fileRegex = Regex("""62[0-9a-f]{2}0a14([0-9a-f]{40})""")
        val foundFiles = fileRegex.findAll(hexStr).map { it.groupValues[1] }.toList()
        println("Found Audio Files (field 12): $foundFiles")

        // Also check if 64def3f88fba87ae7235c0a912931c9452d7fd84 is anywhere in the metadata!
        println("Contains 64def3f88fba87ae7235c0a912931c9452d7fd84: ${hexStr.contains("64def3f88fba87ae7235c0a912931c9452d7fd84")}")




        // Direct test of 64def3f88fba87ae7235c0a912931c9452d7fd84 (known real audio file ID)
        val testFileId = "64def3f88fba87ae7235c0a912931c9452d7fd84"
        val resolveUrl = "https://gae2-spclient.spotify.com/storage-resolve/v2/files/audio/interactive/10/$testFileId?version=10000000&product=9&platform=39&alt=json"
        val rConn = (URL(resolveUrl).openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("authorization", "Bearer $token")
            setRequestProperty("client-token", clientToken)
            setRequestProperty("app-platform", "WebPlayer")
            setRequestProperty("spotify-app-version", "1.3.5.316.gd873ee9db135")
            setRequestProperty("origin", "https://open.spotify.com")
            setRequestProperty("referer", "https://open.spotify.com/")
        }
        val rCode = rConn.responseCode
        val rStream = if (rCode in 200..299) rConn.inputStream else rConn.errorStream
        val rBody = java.io.BufferedReader(java.io.InputStreamReader(rStream)).readText()
        println("Direct Audio File StorageResolve Status: $rCode")
        println("Direct Audio File StorageResolve Response: $rBody")
    }

    @Test
    fun testDealerAndConnectStatePlayback() {
        val token = "BQARZ95Iz0OaCr6e2qQ43IgRlW7qaSBFMI2HRGefAE4d_K0ofgm2PFmDwkyURQKEUB_a2VHtszL1tfREbf8rT_JxCx6pjzEJheauEDY6V86O48tFJphhxc7dhHJyGwkw-SMHeyN0hQxXv0nbPwvrP1pLinMtKUZcJitkl9uh-D0zcE9WCwGXEKzsGK8OqJDogNQwmSnY0kZ2wmJcov7KQjrlZ6s659qvL5pGrLJZa7VkvmOG_PhtrVVnY_6LqrKU5-ntE6z1fuZT6Usf6M-jMsNGxNinf_O7ziyOSqWQYZ2be2la9n9qpCrvKSFCpUxVF5r_I9q3VyTMYf3kAad1wB5_71yW_MwlLBGoB1jRnYukCTFUOn-zwpvgPZ9PjJHIPmUFq0nwbNVWyzfBT1OS"
        val clientToken = "AAF2cbcSyqztb/skX/G1OLT1DpFy8vcibKMaAWSkIZOoJhd/a8J3rOIkeb4iUpJ7ERkfl+k1e7rkkKUd+eG1bkgkiJB6XoDssmOrlqTJIrLz3AYqnNWCnW/a/jeKahNYfo9eULzpyJlwr6AcSKbqjD1458O2AXyDaQb6jTJinGnQMKbxZZy0fp98bOSDSr1PGDiYECkEqvrlQ9cSa3bROqwlkY3+Yi/Bw7G00xzB2DB3e7f7lcqV/G7DKjXuKdpJQelaXdghaMXLylMPVmrBzfYxkc3I3zR5VEje5Eqx71oXLLUFJ2CyZZsYZlE3aEqymjcDnHKofKpSZcxdaH2JKF+pVS/fmkc2757/aZJxOPLTFgK+iIVnZzHqy3XjlsDV9GYH/XpBJwrXfQ=="
        val deviceId = "af6fd454eccef1714bc99a09a10c119366e8da1c"

        val wsUrl = java.net.URI.create("wss://gae2-dealer.g2.spotify.com/?access_token=$token")
        val latch = java.util.concurrent.CountDownLatch(1)
        var connectionId: String? = null
        val receivedWsMessages = java.util.concurrent.CopyOnWriteArrayList<String>()

        // Test OkHttp Dealer WebSocket
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val okWsUrl = "wss://gae2-dealer.g2.spotify.com/?access_token=$token"
        val okWsRequest = okhttp3.Request.Builder().url(okWsUrl).build()

        var okConnectionId: String? = null
        val okLatch = java.util.concurrent.CountDownLatch(1)
        val audioFileMap = java.util.concurrent.ConcurrentHashMap<String, String>()

        val okWs = okHttpClient.newWebSocket(okWsRequest, object : okhttp3.WebSocketListener() {
            override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
                println("OkHttp WebSocket opened!")
            }

            override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                try {
                    val json = org.json.JSONObject(text)
                    val headers = json.optJSONObject("headers")
                    val connId = headers?.optString("Spotify-Connection-Id")
                    if (!connId.isNullOrEmpty() && okConnectionId == null) {
                        okConnectionId = connId
                        println("OkHttp Captured connection ID: $connId")
                        okLatch.countDown()
                    }

                    if (text.contains("file_ids_mp4")) {
                        val obj = org.json.JSONObject(text)
                        val payloads = obj.optJSONArray("payloads")
                        if (payloads != null && payloads.length() > 0) {
                            val sm = payloads.getJSONObject(0).optJSONObject("state_machine")
                            println("sm keys: ${sm?.keys()?.asSequence()?.toList()}")
                            val tracks = sm?.optJSONArray("tracks")
                            println("tracks count: ${tracks?.length()}")
                            if (tracks != null) {
                                for (i in 0 until tracks.length()) {
                                    val tObj = tracks.getJSONObject(i)
                                    val tUri = tObj.optJSONObject("metadata")?.optString("uri")
                                    val tName = tObj.optJSONObject("metadata")?.optString("name")
                                    val fileIdsMp4 = tObj.optJSONObject("manifest")?.optJSONArray("file_ids_mp4")
                                    val fId = fileIdsMp4?.optJSONObject(0)?.optString("file_id")
                                    println("Track $i: name='$tName', uri='$tUri', file_id='$fId'")
                                    if (tUri != null && fId != null) {
                                        audioFileMap[tUri] = fId
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                }
            }
        })

        okLatch.await(5, java.util.concurrent.TimeUnit.SECONDS)
        println("OkHttp Connection ID: $okConnectionId")
        checkNotNull(okConnectionId)

        // Register device with OkHttp connection ID
        val regConn2 = (URL("https://gae2-spclient.spotify.com/track-playback/v1/devices").openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("authorization", "Bearer $token")
            setRequestProperty("client-token", clientToken)
            setRequestProperty("content-type", "application/json;charset=UTF-8")
            setRequestProperty("app-platform", "WebPlayer")
            setRequestProperty("spotify-app-version", "1.3.5.316.gd873ee9db135")
            setRequestProperty("origin", "https://open.spotify.com")
            setRequestProperty("referer", "https://open.spotify.com/")
        }
        val regPayload2 = """
        {
          "device": {
            "brand": "spotify",
            "capabilities": {
              "change_volume": true,
              "enable_play_token": true,
              "supports_file_media_type": true,
              "play_token_lost_behavior": "pause",
              "disable_connect": false,
              "audio_podcasts": true,
              "video_playback": true,
              "manifest_formats": ["file_ids_mp4", "file_ids_mp4_dual"],
              "supports_preferred_media_type": true,
              "supports_playback_offsets": true,
              "supports_playback_speed": true
            },
            "device_id": "$deviceId",
            "device_type": "computer",
            "metadata": {},
            "model": "web_player",
            "name": "Web Player (Chrome)",
            "platform_identifier": "web_player osx 10.15.7;chrome 155.0.0.0;desktop",
            "is_group": false,
            "is_public": false,
            "correlation_id": "9167488b53cb6529d696cc87a46a8235",
            "client_version": "harmony:4.80.0-3cdf02c91"
          },
          "outro_endcontent_snooping": false,
          "connection_id": "$okConnectionId",
          "client_version": "harmony:4.80.0-3cdf02c91",
          "volume": 65535
        }
        """.trimIndent()
        java.io.OutputStreamWriter(regConn2.outputStream).use { it.write(regPayload2) }
        println("Device registration 2 code: ${regConn2.responseCode}")

        // Command play for 4AyKPZ1b7lUlsAOb3aG6tC
        val playConn2 = (URL("https://gae2-spclient.spotify.com/connect-state/v1/player/command/from/$deviceId/to/$deviceId").openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("authorization", "Bearer $token")
            setRequestProperty("client-token", clientToken)
            setRequestProperty("content-type", "application/json;charset=UTF-8")
            setRequestProperty("app-platform", "WebPlayer")
            setRequestProperty("spotify-app-version", "1.3.5.316.gd873ee9db135")
            setRequestProperty("origin", "https://open.spotify.com")
            setRequestProperty("referer", "https://open.spotify.com/")
        }
        val playPayload2 = """
        {
          "command": {
            "context": {
              "uri": "spotify:track:4AyKPZ1b7lUlsAOb3aG6tC",
              "url": "context://spotify:track:4AyKPZ1b7lUlsAOb3aG6tC",
              "metadata": {}
            },
            "play_origin": {
              "feature_identifier": "auto_play_on_load",
              "feature_version": "web-player_2026-10-09_1791504000000_d873ee9db135",
              "referrer_identifier": "search"
            },
            "options": {
              "license": "tft",
              "skip_to": {},
              "player_options_override": {}
            },
            "logging_params": {
              "page_instance_ids": [],
              "interaction_ids": [],
              "command_id": "7e9cd0d7acb851a5a5a2eebe699c4dd4"
            },
            "endpoint": "play"
          }
        }
        """.trimIndent()
        java.io.OutputStreamWriter(playConn2.outputStream).use { it.write(playPayload2) }
        println("Play command 2 code: ${playConn2.responseCode}")

        Thread.sleep(3000)
        println("AudioFileMap size: ${audioFileMap.size}")
        for ((k, v) in audioFileMap) {
            println("Map entry: $k -> $v")
        }
        okWs.close(1000, "Done")
    }





    private val base62Chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private fun base62ToHex(base62: String): String {
        var bi = java.math.BigInteger.ZERO
        for (c in base62) {
            val idx = base62Chars.indexOf(c)
            bi = bi.multiply(java.math.BigInteger.valueOf(62)).add(java.math.BigInteger.valueOf(idx.toLong()))
        }
        return String.format("%032x", bi)
    }
}



