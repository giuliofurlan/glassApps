package com.giufu.youtube_explorer


import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import com.google.android.glass.widget.CardBuilder
import com.google.android.glass.widget.CardScrollAdapter
import com.google.android.glass.widget.CardScrollView
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

//alternative youtube api
//https://github.com/PierfrancescoSoffritti/android-youtube-player#minsdk
class ResultsAcivity : Activity() {
    private var mCards: List<CardBuilder>? = null
    private var mCardScrollView: CardScrollView? = null
    private var mAdapter: ExampleCardScrollAdapter? = null
    private val ids = java.util.ArrayList<String>()
    var q = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        q = intent.getStringExtra("query")
        doAsync {
            createCards()
        }.execute().get()
        mCardScrollView = CardScrollView(this)
        mAdapter = ExampleCardScrollAdapter()
        mCardScrollView!!.setAdapter(mAdapter)
        mCardScrollView!!.activate()
        setContentView(mCardScrollView)
    }

    private fun createCards() {
        //request
        val client = OkHttpClient()
        val  url = URL("${YoutubeConfig.getApiUrl()}$q")
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body()!!.string()
        val jsonObj = JSONObject(responseBody)
        val items: JSONArray = jsonObj.getJSONArray("items")
        //cards
        mCards = ArrayList()
        for (i in 0 until items.length()) {
            val title = items.getJSONObject(i)
                .getJSONObject("snippet")
                .getString("title")
            val id = items.getJSONObject(i)
                .getJSONObject("id")
                .getString("videoId")
            val thumbnail = items.getJSONObject(i)
                .getJSONObject("snippet")
                .getJSONObject("thumbnails")
                .getJSONObject("medium")
                .getString("url")
            val cover: Drawable? = drawableFromUrl(thumbnail)
            ids.add(id)
            (mCards as ArrayList<CardBuilder>).add(
                CardBuilder(this, CardBuilder.Layout.CAPTION)
                    .setText(title)
                    .setFootnote("I'm the footer!")
                    .setTimestamp("just now")
                    .addImage(cover)
            )
        }
    }

    fun drawableFromUrl(url: String?): Drawable? {
        val x: Bitmap
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        val input: InputStream = connection.getInputStream()
        x = BitmapFactory.decodeStream(input)
        return BitmapDrawable(Resources.getSystem(), x)
    }

    inner class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            var i = mCardScrollView!!.getSelectedItemPosition();
            val intent = Intent(this, VideoActivity::class.java)
            intent.putExtra("id", ids.get(i))
            startActivity(intent)
            return true;
        }
        return super.onKeyDown(keyCode, event)
    }

    private inner class ExampleCardScrollAdapter : CardScrollAdapter() {
        override fun getPosition(item: Any): Int {
            return mCards!!.indexOf(item)
        }
        override fun getCount(): Int {
            return mCards!!.size
        }
        override fun getItem(position: Int): Any {
            return mCards!!.get(position)
        }
        override fun getViewTypeCount(): Int {
            return CardBuilder.getViewTypeCount()
        }
        override fun getItemViewType(position: Int): Int {
            return mCards!!.get(position).getItemViewType()
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            return mCards!!.get(position).getView(convertView, parent)
        }
    }
}