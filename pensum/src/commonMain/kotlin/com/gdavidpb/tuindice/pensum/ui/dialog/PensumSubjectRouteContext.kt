package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumSubjectRelationItem
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_subject_detail_route_after_plural
import tuindice.pensum.generated.resources.pensum_subject_detail_route_after_singular
import tuindice.pensum.generated.resources.pensum_subject_detail_route_before_plural
import tuindice.pensum.generated.resources.pensum_subject_detail_route_before_singular
import tuindice.pensum.generated.resources.pensum_subject_detail_route_context

@Composable
fun PensumSubjectRouteContext(
	node: PensumNodeItem,
	beforeItems: List<PensumSubjectRelationItem>,
	beforeTestTag: String,
	beforeRowTag: (String) -> String,
	afterItems: List<PensumSubjectRelationItem>,
	onRelatedSubjectClick: (nodeId: String) -> Unit
) {
	val hasBeforeItems = beforeItems.isNotEmpty()
	val hasAfterItems = afterItems.isNotEmpty()
	val routeScrollState = rememberScrollState()

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(PensumUiTags.SubjectDetailRouteContext),
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
		Text(
			text = stringResource(Res.string.pensum_subject_detail_route_context),
			style = MaterialTheme.typography.labelLarge,
			fontWeight = FontWeight.Black,
			color = MaterialTheme.colorScheme.onSurface
		)
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.horizontalScroll(routeScrollState),
			horizontalArrangement = Arrangement.spacedBy(0.dp),
			verticalAlignment = Alignment.Top
		) {
			if (hasBeforeItems) {
				PensumSubjectRouteColumn(
					modifier = Modifier.width(SubjectDetailRouteColumnWidth),
					title = stringResource(
						if (beforeItems.size == 1) {
							Res.string.pensum_subject_detail_route_before_singular
						} else {
							Res.string.pensum_subject_detail_route_before_plural
						}
					),
					items = beforeItems,
					testTag = beforeTestTag,
					rowTag = beforeRowTag,
					onRelatedSubjectClick = onRelatedSubjectClick
				)
				PensumRouteConnector(modifier = Modifier.width(SubjectDetailRouteConnectorWidth))
			}
			PensumSelectedSubjectRouteCard(
				modifier = Modifier.width(SubjectDetailRouteSelectedColumnWidth),
				node = node
			)
			if (hasAfterItems) {
				PensumRouteConnector(modifier = Modifier.width(SubjectDetailRouteConnectorWidth))
				PensumSubjectRouteColumn(
					modifier = Modifier.width(SubjectDetailRouteColumnWidth),
					title = stringResource(
						if (afterItems.size == 1) {
							Res.string.pensum_subject_detail_route_after_singular
						} else {
							Res.string.pensum_subject_detail_route_after_plural
						}
					),
					items = afterItems,
					testTag = PensumUiTags.SubjectDetailUnlocks,
					rowTag = { nodeId -> PensumUiTags.subjectDetailUnlock(nodeId) },
					onRelatedSubjectClick = onRelatedSubjectClick
				)
			}
		}
	}
}
