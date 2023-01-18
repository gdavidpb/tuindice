package com.gdavidpb.tuindice.evaluations.ui.custom

import android.content.Context
import android.util.AttributeSet
import com.gdavidpb.tuindice.base.ui.customs.InputLayout
import com.gdavidpb.tuindice.evaluations.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.view_evaluation_name_input.view.textInputLayout as input

class EvaluationNameInputLayout(context: Context, attrs: AttributeSet) :
	InputLayout(context, attrs) {

	override fun onInflateView(): Int = R.layout.view_evaluation_name_input

	override val textInputLayout: TextInputLayout by lazy { input }

	override fun isValid(): Boolean {
		return textInputLayout.editText?.text.isNullOrBlank()
	}

	fun getName(): String {
		return "${textInputLayout.editText?.text}"
	}

	fun setName(name: String) {
		textInputLayout.editText?.setText(name)
	}
}