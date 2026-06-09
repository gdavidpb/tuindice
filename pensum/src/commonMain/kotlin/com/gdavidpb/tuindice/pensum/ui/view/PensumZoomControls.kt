package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.model.toStatusIconVisual
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_fit_to_screen
import tuindice.pensum.generated.resources.pensum_focus_progress
import tuindice.pensum.generated.resources.pensum_hide_minimap
import tuindice.pensum.generated.resources.pensum_show_minimap
import tuindice.pensum.generated.resources.pensum_zoom_in
import tuindice.pensum.generated.resources.pensum_zoom_out

@Composable
fun PensumZoomControls(
	isCurrentFocusVisible: Boolean,
	isMinimapToggleVisible: Boolean,
	isMinimapVisible: Boolean,
	isFitToScreenVisible: Boolean,
	onFocusProgress: () -> Unit,
	onFitToScreen: () -> Unit,
	onToggleMinimap: () -> Unit,
	onZoomIn: () -> Unit,
	onZoomOut: () -> Unit,
	modifier: Modifier = Modifier
) {
	val graphColors = pensumGraphColors()
	val controlsCount = 2 +
		(if (isCurrentFocusVisible) 1 else 0) +
		(if (isFitToScreenVisible) 1 else 0) +
		(if (isMinimapToggleVisible) 1 else 0)
	val controlsHeight = ZoomControlButtonHeight * controlsCount.toFloat()
	val currentStatusIcon = PensumNodeStatusType.CURRENT.toStatusIconVisual()

	Column(
		modifier = modifier
			.width(48.dp)
			.height(controlsHeight)
			.background(graphColors.controlsBackground, PensumElementShape)
			.border(1.dp, graphColors.canvasNeutral, PensumElementShape)
	) {
		if (isCurrentFocusVisible) {
			IconButton(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth()
					.testTag(PensumUiTags.FocusProgress),
				onClick = onFocusProgress
			) {
				Icon(
					imageVector = currentStatusIcon.imageVector,
					contentDescription = stringResource(Res.string.pensum_focus_progress),
					tint = graphColors.current
				)
			}
			PensumZoomControlDivider()
		}
		if (isFitToScreenVisible) {
			IconButton(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth()
					.testTag(PensumUiTags.FitToScreen),
				onClick = onFitToScreen
			) {
				Icon(
					imageVector = Icons.Outlined.CenterFocusStrong,
					contentDescription = stringResource(Res.string.pensum_fit_to_screen),
					tint = graphColors.textSecondary
				)
			}
			PensumZoomControlDivider()
		}
		if (isMinimapToggleVisible) {
			IconButton(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth()
					.testTag(PensumUiTags.MinimapToggle),
				onClick = onToggleMinimap
			) {
				Icon(
					imageVector = Icons.Outlined.Map,
					contentDescription = stringResource(
						if (isMinimapVisible) {
							Res.string.pensum_hide_minimap
						} else {
							Res.string.pensum_show_minimap
						}
					),
					tint = if (isMinimapVisible) graphColors.current else graphColors.textSecondary
				)
			}
			PensumZoomControlDivider()
		}
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
				tint = graphColors.textSecondary
			)
		}
		PensumZoomControlDivider()
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
				tint = graphColors.textSecondary
			)
		}
	}
}

@Composable
private fun PensumZoomControlDivider() {
	val graphColors = pensumGraphColors()
	Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(graphColors.canvasNeutral.copy(alpha = 0.4f)))
}
