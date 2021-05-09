package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.util.AttributeSet
import com.gdavidpb.tuindice.R

class EvaluationNameInputLayout(context: Context, attrs: AttributeSet) : InputLayout(context, attrs) {

    override fun onInflateView(): Int {
        return R.layout.view_evaluation_name_input
    }

    override fun isValid(): Boolean {
        return "${textInputEditText.text}".isNotBlank()
    }

    fun getName(): String {
        return "${textInputEditText.text}"
    }

    fun setName(name: String) {
        textInputEditText.setText(name)
    }
}