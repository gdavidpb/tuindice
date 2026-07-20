package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavContribution
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

object SummaryNavContribution : TuIndiceNavContribution {

	override fun registerNavKeys(builder: PolymorphicModuleBuilder<NavKey>) {
		builder.subclass(SummaryDestination.Summary::class)
		builder.subclass(SummaryDestination.ProfilePictureSettingsDialog::class)
		builder.subclass(SummaryDestination.RemoveProfilePictureConfirmationDialog::class)
	}
}
