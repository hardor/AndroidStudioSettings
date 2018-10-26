package ru.profapp.RanobeReader.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.profapp.RanobeReader.Models.ChapterHistory
import ru.profapp.RanobeReader.R
import java.text.DateFormat
import java.util.*

class HistoryRecyclerViewAdapter(private val context: Context, private val mValues: List<ChapterHistory>) : RecyclerView.Adapter<HistoryRecyclerViewAdapter.MyViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = inflater.inflate(R.layout.item_history, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.item = mValues[position]
        holder.chTextView.text = holder.item.chapterName

        holder.rTextView.text = "${DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault()).format(holder.item.readDate)} ${holder.item.ranobeName}"

    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chTextView: TextView = itemView.findViewById(R.id.chapterTitle)
        val rTextView: TextView = itemView.findViewById(R.id.ranobeTitleWithTime)
        lateinit var item: ChapterHistory

    }
}