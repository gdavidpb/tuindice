package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.datetime.LocalDate

/** [AcademicTerm.classesStartDate] as a [LocalDate], or null when it is absent or not a valid ISO date. */
fun AcademicTerm.classesStartLocalDate(): LocalDate? = parseClassesStartDate(classesStartDate)
