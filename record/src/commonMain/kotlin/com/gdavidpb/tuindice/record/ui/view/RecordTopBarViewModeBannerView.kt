package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.record_view_mode_banner_historical
import tuindice.record.generated.resources.record_view_mode_banner_projection
import tuindice.record.generated.resources.record_view_mode_info_button_description
import tuindice.record.generated.resources.record_view_mode_info_message_historical
import tuindice.record.generated.resources.record_view_mode_info_message_projection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordTopBarViewModeBannerView(
	selectedMode: RecordViewMode,
	modifier: Modifier = Modifier
) {
	val colors = recordViewModeBannerColors(selectedMode)
	val bannerText = when (selectedMode) {
		RecordViewMode.Historical -> stringResource(Res.string.record_view_mode_banner_historical)
		RecordViewMode.Projection -> stringResource(Res.string.record_view_mode_banner_projection)
	}
	val infoMessageText = when (selectedMode) {
		RecordViewMode.Historical -> stringResource(Res.string.record_view_mode_info_message_historical)
		RecordViewMode.Projection -> stringResource(Res.string.record_view_mode_info_message_projection)
	}
	val tooltipState = rememberTooltipState(isPersistent = true)
	val tooltipScope = rememberCoroutineScope()

	LaunchedEffect(selectedMode) {
		tooltipState.dismiss()
	}

	Box(
		modifier = modifier
			.fillMaxWidth()
			.background(color = colors.containerColor)
			.testTag(RecordUiTags.TopBarViewModeBanner)
	) {
		Row(
			modifier = Modifier
				.align(Alignment.Center)
				.padding(horizontal = 16.dp, vertical = 10.dp),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = recordViewModeIcon(
					mode = selectedMode,
					isPrimary = true
				),
				contentDescription = null,
				tint = colors.contentColor
			)
			Text(
				text = bannerText,
				modifier = Modifier.padding(start = 8.dp),
				color = colors.contentColor,
				style = MaterialTheme.typography.labelLarge
			)
		}

		Box(
			modifier = Modifier.align(Alignment.CenterEnd),
		) {
			TooltipBox(
				positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
					TooltipAnchorPosition.Below
				),
				tooltip = {
					PlainTooltip(
						caretShape = TooltipDefaults.caretShape()
					) {
						Text(
							modifier = Modifier.testTag(RecordUiTags.ViewModeInfoMessage),
							text = infoMessageText
						)
					}
				},
				state = tooltipState,
				onDismissRequest = tooltipState::dismiss,
				enableUserInput = false
			) {
				IconButton(
					modifier = Modifier.testTag(RecordUiTags.TopBarViewModeInfoButton),
					onClick = {
						if (tooltipState.isVisible) {
							tooltipState.dismiss()
						} else {
							tooltipScope.launch {
								tooltipState.show()
							}
						}
					}
				) {
					Icon(
						imageVector = Icons.Outlined.Info,
						contentDescription = stringResource(Res.string.record_view_mode_info_button_description),
						tint = colors.contentColor
					)
				}
			}
		}
	}
}
