package com.gdavidpb.tuindice.base.utils.extension

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.isActive

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

@OptIn(ExperimentalCoroutinesApi::class)
@SuppressLint("RestrictedApi")
fun NavController.viewModelFlow() =
	currentBackStackEntryFlow
		.mapLatest { backStackEntry ->
			val viewModelStore = backStackEntry.viewModelStore

			viewModelStore.keys()
				.mapNotNull { key -> viewModelStore[key] }
				.filterIsInstance<BaseViewModel<*, *, *>>()
				.find { viewModel -> viewModel.viewModelScope.isActive }!!
		}
		.retry { throwable ->
			delay(100)
			throwable is NullPointerException
		}

val DoubleOptNavType = object : NavType<Double?>(isNullableAllowed = true) {
	override fun get(bundle: Bundle, key: String): Double? {
		if (bundle.getBoolean("isNull")) return null

		return bundle.getDouble(key)
	}

	override fun put(bundle: Bundle, key: String, value: Double?) {
		bundle.putBoolean("isNull", value == null)

		if (value != null) bundle.putDouble(key, value)
	}

	override fun parseValue(value: String): Double {
		return value.toDouble()
	}
}

val DoubleNavType = object : NavType<Double>(isNullableAllowed = false) {
	override fun get(bundle: Bundle, key: String): Double {
		return bundle.getDouble(key)
	}

	override fun put(bundle: Bundle, key: String, value: Double) {
		bundle.putDouble(key, value)
	}

	override fun parseValue(value: String): Double {
		return value.toDouble()
	}
}