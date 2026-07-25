package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicPensumStatusEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.toAcademicPensumSnapshot
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumCacheDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PensumSelectionDao
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleAcademicRecordRepository
import com.gdavidpb.tuindice.subjects.data.model.SubjectSearchPensumCacheResponse
import com.gdavidpb.tuindice.subjects.data.repository.SubjectSearchPensumStatusDataRepository
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
	override fun observeStatusBySubjectCode(): Flow<Map<String, AcademicPensumNodeStatus>> {
		return combine(
			observePensum(),
			visibleAcademicRecordRepository.observeVisibleAcademicRecordFlow()
		) { pensum, visibleRecord ->
			val graph = pensum?.toAcademicPensumGraph() ?: return@combine emptyMap()
			val progress = statusEngine.resolve(
				pensum = graph,
				academicSnapshot = visibleRecord.toAcademicPensumSnapshot()
			)

			graph.nodes
				.asSequence()
				.filter { node -> node.nodeType == AcademicPensumGraph.NodeType.COURSE }
				.mapNotNull { node ->
					val subjectCode = node.subjectCode
						?.trim()
						?.uppercase()
						?.takeIf(RealSubjectCodeRegex::matches)
						?: return@mapNotNull null
					val status = progress.nodeStatuses[node.id] ?: return@mapNotNull null
					subjectCode to status
				}
				.toMap()
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
					minSubjects = rule.minSubjects
				)
			}
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
		private val RealSubjectCodeRegex = Regex("^([A-Z]{2}\\d{4}|[A-Z]{3}\\d{3})$")
	}
}
