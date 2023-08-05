package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.utils.DEFAULT_TIME_ZONE
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import java.util.Date

private val zoneId = ZoneId.of(DEFAULT_TIME_ZONE.id, ZoneId.SHORT_IDS)

fun Date.daysDistance(): Long {
	val now = LocalDate.now(zoneId)
	val target = Instant.ofEpochMilli(time).atZone(zoneId).toLocalDate()

	return ChronoUnit.DAYS.between(now, target)
}

fun Date.weeksDistance(): Long {
	val now = LocalDate.now(zoneId)
	val target = Instant.ofEpochMilli(time).atZone(zoneId).toLocalDate()

	return ChronoUnit.WEEKS.between(now, target)
}