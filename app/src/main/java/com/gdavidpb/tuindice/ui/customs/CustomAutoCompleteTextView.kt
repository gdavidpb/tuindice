package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.util.AttributeSet
import android.widget.AutoCompleteTextView
import com.gdavidpb.tuindice.R
import org.jetbrains.anko.px2dip

class CustomAutoCompleteTextView(context: Context, attrs: AttributeSet)
    : AutoCompleteTextView(context, attrs) {

    private var itemHeight: Int = 0

    private val margin = context.resources.getDimension(R.dimen.dp_8).toInt()

    override fun onFilterComplete(count: Int) {
        if (count > 0) {
            if (itemHeight == 0) {
                val unbounded = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                val view = adapter.getView(0, null, null)

                view.measure(unbounded, unbounded)

                itemHeight = view.measuredHeight
            }

            val showCount = Math.min(5, count)

            dropDownHeight = (showCount * context.px2dip(itemHeight).toInt()) + (margin * showCount)
        }

        super.onFilterComplete(count)
    }
}
