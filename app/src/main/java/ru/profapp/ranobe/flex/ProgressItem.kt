package ru.profapp.ranobe.flex


import android.animation.Animator
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.Payload
import eu.davidea.flexibleadapter.helpers.AnimatorHelper
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import ru.profapp.ranobe.R

/**
 * @author Davide Steduto
 * @since 22/04/2016
 */
class ProgressItem : AbstractFlexibleItem<ProgressItem.ProgressViewHolder>() {


    override fun createViewHolder(view: View,
                                  adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ProgressViewHolder {
        return ProgressViewHolder(view, adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ProgressViewHolder,
                                position: Int,
                                payloads: MutableList<Any>) {
        val context = holder.itemView.context
        holder.progressBar.visibility = View.GONE
        holder.progressMessage.visibility = View.VISIBLE

        if (!adapter.isEndlessScrollEnabled) {
            status = StatusEnum.DISABLE_ENDLESS
        } else if (payloads.contains(Payload.NO_MORE_LOAD)) {
            status = StatusEnum.NO_MORE_LOAD
        }

        when (this.status) {
            ProgressItem.StatusEnum.NO_MORE_LOAD -> {
                holder.progressMessage.text = context.getString(R.string.no_more_load_retry)
                // Reset to default status for next binding
                status = StatusEnum.MORE_TO_LOAD
            }
            ProgressItem.StatusEnum.DISABLE_ENDLESS -> holder.progressMessage.text = context.getString(R.string.endless_disabled)
            ProgressItem.StatusEnum.ON_CANCEL -> {
                holder.progressMessage.text = context.getString(R.string.endless_cancel)
                // Reset to default status for next binding
                status = StatusEnum.MORE_TO_LOAD
            }
            ProgressItem.StatusEnum.ON_ERROR -> {
                holder.progressMessage.text = context.getString(R.string.endless_error)
                // Reset to default status for next binding
                status = StatusEnum.MORE_TO_LOAD
            }
            else -> {
                holder.progressBar.visibility = View.VISIBLE
                holder.progressMessage.visibility = View.GONE
            }
        }
    }

    var status = StatusEnum.MORE_TO_LOAD

    override fun equals(other: Any?): Boolean {
        return this === other//The default implementation
    }

    override fun getLayoutRes(): Int {
        return R.layout.progress_item
    }


     class ProgressViewHolder(view: View, adapter: FlexibleAdapter<*>) :
        FlexibleViewHolder(view, adapter) {


        var progressBar: ProgressBar = view.findViewById(R.id.progress_bar)

        var progressMessage: TextView = view.findViewById(R.id.progress_message)


    }

     enum class StatusEnum {
        MORE_TO_LOAD, //Default = should have an empty Payload
        DISABLE_ENDLESS, //Endless is disabled because user has set limits
        NO_MORE_LOAD, //Non-empty Payload = Payload.NO_MORE_LOAD
        ON_CANCEL,
        ON_ERROR
    }

}