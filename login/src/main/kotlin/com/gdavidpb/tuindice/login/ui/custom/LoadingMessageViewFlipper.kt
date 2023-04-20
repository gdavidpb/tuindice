package com.gdavidpb.tuindice.login.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.AdapterViewFlipper
import com.gdavidpb.tuindice.login.ui.adapter.LoadingAdapter

class LoadingMessageViewFlipper(context: Context, attrs: AttributeSet) :
	AdapterViewFlipper(context, attrs) {

	fun setMessages(values: List<String>) {
		adapter = LoadingAdapter(values)
	}
}