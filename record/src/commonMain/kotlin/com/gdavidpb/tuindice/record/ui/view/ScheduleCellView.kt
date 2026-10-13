package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
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
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_evaluation_click_label

// The space the evaluation icon leaves to the cell's edge: the cell's own vertical padding.
private val EvaluationIconInset = TuIndiceSpacing.XSmall

// A cell of a single block is 32dp tall and 70dp wide, and a real subject code takes up to 53dp of
// them: its icon is the compact one and sits closer to the corner so the code keeps its place.
private val CompactEvaluationIconInset = TuIndiceSpacing.Two

/**
 * One meeting of a subject, in the colours of the subject's chip. A meeting that overlaps another
 * subject's that day gains the alert border, the same rule the table follows. One an evaluation of
 * this week falls on carries the evaluation icon in its bottom corner, whatever else it is, and is a
 * button that asks for that week of the Evaluations tab ([onEvaluationClick]), like the table's
 * cell; the others are not.
 */
@Composable
fun ScheduleCellView(
	modifier: Modifier = Modifier,
	day: ScheduleDay,
	cell: ScheduleCellItem,
	onEvaluationClick: (weekStart: LocalDate) -> Unit
) {
	val warning = AcademicStatusColors.warning()
	val description = cell.description.asString()
	val shape = RoundedCornerShape(TuIndiceRadius.Small)
	val cellModifier = modifier
		.testTag(RecordUiTags.scheduleCell(cell.attemptId, day.code, cell.startBlock))
		.semantics(mergeDescendants = true) { contentDescription = description }
		.evaluationButton(shape, cell.evaluationWeekStart, onEvaluationClick)

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
				val inset = if (isSingleBlock) CompactEvaluationIconInset else EvaluationIconInset

				ScheduleEvaluationMarkIcon(
					size = if (isSingleBlock) TuIndiceComponentSizes.IconXSmall else TuIndiceComponentSizes.IconSmall,
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.offset(x = -inset, y = -inset)
						.testTag(RecordUiTags.scheduleCellEvaluation(cell.attemptId, day.code, cell.startBlock))
				)
			}
		}
	}
}

// The cell as a button for the week of its evaluation, or as it is when it has none. Clipped to the
// cell's shape so the ripple does not leave it; the cell says what it is.
@Composable
private fun Modifier.evaluationButton(
	shape: Shape,
	weekStart: LocalDate?,
	onClick: (weekStart: LocalDate) -> Unit
): Modifier {
	val label = stringResource(Res.string.schedule_evaluation_click_label)

	if (weekStart == null) return this

	return clip(shape).clickable(onClickLabel = label, role = Role.Button) { onClick(weekStart) }
}
