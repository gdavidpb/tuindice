package com.gdavidpb.tuindice.assistant.ui.component.materias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.RecommendedElectivesComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_electives_label

// materias.recomendadas/1: no figure. A row per recommended elective, its code and name, and under it the short reason
// the service gives for recommending it.
@Composable
fun RecommendedElectivesComponentView(
	component: RecommendedElectivesComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Subjects.electives(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_electives_label))
		component.items.forEachIndexed { index, elective ->
			Column(verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small)) {
				CardRowView(code = elective.code, name = elective.name, showsDivider = index > 0)
				Text(
					text = elective.reason,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
