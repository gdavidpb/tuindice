package com.gdavidpb.tuindice.wizard.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.presentation.machine.WizardMachine

class WizardViewModel(
	override val screenMachine: WizardMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<Wizard.State, Wizard.Action, Wizard.Effect>(
	name = "wizard",
	initialState = screenMachine.initialState(),
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
}
