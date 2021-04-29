package com.gdavidpb.tuindice.ui.customs

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatTextView
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.NO_SETTER
import com.gdavidpb.tuindice.utils.extensions.getCompatColor
import com.gdavidpb.tuindice.utils.mappers.formatEvaluationDate
import java.util.*
import kotlin.DeprecationLevel.ERROR

@Deprecated("Migrate to a custom view")
class EvaluationDatePicker(private val textView: AppCompatTextView) {
    var selectedDate = Date(0)
        get() = if (isDateSelectable) field else Date(0)
        set(value) {
            field = value
            updateText()
        }

    var isDateSelectable: Boolean = true
        set(value) {
            field = value
            updateText()
        }

    @Suppress("UNUSED_PARAMETER")
    var isValidState
        get() = !isDateSelectable || selectedDate.time != 0L
        @Deprecated(message = NO_SETTER, level = ERROR) set(value) = throw NotImplementedError()

    private fun updateText() {
        val context = textView.context

        textView.text = when {
            !isDateSelectable -> {
                setIconColor(R.color.color_primary)

                context.getString(R.string.label_evaluation_no_date)
            }
            selectedDate.time != 0L -> {
                setIconColor(R.color.color_primary)

                selectedDate.formatEvaluationDate()
            }
            else -> {
                setIconColor(R.color.color_secondary_text)

                context.getString(R.string.label_evaluation_select_date)
            }
        }
    }

    private fun setIconColor(@ColorRes resId: Int) {
        textView.compoundDrawables[0].apply {
            colorFilter = PorterDuffColorFilter(textView.context.getCompatColor(resId), PorterDuff.Mode.SRC_IN)
        }
    }
}
