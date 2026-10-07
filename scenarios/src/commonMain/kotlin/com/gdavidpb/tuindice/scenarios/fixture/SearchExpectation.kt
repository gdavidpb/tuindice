package com.gdavidpb.tuindice.scenarios.fixture

/** What the subject search answers for [query]: these codes are among the results. */
data class SearchExpectation(val query: String, val subjectCodes: List<String>)
