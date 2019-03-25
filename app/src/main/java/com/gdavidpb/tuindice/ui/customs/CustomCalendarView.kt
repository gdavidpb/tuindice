package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.CalendarView
import androidx.core.content.ContextCompat
import com.gdavidpb.tuindice.R
import org.jetbrains.anko.childrenRecursiveSequence
import org.jetbrains.anko.dip
import java.util.*

class CustomCalendarView(context: Context, attrs: AttributeSet) : CalendarView(context, attrs) {

    init {
        setWillNotDraw(false)
    }

    private val highlightedDays = mutableListOf<Calendar>()

    private val dotPaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
            isAntiAlias = true
        }
    }

    private val dotSize by lazy {
        dip(4).toFloat()
    }

    private val initialMonthId by lazy {
        findMonthId()
    }

    private val monthView by lazy {
        childrenRecursiveSequence().first {
            it.javaClass.name.endsWith("SimpleMonthView")
        }
    }

    private val monthViewPager by lazy {
        childrenRecursiveSequence().first {
            it.javaClass.name.endsWith("DayPickerViewPager")
        }
    }

    private val cellWidth by lazy {
        monthView.run {
            (width - paddingLeft - paddingRight) / 7f
        }
    }

    private val cellHeight by lazy {
        monthView.getInt("mDayHeight")
    }

    private val cellXBias by lazy {
        monthView.paddingLeft
    }

    private val cellYBias by lazy {
        val monthHeight = monthView.getInt("mMonthHeight")
        val dayOfWeekHeight = monthView.getInt("mDayOfWeekHeight")

        monthView.paddingTop + monthHeight + dayOfWeekHeight
    }

    override fun onDraw(canvas: Canvas) {
        if (highlightedDays.isEmpty()) {
            super.onDraw(canvas)
            return
        }

        val start = getCurrentCalendar()

        val end = Calendar.getInstance().apply {
            time = start.time

            add(Calendar.MONTH, 1)
        }

        val range = start..end

        highlightedDays.forEach {
            val thisMonth = range.contains(it)

            if (thisMonth) {
                val (x, y) = computeCalendarBias(it)

                canvas.drawCircle(
                        cellXBias + (cellWidth * (x + .5f)),
                        cellYBias + (cellHeight * y) - dotSize,
                        dotSize,
                        dotPaint
                )
            }
        }

        super.onDraw(canvas)
    }

    fun addHighlighted(value: Date) {
        val calendar = Calendar.getInstance().apply {
            time = value

            firstDayOfWeek = this@CustomCalendarView.firstDayOfWeek
        }

        highlightedDays.add(calendar)

        invalidate()
    }

    fun removeHighlighted(value: Date) {
        val calendar = Calendar.getInstance().apply {
            time = value

            firstDayOfWeek = this@CustomCalendarView.firstDayOfWeek
        }

        highlightedDays.remove(calendar)

        invalidate()
    }

    private fun computeCalendarBias(calendar: Calendar): Pair<Float, Float> {
        val x = (calendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek).run {
            (this % 7) + if (this >= 0) 0 else 7
        }.toFloat()

        val y = calendar.get(Calendar.WEEK_OF_MONTH).toFloat()

        return x to y
    }

    private fun getCurrentCalendar(): Calendar {
        return Calendar.getInstance().apply {
            val months = findMonthId() - initialMonthId

            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            add(Calendar.MONTH, months)
        }
    }

    private fun findMonthId(): Int {
        return monthViewPager.invokeInt("getCurrentItem")
    }

    private fun Any.getInt(name: String): Int {
        return javaClass.getDeclaredField(name).let {
            it.isAccessible = true

            it.getInt(this)
        }
    }

    private fun Any.invokeInt(name: String): Int {
        return javaClass.getMethod(name).invoke(this) as Int
    }
}