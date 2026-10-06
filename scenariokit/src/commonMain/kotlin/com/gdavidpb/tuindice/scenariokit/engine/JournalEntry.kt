package com.gdavidpb.tuindice.scenariokit.engine

/** One request WireMock served, as the failure report prints it. */
internal data class JournalEntry(val method: String, val url: String, val status: Int?, val authorization: String?)
