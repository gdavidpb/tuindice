package com.gdavidpb.tuindice.evaluations.ui.customs

import android.content.Context
import android.util.AttributeSet
import com.gdavidpb.tuindice.base.ui.customs.InputLayout
import com.gdavidpb.tuindice.evaluations.R

class EvaluationNameInputLayout(context: Context, attrs: AttributeSet) :
	InputLayout(context, attrs) {

	override fun onInflateView(): Int {
		return R.layout.view_evaluation_name_input
	}

	override fun isValid(): Boolean {
		return editText?.text.isNullOrBlank()
	}

	fun getName(): String {
		return "${editText?.text}"
	}

	fun setName(name: String) {
		editText?.setText(name)
	}
}