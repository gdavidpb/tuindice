package com.gdavidpb.tuindice.utils.extensions

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import java.util.*

inline fun Context.datePicker(f: DatePickerDialogBuilder.() -> Unit) {
    val builder = DatePickerDialogBuilder().apply(f)

    val calendar = Calendar.getInstance().apply {
        time = builder.selectedDate
    }

    DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        Calendar.getInstance().run {
            precision(Calendar.DATE)

            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)

            time
        }.also { selectedDate -> builder.onDateSelected(selectedDate) }
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            .also { dialog -> builder.setUpDatePicker(dialog.datePicker) }
            .show()
}

data class DatePickerDialogBuilder(
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
}