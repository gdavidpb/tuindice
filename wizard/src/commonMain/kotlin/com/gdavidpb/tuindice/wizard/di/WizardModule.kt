package com.gdavidpb.tuindice.wizard.di

import com.gdavidpb.tuindice.wizard.domain.usecase.CompleteWizardUseCase
import com.gdavidpb.tuindice.wizard.domain.usecase.ShouldStartWizardUseCase
import com.gdavidpb.tuindice.wizard.presentation.action.AdvanceWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.BackWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.ConsumeTopBarWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.DismissWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.FinishWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.OpenEvaluationFormWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.OpenSubjectDetailWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.SetSubjectChartsVisibleWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.SelectSubjectTabWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.SelectTermWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.action.SetRecordViewModeWizardActionProcessor
import com.gdavidpb.tuindice.wizard.presentation.model.WizardTopBarActionBus
import com.gdavidpb.tuindice.wizard.presentation.viewmodel.WizardViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val wizardModule = module {
	/* View models */

	viewModelOf(::WizardViewModel)

	/* Action processor */

	factoryOf(::AdvanceWizardActionProcessor)
	factoryOf(::BackWizardActionProcessor)
	factoryOf(::DismissWizardActionProcessor)
	factoryOf(::FinishWizardActionProcessor)
	factoryOf(::OpenSubjectDetailWizardActionProcessor)
	factoryOf(::OpenEvaluationFormWizardActionProcessor)
	factoryOf(::ConsumeTopBarWizardActionProcessor)
	factoryOf(::SetSubjectChartsVisibleWizardActionProcessor)
	factoryOf(::SelectSubjectTabWizardActionProcessor)
	factoryOf(::SetRecordViewModeWizardActionProcessor)
	factoryOf(::SelectTermWizardActionProcessor)

	/* Use cases */

	factoryOf(::ShouldStartWizardUseCase)
	factoryOf(::CompleteWizardUseCase)

	/* Shared wizard runtime */

	singleOf(::WizardTopBarActionBus)
}
