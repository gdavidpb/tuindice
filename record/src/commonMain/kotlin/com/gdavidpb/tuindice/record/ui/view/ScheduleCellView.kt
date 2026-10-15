package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleClashDefaults

// The space the evaluation icon leaves to the cell's edge: the cell's own vertical padding.
private val EvaluationIconInset = TuIndiceSpacing.XSmall

// A cell of a single block is 32dp tall and 70dp wide, and a real subject code takes up to 53dp of
// them: its icon is the compact one and sits closer to the bottom edge so the code keeps its place;
// from the end edge it keeps the regular inset, which is where the eye expects the mark.
private val CompactEvaluationIconBottomInset = TuIndiceSpacing.Two
private val CompactEvaluationIconEndInset = TuIndiceSpacing.XSmall

/**
 * One meeting of a subject, in the colours of the subject's chip. A meeting that overlaps another
 * subject's that day gains the alert border, the same rule the table follows. One an evaluation of
 * this week falls on carries the evaluation icon in its bottom corner, whatever else it is, and is a
 * button that asks the Evaluations tab for that evaluation ([onEvaluationClick]), like the table's
 * cell; the others are not.
 */
@Composable
fun ScheduleCellView(
	modifier: Modifier = Modifier,
	day: ScheduleDay,
	cell: ScheduleCellItem,
	onEvaluationClick: (evaluationId: String) -> Unit
) {
	val warning = AcademicStatusColors.warning()
	val description = cell.description.asString()
	val shape = RoundedCornerShape(TuIndiceRadius.Small)
	val cellModifier = modifier
		.testTag(RecordUiTags.scheduleCell(cell.attemptId, day.code, cell.startBlock))
		.semantics(mergeDescendants = true) { contentDescription = description }
		.scheduleEvaluationButton(shape = shape, evaluationId = cell.evaluationId, onClick = onEvaluationClick)

	Surface(
		modifier = cellModifier,
		shape = shape,
		color = cell.codeContainerColor,
		contentColor = cell.codeColor,
		border = if (cell.isClash) {
			BorderStroke(width = ScheduleClashDefaults.BorderWidth, color = warning.copy(alpha = TuIndiceAlpha.BorderStrong))
		} else {
			null
		}
	) {
		Box {
			Column(
				modifier = Modifier.padding(
					horizontal = TuIndiceSpacing.Small,
					vertical = TuIndiceSpacing.XSmall
				)
			) {
				Text(
					text = cell.codeText,
					style = MaterialTheme.typography.labelMedium,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)

				if (cell.classroomText != null) {
					Text(
						text = cell.classroomText,
						style = MaterialTheme.typography.labelSmall,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}
			}

			if (cell.hasEvaluation) {
				// Decorative: the description already says "con evaluación ese día".
				val isSingleBlock = cell.blockSpan == 1
				val endInset = if (isSingleBlock) CompactEvaluationIconEndInset else EvaluationIconInset
				val bottomInset = if (isSingleBlock) CompactEvaluationIconBottomInset else EvaluationIconInset

				ScheduleEvaluationMarkIcon(
					size = if (isSingleBlock) TuIndiceComponentSizes.IconXSmall else TuIndiceComponentSizes.IconSmall,
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.offset(x = -endInset, y = -bottomInset)
						.testTag(RecordUiTags.scheduleCellEvaluation(cell.attemptId, day.code, cell.startBlock))
				)
			}
		}
	}
}
