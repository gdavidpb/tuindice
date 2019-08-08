package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.CalendarView
import androidx.core.content.ContextCompat
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.containsInMonth
import com.gdavidpb.tuindice.utils.negRem
import org.jetbrains.anko.childrenRecursiveSequence
import org.jetbrains.anko.dip
import org.jetbrains.anko.sdk27.coroutines.onScrollChange
import java.util.*
import kotlin.math.roundToInt

class CustomCalendarView(context: Context, attrs: AttributeSet) : CalendarView(context, attrs) {

    init {
        setWillNotDraw(false)

        maxDate = Calendar.getInstance().run {
            add(Calendar.YEAR, 5)

            timeInMillis
        }

        minDate = Calendar.getInstance().run {
            add(Calendar.YEAR, -5)

            timeInMillis
        }
    }

    private val highlightedDays = mutableListOf<Calendar>()

    private val dotPaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, R.color.color_primary_dark)
            isAntiAlias = true
        }
    }

    private val dotSize by lazy {
        dip(4).toFloat()
    }

    private val monthView by lazy {
        childrenRecursiveSequence().first {
            it.javaClass.name.endsWith("SimpleMonthView")
        }
    }

    private val monthViewPager by lazy {
        childrenRecursiveSequence().first {
            it.javaClass.name.endsWith("DayPickerViewPager")
        }.also {
            it.onScrollChange { _, _, _, _, _ ->
                invalidate()
            }
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
        val scrollIdle = with(monthViewPager) { (if (width != 0) scrollX % width else scrollX) == 0 }

        if (highlightedDays.isNotEmpty() && scrollIdle) {
            val currentCalendar = getCurrentCalendar()

            highlightedDays.forEach {
                if (currentCalendar.containsInMonth(it)) {
                    val (x, y) = computeCalendarBias(it)

                    canvas.drawCircle(
                            cellXBias + (cellWidth * (x + .5f)),
                            cellYBias + (cellHeight * y) - (dotSize * 1.5f),
                            dotSize,
                            dotPaint
                    )
                }
            }
        }

        super.onDraw(canvas)
    }

    fun setHighlights(vararg values: Date) {
        highlightedDays.clear()

        values.forEach { date ->
            val calendar = Calendar.getInstance().apply {
                time = date

                firstDayOfWeek = this@CustomCalendarView.firstDayOfWeek
            }

            highlightedDays.add(calendar)
        }

        invalidate()
    }

    private fun computeCalendarBias(calendar: Calendar): Pair<Float, Float> {
        val x = (calendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek) negRem 7
        val y = calendar.get(Calendar.WEEK_OF_MONTH)

        return x.toFloat() to y.toFloat()
    }

    private fun getCurrentCalendar(): Calendar {
        return Calendar.getInstance().apply {
            val relation = with(monthViewPager) { scrollX / width.toDouble() }
            val months = relation.roundToInt()

            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            add(Calendar.MONTH, months)
        }
    }

    private fun Any.getInt(name: String): Int {
        return javaClass.getDeclaredField(name).let {
            it.isAccessible = true

            it.getInt(this)
        }
    }
}