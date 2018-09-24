package ru.profapp.RanobeReader.Adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import java.util.Date

import ru.profapp.RanobeReader.JsonApi.Rulate.RulateComment
import ru.profapp.RanobeReader.R

class CommentsRecyclerViewAdapter(private val mValues: List<RulateComment>) : RecyclerView.Adapter<CommentsRecyclerViewAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsRecyclerViewAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return MyViewHolder(view)
    }


    override fun onBindViewHolder(@NonNull holder: CommentsRecyclerViewAdapter.MyViewHolder, position: Int) {
        holder.item = mValues[position]
        holder.bodyTextView.text = holder.item.body
        if (holder.item.time != null)
            holder.dateView.text = String.format("%s %s", holder.item.author, DateFormat.getDateFormat(holder.context).format(Date(holder.item.time!!.times(1000))))

        Glide.with(holder.context)
                .load(mValues[position].avatar).apply(
                        RequestOptions()
                                .placeholder(R.drawable.ic_adb_black_24dp)
                                .error(R.drawable.ic_error_outline_black_24dp)
                                .fitCenter()
                ).into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bodyTextView: TextView = itemView.findViewById(R.id.body)
        val dateView: TextView = itemView.findViewById(R.id.date)
        val imageView: ImageView = itemView.findViewById(R.id.icon)
        val context: Context = itemView.context
        lateinit var item: RulateComment

    }
}
