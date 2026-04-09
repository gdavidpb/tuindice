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
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.regex.Pattern
import wiremock.com.fasterxml.jackson.databind.JsonNode
import wiremock.com.fasterxml.jackson.databind.ObjectMapper

private const val OFFICIAL_HISTORICAL_TERM_KIND = "official_historical"
private const val OFFICIAL_CURRENT_TERM_KIND = "official_current"
private const val SYNTHETIC_TERM_KIND = "synthetic"

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
				addedTermOverride = latestRequestedAddedTerm
			).sortedWith(DESCENDING_TERM_ORDER)

			return linkedMapOf(
				"record" to buildRecordModel(
					terms = terms,
					scenarioStates = scenarioStates
				)
			)
		}

		private fun readBaseState(fileSource: FileSource): BaseState {
			try {
				val configFile: TextFile = fileSource.child(CONFIG_DIRECTORY).getTextFileNamed(CONFIG_FILENAME)
				val root = objectMapper.readTree(configFile.readContentsAsString())
				val terms = root.get("terms").map(::parseTerm)
				val addedTerm = parseTerm(root.get("added_term"))
				return BaseState(terms, addedTerm)
			} catch (exception: Exception) {
				throw IllegalStateException("Unable to load record base state for WireMock", exception)
			}
		}

		private fun parseTerm(node: JsonNode): TermModel {
			val attempts = node.get("attempts").map(::parseAttempt)
			return TermModel(
				id = node.get("id").asText(),
				name = node.get("name").asText(),
				startDate = node.get("start_date").asLong(),
				endDate = node.get("end_date").asLong(),
				grade = node.get("grade").asDouble(),
				gradeSum = node.get("grade_sum").asDouble(),
				credits = node.get("credits").asInt(),
				creditsSum = node.get("credits_sum").asInt(),
				simulationGrade = node.path("simulation_grade").takeIf(JsonNode::isNumber)?.asDouble(),
				simulationGradeSum = node.path("simulation_grade_sum").takeIf(JsonNode::isNumber)?.asDouble(),
				simulationCredits = node.path("simulation_credits").takeIf(JsonNode::isInt)?.asInt(),
				simulationCreditsSum = node.path("simulation_credits_sum").takeIf(JsonNode::isInt)?.asInt(),
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
				simulationStatus = if (node.hasNonNull("simulation_status")) node.get("simulation_status").asText() else null,
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
			addedTermOverride: TermModel? = null
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

		private fun resolveTerm(baseTerm: TermModel, scenarioStates: Map<String, String>): TermModel {
			val attempts = baseTerm.attempts.map { baseAttempt -> resolveAttempt(baseAttempt, scenarioStates) }
			return baseTerm.copyWith(
				grade = baseTerm.grade,
				gradeSum = baseTerm.gradeSum,
				credits = baseTerm.credits,
				creditsSum = baseTerm.creditsSum,
				revision = baseTerm.revision,
				attempts = attempts,
			)
		}

		private fun resolveAddedTerm(addedTerm: TermModel, scenarioStates: Map<String, String>): TermModel {
			val presenceScenario = baseState.addedTerm.presenceScenario ?: return addedTerm
			val state = scenarioStates[presenceScenario] ?: Scenario.STARTED
			return addedTerm.copyWith(
				grade = addedTerm.grade,
				gradeSum = addedTerm.gradeSum,
				credits = addedTerm.credits,
				creditsSum = addedTerm.creditsSum,
				revision = parseTermRevision(state, addedTerm.revision),
				attempts = addedTerm.attempts.toList(),
			)
		}

		private fun requestAddedTerm(
			request: Request,
			scenarioStates: Map<String, String>
		): TermModel? {
			val pathSegments = pathSegments(request)
			if (pathSegments != listOf("record", "v3", "overlay", "terms")) return null

			val requestBody = request.bodyAsString
				.takeIf { body -> body.isNotBlank() }
				?: return null
			val root = runCatching { objectMapper.readTree(requestBody) }
				.getOrNull()
				?: return null
			if (!root.hasNonNull("label") || !root.hasNonNull("start_at") || !root.hasNonNull("end_at")) return null

			val attemptsNode = root.get("attempts")
				?: return null
			if (!attemptsNode.isArray || attemptsNode.size() == 0) return null

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
					simulationStatus = null,
					revision = 1L,
				)
			}

			return resolvedTerm.copy(
				name = root.get("label").asText(),
				startDate = root.get("start_at").asLong(),
				endDate = root.get("end_at").asLong(),
				attempts = attempts,
			)
		}

		private fun buildRecordModel(
			terms: List<TermModel>,
			scenarioStates: Map<String, String>
		): Map<String, Any> {
			val visibleTermIds = terms.mapTo(linkedSetOf(), TermModel::id)
			val attemptOverrides = baseState.terms
				.filter { term -> term.id in visibleTermIds }
				.flatMap { term ->
					term.attempts.mapNotNull { baseAttempt ->
						baseAttempt.toAttemptOverrideModel(
							resolved = resolveAttempt(baseAttempt, scenarioStates)
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
				attemptOverrides.maxOfOrNull { override -> override.getValue("updated_at") as Long } ?: 0L
			)

			return linkedMapOf(
				"revision" to revision,
				"record" to linkedMapOf(
					"id" to "mock-record",
					"profile" to mockProfileModel(),
					"terms" to terms.map(TermModel::toRecordTermModel),
					"attempt_overrides" to attemptOverrides
				)
			)
		}

		private fun mockProfileModel(): Map<String, Any> =
			linkedMapOf(
				"user_id" to "mock-user",
				"identity_card_number" to 12345678,
				"usb_id" to "00000000",
				"email" to "mock@tuindice.app",
				"first_names" to "Mock",
				"last_names" to "User",
				"career_name" to "Ingenieria Civil Electronica",
				"career_code" to 12039,
				"scholarship" to false
			)

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

		private fun recompute(terms: List<TermModel>): List<TermModel> {
			if (terms.isEmpty()) return emptyList()

			val ascending = terms.sortedWith(ASCENDING_TERM_ORDER)
			val codeStates = mutableMapOf<String, CodeState>()
			val recomputedAscending = mutableListOf<TermModel>()

			var cumulativeWeighted = 0L
			var cumulativeCredits = 0L

			for (term in ascending) {
				var termAverageCredits = 0L
				var termDisplayedCredits = 0L
				var termWeighted = 0L

				for (attempt in term.attempts) {
					termAverageCredits += attempt.numericCreditsContribution().toLong()
					termDisplayedCredits += attempt.displayedPeriodCreditsContribution().toLong()
					termWeighted += attempt.numericWeightedContribution()
				}

				val termGrade = computeAverage(termWeighted, termAverageCredits)
				val sortedAttempts = term.attempts.sortedByDescending(AttemptModel::id)

				for (attempt in sortedAttempts) {
					if (!attempt.countsTowardRetakeTimeline()) continue

					val state = codeStates.getOrPut(attempt.code) { CodeState() }
					val previousWeighted = state.effectiveWeighted()
					val previousCredits = state.effectiveCredits()

					state.add(CodeAttempt(attempt.grade, attempt.credits, attempt.isApprovalEvent()))

					cumulativeWeighted += state.effectiveWeighted() - previousWeighted
					cumulativeCredits += state.effectiveCredits() - previousCredits
				}

				recomputedAscending += term.copyWith(
					grade = termGrade,
					gradeSum = computeAverage(cumulativeWeighted, cumulativeCredits),
					credits = termDisplayedCredits.toInt(),
					creditsSum = cumulativeCredits.toInt(),
					revision = term.revision,
					attempts = term.attempts.toList(),
				)
			}

			return applySimulationProjection(recomputedAscending.reversed())
		}

		private fun computeAverage(weighted: Long, credits: Long): Double {
			if (credits == 0L) return 0.0

			return BigDecimal.valueOf(weighted.toDouble() / credits.toDouble())
				.setScale(4, RoundingMode.HALF_UP)
				.toDouble()
		}

		private fun applySimulationProjection(terms: List<TermModel>): List<TermModel> {
			if (terms.isEmpty()) return emptyList()

			val ascending = terms.sortedWith(ASCENDING_TERM_ORDER)
			val excludedAttemptIds = resolveExcludedAttemptIds(ascending)
			val codeStates = mutableMapOf<String, CodeState>()

			var cumulativeWeighted = 0L
			var cumulativeCredits = 0L

			val recomputedAscending = ascending.map { term ->
				var termWeighted = 0L
				var termAverageCredits = 0L
				var termDisplayedCredits = 0L

				val attempts = term.attempts.map { attempt ->
					val simulationStatus = when {
						(attempt.status != null) && (attempt.status != "normal") -> null
						attempt.id in excludedAttemptIds -> "without_effect"
						else -> null
					}

					termWeighted += attempt.numericWeightedContribution()
					termAverageCredits += attempt.numericCreditsContribution().toLong()
					termDisplayedCredits += attempt.displayedPeriodCreditsContribution().toLong()

					attempt.copy(simulationStatus = simulationStatus)
				}

				val sortedAttempts = term.attempts.sortedByDescending(AttemptModel::id)

				for (attempt in sortedAttempts) {
					if (!attempt.countsTowardRetakeTimeline()) continue

					val state = codeStates.getOrPut(attempt.code) { CodeState() }
					val previousWeighted = state.effectiveWeighted()
					val previousCredits = state.effectiveCredits()

					state.add(CodeAttempt(attempt.grade, attempt.credits, attempt.isApprovalEvent()))

					cumulativeWeighted += state.effectiveWeighted() - previousWeighted
					cumulativeCredits += state.effectiveCredits() - previousCredits
				}

				if (term.isReadOnly) {
					term.copy(
						simulationGrade = term.grade,
						simulationGradeSum = term.gradeSum,
						simulationCredits = term.credits,
						simulationCreditsSum = term.creditsSum,
						attempts = attempts
					)
				} else {
					term.copy(
						simulationGrade = computeAverage(termWeighted, termAverageCredits),
						simulationGradeSum = computeAverage(cumulativeWeighted, cumulativeCredits),
						simulationCredits = termDisplayedCredits.toInt(),
						simulationCreditsSum = cumulativeCredits.toInt(),
						attempts = attempts
					)
				}
			}

			return recomputedAscending.reversed()
		}

		private fun resolveExcludedAttemptIds(terms: List<TermModel>): Set<String> {
			val codeStates = mutableMapOf<String, SimulationCodeState>()

			terms.forEach { term ->
				val sortedAttempts = term.attempts.sortedByDescending(AttemptModel::id)

				sortedAttempts.forEach { attempt ->
					if (!attempt.countsTowardRetakeTimeline()) return@forEach

					codeStates
						.getOrPut(attempt.code) { SimulationCodeState() }
						.add(
							SimulationAttempt(
								subjectId = attempt.id,
								approved = attempt.isApprovalEvent()
							)
						)
				}
			}

			return buildSet {
				codeStates.values.forEach { state ->
					state.previousSubjectIdWithoutEffect()?.let(::add)
				}
			}
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
		val terms: List<TermModel>,
		val addedTerm: TermModel,
	)

	private data class TermModel(
		val id: String,
		val name: String,
		val startDate: Long,
		val endDate: Long,
		val grade: Double,
		val gradeSum: Double,
		val credits: Int,
		val creditsSum: Int,
		val simulationGrade: Double? = null,
		val simulationGradeSum: Double? = null,
		val simulationCredits: Int? = null,
		val simulationCreditsSum: Int? = null,
		val kind: String,
		val presenceScenario: String?,
		val attempts: List<AttemptModel>,
		val revision: Long,
	) {
		val isCurrent: Boolean
			get() = kind == OFFICIAL_CURRENT_TERM_KIND

		val isReadOnly: Boolean
			get() = kind == OFFICIAL_HISTORICAL_TERM_KIND

		fun copyWith(
			grade: Double,
			gradeSum: Double,
			credits: Int,
			creditsSum: Int,
			revision: Long,
			attempts: List<AttemptModel>,
		): TermModel =
			copy(
				grade = grade,
				gradeSum = gradeSum,
				credits = credits,
				creditsSum = creditsSum,
				revision = revision,
				attempts = attempts,
			)

		fun toTemplateModel(): Map<String, Any> =
			linkedMapOf<String, Any>(
				"id" to id,
				"name" to name,
				"start_date" to startDate,
				"end_date" to endDate,
				"grade" to grade,
				"grade_sum" to gradeSum,
				"credits" to credits,
				"credits_sum" to creditsSum,
				"kind" to kind,
				"revision" to revision,
				"attempts" to attempts.map(AttemptModel::toTemplateModel),
			).also { model ->
				simulationGrade?.let { model["simulation_grade"] = it }
				simulationGradeSum?.let { model["simulation_grade_sum"] = it }
				simulationCredits?.let { model["simulation_credits"] = it }
				simulationCreditsSum?.let { model["simulation_credits_sum"] = it }
			}

		fun toOfficialProjectionModel(): Map<String, Any> =
			toProjectionModel(simulation = false)

		fun toSimulationProjectionModel(): Map<String, Any> =
			toProjectionModel(simulation = true)

		fun toRecordTermModel(): Map<String, Any> =
			linkedMapOf(
				"id" to id,
				"label" to name,
				"start_at" to startDate,
				"end_at" to endDate,
				"term_kind" to kind,
				"attempts" to attempts.map(AttemptModel::toRecordAttemptModel)
			)

		private fun toProjectionModel(simulation: Boolean): Map<String, Any> =
			linkedMapOf<String, Any>(
				"id" to id,
				"label" to name,
				"start_at" to startDate,
				"end_at" to endDate,
				"term_kind" to kind,
				"grade" to if (simulation) (simulationGrade ?: grade) else grade,
				"grade_sum" to if (simulation) (simulationGradeSum ?: gradeSum) else gradeSum,
				"credits" to if (simulation) (simulationCredits ?: credits) else credits,
				"credits_sum" to if (simulation) (simulationCreditsSum ?: creditsSum) else creditsSum,
				"attempts" to attempts.mapIndexed { index, attempt ->
					attempt.toProjectionModel(
						termId = id,
						sequenceInTerm = index,
						simulation = simulation
					)
				}
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
		val simulationStatus: String?,
		val revision: Long,
	) {
		fun copyWith(grade: Int, status: String?, revision: Long): AttemptModel =
			copy(
				grade = grade,
				status = status,
				revision = revision,
			)

		fun toTemplateModel(): Map<String, Any> =
			linkedMapOf<String, Any>(
				"id" to id,
				"term_id" to termId,
				"code" to code,
				"name" to name,
				"credits" to credits,
				"grade" to grade,
				"grading_mode" to gradingMode,
			).also { model ->
				status?.let { model["status"] = it }
				simulationStatus?.let { model["simulation_status"] = it }
				model["revision"] = revision
			}

		fun toProjectionModel(
			termId: String,
			sequenceInTerm: Int,
			simulation: Boolean
		): Map<String, Any> {
			val resolvedStatus = if (simulation) simulationStatus ?: status else status
			val badge = if (resolvedStatus == "without_effect") "without_effect" else "none"
			val outcome = projectionOutcome(resolvedStatus)

			return linkedMapOf<String, Any>(
				"id" to id,
				"term_id" to termId,
				"subject_code" to code,
				"subject_name" to name,
				"credits" to credits,
				"sequence_in_term" to sequenceInTerm,
				"grading_mode" to gradingMode,
				"score" to linkedMapOf<String, Any>(
					"kind" to if (gradingMode == "numeric") "numeric" else "empty",
					"numeric_value" to if (gradingMode == "numeric") grade else 0
				),
				"outcome" to outcome,
				"badge" to badge
			)
		}

		private fun projectionOutcome(resolvedStatus: String?): String {
			return when (resolvedStatus) {
				"approved" -> "approved"
				"failed" -> "failed"
				"retired" -> "retired"
				"unreported" -> "unreported"
				"without_effect",
				null,
				"normal",
				"pending" -> basePendingAwareOutcome()

				else -> basePendingAwareOutcome()
			}
		}

		private fun basePendingAwareOutcome(): String {
			return when {
				gradingMode == "qualitative_pass_fail" -> "pending"
				grade >= 3 -> "approved"
				grade > 0 -> "failed"
				else -> "pending"
			}
		}

		fun toRecordAttemptModel(): Map<String, Any> =
			linkedMapOf<String, Any>(
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
					"value" to resolved.grade
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
					"value" to grade
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

		fun resolvedOutcome(): String {
			return when (status ?: "normal") {
				"unreported",
				"approved",
				"failed",
				"retired",
				"without_effect" -> status ?: "normal"

				else -> when (gradingMode) {
					"qualitative_pass_fail" -> "normal"
					else -> when {
						grade >= 3 -> "approved"
						grade > 0 -> "failed"
						else -> "normal"
					}
				}
			}
		}

		fun countsTowardQuarterNumericAverage(): Boolean =
			(gradingMode == "numeric") &&
				(resolvedOutcome() !in setOf("normal", "retired")) &&
				((grade > 0) || (resolvedOutcome() == "unreported"))

		fun countsTowardNumericAverage(): Boolean =
			(gradingMode == "numeric") &&
				(resolvedOutcome() !in setOf("normal", "retired", "without_effect", "unreported")) &&
				(grade > 0)

		fun numericCreditsContribution(): Int =
			if (countsTowardQuarterNumericAverage()) credits else 0

		fun displayedPeriodCreditsContribution(): Int =
			when {
				gradingMode == "qualitative_pass_fail" -> if (resolvedOutcome() != "retired") credits else 0
				else -> numericCreditsContribution()
			}

		fun numericWeightedContribution(): Long =
			if (countsTowardQuarterNumericAverage()) grade.toLong() * credits.toLong() else 0L

		fun isApprovalEvent(): Boolean =
			resolvedOutcome() == "approved"

		fun isResolvedQualitativeOutcome(): Boolean =
			(gradingMode == "qualitative_pass_fail") &&
				(resolvedOutcome() in setOf("approved", "failed"))

		fun countsTowardRetakeTimeline(): Boolean =
			countsTowardNumericAverage() || isResolvedQualitativeOutcome()
	}

	private data class CodeAttempt(
		val grade: Int,
		val credits: Int,
		val approved: Boolean,
	) {
		fun weighted(): Long = grade.toLong() * credits.toLong()
	}

	private class CodeState {
		private var latest: CodeAttempt? = null
		private var second: CodeAttempt? = null
		private var weightedSum = 0L
		private var creditsSum = 0L

		fun add(attempt: CodeAttempt) {
			second = latest
			latest = attempt
			weightedSum += attempt.weighted()
			creditsSum += attempt.credits.toLong()
		}

		fun effectiveWeighted(): Long {
			val latestAttempt = latest ?: return 0L
			val secondAttempt = second ?: return weightedSum
			return if (latestAttempt.approved) weightedSum - secondAttempt.weighted() else weightedSum
		}

		fun effectiveCredits(): Long {
			val latestAttempt = latest ?: return 0L
			val secondAttempt = second ?: return creditsSum
			return if (latestAttempt.approved) creditsSum - secondAttempt.credits.toLong() else creditsSum
		}
	}

	private data class SimulationAttempt(
		val subjectId: String,
		val approved: Boolean,
	)

	private class SimulationCodeState {
		private var latest: SimulationAttempt? = null
		private var second: SimulationAttempt? = null

		fun add(attempt: SimulationAttempt) {
			second = latest
			latest = attempt
		}

		fun previousSubjectIdWithoutEffect(): String? {
			val latestAttempt = latest
			val secondAttempt = second

			return if (
				(latestAttempt != null) &&
				(secondAttempt != null) &&
				latestAttempt.approved
			) {
				secondAttempt.subjectId
			} else {
				null
			}
		}
	}

	private companion object {
		private const val CONFIG_DIRECTORY = "config"
		private const val CONFIG_FILENAME = "record-base-state.json"
		private const val ADDED_TERM_ID = "MOCK-ADDED-QUARTER"
		private const val DEFAULT_ADDED_ATTEMPT_CREDITS = 4
		private val ATTEMPT_STATE_PATTERN = Pattern.compile("^REV_(\\d+)_GRADE_(\\d+)$")
		private val ATTEMPT_STATUS_STATE_PATTERN = Pattern.compile("^REV_(\\d+)_STATUS_([A-Z_]+)$")
		private val PRESENT_STATE_PATTERN = Pattern.compile("^ADDED_R(\\d+)$")
		private val DELETED_STATE_PATTERN = Pattern.compile("^DELETED_R(\\d+)$")
		private val DESCENDING_TERM_ORDER = compareByDescending<TermModel> { it.startDate }
			.thenBy { it.id }
		private val ASCENDING_TERM_ORDER = compareBy<TermModel> { it.startDate }
			.thenByDescending { it.id }
	}
}
