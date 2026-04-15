package com.gdavidpb.tuindice.mocks

import com.github.tomakehurst.wiremock.admin.model.GetScenariosResult
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.common.TextFile
import com.github.tomakehurst.wiremock.core.Admin
import com.github.tomakehurst.wiremock.extension.Extension
import com.github.tomakehurst.wiremock.extension.ExtensionFactory
import com.github.tomakehurst.wiremock.extension.TemplateModelDataProviderExtension
import com.github.tomakehurst.wiremock.extension.WireMockServices
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import java.time.Instant
import java.time.ZoneOffset
import java.util.regex.Pattern
import wiremock.com.fasterxml.jackson.databind.JsonNode
import wiremock.com.fasterxml.jackson.databind.ObjectMapper

class RecordScenarioExtensionFactory : ExtensionFactory {
	override fun create(services: WireMockServices): List<Extension> =
		listOf(RecordTemplateModelProvider(services))

	private class RecordTemplateModelProvider(services: WireMockServices) : TemplateModelDataProviderExtension {
		private val admin: Admin = services.admin
		private val objectMapper = ObjectMapper()
		private val baseState: BaseState = readBaseState(services.files)
		@Volatile
		private var latestRequestedAddedTerm: TermModel? = null

		override fun getName(): String = "record-template-model-provider"

		override fun provideTemplateModelData(serveEvent: ServeEvent): Map<String, Any> {
			val scenarioStates = currentScenarioStates().toMutableMap()
			applyPreviewTransition(scenarioStates, serveEvent.stubMapping)
			requestAddedTerm(serveEvent.request, scenarioStates)?.let { requestedTerm ->
				latestRequestedAddedTerm = requestedTerm
			}

			val terms = resolveVisibleTerms(
				scenarioStates = scenarioStates,
				addedTermOverride = latestRequestedAddedTerm,
			).sortedWith(DESCENDING_TERM_ORDER)

			return linkedMapOf(
				"record" to buildRecordModel(
					terms = terms,
					scenarioStates = scenarioStates,
				),
			)
		}

		private fun readBaseState(fileSource: FileSource): BaseState {
			try {
				val configFile: TextFile = fileSource.child(CONFIG_DIRECTORY).getTextFileNamed(CONFIG_FILENAME)
				val root = objectMapper.readTree(configFile.readContentsAsString())
				val profile = parseProfile(root.get("profile"))
				val terms = root.get("terms").map(::parseTerm)
				val addedTerm = parseTerm(root.get("added_term"))
				return BaseState(profile, terms, addedTerm)
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

		private fun parseTerm(node: JsonNode): TermModel {
			val attempts = node.get("attempts").map(::parseAttempt)
			return TermModel(
				id = node.get("id").asText(),
				name = node.get("name").asText(),
				startDate = node.get("start_date").asLong(),
				endDate = node.get("end_date").asLong(),
				kind = node.get("kind").asText(),
				revision = node.get("revision").asLong(),
				presenceScenario = if (node.hasNonNull("presence_scenario")) node.get("presence_scenario").asText() else null,
				attempts = attempts,
			)
		}

		private fun parseAttempt(node: JsonNode): AttemptModel =
			AttemptModel(
				id = node.get("id").asText(),
				termId = node.get("term_id").asText(),
				code = node.get("code").asText(),
				name = node.get("name").asText(),
				credits = node.get("credits").asInt(),
				grade = node.get("grade").asInt(),
				gradingMode = node.path("grading_mode").asText("numeric"),
				mutable = node.path("mutable").asBoolean(false),
				scenario = if (node.hasNonNull("scenario")) node.get("scenario").asText() else null,
				status = if (node.hasNonNull("status")) node.get("status").asText() else null,
				revision = node.get("revision").asLong(),
			)

		private fun currentScenarioStates(): Map<String, String> {
			val result: GetScenariosResult = admin.getAllScenarios()
			return result.scenarios.associate { scenario -> scenario.name to scenario.state }
		}

		private fun applyPreviewTransition(scenarioStates: MutableMap<String, String>, stubMapping: StubMapping?) {
			if (stubMapping == null || !stubMapping.modifiesScenarioState()) return

			val scenarioName = stubMapping.scenarioName
			val nextState = stubMapping.newScenarioState
			if (scenarioName == null || nextState == null) return

			scenarioStates[scenarioName] = nextState
		}

		private fun resolveVisibleTerms(
			scenarioStates: Map<String, String>,
			addedTermOverride: TermModel? = null,
		): List<TermModel> {
			val terms = mutableListOf<TermModel>()

			for (baseTerm in baseState.terms) {
				if (!isVisible(baseTerm, scenarioStates)) continue
				terms += baseTerm
			}

			val addedTerm = addedTermOverride ?: baseState.addedTerm
			if (isAddedTermVisible(baseState.addedTerm, scenarioStates)) {
				terms += resolveAddedTerm(addedTerm, scenarioStates)
			}

			terms.sortWith(DESCENDING_TERM_ORDER)
			return terms
		}

		private fun isVisible(baseTerm: TermModel, scenarioStates: Map<String, String>): Boolean {
			val presenceScenario = baseTerm.presenceScenario ?: return true
			val state = scenarioStates[presenceScenario] ?: Scenario.STARTED
			return !DELETED_STATE_PATTERN.matcher(state).matches()
		}

		private fun isAddedTermVisible(addedTerm: TermModel, scenarioStates: Map<String, String>): Boolean {
			val presenceScenario = addedTerm.presenceScenario ?: return false
			val state = scenarioStates[presenceScenario] ?: Scenario.STARTED
			return PRESENT_STATE_PATTERN.matcher(state).matches()
		}

		private fun resolveAddedTerm(addedTerm: TermModel, scenarioStates: Map<String, String>): TermModel {
			val presenceScenario = baseState.addedTerm.presenceScenario ?: return addedTerm
			val state = scenarioStates[presenceScenario] ?: Scenario.STARTED
			return addedTerm.copy(
				revision = parseTermRevision(state, addedTerm.revision),
				attempts = addedTerm.attempts.toList(),
			)
		}

		private fun requestAddedTerm(
			request: Request,
			scenarioStates: Map<String, String>,
		): TermModel? {
			val pathSegments = pathSegments(request)
			if (pathSegments != listOf("record", "v4", "overlay", "terms")) return null

			val requestBody = request.bodyAsString
				.takeIf { body -> body.isNotBlank() }
				?: return null
			val root = runCatching { objectMapper.readTree(requestBody) }
				.getOrNull()
				?: return null
			if (!root.hasNonNull("start_at") || !root.hasNonNull("end_at")) return null

			val attemptsNode = root.get("attempts")
				?: return null
			if (!attemptsNode.isArray || attemptsNode.size() == 0) return null
			val startAt = root.get("start_at").asLong()
			val endAt = root.get("end_at").asLong()

			val resolvedTerm = resolveAddedTerm(baseState.addedTerm, scenarioStates)
			val attempts = attemptsNode.mapIndexed { index, node ->
				val code = node.path("subject_code").asText(baseState.addedTerm.attempts.firstOrNull()?.code ?: "MOCK101")
				val scoreNode = node.path("score")
				val numericScore = if (scoreNode.path("type").asText() == "numeric") {
					scoreNode.path("value").asInt(0)
				} else {
					0
				}
				AttemptModel(
					id = "$code-${resolvedTerm.id}-${index + 1}",
					termId = resolvedTerm.id,
					code = code,
					name = node.path("subject_name").asText("MOCK $code"),
					credits = node.path("credits").asInt(DEFAULT_ADDED_ATTEMPT_CREDITS),
					grade = numericScore,
					gradingMode = node.path("grading_mode").asText("numeric"),
					mutable = false,
					scenario = null,
					status = if (node.hasNonNull("outcome")) node.get("outcome").asText() else null,
					revision = 1L,
				)
			}

			return resolvedTerm.copy(
				name = formatTermName(startAtMillis = startAt, endAtMillis = endAt),
				startDate = startAt,
				endDate = endAt,
				attempts = attempts,
			)
		}

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

		private fun buildRecordModel(
			terms: List<TermModel>,
			scenarioStates: Map<String, String>,
		): Map<String, Any> {
			val visibleTermIds = terms.mapTo(linkedSetOf(), TermModel::id)
			val attemptOverrides = baseState.terms
				.filter { term -> term.id in visibleTermIds }
				.flatMap { term ->
					term.attempts.mapNotNull { baseAttempt ->
						baseAttempt.toAttemptOverrideModel(
							resolved = resolveAttempt(baseAttempt, scenarioStates),
						)
					}
				}
			val resolvedAttemptRevision = terms
				.flatMap(TermModel::attempts)
				.maxOfOrNull(AttemptModel::revision)
				?: 0L
			val revision = maxOf(
				terms.maxOfOrNull(TermModel::revision) ?: 0L,
				resolvedAttemptRevision,
				attemptOverrides.maxOfOrNull { override -> override.getValue("updated_at") as Long } ?: 0L,
			)

			return linkedMapOf(
				"revision" to revision,
				"record" to linkedMapOf(
					"id" to "mock-record",
					"profile" to baseState.profile.toRecordProfileModel(),
					"terms" to terms.map(TermModel::toRecordTermModel),
					"attempt_overrides" to attemptOverrides,
				),
			)
		}

		private fun resolveAttempt(baseAttempt: AttemptModel, scenarioStates: Map<String, String>): AttemptModel {
			val scenario = baseAttempt.scenario
			if (!baseAttempt.mutable || scenario == null) return baseAttempt

			val state = scenarioStates[scenario] ?: Scenario.STARTED
			if (state == Scenario.STARTED) return baseAttempt

			val matcher = ATTEMPT_STATE_PATTERN.matcher(state)
			if (matcher.matches()) {
				return baseAttempt.copyWith(
					grade = matcher.group(2).toInt(),
					status = if (baseAttempt.gradingMode == "qualitative_pass_fail") "normal" else baseAttempt.status,
					revision = matcher.group(1).toLong(),
				)
			}

			val statusMatcher = ATTEMPT_STATUS_STATE_PATTERN.matcher(state)
			if (!statusMatcher.matches()) return baseAttempt

			return baseAttempt.copyWith(
				grade = 0,
				status = statusMatcher.group(2).lowercase(),
				revision = statusMatcher.group(1).toLong(),
			)
		}

		private fun parseTermRevision(state: String, fallback: Long): Long {
			val presentMatcher = PRESENT_STATE_PATTERN.matcher(state)
			if (presentMatcher.matches()) return presentMatcher.group(1).toLong()

			val deletedMatcher = DELETED_STATE_PATTERN.matcher(state)
			if (deletedMatcher.matches()) return deletedMatcher.group(1).toLong()

			return fallback
		}

		private fun pathSegments(request: Request): List<String> {
			val rawPath = request.url
			val queryIndex = rawPath.indexOf('?')
			val path = if (queryIndex >= 0) rawPath.substring(0, queryIndex) else rawPath
			if (path.isEmpty() || path == "/") return emptyList()

			return path.split('/')
				.filter(String::isNotBlank)
		}
	}

	private data class BaseState(
		val profile: ProfileModel,
		val terms: List<TermModel>,
		val addedTerm: TermModel,
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
		val presenceScenario: String?,
		val attempts: List<AttemptModel>,
		val revision: Long,
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
		val grade: Int,
		val gradingMode: String,
		val mutable: Boolean,
		val scenario: String?,
		val status: String?,
		val revision: Long,
	) {
		fun copyWith(grade: Int, status: String?, revision: Long): AttemptModel =
			copy(
				grade = grade,
				status = status,
				revision = revision,
			)

		fun toRecordAttemptModel(): Map<String, Any> =
			linkedMapOf(
				"id" to id,
				"subject_code" to code,
				"subject_name" to name,
				"credits" to credits,
				"grading_mode" to gradingMode,
				"official_score" to officialScoreModel(),
				"official_outcome" to canonicalOutcomeValue(status),
				"official_badge" to canonicalBadgeValue(status),
			)

		fun toAttemptOverrideModel(resolved: AttemptModel): Map<String, Any>? {
			val score = when {
				gradingMode != "numeric" -> null
				resolved.grade != grade -> linkedMapOf<String, Any>(
					"type" to "numeric",
					"value" to resolved.grade,
				)

				else -> null
			}
			val normalizedStatus = status ?: "normal"
			val normalizedResolvedStatus = resolved.status ?: "normal"
			val outcome = when {
				normalizedResolvedStatus != normalizedStatus -> canonicalOutcomeValue(resolved.status)
				else -> null
			}

			if ((score == null) && (outcome == null)) return null

			return linkedMapOf<String, Any>(
				"attempt_id" to id,
				"updated_at" to resolved.revision,
			).also { model ->
				score?.let { model["score"] = it }
				outcome?.let { model["outcome"] = it }
			}
		}

		private fun officialScoreModel(): Map<String, Any> =
			when {
				gradingMode != "numeric" -> linkedMapOf("type" to "empty")
				grade > 0 -> linkedMapOf<String, Any>(
					"type" to "numeric",
					"value" to grade,
				)

				else -> linkedMapOf("type" to "empty")
			}

		private fun canonicalOutcomeValue(sourceStatus: String?): String =
			when (sourceStatus ?: "normal") {
				"approved" -> "approved"
				"failed" -> "failed"
				"retired" -> "retired"
				"unreported" -> "unreported"
				else -> "pending"
			}

		private fun canonicalBadgeValue(sourceStatus: String?): String =
			if (sourceStatus == "without_effect") "without_effect" else "none"
	}

	private companion object {
		private const val CONFIG_DIRECTORY = "config"
		private const val CONFIG_FILENAME = "record-base-state.json"
		private const val DEFAULT_ADDED_ATTEMPT_CREDITS = 4
		private val ATTEMPT_STATE_PATTERN = Pattern.compile("^REV_(\\d+)_GRADE_(\\d+)$")
		private val ATTEMPT_STATUS_STATE_PATTERN = Pattern.compile("^REV_(\\d+)_STATUS_([A-Z_]+)$")
		private val PRESENT_STATE_PATTERN = Pattern.compile("^ADDED_R(\\d+)$")
		private val DELETED_STATE_PATTERN = Pattern.compile("^DELETED_R(\\d+)$")
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
