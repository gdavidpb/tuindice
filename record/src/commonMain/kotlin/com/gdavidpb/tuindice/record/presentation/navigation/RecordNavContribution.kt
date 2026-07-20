package com.gdavidpb.tuindice.record.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavContribution
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

object RecordNavContribution : TuIndiceNavContribution {

	override fun registerNavKeys(builder: PolymorphicModuleBuilder<NavKey>) {
		builder.subclass(RecordDestination.Record::class)
		builder.subclass(RecordDestination.CreateSyntheticTerm::class)
		builder.subclass(RecordDestination.DeleteSyntheticTermConfirmationDialog::class)
	}
}
