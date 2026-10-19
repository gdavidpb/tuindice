package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.chatcontract.consent.ConsentStatus

/** The consent the server holds for the signed-in student. The consent the device keeps follows it when it is read. */
interface AssistantServerConsentRepository {
	/** Marks the account a status is read for, before the status is requested. */
	fun consentSessionMark(): Int

	/** Keeps the status only if the account is still the one marked; a status from an account that has left is dropped. */
	suspend fun reconcileConsent(status: ConsentStatus, consentSessionMark: Int)
}
