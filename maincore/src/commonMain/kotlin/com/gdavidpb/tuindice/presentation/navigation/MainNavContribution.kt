package com.gdavidpb.tuindice.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavContribution
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

object MainNavContribution : TuIndiceNavContribution {

	override fun registerNavKeys(builder: PolymorphicModuleBuilder<NavKey>) {
		builder.subclass(MainDestination.GooglePlayServicesUnavailableDialog::class)
		builder.subclass(BrowserDestination.Browser::class)
		builder.subclass(BrowserDestination.ExternalResourceDialog::class)
	}
}
