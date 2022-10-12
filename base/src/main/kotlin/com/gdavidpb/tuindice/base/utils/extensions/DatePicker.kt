package com.gdavidpb.tuindice.base.utils.extensions

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import java.util.*

data class DatePickerDialogBuilder(
        val context: Context,
        var onDateSelected: (Date) -> Unit = {},
        var setUpDatePicker: DatePicker.() -> Unit = {},
        var selectedDate: Date = Date()
) {
    fun onDateSelected(value: (Date) -> Unit) {
        onDateSelected = value
    }

    fun setUpDatePicker(value: DatePicker.() -> Unit) {
        setUpDatePicker = value
    }

    fun build(): DatePickerDialog {
        val calendar = Calendar.getInstance().apply {
            time = selectedDate
        }

        return DatePickerDialog(context, { _, year, month, dayOfMonth ->
            Calendar.getInstance().run {
                precision(Calendar.DATE)

                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)

                time
            }.also { selectedDate -> onDateSelected(selectedDate) }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .also { dialog -> setUpDatePicker(dialog.datePicker) }
    }
}

inline fun Context.datePicker(builder: (DatePickerDialogBuilder) -> Unit) =
        DatePickerDialogBuilder(this).apply(builder).build().show()