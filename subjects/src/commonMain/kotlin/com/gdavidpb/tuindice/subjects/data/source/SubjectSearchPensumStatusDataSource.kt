package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicPensumSlotEligibilityResolver
import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicPensumStatusEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.fulfilledSubjectStatuses
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumProgress
import com.gdavidpb.tuindice.academiccore.domain.model.toAcademicPensumSnapshot
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumSelectionDao
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleAcademicRecordRepository
import com.gdavidpb.tuindice.subjects.data.model.SubjectSearchPensumCacheResponse
import com.gdavidpb.tuindice.subjects.data.model.SubjectSearchPensumStatusResolver
import com.gdavidpb.tuindice.subjects.data.repository.SubjectSearchPensumStatusDataRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchPensumStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class SubjectSearchPensumStatusDataSource(
	private val pensumSelectionDao: PensumSelectionDao,
	private val pensumCacheDao: PensumCacheDao,
	private val visibleAcademicRecordRepository: VisibleAcademicRecordRepository,
	private val json: Json,
	private val statusEngine: AcademicPensumStatusEngine
) : SubjectSearchPensumStatusDataRepository {
	override fun observePensumStatusResolver(): Flow<SubjectSearchPensumStatusResolver> {
		return combine(
			observePensum(),
			visibleAcademicRecordRepository.observeVisibleAcademicRecordFlow()
		) { pensum, visibleRecord ->
			val graph = pensum?.toAcademicPensumGraph() ?: return@combine SubjectSearchPensumStatusResolver.Empty
			val progress = statusEngine.resolve(
				pensum = graph,
				academicSnapshot = visibleRecord.toAcademicPensumSnapshot()
			)

			SubjectSearchPensumStatusResolver(
				statusBySubjectCode = graph.statusBySubjectCode(progress),
				slotEligibility = AcademicPensumSlotEligibilityResolver(pensum = graph, progress = progress)
			)
		}
	}

	// A fixed course labels its own code and its equivalence codes with its status; a code that
	// already fulfilled a node (a slot, a course by equivalence) is approved or current.
	private fun AcademicPensumGraph.statusBySubjectCode(
		progress: AcademicPensumProgress
	): Map<String, SubjectSearchPensumStatus> {
		return buildMap {
			nodes
				.filter { node -> node.nodeType == AcademicPensumGraph.NodeType.COURSE }
				.forEach { node ->
					val status = progress.nodeStatuses[node.id]?.toSearchStatus() ?: return@forEach
					val equivalenceCodes = node.fulfillmentRules
						.filter { rule -> rule.ruleType == RuleTypeEquivalence }
						.flatMap { rule -> rule.subjectCodes }

					(listOfNotNull(node.subjectCode) + equivalenceCodes)
						.mapNotNull { code -> code.trim().uppercase().takeIf(RealSubjectCodeRegex::matches) }
						.forEach { code -> getOrPut(code) { status } }
				}
			progress.fulfilledSubjectStatuses().forEach { (code, status) -> put(code, status.toSearchStatus()) }
		}
	}

	private fun AcademicPensumNodeStatus.toSearchStatus(): SubjectSearchPensumStatus {
		return when (this) {
			AcademicPensumNodeStatus.APPROVED -> SubjectSearchPensumStatus.APPROVED
			AcademicPensumNodeStatus.CURRENT -> SubjectSearchPensumStatus.CURRENT
			AcademicPensumNodeStatus.AVAILABLE -> SubjectSearchPensumStatus.AVAILABLE
			AcademicPensumNodeStatus.BLOCKED -> SubjectSearchPensumStatus.BLOCKED
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private fun observePensum(): Flow<SubjectSearchPensumCacheResponse?> {
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
									json.decodeFromString<SubjectSearchPensumCacheResponse>(payload)
								}.getOrNull()
							}
						}
				}
			}
	}

	private fun SubjectSearchPensumCacheResponse.toAcademicPensumGraph(): AcademicPensumGraph {
		return AcademicPensumGraph(
			nodes = pensum.nodes.mapNotNull { node -> node.toAcademicPensumNode() },
			edges = pensum.edges.mapNotNull { edge -> edge.toAcademicPensumEdge() }
		)
	}

	private fun SubjectSearchPensumCacheResponse.Node.toAcademicPensumNode(): AcademicPensumGraph.Node? {
		val type = when (nodeType.uppercase()) {
			NodeTypeCourse -> AcademicPensumGraph.NodeType.COURSE
			NodeTypeSlot -> AcademicPensumGraph.NodeType.SLOT
			else -> return null
		}
		return AcademicPensumGraph.Node(
			id = id,
			nodeType = type,
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

	private fun SubjectSearchPensumCacheResponse.Edge.toAcademicPensumEdge(): AcademicPensumGraph.Edge? {
		val type = when (relationshipType.uppercase()) {
			RelationshipTypeCorequisite -> AcademicPensumGraph.RelationshipType.COREQUISITE
			RelationshipTypeRequirement -> AcademicPensumGraph.RelationshipType.REQUIREMENT
			else -> AcademicPensumGraph.RelationshipType.REQUIREMENT
		}
		return AcademicPensumGraph.Edge(
			fromNodeId = fromNodeId,
			toNodeId = toNodeId,
			relationshipType = type
		)
	}

	private companion object {
		private const val NodeTypeCourse = "COURSE"
		private const val NodeTypeSlot = "SLOT"
		private const val RelationshipTypeCorequisite = "COREQUISITE"
		private const val RelationshipTypeRequirement = "REQUIREMENT"
		private const val RuleTypeEquivalence = "EQUIVALENCE"
		private val RealSubjectCodeRegex = Regex("^([A-Z]{2}\\d{4}|[A-Z]{3}\\d{3})$")
	}
}
