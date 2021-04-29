package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.extensions.getCompatColor
import com.gdavidpb.tuindice.utils.extensions.onTextChanged
import com.google.android.material.textfield.TextInputLayout

open class ValidatedTextInputLayout(context: Context, attrs: AttributeSet)
    : TextInputLayout(context, attrs) {

    private val primaryColor = context.getCompatColor(R.color.color_primary)
    private val secondaryColor = context.getCompatColor(R.color.color_secondary_text)

    private val primaryTint = ColorStateList.valueOf(primaryColor)
    private val secondaryTint = ColorStateList.valueOf(secondaryColor)

    private var validator: (TextInputLayout) -> Boolean = { true }

    init {
        hintTextColor = primaryTint
        boxBackgroundMode = BOX_BACKGROUND_OUTLINE
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (isErrorEnabled) {
            val errorColor = context.getCompatColor(R.color.color_error)
            val errorTint = ColorStateList.valueOf(errorColor)

            boxStrokeErrorColor = errorTint

            setErrorTextColor(errorTint)
            setErrorIconTintList(errorTint)
        }

        setStartIconTintList(secondaryTint)
        setBoxCornerRadiiResources(R.dimen.dp_6, R.dimen.dp_6, R.dimen.dp_6, R.dimen.dp_6)

        editText?.onTextChanged { _, _, _, _ ->
            if (error != null) error = null

            val isValid = isValid()

            setStartIconTintList(if (isValid) primaryTint else secondaryTint)
        }
    }

    fun onValidate(block: (TextInputLayout) -> Boolean) {
        validator = block
    }

    fun isValid(): Boolean {
        return validator(this)
    }
}