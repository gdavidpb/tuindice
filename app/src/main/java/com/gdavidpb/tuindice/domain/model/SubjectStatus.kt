package com.gdavidpb.tuindice.domain.model

import androidx.annotation.StringRes
import com.gdavidpb.tuindice.R

enum class SubjectStatus(
        val value: Int,
        @StringRes private val stringRes: Int
) {
    RETIRED(1, R.string.retired),
    NO_EFFECT(2, R.string.noEffect),
    APPROVED(3, R.string.approved)
}