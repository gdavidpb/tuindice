package com.gdavidpb.tuindice.chatcontract.insights

import kotlinx.serialization.Serializable

/** The student's thumbs on a term report: the body of `POST /assistant/v1/insights/{insightId}/feedback`. */
@Serializable
public data class InsightFeedbackRequest(public val useful: Boolean)
