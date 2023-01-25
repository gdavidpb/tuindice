package com.gdavidpb.tuindice.evaluations.ui.custom

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.gdavidpb.tuindice.base.utils.extension.*
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatEvaluationDate
import kotlinx.android.synthetic.main.view_evaluation_date_picker.view.*
import java.util.*

class EvaluationDatePickerView(context: Context, attrs: AttributeSet) :
	ConstraintLayout(context, attrs) {

	init {
		inflate(context, R.layout.view_evaluation_date_picker, this)

		initListeners()
	}

	var selectedDate = Date(0)
		get() = if (sEvaluationDate.isChecked) field else Date(0)
		set(value) {
			field = value
			updateState()
		}

	var isChecked: Boolean
		get() = sEvaluationDate.isChecked
		set(value) {
			sEvaluationDate.isChecked = value
			tViewEvaluationDate.isClickable = value
		}

	fun isValid(): Boolean {
		return !sEvaluationDate.isChecked || selectedDate.time != 0L
	}

	private fun initListeners() {
		sEvaluationDate.onCheckedChange {
			updateState()
		}

		tViewEvaluationDate.onClickOnce {
			context.datePicker { dialog ->
				if (selectedDate.time != 0L)
					dialog.selectedDate = selectedDate

				dialog.onDateSelected { date ->
					selectedDate = date
				}

				dialog.setUpDatePicker {
					val startDate = Date().add(Calendar.YEAR, -1).time
					val endDate = Date().add(Calendar.YEAR, 1).time

					minDate = startDate
					maxDate = endDate
				}
			}
		}
	}

	private fun updateState() {
		tViewEvaluationDate.isClickable = sEvaluationDate.isChecked

		tViewEvaluationDate.text = when {
			!sEvaluationDate.isChecked -> {
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
		tViewEvaluationDate.compoundDrawables[0].apply {
			colorFilter =
				PorterDuffColorFilter(context.getCompatColor(resId), PorterDuff.Mode.SRC_IN)
		}
	}
}