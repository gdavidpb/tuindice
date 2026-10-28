package com.gdavidpb.tuindice.evaluations.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

// The order of the picks of the selected week made in this process. A pick takes the next
// number when it is made, at once, so the numbers follow the order in which the picks were
// made, whichever machine made them and whenever their saves run.
object SelectedWeekOrder {
	private val lastOrder = MutableStateFlow(0L)

	fun next(): Long = lastOrder.getAndUpdate { order -> order + 1 } + 1
}
