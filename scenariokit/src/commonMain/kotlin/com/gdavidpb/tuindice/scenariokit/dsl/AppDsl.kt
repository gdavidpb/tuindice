package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Step

/** Starts the app again keeping its state, with new launch arguments. */
fun StepBuilder.relaunch(arguments: Map<String, String> = emptyMap()) = add(Step.Relaunch(arguments, site()))

fun StepBuilder.foreground() = add(Step.Foreground(site()))
