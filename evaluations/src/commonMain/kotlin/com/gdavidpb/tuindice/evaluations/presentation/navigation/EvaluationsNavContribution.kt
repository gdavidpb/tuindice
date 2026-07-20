package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavContribution
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

object EvaluationsNavContribution : TuIndiceNavContribution {

	override fun registerNavKeys(builder: PolymorphicModuleBuilder<NavKey>) {
		builder.subclass(EvaluationsDestination.Evaluations::class)
		builder.subclass(EvaluationsDestination.Evaluation::class)
		builder.subclass(EvaluationsDestination.GradePickerDialog::class)
		builder.subclass(EvaluationsDestination.MaxGradePickerDialog::class)
		builder.subclass(EvaluationsDestination.EvaluationGradePickerDialog::class)
		builder.subclass(EvaluationsDestination.DeleteEvaluationConfirmationDialog::class)
	}
}
