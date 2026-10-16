package com.gdavidpb.tuindice.sampleflow.presentation.mapper

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate

// Disparo de academic-week-monday-reimplemented: el lunes lo calcula AcademicWeekEngine.mondayOf.
fun sampleFlowMonday(date: LocalDate): LocalDate = date.minus(DatePeriod(days = date.dayOfWeek.ordinal))
