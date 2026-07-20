package com.gdavidpb.tuindice.about.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavContribution
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

object AboutNavContribution : TuIndiceNavContribution {

	override fun registerNavKeys(builder: PolymorphicModuleBuilder<NavKey>) {
		builder.subclass(AboutDestination.About::class)
	}
}
