package com.gdavidpb.tuindice.login.domain.model

import com.gdavidpb.tuindice.base.domain.model.RiskOperation

enum class IssueTokensFlow(
    val headerValue: String,
    val operation: RiskOperation
) {
    IssueTokens(
        headerValue = "issue_tokens",
        operation = RiskOperation.IssueTokens
    ),
    ReissueTokens(
        headerValue = "reissue_tokens",
        operation = RiskOperation.ReissueTokens
    )
}
