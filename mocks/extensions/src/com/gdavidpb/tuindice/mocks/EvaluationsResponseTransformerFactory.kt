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
import java.util.LinkedHashMap
import wiremock.com.fasterxml.jackson.databind.JsonNode
import wiremock.com.fasterxml.jackson.databind.ObjectMapper

class EvaluationsResponseTransformerFactory : ExtensionFactory {
	override fun create(services: WireMockServices): List<Extension> =
		listOf(EvaluationsResponseTransformer(services))

	private class EvaluationsResponseTransformer(services: WireMockServices) : ResponseDefinitionTransformerV2 {
		private val objectMapper = ObjectMapper()
		private val lock = Any()
		private val state: RuntimeState = readBaseState(services.files)

		override fun getName(): String = "evaluations-response-transformer"

		override fun applyGlobally(): Boolean = false

		override fun transform(serveEvent: ServeEvent): ResponseDefinition = synchronized(lock) {
			val request = serveEvent.request
			val pathSegments = pathSegments(request)

			return when {
				isListRequest(request, pathSegments) -> listResponse()
				isSingleGetRequest(request, pathSegments) -> singleGetResponse(pathSegments)
				isPostRequest(request, pathSegments) -> postResponse(request)
				isPatchRequest(request, pathSegments) -> patchResponse(request, pathSegments)
				isDeleteRequest(request, pathSegments) -> deleteResponse(request, pathSegments)
				else -> jsonResponse(404, mapOf("error" to "evaluation_not_found"), ERROR_DELAY_MS)
			}
		}

		private fun readBaseState(fileSource: FileSource): RuntimeState {
			try {
				val configFile: TextFile = fileSource.child(CONFIG_DIRECTORY).getTextFileNamed(SETTINGS_FILENAME)
				val root = objectMapper.readTree(configFile.readContentsAsString())
				val anchorRevision = root.get("anchor_revision").asLong()
				val evaluations = root.get("evaluations").map(::parseEvaluation)
				return RuntimeState(
					anchorRevision = anchorRevision,
					defaultTermId = evaluations.firstOrNull()?.termId ?: DEFAULT_TERM_ID,
					evaluationsById = LinkedHashMap(evaluations.associateBy { it.id }),
				)
			} catch (exception: Exception) {
				throw IllegalStateException("Unable to load evaluations base state for WireMock", exception)
			}
		}

		private fun parseEvaluation(node: JsonNode): EvaluationModel =
			EvaluationModel(
				id = node.get("id").asText(),
				referenceId = node.path("reference_id").takeIf { it.isTextual }?.asText() ?: node.get("id").asText(),
				termId = node.get("term_id").asText(),
				attemptId = node.get("attempt_id").asText(),
				subjectCode = node.get("subject_code").asText(),
				type = node.get("type").asInt(),
				scheduleMode = node.get("schedule_mode").asText(),
				grade = node.get("grade").takeIf { !it.isNull }?.asDouble(),
				maxGrade = node.get("max_grade").asDouble(),
				date = node.get("date").takeIf { !it.isNull }?.asLong(),
				isDone = node.get("is_done").asBoolean(),
				revision = node.get("revision").asLong(),
			)

		private fun isListRequest(request: com.github.tomakehurst.wiremock.http.Request, pathSegments: List<String>): Boolean =
			request.method == RequestMethod.GET && pathSegments == listOf("evaluations", "v2")

		private fun isSingleGetRequest(request: com.github.tomakehurst.wiremock.http.Request, pathSegments: List<String>): Boolean =
			request.method == RequestMethod.GET && pathSegments.size == 3 && pathSegments[0] == "evaluations" && pathSegments[1] == "v2"

		private fun isPostRequest(request: com.github.tomakehurst.wiremock.http.Request, pathSegments: List<String>): Boolean =
			request.method == RequestMethod.POST && pathSegments == listOf("evaluations", "v2")

		private fun isPatchRequest(request: com.github.tomakehurst.wiremock.http.Request, pathSegments: List<String>): Boolean =
			request.method == RequestMethod.PATCH && pathSegments.size == 3 && pathSegments[0] == "evaluations" && pathSegments[1] == "v2"

		private fun isDeleteRequest(request: com.github.tomakehurst.wiremock.http.Request, pathSegments: List<String>): Boolean =
			request.method == RequestMethod.DELETE && pathSegments.size == 3 && pathSegments[0] == "evaluations" && pathSegments[1] == "v2"

		private fun listResponse(): ResponseDefinition =
			jsonResponse(
				200,
				mapOf(
					"anchor_revision" to state.anchorRevision,
					"evaluations" to activeEvaluations().map { it.toTemplateModel() },
				),
				GET_DELAY_MS,
			)

		private fun singleGetResponse(pathSegments: List<String>): ResponseDefinition {
			val evaluationId = pathSegments.getOrNull(2) ?: return jsonResponse(404, mapOf("error" to "evaluation_not_found"), ERROR_DELAY_MS)
			val evaluation = activeEvaluationById(evaluationId)
				?: return jsonResponse(404, mapOf("error" to "evaluation_not_found"), ERROR_DELAY_MS)
			return jsonResponse(
				200,
				mapOf(
					"anchor_revision" to state.anchorRevision,
					"evaluation" to evaluation.toTemplateModel(),
				),
				GET_DELAY_MS,
			)
		}

		private fun postResponse(request: com.github.tomakehurst.wiremock.http.Request): ResponseDefinition {
			val root = parseBody(request) ?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val mutationId = root.path("mutation_id").takeIf { it.isTextual }?.asText()
				?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val referenceId = root.path("reference_id").takeIf { it.isTextual }?.asText()
				?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val expectedRevision = root.path("expected_revision").takeIf { it.canConvertToLong() }?.asLong()
				?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)

			state.mutationResults[mutationId]?.let { cachedResponse ->
				return jsonResponse(200, cachedResponse, POST_DELAY_MS)
			}

			if (state.evaluationsById.values.any { it.referenceId == referenceId }) {
				return jsonResponse(412, mapOf("error" to "duplicate_reference_id"), ERROR_DELAY_MS)
			}

			if (expectedRevision != state.anchorRevision) {
				return jsonResponse(409, mapOf("error" to "anchor_revision_conflict"), ERROR_DELAY_MS)
			}

			val attemptId = root.path("attempt_id").takeIf { it.isTextual }?.asText()
				?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val termId = root.path("term_id").takeIf { it.isTextual }?.asText()?.takeIf(String::isNotBlank)
				?: inferTermId(attemptId)
			val scheduleMode = root.path("schedule_mode").takeIf { it.isTextual }?.asText()
				?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val type = root.path("type").takeIf { it.canConvertToInt() }?.asInt()
				?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val maxGrade = root.path("max_grade").takeIf { it.isNumber }?.asDouble()
				?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val isDone = root.path("is_done").takeIf { it.isBoolean }?.asBoolean()
				?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val grade = root.path("grade").takeIf { !it.isMissingNode && !it.isNull }?.asDouble()
			val date = root.path("date").takeIf { !it.isMissingNode && !it.isNull }?.asLong()
			val realId = generateEvaluationId(referenceId)
			val evaluation = EvaluationModel(
				id = realId,
				referenceId = referenceId,
				termId = termId,
				attemptId = attemptId,
				subjectCode = inferSubjectCode(attemptId),
				type = type,
				scheduleMode = scheduleMode,
				grade = grade,
				maxGrade = maxGrade,
				date = date,
				isDone = isDone,
				revision = 1L,
			)

			state.anchorRevision += 1
			state.evaluationsById[realId] = evaluation
			val responseBody = buildMutationResponse(mutationId, evaluation)
			state.mutationResults[mutationId] = responseBody
			return jsonResponse(200, responseBody, POST_DELAY_MS)
		}

		private fun patchResponse(request: com.github.tomakehurst.wiremock.http.Request, pathSegments: List<String>): ResponseDefinition {
			val root = parseBody(request) ?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val mutationId = root.path("mutation_id").takeIf { it.isTextual }?.asText()
				?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val expectedRevision = root.path("expected_revision").takeIf { it.canConvertToLong() }?.asLong()
				?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val evaluationId = pathSegments.getOrNull(2)
				?: return jsonResponse(404, mapOf("error" to "evaluation_not_found"), ERROR_DELAY_MS)

			state.mutationResults[mutationId]?.let { cachedResponse ->
				return jsonResponse(200, cachedResponse, PATCH_DELAY_MS)
			}

			val evaluation = activeEvaluationById(evaluationId)
				?: return jsonResponse(404, mapOf("error" to "evaluation_not_found"), ERROR_DELAY_MS)

			if (expectedRevision != evaluation.revision) {
				return jsonResponse(409, mapOf("error" to "revision_conflict"), ERROR_DELAY_MS)
			}

			if (root.has("schedule_mode") && !root.get("schedule_mode").isNull) {
				evaluation.scheduleMode = root.get("schedule_mode").asText()
			}
			if (root.has("grade")) {
				evaluation.grade = if (root.get("grade").isNull) null else root.get("grade").asDouble()
			}
			if (root.has("max_grade") && !root.get("max_grade").isNull) {
				evaluation.maxGrade = root.get("max_grade").asDouble()
			}
			if (root.has("date")) {
				evaluation.date = if (root.get("date").isNull) null else root.get("date").asLong()
			}
			if (root.has("type") && !root.get("type").isNull) {
				evaluation.type = root.get("type").asInt()
			}
			if (root.has("is_done") && !root.get("is_done").isNull) {
				evaluation.isDone = root.get("is_done").asBoolean()
			}

			evaluation.revision += 1
			state.anchorRevision += 1
			val responseBody = buildMutationResponse(mutationId, evaluation)
			state.mutationResults[mutationId] = responseBody
			return jsonResponse(200, responseBody, PATCH_DELAY_MS)
		}

		private fun deleteResponse(request: com.github.tomakehurst.wiremock.http.Request, pathSegments: List<String>): ResponseDefinition {
			val root = parseBody(request) ?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val mutationId = root.path("mutation_id").takeIf { it.isTextual }?.asText()
				?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val expectedRevision = root.path("expected_revision").takeIf { it.canConvertToLong() }?.asLong()
				?: return jsonResponse(400, mapOf("error" to "invalid_payload"), ERROR_DELAY_MS)
			val evaluationId = pathSegments.getOrNull(2)
				?: return jsonResponse(404, mapOf("error" to "evaluation_not_found"), ERROR_DELAY_MS)

			state.mutationResults[mutationId]?.let { cachedResponse ->
				return jsonResponse(200, cachedResponse, DELETE_DELAY_MS)
			}

			val evaluation = activeEvaluationById(evaluationId)
				?: return jsonResponse(404, mapOf("error" to "evaluation_not_found"), ERROR_DELAY_MS)

			if (expectedRevision != state.anchorRevision) {
				return jsonResponse(409, mapOf("error" to "anchor_revision_conflict"), ERROR_DELAY_MS)
			}

			evaluation.deleted = true
			state.anchorRevision += 1
			val responseBody = buildDeleteResponse(mutationId, evaluation.id)
			state.mutationResults[mutationId] = responseBody
			return jsonResponse(200, responseBody, DELETE_DELAY_MS)
		}

		private fun activeEvaluations(): List<EvaluationModel> =
			state.evaluationsById.values
				.filterNot { it.deleted }
				.sortedWith(compareBy<EvaluationModel> { it.date ?: Long.MAX_VALUE }.thenBy { it.id })

		private fun activeEvaluationById(id: String): EvaluationModel? =
			state.evaluationsById[id]?.takeIf { !it.deleted }

		private fun parseBody(request: com.github.tomakehurst.wiremock.http.Request): JsonNode? {
			val body = request.bodyAsString.takeIf { it.isNotBlank() } ?: return null
			return runCatching { objectMapper.readTree(body) }.getOrNull()
		}

		private fun pathSegments(request: com.github.tomakehurst.wiremock.http.Request): List<String> =
			request.url
				.substringBefore("?")
				.trim('/')
				.split('/')
				.filter { it.isNotBlank() }

		private fun buildMutationResponse(mutationId: String, evaluation: EvaluationModel): Map<String, Any?> =
			linkedMapOf(
				"mutation_id" to mutationId,
				"anchor_revision" to state.anchorRevision,
				"evaluation_patch" to evaluation.toTemplateModel(),
			)

		private fun buildDeleteResponse(mutationId: String, removedEvaluationId: String): Map<String, Any?> =
			linkedMapOf(
				"mutation_id" to mutationId,
				"anchor_revision" to state.anchorRevision,
				"removed_evaluation_id" to removedEvaluationId,
			)

		private fun jsonResponse(status: Int, body: Any, delayMs: Int): ResponseDefinition =
			ResponseDefinitionBuilder.responseDefinition()
				.withStatus(status)
				.withHeader("Content-Type", "application/json")
				.withBody(objectMapper.writeValueAsString(body))
				.withFixedDelay(delayMs)
				.build()

		private fun EvaluationModel.toTemplateModel(): Map<String, Any?> =
			linkedMapOf(
				"id" to id,
				"reference_id" to referenceId,
				"term_id" to termId,
				"attempt_id" to attemptId,
				"subject_code" to subjectCode,
				"type" to type,
				"schedule_mode" to scheduleMode,
				"grade" to grade,
				"max_grade" to maxGrade,
				"date" to date,
				"is_done" to isDone,
				"revision" to revision,
			)

		private fun inferSubjectCode(attemptId: String): String =
			SUBJECT_CODE_PATTERN.find(attemptId)?.groupValues?.getOrNull(1) ?: attemptId

		private fun inferTermId(attemptId: String): String =
			SUBJECT_CODE_PATTERN.find(attemptId)?.groupValues?.getOrNull(2) ?: state.defaultTermId

		private fun generateEvaluationId(referenceId: String): String =
			"EV" + referenceId.filter(Char::isLetterOrDigit).uppercase().ifBlank { "NEWID" }

		private data class RuntimeState(
			var anchorRevision: Long,
			val defaultTermId: String,
			val evaluationsById: LinkedHashMap<String, EvaluationModel>,
			val mutationResults: MutableMap<String, Map<String, Any?>> = linkedMapOf(),
		)

		private data class EvaluationModel(
			val id: String,
			val referenceId: String,
			var termId: String,
			var attemptId: String,
			var subjectCode: String,
			var type: Int,
			var scheduleMode: String,
			var grade: Double?,
			var maxGrade: Double,
			var date: Long?,
			var isDone: Boolean,
			var revision: Long,
			var deleted: Boolean = false,
		)

		private companion object {
			private const val CONFIG_DIRECTORY = "config"
			private const val SETTINGS_FILENAME = "evaluations-base-state.json"
			private const val GET_DELAY_MS = 3000
			private const val POST_DELAY_MS = 1500
			private const val PATCH_DELAY_MS = 1500
			private const val DELETE_DELAY_MS = 1500
			private const val ERROR_DELAY_MS = 1000
			private const val DEFAULT_TERM_ID = "Q2026A"
			private val SUBJECT_CODE_PATTERN = Regex("^(.+?)(Q\\d{4}[A-Z])$")
		}
	}
}
