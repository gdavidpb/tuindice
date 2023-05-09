package com.gdavidpb.tuindice.base.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.gdavidpb.tuindice.base.R
import com.gdavidpb.tuindice.base.utils.extension.view
import com.google.android.material.button.MaterialButton

class ErrorView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

	private val btnRetry by view<MaterialButton>(R.id.btnRetry)

	private var onRetryClick: () -> Unit = {}

	init {
		inflate(context, R.layout.view_error, this)

		btnRetry.setOnClickListener { onRetryClick() }
	}

	fun setOnRetryClick(onClick: () -> Unit) {
		onRetryClick = onClick
	}
}