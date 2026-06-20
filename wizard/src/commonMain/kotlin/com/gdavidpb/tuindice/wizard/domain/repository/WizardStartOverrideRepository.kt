package com.gdavidpb.tuindice.wizard.domain.repository

interface WizardStartOverrideRepository {
	suspend fun isWizardStartForced(): Boolean
	suspend fun setWizardStartForced(isForced: Boolean)
}
