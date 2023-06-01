package com.gdavidpb.tuindice.base.utils.extension

import android.app.Activity
import android.content.Intent
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gdavidpb.tuindice.base.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.full.createInstance

fun LifecycleOwner.connectionSnackBar(
	isNetworkAvailable: Boolean,
	onRetryClick: (() -> Unit)? = null
) {
	val message = if (isNetworkAvailable)
		R.string.snack_service_unavailable
	else
		R.string.snack_network_unavailable

	errorSnackBar(message, onRetryClick)
}

fun LifecycleOwner.errorSnackBar(
	@StringRes message: Int = R.string.snack_default_error,
	onRetryClick: (() -> Unit)? = null
) {
	snackBar {
		messageResource = message

		if (onRetryClick != null) action(R.string.retry) { onRetryClick() }
	}
}

fun LifecycleOwner.context() = when (this) {
	is Fragment -> requireContext()
	is Activity -> this
	else -> throw NoWhenBranchMatchedException()
}

fun LifecycleOwner.startActivity(intent: Intent) = when (this) {
	is Fragment -> startActivity(intent)
	is Activity -> startActivity(intent)
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

fun LifecycleOwner.launchRepeatOnLifecycle(
	state: Lifecycle.State = Lifecycle.State.STARTED,
	block: suspend CoroutineScope.() -> Unit
) {
	lifecycleScope.launch {
		lifecycle.repeatOnLifecycle(state) {
			block()
		}
	}
}