package com.gdavidpb.tuindice.domain.usecase.request

data class CountdownRequest(
        val time: Long,
        val reset: Boolean
)