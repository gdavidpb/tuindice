package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBasis
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadConfidence
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadDetail
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_load_info_content_description
import tuindice.record.generated.resources.create_term_load_label
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
import tuindice.record.generated.resources.create_term_load_tooltip_unavailable
import tuindice.record.generated.resources.create_term_load_tooltip_unknown_subject
import tuindice.record.generated.resources.create_term_period_label

private val TermDropdownMaxHeight = 280.dp
private val TermControlHeight = 48.dp
private val LoadControlWidth = 148.dp

@Composable
fun CreateTermPeriodRow(
	selectedPeriod: SyntheticTermPeriodOption,
	periodOptions: List<SyntheticTermPeriodOption>,
	loadPreview: SyntheticTermLoadPreview?,
	hasSelectedSubjects: Boolean,
	isLoadingLoadPreview: Boolean,
	hasLoadPreviewError: Boolean,
	onPeriodSelected: (String) -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.Top
	) {
		val expanded = remember { mutableStateOf(false) }
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(6.dp)
		) {
			CreateTermControlLabel(text = stringResource(Res.string.create_term_period_label))
			Box {
				OutlinedButton(
					modifier = Modifier
						.fillMaxWidth()
						.height(TermControlHeight)
						.testTag(RecordUiTags.CreateSyntheticTermPeriodSelector),
					onClick = { expanded.value = true },
					shape = RoundedCornerShape(14.dp),
					contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
				) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							modifier = Modifier.padding(end = 8.dp),
							imageVector = Icons.Outlined.CalendarToday,
							contentDescription = null
						)
						Text(
							modifier = Modifier.weight(1f),
							text = selectedPeriod.label,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
						Icon(
							imageVector = Icons.Outlined.ArrowDropDown,
							contentDescription = null
						)
					}
				}

				DropdownMenu(
					modifier = Modifier.heightIn(max = TermDropdownMaxHeight),
					expanded = expanded.value,
					onDismissRequest = { expanded.value = false }
				) {
					periodOptions.forEach { option ->
						DropdownMenuItem(
							modifier = Modifier.testTag(RecordUiTags.createSyntheticTermPeriodOption(option.termKey)),
							text = { Text(text = option.label) },
							onClick = {
								expanded.value = false
								onPeriodSelected(option.termKey)
							}
						)
					}
				}
			}
		}

		Column(
			modifier = Modifier.width(LoadControlWidth),
			verticalArrangement = Arrangement.spacedBy(6.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(modifier = Modifier.weight(1f)) {
					CreateTermControlLabel(text = stringResource(Res.string.create_term_load_label))
				}
				CreateTermLoadInfoButton(
					loadPreview = loadPreview,
					hasSelectedSubjects = hasSelectedSubjects,
					isLoadingLoadPreview = isLoadingLoadPreview,
					hasLoadPreviewError = hasLoadPreviewError
				)
			}
			CreateTermLoadChip(
				modifier = Modifier.height(TermControlHeight),
				loadPreview = loadPreview,
				hasSelectedSubjects = hasSelectedSubjects,
				isLoading = isLoadingLoadPreview,
				hasError = hasLoadPreviewError
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTermLoadInfoButton(
	loadPreview: SyntheticTermLoadPreview?,
	hasSelectedSubjects: Boolean,
	isLoadingLoadPreview: Boolean,
	hasLoadPreviewError: Boolean
) {
	val tooltipState = rememberTooltipState(isPersistent = true)
	val tooltipScope = rememberCoroutineScope()
	val message = createTermLoadTooltipMessage(
		loadPreview = loadPreview,
		hasSelectedSubjects = hasSelectedSubjects,
		isLoadingLoadPreview = isLoadingLoadPreview,
		hasLoadPreviewError = hasLoadPreviewError
	)

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
				.size(32.dp)
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
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
private fun createTermLoadTooltipMessage(
	loadPreview: SyntheticTermLoadPreview?,
	hasSelectedSubjects: Boolean,
	isLoadingLoadPreview: Boolean,
	hasLoadPreviewError: Boolean
): String {
	val baseMessage = when {
		isLoadingLoadPreview ->
			stringResource(Res.string.create_term_load_tooltip_loading)

		!hasSelectedSubjects ->
			stringResource(Res.string.create_term_load_tooltip_placeholder)

		hasLoadPreviewError ->
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
				stringResource(Res.string.create_term_load_tooltip_unavailable)
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
	return when (this) {
		"INSUFFICIENT_PERSONAL_HISTORY" -> SyntheticTermLoadDetail.INSUFFICIENT_REFERENCE_DATA
		null -> null
		else -> runCatching { SyntheticTermLoadDetail.valueOf(this) }.getOrNull()
	}
}
