package com.gdavidpb.tuindice.about.presentation.resource

// The texts the about machine puts in its effects and internal events. The machine reads them through this seam, so
// its walk runs on any host without platform resources.
interface AboutTextProvider {
	suspend fun versionUnavailable(): String
	suspend fun termsAndConditionsLabel(): String
	suspend fun privacyPolicyLabel(): String
	suspend fun supportLabel(): String
	suspend fun shareSubject(): String
	suspend fun shareMessage(): String
	suspend fun assistantConsentFailed(): String
	suspend fun assistantConversationsDeleted(): String
	suspend fun assistantConversationsDeleteFailed(): String
}
