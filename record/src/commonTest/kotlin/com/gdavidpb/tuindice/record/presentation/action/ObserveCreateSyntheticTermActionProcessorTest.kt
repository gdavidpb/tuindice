package com.gdavidpb.tuindice.record.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationSnapshot
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermCreationRepository
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermLoadPreviewRepository
import com.gdavidpb.tuindice.record.domain.usecase.LoadSyntheticTermPreviewUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveSyntheticTermCreationUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RefreshSyntheticTermSubjectSearchUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ObserveCreateSyntheticTermActionProcessorTest {
	@Test
	fun process_loadsPreviewForDefaultPeriod_whenSubjectsAreSelectedWithoutManualPeriodSelection() = runTest {
		val defaultPeriod = SyntheticTermPeriodOption(
			periodYear = 2026,
			periodCode = AcademicTermPeriod.APR_JUL
		)
		val selectedSubject = SyntheticTermSubject(
			subjectCode = "MA1111",
			name = "Matemáticas I",
			credits = 4
		)
		val creationRepository = FakeSyntheticTermCreationRepository(
			periodOptions = listOf(defaultPeriod)
		)
		val loadPreviewRepository = RecordingSyntheticTermLoadPreviewRepository()
		val selectedSubjectsFlow = MutableStateFlow<List<SyntheticTermSubject>>(emptyList())
		val selectedPeriodKeyFlow = MutableStateFlow<String?>(null)
		var state = CreateSyntheticTerm.State()

		processor(
			creationRepository = creationRepository,
			loadPreviewRepository = loadPreviewRepository
		).process(
			action = CreateSyntheticTerm.Action.Observe(
				queryFlow = MutableStateFlow(""),
				selectedSubjectsFlow = selectedSubjectsFlow,
				selectedPeriodKeyFlow = selectedPeriodKeyFlow,
				editingTermIdFlow = MutableStateFlow(null),
				editingTermKeyFlow = MutableStateFlow(null)
			),
			sideEffect = {}
		).test {
			while (state.selectedPeriod != defaultPeriod) {
				state = awaitItem()(state)
			}

			selectedSubjectsFlow.value = listOf(selectedSubject)

			while (
				state.selectedSubjects != listOf(selectedSubject) ||
				state.loadPreview?.band != SyntheticTermLoadBand.NORMAL
			) {
				state = awaitItem()(state)
			}

			assertEquals(defaultPeriod, state.selectedPeriod)
			assertEquals(listOf(selectedSubject), state.selectedSubjects)
			assertFalse(state.isLoadingLoadPreview)
			assertEquals(
				listOf(
					LoadPreviewCall(
						termKey = defaultPeriod.termKey,
						subjectCodes = listOf(selectedSubject.subjectCode)
					)
				),
				loadPreviewRepository.calls
			)

			cancelAndIgnoreRemainingEvents()
		}
	}

	private fun processor(
		creationRepository: SyntheticTermCreationRepository,
		loadPreviewRepository: SyntheticTermLoadPreviewRepository
	): ObserveCreateSyntheticTermActionProcessor {
		return ObserveCreateSyntheticTermActionProcessor(
			observeSyntheticTermCreationUseCase = ObserveSyntheticTermCreationUseCase(
				repository = creationRepository,
				reportingRepository = RecordingReportingRepository()
			),
			refreshSyntheticTermSubjectSearchUseCase = RefreshSyntheticTermSubjectSearchUseCase(
				repository = creationRepository,
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = RecordExceptionHandler()
			),
			loadSyntheticTermPreviewUseCase = LoadSyntheticTermPreviewUseCase(
				repository = loadPreviewRepository,
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = RecordExceptionHandler()
			)
		)
	}
}

private class FakeSyntheticTermCreationRepository(
	private val periodOptions: List<SyntheticTermPeriodOption>
) : SyntheticTermCreationRepository {
	override fun observeSnapshot(
		queryFlow: StateFlow<String>,
		selectedSubjectsFlow: StateFlow<List<SyntheticTermSubject>>,
		selectedPeriodKeyFlow: StateFlow<String?>,
		editingTermIdFlow: StateFlow<String?>,
		editingTermKeyFlow: StateFlow<String?>
	): Flow<SyntheticTermCreationSnapshot> {
		return combine(
			selectedSubjectsFlow,
			selectedPeriodKeyFlow
		) { selectedSubjects, selectedPeriodKey ->
			val selectedPeriod = periodOptions.firstOrNull { option -> option.termKey == selectedPeriodKey }
				?: periodOptions.first()

			SyntheticTermCreationSnapshot(
				periodOptions = periodOptions,
				selectedPeriod = selectedPeriod,
				selectedSubjects = selectedSubjects,
				suggestedSubjects = emptyList(),
				searchResults = emptyList()
			)
		}
	}

	override suspend fun refreshSearch(query: String) = Unit
}

private class RecordingSyntheticTermLoadPreviewRepository : SyntheticTermLoadPreviewRepository {
	val calls = mutableListOf<LoadPreviewCall>()

	override suspend fun loadSyntheticTermPreview(
		termKey: String,
		subjectCodes: List<String>
	): SyntheticTermLoadPreview {
		calls += LoadPreviewCall(
			termKey = termKey,
			subjectCodes = subjectCodes
		)
		return SyntheticTermLoadPreview(
			available = true,
			band = SyntheticTermLoadBand.NORMAL
		)
	}
}

private data class LoadPreviewCall(
	val termKey: String,
	val subjectCodes: List<String>
)
