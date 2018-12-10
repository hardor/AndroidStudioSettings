package ru.profapp.ranobe.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import ru.profapp.ranobe.activities.RanobeInfoActivity
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.common.OnLoadMoreListener
import ru.profapp.ranobe.models.Ranobe
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [Ranobe] and makes a call to the specified
 */
class RanobeRecyclerViewAdapter(private val glide: RequestManager, recyclerView: RecyclerView, private var mValues: List<Ranobe>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_GROUP_TITLE = 2

    var onLoadMoreListener: OnLoadMoreListener? = null

    var isLoading: Boolean = false
    var pastVisibleItems: Int = 0
    var lastVisibleItem: Int = 0
    var totalItemCount: Int = 0

    init {
        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                if (dy > 0) {
                    lastVisibleItem = linearLayoutManager.childCount
                    totalItemCount = linearLayoutManager.itemCount
                    pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition()

                    if (!isLoading) {
                        if (lastVisibleItem + pastVisibleItems >= totalItemCount) {

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
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater
                        .from(parent.context).inflate(R.layout.item_ranobe, parent, false)
                RanobeViewHolder(view)
            }
            VIEW_TYPE_GROUP_TITLE -> {
                val view = LayoutInflater
                        .from(parent.context).inflate(R.layout.item_title, parent, false)
                TitleViewHolder(view)
            }
            else -> {
                val view = LayoutInflater
                        .from(parent.context).inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mContext = holder.itemView.context
        when (holder) {
            is RanobeViewHolder -> {

                holder.mItem = mValues[position]
                holder.titleView.text = mValues[position].title

                if (mValues[position].readyDate != null) {
                    val diff = ((Date().time - mValues[position].readyDate!!.time) / 1000 / 60)
                    val numOfDays = (diff / (60 * 24)).toInt()
                    val hours = (diff / 60 - numOfDays * 24).toInt()
                    val minutes = (diff % 60).toInt()

                    val updateTime = "${mContext.getString(R.string.Updated)} ${mContext.resources.getQuantityString(R.plurals.numberOfDays, numOfDays, numOfDays)} ${mContext.resources.getQuantityString(R.plurals.numberOfHours, hours, hours)} ${mContext.resources.getQuantityString(R.plurals.numberOfMinutes, minutes, minutes)} ${mContext.getString(R.string.ago)}"

                    holder.updateTime.text = updateTime
                    holder.updateTime.visibility = View.VISIBLE
                }

                holder.imageView.visibility = View.VISIBLE
                glide.load(mValues[position].image).into(holder.imageView)

                when (holder.mItem.ranobeSite) {
                    Constants.RanobeSite.Rulate.url -> holder.ranobeSiteLogo.setImageResource(R.mipmap.ic_rulate)
                    Constants.RanobeSite.RanobeHub.url -> holder.ranobeSiteLogo.setImageResource(R.mipmap.ic_ranobehub)
                    Constants.RanobeSite.RanobeRf.url -> holder.ranobeSiteLogo.setImageResource(R.mipmap.ic_ranoberf)
                    else -> holder.ranobeSiteLogo.visibility = View.GONE

                }
                val chapterList = holder.mItem.chapterList
                if (chapterList.isNotEmpty()) {

                    var templist = chapterList.filter { it.canRead }
                    if (!templist.any()) {
                        templist = chapterList.take(Constants.chaptersNum)
                    }
                    val chapterlist2 = templist.take(Constants.chaptersNum)

                    val lastChapterIdPref = mContext.applicationContext.getSharedPreferences(Constants.last_chapter_id_Pref, Context.MODE_PRIVATE)
                    if (lastChapterIdPref != null && lastChapterIdPref.contains(chapterlist2.first().ranobeUrl)) {
                        val lastId = lastChapterIdPref.getInt(chapterlist2.first().ranobeUrl, -1)
                        if (lastId > 0) {

                            for (chapter in chapterlist2) {
                                if (chapter.id != null)
                                    chapter.isRead = chapter.id!! <= lastId
                            }
                        }

                    }

                    val adapter = ChapterRecyclerViewAdapter(chapterlist2, holder.mItem)
                    holder.chaptersListView.adapter = adapter
                    holder.chaptersListView.visibility = View.VISIBLE
                    holder.description.visibility = View.GONE
                } else {
                    holder.chaptersListView.visibility = View.GONE
                    if (!holder.mItem.description.isNullOrBlank()) {
                        holder.description.text = holder.mItem.description
                        holder.description.visibility = View.VISIBLE
                    } else {
                        holder.description.visibility = View.GONE
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

        val progressBar: ProgressBar = view.findViewById(R.id.progressBar_itemLoading)

    }

    inner class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val mTextView: TextView = view.findViewById(R.id.group_title)

    }

    inner class RanobeViewHolder(mView: View) : RecyclerView.ViewHolder(mView), View.OnClickListener {

        val imageView: ImageView = mView.findViewById(R.id.imageView)
        val ranobeSiteLogo: ImageView = mView.findViewById(R.id.iV_ranobe_siteLogo)
        val chaptersListView: RecyclerView = mView.findViewById(R.id.list_chapter_list)
        val titleView: TextView = mView.findViewById(R.id.ranobeTitle)
        val updateTime: TextView = mView.findViewById(R.id.ranobeUpdateTime)
        val description: TextView = mView.findViewById(R.id.ranobe_description)
        lateinit var mItem: Ranobe

        init {
            chaptersListView.setHasFixedSize(true)
            chaptersListView.layoutManager = LinearLayoutManager(mView.context)
            mView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (adapterPosition != RecyclerView.NO_POSITION) {

                val intent = Intent(v.context, RanobeInfoActivity::class.java)
                MyApp.ranobe = mItem
                if (MyApp.ranobe != null) {
                    v.context.startActivity(intent)
                }

            }
        }

    }

}
