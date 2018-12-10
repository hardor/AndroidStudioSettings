package ru.profapp.ranobe.helpers

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.View.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.utils.GlideApp

//
//You can use a lambda expressions.
//
//android:onClick="@{() -> callback.onCategoryClick(viewModel)}"
//
//If you need the view, you can pass that as well with:
//
//android:onClick="@{(v) -> callback.onCategoryClick(v, viewModel)}"

@set:BindingAdapter("visibleOrGone")
var View.visibleOrGone
    get() = visibility == VISIBLE
    set(value) {
        visibility = if (value) VISIBLE else GONE
    }

@set:BindingAdapter("visible")
var View.visible
    get() = visibility == VISIBLE
    set(value) {
        visibility = if (value) VISIBLE else INVISIBLE
    }

@set:BindingAdapter("invisible")
var View.invisible
    get() = visibility == INVISIBLE
    set(value) {
        visibility = if (value) INVISIBLE else VISIBLE
    }

@set:BindingAdapter("gone")
var View.gone
    get() = visibility == GONE
    set(value) {
        visibility = if (value) GONE else VISIBLE
    }

@BindingAdapter("drawableUrl", "drawableSide")
fun TextView.setDrawable(url: String?, side: Constants.ApplicationConstants?) {

    // Todo: check url isEmpty
    val dp50 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, context.resources.displayMetrics).toInt()
    GlideApp.with(context).load(url).into(object : SimpleTarget<Drawable>(dp50, dp50) {
        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            setVectorForPreLollipop(this@setDrawable, resource, context, side
                    ?: Constants.ApplicationConstants.DRAWABLE_LEFT)
        }
    })

}

@BindingAdapter("imageUrl")
fun ImageView.setImageUrl(url: String?) {
    GlideApp.with(context).load(url).into(this)
}

@BindingAdapter("onSearch")
fun onSearch(input: EditText, listener: OnTextListener?) {
    input.setOnEditorActionListener({ v, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            if (listener != null) {
                val query = input.text.toString()
                v.dismissKeyboard()
                listener.onChanged(query)
            }
            true
        } else {
            false
        }
    })
}

@BindingAdapter("onKeyDown")
fun onKeyDown(input: EditText, listener: OnTextListener?) {
    input.setOnKeyListener { _, keyCode, event ->
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            if (listener != null) {
                val query = input.text.toString()
                input.dismissKeyboard()
                listener.onChanged(query)
            }
            true
        } else {
            false
        }
    }
}

interface OnTextListener {
    fun onChanged(s: String)
}

fun TextView.dismissKeyboard() {
    if (context != null) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}