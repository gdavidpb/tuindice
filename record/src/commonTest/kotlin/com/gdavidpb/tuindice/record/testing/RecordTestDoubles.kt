package com.gdavidpb.tuindice.record.testing

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationSnapshot
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermCreationRepository
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermLoadPreviewRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull

class ControllableAcademicRecordRepository(
	initialRecord: AcademicRecord = AcademicRecord(id = "record"),
	initialHasSynced: Boolean = false,
	private val observedRecordFlow: Flow<AcademicRecord>? = null,
	private val cachedRecord: AcademicRecord? = null
) : AcademicRecordRepository {
	val recordFlow = MutableStateFlow(initialRecord)
	val hasSyncedFlow = MutableStateFlow(initialHasSynced)
	val addedTerms = mutableListOf<SyntheticTermCreationCommand>()
	val updatedTerms = mutableListOf<SyntheticTermUpdateCommand>()
	val deletedTermIds = mutableListOf<String>()
	var addSyntheticTermGate: CompletableDeferred<Unit>? = null
	var recordAvailable: Boolean = true
	var updateAcademicRecordCalls: Int = 0
		private set
	val updateAcademicRecordForceRemoteCalls = mutableListOf<Boolean>()
	var updateAcademicRecordThrowable: Throwable? = null

	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> =
		observedRecordFlow ?: recordFlow

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> = hasSyncedFlow

	override suspend fun getAcademicRecord(): AcademicRecord? =
		if (recordAvailable) cachedRecord ?: recordFlow.value else null

	override suspend fun updateAcademicRecord() {
		updateAcademicRecord(forceRemote = false)
	}

	override suspend fun updateAcademicRecord(forceRemote: Boolean) {
		updateAcademicRecordCalls++
		updateAcademicRecordForceRemoteCalls += forceRemote
		updateAcademicRecordThrowable?.let { throwable -> throw throwable }
	}

	override suspend fun drainPendingMutations() = Unit

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?
	) = Unit

	override suspend fun deleteAttemptOverride(attemptId: String) = Unit

	override suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand) {
		addSyntheticTermGate?.await()
		addedTerms += command
	}

	override suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand) {
		updatedTerms += command
	}

	override suspend fun deleteSyntheticTerm(termId: String) {
		deletedTermIds += termId
	}
}

class RecordingRecordSelectionRepository(
	initialViewMode: RecordViewMode = RecordViewMode.Historical
) : RecordSelectionRepository {
	private val viewModeFlow = MutableStateFlow(initialViewMode)
	private val historicalTermIdFlow = MutableStateFlow<String?>(null)
	private val projectionTermIdFlow = MutableStateFlow<String?>(null)
	val setSelectedTermCalls = mutableListOf<Pair<RecordViewMode, String>>()
	val setViewModeCalls = mutableListOf<RecordViewMode>()

	override fun observeSelectedTermId(viewMode: RecordViewMode): Flow<String?> =
		selectedTermIdFlow(viewMode)

	override fun observeRecordViewMode(): Flow<RecordViewMode> = viewModeFlow

	override suspend fun getSelectedTermId(viewMode: RecordViewMode): String? =
		selectedTermIdFlow(viewMode).value

	override suspend fun setSelectedTermId(viewMode: RecordViewMode, termId: String) {
		setSelectedTermCalls += viewMode to termId
		selectedTermIdFlow(viewMode).value = termId
	}

	override suspend fun getRecordViewMode(): RecordViewMode = viewModeFlow.value

	override suspend fun setRecordViewMode(viewMode: RecordViewMode) {
		setViewModeCalls += viewMode
		viewModeFlow.value = viewMode
	}

	private fun selectedTermIdFlow(viewMode: RecordViewMode): MutableStateFlow<String?> =
		if (viewMode == RecordViewMode.Historical) historicalTermIdFlow else projectionTermIdFlow
}

class ControllableSyntheticTermCreationRepository : SyntheticTermCreationRepository {
	data class ObserveSnapshotCall(
		val queryFlow: StateFlow<String>,
		val selectedSubjectsFlow: StateFlow<List<SyntheticTermSubject>>,
		val selectedPeriodKeyFlow: StateFlow<String?>,
		val editingTermIdFlow: StateFlow<String?>,
		val editingTermKeyFlow: StateFlow<String?>
	)

	val snapshotFlow = MutableStateFlow<SyntheticTermCreationSnapshot?>(null)
	val refreshCalls = mutableListOf<String>()
	val observeSnapshotCalls = mutableListOf<ObserveSnapshotCall>()
	var refreshSearchThrowable: Throwable? = null

	override fun observeSnapshot(
		queryFlow: StateFlow<String>,
		selectedSubjectsFlow: StateFlow<List<SyntheticTermSubject>>,
		selectedPeriodKeyFlow: StateFlow<String?>,
		editingTermIdFlow: StateFlow<String?>,
		editingTermKeyFlow: StateFlow<String?>
	): Flow<SyntheticTermCreationSnapshot> {
		observeSnapshotCalls += ObserveSnapshotCall(
			queryFlow = queryFlow,
			selectedSubjectsFlow = selectedSubjectsFlow,
			selectedPeriodKeyFlow = selectedPeriodKeyFlow,
			editingTermIdFlow = editingTermIdFlow,
			editingTermKeyFlow = editingTermKeyFlow
		)
		return snapshotFlow.filterNotNull()
	}

	override suspend fun refreshSearch(query: String) {
		refreshCalls += query
		refreshSearchThrowable?.let { throwable -> throw throwable }
	}
}

class FakeSyntheticTermLoadPreviewRepository(
	var preview: SyntheticTermLoadPreview = SyntheticTermLoadPreview(available = false),
	var throwable: Throwable? = null
) : SyntheticTermLoadPreviewRepository {
	val loadCalls = mutableListOf<Pair<String, List<String>>>()

	override suspend fun loadSyntheticTermPreview(
		termKey: String,
		subjectCodes: List<String>
	): SyntheticTermLoadPreview {
		loadCalls += termKey to subjectCodes
		throwable?.let { error -> throw error }
		return preview
	}
}

fun academicTerm(
	id: String,
	kind: TermKind = TermKind.HISTORICAL,
	periodYear: Int = 2024,
	periodCode: AcademicTermPeriod = AcademicTermPeriod.JAN_MAR,
	attempts: List<AcademicAttempt> = emptyList()
): AcademicTerm {
	return AcademicTerm(
		id = id,
		periodYear = periodYear,
		periodCode = periodCode,
		kind = kind,
		attempts = attempts
	)
}

fun academicAttempt(
	subjectCode: String,
	outcome: AttemptOutcome = AttemptOutcome.APPROVED,
	id: String = "attempt-$subjectCode",
	subjectName: String = subjectCode,
	credits: Int = 4,
	gradingMode: AttemptGradingMode = AttemptGradingMode.NUMERIC,
	score: AttemptScore = AttemptScore.empty()
): AcademicAttempt {
	return AcademicAttempt(
		id = id,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		gradingMode = gradingMode,
		academicScore = score,
		academicOutcome = outcome
	)
}
