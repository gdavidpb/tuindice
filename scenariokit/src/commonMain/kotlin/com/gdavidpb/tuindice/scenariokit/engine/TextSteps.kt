package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.TextEntryMode
import com.gdavidpb.tuindice.scenariokit.model.Timeouts

internal class TextSteps(private val driver: ScenarioDriver, private val poller: Poller) {
	fun execute(step: Step): StepResult = when (step) {
		is Step.EnterText -> enterText(step)
		is Step.ClearText -> driver.awaitTarget(step.q)
			?: passIf(driver.clearText(step.q), FailureKind.ASSERTION) { "clearText ${step.target} was refused" }
		is Step.FinishTextEntry -> passIf(
			driver.finishTextEntry(),
			FailureKind.ASSERTION
		) { "keyboard could not be dismissed" }
		else -> unhandled(step)
	}

	/** Waits for the field, optionally clears it, enters the text once and re-reads it. */
	private fun enterText(step: Step.EnterText): StepResult =
		driver.awaitTarget(step.q)
			?: clearBeforeTyping(step)
			?: type(step)
			?: reread(step)
			?: StepResult.Passed

	private fun clearBeforeTyping(step: Step.EnterText): StepResult.Failed? =
		if (step.replace && !driver.clearText(step.q)) {
			StepResult.Failed(FailureKind.ASSERTION, "clearText ${step.target} was refused")
		} else {
			null
		}

	private fun type(step: Step.EnterText): StepResult.Failed? {
		val accepted = when (step.mode) {
			TextEntryMode.Keys -> driver.typeKeys(step.q, step.text)
			TextEntryMode.Set -> driver.setText(step.q, step.text)
		}
		return if (accepted) null else StepResult.Failed(FailureKind.ASSERTION, "typing into ${step.target} was refused")
	}

	/** Secure fields cannot be read back; for the rest, the driver does not retype, the interpreter judges. */
	private fun reread(step: Step.EnterText): StepResult.Failed? {
		if (step.secure) return null
		val wanted = step.expect ?: step.text
		var seen: String? = null
		val matched = poller.until(Timeouts.TextReread) {
			seen = driver.readText(step.q)
			seen == wanted
		}
		return when {
			matched -> null
			seen == null -> StepResult.Failed(FailureKind.ASSERTION, "${step.target} could not be read back after typing")
			else -> StepResult.Failed(
				FailureKind.TYPED_TEXT_MISMATCH,
				"typed \"$wanted\" into ${step.target} but the field shows \"$seen\"",
				expected = wanted,
				actual = seen.orEmpty()
			)
		}
	}
}
