package ru.profapp.RanobeReader.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.R

class DownloadRecyclerViewAdapter(private val values: List<Chapter>) : RecyclerView.Adapter<DownloadRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chapter_item_download, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadRecyclerViewAdapter.ViewHolder, position: Int) {
        holder.chapterItem = values[position]
        holder.checkBox.text = holder.chapterItem.title

        if (holder.chapterItem.canRead) {
            holder.checkBox.isEnabled = true

            holder.checkBox.isChecked = holder.chapterItem.isChecked
            if (holder.chapterItem.downloaded) {
                holder.checkBox.setTextColor(holder.context.resources.getColor(R.color.colorAccent))
            } else {
                holder.checkBox.setTextColor(holder.context.resources.getColor(R.color.webViewText))
            }

            holder.checkBox.setOnClickListener { v ->

                holder.checkBox.isChecked = !holder.chapterItem.isChecked
                holder.chapterItem.isChecked = !holder.chapterItem.isChecked

            }
        } else {
            holder.checkBox.isEnabled = false
        }

    }

    override fun getItemCount(): Int {
        return values.size
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val checkBox: CheckBox = mView.findViewById(R.id.checkBox)
        val context: Context = mView.context
        lateinit var chapterItem: Chapter

    }
}
