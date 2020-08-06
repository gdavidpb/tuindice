package com.gdavidpb.tuindice.utils

import com.gdavidpb.tuindice.data.model.service.DstScheduledSubject
import com.gdavidpb.tuindice.domain.model.service.DstQuarter
import com.gdavidpb.tuindice.domain.model.service.DstSubject
import java.util.*

fun createQuarter(startDate: Date,
                  endDate: Date,
                  subjects: List<DstSubject>,
                  status: Int = STATUS_QUARTER_COMPLETED) = DstQuarter(
        startDate = startDate,
        endDate = endDate,
        grade = subjects.computeGrade(),
        gradeSum = 0.0,
        status = status,
        subjects = subjects
)

fun createSubject(code: String,
                  name: String,
                  credits: Int,
                  grade: Int,
                  status: String = "") = DstSubject(
        code = code,
        name = name,
        credits = credits,
        grade = grade,
        status = status
)

fun createScheduledSubject(code: String,
                           name: String,
                           credits: Int) = DstScheduledSubject(
        code = code,
        section = 0,
        name = name,
        credits = credits,
        status = "",
        schedule = listOf()
)