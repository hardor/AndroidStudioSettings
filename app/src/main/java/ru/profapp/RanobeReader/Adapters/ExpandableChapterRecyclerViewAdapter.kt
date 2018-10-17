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

    constructor(context: Context, mChapters: ArrayList<Chapter>, mRanobe: Ranobe) : this(context, mRanobe) {

        val numInGroup = 100
        val num = mChapters.size / numInGroup
        for (i in 0..num) {
            val parentDataItem = ParentDataItem("${mChapters[minOf((i + 1) * numInGroup, mChapters.size - 1)].title} - ${mChapters[minOf(i * numInGroup, mChapters.size - 1)].title}", (mChapters.subList(i * numInGroup, minOf((i + 1) * numInGroup, mChapters.size))))
            parentDataItems.add(parentDataItem)
        }

    }

    private val parentDataItems: ArrayList<ParentDataItem> = ArrayList()

    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ExpandableChapterRecyclerViewAdapter.GroupViewHolder {
        val view = inflater.inflate(R.layout.item_parent_child_listing, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: ExpandableChapterRecyclerViewAdapter.GroupViewHolder, position: Int) {

        val parentDataItem = parentDataItems[position]
        holder.textView_parentName.text = parentDataItem.parentName

        val noOfChildTextViews = holder.linearLayout_childItems.childCount
        val noOfChild = parentDataItem.childDataItems
        if (noOfChild.size < noOfChildTextViews) {
            for (index in noOfChild.size until noOfChildTextViews) {
                val currentTextView = holder.linearLayout_childItems.getChildAt(index) as TextView
                currentTextView.visibility = View.GONE
            }
        }
        for ((textViewIndex, childItem) in noOfChild.withIndex()) {
            val currentTextView = holder.linearLayout_childItems.getChildAt(textViewIndex) as TextView
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

            if (childItem.canRead && childItem.isRead) {
                currentTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
            }

        }

    }

    override fun getItemCount(): Int {
        return parentDataItems.size
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val textView_parentName: TextView = itemView.findViewById(R.id.tv_parent_name)
        val linearLayout_childItems: LinearLayout = itemView.findViewById(R.id.ll_child_items)

        init {
            linearLayout_childItems.visibility = View.GONE
            var intMaxNoOfChild = 0
            for (index in 0 until parentDataItems.size) {
                val intMaxSizeTemp = parentDataItems[index].childDataItems.size
                if (intMaxSizeTemp > intMaxNoOfChild) intMaxNoOfChild = intMaxSizeTemp
            }
            for (indexView in 0 until intMaxNoOfChild) {
                val textView = TextView(context)
                textView.id = indexView
                val dp2 = Helper.convertDpToPixel(2, context)
                val dp6 = Helper.convertDpToPixel(6, context)
                textView.setPadding(dp2, dp6, dp2, dp6)
                textView.textSize = 14.0F
                textView.ellipsize = android.text.TextUtils.TruncateAt.END
                textView.maxLines = 1
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                linearLayout_childItems.addView(textView, layoutParams)
            }
            textView_parentName.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (view.id == R.id.tv_parent_name) {
                if (linearLayout_childItems.visibility == View.VISIBLE) {
                    linearLayout_childItems.visibility = View.GONE
                } else {
                    linearLayout_childItems.visibility = View.VISIBLE
                }
            }

        }
    }

}

