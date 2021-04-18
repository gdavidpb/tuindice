package com.gdavidpb.tuindice.domain.usecase.request

data class CountdownRequest(
        val duration: Long,
        val reset: Boolean
)