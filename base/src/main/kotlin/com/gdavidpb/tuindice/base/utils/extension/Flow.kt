package com.gdavidpb.tuindice.base.utils.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

fun <T> CoroutineScope.collect(flow: Flow<T>, collector: FlowCollector<T>) =
	launch { flow.collect(collector) }