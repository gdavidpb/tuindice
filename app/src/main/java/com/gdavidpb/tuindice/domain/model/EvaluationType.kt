package com.gdavidpb.tuindice.domain.model

import androidx.annotation.StringRes
import com.gdavidpb.tuindice.R

enum class EvaluationType(@StringRes val stringRes: Int) {
    TEST(R.string.evaluation_test),
    ESSAY(R.string.evaluation_essay),
    ATTENDANCE(R.string.evaluation_attendance),
    INTERVENTIONS(R.string.evaluation_interventions),
    LABORATORY(R.string.evaluation_laboratory),
    MODEL(R.string.evaluation_model),
    PRESENTATION(R.string.evaluation_presentation),
    PROJECT(R.string.evaluation_project),
    QUIZ(R.string.evaluation_quiz),
    REPORT(R.string.evaluation_report),
    WORKSHOP(R.string.evaluation_workshop),
    WRITTEN_WORK(R.string.evaluation_written_work),
    OTHER(R.string.evaluation_other)
}