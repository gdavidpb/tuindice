package com.gdavidpb.tuindice.ui.customs

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextSwitcher
import android.widget.TextView
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.lookAtMe
import kotlin.math.abs
import kotlin.math.sign

class CustomTextSwitcher<T>(context: Context, attrs: AttributeSet)
    : TextSwitcher(context, attrs) {

    private var index = 0

    private lateinit var source: Collection<T>
    private lateinit var display: T.() -> String
    private lateinit var detector: GestureDetectorCompat

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        detector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    fun getSelectedItem() = source.elementAt(index)

    fun setSource(source: Collection<T>, display: T.() -> String) {
        this.source = source
        this.display = display

        setFactory { TextView.inflate(context, R.layout.view_quarter_switcher, null) }

        setText(source.first().display())

        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)

        detector = GestureDetectorCompat(context, FlingListener(this))
    }

    inner class FlingListener(private val textSwitcher: TextSwitcher)
        : GestureDetector.SimpleOnGestureListener() {
        private val width = textSwitcher.measuredWidth.toFloat()
        private val sensitive = width * 0.25f

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val delta = (e1.x - e2.x)
            val distance = abs(delta)
            val sense = sign(delta).toInt()

            if (distance > sensitive) {
                if (sense > 0) {
                    textSwitcher.setInAnimation(textSwitcher.context, R.anim.slide_in_right)
                    textSwitcher.setOutAnimation(textSwitcher.context, R.anim.slide_out_left)
                } else {
                    textSwitcher.setInAnimation(textSwitcher.context, R.anim.slide_in_left)
                    textSwitcher.setOutAnimation(textSwitcher.context, R.anim.slide_out_right)
                }

                if ((index + sense) in source.indices) {
                    index += sense

                    textSwitcher.setText(source.elementAt(index).display())
                } else
                    textSwitcher.lookAtMe()
            }

            return true
        }
    }
}
