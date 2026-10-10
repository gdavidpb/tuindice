package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class NoticeViewUiTest {
	@Test
	fun when_titleProvided_then_rendersTitleAndMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			NoticeView(title = "Tu inscripción aparece anulada", message = "Tus notas se mantienen.")
		}

		assertNodeVisible(BaseUiTags.NoticeView)
		assertNodeVisible(BaseUiTags.NoticeIcon)
		onNodeWithTag(BaseUiTags.NoticeTitle).assertTextEquals("Tu inscripción aparece anulada")
		onNodeWithTag(BaseUiTags.NoticeMessage).assertTextEquals("Tus notas se mantienen.")
	}

	@Test
	fun when_titleIsAbsent_then_rendersAOneLineNoticeWithoutTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			NoticeView(message = "Actualizado por última vez el 22 sep.", icon = Icons.Outlined.Schedule)
		}

		assertNodeVisible(BaseUiTags.NoticeMessage)
		assertNodeHidden(BaseUiTags.NoticeTitle)
	}

	@Test
	fun when_notVisible_then_rendersNothing() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			NoticeView(message = "Aviso", visible = false)
		}

		assertNodeHidden(BaseUiTags.NoticeView)
	}
}
