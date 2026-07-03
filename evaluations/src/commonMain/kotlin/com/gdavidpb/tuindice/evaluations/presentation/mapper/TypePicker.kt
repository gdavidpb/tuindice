package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationTypePickerItem
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluation_attendance
import tuindice.evaluations.generated.resources.evaluation_essay
import tuindice.evaluations.generated.resources.evaluation_interventions
import tuindice.evaluations.generated.resources.evaluation_laboratory
import tuindice.evaluations.generated.resources.evaluation_model
import tuindice.evaluations.generated.resources.evaluation_other
import tuindice.evaluations.generated.resources.evaluation_presentation
import tuindice.evaluations.generated.resources.evaluation_project
import tuindice.evaluations.generated.resources.evaluation_quiz
import tuindice.evaluations.generated.resources.evaluation_report
import tuindice.evaluations.generated.resources.evaluation_test
import tuindice.evaluations.generated.resources.evaluation_workshop
import tuindice.evaluations.generated.resources.evaluation_written_work

@Composable
fun rememberEvaluationTypePickerItemList(
	selectedType: EvaluationType?
): List<EvaluationTypePickerItem> {
	val typeLabels = rememberEvaluationTypeLabels()

	return remember(
		selectedType,
		typeLabels
	) {
		EvaluationType.entries.toEvaluationTypePickerItemList(
			selectedType = selectedType,
			typeLabels = typeLabels
		)
	}
}

@Composable
fun rememberEvaluationTypeLabels(): Map<EvaluationType, String> {
	val testLabel = stringResource(Res.string.evaluation_test)
	val essayLabel = stringResource(Res.string.evaluation_essay)
	val attendanceLabel = stringResource(Res.string.evaluation_attendance)
	val interventionsLabel = stringResource(Res.string.evaluation_interventions)
	val laboratoryLabel = stringResource(Res.string.evaluation_laboratory)
	val modelLabel = stringResource(Res.string.evaluation_model)
	val presentationLabel = stringResource(Res.string.evaluation_presentation)
	val projectLabel = stringResource(Res.string.evaluation_project)
	val quizLabel = stringResource(Res.string.evaluation_quiz)
	val reportLabel = stringResource(Res.string.evaluation_report)
	val workshopLabel = stringResource(Res.string.evaluation_workshop)
	val writtenWorkLabel = stringResource(Res.string.evaluation_written_work)
	val otherLabel = stringResource(Res.string.evaluation_other)

	return remember(
		testLabel,
		essayLabel,
		attendanceLabel,
		interventionsLabel,
		laboratoryLabel,
		modelLabel,
		presentationLabel,
		projectLabel,
		quizLabel,
		reportLabel,
		workshopLabel,
		writtenWorkLabel,
		otherLabel
	) {
		buildEvaluationTypeLabels(
			testLabel = testLabel,
			essayLabel = essayLabel,
			attendanceLabel = attendanceLabel,
			interventionsLabel = interventionsLabel,
			laboratoryLabel = laboratoryLabel,
			modelLabel = modelLabel,
			presentationLabel = presentationLabel,
			projectLabel = projectLabel,
			quizLabel = quizLabel,
			reportLabel = reportLabel,
			workshopLabel = workshopLabel,
			writtenWorkLabel = writtenWorkLabel,
			otherLabel = otherLabel
		)
	}
}

suspend fun getEvaluationTypeLabels(): Map<EvaluationType, String> {
	return buildEvaluationTypeLabels(
		testLabel = getString(Res.string.evaluation_test),
		essayLabel = getString(Res.string.evaluation_essay),
		attendanceLabel = getString(Res.string.evaluation_attendance),
		interventionsLabel = getString(Res.string.evaluation_interventions),
		laboratoryLabel = getString(Res.string.evaluation_laboratory),
		modelLabel = getString(Res.string.evaluation_model),
		presentationLabel = getString(Res.string.evaluation_presentation),
		projectLabel = getString(Res.string.evaluation_project),
		quizLabel = getString(Res.string.evaluation_quiz),
		reportLabel = getString(Res.string.evaluation_report),
		workshopLabel = getString(Res.string.evaluation_workshop),
		writtenWorkLabel = getString(Res.string.evaluation_written_work),
		otherLabel = getString(Res.string.evaluation_other)
	)
}

suspend fun getEvaluationTypePickerItemList(
	selectedType: EvaluationType?
): List<EvaluationTypePickerItem> {
	return EvaluationType.entries.toEvaluationTypePickerItemList(
		selectedType = selectedType,
		typeLabels = getEvaluationTypeLabels()
	)
}

fun List<EvaluationType>.toEvaluationTypePickerItemList(
	selectedType: EvaluationType?,
	typeLabels: Map<EvaluationType, String>
): List<EvaluationTypePickerItem> {
	return map { type ->
		EvaluationTypePickerItem(
			type = type,
			labelText = typeLabels.getValue(type),
			icon = type.asIcon(),
			isSelected = (type == selectedType),
			isVisible = (selectedType == null) || (type == selectedType)
		)
	}
}

fun List<EvaluationTypePickerItem>.withSelectedType(
	selectedType: EvaluationType?
): List<EvaluationTypePickerItem> {
	return map { item ->
		item.copy(
			isSelected = (item.type == selectedType),
			isVisible = (selectedType == null) || (item.type == selectedType)
		)
	}
}

private fun buildEvaluationTypeLabels(
	testLabel: String,
	essayLabel: String,
	attendanceLabel: String,
	interventionsLabel: String,
	laboratoryLabel: String,
	modelLabel: String,
	presentationLabel: String,
	projectLabel: String,
	quizLabel: String,
	reportLabel: String,
	workshopLabel: String,
	writtenWorkLabel: String,
	otherLabel: String
): Map<EvaluationType, String> {
	return mapOf(
		EvaluationType.TEST to testLabel,
		EvaluationType.ESSAY to essayLabel,
		EvaluationType.ATTENDANCE to attendanceLabel,
		EvaluationType.INTERVENTIONS to interventionsLabel,
		EvaluationType.LABORATORY to laboratoryLabel,
		EvaluationType.MODEL to modelLabel,
		EvaluationType.PRESENTATION to presentationLabel,
		EvaluationType.PROJECT to projectLabel,
		EvaluationType.QUIZ to quizLabel,
		EvaluationType.REPORT to reportLabel,
		EvaluationType.WORKSHOP to workshopLabel,
		EvaluationType.WRITTEN_WORK to writtenWorkLabel,
		EvaluationType.OTHER to otherLabel
	)
}
