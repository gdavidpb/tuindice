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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_load_loading
import tuindice.record.generated.resources.create_term_load_placeholder
import tuindice.record.generated.resources.create_term_load_unavailable

private val LoadChipWidth = 148.dp
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
				.padding(horizontal = 12.dp),
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
		}
	}
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
