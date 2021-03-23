package com.gdavidpb.tuindice.ui.adapters.payloads

sealed class EvaluationPayload {
    data class UpdateGrade(val grade: Double) : EvaluationPayload()
    data class UpdateStates(val isDone: Boolean, val isSwiping: Boolean) : EvaluationPayload()
}
