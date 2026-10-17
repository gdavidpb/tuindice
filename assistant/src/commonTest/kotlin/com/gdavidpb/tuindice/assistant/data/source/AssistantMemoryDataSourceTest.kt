package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.testing.RecordingServerDisabledRepository
import com.gdavidpb.tuindice.assistant.testing.assistantHttpClient
import com.gdavidpb.tuindice.assistant.testing.jsonAnswer
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantMemoryDataSourceTest {

	@Test
	fun memoryOperationsCallTheirEndpoints() = runTest {
		val calls = mutableListOf<String>()
		val client = assistantHttpClient { call ->
			calls += "${call.method.value} ${call.url.encodedPath}"

			if (call.method.value == "GET") {
				jsonAnswer(HttpStatusCode.OK, """{"items":[]}""")
			} else {
				respond(content = "", status = HttpStatusCode.NoContent)
			}
		}
		val memories = AssistantMemoryDataSource(KtorAssistantApiDataSource(client, RecordingServerDisabledRepository()))

		memories.listMemories()
		memories.deleteMemory("m1")
		memories.answerStillApplies("m1", applies = true)
		memories.deleteAllMemories()

		assertEquals(
			listOf(
				"GET /assistant/v1/memories",
				"DELETE /assistant/v1/memories/m1",
				"POST /assistant/v1/memories/m1/still-applies",
				"DELETE /assistant/v1/memories"
			),
			calls
		)
	}
}
