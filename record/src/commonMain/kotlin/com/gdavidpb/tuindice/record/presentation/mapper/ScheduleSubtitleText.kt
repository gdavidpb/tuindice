package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_subtitle_week

/**
 * What the schedule sheet says under its title: the term's name and, when the date falls in one of
 * its weeks, which one ("Sep - Dic 2026 · Semana 3"). Outside the weeks of the term there is only
 * the name, with no separator left hanging.
 */
internal fun scheduleSubtitleText(termName: String, academicWeek: Int?): UiText {
	return if (academicWeek != null) {
		UiText.Resource(Res.string.schedule_subtitle_week, listOf(termName, academicWeek))
	} else {
		UiText.Raw(termName)
	}
}
