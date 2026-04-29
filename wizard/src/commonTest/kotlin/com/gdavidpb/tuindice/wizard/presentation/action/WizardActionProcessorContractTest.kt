package com.gdavidpb.tuindice.wizard.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.wizard.domain.usecase.CompleteWizardUseCase
import com.gdavidpb.tuindice.wizard.presentation.contract.CURRENT_TERM_ID
import com.gdavidpb.tuindice.wizard.presentation.contract.HISTORICAL_TERM_ID
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.presentation.model.WizardStepId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WizardActionProcessorContractTest {
	@Test
	fun initialState_startsAtWelcomeStep() {
		val state = Wizard.State.Content()

		assertEquals(WizardStepId.Welcome, state.currentStep.id)
		assertEquals(1, state.currentProgress)
		assertEquals(9, state.totalProgress)
		assertTrue(state.isWelcomeStep)
	}

	@Test
	fun advanceAndBack_changeStepWithoutEffects() = runTest {
		val effects = mutableListOf<Wizard.Effect>()
		val advanced = reduce(
			initialState = Wizard.State.Content(),
			mutations = AdvanceWizardActionProcessor()
				.process(Wizard.Action.Advance, effects::add)
		)
		val backed = reduce(
			initialState = advanced,
			mutations = BackWizardActionProcessor()
				.process(Wizard.Action.Back, effects::add)
		)

		assertEquals(WizardStepId.Summary, (advanced as Wizard.State.Content).currentStep.id)
		assertEquals(WizardStepId.Welcome, (backed as Wizard.State.Content).currentStep.id)
		assertContentEquals(emptyList(), effects)
	}

	@Test
	fun syntheticActions_changeOnlyWizardState() = runTest {
		val effects = mutableListOf<Wizard.Effect>()
		var state: Wizard.State = Wizard.State.Content()

		state = reduce(
			initialState = state,
			mutations = OpenSubjectDetailWizardActionProcessor()
				.process(Wizard.Action.OpenSubjectDetail, effects::add)
		)
		state = reduce(
			initialState = state,
			mutations = SelectSubjectTabWizardActionProcessor()
				.process(Wizard.Action.SelectSubjectTab(SubjectSegmentTab.GLOBAL), effects::add)
		)
		state = reduce(
			initialState = state,
			mutations = OpenEvaluationFormWizardActionProcessor()
				.process(Wizard.Action.OpenEvaluationForm, effects::add)
		)
		val content = state as Wizard.State.Content
		assertEquals(WizardStepId.EvaluationForm, content.currentStep.id)
		assertEquals(SubjectSegmentTab.GLOBAL, content.selectedSubjectTab)
		assertEquals(CURRENT_TERM_ID, content.selectedTermId)
		assertContentEquals(emptyList(), effects)
	}

	@Test
	fun consumeTopBarAction_consumesEnrollmentProofWithoutNavigationOrEffect() = runTest {
		val effects = mutableListOf<Wizard.Effect>()
		val initialState = Wizard.State.Content(currentIndex = 2)
		val state = reduce(
			initialState = initialState,
			mutations = ConsumeTopBarWizardActionProcessor()
				.process(
					action = Wizard.Action.ConsumeTopBarAction(
						TopBarAction.FetchEnrollmentProofAction
					),
					sideEffect = effects::add
				)
		)

		val content = state as Wizard.State.Content
		assertEquals(WizardStepId.RecordActions, content.currentStep.id)
		assertContentEquals(emptyList(), effects)
	}

	@Test
	fun setRecordViewMode_updatesSyntheticModeAndSelectedTerm() = runTest {
		val effects = mutableListOf<Wizard.Effect>()
		val official = reduce(
			initialState = Wizard.State.Content(),
			mutations = SetRecordViewModeWizardActionProcessor()
				.process(Wizard.Action.SetRecordViewMode(RecordViewMode.Official), effects::add)
		) as Wizard.State.Content
		val working = reduce(
			initialState = official,
			mutations = SetRecordViewModeWizardActionProcessor()
				.process(Wizard.Action.SetRecordViewMode(RecordViewMode.Working), effects::add)
		) as Wizard.State.Content

		assertEquals(RecordViewMode.Official, official.recordViewMode)
		assertEquals(HISTORICAL_TERM_ID, official.selectedTermId)
		assertEquals(RecordViewMode.Working, working.recordViewMode)
		assertEquals(CURRENT_TERM_ID, working.selectedTermId)
		assertContentEquals(emptyList(), effects)
	}

	@Test
	fun setSubjectChartsVisible_switchesOnlyBetweenSubjectSteps() = runTest {
		val effects = mutableListOf<Wizard.Effect>()
		val subjectDetailState = Wizard.State.Content().goTo(WizardStepId.SubjectDetail)
		val movedToCharts = reduce(
			initialState = subjectDetailState,
			mutations = SetSubjectChartsVisibleWizardActionProcessor()
				.process(Wizard.Action.SetSubjectChartsVisible(true), effects::add)
		) as Wizard.State.Content
		val movedBackToDetail = reduce(
			initialState = movedToCharts,
			mutations = SetSubjectChartsVisibleWizardActionProcessor()
				.process(Wizard.Action.SetSubjectChartsVisible(false), effects::add)
		) as Wizard.State.Content
		val unchangedOnOtherStep = reduce(
			initialState = Wizard.State.Content().goTo(WizardStepId.Evaluations),
			mutations = SetSubjectChartsVisibleWizardActionProcessor()
				.process(Wizard.Action.SetSubjectChartsVisible(true), effects::add)
		) as Wizard.State.Content

		assertEquals(WizardStepId.SubjectCharts, movedToCharts.currentStep.id)
		assertEquals(WizardStepId.SubjectDetail, movedBackToDetail.currentStep.id)
		assertEquals(WizardStepId.Evaluations, unchangedOnOtherStep.currentStep.id)
		assertContentEquals(emptyList(), effects)
	}

	@Test
	fun dismissAndFinish_persistCompletionAndEmitFinishEffect() = runTest {
		val dismissSettings = FakeSettingsRepository(wizardCompleted = false)
		val dismissEffects = mutableListOf<Wizard.Effect>()
		reduce(
			initialState = Wizard.State.Content(),
			mutations = DismissWizardActionProcessor(
				completeWizardUseCase = CompleteWizardUseCase(
					settingsRepository = dismissSettings,
					reportingRepository = RecordingReportingRepository()
				)
			).process(Wizard.Action.Dismiss, dismissEffects::add)
		)

		val finishSettings = FakeSettingsRepository(wizardCompleted = false)
		val finishEffects = mutableListOf<Wizard.Effect>()
		reduce(
			initialState = Wizard.State.Content(),
			mutations = FinishWizardActionProcessor(
				completeWizardUseCase = CompleteWizardUseCase(
					settingsRepository = finishSettings,
					reportingRepository = RecordingReportingRepository()
				)
			).process(Wizard.Action.Finish, finishEffects::add)
		)

		assertTrue(dismissSettings.isWizardCompleted())
		assertTrue(finishSettings.isWizardCompleted())
		assertContentEquals(listOf(Wizard.Effect.FinishWizard), dismissEffects)
		assertContentEquals(listOf(Wizard.Effect.FinishWizard), finishEffects)
	}

	private suspend fun reduce(
		initialState: Wizard.State,
		mutations: Flow<Mutation<Wizard.State>>
	): Wizard.State {
		var state = initialState
		mutations.collect { mutation ->
			state = mutation(state)
		}
		return state
	}
}
