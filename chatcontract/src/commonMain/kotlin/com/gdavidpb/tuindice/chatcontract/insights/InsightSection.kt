package com.gdavidpb.tuindice.chatcontract.insights

import kotlinx.serialization.Serializable

/**
 * One section of a term report: a [title] of at most 60 characters and a [text] of at most 400, written by
 * the model from the figures of the term. The figures themselves come from the record, not from the model.
 */
@Serializable
public data class InsightSection(public val title: String, public val text: String)
