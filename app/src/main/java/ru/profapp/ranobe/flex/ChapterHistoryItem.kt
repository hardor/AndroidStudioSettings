package ru.profapp.ranobe.flex

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import ru.profapp.ranobe.R
import ru.profapp.ranobe.models.ChapterHistory
import java.text.DateFormat
import java.util.*



class ChapterHistoryItem(val mItem:ChapterHistory) : AbstractFlexibleItem<ChapterHistoryItem.MyViewHolder>() {

    @SuppressLint("SetTextI18n")
    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: MyViewHolder,
                                position: Int,
                                payloads: MutableList<Any>) {

        holder.chTextView.text = mItem.chapterName

        holder.rTextView.text = "${DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
            DateFormat.SHORT,
            Locale.getDefault()).format(mItem.readDate)} ${mItem.ranobeName}"
    }

    override fun equals(other: Any?): Boolean {
        if (other is ChapterHistoryItem) {
            return this.mItem == other.mItem
        }
        return false
    }

    override fun createViewHolder(view: View,
                                  adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): MyViewHolder {
        return MyViewHolder(view, adapter)
    }

    override fun getLayoutRes(): Int {
        return R.layout.item_history;
    }

    override fun hashCode(): Int {
        return mItem.hashCode()
    }


    inner class MyViewHolder(view: View,adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>) : FlexibleViewHolder(view,adapter) {
        val chTextView: TextView = itemView.findViewById(R.id.chapterTitle)
        val rTextView: TextView = itemView.findViewById(R.id.ranobeTitleWithTime)
    }
}
