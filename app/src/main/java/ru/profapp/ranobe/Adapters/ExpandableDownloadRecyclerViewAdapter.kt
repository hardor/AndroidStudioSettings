package ru.profapp.ranobe.Adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import ru.profapp.ranobe.Models.Chapter
import ru.profapp.ranobe.R

class ExpandableDownloadRecyclerViewAdapter(private val mContext: Context) : RecyclerView.Adapter<ExpandableDownloadRecyclerViewAdapter.GroupViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(mContext)
    var selectAll: Boolean? = null

    constructor(context: Context, mChapters: List<Chapter>) : this(context) {

        val numInGroup = 100
        if (mChapters.isNotEmpty()) {
            val num = Math.ceil((mChapters.size).toDouble() / numInGroup).toInt()
            for (i in 0 until num) {
                val parentDataItem = ParentDataItem("${mChapters[minOf((i + 1) * numInGroup, mChapters.size - 1)].title} - ${mChapters[minOf(i * numInGroup, mChapters.size - 1)].title}", (mChapters.subList(i * numInGroup, minOf((i + 1) * numInGroup, mChapters.size))))
                parentDataItem.canRead = parentDataItem.childDataItems.any { it -> it.canRead }
                parentDataItems.add(parentDataItem)
            }
        }
    }

    private val parentDataItems: ArrayList<ParentDataItem> = ArrayList()

    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ExpandableDownloadRecyclerViewAdapter.GroupViewHolder {
        val view = inflater.inflate(R.layout.item_parent_child_checkbox, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: ExpandableDownloadRecyclerViewAdapter.GroupViewHolder, position: Int) {

        val parentDataItem = parentDataItems[position]
        holder.textViewParentName.text = parentDataItem.parentName
        if (!parentDataItem.canRead) {
            holder.textViewParentName.setBackgroundColor(Color.GRAY)
            holder.checkBoxParentName.isEnabled = false
        } else if (selectAll != null) {
            holder.checkBoxParentName.isChecked = selectAll!!
        }

        val noOfChildTextViews = holder.linearLayoutChildItems.childCount
        val noOfChild = parentDataItem.childDataItems
        if (noOfChild.size < noOfChildTextViews) {
            for (index in noOfChild.size until noOfChildTextViews) {
                val currentTextView = holder.linearLayoutChildItems.getChildAt(index) as TextView
                currentTextView.visibility = View.GONE
            }
        }
        for ((checkBox, childItem) in noOfChild.withIndex()) {
            val currentCheckBox = holder.linearLayoutChildItems.getChildAt(checkBox) as CheckBox
            currentCheckBox.text = childItem.title
            currentCheckBox.id = childItem.index
            if (childItem.canRead) {
                currentCheckBox.isEnabled = true
                currentCheckBox.isChecked = childItem.isChecked
                if (childItem.downloaded) {
                    currentCheckBox.setTextColor(mContext.resources.getColor(R.color.colorAccent))
                } else {
                    currentCheckBox.setTextColor(mContext.resources.getColor(R.color.webViewText))
                }

                currentCheckBox.setOnClickListener {

                    currentCheckBox.isChecked = !childItem.isChecked
                    childItem.isChecked = !childItem.isChecked

                }
            } else {
                currentCheckBox.setBackgroundColor(Color.GRAY)
                currentCheckBox.isEnabled = false
                currentCheckBox.isChecked = false
            }

            if (childItem.canRead && childItem.isRead) {
                currentCheckBox.setBackgroundColor(mContext.resources.getColor(R.color.colorPrimaryDark))
            }

        }

    }

    override fun getItemCount(): Int {
        return parentDataItems.size
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val textViewParentName: TextView = itemView.findViewById(R.id.tv_parent_name)
        val checkBoxParentName: CheckBox = itemView.findViewById(R.id.cb_parent_name)
        val linearLayoutChildItems: LinearLayout = itemView.findViewById(R.id.ll_child_items)

        init {
            linearLayoutChildItems.visibility = View.GONE
            var intMaxNoOfChild = 0
            for (index in 0 until parentDataItems.size) {
                val intMaxSizeTemp = parentDataItems[index].childDataItems.size
                if (intMaxSizeTemp > intMaxNoOfChild) intMaxNoOfChild = intMaxSizeTemp
            }
            for (indexView in 0 until intMaxNoOfChild) {
                val checkBox = CheckBox(mContext)
                checkBox.id = indexView
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                linearLayoutChildItems.addView(checkBox, layoutParams)
            }
            textViewParentName.setOnClickListener(this)



            checkBoxParentName.setOnClickListener { buttonView ->
                val isChecked = (buttonView as CheckBox).isChecked
                if (parentDataItems[adapterPosition].canRead) {
                    buttonView.isChecked = isChecked
                    for (i in 0 until linearLayoutChildItems.childCount) {
                        val item = linearLayoutChildItems.getChildAt(i) as CheckBox
                        if (item.isEnabled) item.isChecked = isChecked
                    }
                    parentDataItems[adapterPosition].childDataItems.filter { it.canRead }.forEach {
                        it.isChecked = isChecked
                    }
                }
            }
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

