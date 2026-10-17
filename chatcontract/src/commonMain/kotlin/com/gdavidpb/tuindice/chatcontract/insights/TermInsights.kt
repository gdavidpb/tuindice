package com.gdavidpb.tuindice.chatcontract.insights

import kotlinx.serialization.Serializable

/** Insights the assistant has about the user's academic terms. */
@Serializable
public data class TermInsights(public val items: List<TermInsight>)
