package com.gdavidpb.tuindice.base.utils.extensions

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.reflect.full.createInstance

fun LifecycleOwner.context() = when (this) {
	is Fragment -> requireContext()
	is FragmentActivity -> this
	else -> throw NoWhenBranchMatchedException()
}

fun LifecycleOwner.browse(url: String) {
	runCatching {
		val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

		startActivity(intent)
	}
}

fun LifecycleOwner.startActivity(intent: Intent) = when (this) {
	is Fragment -> startActivity(intent)
	is FragmentActivity -> startActivity(intent)
	else -> throw NoWhenBranchMatchedException()
}

inline fun <reified T : BottomSheetDialogFragment> LifecycleOwner.bottomSheetDialog(
	builder: T.() -> Unit
): BottomSheetDialogFragment {
	val fragmentManager = when (this) {
		is Fragment -> childFragmentManager
		is FragmentActivity -> supportFragmentManager
		else -> throw NoWhenBranchMatchedException()
	}

	val dialog = T::class.createInstance()
	val tag = T::class.simpleName

	return dialog.apply {
		builder()

		show(fragmentManager, tag)
	}
}