package com.gdavidpb.tuindice.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gdavidpb.tuindice.R
import kotlinx.android.synthetic.main.dialog_calendar.*
import java.util.*

open class CalendarDialog(
        private val startDate: Date,
        private val endDate: Date
) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(dPickerCalendar) {
            minDate = startDate.time
            maxDate = endDate.time
        }
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, CalendarDialog::class.java.name)
    }
}