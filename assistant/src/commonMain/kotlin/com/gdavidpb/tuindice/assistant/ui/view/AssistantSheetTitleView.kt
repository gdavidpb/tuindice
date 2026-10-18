package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.icon.assistantSparkleTint
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The title of the assistant's sheets (the chat and its consent): the sparkle in the accent, on the same line and at
// the start, then the text with the style of the app's sheets (ConfirmationDialog). The row reads as its text alone.
@Composable
fun AssistantSheetTitleView(
	text: String,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier.semantics(mergeDescendants = true) {},
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Large),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = rememberAssistantSparkleIcon(),
			contentDescription = null,
			modifier = Modifier.size(TuIndiceComponentSizes.Icon).testTag(AssistantUiTags.TitleSparkle),
			tint = assistantSparkleTint()
		)
		Text(
			text = text,
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onSurface
		)
	}
}
