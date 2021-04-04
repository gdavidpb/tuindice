package com.gdavidpb.tuindice.domain.usecase.request

import com.gdavidpb.tuindice.domain.model.EvaluationUpdate

data class UpdateEvaluationRequest(
        val update: EvaluationUpdate,
        val dispatchChanges: Boolean
)