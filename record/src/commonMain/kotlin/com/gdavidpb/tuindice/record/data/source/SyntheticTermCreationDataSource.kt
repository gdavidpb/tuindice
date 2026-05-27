@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.isCurrent
import com.gdavidpb.tuindice.academiccore.domain.model.isHistorical
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumSelectionDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectCatalogCacheDao
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectCatalogCacheEntity
import com.gdavidpb.tuindice.base.domain.utils.SubjectCatalogSearchNormalizer
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
			val availabilityBySubjectCode = record.availabilityBySubjectCode(editingTermId = selection.editingTermId)
			val pensumAvailabilityBySubjectCode = record.pensumAvailabilityBySubjectCode(pensum = pensum)

			SyntheticTermCreationSnapshot(
				editingTermId = selection.editingTermId,
				editingTermKey = selection.editingTermKey,
				periodOptions = periodOptions,
				selectedPeriod = selectedPeriod,
				selectedSubjects = selection.selectedSubjects,
				suggestedSubjects = record.suggestedSubjects(
					pensum = pensum,
					selectedCodes = selectedCodes,
					availabilityBySubjectCode = availabilityBySubjectCode,
					pensumAvailabilityBySubjectCode = pensumAvailabilityBySubjectCode
				),
				searchResults = searchResults
					.mapNotNull { subject ->
						subject
							.takeIf { item -> RealSubjectCodeRegex.matches(item.subjectCode) }
							?.withAvailability(
								selectedCodes = selectedCodes,
								availabilityBySubjectCode = availabilityBySubjectCode,
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

		repeat(FuturePeriodCount) {
			val period = AcademicTermPeriod.entries
				.first { value -> value.sequence == sequence }
			val option = SyntheticTermPeriodOption(
				periodYear = year,
				periodCode = period
			)
			if (maxExistingOrder?.let { latestOrder -> option.termOrder > latestOrder } != false) {
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
		val dateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
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

	private fun AcademicRecord.availabilityBySubjectCode(editingTermId: String?): Map<String, SubjectAvailabilityResolution> {
		return buildMap {
			terms.filterNot { term -> term.id == editingTermId }.forEach { term ->
				term.attempts.forEach { attempt ->
					val availability = when {
						term.kind.isHistorical && attempt.academicOutcome == AttemptOutcome.APPROVED ->
							SyntheticTermSubjectAvailability.ALREADY_TAKEN
						term.kind.isSynthetic ->
							SyntheticTermSubjectAvailability.ALREADY_PLANNED
						term.kind.isCurrent && attempt.academicOutcome == AttemptOutcome.APPROVED ->
							SyntheticTermSubjectAvailability.ALREADY_TAKEN
						else ->
							null
					}

					if (availability != null) {
						putWithPriority(
							key = attempt.subjectCode.uppercase(),
							resolution = SubjectAvailabilityResolution(
								availability = availability,
								detail = SyntheticTermSubjectAvailabilityDetail(
									termLabel = term.shortLabel
								)
							)
						)
					}
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

	private fun AcademicRecord.suggestedSubjects(
		pensum: CreateSyntheticTermPensumCacheResponse?,
		selectedCodes: Set<String>,
		availabilityBySubjectCode: Map<String, SubjectAvailabilityResolution>,
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
				if (subjectCode in selectedCodes || subjectCode in availabilityBySubjectCode) return@mapNotNull null
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
		val courseNodes = pensum?.pensum?.nodes
			.orEmpty()
			.filter { node -> node.nodeType == NodeTypeCourse }
		val edges = pensum?.pensum?.edges.orEmpty()
		if (courseNodes.isEmpty()) return emptyMap()
		val courseNodeById = courseNodes.associateBy { node -> node.id }

		val approvedSubjects = terms
			.flatMap(AcademicTerm::attempts)
			.filter { attempt -> attempt.academicOutcome == AttemptOutcome.APPROVED }
			.map { attempt -> attempt.subjectCode.uppercase() }
			.toSet()
		val currentSubjects = terms
			.filter { term -> term.kind.isCurrent }
			.flatMap(AcademicTerm::attempts)
			.map { attempt -> attempt.subjectCode.uppercase() }
			.toSet()
		val approvedNodeIds = courseNodes
			.filter { node -> node.subjectCode?.uppercase() in approvedSubjects }
			.map { node -> node.id }
			.toSet()
		val currentNodeIds = courseNodes
			.filter { node -> node.subjectCode?.uppercase() in currentSubjects }
			.map { node -> node.id }
			.toSet()

		return courseNodes.mapNotNull { node ->
			val subjectCode = node.subjectCode
				?.trim()
				?.uppercase()
				?.takeIf(RealSubjectCodeRegex::matches)
				?: return@mapNotNull null
			val missingSubjectCodes = node.missingRequirementCodes(
				edges = edges,
				courseNodeById = courseNodeById,
				approvedNodeIds = approvedNodeIds,
				currentNodeIds = currentNodeIds
			)
			val resolution = if (missingSubjectCodes.isEmpty()) {
				SubjectAvailabilityResolution(
					availability = SyntheticTermSubjectAvailability.AVAILABLE
				)
			} else {
				SubjectAvailabilityResolution(
					availability = SyntheticTermSubjectAvailability.UNAVAILABLE,
					detail = SyntheticTermSubjectAvailabilityDetail(
						missingSubjectCodes = missingSubjectCodes
					)
				)
			}

			subjectCode to resolution
		}.toMap()
	}

	private fun CreateSyntheticTermPensumCacheResponse.Node.missingRequirementCodes(
		edges: List<CreateSyntheticTermPensumCacheResponse.Edge>,
		courseNodeById: Map<String, CreateSyntheticTermPensumCacheResponse.Node>,
		approvedNodeIds: Set<String>,
		currentNodeIds: Set<String>
	): List<String> {
		return edges
			.asSequence()
			.filter { edge -> edge.toNodeId == id }
			.filterNot { edge ->
				edge.isSatisfied(
					approvedNodeIds = approvedNodeIds,
					currentNodeIds = currentNodeIds
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
		approvedNodeIds: Set<String>,
		currentNodeIds: Set<String>
	): Boolean {
		return when (relationshipType) {
			"COREQUISITE" -> fromNodeId in approvedNodeIds || fromNodeId in currentNodeIds
			else -> fromNodeId in approvedNodeIds
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
		selectedCodes: Set<String>,
		availabilityBySubjectCode: Map<String, SubjectAvailabilityResolution>,
		pensumAvailabilityBySubjectCode: Map<String, SubjectAvailabilityResolution>
	): SyntheticTermSubject {
		val resolution = when {
			subjectCode in selectedCodes ->
				SubjectAvailabilityResolution(availability = SyntheticTermSubjectAvailability.SELECTED)
			availabilityBySubjectCode[subjectCode] != null ->
				availabilityBySubjectCode.getValue(subjectCode)
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
			SyntheticTermSubjectAvailability.NOT_IN_PENSUM -> 1
			SyntheticTermSubjectAvailability.SELECTED -> 2
			SyntheticTermSubjectAvailability.ALREADY_TAKEN -> 3
			SyntheticTermSubjectAvailability.ALREADY_PLANNED -> 4
			SyntheticTermSubjectAvailability.UNAVAILABLE -> 5
		}
}

private val AcademicTerm.shortLabel: String
	get() = "${periodCode.shortLabel} $periodYear"

private const val MinimumSearchQueryLength = 2
private const val SearchLimit = 20
private const val FuturePeriodCount = 20
private const val SuggestedSubjectLimit = 8
private const val NodeTypeCourse = "COURSE"
private val RealSubjectCodeRegex = Regex("^([A-Z]{2}\\d{4}|[A-Z]{3}\\d{3})$")
