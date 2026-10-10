package com.gdavidpb.tuindice.scenariokit.engine

/** A request the app "made" to the fake backend. */
data class AppRequest(val method: String, val url: String, val status: Int, val authorization: String?)
