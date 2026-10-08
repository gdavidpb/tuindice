package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

/** The subject search mocks, as WireMock picks among them. */
internal object SearchMocks {
	class ResolvedBody(val fileName: String, val root: JsonObject)

	/** The search mock WireMock picks for [query]: lowest priority number among the matching GET stubs. */
	fun bodyFor(query: String): ResolvedBody {
		val candidates = RepoFiles.allMappings.resolve("subjects")
			.listFiles { file -> file.name.startsWith("search-subjects-") }
			.orEmpty()
			.map { MockJson.obj(it) }
			.filter { mapping ->
				val request = mapping["request"] as JsonObject
				val contains = ((request["queryParameters"] as? JsonObject)?.get("query") as? JsonObject)
					?.string("contains")

				request.string("method") == "GET" &&
					request.string("urlPath") == "/subjects/v1/search" &&
					(mapping["response"] as JsonObject).string("bodyFileName") != null &&
					(contains == null || contains in query)
			}
		val best = candidates.minBy { (it["priority"] as? JsonPrimitive)?.intOrNull ?: DEFAULT_PRIORITY }
		val bodyName = checkNotNull((best["response"] as JsonObject).string("bodyFileName"))

		return ResolvedBody(bodyName, MockJson.obj(RepoFiles.file("mocks/__files/$bodyName")))
	}

	private const val DEFAULT_PRIORITY = 5
}
