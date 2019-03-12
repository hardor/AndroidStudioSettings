package ru.profapp.ranobe.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.schedulers.Schedulers
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.activities.ChapterTextActivity
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.helpers.launchActivity
import ru.profapp.ranobe.models.Chapter
import ru.profapp.ranobe.models.Ranobe

class ChapterRecyclerViewAdapter(private val mValues: List<Chapter>, private val mRanobe: Ranobe) : RecyclerView.Adapter<ChapterRecyclerViewAdapter.ViewHolder>() {

    private val clickListener = object : OnItemClickListener {
        override fun onItemClick(mContext: Context, item: Chapter) {
            if (item.canRead) {
                if (MyApp.ranobe?.wasUpdated != true || !MyApp.ranobe!!.url.contains(item.ranobeUrl)) {

                    val ranobe = Ranobe()
                    ranobe.url = item.ranobeUrl
                    if (MyApp.fragmentType != null && MyApp.fragmentType != Constants.FragmentType.Saved) {

                        if (ranobe.updateRanobe(mContext).subscribeOn(Schedulers.io()).blockingGet())
                            MyApp.ranobe = ranobe
                        else {
                            MyApp.ranobe = mRanobe
                        }

                    } else {
                        MyApp.ranobe = mRanobe
                    }

                }
                if (MyApp.ranobe != null) {
                    mContext.launchActivity<ChapterTextActivity> {
                        putExtra("ChapterUrl", item.url)
                    }
                }
            }

        }

    }

    interface OnItemClickListener {
        fun onItemClick(mContext: Context, item: Chapter)
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ChapterRecyclerViewAdapter.ViewHolder {

        val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_chapter, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ChapterRecyclerViewAdapter.ViewHolder, position: Int) {

        val mContext = holder.itemView.context

        holder.mChapterItem = mValues[position]
        holder.mTextView.text = mValues[position].title

        if (!holder.mChapterItem.canRead) {
            holder.mView.setBackgroundColor(Color.GRAY)
        }

        if (holder.mChapterItem.isRead) {
            holder.mView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))
        }

    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mTextView: TextView = mView.findViewById(R.id.id)
        lateinit var mChapterItem: Chapter

        init {

            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(mView.context, mChapterItem)
                }
            }

        }

    }

}
