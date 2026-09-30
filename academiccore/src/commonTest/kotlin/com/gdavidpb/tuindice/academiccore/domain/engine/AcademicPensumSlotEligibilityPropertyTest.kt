package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumProgress
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSlotEligibility
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.testing.LEGACY_SUBJECT_POOL
import com.gdavidpb.tuindice.academiccore.testing.SUBJECT_POOL
import com.gdavidpb.tuindice.academiccore.testing.nextPensumGraph
import com.gdavidpb.tuindice.academiccore.testing.nextSnapshot
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

private const val ROUNDS = 300

class AcademicPensumSlotEligibilityPropertyTest {
	private val engine = AcademicPensumStatusEngine()

	// The resolver's promise, checked against the engine itself: "counts toward a slot" holds
	// exactly when approving the subject makes the engine approve one more slot, and that slot has
	// the kind the resolver announced.
	@Test
	fun countsTowardSlot_exactlyWhenApprovingTheSubjectFillsASlotOfThatKind() {
		repeat(ROUNDS) { seed ->
			val random = Random(seed.toLong())
			val pensum = random.nextPensumGraph()
			val snapshot = random.nextSnapshot()
			val candidate = (SUBJECT_POOL + LEGACY_SUBJECT_POOL).random(random)
			val credits = random.nextInt(1, 7)
			val before = engine.resolve(pensum, snapshot)
			val after = engine.resolve(
				pensum,
				snapshot.copy(attempts = snapshot.attempts + latestApproved(candidate, credits))
			)

			val eligibility = AcademicPensumSlotEligibilityResolver(pensum, before)
				.eligibilityOf(subjectCode = candidate, credits = credits)
			val newlyApprovedSlots = pensum.approvedSlots(after) - pensum.approvedSlots(before)

			assertEquals(
				newlyApprovedSlots.isNotEmpty(),
				eligibility is AcademicPensumSlotEligibility.CountsTowardSlot,
				"seed=$seed candidate=$candidate credits=$credits eligibility=$eligibility"
			)
			if (eligibility is AcademicPensumSlotEligibility.CountsTowardSlot) {
				assertEquals(
					listOf(eligibility.slotKind),
					newlyApprovedSlots.map(AcademicPensumGraph.Node::slotKind),
					"seed=$seed: the slot the engine filled is not the one the resolver announced"
				)
			}
		}
	}

	private fun AcademicPensumGraph.approvedSlots(progress: AcademicPensumProgress): List<AcademicPensumGraph.Node> {
		return nodes.filter { node ->
			node.nodeType == AcademicPensumGraph.NodeType.SLOT &&
				progress.nodeStatuses[node.id] == AcademicPensumNodeStatus.APPROVED
		}
	}

	private fun latestApproved(subjectCode: String, credits: Int): AcademicPensumSnapshot.Attempt {
		return AcademicPensumSnapshot.Attempt(
			id = "candidate-attempt",
			subjectCode = subjectCode,
			subjectName = subjectCode,
			credits = credits,
			// Later than every generated term, so the greedy assignment reaches it last.
			termOrder = 99999,
			positionInTerm = 0,
			termKind = TermKind.HISTORICAL,
			outcome = AttemptOutcome.APPROVED
		)
	}
}
