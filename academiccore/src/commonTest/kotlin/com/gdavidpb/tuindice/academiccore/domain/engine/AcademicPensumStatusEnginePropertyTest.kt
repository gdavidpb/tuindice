package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.testing.nextPensumGraph
import com.gdavidpb.tuindice.academiccore.testing.nextSnapshot
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val ROUNDS = 150

class AcademicPensumStatusEnginePropertyTest {
	private val engine = AcademicPensumStatusEngine()

	@Test
	fun resolve_assignsExactlyOneStatusPerNode_evenWithDanglingEdges() {
		repeat(ROUNDS) { seed ->
			val random = Random(seed.toLong())
			val pensum = random.nextPensumGraph()
			val progress = engine.resolve(pensum, random.nextSnapshot())

			assertEquals(
				pensum.nodes.map(AcademicPensumGraph.Node::id).toSet(),
				progress.nodeStatuses.keys,
				"seed=$seed: every node must get exactly one status"
			)
		}
	}

	@Test
	fun resolve_isDeterministic() {
		repeat(ROUNDS) { seed ->
			val random = Random(seed.toLong())
			val pensum = random.nextPensumGraph()
			val snapshot = random.nextSnapshot()

			assertEquals(
				engine.resolve(pensum, snapshot),
				engine.resolve(pensum, snapshot),
				"seed=$seed: resolution is not deterministic"
			)
		}
	}

	@Test
	fun resolve_approvedCredits_matchTheApprovedNodes() {
		repeat(ROUNDS) { seed ->
			val random = Random(seed.toLong())
			val pensum = random.nextPensumGraph()
			val progress = engine.resolve(pensum, random.nextSnapshot())
			val expected = pensum.nodes
				.filter { node -> progress.nodeStatuses[node.id] == AcademicPensumNodeStatus.APPROVED }
				.sumOf(AcademicPensumGraph.Node::credits)

			assertEquals(expected, progress.approvedCredits, "seed=$seed")
		}
	}

	@Test
	fun resolve_fulfillments_onlyLandOnSlots_andNeverOutnumberAttempts() {
		repeat(ROUNDS) { seed ->
			val random = Random(seed.toLong())
			val pensum = random.nextPensumGraph()
			val snapshot = random.nextSnapshot()
			val progress = engine.resolve(pensum, snapshot)
			val slotIds = pensum.nodes
				.filter { node -> node.nodeType == AcademicPensumGraph.NodeType.SLOT }
				.map(AcademicPensumGraph.Node::id)
				.toSet()

			assertTrue(
				progress.nodeFulfillments.keys.all { id -> id in slotIds },
				"seed=$seed: fulfillments must only land on slot nodes"
			)
			assertTrue(
				progress.nodeFulfillments.size <= snapshot.attempts.size,
				"seed=$seed: ${progress.nodeFulfillments.size} fulfillments from ${snapshot.attempts.size} attempts"
			)
		}
	}

	@Test
	fun resolve_syntheticApprovals_neverApproveNorOccupyNodes() {
		repeat(ROUNDS) { seed ->
			val random = Random(seed.toLong())
			val pensum = random.nextPensumGraph()
			val snapshot = AcademicPensumSnapshot(
				attempts = random.nextSnapshot().attempts.map { attempt ->
					attempt.copy(termKind = TermKind.SYNTHETIC, outcome = AttemptOutcome.APPROVED)
				}
			)

			val progress = engine.resolve(pensum, snapshot)

			assertTrue(
				progress.nodeStatuses.values.none { status ->
					status == AcademicPensumNodeStatus.APPROVED || status == AcademicPensumNodeStatus.CURRENT
				},
				"seed=$seed: synthetic approvals must not approve or occupy pensum nodes"
			)
			assertEquals(0, progress.approvedCredits, "seed=$seed")
		}
	}
}
