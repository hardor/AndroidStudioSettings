package ru.profapp.RanobeReader.Adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

import java.util.Date

import ru.profapp.RanobeReader.JsonApi.Rulate.RulateComment
import ru.profapp.RanobeReader.R
import android.R.attr.resource
import android.graphics.drawable.BitmapDrawable
import android.util.TypedValue


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

        val dp50=   TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, holder.context.resources.displayMetrics).toInt()
        Glide.with(holder.context)
                .load(mValues[position].avatar).apply(
                        RequestOptions()
                                .placeholder(R.drawable.ic_adb_black_24dp)
                                .error(R.drawable.ic_error_outline_black_24dp)
                                .fitCenter()
                ).into(object : SimpleTarget<Drawable>(dp50, dp50) {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        holder.bodyTextView.setCompoundDrawablesWithIntrinsicBounds(resource, null, null, null);
                    }
                })
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bodyTextView: TextView = itemView.findViewById(R.id.body)
        val dateView: TextView = itemView.findViewById(R.id.date)
        val context: Context = itemView.context
        lateinit var item: RulateComment

    }
}
