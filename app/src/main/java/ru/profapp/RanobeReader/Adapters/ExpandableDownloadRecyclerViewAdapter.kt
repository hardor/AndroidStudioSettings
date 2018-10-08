package ru.profapp.RanobeReader.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import ru.profapp.RanobeReader.Models.Chapter
import ru.profapp.RanobeReader.R

class ExpandableDownloadRecyclerViewAdapter(private val context: Context) : RecyclerView.Adapter<ExpandableDownloadRecyclerViewAdapter.GroupViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    public var selectAll: Boolean? = null
    fun pxToDp(x: Int): Int {
        return (context.resources.displayMetrics.density * 6 + 0.5f).toInt()
    }

    constructor(context: Context, mChapters: List<Chapter>) : this(context) {

        val numInGroup = 100
        val num = mChapters.size / numInGroup
        for (i in 0..num) {
            val parentDataItem = ParentDataItem("${mChapters[minOf((i + 1) * numInGroup, mChapters.size - 1)].title} - ${mChapters[minOf(i * numInGroup, mChapters.size - 1)].title}", (mChapters.subList(i * numInGroup, minOf((i + 1) * numInGroup, mChapters.size))))
            parentDataItems.add(parentDataItem)
        }

    }

    private val parentDataItems: ArrayList<ParentDataItem> = ArrayList()

    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ExpandableDownloadRecyclerViewAdapter.GroupViewHolder {
        val view = inflater.inflate(R.layout.item_parent_child_checkbox, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: ExpandableDownloadRecyclerViewAdapter.GroupViewHolder, position: Int) {

        val parentDataItem = parentDataItems[position]
        holder.textView_parentName.text = parentDataItem.parentName

        if (selectAll != null)
            holder.checkBox_parentName.isChecked = selectAll!!

        val noOfChildTextViews = holder.linearLayout_childItems.childCount
        val noOfChild = parentDataItem.childDataItems
        if (noOfChild.size < noOfChildTextViews) {
            for (index in noOfChild.size until noOfChildTextViews) {
                val currentTextView = holder.linearLayout_childItems.getChildAt(index) as TextView
                currentTextView.visibility = View.GONE
            }
        }
        for ((checkBox, childItem) in noOfChild.withIndex()) {
            val currentCheckBox = holder.linearLayout_childItems.getChildAt(checkBox) as CheckBox
            currentCheckBox.text = childItem.title
            currentCheckBox.id = childItem.index
            if (childItem.canRead) {
                currentCheckBox.isEnabled = true

                currentCheckBox.isChecked = childItem.isChecked
                if (childItem.downloaded) {
                    currentCheckBox.setTextColor(context.resources.getColor(R.color.colorAccent))
                } else {
                    currentCheckBox.setTextColor(context.resources.getColor(R.color.webViewText))
                }

                currentCheckBox.setOnClickListener {

                    currentCheckBox.isChecked = !childItem.isChecked
                    childItem.isChecked = !childItem.isChecked

                }
            } else {
                currentCheckBox.isEnabled = false
            }

            if (childItem.isRead) {
                currentCheckBox.setBackgroundColor(context.resources.getColor(R.color.colorPrimaryDark))
            }

        }

    }

    override fun getItemCount(): Int {
        return parentDataItems.size
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val textView_parentName: TextView = itemView.findViewById(R.id.tv_parent_name)
        val checkBox_parentName: CheckBox = itemView.findViewById(R.id.cb_parent_name)
        val linearLayout_childItems: LinearLayout = itemView.findViewById(R.id.ll_child_items)

        init {
            linearLayout_childItems.visibility = View.GONE
            var intMaxNoOfChild = 0
            for (index in 0 until parentDataItems.size) {
                val intMaxSizeTemp = parentDataItems[index].childDataItems.size
                if (intMaxSizeTemp > intMaxNoOfChild) intMaxNoOfChild = intMaxSizeTemp
            }
            for (indexView in 0 until intMaxNoOfChild) {
                val checkBox = CheckBox(context)
                checkBox.id = indexView
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                linearLayout_childItems.addView(checkBox, layoutParams)
            }
            textView_parentName.setOnClickListener(this)



            checkBox_parentName.setOnCheckedChangeListener { buttonView, isChecked ->
                buttonView.isChecked = isChecked
                for (i in 0 until linearLayout_childItems.childCount) {
                    val item = linearLayout_childItems.getChildAt(i) as CheckBox
                    if (item.isEnabled) item.isChecked = isChecked
                }
                parentDataItems[adapterPosition].childDataItems.forEach { it.isChecked = isChecked }
            }
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

