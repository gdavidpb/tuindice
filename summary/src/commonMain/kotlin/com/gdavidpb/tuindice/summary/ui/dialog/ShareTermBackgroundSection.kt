package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.ui.share.ShareTermBackgroundStrip
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_background_label

/**
 * The strip of backgrounds under its label, the one the sheet calls «Fondo», with the one of [loadingId] marked while
 * the card waits for its picture.
 */
@Composable
fun ShareTermBackgroundSection(
	backgrounds: List<ShareTermBackgroundItem>,
	onBackgroundClick: (backgroundId: String) -> Unit,
	loadingId: String? = null
) {
	Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
		Text(
			text = stringResource(Res.string.share_term_background_label),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		ShareTermBackgroundStrip(
			backgrounds = backgrounds,
			onBackgroundClick = onBackgroundClick,
			loadingId = loadingId
		)
	}
}
