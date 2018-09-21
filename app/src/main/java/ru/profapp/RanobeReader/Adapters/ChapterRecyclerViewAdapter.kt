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
import ru.profapp.RanobeReader.Common.RanobeConstants
import ru.profapp.RanobeReader.Helpers.RanobeKeeper
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R

class ChapterRecyclerViewAdapter(private val mValues: List<Chapter>, private val mRanobe: Ranobe) : RecyclerView.Adapter<ChapterRecyclerViewAdapter.ViewHolder>() {


    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ChapterRecyclerViewAdapter.ViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chapter, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(@NonNull holder: ChapterRecyclerViewAdapter.ViewHolder, position: Int) {


        holder.mChapterItem = mValues[position]
        holder.mTextView.text = mValues[position].title

        if (!holder.mChapterItem.canRead) {
            holder.mView.setBackgroundColor(Color.GRAY)
        }

        if (holder.mChapterItem.isRead) {
            holder.mView.setBackgroundColor(ContextCompat.getColor(holder.context, R.color.colorPrimaryDark))
        }


    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val context: Context = mView.context
        val mTextView: TextView = mView.findViewById(R.id.id)
        private val mContext: Context = mView.context
        lateinit var mChapterItem: Chapter

        init {
            mView.setOnClickListener { v ->
                if (mChapterItem.canRead) {
                    if (MyApp.ranobe == null || mChapterItem.ranobeUrl != MyApp.ranobe!!.url) {

                        var ranobe = Ranobe()
                        ranobe.url = mChapterItem.ranobeUrl
                        if (RanobeKeeper.fragmentType != null && RanobeKeeper.fragmentType != RanobeConstants.FragmentType.Saved) {
                            try {
                                ranobe.updateRanobe(mContext)
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
                        intent.putExtra("ChapterIndex", adapterPosition)
                        context.startActivity(intent)
                    }
                }
            }

        }

    }

}
