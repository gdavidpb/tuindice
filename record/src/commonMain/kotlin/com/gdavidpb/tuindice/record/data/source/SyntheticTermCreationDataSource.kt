@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicPensumSlotEligibilityResolver
import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicPensumStatusEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.fulfilledSubjectStatuses
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumProgress
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSlotEligibility
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.isCurrent
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.academiccore.domain.utils.SubjectCatalogSearchNormalizer
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

		// Resolved once per record or pensum change, never per keystroke: the status engine and the
		// slot resolver both walk the whole pensum.
		val pensumStateFlow = combine(recordFlow, pensumFlow) { record, pensum ->
			PlannerPensumState(
				record = record,
				pensum = pensum,
				availability = record.pensumAvailability(pensum = pensum)
			)
		}

		return combine(
			pensumStateFlow,
			searchFlow,
			selectionFlow
		) { pensumState, searchResults, selection ->
			val record = pensumState.record
			val pensum = pensumState.pensum
			val periodOptions = record.periodOptions(editingTermId = selection.editingTermId)
			val selectedPeriod = periodOptions.firstOrNull { option -> option.termKey == selection.selectedPeriodKey }
				?: periodOptions.firstOrNull()
			val selectedCodes = selection.selectedSubjects.map(SyntheticTermSubject::subjectCode).toSet()
			val editorAvailabilityBySubjectCode = record.editorAvailabilityBySubjectCode(
				editingTermId = selection.editingTermId
			)
			val pensumAvailability = pensumState.availability

			SyntheticTermCreationSnapshot(
				editingTermId = selection.editingTermId,
				editingTermKey = selection.editingTermKey,
				periodOptions = periodOptions,
				selectedPeriod = selectedPeriod,
				selectedSubjects = selection.selectedSubjects.map { subject ->
					subject.withAvailability(
						editorAvailabilityBySubjectCode = editorAvailabilityBySubjectCode,
						pensumAvailability = pensumAvailability
					)
				},
				suggestedSubjects = record.suggestedSubjects(
					pensum = pensum,
					selectedCodes = selectedCodes,
					editorAvailabilityBySubjectCode = editorAvailabilityBySubjectCode,
					pensumAvailabilityBySubjectCode = pensumAvailability.bySubjectCode
				),
				searchResults = searchResults
					.mapNotNull { subject ->
						subject
							.takeIf { item -> RealSubjectCodeRegex.matches(item.subjectCode) }
							?.withAvailability(
								editorAvailabilityBySubjectCode = editorAvailabilityBySubjectCode,
								pensumAvailability = pensumAvailability
							)
					}
					.sortedWith(
						compareBy<SyntheticTermSubject> { subject ->
							subject.subjectCode !in pensumAvailability.bySubjectCode
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

	private data class PlannerPensumState(
		val record: AcademicRecord,
		val pensum: CreateSyntheticTermPensumCacheResponse?,
		val availability: PensumAvailability
	)

	// What the pensum says about a subject code: fixed courses, their equivalences and the codes that
	// already fulfil a node are looked up; anything else is asked to the slot resolver.
	private data class PensumAvailability(
		val bySubjectCode: Map<String, SubjectAvailabilityResolution>,
		val slotEligibility: AcademicPensumSlotEligibilityResolver?
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

	private fun AcademicRecord.pensumAvailability(
		pensum: CreateSyntheticTermPensumCacheResponse?
	): PensumAvailability {
		val pensumGraph = pensum?.pensum
		val courseNodes = pensumGraph?.nodes
			.orEmpty()
			.filter { node -> node.nodeType == NodeTypeCourse }
		return if (pensumGraph == null || courseNodes.isEmpty()) {
			PensumAvailability(bySubjectCode = emptyMap(), slotEligibility = null)
		} else {
			val courseNodeById = courseNodes.associateBy { node -> node.id }
			val academicGraph = pensumGraph.toAcademicPensumGraph()
			val progress = academicPensumStatusEngine.resolve(
				pensum = academicGraph,
				academicSnapshot = toAcademicPensumSnapshot()
			)
			val detailBySubjectCode = pensumStatusDetailBySubjectCode()

			val bySubjectCode = buildMap {
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
						SyntheticTermSubjectAvailability.COUNTS_AS_SLOT,
						-> null
					}
					val resolution = SubjectAvailabilityResolution(
						availability = availability,
						detail = detail
					)

					putWithPensumPriority(key = subjectCode, resolution = resolution)
					// An equivalence code fulfils this same node, so it reads exactly like it.
					node.equivalenceSubjectCodes().forEach { equivalenceCode ->
						putWithPensumPriority(key = equivalenceCode, resolution = resolution)
					}
				}
				putFulfilledSubjects(progress = progress, detailBySubjectCode = detailBySubjectCode)
			}

			PensumAvailability(
				bySubjectCode = bySubjectCode,
				slotEligibility = AcademicPensumSlotEligibilityResolver(pensum = academicGraph, progress = progress)
			)
		}
	}

	// A code that already fulfilled a node (an elective slot, a course by equivalence) is approved or
	// current, not outside the pensum.
	private fun MutableMap<String, SubjectAvailabilityResolution>.putFulfilledSubjects(
		progress: AcademicPensumProgress,
		detailBySubjectCode: Map<String, SyntheticTermSubjectAvailabilityDetail>
	) {
		progress.fulfilledSubjectStatuses().forEach { (subjectCode, status) ->
			putWithPensumPriority(
				key = subjectCode,
				resolution = SubjectAvailabilityResolution(
					availability = status.toSyntheticTermSubjectAvailability(),
					detail = detailBySubjectCode[subjectCode]
				)
			)
		}
	}

	private fun CreateSyntheticTermPensumCacheResponse.Node.equivalenceSubjectCodes(): List<String> {
		return fulfillmentRules
			.filter { rule -> rule.ruleType == RULE_TYPE_EQUIVALENCE }
			.flatMap { rule -> rule.subjectCodes }
			.mapNotNull { code -> code.trim().uppercase().takeIf(RealSubjectCodeRegex::matches) }
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
			fulfillmentRules = fulfillmentRules.map { rule ->
				AcademicPensumGraph.FulfillmentRule(
					ruleType = rule.ruleType,
					subjectCodes = rule.subjectCodes,
					subjectCodePrefixes = rule.subjectCodePrefixes,
					minCredits = rule.minCredits,
					minSubjects = rule.minSubjects,
					slotEligibilityKind = rule.slotEligibilityKind
				)
			},
			category = category
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

	// Editor first (already planned elsewhere), then what the pensum lists for the code, and only
	// then whether one of its open slots would take it.
	private fun SyntheticTermSubject.withAvailability(
		editorAvailabilityBySubjectCode: Map<String, SubjectAvailabilityResolution>,
		pensumAvailability: PensumAvailability
	): SyntheticTermSubject {
		val resolution = editorAvailabilityBySubjectCode[subjectCode]
			?: pensumAvailability.bySubjectCode[subjectCode]
			?: pensumAvailability.slotEligibility
				?.eligibilityOf(subjectCode = subjectCode, credits = credits)
				.toSlotResolution()

		return copy(
			availability = resolution.availability,
			availabilityDetail = resolution.detail
		)
	}

	private fun AcademicPensumSlotEligibility?.toSlotResolution(): SubjectAvailabilityResolution {
		return when (this) {
			is AcademicPensumSlotEligibility.CountsTowardSlot -> SubjectAvailabilityResolution(
				availability = SyntheticTermSubjectAvailability.COUNTS_AS_SLOT,
				detail = SyntheticTermSubjectAvailabilityDetail(slotKind = slotKind)
			)

			// Taking it would count toward nothing: those slots are full. The kind still tells why.
			is AcademicPensumSlotEligibility.SlotsFilled -> SubjectAvailabilityResolution(
				availability = SyntheticTermSubjectAvailability.NOT_IN_PENSUM,
				detail = SyntheticTermSubjectAvailabilityDetail(slotKind = slotKind)
			)

			AcademicPensumSlotEligibility.NotEligible,
			null,
			-> SubjectAvailabilityResolution(availability = SyntheticTermSubjectAvailability.NOT_IN_PENSUM)
		}
	}

	private val SyntheticTermSubjectAvailability.searchOrder: Int
		get() = when (this) {
			SyntheticTermSubjectAvailability.AVAILABLE -> 0
			SyntheticTermSubjectAvailability.ALREADY_PLANNED -> 0
			SyntheticTermSubjectAvailability.BLOCKED -> 3
			SyntheticTermSubjectAvailability.CURRENT -> 4
			SyntheticTermSubjectAvailability.APPROVED -> 6
			SyntheticTermSubjectAvailability.NOT_IN_PENSUM -> 1
			SyntheticTermSubjectAvailability.COUNTS_AS_SLOT -> 0
		}

	private val SyntheticTermSubjectAvailability.pensumStatusPriority: Int
		get() = when (this) {
			SyntheticTermSubjectAvailability.APPROVED -> 0
			SyntheticTermSubjectAvailability.CURRENT -> 1
			SyntheticTermSubjectAvailability.AVAILABLE -> 2
			SyntheticTermSubjectAvailability.BLOCKED -> 3
			SyntheticTermSubjectAvailability.ALREADY_PLANNED,
			SyntheticTermSubjectAvailability.NOT_IN_PENSUM,
			SyntheticTermSubjectAvailability.COUNTS_AS_SLOT,
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
private const val RULE_TYPE_EQUIVALENCE = "EQUIVALENCE"
private const val AcademicCalendarTimeZoneId = "America/Caracas"
private val RealSubjectCodeRegex = Regex("^([A-Z]{2}\\d{4}|[A-Z]{3}\\d{3})$")
