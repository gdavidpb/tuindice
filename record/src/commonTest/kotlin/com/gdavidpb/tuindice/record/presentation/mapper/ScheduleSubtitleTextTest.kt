package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_subtitle_week
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleSubtitleTextTest {
	@Test
	fun withAWeek_theTermIsFollowedByIt() {
		assertEquals(
			UiText.Resource(Res.string.schedule_subtitle_week, listOf("Sep - Dic 2026", 3)),
			scheduleSubtitleText(termName = "Sep - Dic 2026", academicWeek = 3)
		)
	}

	@Test
	fun withoutAWeek_itIsTheTermAlone_withNoSeparatorLeftHanging() {
		assertEquals(
			UiText.Raw("Sep - Dic 2026"),
			scheduleSubtitleText(termName = "Sep - Dic 2026", academicWeek = null)
		)
	}
}
