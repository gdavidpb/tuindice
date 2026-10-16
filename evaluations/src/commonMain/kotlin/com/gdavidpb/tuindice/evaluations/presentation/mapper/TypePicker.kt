package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationTypePickerItem
import org.jetbrains.compose.resources.getString
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
