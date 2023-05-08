package com.gdavidpb.tuindice.base.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.gdavidpb.tuindice.base.R
import kotlinx.android.synthetic.main.view_error.view.btnRetry

class ErrorView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

	private var onRetryClick: () -> Unit = {}

	init {
		inflate(context, R.layout.view_error, this)

		btnRetry.setOnClickListener { onRetryClick() }
	}

	fun setOnRetryClick(onClick: () -> Unit) {
		onRetryClick = onClick
	}
}