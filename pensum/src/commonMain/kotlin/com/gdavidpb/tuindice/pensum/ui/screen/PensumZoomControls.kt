package com.gdavidpb.tuindice.pensum.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_zoom_in
import tuindice.pensum.generated.resources.pensum_zoom_out

@Composable
internal fun ZoomControls(
	onZoomIn: () -> Unit,
	onZoomOut: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.width(48.dp)
			.height(MinimapHeight)
			.background(Color.Black.copy(alpha = 0.68f), RoundedCornerShape(8.dp))
			.border(1.dp, Available, RoundedCornerShape(8.dp))
	) {
		IconButton(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.testTag(PensumUiTags.ZoomIn),
			onClick = onZoomIn
		) {
			Icon(
				imageVector = Icons.Filled.Add,
				contentDescription = stringResource(Res.string.pensum_zoom_in),
				tint = TextPrimary
			)
		}
		Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(Available.copy(alpha = 0.4f)))
		IconButton(
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth()
				.testTag(PensumUiTags.ZoomOut),
			onClick = onZoomOut
		) {
			Icon(
				imageVector = Icons.Filled.Remove,
				contentDescription = stringResource(Res.string.pensum_zoom_out),
				tint = TextPrimary
			)
		}
	}
}
