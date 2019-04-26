package ru.profapp.ranobe.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.schedulers.Schedulers
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.activities.ChapterTextActivity
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.helpers.convertDpToPixel
import ru.profapp.ranobe.helpers.launchActivity
import ru.profapp.ranobe.helpers.setVectorForPreLollipop
import ru.profapp.ranobe.models.Chapter
import ru.profapp.ranobe.models.Ranobe

class ExpandableChapterRecyclerViewAdapter(private val mContext: Context,
                                           private val mRanobe: Ranobe) :
    RecyclerView.Adapter<ExpandableChapterRecyclerViewAdapter.GroupViewHolder>() {

    companion object {
        private val TAG = "Expandable Chapter RecyclerView Adapter"
    }

    val dp2 = convertDpToPixel(2, mContext)
    val dp6 = convertDpToPixel(6, mContext)

    constructor(context: Context, mChapters: List<Chapter>, mRanobe: Ranobe) : this(context,
        mRanobe) {

        val numInGroup = 100
        if (mChapters.isNotEmpty()) {
            val num = Math.ceil((mChapters.size).toDouble() / numInGroup).toInt()
            for (i in 0 until num) {
                val parentDataItem = ParentDataItem("${mChapters[minOf(i * numInGroup,
                    mChapters.size - 1)].title} - ${mChapters[minOf((i + 1) * numInGroup - 1,
                    mChapters.size - 1)].title}",
                    (mChapters.subList(i * numInGroup,
                        minOf((i + 1) * numInGroup, mChapters.size))))
                parentDataItem.canRead = parentDataItem.childDataItems.any { it.canRead }
                parentDataItems.add(parentDataItem)
            }
        }
    }

    private val parentDataItems: ArrayList<ParentDataItem> = ArrayList()

    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ExpandableChapterRecyclerViewAdapter.GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_parent_child_listing, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpandableChapterRecyclerViewAdapter.GroupViewHolder,
                                  position: Int) {

        val parentDataItem = parentDataItems[position]
        holder.textViewParentName.text = parentDataItem.parentName
        if (!parentDataItem.canRead) holder.textViewParentName.setBackgroundColor(Color.GRAY)

        val noOfChildTextViews = holder.linearLayoutChildItems.childCount
        val noOfChild = parentDataItem.childDataItems
        if (noOfChild.size < noOfChildTextViews) {
            for (index in noOfChild.size until noOfChildTextViews) {
                val currentTextView = holder.linearLayoutChildItems.getChildAt(index) as TextView
                currentTextView.visibility = View.GONE
            }
        }
        for ((textViewIndex, childItem) in noOfChild.withIndex()) {
            val currentTextView = holder.linearLayoutChildItems.getChildAt(textViewIndex) as TextView
            currentTextView.text = childItem.title
            if (childItem.canRead) {
                currentTextView.setOnClickListener {
                    if (MyApp.ranobe?.wasUpdated != true || !MyApp.ranobe!!.url.contains(childItem.ranobeUrl)) {

                        val ranobe = Ranobe()
                        ranobe.url = childItem.ranobeUrl
                        if (MyApp.fragmentType != null && MyApp.fragmentType != Constants.FragmentType.Saved) {
                            if (ranobe.updateRanobe(mContext).subscribeOn(Schedulers.io()).blockingGet()) MyApp.ranobe = ranobe
                            else {
                                MyApp.ranobe = mRanobe
                            }

                        } else {
                            MyApp.ranobe = mRanobe
                        }
                    }
                    if (MyApp.ranobe != null) {

                        mContext.launchActivity<ChapterTextActivity> {
                            putExtra("ChapterUrl", childItem.url)
                        }

                    }
                }
            }

            if (!childItem.canRead && childItem.isRead) currentTextView.setBackgroundColor(
                ContextCompat.getColor(mContext, R.color.colorPrimaryDarkNest))
            else if (!childItem.canRead) currentTextView.setBackgroundColor(Color.GRAY)
            else if (childItem.isRead) {
                currentTextView.setBackgroundColor(ContextCompat.getColor(mContext,
                    R.color.colorPrimaryDark))
            }

        }

    }

    override fun getItemCount(): Int {
        return parentDataItems.size
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val textViewParentName: TextView = itemView.findViewById(R.id.tv_parent_name)
        val linearLayoutChildItems: LinearLayout = itemView.findViewById(R.id.ll_child_items)

        init {
            linearLayoutChildItems.visibility = View.GONE
            var intMaxNoOfChild = 0
            for (index in 0 until parentDataItems.size) {
                val intMaxSizeTemp = parentDataItems[index].childDataItems.size
                if (intMaxSizeTemp > intMaxNoOfChild) intMaxNoOfChild = intMaxSizeTemp
            }
            for (indexView in 0 until intMaxNoOfChild) {
                val textView = TextView(mContext)
                textView.id = indexView
                textView.setPadding(dp2, dp6, dp2, dp6)
                textView.textSize = 14.0F
                textView.ellipsize = android.text.TextUtils.TruncateAt.END
                textView.maxLines = 1
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(0, dp2, 0, dp2)
                linearLayoutChildItems.addView(textView, layoutParams)
            }
            setVectorForPreLollipop(textViewParentName,
                R.drawable.ic_expand_more_black_24dp,
                mContext,
                Constants.ApplicationConstants.DRAWABLE_RIGHT)
            textViewParentName.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (view.id == R.id.tv_parent_name) {
                if (linearLayoutChildItems.visibility == View.VISIBLE) {
                    linearLayoutChildItems.visibility = View.GONE
                } else {
                    linearLayoutChildItems.visibility = View.VISIBLE
                }
            }

        }
    }

}

