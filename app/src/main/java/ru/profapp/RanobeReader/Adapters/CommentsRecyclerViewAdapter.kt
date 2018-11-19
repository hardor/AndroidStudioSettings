package ru.profapp.RanobeReader.Adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Helpers.Helper
import ru.profapp.RanobeReader.Network.DTO.RulateDTO.RulateComment
import ru.profapp.RanobeReader.R
import ru.profapp.RanobeReader.Utils.GlideApp
import ru.profapp.RanobeReader.Utils.GlideRequests
import java.util.*

class CommentsRecyclerViewAdapter(private val mContext: Context, private val mValues: List<RulateComment>) : RecyclerView.Adapter<CommentsRecyclerViewAdapter.MyViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(mContext)
    private val glide: GlideRequests = GlideApp.with(mContext)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsRecyclerViewAdapter.MyViewHolder {
        val view = inflater.inflate(R.layout.item_comment, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: CommentsRecyclerViewAdapter.MyViewHolder, position: Int) {
        holder.item = mValues[position]
        holder.bodyTextView.text = holder.item.body
        if (holder.item.time != null)
            holder.dateView.text = String.format("%s %s", holder.item.author, DateFormat.getDateFormat(mContext).format(Date(holder.item.time!!.times(1000))))

        val dp50 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, mContext.resources.displayMetrics).toInt()
        glide.load(mValues[position].avatar).into(object : SimpleTarget<Drawable>(dp50, dp50) {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                Helper.setVectorForPreLollipop( holder.bodyTextView, resource, mContext, Constants.ApplicationConstants.DRAWABLE_LEFT);
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
