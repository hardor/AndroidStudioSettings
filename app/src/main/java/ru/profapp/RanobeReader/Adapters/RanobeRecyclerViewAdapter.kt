package ru.profapp.RanobeReader.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import ru.profapp.RanobeReader.Activities.RanobeInfoActivity
import ru.profapp.RanobeReader.Common.OnLoadMoreListener
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Fragments.RanobeRecyclerFragment.OnListFragmentInteractionListener
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [Ranobe] and makes a call to the specified
 * [OnListFragmentInteractionListener].
 */
class RanobeRecyclerViewAdapter(recyclerView: RecyclerView, private val mValues: List<Ranobe>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_GROUP_TITLE = 2
    var onLoadMoreListener: OnLoadMoreListener? = null

    var isLoading: Boolean = false
    var pastVisiblesItems: Int = 0
    var lastVisibleItem: Int = 0
    var totalItemCount: Int = 0

    init {

        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                if (dy > 0) {
                    lastVisibleItem = linearLayoutManager.childCount
                    totalItemCount = linearLayoutManager.itemCount
                    pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition()

                    if (!isLoading) {
                        if (lastVisibleItem + pastVisiblesItems >= totalItemCount) {

                            if (onLoadMoreListener != null) {
                                isLoading = true
                                onLoadMoreListener!!.onLoadMore()
                            } else {
                                isLoading = false
                            }
                        }
                    }
                }
            }
        })

    }

    override fun getItemViewType(position: Int): Int {
        return  if (mValues[position].ranobeSite == Constants.RanobeSite.Title.url) {
            VIEW_TYPE_GROUP_TITLE
        } else {
            VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranobe,
                        parent, false)
                return RanobeViewHolder(view)
            }
            VIEW_TYPE_GROUP_TITLE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_title,
                        parent, false)
                return TitleViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
                return LoadingViewHolder(view)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RanobeViewHolder -> {
                holder.mItem = mValues[position]
                holder.mTitleView.text = mValues[position].title

                if (mValues[position].readyDate != null) {
                    val diff = ((Date().time - mValues[position].readyDate!!.time)
                            / 1000 / 60)

                    val numOfDays = (diff / (60 * 24)).toInt()
                    val hours = (diff / 60 - numOfDays * 24).toInt()
                    val minutes = (diff % 60).toInt()

                    val updateTime = holder.context.getString(R.string.update_time, numOfDays, hours,
                            minutes)

                    holder.mUpdateTime.text = updateTime
                } else {
                    holder.mUpdateTime.visibility = View.INVISIBLE
                }

                holder.mImageView.visibility = View.VISIBLE
                Glide.with(holder.context)
                        .load("yuyuyu").apply(
                                RequestOptions()
                                        .placeholder(R.drawable.ic_adb_black_24dp)
                                        .error(R.drawable.ic_error_outline_black_24dp)
                                        .fitCenter()
                        ).into(holder.mImageView)

                holder.mView.setOnClickListener { v ->

                    val intent = Intent(holder.context, RanobeInfoActivity::class.java)

                    MyApp.ranobe =  holder.mItem

                    if (MyApp.ranobe != null) {
                        holder.mView.context.startActivity(intent)
                    }

                }

                val chapterList = holder.mItem.chapterList

                val adapter = ChapterRecyclerViewAdapter(
                        ArrayList(chapterList.subList(0, Math.min(Constants.chaptersNum, chapterList.size))),
                        holder.mItem)

                val itemDecorator = DividerItemDecoration(holder.context,DividerItemDecoration.VERTICAL)
                itemDecorator.setDrawable(holder.context.resources.getDrawable(R.drawable.divider))
                holder.mChaptersListView.addItemDecoration(itemDecorator)
                holder.mChaptersListView.adapter = adapter

            }
            is LoadingViewHolder -> holder.progressBar.isIndeterminate = true
            is TitleViewHolder -> holder.mTextView.text = mValues[position].title
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    fun setLoaded() {
        isLoading = false
    }


    inner class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)

    }

    inner class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val mTextView: TextView = view.findViewById(R.id.group_title)

    }

    inner class RanobeViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val context: Context = mView.context
        val mImageView: ImageView = mView.findViewById(R.id.imageView)
        val mChaptersListView: RecyclerView = mView.findViewById(R.id.list_chapter_list)
        val mTitleView: TextView = mView.findViewById(R.id.ranobeTitle)
        val mUpdateTime: TextView = mView.findViewById(R.id.ranobeUpdateTime)
        lateinit var mItem: Ranobe

        init {
            mChaptersListView.setHasFixedSize(true)
            mChaptersListView.layoutManager = LinearLayoutManager(context)
        }

    }

}
