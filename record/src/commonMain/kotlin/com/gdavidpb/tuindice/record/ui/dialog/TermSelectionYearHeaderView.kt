package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun TermSelectionYearHeaderView(year: Int) {
	Text(
		modifier = Modifier.testTag(RecordUiTags.termSelectionYear(year)),
		text = year.toString(),
		style = MaterialTheme.typography.titleSmall,
		fontWeight = FontWeight.Black,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
}
