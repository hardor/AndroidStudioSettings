package ru.profapp.RanobeReader.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.profapp.RanobeReader.Activities.ChapterTextActivity
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R

class ChapterRecyclerViewAdapter(private val context: Context, private val mValues: List<Chapter>, private val mRanobe: Ranobe) : RecyclerView.Adapter<ChapterRecyclerViewAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val clickListener = object : OnItemClickListener {
        override fun onItemClick(item: Chapter) {
            if (item.canRead) {
                if (MyApp.ranobe == null || item.ranobeUrl != MyApp.ranobe!!.url) {

                    var ranobe = Ranobe()
                    ranobe.url = item.ranobeUrl
                    if (MyApp.fragmentType != null && MyApp.fragmentType != Constants.FragmentType.Saved) {
                        try {
                            ranobe = ranobe.updateRanobe(context).blockingGet()
                        } catch (ignored: Exception) {
                            ranobe = mRanobe
                        }

                    } else {
                        ranobe = mRanobe
                    }

                    MyApp.ranobe = ranobe
                }
                if (MyApp.ranobe != null) {
                    val intent = Intent(context, ChapterTextActivity::class.java)
                    intent.putExtra("ChapterIndex", item.index)
                    context.startActivity(intent)
                }
            }

        }

    }

    interface OnItemClickListener {
        fun onItemClick(item: Chapter)
    }


    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ChapterRecyclerViewAdapter.ViewHolder {

        val view = inflater.inflate(R.layout.item_chapter, parent, false)
        return ViewHolder(view)

    }


    override fun onBindViewHolder(@NonNull holder: ChapterRecyclerViewAdapter.ViewHolder, position: Int) {

        holder.mChapterItem = mValues[position]
        holder.mTextView.text = mValues[position].title

        if (!holder.mChapterItem.canRead) {
            holder.mView.setBackgroundColor(Color.GRAY)
        }

        if (holder.mChapterItem.isRead) {
            holder.mView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
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
                    clickListener.onItemClick(mChapterItem)
                }
            }

        }

    }

}
