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
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import ru.profapp.RanobeReader.Activities.RanobeInfoActivity
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Common.OnLoadMoreListener
import ru.profapp.RanobeReader.Fragments.RanobeRecyclerFragment.OnListFragmentInteractionListener
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [Ranobe] and makes a call to the specified
 * [OnListFragmentInteractionListener].
 */
class RanobeRecyclerViewAdapter(private val context: Context, recyclerView: RecyclerView, private val mValues: List<Ranobe>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_GROUP_TITLE = 2
    private val glide: RequestManager = Glide.with(context)
    private val imageOptions: RequestOptions = RequestOptions().placeholder(R.drawable.ic_adb_black_24dp).error(R.drawable.ic_error_outline_black_24dp).fitCenter()
    var onLoadMoreListener: OnLoadMoreListener? = null

    var isLoading: Boolean = false
    var pastVisiblesItems: Int = 0
    var lastVisibleItem: Int = 0
    var totalItemCount: Int = 0
    private val inflater: LayoutInflater = LayoutInflater.from(context)

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
        return if (mValues[position].ranobeSite == Constants.RanobeSite.Title.url) {
            VIEW_TYPE_GROUP_TITLE
        } else {
            VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                val view = inflater.inflate(R.layout.item_ranobe,
                        parent, false)
                return RanobeViewHolder(view)
            }
            VIEW_TYPE_GROUP_TITLE -> {
                val view = inflater.inflate(R.layout.item_title,
                        parent, false)
                return TitleViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_loading, parent, false)
                return LoadingViewHolder(view)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RanobeViewHolder -> {
                holder.mItem = mValues[position]
                holder.titleView.text = mValues[position].title

                if (mValues[position].readyDate != null) {
                    val diff = ((Date().time - mValues[position].readyDate!!.time)
                            / 1000 / 60)
                    val numOfDays = (diff / (60 * 24)).toInt()
                    val hours = (diff / 60 - numOfDays * 24).toInt()
                    val minutes = (diff % 60).toInt()

                    val updateTime = "${context.getString(R.string.Updated)} ${context.resources.getQuantityString(R.plurals.numberOfDays, numOfDays, numOfDays)} ${context.resources.getQuantityString(R.plurals.numberOfHours, hours, hours)} ${context.resources.getQuantityString(R.plurals.numberOfMinutes, minutes, minutes)} ${context.getString(R.string.ago)}"

                    holder.updateTime.text = updateTime
                    holder.updateTime.visibility = View.VISIBLE
                }

                holder.imageView.visibility = View.VISIBLE
                glide.load(/*mValues[position].image*/"").apply(
                        imageOptions
                ).into(holder.imageView)

                holder.mView.setOnClickListener { v ->

                    val intent = Intent(context, RanobeInfoActivity::class.java)

                    MyApp.ranobe = holder.mItem

                    if (MyApp.ranobe != null) {
                        holder.mView.context.startActivity(intent)
                    }

                }

                val chapterList = holder.mItem.chapterList
                if (chapterList.isNotEmpty()) {
                    val adapter = ChapterRecyclerViewAdapter(context,
                            ArrayList(chapterList.subList(0, Math.min(Constants.chaptersNum, chapterList.size))),
                            holder.mItem)

                    val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                    itemDecorator.setDrawable(context.resources.getDrawable(R.drawable.divider))
                    holder.chaptersListView.addItemDecoration(itemDecorator)
                    holder.chaptersListView.adapter = adapter
                    holder.chaptersListView.visibility = View.VISIBLE
                } else {
                    if (!holder.mItem.description.isNullOrBlank()) {
                        holder.description.text = holder.mItem.description
                        holder.description.visibility = View.VISIBLE
                    }
                }


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

        val imageView: ImageView = mView.findViewById(R.id.imageView)
        val chaptersListView: RecyclerView = mView.findViewById(R.id.list_chapter_list)
        val titleView: TextView = mView.findViewById(R.id.ranobeTitle)
        val updateTime: TextView = mView.findViewById(R.id.ranobeUpdateTime)
        val description: TextView = mView.findViewById(R.id.ranobe_description)
        lateinit var mItem: Ranobe

        init {
            chaptersListView.setHasFixedSize(true)
            chaptersListView.layoutManager = LinearLayoutManager(context)
        }

    }

}
