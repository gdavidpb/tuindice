// Fixture de semgrep --test para academiccore.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
// La exención de academiccore/** no aplica en --test; se valida en la prueba de generalidad.
package com.gdavidpb.tuindice.sample.domain.engine

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate

// ruleid: academic-week-monday-reimplemented
fun sampleMonday(date: LocalDate): LocalDate = date.minus(DatePeriod(days = date.dayOfWeek.ordinal))

// ruleid: academic-week-monday-reimplemented
fun sampleMondayIso(date: LocalDate): LocalDate = date.minus(DatePeriod(days = date.dayOfWeek.isoDayNumber - 1))

// ruleid: academic-week-monday-reimplemented
fun sampleMondayUnit(date: LocalDate): LocalDate = date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)

// ok: academic-week-monday-reimplemented
fun sampleShift(date: LocalDate): LocalDate = date.minus(DatePeriod(days = 1))

// ok: academic-week-monday-reimplemented
fun sampleOtherDay(date: LocalDate, other: LocalDate): LocalDate = date.minus(DatePeriod(days = other.dayOfWeek.ordinal))
