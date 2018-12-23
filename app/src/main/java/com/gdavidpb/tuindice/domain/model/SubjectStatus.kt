package com.gdavidpb.tuindice.domain.model

import android.content.Context
import androidx.annotation.StringRes
import com.gdavidpb.tuindice.R

enum class SubjectStatus(
        val value: Int,
        @StringRes private val stringRes: Int
) {
    NONE(-1, 0),
    OK(0, 0),
    RETIRED(1, R.string.retired),
    NO_EFFECT(2, R.string.noEffect),
    APPROVED(3, R.string.approved);

    fun toString(context: Context): String = context.getString(stringRes)

    companion object {
        fun fromValue(value: Int): SubjectStatus = when (value) {
            OK.value -> OK
            RETIRED.value -> RETIRED
            NO_EFFECT.value -> NO_EFFECT
            APPROVED.value -> APPROVED
            else -> NONE
        }
    }
}