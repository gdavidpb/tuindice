package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.SimulationItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_simulation_label

// informe.simulacion/1: the index of the simulated term, with the simulated cumulative index beside it, then one
// row per simulated grade with its result and its grade.
@Composable
fun SimulationComponentView(
	item: SimulationItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Report.simulation(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_simulation_label))
		item.primaryFigure?.let { primary ->
			CardFiguresView(primary = primary.toCardFigure(), secondary = item.secondaryFigure?.toCardFigure())
		}
		item.changes.forEachIndexed { index, change ->
			CardRowView(
				code = change.code,
				name = change.outcomeText.asString(),
				showsDivider = index > 0,
				trailing = { change.gradeText?.let { grade -> CardValueView(text = grade) } }
			)
		}
	}
}
