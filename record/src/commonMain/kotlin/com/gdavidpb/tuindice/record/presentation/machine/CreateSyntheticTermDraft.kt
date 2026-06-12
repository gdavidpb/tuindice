package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.record.domain.model.SyntheticTermEditSeed
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Extended input registers for the synthetic-term editor: the mutable sources of Σ
 * that the machine's observation pipelines fold back into the table as internal
 * events. Written only from machine level (transition rows and commands) so every
 * write is a table-visible input symbol; the view model never touches it.
 */
class CreateSyntheticTermDraft {
	private val mutableQueryFlow = MutableStateFlow("")
	private val mutableSelectedSubjectsFlow = MutableStateFlow<List<SyntheticTermSubject>>(emptyList())
	private val mutableSelectedPeriodKeyFlow = MutableStateFlow<String?>(null)
	private val mutableEditingTermIdFlow = MutableStateFlow<String?>(null)
	private val mutableEditingTermKeyFlow = MutableStateFlow<String?>(null)
	private var configuredTermId: String? = null

	val queryFlow: StateFlow<String> = mutableQueryFlow
	val selectedSubjectsFlow: StateFlow<List<SyntheticTermSubject>> = mutableSelectedSubjectsFlow
	val selectedPeriodKeyFlow: StateFlow<String?> = mutableSelectedPeriodKeyFlow
	val editingTermIdFlow: StateFlow<String?> = mutableEditingTermIdFlow
	val editingTermKeyFlow: StateFlow<String?> = mutableEditingTermKeyFlow

	fun setQuery(query: String) {
		mutableQueryFlow.value = query
	}

	fun selectPeriod(termKey: String) {
		mutableSelectedPeriodKeyFlow.value = termKey
	}

	fun addSubject(subjectItem: CreateTermSubjectItem) {
		val subject = subjectItem.subject
		if (!subject.canAdd) return
		val current = mutableSelectedSubjectsFlow.value
		if (current.any { item -> item.subjectCode == subject.subjectCode }) return
		mutableSelectedSubjectsFlow.value = current + subject
	}

	fun removeSubject(subjectCode: String) {
		mutableSelectedSubjectsFlow.value = mutableSelectedSubjectsFlow.value
			.filterNot { subject -> subject.subjectCode == subjectCode }
	}

	fun tryConfigure(termId: String?): Boolean {
		if (configuredTermId == termId) return false
		configuredTermId = termId
		return true
	}

	fun clearEditing() {
		mutableEditingTermIdFlow.value = null
		mutableEditingTermKeyFlow.value = null
		mutableSelectedPeriodKeyFlow.value = null
		mutableSelectedSubjectsFlow.value = emptyList()
	}

	fun applySeed(seed: SyntheticTermEditSeed) {
		mutableSelectedPeriodKeyFlow.value = seed.period.termKey
		mutableSelectedSubjectsFlow.value = seed.subjects
		mutableEditingTermKeyFlow.value = seed.termKey
		mutableEditingTermIdFlow.value = seed.termId
	}
}
