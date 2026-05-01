package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationsView(
	lazyListState: LazyListState,
	evaluations: List<EvaluationsGroupItem>,
	onEvaluationClick: (evaluationId: String, evaluationName: String) -> Unit,
	onEvaluationEdit: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit,
	scrollEnabled: Boolean = true,
	openActionsEvaluationId: String? = null,
	focusEvaluationId: String? = null,
	onFocusEvaluationBoundsChange: (Rect?) -> Unit = {}
) {
	LazyColumn(
		modifier = Modifier.testTag(EvaluationsUiTags.EvaluationsList),
		state = lazyListState,
		userScrollEnabled = scrollEnabled
	) {
		evaluations.forEach { (title, items) ->
			stickyHeader {
				EvaluationHeaderView(label = title)
			}

			items(
				items = items,
				key = { evaluation -> evaluation.evaluationId }
			) { evaluation ->
				EvaluationSwipeToDismiss(
					modifier = if (evaluation.evaluationId == focusEvaluationId) {
						Modifier.onGloballyPositioned { coordinates ->
							onFocusEvaluationBoundsChange(coordinates.boundsInRoot())
						}
					} else {
						Modifier
					},
					initiallyOpen = evaluation.evaluationId == openActionsEvaluationId,
					onEdit = { onEvaluationEdit(evaluation.evaluationId) },
					onDelete = { onEvaluationDelete(evaluation.evaluationId) }
				) { onActionsClick ->
					EvaluationItemView(
						modifier = Modifier.animateItem(),
						item = evaluation,
						onGradeClick = {
							if (evaluation.isClickable) {
								onEvaluationClick(evaluation.evaluationId, evaluation.nameText)
							}
						},
						onCardClick = onActionsClick
					)
				}
			}
		}
	}
}
