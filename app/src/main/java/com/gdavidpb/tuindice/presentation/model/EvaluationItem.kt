package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.domain.model.Evaluation
import java.util.*

data class EvaluationItem(
        val id: String,
        val typeText: CharSequence,
        val notesText: CharSequence,
        val gradeText: CharSequence,
        val grade: Double,
        val dateText: CharSequence,
        val date: Date,
        val color: Int,
        val isDone: Boolean,
        val isSwiping: Boolean,
        val data: Evaluation
)