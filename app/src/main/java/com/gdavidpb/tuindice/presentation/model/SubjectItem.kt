package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.domain.model.Subject

data class SubjectItem(
        val id: String,
        val nameText: CharSequence,
        val codeText: CharSequence,
        val gradeText: CharSequence,
        val creditsText: CharSequence,
        val data: Subject
)