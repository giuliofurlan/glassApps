package com.giufu.spotify


import android.annotation.SuppressLint
import android.app.Activity
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
import org.json.JSONArray
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class ResultsActivity : Activity() {
    private var mCards: List<CardBuilder>? = null
    private var mCardScrollView: CardScrollView? = null
    private var mAdapter: ExampleCardScrollAdapter? = null
    private val ids = ArrayList<String>()
    private lateinit var spotifyAPI: SpotifyAPI
    private var q = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        q = intent.getStringExtra("query")
        DoAsync {
            createCards()
        }.execute().get()
        mCardScrollView = CardScrollView(this)
        mAdapter = ExampleCardScrollAdapter()
        mCardScrollView!!.adapter = mAdapter
        mCardScrollView!!.activate()
        setContentView(mCardScrollView)
    }

    private fun createCards() {
        //request
        spotifyAPI = SpotifyAPI(SpotifyAPI.oauth)
        val responseBody = spotifyAPI.searchForItems(q,5, "track")
        val jsonObj = (responseBody).getJSONObject("tracks")
        val items: JSONArray = jsonObj.getJSONArray("items")
        //cards
        mCards = ArrayList()
        for (i in 0 until items.length()) {
            val title = items.getJSONObject(i)
                .getString("name")
            val id = items.getJSONObject(i)
                .getString("id")
            val thumbnail = items.getJSONObject(i)
                .getJSONObject("album")
                .getJSONArray("images")
                .getJSONObject(1)//0-2 0:640 1:300 2:64
                .getString("url")
            val cover: Drawable = drawableFromUrl(thumbnail)
            val artistsJson = items.getJSONObject(i)
                .getJSONArray("artists")
            var artists = artistsJson.getJSONObject(0).getString("name")
            if (artistsJson.length() > 1){
                for (e in 1 until artistsJson.length())
                    artists += ", ${artistsJson.getJSONObject(e).getString("name")}"
            }
            val timestamp = items.getJSONObject(i)
                .getString("duration_ms")
            val duration = millisToMinutesAndSeconds(timestamp.toLong())
            ids.add(id)
            (mCards as ArrayList<CardBuilder>).add(
                CardBuilder(this, CardBuilder.Layout.COLUMNS)
                    .setText(title)
                    .setFootnote(artists)
                    .setTimestamp(duration)
                    .addImage(cover)
            )
        }
    }

    private fun millisToMinutesAndSeconds(milliSeconds: Long): String {
        val s: Long = milliSeconds / 1000 % 60
        val m: Long = milliSeconds / (1000*60) % 60
        return String.format("%02d:%02d", m, s)
    }

    private fun drawableFromUrl(url: String?): Drawable {
        val x: Bitmap
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        val input: InputStream = connection.inputStream
        x = BitmapFactory.decodeStream(input)
        return BitmapDrawable(Resources.getSystem(), x)
    }

    @SuppressLint("StaticFieldLeak")
    inner class DoAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            val i = mCardScrollView!!.selectedItemPosition
            spotifyAPI.addToQueue(ids[i])
            spotifyAPI.skipToNext()
            finish()
            return true
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
            return mCards!![position]
        }
        override fun getViewTypeCount(): Int {
            return CardBuilder.getViewTypeCount()
        }
        override fun getItemViewType(position: Int): Int {
            return mCards!![position].itemViewType
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            return mCards!![position].getView(convertView, parent)
        }
    }
}