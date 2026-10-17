package com.gdavidpb.tuindice.chatcontract

import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.SupportComponent
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.Progress
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.event.UnknownAssistantEvent
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.UnknownIntentArgs
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import com.gdavidpb.tuindice.chatcontract.message.UnknownMessagePart
import kotlinx.serialization.KSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ChatContractUnknownTypesTest {

	private fun <T> decode(serializer: KSerializer<T>, text: String): T =
		ChatContract.json.decodeFromString(serializer, text)

	@Test
	fun unknownComponentKeepsItsFallbackText() {
		val text = """{"type":"x.y/9","id":"c9","fallbackText":"Texto de respaldo"}"""

		assertEquals(UnknownChatComponent("c9", "Texto de respaldo"), decode(ChatComponent.serializer(), text))
	}

	@Test
	fun unknownIntentArgsEventAndMessagePartDecodeWithoutFailing() {
		assertIs<UnknownIntentArgs>(decode(IntentArgs.serializer(), """{"type":"x.y/9","foo":1}"""))
		assertEquals(
			UnknownAssistantEvent(5, "t1"),
			decode(AssistantEvent.serializer(), """{"type":"x.y/9","seq":5,"turnId":"t1"}""")
		)
		assertIs<UnknownMessagePart>(decode(MessagePart.serializer(), """{"type":"x.y/9"}"""))
	}

	@Test
	fun extraFieldsOfAKnownTypeAreIgnored() {
		val support = """{"type":"sistema.apoyo/1","id":"c1","fallbackText":"Ayuda","lines":[],"extra":{"a":1}}"""
		val progress = """{"type":"progress","seq":1,"turnId":"t1","tool":"a","label":"b","extra":true}"""

		assertEquals(SupportComponent("c1", "Ayuda", lines = emptyList()), decode(ChatComponent.serializer(), support))
		assertEquals(Progress(1, "t1", "a", "b"), decode(AssistantEvent.serializer(), progress))
	}

	@Test
	fun unknownEnumCodeOfAKnownEventFallsBackToTheDefault() {
		val text = """{"type":"run_error","seq":1,"turnId":"t1","code":"nuevo_codigo","retryable":true}"""

		assertEquals(RunError(1, "t1", RunErrorCode.OTHER, retryable = true), decode(AssistantEvent.serializer(), text))
	}
}
