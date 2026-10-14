package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleClashDefaults
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_evaluation_click_label

/**
 * One meeting in the schedule table, as the range of blocks it covers. A meeting that overlaps
 * another subject's that day takes the alert tone, the same clash the weekly grid borders. The
 * class being taught right now is filled with the accent, in bold over it. A meeting can be both:
 * the fill says "in progress" and the alert border still says "clash". One an evaluation of this
 * week falls on carries the evaluation icon under its blocks, whatever else it is, and is a button
 * that asks the Evaluations tab for that week and that evaluation ([onEvaluationClick]); the others
 * are not.
 */
@Composable
fun ScheduleTableMeetingView(
	modifier: Modifier = Modifier,
	attemptId: String,
	day: ScheduleDay,
	meeting: ScheduleCellItem,
	onEvaluationClick: (weekStart: LocalDate, evaluationId: String) -> Unit
) {
	val warning = AcademicStatusColors.warning()
	val isClash = meeting.isClash
	val isInProgress = meeting.isInProgress
	val textColor = when {
		isInProgress -> MaterialTheme.colorScheme.onPrimary
		isClash -> warning
		else -> MaterialTheme.colorScheme.onSurface
	}
	val fillColor = when {
		isInProgress -> MaterialTheme.colorScheme.primary
		isClash -> warning.copy(alpha = TuIndiceAlpha.SurfaceTint)
		else -> Color.Transparent
	}
	val shape = RoundedCornerShape(TuIndiceRadius.ExtraSmall)
	val evaluationWeekStart = meeting.evaluationWeekStart
	val evaluationId = meeting.evaluationId
	val evaluationClickLabel = stringResource(Res.string.schedule_evaluation_click_label)
	val description = meeting.description.asString()
	val clickableModifier = if (evaluationWeekStart != null && evaluationId != null) {
		// Clipped to the cell's shape so the ripple does not leave it. Being a button it stands out of
		// the row's merged description, so it says everything about itself (the day, the blocks, the
		// evaluation, the clash, the class in progress) the way the week grid's block does, and
		// carries its own tag; the clash and in-progress ones sit on its content.
		modifier
			.testTag(RecordUiTags.scheduleTableEvaluation(attemptId, day.code))
			.clip(shape)
			.clickable(onClickLabel = evaluationClickLabel, role = Role.Button) {
				onEvaluationClick(evaluationWeekStart, evaluationId)
			}
			.semantics { contentDescription = description }
	} else {
		modifier
	}

	Surface(
		modifier = clickableModifier.fillMaxWidth(),
		shape = shape,
		color = fillColor,
		border = if (isClash) {
			BorderStroke(width = ScheduleClashDefaults.BorderWidth, color = warning.copy(alpha = TuIndiceAlpha.BorderStrong))
		} else {
			null
		}
	) {
		ScheduleTableMeetingBlocksView(
			attemptId = attemptId,
			day = day,
			meeting = meeting,
			textColor = textColor
		)
	}
}
