package com.gdavidpb.tuindice.chatcontract.memory

import com.gdavidpb.tuindice.chatcontract.ChatContract
import kotlin.test.Test
import kotlin.test.assertEquals

class StillAppliesAnswerTest {

	@Test
	fun answerKeepsTheContractJson() {
		val json = ChatContract.json.encodeToString(StillAppliesAnswer.serializer(), StillAppliesAnswer(applies = false))

		assertEquals("""{"applies":false}""", json)
	}

	@Test
	fun answerRoundTripsThroughTheContractJson() {
		val answer = StillAppliesAnswer(applies = true)
		val json = ChatContract.json.encodeToString(StillAppliesAnswer.serializer(), answer)

		assertEquals(answer, ChatContract.json.decodeFromString(StillAppliesAnswer.serializer(), json))
	}
}
