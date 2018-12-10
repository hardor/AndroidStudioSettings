package ru.profapp.ranobe.adapters

import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.helpers.setVectorForPreLollipop
import ru.profapp.ranobe.network.dto.rulateDTO.RulateComment
import ru.profapp.ranobe.R
import ru.profapp.ranobe.utils.GlideRequests
import java.util.*

class CommentsRecyclerViewAdapter(private val glide: GlideRequests, private val mValues: List<RulateComment>) : RecyclerView.Adapter<CommentsRecyclerViewAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsRecyclerViewAdapter.MyViewHolder {
        val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_comment, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentsRecyclerViewAdapter.MyViewHolder, position: Int) {

        val mContext = holder.itemView.context
        holder.item = mValues[position]
        holder.bodyTextView.text = holder.item.body
        if (holder.item.time != null)
            holder.dateView.text = String.format("%s %s", holder.item.author, DateFormat.getDateFormat(mContext).format(Date(holder.item.time!!.times(1000))))

        val dp50 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, mContext.resources.displayMetrics).toInt()
        glide.load(mValues[position].avatar).into(object : SimpleTarget<Drawable>(dp50, dp50) {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                setVectorForPreLollipop(holder.bodyTextView, resource, Constants.ApplicationConstants.DRAWABLE_LEFT)
            }
        })
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bodyTextView: TextView = itemView.findViewById(R.id.tV_comment)
        val dateView: TextView = itemView.findViewById(R.id.date)
        lateinit var item: RulateComment

    }
}
