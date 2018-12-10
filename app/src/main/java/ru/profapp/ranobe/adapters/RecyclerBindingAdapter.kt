package ru.profapp.ranobe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class RecyclerBindingAdapter<T>(private val holderLayout: Int, private val variableId: Int, private val items: List<T>) : RecyclerView.Adapter<RecyclerBindingAdapter.BindingHolder>() {

    private var onItemClickListener: OnItemClickListener<T>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerBindingAdapter.BindingHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(holderLayout, parent, false)

        return BindingHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerBindingAdapter.BindingHolder, position: Int) {
        val item = items[position]
        holder.binding!!.root.setOnClickListener { view ->
            if (onItemClickListener != null)
                onItemClickListener!!.onItemClick(position, item)
        }
        holder.binding.setVariable(variableId, item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<T>) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener<T> {
        fun onItemClick(position: Int, item: T)
    }

    class BindingHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: ViewDataBinding? = DataBindingUtil.bind(view)
    }
}