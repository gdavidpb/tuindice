package com.gdavidpb.tuindice.scenariokit.engine

internal const val HARNESS_RESET_AUTHORIZATION = "Bearer e2e-harness-reset"

/** One backend reset call made before every scenario. */
data class ResetRequest(val method: String, val path: String, val authorization: String?)
