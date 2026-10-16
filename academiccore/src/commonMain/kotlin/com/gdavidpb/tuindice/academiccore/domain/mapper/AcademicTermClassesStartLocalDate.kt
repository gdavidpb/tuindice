package com.gdavidpb.tuindice.academiccore.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import kotlinx.datetime.LocalDate

/** [AcademicTerm.classesStartDate] as a [LocalDate], or null when it is absent or not a valid ISO date. */
fun AcademicTerm.classesStartLocalDate(): LocalDate? = parseClassesStartDate(classesStartDate)
