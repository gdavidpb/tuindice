package com.gdavidpb.tuindice.mocks

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.common.TextFile
import com.github.tomakehurst.wiremock.extension.Extension
import com.github.tomakehurst.wiremock.extension.ExtensionFactory
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2
import com.github.tomakehurst.wiremock.extension.WireMockServices
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import java.time.Instant
import java.time.ZoneOffset
import wiremock.com.fasterxml.jackson.databind.JsonNode
import wiremock.com.fasterxml.jackson.databind.ObjectMapper

class RecordResponseTransformerFactory : ExtensionFactory {
	override fun create(services: WireMockServices): List<Extension> =
		listOf(RecordResponseTransformer(services))

	private class RecordResponseTransformer(services: WireMockServices) : ResponseDefinitionTransformerV2 {
		private val objectMapper = ObjectMapper()
		private val lock = Any()
		private val baseState: BaseState = readBaseState(services.files)
		private val state = RuntimeState(
			recordRevision = initialRevision(baseState)
		)

		override fun getName(): String = "record-response-transformer"

		override fun applyGlobally(): Boolean = false

		override fun transform(serveEvent: ServeEvent): ResponseDefinition = synchronized(lock) {
			val request = serveEvent.request
			val pathSegments = pathSegments(request)

			return when {
				isGetRequest(request, pathSegments) -> getResponse()
				isPutAttemptRequest(request, pathSegments) -> putAttemptResponse(request, pathSegments[4])
				isDeleteAttemptRequest(request, pathSegments) -> deleteAttemptResponse(request, pathSegments[4])
				isPostTermRequest(request, pathSegments) -> postTermResponse(request)
				isDeleteTermRequest(request, pathSegments) -> deleteTermResponse(request, pathSegments[4])
				else -> jsonResponse(404, mapOf("error" to "record_endpoint_not_found"), ERROR_DELAY_MS)
			}
		}

		private fun readBaseState(fileSource: FileSource): BaseState {
			try {
				val configFile: TextFile = fileSource.child(CONFIG_DIRECTORY).getTextFileNamed(CONFIG_FILENAME)
				val root = objectMapper.readTree(configFile.readContentsAsString())
				val profile = parseProfile(root.get("profile"))
				val terms = root.get("terms").map(::parseTerm)
				val addedTermTemplate = parseTerm(root.get("added_term"))
				return BaseState(
					profile = profile,
					terms = terms,
					addedTermTemplate = addedTermTemplate,
				)
			} catch (exception: Exception) {
				throw IllegalStateException("Unable to load record base state for WireMock", exception)
			}
		}

		private fun parseProfile(node: JsonNode): ProfileModel =
			ProfileModel(
				userId = node.get("user_id").asText(),
				identityCardNumber = node.get("identity_card_number").asInt(),
				usbId = node.get("usb_id").asText(),
				email = node.get("email").asText(),
				firstNames = node.get("first_names").asText(),
				lastNames = node.get("last_names").asText(),
				careerName = node.get("career_name").asText(),
				careerCode = node.get("career_code").asInt(),
				scholarship = node.get("scholarship").asBoolean(),
			)

		private fun parseTerm(node: JsonNode): TermModel =
			TermModel(
				id = node.get("id").asText(),
				name = node.get("name").asText(),
				startDate = node.get("start_date").asLong(),
				endDate = node.get("end_date").asLong(),
				kind = node.get("kind").asText(),
				revision = node.get("revision").asLong(),
				attempts = node.get("attempts").map(::parseAttempt),
			)

		private fun parseAttempt(node: JsonNode): AttemptModel {
			val gradingMode = node.path("grading_mode").asText("numeric")
			val status = node.path("status").takeIf(JsonNode::isTextual)?.asText()
			return AttemptModel(
				id = node.get("id").asText(),
				termId = node.get("term_id").asText(),
				code = node.get("code").asText(),
				name = node.get("name").asText(),
				credits = node.get("credits").asInt(),
				gradingMode = gradingMode,
				officialScore = when {
					gradingMode != NUMERIC_GRADING_MODE -> ScoreModel.empty()
					node.path("grade").canConvertToInt() && node.path("grade").asInt() > 0 ->
						ScoreModel.numeric(node.path("grade").asInt())

					else -> ScoreModel.empty()
				},
				officialOutcome = canonicalOutcomeValue(status),
				officialBadge = canonicalBadgeValue(status),
				mutable = node.path("mutable").asBoolean(false),
			)
		}

		private fun isGetRequest(request: com.github.tomakehurst.wiremock.http.Request, pathSegments: List<String>): Boolean =
			request.method == RequestMethod.GET && pathSegments == listOf("record", "v5")

		private fun isPutAttemptRequest(request: com.github.tomakehurst.wiremock.http.Request, pathSegments: List<String>): Boolean =
			request.method == RequestMethod.PUT &&
				pathSegments.size == 5 &&
				pathSegments[0] == "record" &&
				pathSegments[1] == "v5" &&
				pathSegments[2] == "overlay" &&
				pathSegments[3] == "attempts"

		private fun isDeleteAttemptRequest(request: com.github.tomakehurst.wiremock.http.Request, pathSegments: List<String>): Boolean =
			request.method == RequestMethod.DELETE &&
				pathSegments.size == 5 &&
				pathSegments[0] == "record" &&
				pathSegments[1] == "v5" &&
				pathSegments[2] == "overlay" &&
				pathSegments[3] == "attempts"

		private fun isPostTermRequest(request: com.github.tomakehurst.wiremock.http.Request, pathSegments: List<String>): Boolean =
			request.method == RequestMethod.POST && pathSegments == listOf("record", "v5", "overlay", "terms")

		private fun isDeleteTermRequest(request: com.github.tomakehurst.wiremock.http.Request, pathSegments: List<String>): Boolean =
			request.method == RequestMethod.DELETE &&
				pathSegments.size == 5 &&
				pathSegments[0] == "record" &&
				pathSegments[1] == "v5" &&
				pathSegments[2] == "overlay" &&
				pathSegments[3] == "terms"

		private fun getResponse(): ResponseDefinition =
			jsonResponse(
				status = 200,
				body = buildRecordResponse(),
				delayMs = GET_DELAY_MS,
			)

		private fun putAttemptResponse(
			request: com.github.tomakehurst.wiremock.http.Request,
			attemptId: String,
		): ResponseDefinition {
			val root = parseBody(request) ?: return invalidPayload()
			val mutationId = root.path("mutation_id").takeIf { it.isTextual }?.asText()
				?: return invalidPayload()
			val expectedRevision = root.path("expected_revision").takeIf { it.canConvertToLong() }?.asLong()
				?: return invalidPayload()

			state.mutationResults[mutationId]?.let { cachedResponse ->
				return jsonResponse(200, cachedResponse, PUT_DELAY_MS)
			}

			if (expectedRevision != state.recordRevision) {
				return jsonResponse(412, mapOf("error" to "precondition_failed"), ERROR_DELAY_MS)
			}

			val attempt = editableAttemptById(attemptId)
				?: return jsonResponse(404, mapOf("error" to "attempt_not_found"), ERROR_DELAY_MS)
			val score = root.get("score")?.let(::parseScore) ?: if (root.has("score")) return invalidPayload() else null
			val outcome = root.get("outcome")?.let(::parseOutcome) ?: if (root.has("outcome")) return invalidPayload() else null

			if (score == null && outcome == null) {
				return invalidPayload()
			}

			validateOverride(score = score, attempt = attempt) ?: return invalidPayload()

			val nextRevision = state.recordRevision + 1
			state.recordRevision = nextRevision
			state.overridesByAttemptId[attemptId] = AttemptOverrideState(
				score = score,
				outcome = outcome,
				updatedAt = nextRevision,
			)

			val responseBody = buildRecordResponse()
			state.mutationResults[mutationId] = responseBody
			return jsonResponse(200, responseBody, PUT_DELAY_MS)
		}

		private fun deleteAttemptResponse(
			request: com.github.tomakehurst.wiremock.http.Request,
			attemptId: String,
		): ResponseDefinition {
			val root = parseBody(request) ?: return invalidPayload()
			val mutationId = root.path("mutation_id").takeIf { it.isTextual }?.asText()
				?: return invalidPayload()
			val expectedRevision = root.path("expected_revision").takeIf { it.canConvertToLong() }?.asLong()
				?: return invalidPayload()

			state.mutationResults[mutationId]?.let { cachedResponse ->
				return jsonResponse(200, cachedResponse, DELETE_DELAY_MS)
			}

			if (expectedRevision != state.recordRevision) {
				return jsonResponse(412, mapOf("error" to "precondition_failed"), ERROR_DELAY_MS)
			}

			editableAttemptById(attemptId)
				?: return jsonResponse(404, mapOf("error" to "attempt_not_found"), ERROR_DELAY_MS)

			state.recordRevision += 1
			state.overridesByAttemptId.remove(attemptId)

			val responseBody = buildRecordResponse()
			state.mutationResults[mutationId] = responseBody
			return jsonResponse(200, responseBody, DELETE_DELAY_MS)
		}

		private fun postTermResponse(request: com.github.tomakehurst.wiremock.http.Request): ResponseDefinition {
			val root = parseBody(request) ?: return invalidPayload()
			val mutationId = root.path("mutation_id").takeIf { it.isTextual }?.asText()
				?: return invalidPayload()
			val expectedRevision = root.path("expected_revision").takeIf { it.canConvertToLong() }?.asLong()
				?: return invalidPayload()

			state.mutationResults[mutationId]?.let { cachedResponse ->
				return jsonResponse(200, cachedResponse, POST_DELAY_MS)
			}

			if (expectedRevision != state.recordRevision) {
				return jsonResponse(412, mapOf("error" to "precondition_failed"), ERROR_DELAY_MS)
			}

			if (state.addedTerm != null) {
				return jsonResponse(412, mapOf("error" to "term_already_exists"), ERROR_DELAY_MS)
			}

			val startAt = root.path("start_at").takeIf { it.canConvertToLong() }?.asLong()
				?: return invalidPayload()
			val endAt = root.path("end_at").takeIf { it.canConvertToLong() }?.asLong()
				?: return invalidPayload()
			val attemptsNode = root.get("attempts")
				?.takeIf(JsonNode::isArray)
				?.takeIf { it.size() > 0 }
				?: return invalidPayload()

			val nextRevision = state.recordRevision + 1
			val addedTerm = buildSyntheticTerm(
				startAt = startAt,
				endAt = endAt,
				attemptsNode = attemptsNode,
				revision = nextRevision,
			) ?: return invalidPayload()

			state.recordRevision = nextRevision
			state.addedTerm = addedTerm

			val responseBody = buildRecordResponse()
			state.mutationResults[mutationId] = responseBody
			return jsonResponse(200, responseBody, POST_DELAY_MS)
		}

		private fun deleteTermResponse(
			request: com.github.tomakehurst.wiremock.http.Request,
			termId: String,
		): ResponseDefinition {
			val root = parseBody(request) ?: return invalidPayload()
			val mutationId = root.path("mutation_id").takeIf { it.isTextual }?.asText()
				?: return invalidPayload()
			val expectedRevision = root.path("expected_revision").takeIf { it.canConvertToLong() }?.asLong()
				?: return invalidPayload()

			state.mutationResults[mutationId]?.let { cachedResponse ->
				return jsonResponse(200, cachedResponse, DELETE_DELAY_MS)
			}

			if (expectedRevision != state.recordRevision) {
				return jsonResponse(412, mapOf("error" to "precondition_failed"), ERROR_DELAY_MS)
			}

			val addedTerm = state.addedTerm
				?.takeIf { term -> term.id == termId }
				?: return jsonResponse(404, mapOf("error" to "term_not_found"), ERROR_DELAY_MS)

			state.recordRevision += 1
			state.overridesByAttemptId.keys.removeAll(addedTerm.attempts.map(AttemptModel::id).toSet())
			state.addedTerm = null

			val responseBody = buildRecordResponse()
			state.mutationResults[mutationId] = responseBody
			return jsonResponse(200, responseBody, DELETE_DELAY_MS)
		}

		private fun buildSyntheticTerm(
			startAt: Long,
			endAt: Long,
			attemptsNode: JsonNode,
			revision: Long,
		): TermModel? {
			val template = baseState.addedTermTemplate
			val attempts = attemptsNode.mapIndexed { index, node ->
				val subjectCode = node.path("subject_code").takeIf(JsonNode::isTextual)?.asText()?.trim().orEmpty()
				if (subjectCode.isBlank()) return null

				val gradingMode = node.path("grading_mode").takeIf(JsonNode::isTextual)?.asText()?.trim().orEmpty()
					.ifBlank { NUMERIC_GRADING_MODE }
				val score = node.get("score")?.let(::parseScore) ?: if (node.has("score")) return null else null
				val outcome = node.get("outcome")?.let(::parseOutcome) ?: if (node.has("outcome")) return null else null

				AttemptModel(
					id = "$subjectCode-${template.id}-${index + 1}",
					termId = template.id,
					code = subjectCode,
					name = node.path("subject_name").takeIf(JsonNode::isTextual)?.asText()?.trim().orEmpty()
						.ifBlank { "MOCK $subjectCode" },
					credits = node.path("credits").takeIf { value -> value.canConvertToInt() }?.asInt()
						?: DEFAULT_ADDED_ATTEMPT_CREDITS,
					gradingMode = gradingMode,
					officialScore = score ?: ScoreModel.empty(),
					officialOutcome = outcome ?: PENDING_OUTCOME,
					officialBadge = NONE_BADGE,
					mutable = true,
				)
			}

			return TermModel(
				id = template.id,
				name = formatTermName(startAtMillis = startAt, endAtMillis = endAt),
				startDate = startAt,
				endDate = endAt,
				kind = template.kind,
				revision = revision,
				attempts = attempts,
			)
		}

		private fun editableAttemptById(attemptId: String): AttemptModel? =
			visibleTerms()
				.asSequence()
				.flatMap { term -> term.attempts.asSequence() }
				.firstOrNull { attempt -> attempt.id == attemptId && attempt.mutable }

		private fun visibleTerms(): List<TermModel> =
			(baseState.terms + listOfNotNull(state.addedTerm))
				.sortedWith(DESCENDING_TERM_ORDER)

		private fun buildRecordResponse(): Map<String, Any> {
			val terms = visibleTerms()
			val overrides = terms
				.flatMap(TermModel::attempts)
				.mapNotNull { attempt ->
					state.overridesByAttemptId[attempt.id]?.toRecordAttemptOverrideModel(attempt.id)
				}

			return linkedMapOf(
				"revision" to state.recordRevision,
				"record" to linkedMapOf(
					"id" to RECORD_ID,
					"profile" to baseState.profile.toRecordProfileModel(),
					"terms" to terms.map(TermModel::toRecordTermModel),
					"attempt_overrides" to overrides,
				),
			)
		}

		private fun validateOverride(score: ScoreModel?, attempt: AttemptModel): Unit? {
			if (score == null) return Unit
			if (attempt.gradingMode == QUALITATIVE_GRADING_MODE && score.type != EMPTY_SCORE_TYPE) {
				return null
			}
			return Unit
		}

		private fun parseBody(request: com.github.tomakehurst.wiremock.http.Request): JsonNode? {
			val body = request.bodyAsString.takeIf(String::isNotBlank) ?: return null
			return runCatching { objectMapper.readTree(body) }.getOrNull()
		}

		private fun parseScore(node: JsonNode): ScoreModel? =
			when (node.path("type").takeIf(JsonNode::isTextual)?.asText()) {
				EMPTY_SCORE_TYPE -> ScoreModel.empty()
				NUMERIC_SCORE_TYPE -> node.path("value")
					.takeIf { value -> value.canConvertToInt() && value.asInt() in MIN_NUMERIC_SCORE..MAX_NUMERIC_SCORE }
					?.asInt()
					?.let(ScoreModel::numeric)

				SYMBOLIC_SCORE_TYPE -> node.path("value")
					.takeIf(JsonNode::isTextual)
					?.asText()
					?.trim()
					?.takeIf(String::isNotBlank)
					?.let(ScoreModel::symbolic)

				else -> null
			}

		private fun parseOutcome(node: JsonNode): String? =
			node.takeIf(JsonNode::isTextual)
				?.asText()
				?.trim()
				?.takeIf(String::isNotBlank)
				?.lowercase()
				?.takeIf(VALID_OUTCOMES::contains)

		private fun invalidPayload(): ResponseDefinition =
			jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)

		private fun formatTermName(startAtMillis: Long, endAtMillis: Long): String {
			val startDate = Instant.ofEpochMilli(startAtMillis).atZone(ZoneOffset.UTC).toLocalDate()
			val endDate = Instant.ofEpochMilli(endAtMillis).atZone(ZoneOffset.UTC).toLocalDate()
			val startMonth = SPANISH_MONTH_NAMES[startDate.monthValue - 1]
			val endMonth = SPANISH_MONTH_NAMES[endDate.monthValue - 1]

			return when {
				startDate.year == endDate.year && startDate.month == endDate.month ->
					"$startMonth ${startDate.year}"

				startDate.year == endDate.year ->
					"$startMonth - $endMonth ${startDate.year}"

				else ->
					"$startMonth ${startDate.year} - $endMonth ${endDate.year}"
			}
		}

		private fun pathSegments(request: com.github.tomakehurst.wiremock.http.Request): List<String> =
			request.url
				.substringBefore("?")
				.trim('/')
				.split('/')
				.filter { it.isNotBlank() }

		private fun jsonResponse(status: Int, body: Any, delayMs: Int): ResponseDefinition =
			ResponseDefinitionBuilder.responseDefinition()
				.withStatus(status)
				.withHeader("Content-Type", "application/json")
				.withBody(objectMapper.writeValueAsString(body))
				.withFixedDelay(delayMs)
				.build()

		private fun initialRevision(baseState: BaseState): Long =
			maxOf(
				baseState.terms.maxOfOrNull(TermModel::revision) ?: 0L,
				baseState.addedTermTemplate.revision,
			)

		private fun canonicalOutcomeValue(sourceStatus: String?): String =
			when (sourceStatus ?: NORMAL_STATUS) {
				APPROVED_OUTCOME -> APPROVED_OUTCOME
				FAILED_OUTCOME -> FAILED_OUTCOME
				RETIRED_OUTCOME -> RETIRED_OUTCOME
				UNREPORTED_OUTCOME -> UNREPORTED_OUTCOME
				else -> PENDING_OUTCOME
			}

		private fun canonicalBadgeValue(sourceStatus: String?): String =
			if (sourceStatus == WITHOUT_EFFECT_BADGE) WITHOUT_EFFECT_BADGE else NONE_BADGE

		private data class RuntimeState(
			var recordRevision: Long,
			var addedTerm: TermModel? = null,
			val overridesByAttemptId: LinkedHashMap<String, AttemptOverrideState> = linkedMapOf(),
			val mutationResults: MutableMap<String, Map<String, Any>> = linkedMapOf(),
		)

		private data class BaseState(
			val profile: ProfileModel,
			val terms: List<TermModel>,
			val addedTermTemplate: TermModel,
		)

		private data class ProfileModel(
			val userId: String,
			val identityCardNumber: Int,
			val usbId: String,
			val email: String,
			val firstNames: String,
			val lastNames: String,
			val careerName: String,
			val careerCode: Int,
			val scholarship: Boolean,
		) {
			fun toRecordProfileModel(): Map<String, Any> =
				linkedMapOf(
					"user_id" to userId,
					"identity_card_number" to identityCardNumber,
					"usb_id" to usbId,
					"email" to email,
					"first_names" to firstNames,
					"last_names" to lastNames,
					"career_name" to careerName,
					"career_code" to careerCode,
					"scholarship" to scholarship,
				)
		}

		private data class TermModel(
			val id: String,
			val name: String,
			val startDate: Long,
			val endDate: Long,
			val kind: String,
			val revision: Long,
			val attempts: List<AttemptModel>,
		) {
			fun toRecordTermModel(): Map<String, Any> =
				linkedMapOf(
					"id" to id,
					"label" to name,
					"start_at" to startDate,
					"end_at" to endDate,
					"term_kind" to kind,
					"attempts" to attempts.map(AttemptModel::toRecordAttemptModel),
				)
		}

		private data class AttemptModel(
			val id: String,
			val termId: String,
			val code: String,
			val name: String,
			val credits: Int,
			val gradingMode: String,
			val officialScore: ScoreModel,
			val officialOutcome: String,
			val officialBadge: String,
			val mutable: Boolean,
		) {
			fun toRecordAttemptModel(): Map<String, Any> =
				linkedMapOf(
					"id" to id,
					"subject_code" to code,
					"subject_name" to name,
					"credits" to credits,
					"grading_mode" to gradingMode,
					"official_score" to officialScore.toJson(),
					"official_outcome" to officialOutcome,
					"official_badge" to officialBadge,
				)
		}

		private data class AttemptOverrideState(
			val score: ScoreModel?,
			val outcome: String?,
			val updatedAt: Long,
		) {
			fun toRecordAttemptOverrideModel(attemptId: String): Map<String, Any> =
				linkedMapOf<String, Any>(
					"attempt_id" to attemptId,
					"updated_at" to updatedAt,
				).also { model ->
					score?.let { model["score"] = it.toJson() }
					outcome?.let { model["outcome"] = it }
				}
		}

		private data class ScoreModel(
			val type: String,
			val numericValue: Int? = null,
			val symbolicValue: String? = null,
		) {
			fun toJson(): Map<String, Any> =
				when (type) {
					NUMERIC_SCORE_TYPE -> linkedMapOf(
						"type" to type,
						"value" to requireNotNull(numericValue),
					)

					SYMBOLIC_SCORE_TYPE -> linkedMapOf(
						"type" to type,
						"value" to requireNotNull(symbolicValue),
					)

					else -> linkedMapOf("type" to EMPTY_SCORE_TYPE)
				}

			companion object {
				fun empty(): ScoreModel = ScoreModel(type = EMPTY_SCORE_TYPE)

				fun numeric(value: Int): ScoreModel = ScoreModel(
					type = NUMERIC_SCORE_TYPE,
					numericValue = value,
				)

				fun symbolic(value: String): ScoreModel = ScoreModel(
					type = SYMBOLIC_SCORE_TYPE,
					symbolicValue = value,
				)
			}
		}

		private companion object {
			private const val CONFIG_DIRECTORY = "config"
			private const val CONFIG_FILENAME = "record-base-state.json"
			private const val RECORD_ID = "mock-record"
			private const val GET_DELAY_MS = 3000
			private const val PUT_DELAY_MS = 1500
			private const val DELETE_DELAY_MS = 1000
			private const val POST_DELAY_MS = 1200
			private const val ERROR_DELAY_MS = 1000
			private const val DEFAULT_ADDED_ATTEMPT_CREDITS = 4
			private const val MIN_NUMERIC_SCORE = 0
			private const val MAX_NUMERIC_SCORE = 5
			private const val EMPTY_SCORE_TYPE = "empty"
			private const val NUMERIC_SCORE_TYPE = "numeric"
			private const val SYMBOLIC_SCORE_TYPE = "symbolic"
			private const val NUMERIC_GRADING_MODE = "numeric"
			private const val QUALITATIVE_GRADING_MODE = "qualitative_pass_fail"
			private const val NORMAL_STATUS = "normal"
			private const val PENDING_OUTCOME = "pending"
			private const val APPROVED_OUTCOME = "approved"
			private const val FAILED_OUTCOME = "failed"
			private const val RETIRED_OUTCOME = "retired"
			private const val UNREPORTED_OUTCOME = "unreported"
			private const val NONE_BADGE = "none"
			private const val WITHOUT_EFFECT_BADGE = "without_effect"
			private val VALID_OUTCOMES = setOf(
				PENDING_OUTCOME,
				APPROVED_OUTCOME,
				FAILED_OUTCOME,
				RETIRED_OUTCOME,
				UNREPORTED_OUTCOME,
			)
			private val SPANISH_MONTH_NAMES = listOf(
				"Enero",
				"Febrero",
				"Marzo",
				"Abril",
				"Mayo",
				"Junio",
				"Julio",
				"Agosto",
				"Septiembre",
				"Octubre",
				"Noviembre",
				"Diciembre",
			)
			private val DESCENDING_TERM_ORDER = compareByDescending<TermModel> { it.startDate }
				.thenBy { it.id }
		}
	}
}
