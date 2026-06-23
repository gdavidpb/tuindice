@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicPensumStatusEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.isCurrent
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.base.domain.utils.SubjectCatalogSearchNormalizer
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumSelectionDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectCatalogCacheDao
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectCatalogCacheEntity
import com.gdavidpb.tuindice.record.data.model.CreateSyntheticTermPensumCacheResponse
import com.gdavidpb.tuindice.record.data.model.CreateSyntheticTermSubjectSearchResponse
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationSnapshot
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailabilityDetail
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermCreationRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SyntheticTermCreationDataSource(
	private val academicRecordRepository: AcademicRecordRepository,
	private val pensumCacheDao: PensumCacheDao,
	private val pensumSelectionDao: PensumSelectionDao,
	private val subjectCatalogCacheDao: SubjectCatalogCacheDao,
	private val ktorClient: HttpClient,
	private val json: Json
) : SyntheticTermCreationRepository {
	private val academicPensumStatusEngine = AcademicPensumStatusEngine()

	@OptIn(ExperimentalCoroutinesApi::class)
	override fun observeSnapshot(
		queryFlow: StateFlow<String>,
		selectedSubjectsFlow: StateFlow<List<SyntheticTermSubject>>,
		selectedPeriodKeyFlow: StateFlow<String?>,
		editingTermIdFlow: StateFlow<String?>,
		editingTermKeyFlow: StateFlow<String?>
	): Flow<SyntheticTermCreationSnapshot> {
		val recordFlow = observeRecord()
		val pensumFlow = observePensum()
		val searchFlow = observeLocalSearch(queryFlow)
		val selectionFlow = combine(
			selectedSubjectsFlow,
			selectedPeriodKeyFlow,
			editingTermIdFlow,
			editingTermKeyFlow
		) { selectedSubjects, selectedPeriodKey, editingTermId, editingTermKey ->
			FormSelectionState(
				selectedSubjects = selectedSubjects,
				selectedPeriodKey = selectedPeriodKey,
				editingTermId = editingTermId,
				editingTermKey = editingTermKey
			)
		}

		return combine(
			recordFlow,
			pensumFlow,
			searchFlow,
			selectionFlow
		) { record, pensum, searchResults, selection ->
			val periodOptions = record.periodOptions(editingTermId = selection.editingTermId)
			val selectedPeriod = periodOptions.firstOrNull { option -> option.termKey == selection.selectedPeriodKey }
				?: periodOptions.firstOrNull()
			val selectedCodes = selection.selectedSubjects.map(SyntheticTermSubject::subjectCode).toSet()
			val editorAvailabilityBySubjectCode = record.editorAvailabilityBySubjectCode(
				editingTermId = selection.editingTermId
			)
			val pensumAvailabilityBySubjectCode = record.pensumAvailabilityBySubjectCode(pensum = pensum)

			SyntheticTermCreationSnapshot(
				editingTermId = selection.editingTermId,
				editingTermKey = selection.editingTermKey,
				periodOptions = periodOptions,
				selectedPeriod = selectedPeriod,
				selectedSubjects = selection.selectedSubjects.map { subject ->
					subject.withAvailability(
						editorAvailabilityBySubjectCode = editorAvailabilityBySubjectCode,
						pensumAvailabilityBySubjectCode = pensumAvailabilityBySubjectCode
					)
				},
				suggestedSubjects = record.suggestedSubjects(
					pensum = pensum,
					selectedCodes = selectedCodes,
					editorAvailabilityBySubjectCode = editorAvailabilityBySubjectCode,
					pensumAvailabilityBySubjectCode = pensumAvailabilityBySubjectCode
				),
				searchResults = searchResults
					.mapNotNull { subject ->
						subject
							.takeIf { item -> RealSubjectCodeRegex.matches(item.subjectCode) }
							?.withAvailability(
								editorAvailabilityBySubjectCode = editorAvailabilityBySubjectCode,
								pensumAvailabilityBySubjectCode = pensumAvailabilityBySubjectCode
							)
					}
					.sortedWith(
						compareBy<SyntheticTermSubject> { subject ->
							subject.subjectCode !in pensumAvailabilityBySubjectCode
						}
							.thenBy { subject -> subject.availability.searchOrder }
							.thenBy(SyntheticTermSubject::subjectCode)
					)
			)
		}
	}

	override suspend fun refreshSearch(query: String) {
		val normalizedQuery = SubjectCatalogSearchNormalizer.normalize(query)
		if (normalizedQuery.length < MinimumSearchQueryLength) return

		val response = ktorClient.get("subjects/v1/search") {
			parameter("query", query)
			parameter("limit", SearchLimit)
		}.body<CreateSyntheticTermSubjectSearchResponse>()

		val now = currentTimeMillis()
		val entities = response.results
			.mapNotNull { result ->
				val subjectCode = result.subjectCode.trim().uppercase()
					.takeIf(RealSubjectCodeRegex::matches)
					?: return@mapNotNull null

				SubjectCatalogCacheEntity(
					subjectCode = subjectCode,
					name = result.name,
					credits = result.credits,
					gradingMode = result.gradingMode?.name,
					normalizedCode = SubjectCatalogSearchNormalizer.normalize(subjectCode),
					normalizedName = SubjectCatalogSearchNormalizer.normalize(result.name),
					updatedAt = now
				)
			}
			.distinctBy(SubjectCatalogCacheEntity::subjectCode)

		if (entities.isNotEmpty()) {
			subjectCatalogCacheDao.upsertEntities(entities)
		}
	}

	private fun observeRecord(): Flow<AcademicRecord> {
		return flow {
			emitAll(academicRecordRepository.observeAcademicRecordFlow())
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private fun observePensum(): Flow<CreateSyntheticTermPensumCacheResponse?> {
		return pensumSelectionDao.observeSelection()
			.flatMapLatest { selection ->
				val cacheKey = selection?.cacheKey
				if (cacheKey == null) {
					flowOf(null)
				} else {
					pensumCacheDao.observePensum(cacheKey)
						.map { cache ->
							cache?.payloadJson?.let { payload ->
								runCatching {
									json.decodeFromString<CreateSyntheticTermPensumCacheResponse>(payload)
								}.getOrNull()
							}
						}
				}
			}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private fun observeLocalSearch(queryFlow: StateFlow<String>): Flow<List<SyntheticTermSubject>> {
		return queryFlow
			.distinctUntilChanged { old, new ->
				SubjectCatalogSearchNormalizer.normalize(old) == SubjectCatalogSearchNormalizer.normalize(new)
			}
			.flatMapLatest { query ->
				val normalizedQuery = SubjectCatalogSearchNormalizer.normalize(query)
				if (normalizedQuery.length < MinimumSearchQueryLength) {
					flowOf(emptyList())
				} else {
					subjectCatalogCacheDao.observeSearch(
						normalizedQuery = normalizedQuery,
						limit = SearchLimit
					).map { entities ->
						entities.map { entity -> entity.toSyntheticTermSubject() }
					}
				}
			}
	}

	private fun AcademicRecord.periodOptions(editingTermId: String?): List<SyntheticTermPeriodOption> {
		val editingTerm = terms.firstOrNull { term ->
			term.id == editingTermId && term.kind.isSynthetic
		}
		val baselineTerms = if (editingTerm == null) {
			terms
		} else {
			terms.filterNot { term -> term.id == editingTerm.id }
		}
		val maxExistingOrder = baselineTerms.maxOfOrNull(AcademicTerm::termOrder)
		val currentOrder = currentAcademicTermOrder()
		val options = mutableListOf<SyntheticTermPeriodOption>()
		var year = currentOrder / 10
		var sequence = currentOrder % 10

		while (options.size < FuturePeriodCount) {
			val period = AcademicTermPeriod.entries
				.first { value -> value.sequence == sequence }
			val option = SyntheticTermPeriodOption(
				periodYear = year,
				periodCode = period
			)
			if (
				period.supportsSyntheticPlanning &&
				maxExistingOrder?.let { latestOrder -> option.termOrder > latestOrder } != false
			) {
				options += option
			}

			sequence += 1
			if (sequence > AcademicTermPeriod.SEP_DEC.sequence) {
				sequence = AcademicTermPeriod.JAN_MAR.sequence
				year += 1
			}
		}

		val editingOption = editingTerm?.let { term ->
			SyntheticTermPeriodOption(
				periodYear = term.periodYear,
				periodCode = term.periodCode
			)
		}

		return (listOfNotNull(editingOption) + options)
			.distinctBy(SyntheticTermPeriodOption::termKey)
	}

	private fun currentAcademicTermOrder(): Int {
		val dateTime = Clock.System.now().toLocalDateTime(TimeZone.of(AcademicCalendarTimeZoneId))
		return dateTime.year * 10 + periodForMonth(dateTime.month.ordinal + 1).sequence
	}

	private fun periodForMonth(month: Int): AcademicTermPeriod {
		return when (month) {
			in 1..3 -> AcademicTermPeriod.JAN_MAR
			in 4..6 -> AcademicTermPeriod.APR_JUL
			in 7..8 -> AcademicTermPeriod.JUL_AUG
			else -> AcademicTermPeriod.SEP_DEC
		}
	}

	private fun AcademicRecord.editorAvailabilityBySubjectCode(
		editingTermId: String?
	): Map<String, SubjectAvailabilityResolution> {
		return buildMap {
			terms.filterNot { term -> term.id == editingTermId }.forEach { term ->
				if (!term.kind.isSynthetic) return@forEach

				term.attempts.forEach { attempt ->
					putWithPriority(
						key = attempt.subjectCode.uppercase(),
						resolution = SubjectAvailabilityResolution(
							availability = SyntheticTermSubjectAvailability.ALREADY_PLANNED,
							detail = SyntheticTermSubjectAvailabilityDetail(
								termLabel = term.shortLabel
							)
						)
					)
				}
			}
		}
	}

	private data class FormSelectionState(
		val selectedSubjects: List<SyntheticTermSubject>,
		val selectedPeriodKey: String?,
		val editingTermId: String?,
		val editingTermKey: String?
	)

	private data class SubjectAvailabilityResolution(
		val availability: SyntheticTermSubjectAvailability,
		val detail: SyntheticTermSubjectAvailabilityDetail? = null
	)

	private fun MutableMap<String, SubjectAvailabilityResolution>.putWithPriority(
		key: String,
		resolution: SubjectAvailabilityResolution
	) {
		val current = this[key]
		if (current == null || resolution.availability.searchOrder < current.availability.searchOrder) {
			this[key] = resolution
		}
	}

	private fun MutableMap<String, SubjectAvailabilityResolution>.putWithPensumPriority(
		key: String,
		resolution: SubjectAvailabilityResolution
	) {
		val current = this[key]
		if (current == null || resolution.availability.pensumStatusPriority < current.availability.pensumStatusPriority) {
			this[key] = resolution
		}
	}

	private fun AcademicRecord.suggestedSubjects(
		pensum: CreateSyntheticTermPensumCacheResponse?,
		selectedCodes: Set<String>,
		editorAvailabilityBySubjectCode: Map<String, SubjectAvailabilityResolution>,
		pensumAvailabilityBySubjectCode: Map<String, SubjectAvailabilityResolution>
	): List<SyntheticTermSubject> {
		val nodes = pensum?.pensum?.nodes.orEmpty()
		val courseNodes = nodes.filter { node -> node.nodeType == NodeTypeCourse }

		return courseNodes
			.asSequence()
			.mapNotNull { node ->
				val subjectCode = node.subjectCode
					?.trim()
					?.uppercase()
					?.takeIf(RealSubjectCodeRegex::matches)
					?: return@mapNotNull null
				if (subjectCode in selectedCodes || subjectCode in editorAvailabilityBySubjectCode) {
					return@mapNotNull null
				}
				if (pensumAvailabilityBySubjectCode[subjectCode]?.availability != SyntheticTermSubjectAvailability.AVAILABLE) {
					return@mapNotNull null
				}

				SyntheticTermSubject(
					subjectCode = subjectCode,
					name = node.name,
					credits = node.credits
				)
			}
			.distinctBy(SyntheticTermSubject::subjectCode)
			.sortedWith(compareBy(SyntheticTermSubject::subjectCode))
			.take(SuggestedSubjectLimit)
			.toList()
	}

	private fun AcademicRecord.pensumAvailabilityBySubjectCode(
		pensum: CreateSyntheticTermPensumCacheResponse?
	): Map<String, SubjectAvailabilityResolution> {
		val pensumGraph = pensum?.pensum
		val courseNodes = pensumGraph?.nodes
			.orEmpty()
			.filter { node -> node.nodeType == NodeTypeCourse }
		return if (pensumGraph == null || courseNodes.isEmpty()) {
			emptyMap()
		} else {
			val courseNodeById = courseNodes.associateBy { node -> node.id }
			val progress = academicPensumStatusEngine.resolve(
				pensum = pensumGraph.toAcademicPensumGraph(),
				academicSnapshot = toAcademicPensumSnapshot()
			)
			val detailBySubjectCode = pensumStatusDetailBySubjectCode()

			buildMap {
				courseNodes.forEach { node ->
					val subjectCode = node.subjectCode
						?.trim()
						?.uppercase()
						?.takeIf(RealSubjectCodeRegex::matches)
						?: return@forEach
					val availability = progress.nodeStatuses[node.id].toSyntheticTermSubjectAvailability()
					val detail = when (availability) {
						SyntheticTermSubjectAvailability.BLOCKED -> {
							SyntheticTermSubjectAvailabilityDetail(
								missingSubjectCodes = node.missingRequirementCodes(
									edges = pensumGraph.edges,
									courseNodeById = courseNodeById,
									nodeStatuses = progress.nodeStatuses
								)
							)
						}

						SyntheticTermSubjectAvailability.APPROVED,
						SyntheticTermSubjectAvailability.CURRENT,
						-> detailBySubjectCode[subjectCode]

						SyntheticTermSubjectAvailability.AVAILABLE,
						SyntheticTermSubjectAvailability.ALREADY_PLANNED,
						SyntheticTermSubjectAvailability.NOT_IN_PENSUM,
						-> null
					}

					putWithPensumPriority(
						key = subjectCode,
						resolution = SubjectAvailabilityResolution(
							availability = availability,
							detail = detail
						)
					)
				}
			}
		}
	}

	private fun CreateSyntheticTermPensumCacheResponse.Node.missingRequirementCodes(
		edges: List<CreateSyntheticTermPensumCacheResponse.Edge>,
		courseNodeById: Map<String, CreateSyntheticTermPensumCacheResponse.Node>,
		nodeStatuses: Map<String, AcademicPensumNodeStatus>
	): List<String> {
		return edges
			.asSequence()
			.filter { edge -> edge.toNodeId == id }
			.filterNot { edge ->
				edge.isSatisfied(
					nodeStatuses = nodeStatuses
				)
			}
			.mapNotNull { edge ->
				courseNodeById[edge.fromNodeId]
					?.subjectCode
					?.trim()
					?.uppercase()
					?.takeIf(RealSubjectCodeRegex::matches)
			}
			.distinct()
			.sorted()
			.toList()
	}

	private fun CreateSyntheticTermPensumCacheResponse.Edge.isSatisfied(
		nodeStatuses: Map<String, AcademicPensumNodeStatus>
	): Boolean {
		val status = nodeStatuses[fromNodeId]
		return when (relationshipType) {
			"COREQUISITE" ->
				status == AcademicPensumNodeStatus.APPROVED ||
					status == AcademicPensumNodeStatus.CURRENT
			else -> status == AcademicPensumNodeStatus.APPROVED
		}
	}

	private fun CreateSyntheticTermPensumCacheResponse.Pensum.toAcademicPensumGraph(): AcademicPensumGraph {
		return AcademicPensumGraph(
			nodes = nodes.map { node -> node.toAcademicPensumNode() },
			edges = edges.map { edge -> edge.toAcademicPensumEdge() }
		)
	}

	private fun CreateSyntheticTermPensumCacheResponse.Node.toAcademicPensumNode(): AcademicPensumGraph.Node {
		return AcademicPensumGraph.Node(
			id = id,
			nodeType = if (nodeType == NodeTypeCourse) {
				AcademicPensumGraph.NodeType.COURSE
			} else {
				AcademicPensumGraph.NodeType.SLOT
			},
			subjectCode = subjectCode,
			credits = credits,
			fulfillmentRules = emptyList()
		)
	}

	private fun CreateSyntheticTermPensumCacheResponse.Edge.toAcademicPensumEdge(): AcademicPensumGraph.Edge {
		return AcademicPensumGraph.Edge(
			fromNodeId = fromNodeId,
			toNodeId = toNodeId,
			relationshipType = if (relationshipType == "COREQUISITE") {
				AcademicPensumGraph.RelationshipType.COREQUISITE
			} else {
				AcademicPensumGraph.RelationshipType.REQUIREMENT
			}
		)
	}

	private fun AcademicRecord.toAcademicPensumSnapshot(): AcademicPensumSnapshot {
		return AcademicPensumSnapshot(
			attempts = terms.flatMap { term ->
				term.attempts.mapIndexed { index, attempt ->
					AcademicPensumSnapshot.Attempt(
						id = attempt.id,
						subjectCode = attempt.subjectCode,
						subjectName = attempt.subjectName,
						credits = attempt.credits,
						termOrder = term.termOrder,
						positionInTerm = index,
						termKind = term.kind,
						outcome = attempt.academicOutcome
					)
				}
			}
		)
	}

	private fun AcademicRecord.pensumStatusDetailBySubjectCode(): Map<String, SyntheticTermSubjectAvailabilityDetail> {
		return buildMap {
			terms.forEach { term ->
				if (term.kind.isSynthetic) return@forEach

				term.attempts.forEach { attempt ->
					val matchesPensumStatus = attempt.academicOutcome == AttemptOutcome.APPROVED ||
						(term.kind.isCurrent && attempt.academicOutcome != AttemptOutcome.APPROVED)
					if (!matchesPensumStatus) return@forEach

					val subjectCode = attempt.subjectCode.uppercase()
					if (subjectCode !in this) {
						this[subjectCode] = SyntheticTermSubjectAvailabilityDetail(termLabel = term.shortLabel)
					}
				}
			}
		}
	}

	private fun AcademicPensumNodeStatus?.toSyntheticTermSubjectAvailability(): SyntheticTermSubjectAvailability {
		return when (this) {
			AcademicPensumNodeStatus.APPROVED -> SyntheticTermSubjectAvailability.APPROVED
			AcademicPensumNodeStatus.CURRENT -> SyntheticTermSubjectAvailability.CURRENT
			AcademicPensumNodeStatus.AVAILABLE -> SyntheticTermSubjectAvailability.AVAILABLE
			AcademicPensumNodeStatus.BLOCKED,
			null,
			-> SyntheticTermSubjectAvailability.BLOCKED
		}
	}

	private fun SubjectCatalogCacheEntity.toSyntheticTermSubject(): SyntheticTermSubject {
		return SyntheticTermSubject(
			subjectCode = subjectCode.uppercase(),
			name = name,
			credits = credits,
			gradingMode = gradingMode.toAttemptGradingMode()
		)
	}

	private fun String?.toAttemptGradingMode(): AttemptGradingMode {
		return when (this?.let { value -> runCatching { GradingMode.valueOf(value) }.getOrNull() }) {
			GradingMode.QUALITATIVE_PASS_FAIL -> AttemptGradingMode.QUALITATIVE_PASS_FAIL
			else -> AttemptGradingMode.NUMERIC
		}
	}

	private fun SyntheticTermSubject.withAvailability(
		editorAvailabilityBySubjectCode: Map<String, SubjectAvailabilityResolution>,
		pensumAvailabilityBySubjectCode: Map<String, SubjectAvailabilityResolution>
	): SyntheticTermSubject {
		val resolution = when {
			editorAvailabilityBySubjectCode[subjectCode] != null ->
				editorAvailabilityBySubjectCode.getValue(subjectCode)
			else -> pensumAvailabilityBySubjectCode[subjectCode]
				?: SubjectAvailabilityResolution(availability = SyntheticTermSubjectAvailability.NOT_IN_PENSUM)
		}

		return copy(
			availability = resolution.availability,
			availabilityDetail = resolution.detail
		)
	}

	private val SyntheticTermSubjectAvailability.searchOrder: Int
		get() = when (this) {
			SyntheticTermSubjectAvailability.AVAILABLE -> 0
			SyntheticTermSubjectAvailability.ALREADY_PLANNED -> 0
			SyntheticTermSubjectAvailability.BLOCKED -> 3
			SyntheticTermSubjectAvailability.CURRENT -> 4
			SyntheticTermSubjectAvailability.APPROVED -> 6
			SyntheticTermSubjectAvailability.NOT_IN_PENSUM -> 1
		}

	private val SyntheticTermSubjectAvailability.pensumStatusPriority: Int
		get() = when (this) {
			SyntheticTermSubjectAvailability.APPROVED -> 0
			SyntheticTermSubjectAvailability.CURRENT -> 1
			SyntheticTermSubjectAvailability.AVAILABLE -> 2
			SyntheticTermSubjectAvailability.BLOCKED -> 3
			SyntheticTermSubjectAvailability.ALREADY_PLANNED,
			SyntheticTermSubjectAvailability.NOT_IN_PENSUM,
			-> 4
		}
}

private val AcademicTerm.shortLabel: String
	get() = "${periodCode.shortLabel} $periodYear"

private const val MinimumSearchQueryLength = 2
private const val SearchLimit = 20
private const val FuturePeriodCount = 20
private const val SuggestedSubjectLimit = 8
private const val NodeTypeCourse = "COURSE"
private const val AcademicCalendarTimeZoneId = "America/Caracas"
private val RealSubjectCodeRegex = Regex("^([A-Z]{2}\\d{4}|[A-Z]{3}\\d{3})$")
