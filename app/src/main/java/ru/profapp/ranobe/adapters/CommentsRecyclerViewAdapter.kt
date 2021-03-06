package ru.profapp.ranobe.adapters

import android.graphics.drawable.Drawable
import android.text.Html
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import ru.profapp.ranobe.R
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.helpers.GlideRequests
import ru.profapp.ranobe.helpers.setVectorForPreLollipop
import ru.profapp.ranobe.models.Comment
import java.text.DateFormat
import java.util.*

class CommentsRecyclerViewAdapter(private val glide: GlideRequests,
                                  private val mValues: List<Comment>) :
    RecyclerView.Adapter<CommentsRecyclerViewAdapter.MyViewHolder>() {

    companion object {
        private val TAG = "Comments RecyclerView Adapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): CommentsRecyclerViewAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentsRecyclerViewAdapter.MyViewHolder, position: Int) {

        val mContext = holder.itemView.context
        holder.item = mValues[position]
        holder.bodyTextView.text = Html.fromHtml(holder.item.comment)
        holder.item.createdAt?.let {date->
            holder.dateView.text = String.format("%s %s",
                holder.item.name,
                DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                    DateFormat.SHORT,
                    Locale.getDefault()).format(Date(date.times(1000))))
        }
        val dp50 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            50f,
            mContext.resources.displayMetrics).toInt()
        glide.load(mValues[position].image).into(object : SimpleTarget<Drawable>(dp50, dp50) {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                setVectorForPreLollipop(holder.bodyTextView,
                    resource,
                    Constants.ApplicationConstants.DRAWABLE_LEFT)
            }
        })
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bodyTextView: TextView = itemView.findViewById(R.id.tV_comment)
        val dateView: TextView = itemView.findViewById(R.id.date)
        lateinit var item: Comment

    }
}
