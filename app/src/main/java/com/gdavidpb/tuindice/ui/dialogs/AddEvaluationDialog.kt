package com.gdavidpb.tuindice.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.EvaluationType
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.presentation.model.Evaluation
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toEvaluationDate
import kotlinx.android.synthetic.main.dialog_add_evaluation.*
import java.util.*

open class AddEvaluationDialog(
        val subject: Subject,
        val callback: (Evaluation) -> Unit
) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_add_evaluation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val headerText = getString(R.string.label_add_evaluation_subject_header, subject.code, subject.name)

        cGroupEvaluation.setOnCheckedChangeListener { _, _ ->
            validateParams()
        }

        tViewSubjectHeader.text = headerText

        updateGradeValue(grade = 0)

        tViewLabelNotes.onClickOnce(::onNotesClicked)
        btnEvaluationAdd.onClickOnce(::onAddClicked)
        btnEvaluationCancel.onClickOnce(::onCancelClicked)
        tViewDate.onClickOnce(::onDateClicked)

        sBarMaxGrade.onSeekBarChange { progress, fromUser ->
            if (fromUser) {
                updateGradeValue(grade = progress)

                validateParams()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireActivity(), R.style.AppTheme_FullScreenDialog)
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, AddEvaluationDialog::class.java.name)
    }

    private fun updateGradeValue(grade: Int) {
        tViewLabelGradeValue.text = getString(R.string.label_add_evaluation_grade_value, grade)
    }

    private fun validateParams() {
        when {
            cGroupEvaluation.checkedChipId == -1 -> false
            sBarMaxGrade.progress == 0 -> false
            tViewDate.tag == null -> false
            else -> true
        }.let { isOk ->
            btnEvaluationAdd.isEnabled = isOk
        }
    }

    private fun onAddClicked() {
        val evaluationType = EvaluationType.values()[cGroupEvaluation.checkedChipId - 1]

        val evaluation = Evaluation(
                type = evaluationType,
                maxGrade = sBarMaxGrade.progress,
                date = tViewDate.tag as Date,
                notes = "${eTextNotes.text}"
        )

        callback(evaluation)

        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    private fun onDateClicked() {
        requireActivity().datePicker {
            (tViewDate.tag as? Date)?.also(::selectDate)

            onDateSelected { selectedDate ->
                tViewDate.text = selectedDate.toEvaluationDate()

                tViewDate.tag = selectedDate

                validateParams()
            }

            setUpPicker {
                val startDate = Date().add(Calendar.YEAR, -1).time
                val endDate = Date().add(Calendar.YEAR, 1).time

                minDate = startDate
                maxDate = endDate
            }
        }
    }

    private fun onNotesClicked() {
        eTextNotes.isVisible = !eTextNotes.isVisible

        tViewLabelNotes.drawables(right = if (eTextNotes.isVisible) R.drawable.ic_expand_less else R.drawable.ic_expand_more)
    }
}