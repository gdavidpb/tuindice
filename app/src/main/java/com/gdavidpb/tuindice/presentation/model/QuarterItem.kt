package com.gdavidpb.tuindice.presentation.model

import androidx.annotation.ColorInt
import com.gdavidpb.tuindice.base.domain.model.Quarter

data class QuarterItem(
        val uid: Long,
        val id: String,
        @ColorInt
        val color: Int,
        val isMock: Boolean,
        val isEditable: Boolean,
        val isCurrent: Boolean,
        val TitleText: CharSequence,
        val gradeDiffText: CharSequence,
        val gradeSumText: CharSequence,
        val creditsText: CharSequence,
        val subjectsItems: List<SubjectItem>,
        val data: Quarter
)