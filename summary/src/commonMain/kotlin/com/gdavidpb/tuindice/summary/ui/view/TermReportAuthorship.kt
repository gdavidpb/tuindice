package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.icon.assistantSparkleTint
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_assistant_ai

// The mark of the text: the sparkle and the words that say the model wrote it from the notes.
@Composable
fun TermReportAuthorship() {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		Icon(
			modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
			imageVector = rememberAssistantSparkleIcon(),
			contentDescription = null,
			tint = assistantSparkleTint()
		)

		Text(
			text = stringResource(Res.string.term_report_assistant_ai),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.primary
		)
	}
}
