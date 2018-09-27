package ru.profapp.RanobeReader.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.profapp.RanobeReader.Models.ChapterHistory
import ru.profapp.RanobeReader.R
import java.text.SimpleDateFormat
import java.util.*

class HistoryRecyclerViewAdapter(private val mValues: List<ChapterHistory>) : RecyclerView.Adapter<HistoryRecyclerViewAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.item = mValues[position]
        holder.chTextView.text = "${holder.item.chapterName} ${holder.item.progress}"
        holder.rTextView.text = "${SimpleDateFormat(holder.itemView.context.getString(R.string.date_format)).format(holder.item.readDate)} ${holder.item.ranobeName}"

    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chTextView: TextView = itemView.findViewById(R.id.chapterTitle)
        val rTextView: TextView = itemView.findViewById(R.id.ranobeTitleWithTime)
        lateinit var item: ChapterHistory


    }
}