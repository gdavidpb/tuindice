package com.gdavidpb.tuindice.base.utils.extensions

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.reflect.full.createInstance
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.R
import java.io.File

fun LifecycleOwner.hideSoftKeyboard(inputMethodManager: InputMethodManager) {
	val view = when (this) {
		is Fragment -> requireView().rootView
		is FragmentActivity -> currentFocus
		else -> throw NoWhenBranchMatchedException()
	}

	view?.hideSoftKeyboard(inputMethodManager)
}

fun LifecycleOwner.openPdf(file: File) {
	val uri = FileProvider.getUriForFile(context(), BuildConfig.APPLICATION_ID, file)

	val intent = Intent(Intent.ACTION_VIEW, uri).apply {
		flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
	}

	startActivity(intent)
}

fun LifecycleOwner.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
	Toast.makeText(context(), resId, duration).show()
}

fun LifecycleOwner.browse(url: String) {
	runCatching {
		val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

		startActivity(intent)
	}
}

fun LifecycleOwner.connectionSnackBar(isNetworkAvailable: Boolean, retry: (() -> Unit)? = null) {
	val message = if (isNetworkAvailable)
		R.string.snack_service_unavailable
	else
		R.string.snack_network_unavailable

	errorSnackBar(message, retry)
}

fun LifecycleOwner.errorSnackBar(
	@StringRes message: Int = R.string.snack_default_error,
	retry: (() -> Unit)? = null
) {
	snackBar {
		messageResource = message

		if (retry != null) action(R.string.retry) { retry() }
	}
}

fun LifecycleOwner.context() = when (this) {
	is Fragment -> requireContext()
	is FragmentActivity -> this
	else -> throw NoWhenBranchMatchedException()
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