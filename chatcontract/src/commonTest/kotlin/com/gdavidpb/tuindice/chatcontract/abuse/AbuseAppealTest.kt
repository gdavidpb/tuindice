package com.gdavidpb.tuindice.chatcontract.abuse

import kotlin.test.Test
import kotlin.test.assertFailsWith

class AbuseAppealTest {

	@Test
	fun appealAtTheLimitIsAccepted() {
		AbuseAppeal("a".repeat(AbuseAppeal.MAX_LENGTH))
	}

	@Test
	fun appealOverTheLimitIsRejected() {
		assertFailsWith<IllegalArgumentException> { AbuseAppeal("a".repeat(AbuseAppeal.MAX_LENGTH + 1)) }
	}
}
