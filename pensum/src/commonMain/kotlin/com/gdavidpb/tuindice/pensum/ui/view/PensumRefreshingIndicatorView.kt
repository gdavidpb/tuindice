package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_refreshing_indicator

@Composable
fun PensumRefreshingIndicatorView(
	modifier: Modifier = Modifier
) {
	val graphColors = pensumGraphColors()
	Row(
		modifier = modifier
			.testTag(PensumUiTags.RefreshingIndicator)
			.background(graphColors.panelBackground, PensumElementShape)
			.border(1.dp, graphColors.current.copy(alpha = 0.42f), PensumElementShape)
			.padding(horizontal = 12.dp, vertical = 8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		CircularProgressIndicator(
			modifier = Modifier.size(14.dp),
			color = graphColors.current,
			strokeWidth = 2.dp
		)
		Text(
			text = stringResource(Res.string.pensum_refreshing_indicator),
			color = graphColors.textPrimary,
			style = MaterialTheme.typography.labelMedium
		)
	}
}
