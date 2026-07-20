package com.gdavidpb.tuindice.enrollmentproof.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavContribution
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

object EnrollmentProofNavContribution : TuIndiceNavContribution {

	override fun registerNavKeys(builder: PolymorphicModuleBuilder<NavKey>) {
		builder.subclass(EnrollmentProofDestination.EnrollmentProofDialog::class)
	}
}
