package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.StepOutcome

internal sealed interface StepResult {
	val outcome: StepOutcome

	data object Passed : StepResult {
		override val outcome = StepOutcome.Passed
	}

	data object Skipped : StepResult {
		override val outcome = StepOutcome.Skipped
	}

	/** A failed step; [stepIndex] and [step] are set once, by the leaf that failed. */
	data class Failed(
		val kind: FailureKind,
		val message: String,
		val expected: String = "",
		val actual: String = "",
		val stepIndex: Int = -1,
		val step: Step? = null
	) : StepResult {
		override val outcome = StepOutcome.Failed

		fun locatedAt(index: Int, at: Step): Failed =
			if (step != null) this else copy(stepIndex = index, step = at)
	}
}

internal fun passIf(condition: Boolean, kind: FailureKind, message: () -> String): StepResult =
	if (condition) StepResult.Passed else StepResult.Failed(kind, message())
