package com.gdavidpb.tuindice.academiccore.testing

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlin.random.Random

/**
 * Deterministic generators for the academic domain: every value derives from the given
 * [Random], so a failing property reproduces from the seed embedded in its message.
 * The subject pool is intentionally small to make retakes likely.
 */
internal val SUBJECT_POOL = listOf(
	"MA1111", "MA1112", "CI2525", "EP1308", "ID1101", "FS1111", "QM1181", "EP4135"
)

// A disjoint namespace standing in for pre-migration DST codes: never a pensum node's own
// subjectCode, only ever reachable through a curated EQUIVALENCE rule.
internal val LEGACY_SUBJECT_POOL = listOf("LL1111", "LL1112", "CS1111", "CS1112")

private val SNAPSHOT_SUBJECT_POOL = SUBJECT_POOL + LEGACY_SUBJECT_POOL

internal fun Random.nextScore(gradingMode: AttemptGradingMode): AttemptScore {
	return when (gradingMode) {
		AttemptGradingMode.NUMERIC -> when (nextInt(4)) {
			0 -> AttemptScore.empty()
			else -> AttemptScore.numeric(nextInt(0, 6))
		}

		AttemptGradingMode.QUALITATIVE_PASS_FAIL -> when (nextInt(4)) {
			0 -> AttemptScore.empty()
			1 -> AttemptScore.symbolic("A")
			2 -> AttemptScore.symbolic("R")
			else -> AttemptScore.symbolic("X")
		}
	}
}

internal fun Random.nextAttempt(
	id: String,
	subjectCode: String = SUBJECT_POOL[nextInt(SUBJECT_POOL.size)]
): AcademicAttempt {
	val gradingMode = if (nextInt(5) == 0) {
		AttemptGradingMode.QUALITATIVE_PASS_FAIL
	} else {
		AttemptGradingMode.NUMERIC
	}

	return AcademicAttempt(
		id = id,
		subjectCode = subjectCode,
		subjectName = "Subject $subjectCode",
		credits = nextInt(1, 7),
		gradingMode = gradingMode,
		academicScore = nextScore(gradingMode),
		academicOutcome = AttemptOutcome.entries[nextInt(AttemptOutcome.entries.size)]
	)
}

internal fun Random.nextRecord(
	maxTerms: Int = 6,
	kinds: List<TermKind> = TermKind.entries,
	withOverrides: Boolean = false
): AcademicRecord {
	val termCount = nextInt(0, maxTerms + 1)
	val periods = buildList {
		for (year in 2020..2026) {
			for (period in AcademicTermPeriod.entries) {
				add(year to period)
			}
		}
	}.shuffled(this).take(termCount).sortedBy { (year, period) -> year * 10 + period.sequence }

	var attemptCounter = 0
	val terms = periods.mapIndexed { index, (year, period) ->
		AcademicTerm(
			id = "term-$index",
			periodYear = year,
			periodCode = period,
			// The latest generated term may be CURRENT/SYNTHETIC; earlier ones historical
			// half the time, to exercise frozen-metrics and synthetic filtering paths.
			kind = if (index == periods.lastIndex) {
				kinds[nextInt(kinds.size)]
			} else if (nextBoolean()) {
				TermKind.HISTORICAL
			} else {
				kinds[nextInt(kinds.size)]
			},
			attempts = List(nextInt(0, 6)) {
				nextAttempt(id = "attempt-${attemptCounter++}")
			}
		)
	}

	val allAttempts = terms.flatMap(AcademicTerm::attempts)
	val overrides = if (withOverrides && allAttempts.isNotEmpty()) {
		List(nextInt(0, minOf(3, allAttempts.size) + 1)) { index ->
			val target = allAttempts[nextInt(allAttempts.size)]
			AttemptOverride(
				attemptId = target.id,
				score = AttemptScore.numeric(nextInt(0, 6)),
				outcome = if (nextBoolean()) AttemptOutcome.APPROVED else null,
				updatedAtMillis = 1_000L + index
			)
		}.distinctBy(AttemptOverride::attemptId)
	} else {
		emptyList()
	}

	return AcademicRecord(
		id = "record",
		terms = terms,
		attemptOverrides = overrides
	)
}

/**
 * Record with unique numeric subjects per record (no retakes), every attempt counting
 * toward both averages — the subset where a naive weighted mean must match the engine.
 */
internal fun Random.nextSimpleNumericRecord(maxTerms: Int = 4): AcademicRecord {
	val termCount = nextInt(1, maxTerms + 1)
	var subjectCounter = 0
	var attemptCounter = 0

	val terms = List(termCount) { index ->
		AcademicTerm(
			id = "term-$index",
			periodYear = 2020 + index,
			periodCode = AcademicTermPeriod.SEP_DEC,
			kind = TermKind.HISTORICAL,
			attempts = List(nextInt(1, 5)) {
				val value = nextInt(1, 6)
				AcademicAttempt(
					id = "attempt-${attemptCounter++}",
					subjectCode = "SC${subjectCounter++}",
					subjectName = "Subject $subjectCounter",
					credits = nextInt(1, 7),
					gradingMode = AttemptGradingMode.NUMERIC,
					academicScore = AttemptScore.numeric(value),
					academicOutcome = if (value >= 3) AttemptOutcome.APPROVED else AttemptOutcome.FAILED
				)
			}
		)
	}

	return AcademicRecord(id = "record", terms = terms)
}

internal fun Random.nextPensumGraph(maxNodes: Int = 8): AcademicPensumGraph {
	val nodeCount = nextInt(1, maxNodes + 1)
	val nodes = List(nodeCount) { index ->
		val isSlot = nextInt(4) == 0
		AcademicPensumGraph.Node(
			id = "node-$index",
			nodeType = if (isSlot) {
				AcademicPensumGraph.NodeType.SLOT
			} else {
				AcademicPensumGraph.NodeType.COURSE
			},
			subjectCode = if (isSlot) null else SUBJECT_POOL[nextInt(SUBJECT_POOL.size)],
			credits = nextInt(1, 7),
			fulfillmentRules = if (isSlot) {
				listOf(
					AcademicPensumGraph.FulfillmentRule(
						ruleType = "ELECTIVE",
						subjectCodes = emptyList(),
						subjectCodePrefixes = listOf(SUBJECT_POOL[nextInt(SUBJECT_POOL.size)].take(2)),
						minCredits = if (nextBoolean()) nextInt(1, 4) else null,
						minSubjects = null
					)
				)
				// A third of COURSE nodes also carry a curated EQUIVALENCE rule, so the properties
				// below exercise a mix of course-fulfillment and no-fulfillment shapes.
			} else if (nextInt(3) == 0) {
				listOf(
					AcademicPensumGraph.FulfillmentRule(
						ruleType = "EQUIVALENCE",
						subjectCodes = listOf(LEGACY_SUBJECT_POOL[nextInt(LEGACY_SUBJECT_POOL.size)]),
						subjectCodePrefixes = emptyList(),
						minCredits = null,
						minSubjects = null
					)
				)
			} else {
				emptyList()
			}
		)
	}

	val edges = List(nextInt(0, nodeCount * 2)) {
		AcademicPensumGraph.Edge(
			// Deliberately allows dangling ids to assert the engine never crashes on them.
			fromNodeId = if (nextInt(8) == 0) "ghost-${nextInt(3)}" else nodes[nextInt(nodes.size)].id,
			toNodeId = nodes[nextInt(nodes.size)].id,
			relationshipType = if (nextBoolean()) {
				AcademicPensumGraph.RelationshipType.REQUIREMENT
			} else {
				AcademicPensumGraph.RelationshipType.COREQUISITE
			}
		)
	}

	return AcademicPensumGraph(nodes = nodes, edges = edges)
}

internal fun Random.nextSnapshot(maxAttempts: Int = 10): AcademicPensumSnapshot {
	return AcademicPensumSnapshot(
		attempts = List(nextInt(0, maxAttempts + 1)) { index ->
			AcademicPensumSnapshot.Attempt(
				id = "snapshot-attempt-$index",
				subjectCode = SNAPSHOT_SUBJECT_POOL[nextInt(SNAPSHOT_SUBJECT_POOL.size)],
				subjectName = "Subject $index",
				credits = nextInt(1, 7),
				termOrder = nextInt(20200, 20270),
				positionInTerm = nextInt(0, 6),
				termKind = TermKind.entries[nextInt(TermKind.entries.size)],
				outcome = AttemptOutcome.entries[nextInt(AttemptOutcome.entries.size)]
			)
		}
	)
}
