package com.gdavidpb.tuindice.record.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.academiccore.domain.engine.RecordProjectionEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnauthorized
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.record.data.source.api.mapper.attemptSelectionToOverridePayload
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_default_error
import tuindice.record.generated.resources.snack_network_unavailable
import tuindice.record.generated.resources.snack_service_unavailable
import tuindice.record.generated.resources.snack_timeout

class RecordViewModel(
	private val academicRecordRepository: AcademicRecordRepository,
	private val recordSelectionRepository: RecordSelectionRepository
) : ViewModel() {
	private val effectChannel = Channel<Record.Effect>(Channel.BUFFERED)
	private val currentViewMode = MutableStateFlow(RecordViewMode.Working)
	private val selectedOfficialTermId = MutableStateFlow<String?>(null)
	private val selectedWorkingTermId = MutableStateFlow<String?>(null)
	private var currentRecord: AcademicRecord? = null

	private val _state = MutableStateFlow<Record.State>(Record.State.Loading)
	val state: StateFlow<Record.State> = _state.asStateFlow()
	val effect = effectChannel.receiveAsFlow()

	init {
		viewModelScope.launch {
			currentViewMode.value = recordSelectionRepository.getRecordViewMode()
			selectedOfficialTermId.value = recordSelectionRepository.getSelectedTermId(RecordViewMode.Official)
			selectedWorkingTermId.value = recordSelectionRepository.getSelectedTermId(RecordViewMode.Working)
			academicRecordRepository.observeAcademicRecordFlow().collect { record ->
				currentRecord = record
				publishContent(record)
			}
		}
	}

	fun refreshRecordAction() {
		viewModelScope.launch {
			val previousState = _state.value
			if (previousState !is Record.State.Content) {
				_state.value = Record.State.Loading
			}
			runCatching {
				academicRecordRepository.refreshAcademicRecord()
			}.onFailure { throwable ->
				handleFailure(throwable)
				if (previousState !is Record.State.Content) {
					_state.value = Record.State.Failed
				}
			}
		}
	}

	fun selectTermAction(termId: String) {
		viewModelScope.launch {
			when (currentViewMode.value) {
				RecordViewMode.Official -> selectedOfficialTermId.value = termId
				RecordViewMode.Working -> selectedWorkingTermId.value = termId
			}
			recordSelectionRepository.setSelectedTermId(currentViewMode.value, termId)
			currentRecord?.let { record -> publishContent(record) }
		}
	}

	fun setViewModeAction(viewMode: RecordViewMode) {
		viewModelScope.launch {
			currentViewMode.value = viewMode
			recordSelectionRepository.setRecordViewMode(viewMode)
			currentRecord?.let { record -> publishContent(record) }
		}
	}

	fun upsertAttemptSelectionAction(
		termId: String,
		attemptId: String,
		grade: Int? = null,
		status: SubjectStatus? = null,
		commit: Boolean
	) {
		viewModelScope.launch {
			runCatching {
				val (score, outcome) = attemptSelectionToOverridePayload(
					grade = grade,
					status = status
				)
				if (shouldClearOverride(attemptId = attemptId, score = score, outcome = outcome)) {
					academicRecordRepository.deleteAttemptOverride(attemptId)
				} else {
					academicRecordRepository.upsertAttemptOverride(
						attemptId = attemptId,
						score = score,
						outcome = outcome,
						commit = commit
					)
				}
				if (termId != currentSelectedTermId(currentViewMode.value)) {
					selectTermAction(termId)
				}
			}.onFailure(::handleFailure)
		}
	}

	private suspend fun publishContent(record: AcademicRecord) {
		val viewMode = currentViewMode.value
		val projection = record.projectionFor(viewMode)
		if (projection.terms.isEmpty()) {
			_state.value = Record.State.Empty
			return
		}

		val selectedTermId = resolveSelectedTermId(viewMode = viewMode, projection = projection)
		_state.value = Record.State.Content(
			viewMode = viewMode,
			record = record,
			selectedTermId = selectedTermId
		)
	}

	private suspend fun resolveSelectedTermId(
		viewMode: RecordViewMode,
		projection: RecordProjection
	): String {
		val persistedSelected = currentSelectedTermId(viewMode)
		val mirroredSelected = currentSelectedTermId(viewMode.other())
		val selectedTermId = listOfNotNull(persistedSelected, mirroredSelected)
			.firstOrNull { candidate ->
				projection.terms.any { term -> term.id == candidate }
			}
			?: projection.terms.first().id

		if (selectedTermId != persistedSelected) {
			recordSelectionRepository.setSelectedTermId(viewMode, selectedTermId)
			when (viewMode) {
				RecordViewMode.Official -> selectedOfficialTermId.value = selectedTermId
				RecordViewMode.Working -> selectedWorkingTermId.value = selectedTermId
			}
		}

		return selectedTermId
	}

	private fun currentSelectedTermId(viewMode: RecordViewMode): String? {
		return when (viewMode) {
			RecordViewMode.Official -> selectedOfficialTermId.value
			RecordViewMode.Working -> selectedWorkingTermId.value
		}
	}

	private fun RecordViewMode.other(): RecordViewMode {
		return when (this) {
			RecordViewMode.Official -> RecordViewMode.Working
			RecordViewMode.Working -> RecordViewMode.Official
		}
	}

	private fun AcademicRecord.projectionFor(viewMode: RecordViewMode): RecordProjection {
		return when (viewMode) {
			RecordViewMode.Official -> RecordProjectionEngine.projectOfficial(this)
			RecordViewMode.Working -> RecordProjectionEngine.projectWorking(this)
		}
	}

	private fun shouldClearOverride(
		attemptId: String,
		score: com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore?,
		outcome: AttemptOutcome?
	): Boolean {
		val officialAttempt = currentRecord
			?.terms
			?.flatMap { term -> term.attempts }
			?.firstOrNull { attempt -> attempt.id == attemptId }
			?: return false

		return officialAttempt.officialScore == score &&
			officialAttempt.officialOutcome == (outcome ?: officialAttempt.officialOutcome)
	}

	private fun handleFailure(throwable: Throwable) {
		viewModelScope.launch {
			if (throwable.isUnauthorized()) {
				effectChannel.send(Record.Effect.NavigateToOutdatedCredentials)
				return@launch
			}

			val message = when {
				throwable.isTimeout() -> getString(Res.string.snack_timeout)
				throwable.isUnavailable() -> getString(Res.string.snack_service_unavailable)
				throwable.isConnection() -> getString(Res.string.snack_network_unavailable)
				else -> getString(Res.string.snack_default_error)
			}
			effectChannel.send(Record.Effect.ShowSnackBar(message))
		}
	}
}
