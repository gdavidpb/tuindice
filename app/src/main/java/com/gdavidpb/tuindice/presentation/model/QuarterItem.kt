package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.domain.model.Quarter

data class QuarterItem(
        val uid: Long,
        val id: String,
        val color: Int,
        val isMock: Boolean,
        val isEditable: Boolean,
        val startEndDateText: CharSequence,
        val gradeDiffText: CharSequence,
        val gradeSumText: CharSequence,
        val creditsText: CharSequence,
        val subjectsItems: List<SubjectItem>,
        val data: Quarter
)