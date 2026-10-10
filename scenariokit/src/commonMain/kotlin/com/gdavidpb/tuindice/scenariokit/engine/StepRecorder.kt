package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.StepOutcome
import com.gdavidpb.tuindice.scenariokit.model.StepRecord

internal class StepRecorder {
	private val records = mutableListOf<StepRecord>()

	val all: List<StepRecord> get() = records.toList()

	/** Registers [step] in pre-order and returns its index. */
	fun begin(step: Step): Int {
		val index = records.size
		records += StepRecord(index, step::class.simpleName.orEmpty(), step.target, 0L, StepOutcome.Passed)
		return index
	}

	fun end(index: Int, outcome: StepOutcome, durationMs: Long) {
		records[index] = records[index].copy(durationMs = durationMs, outcome = outcome)
	}
}
