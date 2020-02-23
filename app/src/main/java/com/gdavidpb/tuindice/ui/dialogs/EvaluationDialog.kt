package com.gdavidpb.tuindice.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.EvaluationType
import com.gdavidpb.tuindice.presentation.model.EvaluationRequest
import com.gdavidpb.tuindice.presentation.model.NewEvaluation
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.dialog_evaluation.*
import org.koin.android.ext.android.inject
import java.util.*

open class EvaluationDialog : RequestDialog<EvaluationRequest, NewEvaluation>() {

    private val inputMethodManager by inject<InputMethodManager>()

    private val datePicker by lazy {
        (tViewDate as TextView).wrapEvaluationDatePicker()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_evaluation, container, false)
    }

    override fun onDialogCreated(arguments: EvaluationRequest?) {
        arguments ?: return

        initChipGroup()
        initEvaluation(arguments)
        initListeners()
    }

    private fun updateGradeValue(value: Int) {
        tViewLabelGradeValue.text = getString(R.string.label_evaluation_grade_value, value)
    }

    private fun initEvaluation(request: EvaluationRequest) {
        val (subject, evaluation) = request

        val headerText = getString(R.string.label_evaluation_subject_header, subject.code, subject.name)

        if (evaluation != null) {
            val grade = evaluation.grade
            val notes = evaluation.notes
            val date = evaluation.date
            val evaluationType = evaluation.type.ordinal

            sBarMaxGrade.progress = grade
            sEvaluationDate.isChecked = date.time != 0L
            cGroupEvaluation.checkedChipIndex = evaluationType

            tViewEvaluationHeader.text = getString(R.string.title_edit_evaluation)
            btnEvaluationDone.text = getString(R.string.edit)

            datePicker.selectedDate = date
            datePicker.isDateSelectable = sEvaluationDate.isChecked

            eTextNotes.setText(notes)
            eTextNotes.setSelection(notes.length)
            eTextNotes.isVisible = notes.isNotEmpty()

            checkNotes()

            updateGradeValue(value = grade)
        } else {
            tViewEvaluationHeader.text = getString(R.string.title_add_evaluation)
            btnEvaluationDone.text = getString(R.string.add)

            updateGradeValue(value = 0)
        }

        tViewSubjectHeader.text = headerText

        eTextNotes.showSoftInputOnFocus = true
    }

    private fun initChipGroup() {
        EvaluationType.values().forEach { evaluationType ->
            LayoutInflater
                    .from(context)
                    .inflate(R.layout.view_evaluation_chip, cGroupEvaluation, false).also { chip ->
                        chip as Chip

                        chip.text = getString(evaluationType.stringRes)
                    }.also(cGroupEvaluation::addView)
        }
    }

    private fun initListeners() {
        tViewLabelNotes.onClickOnce(::onNotesClicked)
        btnEvaluationDone.onClickOnce(::onAddClicked)
        btnEvaluationCancel.onClickOnce(::onCancelClicked)
        tViewDate.onClickOnce(::onDateClicked)

        cGroupEvaluation.setOnCheckedChangeListener { _, _ ->
            validateParams()
        }

        sEvaluationDate.onCheckedChange { isChecked ->
            datePicker.isDateSelectable = isChecked

            validateParams()
        }

        sBarMaxGrade.onSeekBarChange {
            onProgressChanged { progress, fromUser ->
                if (fromUser) {
                    updateGradeValue(value = progress)

                    validateParams()
                }
            }
        }

        eTextNotes.onTextChanged { _, _, _, _ ->
            validateParams()
        }
    }

    private fun validateParams() {
        when {
            cGroupEvaluation.checkedChipId == -1 -> false
            sBarMaxGrade.progress == 0 -> false
            !datePicker.isValidState -> false
            else -> true
        }.let { isOk ->
            btnEvaluationDone.isEnabled = isOk
        }
    }

    private fun checkNotes() {
        if (eTextNotes.isVisible) {
            inputMethodManager.showSoftKeyboard(eTextNotes)

            tViewLabelNotes.drawables(end = R.drawable.ic_expand_less)
        } else {
            inputMethodManager.hideSoftKeyboard(eTextNotes)

            tViewLabelNotes.drawables(end = R.drawable.ic_expand_more)
        }
    }

    private fun onAddClicked() {
        val (subject, evaluation) = getRequest() ?: return

        val evaluationType = cGroupEvaluation
                .checkedChipIndex
                .let { index ->
                    EvaluationType.values()[index]
                }

        val maxGrade = sBarMaxGrade.progress

        val evaluationId = evaluation?.id ?: ""

        val newEvaluation = NewEvaluation(
                id = evaluationId,
                sid = subject.id,
                subjectCode = subject.code,
                type = evaluationType,
                maxGrade = maxGrade,
                date = datePicker.selectedDate,
                notes = "${eTextNotes.text}"
        )

        callback(newEvaluation)

        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    private fun onDateClicked() {
        requireActivity().datePicker {
            if (datePicker.selectedDate.time != 0L)
                selectedDate = datePicker.selectedDate

            onDateSelected { selectedDate ->
                datePicker.selectedDate = selectedDate

                validateParams()
            }

            setUpDatePicker {
                val startDate = Date().add(Calendar.YEAR, -1).time
                val endDate = Date().add(Calendar.YEAR, 1).time

                minDate = startDate
                maxDate = endDate
            }
        }
    }

    private fun onNotesClicked() {
        eTextNotes.isVisible = !eTextNotes.isVisible

        checkNotes()
    }
}