package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleGridDefaults
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScheduleNowLineViewUiTest {
	@Test
	fun when_theLineIsDrawn_then_itIsReadAsAhora() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleNowLineView(modifier = Modifier.width(72.dp))
		}

		assertNodeVisible(RecordUiTags.ScheduleNowLine)
		onNodeWithTag(RecordUiTags.ScheduleNowLine).assertContentDescriptionEquals("Ahora")
	}

	@Test
	fun when_theLineIsGivenAColumn_then_itCrossesAllOfIt_atTheHeightOfItsDot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleNowLineView(modifier = Modifier.width(144.dp))
		}

		// The dot is the tallest part: the column offsets the line by half of it to centre it on the hour.
		onNodeWithTag(RecordUiTags.ScheduleNowLine)
			.assertWidthIsEqualTo(144.dp)
			.assertHeightIsEqualTo(ScheduleGridDefaults.NowDotSize)
	}
}
