package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.icon.ShareIcon
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_share_action

/**
 * The button that shares a finished term: a sibling of the body of the term, never inside it, with the share icon of
 * the platform and the sheet's own words for sharing. A tap asks for the share sheet through [onClick].
 */
@Composable
fun SummaryFinishedTermShareButton(onClick: () -> Unit) {
	OutlinedButton(
		modifier = Modifier
			.padding(horizontal = TuIndiceSpacing.Screen)
			.padding(bottom = TuIndiceSpacing.Screen)
			.testTag(SummaryUiTags.TermFinishedShare),
		onClick = onClick
	) {
		Icon(
			modifier = Modifier.size(TuIndiceComponentSizes.IconSmall),
			imageVector = ShareIcon,
			contentDescription = null
		)

		Text(
			modifier = Modifier.padding(start = TuIndiceSpacing.Medium),
			text = stringResource(Res.string.share_term_share_action)
		)
	}
}
