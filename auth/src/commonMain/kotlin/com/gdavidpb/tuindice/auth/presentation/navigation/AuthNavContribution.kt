package com.gdavidpb.tuindice.auth.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavContribution
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

object AuthNavContribution : TuIndiceNavContribution {

	override fun registerNavKeys(builder: PolymorphicModuleBuilder<NavKey>) {
		builder.subclass(AuthDestination.SignIn::class)
		builder.subclass(AuthDestination.SignOutDialog::class)
		builder.subclass(AuthDestination.UpdatePasswordDialog::class)
	}
}
