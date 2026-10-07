package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step

/** Every query a step looks for or acts on. */
internal fun queriesOf(step: Step): List<Query> = gestureQueries(step) + waitQueries(step)

private fun gestureQueries(step: Step): List<Query> = when (step) {
	is Step.Tap -> listOf(step.q)
	is Step.TapAt -> listOfNotNull(step.q)
	is Step.DoubleTap -> listOf(step.q)
	is Step.EnterText -> listOf(step.q)
	is Step.ClearText -> listOf(step.q)
	is Step.Swipe -> listOfNotNull(step.from)
	else -> emptyList()
}

private fun waitQueries(step: Step): List<Query> = when (step) {
	is Step.WaitVisible -> listOf(step.q)
	is Step.WaitGone -> listOf(step.q)
	is Step.WaitAnyVisible -> step.queries
	is Step.AssertEnabled -> listOf(step.q)
	is Step.ScrollUntilVisible -> listOf(step.q)
	is Step.Settle -> listOfNotNull(step.q)
	is Step.IfVisible -> listOf(step.q)
	is Step.IfGone -> listOf(step.q)
	else -> emptyList()
}

/** Every timeout, in milliseconds, a step waits for. */
internal fun timeoutsOf(step: Step): List<Long> = when (step) {
	is Step.WaitVisible -> listOf(step.timeoutMs)
	is Step.WaitGone -> listOf(step.timeoutMs)
	is Step.WaitAnyVisible -> listOf(step.timeoutMs)
	is Step.AssertEnabled -> listOf(step.timeoutMs)
	is Step.ScrollUntilVisible -> listOf(step.timeoutMs)
	is Step.Settle -> listOf(step.timeoutMs)
	is Step.IfVisible -> listOf(step.withinMs)
	is Step.IfGone -> listOf(step.withinMs)
	is Step.ExpectRequest -> listOf(step.timeoutMs)
	else -> emptyList()
}
