package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SetRecordViewModeActionProcessorContractTest {
	@Test
	fun process_persistsViewModeAndEmitsTopBarBannerEffect() = runTest {
		val repository = RecordingRecordSelectionRepository()
		val processor = SetRecordViewModeActionProcessor(
			setRecordViewModeUseCase = SetRecordViewModeUseCase(
				recordSelectionRepository = repository,
				reportingRepository = RecordingReportingRepository()
			)
		)
		val effects = mutableListOf<Record.Effect>()
		val mutations = mutableListOf<suspend (Record.State) -> Record.State>()

		processor.process(
			action = Record.Action.SetViewMode(RecordViewMode.Official),
			sideEffect = effects::add
		).collect { mutation ->
			mutations += mutation
		}

		var finalState: Record.State = Record.State.Loading
		for (mutation in mutations) {
			finalState = mutation(finalState)
		}

		assertEquals(Record.State.Loading, finalState)
		assertContentEquals(listOf(RecordViewMode.Official), repository.setViewModes)

		val effect = assertIs<Record.Effect.ShowTopBarBanner>(effects.single())
		assertEquals(RecordViewMode.Official, effect.viewMode)
		assertEquals(TopBarBannerBehavior.AutoDismiss(millis = 5_000L), effect.behavior)
	}
}

private class RecordingRecordSelectionRepository : RecordSelectionRepository {
	val setViewModes = mutableListOf<RecordViewMode>()
	private val currentViewMode = MutableStateFlow(RecordViewMode.Working)
	private val selectedTermId = MutableStateFlow<String?>(null)

	override fun observeSelectedTermId(viewMode: RecordViewMode): Flow<String?> = selectedTermId

	override fun observeRecordViewMode(): Flow<RecordViewMode> = currentViewMode

	override suspend fun getSelectedTermId(viewMode: RecordViewMode): String? = selectedTermId.value

	override suspend fun setSelectedTermId(viewMode: RecordViewMode, termId: String) {
		selectedTermId.value = termId
	}

	override suspend fun getRecordViewMode(): RecordViewMode = currentViewMode.value

	override suspend fun setRecordViewMode(viewMode: RecordViewMode) {
		currentViewMode.value = viewMode
		setViewModes += viewMode
	}
}
