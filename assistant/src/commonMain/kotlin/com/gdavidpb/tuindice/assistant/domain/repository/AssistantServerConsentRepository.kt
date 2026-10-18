package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.chatcontract.consent.ConsentStatus

/** The consent the server holds for the signed-in student. The consent the device keeps follows it when it is read. */
interface AssistantServerConsentRepository {
	fun reconcileConsent(status: ConsentStatus)
}
