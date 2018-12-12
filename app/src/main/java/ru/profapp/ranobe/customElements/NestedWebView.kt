package ru.profapp.ranobe.customElements

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.WebView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MotionEventCompat
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

class NestedWebView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.webViewStyle) : WebView(context, attrs, defStyleAttr), NestedScrollingChild {
    private var mLastY: Int = 0
    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)
    private var mNestedOffsetY: Int = 0
    private val mChildHelper: NestedScrollingChildHelper = NestedScrollingChildHelper(this)
    var isSingleTap: Boolean = false

    var appbar: AppBarLayout? = null
    var bottomNavigationView: BottomNavigationView? = null
    val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {



            if (appbar != null && bottomNavigationView != null) {
                val fullyExpanded = (appbar!!.height - appbar!!.bottom == 0) && (bottomNavigationView!!.height == bottomNavigationView!!.bottom - bottomNavigationView!!.top)

                appbar?.setExpanded(!fullyExpanded)

                val behavior =  (bottomNavigationView!!.layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomNavigationBehavior?
                behavior?.updateView(bottomNavigationView!!, fullyExpanded)

            }

            return super.onSingleTapConfirmed(e)

        }
    })

    init {
        isNestedScrollingEnabled = true
    }


    override fun onTouchEvent(ev: MotionEvent): Boolean {
        var returnValue = false

        val event = MotionEvent.obtain(ev)
        val action = MotionEventCompat.getActionMasked(event)
        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsetY = 0
        }
        val eventY = event.y.toInt()
        event.offsetLocation(0f, mNestedOffsetY.toFloat())

        val res = gestureDetector.onTouchEvent(event)
        if (isSingleTap)
            return res

        when (action) {
            MotionEvent.ACTION_MOVE -> {
                var deltaY = mLastY - eventY
                // NestedPreScroll
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1]
                    mLastY = eventY - mScrollOffset[1]
                    event.offsetLocation(0f, (-mScrollOffset[1]).toFloat())
                    mNestedOffsetY += mScrollOffset[1]
                }
                returnValue = super.onTouchEvent(event)

                // NestedScroll
                if (dispatchNestedScroll(0, mScrollOffset[1], 0, deltaY, mScrollOffset)) {
                    event.offsetLocation(0f, mScrollOffset[1].toFloat())
                    mNestedOffsetY += mScrollOffset[1]
                    mLastY -= mScrollOffset[1]
                }
            }
            MotionEvent.ACTION_DOWN -> {
                returnValue = super.onTouchEvent(event)
                mLastY = eventY
                // start NestedScroll
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                returnValue = super.onTouchEvent(event)
                // end NestedScroll
                stopNestedScroll()
            }

        }
        return returnValue
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