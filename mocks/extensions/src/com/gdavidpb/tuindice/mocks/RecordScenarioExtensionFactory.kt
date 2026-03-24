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

class RecordScenarioExtensionFactory : ExtensionFactory {
	override fun create(services: WireMockServices): List<Extension> =
		listOf(RecordTemplateModelProvider(services))

	private class RecordTemplateModelProvider(services: WireMockServices) : TemplateModelDataProviderExtension {
		private val admin: Admin = services.admin
		private val objectMapper = ObjectMapper()
		private val baseState: BaseState = readBaseState(services.files)
		@Volatile
		private var latestRequestedAddedQuarter: QuarterModel? = null

		override fun getName(): String = "record-template-model-provider"

		override fun provideTemplateModelData(serveEvent: ServeEvent): Map<String, Any> {
			val scenarioStates = currentScenarioStates().toMutableMap()
			applyPreviewTransition(scenarioStates, serveEvent.stubMapping)
			requestAddedQuarter(serveEvent.request, scenarioStates)?.let { requestedQuarter ->
				latestRequestedAddedQuarter = requestedQuarter
			}

			val quarters = recompute(
				resolveVisibleQuarters(
					scenarioStates = scenarioStates,
					addedQuarterOverride = latestRequestedAddedQuarter
				)
			).sortedWith(DESCENDING_QUARTER_ORDER)
			val pathSegments = pathSegments(serveEvent.request)
			val currentQuarter = findQuarter(quarters, currentQuarterId(pathSegments))
			val currentSubject = findSubject(currentQuarter, currentSubjectId(pathSegments))
			val addedQuarter = findQuarter(quarters, ADDED_QUARTER_ID)

			val record = linkedMapOf<String, Any>(
				"quarters" to quarters.map(QuarterModel::toTemplateModel),
			)
			currentQuarter?.let { record["currentQuarter"] = it.toTemplateModel() }
			currentSubject?.let { record["currentSubject"] = it.toTemplateModel() }
			addedQuarter?.let { record["addedQuarter"] = it.toTemplateModel() }

			return linkedMapOf("record" to record)
		}

		private fun readBaseState(fileSource: FileSource): BaseState {
			try {
				val configFile: TextFile = fileSource.child(CONFIG_DIRECTORY).getTextFileNamed(CONFIG_FILENAME)
				val root = objectMapper.readTree(configFile.readContentsAsString())
				val quarters = root.get("quarters").map(::parseQuarter)
				val addedQuarter = parseQuarter(root.get("added_quarter"))
				return BaseState(quarters, addedQuarter)
			} catch (exception: Exception) {
				throw IllegalStateException("Unable to load record base state for WireMock", exception)
			}
		}

		private fun parseQuarter(node: JsonNode): QuarterModel {
			val subjects = node.get("subjects").map(::parseSubject)
			return QuarterModel(
				id = node.get("id").asText(),
				name = node.get("name").asText(),
				startDate = node.get("start_date").asLong(),
				endDate = node.get("end_date").asLong(),
				grade = node.get("grade").asDouble(),
				gradeSum = node.get("grade_sum").asDouble(),
				credits = node.get("credits").asInt(),
				creditsSum = node.get("credits_sum").asInt(),
				current = node.get("is_current").asBoolean(),
				readOnly = node.get("is_read_only").asBoolean(),
				revision = node.get("revision").asLong(),
				presenceScenario = if (node.hasNonNull("presence_scenario")) node.get("presence_scenario").asText() else null,
				subjects = subjects,
			)
		}

		private fun parseSubject(node: JsonNode): SubjectModel =
			SubjectModel(
				id = node.get("id").asText(),
				quarterId = node.get("qid").asText(),
				code = node.get("code").asText(),
				name = node.get("name").asText(),
				credits = node.get("credits").asInt(),
				grade = node.get("grade").asInt(),
				revision = node.get("revision").asLong(),
				mutable = node.path("mutable").asBoolean(false),
				scenario = if (node.hasNonNull("scenario")) node.get("scenario").asText() else null,
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

		private fun resolveVisibleQuarters(
			scenarioStates: Map<String, String>,
			addedQuarterOverride: QuarterModel? = null
		): List<QuarterModel> {
			val quarters = mutableListOf<QuarterModel>()

			for (baseQuarter in baseState.quarters) {
				if (!isVisible(baseQuarter, scenarioStates)) continue
				quarters += resolveQuarter(baseQuarter, scenarioStates)
			}

			val addedQuarter = addedQuarterOverride ?: baseState.addedQuarter
			if (isAddedQuarterVisible(baseState.addedQuarter, scenarioStates)) {
				quarters += resolveAddedQuarter(addedQuarter, scenarioStates)
			}

			quarters.sortWith(DESCENDING_QUARTER_ORDER)
			return quarters
		}

		private fun isVisible(baseQuarter: QuarterModel, scenarioStates: Map<String, String>): Boolean {
			val presenceScenario = baseQuarter.presenceScenario ?: return true
			val state = scenarioStates[presenceScenario] ?: Scenario.STARTED
			return !DELETED_STATE_PATTERN.matcher(state).matches()
		}

		private fun isAddedQuarterVisible(addedQuarter: QuarterModel, scenarioStates: Map<String, String>): Boolean {
			val presenceScenario = addedQuarter.presenceScenario ?: return false
			val state = scenarioStates[presenceScenario] ?: Scenario.STARTED
			return PRESENT_STATE_PATTERN.matcher(state).matches()
		}

		private fun resolveQuarter(baseQuarter: QuarterModel, scenarioStates: Map<String, String>): QuarterModel {
			val subjects = baseQuarter.subjects.map { baseSubject -> resolveSubject(baseSubject, scenarioStates) }
			return baseQuarter.copyWith(
				grade = baseQuarter.grade,
				gradeSum = baseQuarter.gradeSum,
				credits = baseQuarter.credits,
				creditsSum = baseQuarter.creditsSum,
				revision = baseQuarter.revision,
				subjects = subjects,
			)
		}

		private fun resolveAddedQuarter(addedQuarter: QuarterModel, scenarioStates: Map<String, String>): QuarterModel {
			val presenceScenario = baseState.addedQuarter.presenceScenario ?: return addedQuarter
			val state = scenarioStates[presenceScenario] ?: Scenario.STARTED
			return addedQuarter.copyWith(
				grade = addedQuarter.grade,
				gradeSum = addedQuarter.gradeSum,
				credits = addedQuarter.credits,
				creditsSum = addedQuarter.creditsSum,
				revision = parseQuarterRevision(state, addedQuarter.revision),
				subjects = addedQuarter.subjects.toList(),
			)
		}

		private fun requestAddedQuarter(
			request: Request,
			scenarioStates: Map<String, String>
		): QuarterModel? {
			val pathSegments = pathSegments(request)
			if (pathSegments != listOf("quarters", "v1")) return null

			val requestBody = request.bodyAsString
				.takeIf { body -> body.isNotBlank() }
				?: return null
			val root = runCatching { objectMapper.readTree(requestBody) }
				.getOrNull()
				?: return null
			if (!root.hasNonNull("quarter") || !root.hasNonNull("year")) return null

			val subjectsNode = root.get("subjects")
				?: return null
			if (!subjectsNode.isArray || subjectsNode.size() == 0) return null

			val resolvedQuarter = resolveAddedQuarter(baseState.addedQuarter, scenarioStates)
			val subjects = subjectsNode.mapIndexed { index, node ->
				val code = node.path("code").asText(baseState.addedQuarter.subjects.firstOrNull()?.code ?: "MOCK101")
				SubjectModel(
					id = "$code-${resolvedQuarter.id}-${index + 1}",
					quarterId = resolvedQuarter.id,
					code = code,
					name = "MOCK $code",
					credits = node.path("credits").asInt(DEFAULT_ADDED_SUBJECT_CREDITS),
					grade = node.path("grade").asInt(0),
					revision = 1L,
					mutable = false,
					scenario = null,
				)
			}

			return resolvedQuarter.copy(
				name = "${root.get("year").asInt()}-${root.get("quarter").asInt()}",
				subjects = subjects,
			)
		}

		private fun resolveSubject(baseSubject: SubjectModel, scenarioStates: Map<String, String>): SubjectModel {
			val scenario = baseSubject.scenario
			if (!baseSubject.mutable || scenario == null) return baseSubject

			val state = scenarioStates[scenario] ?: Scenario.STARTED
			if (state == Scenario.STARTED) return baseSubject

			val matcher = SUBJECT_STATE_PATTERN.matcher(state)
			if (!matcher.matches()) return baseSubject

			return baseSubject.copyWith(
				grade = matcher.group(2).toInt(),
				revision = matcher.group(1).toLong(),
			)
		}

		private fun parseQuarterRevision(state: String, fallback: Long): Long {
			val presentMatcher = PRESENT_STATE_PATTERN.matcher(state)
			if (presentMatcher.matches()) return presentMatcher.group(1).toLong()

			val deletedMatcher = DELETED_STATE_PATTERN.matcher(state)
			if (deletedMatcher.matches()) return deletedMatcher.group(1).toLong()

			return fallback
		}

		private fun recompute(quarters: List<QuarterModel>): List<QuarterModel> {
			if (quarters.isEmpty()) return emptyList()

			val ascending = quarters.sortedWith(ASCENDING_QUARTER_ORDER)
			val codeStates = mutableMapOf<String, CodeState>()
			val recomputedAscending = mutableListOf<QuarterModel>()

			var cumulativeWeighted = 0L
			var cumulativeCredits = 0L

			for (quarter in ascending) {
				var quarterCredits = 0L
				var quarterWeighted = 0L

				for (subject in quarter.subjects) {
					if (subject.grade != 0) {
						quarterCredits += subject.credits.toLong()
					}
					quarterWeighted += subject.grade.toLong() * subject.credits.toLong()
				}

				val quarterGrade = computeAverage(quarterWeighted, quarterCredits)
				val sortedSubjects = quarter.subjects.sortedByDescending(SubjectModel::id)

				for (subject in sortedSubjects) {
					if (subject.grade <= 0) continue

					val state = codeStates.getOrPut(subject.code) { CodeState() }
					val previousWeighted = state.effectiveWeighted()
					val previousCredits = state.effectiveCredits()

					state.add(CodeAttempt(subject.grade, subject.credits))

					cumulativeWeighted += state.effectiveWeighted() - previousWeighted
					cumulativeCredits += state.effectiveCredits() - previousCredits
				}

				recomputedAscending += quarter.copyWith(
					grade = quarterGrade,
					gradeSum = computeAverage(cumulativeWeighted, cumulativeCredits),
					credits = quarterCredits.toInt(),
					creditsSum = cumulativeCredits.toInt(),
					revision = quarter.revision,
					subjects = quarter.subjects.toList(),
				)
			}

			return recomputedAscending.reversed()
		}

		private fun computeAverage(weighted: Long, credits: Long): Double {
			if (credits == 0L) return 0.0

			return BigDecimal.valueOf(weighted.toDouble() / credits.toDouble())
				.setScale(4, RoundingMode.HALF_UP)
				.toDouble()
		}

		private fun pathSegments(request: Request): List<String> {
			val rawPath = request.url
			val queryIndex = rawPath.indexOf('?')
			val path = if (queryIndex >= 0) rawPath.substring(0, queryIndex) else rawPath
			if (path.isEmpty() || path == "/") return emptyList()

			return path.split('/')
				.filter(String::isNotBlank)
		}

		private fun currentQuarterId(pathSegments: List<String>): String? =
			if (
				pathSegments.size >= 3 &&
				pathSegments[0] == "quarters" &&
				pathSegments[1] == "v1"
			) {
				pathSegments[2]
			} else {
				null
			}

		private fun currentSubjectId(pathSegments: List<String>): String? =
			if (
				pathSegments.size >= 5 &&
				pathSegments[0] == "quarters" &&
				pathSegments[1] == "v1" &&
				pathSegments[3] == "subjects"
			) {
				pathSegments[4]
			} else {
				null
			}

		private fun findQuarter(quarters: List<QuarterModel>, quarterId: String?): QuarterModel? =
			quarterId?.let { candidate -> quarters.find { quarter -> quarter.id == candidate } }

		private fun findSubject(quarter: QuarterModel?, subjectId: String?): SubjectModel? =
			if (quarter == null || subjectId == null) {
				null
			} else {
				quarter.subjects.find { subject -> subject.id == subjectId }
			}
	}

	private data class BaseState(
		val quarters: List<QuarterModel>,
		val addedQuarter: QuarterModel,
	)

	private data class QuarterModel(
		val id: String,
		val name: String,
		val startDate: Long,
		val endDate: Long,
		val grade: Double,
		val gradeSum: Double,
		val credits: Int,
		val creditsSum: Int,
		val current: Boolean,
		val readOnly: Boolean,
		val revision: Long,
		val presenceScenario: String?,
		val subjects: List<SubjectModel>,
	) {
		fun copyWith(
			grade: Double,
			gradeSum: Double,
			credits: Int,
			creditsSum: Int,
			revision: Long,
			subjects: List<SubjectModel>,
		): QuarterModel =
			copy(
				grade = grade,
				gradeSum = gradeSum,
				credits = credits,
				creditsSum = creditsSum,
				revision = revision,
				subjects = subjects,
			)

		fun toTemplateModel(): Map<String, Any> =
			linkedMapOf(
				"id" to id,
				"name" to name,
				"start_date" to startDate,
				"end_date" to endDate,
				"grade" to grade,
				"grade_sum" to gradeSum,
				"credits" to credits,
				"credits_sum" to creditsSum,
				"is_current" to current,
				"is_read_only" to readOnly,
				"revision" to revision,
				"subjects" to subjects.map(SubjectModel::toTemplateModel),
			)
	}

	private data class SubjectModel(
		val id: String,
		val quarterId: String,
		val code: String,
		val name: String,
		val credits: Int,
		val grade: Int,
		val revision: Long,
		val mutable: Boolean,
		val scenario: String?,
	) {
		fun copyWith(grade: Int, revision: Long): SubjectModel =
			copy(
				grade = grade,
				revision = revision,
			)

		fun toTemplateModel(): Map<String, Any> =
			linkedMapOf(
				"id" to id,
				"qid" to quarterId,
				"code" to code,
				"name" to name,
				"credits" to credits,
				"grade" to grade,
				"revision" to revision,
			)
	}

	private data class CodeAttempt(
		val grade: Int,
		val credits: Int,
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
			return if (latestAttempt.grade >= 3) weightedSum - secondAttempt.weighted() else weightedSum
		}

		fun effectiveCredits(): Long {
			val latestAttempt = latest ?: return 0L
			val secondAttempt = second ?: return creditsSum
			return if (latestAttempt.grade >= 3) creditsSum - secondAttempt.credits.toLong() else creditsSum
		}
	}

	private companion object {
		private const val CONFIG_DIRECTORY = "config"
		private const val CONFIG_FILENAME = "record-base-state.json"
		private const val ADDED_QUARTER_ID = "MOCK-ADDED-QUARTER"
		private const val DEFAULT_ADDED_SUBJECT_CREDITS = 4
		private val SUBJECT_STATE_PATTERN = Pattern.compile("^REV_(\\d+)_GRADE_(\\d+)$")
		private val PRESENT_STATE_PATTERN = Pattern.compile("^ADDED_R(\\d+)$")
		private val DELETED_STATE_PATTERN = Pattern.compile("^DELETED_R(\\d+)$")
		private val DESCENDING_QUARTER_ORDER = compareByDescending<QuarterModel> { it.startDate }
			.thenBy { it.id }
		private val ASCENDING_QUARTER_ORDER = compareBy<QuarterModel> { it.startDate }
			.thenByDescending { it.id }
	}
}
