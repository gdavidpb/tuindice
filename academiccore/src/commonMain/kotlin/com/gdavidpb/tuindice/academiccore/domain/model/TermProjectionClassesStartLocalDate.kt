package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.datetime.LocalDate

/** [TermProjection.classesStartDate] as a [LocalDate], or null when it is absent or not a valid ISO date. */
fun TermProjection.classesStartLocalDate(): LocalDate? = parseClassesStartDate(classesStartDate)
