package com.gdavidpb.tuindice.base.utils.extension

import android.annotation.SuppressLint
import androidx.navigation.NavController
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel

@SuppressLint("RestrictedApi")
inline fun <reified T : BaseViewModel<*, *, *>> NavController.viewModel(): T? {
	return visibleEntries.value.firstNotNullOfOrNull { backStackEntry ->
		backStackEntry.viewModelStore.let { viewModelStore ->
			viewModelStore.keys()
				.find { key -> key.endsWith("${T::class.qualifiedName}") }
				?.let(viewModelStore::get)
		}
	} as? T
}