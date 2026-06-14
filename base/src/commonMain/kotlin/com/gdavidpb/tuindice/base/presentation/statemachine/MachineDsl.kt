package com.gdavidpb.tuindice.base.presentation.statemachine

/**
 * Scope control for the machine-definition DSL: blocks nested inside a state's
 * transitions cannot accidentally call the outer builder's receivers.
 */
@DslMarker
annotation class MachineDsl
