package com.gdavidpb.tuindice.evaluations.ui.custom

import android.content.Context
import android.util.AttributeSet
import com.gdavidpb.tuindice.base.ui.customs.InputLayout
import com.gdavidpb.tuindice.base.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.base.utils.MIN_EVALUATION_GRADE
import com.gdavidpb.tuindice.base.utils.extensions.hideSoftKeyboard
import com.gdavidpb.tuindice.base.utils.extensions.formatGrade
import com.gdavidpb.tuindice.evaluations.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.view_grade_input.view.textInputLayout as input

class GradeInputLayout(context: Context, attrs: AttributeSet) : InputLayout(context, attrs) {

	private object Defaults {
		val RANGE_EVALUATION_GRADE = MIN_EVALUATION_GRADE..MAX_EVALUATION_GRADE
	}

	override fun onInflateView(): Int = R.layout.view_grade_input

	override val textInputLayout: TextInputLayout by lazy { input }

	override fun onViewInflated() {
		textInputLayout.editText?.apply {
			setOnFocusChangeListener { _, hasFocus ->
				if (!hasFocus) hideSoftKeyboard()
			}
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
		return "${textInputLayout.editText?.text}".toDoubleOrNull() ?: 0.0
	}

	fun setGrade(grade: Double) {
		textInputLayout.editText?.setText(grade.formatGrade())
	}
}