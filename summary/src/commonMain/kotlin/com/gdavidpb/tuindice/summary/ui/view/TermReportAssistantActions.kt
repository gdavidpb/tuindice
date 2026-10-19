package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.icon.assistantSparkleTint
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_ask

// The question about the report, across the card when the assistant can be asked, and under it the student's thumbs
// with the words that ask for them. The thumbs are always offered, whether or not the assistant can be asked.
@Composable
fun TermReportAssistantActions(
	feedback: Boolean?,
	onAskAssistantClick: (() -> Unit)?,
	onRateClick: (useful: Boolean) -> Unit
) {
	// No spacing of its own: the touch area of the thumbs already leaves their margin above the icons.
	Column {
		onAskAssistantClick?.let { onAsk ->
			OutlinedButton(
				modifier = Modifier
					.fillMaxWidth()
					.testTag(TermReportUiTags.TermReportAskButton),
				onClick = onAsk
			) {
				Icon(
					modifier = Modifier.size(ASK_ICON_SIZE),
					imageVector = rememberAssistantSparkleIcon(),
					contentDescription = null,
					tint = assistantSparkleTint()
				)

				Spacer(modifier = Modifier.size(TuIndiceSpacing.XSmall))

				Text(text = stringResource(Res.string.term_report_ask))
			}
		}

		TermReportFeedbackRow(feedback = feedback, onRateClick = onRateClick)
	}
}

private val ASK_ICON_SIZE = 18.dp
