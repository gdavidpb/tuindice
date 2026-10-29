package com.gdavidpb.tuindice.assistant.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavContribution
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

object AssistantNavContribution : TuIndiceNavContribution {

	override fun registerNavKeys(builder: PolymorphicModuleBuilder<NavKey>) {
		builder.subclass(AssistantDestination.Chat::class)
		builder.subclass(AssistantDestination.ComponentSheet::class)
		builder.subclass(AssistantDestination.Memory::class)
		builder.subclass(AssistantDestination.ClearMemoryConfirmationDialog::class)
	}
}
