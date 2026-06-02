package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBasis
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadConfidence
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadDetail
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_load_info_content_description
import tuindice.record.generated.resources.create_term_load_loading
import tuindice.record.generated.resources.create_term_load_placeholder
import tuindice.record.generated.resources.create_term_load_tooltip_available
import tuindice.record.generated.resources.create_term_load_tooltip_career
import tuindice.record.generated.resources.create_term_load_tooltip_error
import tuindice.record.generated.resources.create_term_load_tooltip_global
import tuindice.record.generated.resources.create_term_load_tooltip_insufficient_reference
import tuindice.record.generated.resources.create_term_load_tooltip_loading
import tuindice.record.generated.resources.create_term_load_tooltip_missing_difficulty
import tuindice.record.generated.resources.create_term_load_tooltip_personal_high
import tuindice.record.generated.resources.create_term_load_tooltip_personal_low
import tuindice.record.generated.resources.create_term_load_tooltip_personal_medium
import tuindice.record.generated.resources.create_term_load_tooltip_placeholder
import tuindice.record.generated.resources.create_term_load_tooltip_terms_plural
import tuindice.record.generated.resources.create_term_load_tooltip_terms_singular
import tuindice.record.generated.resources.create_term_load_unavailable
import tuindice.record.generated.resources.create_term_load_tooltip_unavailable
import tuindice.record.generated.resources.create_term_load_tooltip_unknown_subject

private val LoadChipWidth = 168.dp
private val LoadChipDefaultHeight = 48.dp

@Composable
fun CreateTermLoadChip(
	loadPreview: SyntheticTermLoadPreview?,
	hasSelectedSubjects: Boolean,
	isLoading: Boolean,
	hasError: Boolean,
	modifier: Modifier = Modifier.height(LoadChipDefaultHeight)
) {
	val status = when {
		isLoading -> LoadChipStatus.Loading
		!hasSelectedSubjects -> LoadChipStatus.Placeholder
		hasError -> LoadChipStatus.Unavailable
		loadPreview?.available == false -> LoadChipStatus.Unavailable
		loadPreview?.band != null -> LoadChipStatus.Available(loadPreview.band)
		else -> LoadChipStatus.Unavailable
	}
	val color = status.color(
		outlineColor = MaterialTheme.colorScheme.outline,
		primaryColor = MaterialTheme.colorScheme.primary,
		onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant,
		errorColor = MaterialTheme.colorScheme.error
	)
	val label = status.label(
		placeholderText = stringResource(Res.string.create_term_load_placeholder),
		loadingText = stringResource(Res.string.create_term_load_loading),
		unavailableText = stringResource(Res.string.create_term_load_unavailable)
	)
	val tooltipMessage = createTermLoadTooltipMessage(
		loadPreview = loadPreview,
		hasSelectedSubjects = hasSelectedSubjects,
		isLoading = isLoading,
		hasError = hasError
	)

	Surface(
		modifier = modifier
			.width(LoadChipWidth),
		shape = RoundedCornerShape(12.dp),
		color = color.copy(alpha = 0.12f),
		border = BorderStroke(
			width = 1.dp,
			color = color.copy(alpha = 0.9f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxSize()
				.padding(start = 12.dp, end = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			if (status == LoadChipStatus.Loading) {
				CircularProgressIndicator(
					modifier = Modifier.size(12.dp),
					color = color,
					strokeWidth = 2.dp
				)
			} else {
				Box(
					modifier = Modifier
						.size(10.dp)
						.background(color, CircleShape)
				)
			}
			Text(
				modifier = Modifier.weight(1f),
				text = label,
				style = MaterialTheme.typography.labelLarge,
				color = color,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			CreateTermLoadInfoButton(
				message = tooltipMessage,
				color = color
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTermLoadInfoButton(
	message: String,
	color: Color
) {
	val tooltipState = rememberTooltipState(isPersistent = true)
	val tooltipScope = rememberCoroutineScope()

	LaunchedEffect(message) {
		tooltipState.dismiss()
	}

	TooltipBox(
		positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
			TooltipAnchorPosition.Below
		),
		tooltip = {
			PlainTooltip(
				caretShape = TooltipDefaults.caretShape()
			) {
				Text(
					modifier = Modifier.testTag(RecordUiTags.CreateSyntheticTermLoadInfoMessage),
					text = message
				)
			}
		},
		state = tooltipState,
		onDismissRequest = tooltipState::dismiss,
		enableUserInput = false
	) {
		IconButton(
			modifier = Modifier
				.size(28.dp)
				.testTag(RecordUiTags.CreateSyntheticTermLoadInfoButton),
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
				modifier = Modifier.size(18.dp),
				imageVector = Icons.Outlined.Info,
				contentDescription = stringResource(Res.string.create_term_load_info_content_description),
				tint = color
			)
		}
	}
}

@Composable
private fun createTermLoadTooltipMessage(
	loadPreview: SyntheticTermLoadPreview?,
	hasSelectedSubjects: Boolean,
	isLoading: Boolean,
	hasError: Boolean
): String {
	val baseMessage = when {
		isLoading ->
			stringResource(Res.string.create_term_load_tooltip_loading)

		!hasSelectedSubjects ->
			stringResource(Res.string.create_term_load_tooltip_placeholder)

		hasError ->
			stringResource(Res.string.create_term_load_tooltip_error)

		loadPreview?.available == true ->
			loadPreview.availableTooltipMessage()

		loadPreview?.available == false ->
			loadPreview.unavailableTooltipMessage()

		else ->
			stringResource(Res.string.create_term_load_tooltip_unavailable)
	}

	if (loadPreview?.available != true) return baseMessage

	val effectiveTerms = loadPreview.effectiveTerms?.takeIf { terms -> terms > 0 } ?: return baseMessage
	val termsMessage = if (effectiveTerms == 1) {
		stringResource(Res.string.create_term_load_tooltip_terms_singular)
	} else {
		stringResource(Res.string.create_term_load_tooltip_terms_plural, effectiveTerms)
	}

	return "$baseMessage $termsMessage"
}

@Composable
private fun SyntheticTermLoadPreview.availableTooltipMessage(): String {
	return when (detail) {
		SyntheticTermLoadDetail.PERSONAL_HISTORY_STRONG ->
			stringResource(Res.string.create_term_load_tooltip_personal_high)

		SyntheticTermLoadDetail.PERSONAL_HISTORY_LIMITED ->
			personalLimitedTooltipMessage()

		SyntheticTermLoadDetail.CAREER_REFERENCE ->
			stringResource(Res.string.create_term_load_tooltip_career)

		SyntheticTermLoadDetail.GLOBAL_REFERENCE ->
			stringResource(Res.string.create_term_load_tooltip_global)

		else -> when (basis) {
			SyntheticTermLoadBasis.PERSONAL ->
				if (confidence == SyntheticTermLoadConfidence.HIGH) {
					stringResource(Res.string.create_term_load_tooltip_personal_high)
				} else {
					personalLimitedTooltipMessage()
				}

			SyntheticTermLoadBasis.CAREER ->
				stringResource(Res.string.create_term_load_tooltip_career)

			SyntheticTermLoadBasis.GLOBAL ->
				stringResource(Res.string.create_term_load_tooltip_global)

			null ->
				stringResource(Res.string.create_term_load_tooltip_available)
		}
	}
}

@Composable
private fun SyntheticTermLoadPreview.personalLimitedTooltipMessage(): String {
	return if (confidence == SyntheticTermLoadConfidence.LOW) {
		stringResource(Res.string.create_term_load_tooltip_personal_low)
	} else {
		stringResource(Res.string.create_term_load_tooltip_personal_medium)
	}
}

@Composable
private fun SyntheticTermLoadPreview.unavailableTooltipMessage(): String {
	return when (detail ?: reason.toSyntheticTermLoadDetailOrNull()) {
		SyntheticTermLoadDetail.UNKNOWN_SUBJECT ->
			stringResource(Res.string.create_term_load_tooltip_unknown_subject)

		SyntheticTermLoadDetail.MISSING_SUBJECT_DIFFICULTY ->
			stringResource(Res.string.create_term_load_tooltip_missing_difficulty)

		SyntheticTermLoadDetail.INSUFFICIENT_REFERENCE_DATA ->
			stringResource(Res.string.create_term_load_tooltip_insufficient_reference)

		else ->
			stringResource(Res.string.create_term_load_tooltip_unavailable)
	}
}

private fun String?.toSyntheticTermLoadDetailOrNull(): SyntheticTermLoadDetail? {
	return this?.let { value -> runCatching { SyntheticTermLoadDetail.valueOf(value) }.getOrNull() }
}

private sealed class LoadChipStatus {
	data object Placeholder : LoadChipStatus()
	data object Loading : LoadChipStatus()
	data object Unavailable : LoadChipStatus()
	data class Available(val band: SyntheticTermLoadBand) : LoadChipStatus()
}

private fun LoadChipStatus.label(
	placeholderText: String,
	loadingText: String,
	unavailableText: String
): String {
	return when (this) {
		LoadChipStatus.Placeholder ->
			placeholderText
		LoadChipStatus.Loading ->
			loadingText
		LoadChipStatus.Unavailable ->
			unavailableText
		is LoadChipStatus.Available ->
			band.label
	}
}

private fun LoadChipStatus.color(
	outlineColor: Color,
	primaryColor: Color,
	onSurfaceVariantColor: Color,
	errorColor: Color
): Color {
	return when (this) {
		LoadChipStatus.Placeholder ->
			outlineColor

		LoadChipStatus.Loading ->
			primaryColor

		LoadChipStatus.Unavailable ->
			onSurfaceVariantColor

		is LoadChipStatus.Available ->
			when (band) {
				SyntheticTermLoadBand.LIGHT ->
					CreateTermLightLoadColor

				SyntheticTermLoadBand.MANAGEABLE ->
					CreateTermManageableLoadColor

				SyntheticTermLoadBand.NORMAL ->
					CreateTermSuccessColor

				SyntheticTermLoadBand.DEMANDING ->
					CreateTermWarningColor

				SyntheticTermLoadBand.VERY_DEMANDING ->
					errorColor
			}
	}
}
