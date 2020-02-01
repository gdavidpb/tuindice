package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.domain.model.Evaluation
import java.util.*

data class EvaluationItem(
        val id: String,
        val typeText: CharSequence,
        val notesText: CharSequence,
        val gradesText: CharSequence,
        val dateText: CharSequence,
        val date: Date,
        val color: Int,
        val isDone: Boolean,
        val isLoading: Boolean,
        val data: Evaluation
)