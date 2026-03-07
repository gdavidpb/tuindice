package com.gdavidpb.tuindice.base.domain.model

enum class RiskOperation(
    val code: String
) {
    IssueTokens("auth.issue_tokens"),
    RefreshTokens("auth.refresh_tokens"),
    ReissueTokens("auth.reissue_tokens")
}
