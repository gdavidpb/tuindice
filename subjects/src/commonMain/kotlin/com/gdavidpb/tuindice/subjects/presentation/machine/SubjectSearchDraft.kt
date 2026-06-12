package com.gdavidpb.tuindice.subjects.presentation.machine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Extended input register for the subject search: the query source the machine's
 * observation pipeline folds back into the table as internal events. Written only
 * from machine level (transition rows), never from the view model.
 */
class SubjectSearchDraft {
	private val mutableQueryFlow = MutableStateFlow("")

	val queryFlow: StateFlow<String> = mutableQueryFlow

	fun setQuery(query: String) {
		mutableQueryFlow.value = query
	}
}
