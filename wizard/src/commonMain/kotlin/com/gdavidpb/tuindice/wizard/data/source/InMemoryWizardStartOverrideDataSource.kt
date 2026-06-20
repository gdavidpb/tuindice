package com.gdavidpb.tuindice.wizard.data.source

import com.gdavidpb.tuindice.wizard.domain.repository.WizardStartOverrideRepository
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryWizardStartOverrideDataSource : WizardStartOverrideRepository {
	private val isForced = MutableStateFlow(false)

	override suspend fun isWizardStartForced(): Boolean = isForced.value

	override suspend fun setWizardStartForced(isForced: Boolean) {
		this.isForced.value = isForced
	}
}
