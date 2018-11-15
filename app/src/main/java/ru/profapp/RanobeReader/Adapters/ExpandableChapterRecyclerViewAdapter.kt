package ru.profapp.RanobeReader.Adapters

import android.content.Context
import android.content.Intent
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
import ru.profapp.RanobeReader.Activities.ChapterTextActivity
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Helpers.Helper
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.Models.Ranobe
import ru.profapp.RanobeReader.MyApp
import ru.profapp.RanobeReader.R

class ExpandableChapterRecyclerViewAdapter(private val context: Context, private val mRanobe: Ranobe) : RecyclerView.Adapter<ExpandableChapterRecyclerViewAdapter.GroupViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    val dp2 = Helper.convertDpToPixel(2, context)
    val dp6 = Helper.convertDpToPixel(6, context)

    constructor(context: Context, mChapters: ArrayList<Chapter>, mRanobe: Ranobe) : this(context, mRanobe) {

        val numInGroup = 100
        if (mChapters.size > 0) {
            val num = Math.ceil((mChapters.size).toDouble() / numInGroup).toInt()
            for (i in 0 until num) {
                val parentDataItem = ParentDataItem("${mChapters[minOf((i + 1) * numInGroup, mChapters.size - 1)].title} - ${mChapters[minOf(i * numInGroup, mChapters.size - 1)].title}", (mChapters.subList(i * numInGroup, minOf((i + 1) * numInGroup, mChapters.size))))
                parentDataItem.canRead = parentDataItem.childDataItems.any { it -> it.canRead }
                parentDataItems.add(parentDataItem)
            }
        }
    }

    private val parentDataItems: ArrayList<ParentDataItem> = ArrayList()

    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ExpandableChapterRecyclerViewAdapter.GroupViewHolder {
        val view = inflater.inflate(R.layout.item_parent_child_listing, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: ExpandableChapterRecyclerViewAdapter.GroupViewHolder, position: Int) {

        val parentDataItem = parentDataItems[position]
        holder.textViewParentName.text = parentDataItem.parentName
        if (!parentDataItem.canRead)
            holder.textViewParentName.setBackgroundColor(Color.GRAY)

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
            if (!childItem.canRead) {
                currentTextView.setBackgroundColor(Color.GRAY)
            } else {

                currentTextView.setOnClickListener {
                    if (MyApp.ranobe?.wasUpdated != true || !MyApp.ranobe!!.url.contains(childItem.ranobeUrl)) {

                        val ranobe = Ranobe()
                        ranobe.url = childItem.ranobeUrl
                        if (MyApp.fragmentType != null && MyApp.fragmentType != Constants.FragmentType.Saved) {
                            if (ranobe.updateRanobe(context).subscribeOn(Schedulers.io()).blockingGet())
                                MyApp.ranobe = ranobe
                            else {
                                MyApp.ranobe = mRanobe
                            }

                        } else {
                            MyApp.ranobe = mRanobe
                        }
                    }
                    if (MyApp.ranobe != null) {
                        val intent = Intent(context, ChapterTextActivity::class.java)
                        intent.putExtra("ChapterUrl", childItem.url)

                        context.startActivity(intent)
                    }
                }
            }

            if (childItem.isRead) {
                currentTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
            }

        }

    }

    override fun getItemCount(): Int {
        return parentDataItems.size
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

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
                val textView = TextView(context)
                textView.id = indexView
                textView.setPadding(dp2, dp6, dp2, dp6)
                textView.textSize = 14.0F
                textView.ellipsize = android.text.TextUtils.TruncateAt.END
                textView.maxLines = 1
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(0, dp2, 0, dp2)
                linearLayoutChildItems.addView(textView, layoutParams)
            }
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

