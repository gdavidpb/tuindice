package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.Serializable

/** A scenario excluded from certification until [until] (ISO date), with the reason on record. */
@Serializable
data class Quarantine(val reason: String, val until: String)
