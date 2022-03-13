package com.giufu.youtube_explorer

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.google.android.glass.widget.CardBuilder
import com.google.android.glass.widget.CardScrollAdapter
import com.google.android.glass.widget.CardScrollView
//better youtube api
//https://github.com/nicholaschum/android-youtube-player
class MainActivity : Activity() {

    private var mCards: List<CardBuilder>? = null
    private var mCardScrollView: CardScrollView? = null
    private var mAdapter: ExampleCardScrollAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createCards()
        mCardScrollView = CardScrollView(this)
        mAdapter = ExampleCardScrollAdapter()
        mCardScrollView!!.setAdapter(mAdapter)
        mCardScrollView!!.activate()
        setContentView(mCardScrollView)
    }

    private fun createCards() {
        mCards = ArrayList()
        (mCards as ArrayList<CardBuilder>).add(
            CardBuilder(this, CardBuilder.Layout.TEXT)
                .setText("This card has a footer.")
                .setFootnote("I'm the footer!")
        )

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