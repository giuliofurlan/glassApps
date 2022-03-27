package com.giufu.spotify

import okhttp3.*
import org.json.JSONObject

class SpotifyAPI(OAuthToken: String) {
    private var mAuthHeader: Headers = Headers.of("Authorization", "Bearer $OAuthToken")
    private var client: OkHttpClient = OkHttpClient()
    private var formBody: RequestBody = FormBody.Builder().build()

    private fun simpleGetRequest(url: String): String {
        val request = Request.Builder()
            .get()
            .headers(mAuthHeader)
            .url(url)
            .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }

    private fun simplePostRequest(url: String): String {
        val request = Request.Builder()
            .post(formBody)
            .headers(mAuthHeader)
            .url(url)
            .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }

    private fun simplePutRequest(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .put(formBody)
            .headers(mAuthHeader)
            .url(url)
            .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }


    fun getCurrentlyPlayingTrack(): JSONObject {
        val url = "https://api.spotify.com/v1/me/player/currently-playing"
        return JSONObject(simpleGetRequest(url))
    }

    fun pausePlayBack(){
        val url = "https://api.spotify.com/v1/me/player/pause"
        simplePutRequest(url)
    }

    fun resumePlayback(){
        val url = "https://api.spotify.com/v1/me/player/play"
        simplePutRequest(url)
        //can be used to start a new song as well
    }

    fun skipToNext() {
        val url = "https://api.spotify.com/v1/me/player/next"
        simplePostRequest(url)
    }

    fun skipToPrevious(){
        val url = "https://api.spotify.com/v1/me/player/previous"
        simplePostRequest(url)
    }
}