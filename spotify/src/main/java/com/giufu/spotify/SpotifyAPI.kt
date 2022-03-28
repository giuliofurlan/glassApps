package com.giufu.spotify

import okhttp3.*
import org.json.JSONObject

class SpotifyAPI(OAuthToken: String) {
    companion object{
        val oauth = "BQAXwGdr-RNDuXVcZeU2eemseooF5AfP9GBeLx1xcxSFj6WPGq9O_Dib9AFAPjJuGUTydLDtfjiaiP8xkzhpRL4dAkGG8Q9MN3ASymHHvkzQGJUCoHnDURHkNEvgcNmZgpVja51Mf1dvGtizi9CAwL7nUQ"
    }

    private var mAuthHeader: Headers = Headers.of("Authorization", "Bearer $OAuthToken")
    private var client: OkHttpClient = OkHttpClient()
    private var formBody: RequestBody = FormBody.Builder().build()

    fun simpleGetRequest(url: String): JSONObject {
        val request = Request.Builder()
        .get()
        .headers(mAuthHeader)
        .url(url)
        .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body()!!.string()
        val jsonObj = JSONObject(responseBody)
        return  jsonObj
    }

    fun simplePostRequest(url: String): String {
        val request = Request.Builder()
            .post(formBody)
            .headers(mAuthHeader)
            .url(url)
            .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }

    fun simplePutRequest(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .put(formBody)
            .headers(mAuthHeader)
            .url(url)
            .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }

    fun searchForItems(q: String, limit: Int, type: String): JSONObject{
        val url = "https://api.spotify.com/v1/search?q=$q&type=$type&limit=$limit"
        return simpleGetRequest(url)
    }

    fun addToQueue(id: String){
        val url = "https://api.spotify.com/v1/me/player/queue?uri=spotify%3Atrack%3A$id"
        simplePostRequest(url)
    }

    fun getCurrentlyPlayingTrack(): JSONObject {
        val url = "https://api.spotify.com/v1/me/player/currently-playing"
        return simpleGetRequest(url)
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

    fun setVolume(volume: Int){
        val url = "https://api.spotify.com/v1/me/player/volume?volume_percent=$volume"
        simplePutRequest(url)
    }
}