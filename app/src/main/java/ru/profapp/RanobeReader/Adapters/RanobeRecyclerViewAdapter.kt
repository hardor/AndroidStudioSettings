package ru.profapp.RanobeReader.Adapters

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
import com.bumptech.glide.request.RequestOptions
import ru.profapp.RanobeReader.Activities.RanobeInfoActivity
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Common.Constants.last_chapter_id_Pref
import ru.profapp.RanobeReader.Common.OnLoadMoreListener
import ru.profapp.RanobeReader.Fragments.RanobeListFragment.OnListFragmentInteractionListener
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R
import ru.profapp.RanobeReader.Utils.GlideApp
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [Ranobe] and makes a call to the specified
 * [OnListFragmentInteractionListener].
 */
class RanobeRecyclerViewAdapter(private val context: Context, recyclerView: RecyclerView, private val mValues: List<Ranobe>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_GROUP_TITLE = 2
    private val glide: RequestManager = GlideApp.with(context)

    private val sPref = context.getSharedPreferences(last_chapter_id_Pref, Context.MODE_PRIVATE)
    private val lastIndexPref = context.getSharedPreferences(Constants.last_chapter_id_Pref, Context.MODE_PRIVATE)

    private val imageOptions: RequestOptions = RequestOptions().placeholder(R.drawable.ic_adb_black_24dp).error(R.drawable.ic_error_outline_black_24dp).fitCenter()
    var onLoadMoreListener: OnLoadMoreListener? = null

    var isLoading: Boolean = false
    var pastVisibleItems: Int = 0
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
                val view = inflater.inflate(R.layout.item_ranobe, parent, false)
                RanobeViewHolder(view)
            }
            VIEW_TYPE_GROUP_TITLE -> {
                val view = inflater.inflate(R.layout.item_title, parent, false)
                TitleViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RanobeViewHolder -> {
                holder.mItem = mValues[position]
                holder.titleView.text = mValues[position].title

                if (mValues[position].readyDate != null) {
                    val diff = ((Date().time - mValues[position].readyDate!!.time) / 1000 / 60)
                    val numOfDays = (diff / (60 * 24)).toInt()
                    val hours = (diff / 60 - numOfDays * 24).toInt()
                    val minutes = (diff % 60).toInt()

                    val updateTime = "${context.getString(R.string.Updated)} ${context.resources.getQuantityString(R.plurals.numberOfDays, numOfDays, numOfDays)} ${context.resources.getQuantityString(R.plurals.numberOfHours, hours, hours)} ${context.resources.getQuantityString(R.plurals.numberOfMinutes, minutes, minutes)} ${context.getString(R.string.ago)}"

                    holder.updateTime.text = updateTime
                    holder.updateTime.visibility = View.VISIBLE
                }

                holder.imageView.visibility = View.VISIBLE
                glide.load(mValues[position].image).into(holder.imageView)

                val chapterList = holder.mItem.chapterList


                if (chapterList.isNotEmpty()) {

                    val templist = chapterList.filter { it.canRead }
                    val chapterlist2 = templist.subList(0, Math.min(Constants.chaptersNum, templist.size))
                    var checked = false
                    if (lastIndexPref != null && lastIndexPref.contains(chapterlist2.first().ranobeUrl)) {
                        val lastInd = lastIndexPref.getInt(chapterlist2.first().ranobeUrl, -1)
                        if (lastInd > 0) {
                            checked = true
                            for (chapter in chapterlist2) {
                                if (chapter.id!! <= lastInd) {
                                    chapter.isRead = true
                                }
                            }
                        }

                    }

                    if (sPref != null && !checked) {
                        for (chapter in chapterlist2) {
                            if (!chapter.isRead) {
                                chapter.isRead = sPref.getBoolean(chapter.url, false)
                            }
                        }
                    }

                    val adapter = ChapterRecyclerViewAdapter(context, chapterlist2, holder.mItem)
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

    inner class RanobeViewHolder(mView: View) : RecyclerView.ViewHolder(mView), View.OnClickListener {

        val imageView: ImageView = mView.findViewById(R.id.imageView)
        val chaptersListView: RecyclerView = mView.findViewById(R.id.list_chapter_list)
        val titleView: TextView = mView.findViewById(R.id.ranobeTitle)
        val updateTime: TextView = mView.findViewById(R.id.ranobeUpdateTime)
        val description: TextView = mView.findViewById(R.id.ranobe_description)
        lateinit var mItem: Ranobe

        init {
            chaptersListView.setHasFixedSize(true)
            chaptersListView.layoutManager = LinearLayoutManager(context)
            mView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (adapterPosition != RecyclerView.NO_POSITION) {

                val intent = Intent(context, RanobeInfoActivity::class.java)
                MyApp.ranobe = mItem
                if (MyApp.ranobe != null) {
                    context.startActivity(intent)
                }

            }
        }

    }

}
