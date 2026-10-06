package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.Serializable

/** Repo-relative Kotlin source position of the DSL call that produced a step. */
@Serializable
data class Site(val file: String, val line: Int)
