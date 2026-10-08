package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts

internal class TextSteps(private val driver: ScenarioDriver, private val poller: Poller) {
	fun execute(step: Step): StepResult = when (step) {
		is Step.EnterText -> enterText(step)
		is Step.SubmitTextEntry -> passIf(driver.submitTextEntry(), FailureKind.ASSERTION) {
			driver.refused("the IME action could not be sent")
		}
		else -> unhandled(step)
	}

	/** Waits for the field, empties it (or checks it is empty), enters the text once and re-reads it. */
	private fun enterText(step: Step.EnterText): StepResult =
		driver.awaitTarget(step.q)
			?: clearBeforeTyping(step)
			?: requireEmptyField(step)
			?: type(step)
			?: reread(step)
			?: StepResult.Passed

	private fun clearBeforeTyping(step: Step.EnterText): StepResult.Failed? =
		if (step.replace && !driver.clearText(step.q)) {
			StepResult.Failed(FailureKind.ASSERTION, driver.refused("clearText ${step.target} was refused"))
		} else {
			null
		}

	/**
	 * Without `replace` the text goes in after whatever the field holds, so a field that is not empty is a scenario
	 * that does not know its start: it fails before a key is typed, and what the field would then show is not blamed
	 * on the typing.
	 */
	private fun requireEmptyField(step: Step.EnterText): StepResult.Failed? {
		val held = if (step.replace) null else driver.readText(step.q)?.takeIf { it.isNotEmpty() }
		return held?.let {
			StepResult.Failed(
				FailureKind.ASSERTION,
				"the field ${step.target} was not empty before typing (it holds ${it.length} characters); nothing was typed"
			)
		}
	}

	private fun type(step: Step.EnterText): StepResult.Failed? {
		val accepted = driver.typeKeys(step.q, step.text)
		if (accepted) return null

		return TypedTextFailures.refused(driver, step, driver.refused("typing into ${step.target} was refused"))
	}

	/**
	 * The field is good when it shows what was typed and shows it again one [Timeouts.PollInterval] later: a first
	 * match alone would miss an echo of the keyboard that lands after it and overwrites the field. The window
	 * [Timeouts.TextReread] bounds the wait for the first match only; the confirming read always follows it, even
	 * when the first match came on the last try. A secure field cannot show its text, but it shows one character per
	 * typed character, so its length is what is compared.
	 */
	private fun reread(step: Step.EnterText): StepResult.Failed? {
		val wanted = step.expect ?: step.text
		var seen: String? = null
		val matched = poller.until(Timeouts.TextReread) {
			seen = driver.readText(step.q)
			matches(step, seen, wanted)
		}
		if (!matched) return TypedTextFailures.unmatched(step, seen, wanted)

		driver.pause(Timeouts.PollInterval)
		val confirmed = driver.readText(step.q)
		return if (matches(step, confirmed, wanted)) null else TypedTextFailures.unheld(step, confirmed, wanted)
	}

	private fun matches(step: Step.EnterText, read: String?, wanted: String): Boolean =
		if (step.secure) read?.length == step.text.length else read == wanted
}
