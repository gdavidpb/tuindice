package com.gdavidpb.tuindice.wizard.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.model.RecordTopBarViewModeState
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.wizard.presentation.model.WizardRouteViewState
import com.gdavidpb.tuindice.wizard.presentation.model.WizardStep
import com.gdavidpb.tuindice.wizard.presentation.model.WizardStepId
import com.gdavidpb.tuindice.wizard.presentation.model.defaultWizardSteps
import com.gdavidpb.tuindice.wizard.presentation.model.indexOfStep

object Wizard {
	sealed class State : ViewState() {
		data class Content(
			val steps: List<WizardStep> = defaultWizardSteps(),
			val currentIndex: Int = 0,
			val selectedSubjectTab: SubjectSegmentTab = SubjectSegmentTab.CAREER,
			val recordViewMode: RecordViewMode = RecordViewMode.Projection,
			val selectedTermId: String = CURRENT_TERM_ID
		) : State(), WizardRouteViewState {
			val currentStep: WizardStep
				get() = steps[currentIndex.coerceIn(0, steps.lastIndex.coerceAtLeast(0))]

			val currentProgress: Int
				get() = (currentIndex + 1 - progressOffset).coerceIn(1, totalProgress)

			val totalProgress: Int
				get() = (steps.size - progressOffset).coerceAtLeast(1)

			val isFirstStep: Boolean
				get() = currentIndex <= 0

			val isLastStep: Boolean
				get() = currentIndex >= steps.lastIndex

			val isWelcomeStep: Boolean
				get() = currentStep.id == WizardStepId.Welcome

			private val progressOffset: Int
				get() = if (steps.firstOrNull()?.id == WizardStepId.Welcome) 1 else 0

			override val topBarTitle: UiText
				get() = currentStep.topBarTitle

			override val topBarConfig: TopBarConfig?
				get() = currentStep.topBarConfig

			override val isTopBarVisible: Boolean
				get() = !isWelcomeStep

			override val isBottomBarVisible: Boolean
				get() = false

			override val topBarViewModeState: RecordTopBarViewModeState?
				get() = if (currentStep.showsRecordViewMode)
					RecordTopBarViewModeState(selectedMode = recordViewMode)
				else
					null

			fun advance(): Content {
				return copy(currentIndex = (currentIndex + 1).coerceAtMost(steps.lastIndex))
			}

			fun goBack(): Content {
				return copy(currentIndex = (currentIndex - 1).coerceAtLeast(0))
			}

			fun goTo(stepId: WizardStepId): Content {
				return copy(currentIndex = steps.indexOfStep(stepId))
			}
		}
	}

	sealed class Action : ViewAction() {
		data object Advance : Action()
		data object Back : Action()
		data object Dismiss : Action()
		data object Finish : Action()
		data object OpenSubjectDetail : Action()
		data object OpenEvaluationForm : Action()
		class ConsumeTopBarAction(val topBarAction: TopBarAction) : Action()
		class SelectSubjectTab(val tab: SubjectSegmentTab) : Action()
		class SetRecordViewMode(val viewMode: RecordViewMode) : Action()
		class SelectTerm(val termId: String) : Action()
		class SetSubjectChartsVisible(val isVisible: Boolean) : Action()
	}

	sealed class Effect : ViewEffect() {
		data object FinishWizard : Effect()
	}
}
