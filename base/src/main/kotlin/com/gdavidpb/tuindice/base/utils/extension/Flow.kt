package com.gdavidpb.tuindice.base.utils.extension

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun <T> emptyStateFlow() = MutableStateFlow<T?>(null)

fun <T> CoroutineScope.collect(
	flow: Flow<T>,
	collector: FlowCollector<T>
) = launch { flow.collect(collector) }

fun LifecycleOwner.launchRepeatOnLifecycle(
	state: Lifecycle.State = Lifecycle.State.STARTED,
	block: suspend CoroutineScope.() -> Unit
) {
	val (lifecycleScope, lifecycle) = when (this) {
		is Fragment -> viewLifecycleOwner.lifecycleScope to viewLifecycleOwner.lifecycle
		is Activity -> lifecycleScope to lifecycle
		else -> throw NoWhenBranchMatchedException()
	}

	lifecycleScope.launch {
		lifecycle.repeatOnLifecycle(state) {
			block()
		}
	}
}

fun <T : ViewModel> LifecycleOwner.requestOn(viewModel: T, block: suspend T.() -> Unit) {
	val lifecycleOwner = when (this) {
		is Fragment -> viewLifecycleOwner
		is Activity -> this
		else -> throw NoWhenBranchMatchedException()
	}

	lifecycleOwner.lifecycleScope.launch {
		block(viewModel)
	}
}