package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.chatcontract.abuse.AbuseAppeal
import com.gdavidpb.tuindice.chatcontract.turn.AssistantStatus

interface AssistantStandingRepository {
	suspend fun status(): AssistantStatus

	suspend fun appeal(appeal: AbuseAppeal)
}
