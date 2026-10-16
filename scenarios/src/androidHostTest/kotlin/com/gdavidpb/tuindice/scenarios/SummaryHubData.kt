package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.MockJson.array
import com.gdavidpb.tuindice.scenarios.MockJson.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** One weekly meeting of an attempt of the hub term, as its sync fixture spells it (day 1 = Sunday). */
internal class HubMeeting(val attempt: String, val day: Int, val start: Int, val end: Int, val room: String?)

/** The files of the two hub accounts, read the way the host tests of the hub read them. */
internal object SummaryHubData {
	private const val SYNC = "mocks/__files/sync/post-sync-summary-hub.json"
	private const val RECORD_READ = "mocks/__files/record/get-record-summary-hub.json"
	private const val EVALUATIONS = "mocks/__files/evaluations/get-evaluations-summary-hub.json"

	val syncRecord: JsonObject = MockJson.obj(RepoFiles.file(SYNC)).obj("record")
	val readRecord: JsonObject = MockJson.obj(RepoFiles.file(RECORD_READ))
	val term: JsonObject = termsOf(syncRecord).single { it.string("term_kind") == "current" }
	val evaluations: List<JsonObject> =
		MockJson.obj(RepoFiles.file(EVALUATIONS)).array("evaluations").map { it as JsonObject }

	fun meetings(): List<HubMeeting> = term.array("attempts").flatMap { attempt ->
		attempt as JsonObject

		attempt.array("schedule").map { entry ->
			entry as JsonObject

			HubMeeting(
				attempt = checkNotNull(attempt.string("id")),
				day = checkNotNull(entry.int("day_of_week")),
				start = checkNotNull(entry.int("start_block")),
				end = checkNotNull(entry.int("end_block")),
				room = entry.string("classroom")
			)
		}
	}

	fun evaluationOf(id: String): JsonObject = evaluations.single { it.string("id") == id }

	fun dateOf(evaluation: JsonObject): LocalDate =
		Instant.ofEpochMilli(checkNotNull(evaluation.long("date"))).atOffset(ZoneOffset.UTC).toLocalDate()

	fun termsOf(record: JsonObject): List<JsonObject> = record.obj("record").array("terms").map { it as JsonObject }

	fun JsonObject.obj(key: String): JsonObject = this[key] as JsonObject

	fun JsonObject.int(key: String): Int? = (this[key] as? JsonPrimitive)?.intOrNull

	fun JsonObject.long(key: String): Long? = (this[key] as? JsonPrimitive)?.longOrNull
}
