package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Step

/** Starts the app again keeping its state, with [arguments], or with the ones the scenario started with when none are given. */
fun StepBuilder.relaunch(arguments: Map<String, String> = emptyMap()) = add(Step.Relaunch(arguments, site()))

fun StepBuilder.foreground() = add(Step.Foreground(site()))
