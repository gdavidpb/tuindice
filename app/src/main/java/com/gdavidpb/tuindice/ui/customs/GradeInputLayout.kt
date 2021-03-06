package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.utils.MIN_EVALUATION_GRADE
import com.gdavidpb.tuindice.utils.extensions.formatGrade
import com.gdavidpb.tuindice.utils.extensions.hideSoftKeyboard
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GradeInputLayout(context: Context, attrs: AttributeSet) : InputLayout(context, attrs),
	KoinComponent {

	private val inputMethodManager by inject<InputMethodManager>()

	private object Defaults {
		val RANGE_EVALUATION_GRADE = MIN_EVALUATION_GRADE..MAX_EVALUATION_GRADE
	}

	override fun onInflateView(): Int {
		return R.layout.view_grade_input
	}

	override fun onViewInflated() {
		textInputEditText.setOnFocusChangeListener { _, hasFocus ->
			if (!hasFocus) textInputEditText.hideSoftKeyboard(inputMethodManager)
		}
	}

	override fun isValid(): Boolean {
		val grade = getGrade()

		return isValidStep(grade) && (grade in Defaults.RANGE_EVALUATION_GRADE)
	}

	fun isValidStep(grade: Double = getGrade()): Boolean {
		return (grade % MIN_EVALUATION_GRADE == 0.0)
	}

	fun getGrade(): Double {
		return "${textInputEditText.text}".toDoubleOrNull() ?: 0.0
	}

	fun setGrade(grade: Double) {
		textInputEditText.setText(grade.formatGrade())
	}
}