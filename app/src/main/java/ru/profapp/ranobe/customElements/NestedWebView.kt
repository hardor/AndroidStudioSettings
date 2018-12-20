package ru.profapp.ranobe.customElements

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.WebView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

class NestedWebView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.webViewStyle) : WebView(context, attrs, defStyleAttr), NestedScrollingChild {

    private val mChildHelper: NestedScrollingChildHelper = NestedScrollingChildHelper(this)
    var isSingleTap: Boolean = false

    var appbar: AppBarLayout? = null
    var bottomNavigationView: BottomNavigationView? = null
    val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {



            if (appbar != null && bottomNavigationView != null) {
                val fullyExpanded = (appbar!!.height - appbar!!.bottom == 0) && (bottomNavigationView!!.height == bottomNavigationView!!.bottom - bottomNavigationView!!.top)

                appbar?.setExpanded(!fullyExpanded)

                bottomNavigationView?.updateView(fullyExpanded)

            }

            return super.onSingleTapConfirmed(e)

        }
    })

    init {
        isNestedScrollingEnabled = true
    }

    private fun BottomNavigationView.hideBottomNavigationView() {
        this.animate().translationY(this.height.toFloat())
    }

    private fun BottomNavigationView.showBottomNavigationView() {
        this.animate().translationY(0f)
    }

    private  fun BottomNavigationView.updateView(fullyExpanded: Boolean) {
        if (fullyExpanded)
            this.hideBottomNavigationView()
        else
            this.showBottomNavigationView()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {


        val event = MotionEvent.obtain(ev)
        val res = gestureDetector.onTouchEvent(event)
        if (isSingleTap)
            return res

        return super.onTouchEvent(event)
    }

    // Nested Scroll implements
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
                                      offsetInWindow: IntArray?): Boolean {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

}