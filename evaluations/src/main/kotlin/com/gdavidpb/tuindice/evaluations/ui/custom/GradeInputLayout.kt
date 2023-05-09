package com.gdavidpb.tuindice.evaluations.ui.custom

import android.content.Context
import android.util.AttributeSet
import com.gdavidpb.tuindice.base.ui.custom.InputLayout
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.base.utils.extension.hideSoftKeyboard
import com.gdavidpb.tuindice.base.utils.extension.view
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.utils.MIN_EVALUATION_GRADE
import com.google.android.material.textfield.TextInputLayout

class GradeInputLayout(context: Context, attrs: AttributeSet) : InputLayout(context, attrs) {

	override fun onInflateView(): Int = R.layout.view_grade_input

	override val textInputLayout: TextInputLayout by view(R.id.textInputLayout)

	override fun onViewInflated() {
		textInputLayout.editText?.apply {
			setOnFocusChangeListener { _, hasFocus ->
				if (!hasFocus) hideSoftKeyboard()
			}
		}
	}

	override fun isValid(): Boolean {
		val grade = getGrade()
		val isValidStep = (grade % MIN_EVALUATION_GRADE == 0.0)
		val isValidRange = (grade in MIN_EVALUATION_GRADE..MAX_EVALUATION_GRADE)

		return isValidStep && isValidRange
	}

	fun getGrade(): Double {
		return "${textInputLayout.editText?.text}".toDoubleOrNull() ?: 0.0
	}

	fun setGrade(grade: Double) {
		textInputLayout.editText?.setText(grade.formatGrade())
	}
}