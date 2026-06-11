package com.gdavidpb.tuindice.wizard.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.wizard.presentation.action.AdvanceWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.BackWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.ConsumeTopBarWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.DismissRecordTermSelectionWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.DismissWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.FinishWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.OpenEvaluationFormWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.OpenSubjectDetailWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.SetSubjectChartsVisibleWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.SelectSubjectTabWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.SelectTermWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.SetRecordViewModeWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import kotlinx.coroutines.flow.Flow

class WizardViewModel(
	private val advanceWizardActionProcessor: AdvanceWizardActionProcessor,
	private val backWizardActionProcessor: BackWizardActionProcessor,
	private val dismissWizardActionProcessor: DismissWizardActionProcessor,
	private val finishWizardActionProcessor: FinishWizardActionProcessor,
	private val openSubjectDetailWizardActionProcessor: OpenSubjectDetailWizardActionProcessor,
	private val openEvaluationFormWizardActionProcessor: OpenEvaluationFormWizardActionProcessor,
	private val consumeTopBarWizardActionProcessor: ConsumeTopBarWizardActionProcessor,
	private val dismissRecordTermSelectionWizardActionProcessor: DismissRecordTermSelectionWizardActionProcessor,
	private val setSubjectChartsVisibleWizardActionProcessor: SetSubjectChartsVisibleWizardActionProcessor,
	private val selectSubjectTabWizardActionProcessor: SelectSubjectTabWizardActionProcessor,
	private val setRecordViewModeWizardActionProcessor: SetRecordViewModeWizardActionProcessor,
	private val selectTermWizardActionProcessor: SelectTermWizardActionProcessor,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : BaseViewModel<Wizard.State, Wizard.Action, Wizard.Effect>(
	name = "wizard",
	initialState = Wizard.State.Content(),
	dispatchers = dispatchers
) {
	fun advanceAction() =
		sendAction(Wizard.Action.Advance)

	fun backAction() =
		sendAction(Wizard.Action.Back)

	fun dismissAction() =
		sendAction(Wizard.Action.Dismiss)

	fun finishAction() =
		sendAction(Wizard.Action.Finish)

	fun openSubjectDetailAction() =
		sendAction(Wizard.Action.OpenSubjectDetail)

	fun openEvaluationFormAction() =
		sendAction(Wizard.Action.OpenEvaluationForm)

	fun consumeTopBarAction(action: TopBarAction) =
		sendAction(Wizard.Action.ConsumeTopBarAction(action))

	fun selectSubjectTabAction(tab: SubjectSegmentTab) =
		sendAction(Wizard.Action.SelectSubjectTab(tab))

	fun setRecordViewModeAction(viewMode: RecordViewMode) =
		sendAction(Wizard.Action.SetRecordViewMode(viewMode))

	fun selectTermAction(termId: String) =
		sendAction(Wizard.Action.SelectTerm(termId))

	fun dismissRecordTermSelectionAction() =
		sendAction(Wizard.Action.DismissRecordTermSelection)

	fun setSubjectChartsVisibleAction(isVisible: Boolean) =
		sendAction(Wizard.Action.SetSubjectChartsVisible(isVisible))

	override suspend fun processAction(
		action: Wizard.Action,
		sideEffect: (Wizard.Effect) -> Unit
	): Flow<Mutation<Wizard.State>> {
		return when (action) {
			is Wizard.Action.Advance ->
				advanceWizardActionProcessor.process(action, sideEffect)

			is Wizard.Action.Back ->
				backWizardActionProcessor.process(action, sideEffect)

			is Wizard.Action.Dismiss ->
				dismissWizardActionProcessor.process(action, sideEffect)

			is Wizard.Action.Finish ->
				finishWizardActionProcessor.process(action, sideEffect)

			is Wizard.Action.OpenSubjectDetail ->
				openSubjectDetailWizardActionProcessor.process(action, sideEffect)

			is Wizard.Action.OpenEvaluationForm ->
				openEvaluationFormWizardActionProcessor.process(action, sideEffect)

			is Wizard.Action.ConsumeTopBarAction ->
				consumeTopBarWizardActionProcessor.process(action, sideEffect)

			is Wizard.Action.SetSubjectChartsVisible ->
				setSubjectChartsVisibleWizardActionProcessor.process(action, sideEffect)

			is Wizard.Action.SelectSubjectTab ->
				selectSubjectTabWizardActionProcessor.process(action, sideEffect)

			is Wizard.Action.SetRecordViewMode ->
				setRecordViewModeWizardActionProcessor.process(action, sideEffect)

			is Wizard.Action.SelectTerm ->
				selectTermWizardActionProcessor.process(action, sideEffect)

			is Wizard.Action.DismissRecordTermSelection ->
				dismissRecordTermSelectionWizardActionProcessor.process(action, sideEffect)
		}
	}
}
