package com.gdavidpb.tuindice.chatcontract.turn

import com.gdavidpb.tuindice.chatcontract.ChatContract
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.consent.ConsentStatus
import com.gdavidpb.tuindice.chatcontract.event.UsageSummary
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantStatusTest {

	private val status = AssistantStatus(
		mode = "guided",
		consent = ConsentStatus(version = 1, accepted = true, acceptedAtMillis = 1_700_000_000_000L),
		standing = AbuseStanding(level = StandingLevel.NONE),
		usage = UsageSummary(remainingHour = 4, remainingDay = 20, remainingMonth = 300)
	)

	@Test
	fun statusKeepsTheContractJson() {
		val json = ChatContract.json.encodeToString(AssistantStatus.serializer(), status)

		assertEquals(
			"""{"mode":"guided","consent":{"version":1,"accepted":true,"acceptedAtMillis":1700000000000},""" +
					""""standing":{"level":"none","appealable":false},""" +
					""""usage":{"remainingHour":4,"remainingDay":20,"remainingMonth":300}}""",
			json
		)
	}

	@Test
	fun statusRoundTripsThroughTheContractJson() {
		val json = ChatContract.json.encodeToString(AssistantStatus.serializer(), status)

		assertEquals(status, ChatContract.json.decodeFromString(AssistantStatus.serializer(), json))
	}
}
