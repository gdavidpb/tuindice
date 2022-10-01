package com.gdavidpb.tuindice.ui.customs

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.utils.extensions.getCompatColor
import com.gdavidpb.tuindice.base.utils.extensions.onTextChanged
import com.google.android.material.textfield.TextInputLayout

abstract class InputLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

	private val primaryColor = context.getCompatColor(R.color.color_primary)
	private val secondaryColor = context.getCompatColor(R.color.color_secondary_text)

	private val primaryTint = ColorStateList.valueOf(primaryColor)
	private val secondaryTint = ColorStateList.valueOf(secondaryColor)

	private val textInputLayout by lazy {
		findViewById<TextInputLayout>(R.id.textInputLayout) ?: error("textInputLayout")
	}

	protected val textInputEditText by lazy {
		textInputLayout.editText ?: error("editText")
	}

	@LayoutRes
	abstract fun onInflateView(): Int

	open fun isValid(): Boolean = true
	open fun onViewInflated() {}

	override fun onFinishInflate() {
		super.onFinishInflate()

		inflate(context, onInflateView(), this)

		with(textInputLayout) {
			textInputEditText.onTextChanged { _, _, _, _ ->
				if (error != null) {
					error = null
					isErrorEnabled = false
				}

				val isValid = isValid()

				setStartIconTintList(if (isValid) primaryTint else secondaryTint)
			}
		}

		onViewInflated()
	}

	fun isEmpty(): Boolean {
		return textInputEditText.text.isNullOrBlank()
	}

	fun setError(resource: Int) {
		with(textInputLayout) {
			isErrorEnabled = true
			error = context.getString(resource)
		}
	}
}