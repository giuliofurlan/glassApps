/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.giufu.youtube_enterprise

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.StrictMode
import android.text.format.DateUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.giufu.youtube_enterprise.BaseActivity
import com.giufu.youtube_enterprise.fragments.BaseFragment
import androidx.viewpager.widget.ViewPager
import com.giufu.youtube_enterprise.MainActivity.ScreenSlidePagerAdapter
import com.giufu.youtube_enterprise.fragments.MainLayoutFragment
import com.giufu.youtube_enterprise.fragments.ColumnLayoutFragment
import com.google.android.material.tabs.TabLayout
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main activity of the application. It provides viewPager to move between fragments.
 */
class MainActivity : BaseActivity() {
    private val fragments: MutableList<BaseFragment> = ArrayList()
    private lateinit var viewPager: ViewPager
    val inputFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    var q = "rap god"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = StrictMode.ThreadPolicy.Builder()
            .permitAll().build()
        StrictMode.setThreadPolicy(policy)
        setContentView(R.layout.view_pager_layout)
        val screenSlidePagerAdapter = ScreenSlidePagerAdapter(
            supportFragmentManager
        )
        viewPager = findViewById(R.id.viewPager)
        viewPager.setAdapter(screenSlidePagerAdapter)

        doAsync {
            createCards()
        }.execute().get()
        screenSlidePagerAdapter.notifyDataSetChanged()
        val tabLayout = findViewById<TabLayout>(R.id.page_indicator)
        tabLayout.setupWithViewPager(viewPager, true)
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
            val channel = items.getJSONObject(i)
                .getJSONObject("snippet")
                .getString("channelTitle")
            val timestamp = items.getJSONObject(i)
                .getJSONObject("snippet")
                .getString("publishTime")
            val dateStr = timestamp
            val date: Date = inputFormat.parse(dateStr)
            val niceDateStr: String = DateUtils.getRelativeTimeSpanString(
                date.time,
                Calendar.getInstance().timeInMillis,
                DateUtils.MINUTE_IN_MILLIS
            ) as String
            fragments.add(
                ColumnLayoutFragment
                    .newInstance(
                        //devo trovare un modo per convertire drawable in int o bypassare il tutto
                        thumbnail, title, channel,
                        niceDateStr
                    )
            )


        }
    }

    inner class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
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

    override fun onGesture(gesture: GlassGestureDetector.Gesture): Boolean {
        return when (gesture) {
            GlassGestureDetector.Gesture.TAP -> {
                fragments[viewPager!!.currentItem].onSingleTapUp()
                true
            }
            else -> super.onGesture(gesture)
        }
    }

    private inner class ScreenSlidePagerAdapter internal constructor(fm: FragmentManager?) :
        FragmentStatePagerAdapter(
            fm!!
        ) {
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }
}