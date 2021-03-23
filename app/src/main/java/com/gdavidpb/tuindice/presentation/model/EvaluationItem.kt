package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.domain.model.Evaluation
import java.util.*

data class EvaluationItem(
        val uid: Long,
        val id: String,
        val grade: Double,
        val maxGrade: Double,
        val typeText: CharSequence,
        val notesText: CharSequence,
        val dateText: CharSequence,
        val date: Date,
        val isDone: Boolean,
        val isSwiping: Boolean,
        val data: Evaluation
)