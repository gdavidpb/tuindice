package com.gdavidpb.tuindice.ui.dialogs

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.EvaluationType
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.presentation.model.NewEvaluation
import com.gdavidpb.tuindice.ui.customs.EvaluationDatePicker
import com.gdavidpb.tuindice.utils.DECIMALS_DIV
import com.gdavidpb.tuindice.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.dialog_evaluation_content.view.*
import kotlinx.android.synthetic.main.dialog_evaluation_header.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.math.max
import kotlin.math.min

class EvaluationDialogBuilder(context: Context) : AlertDialog.Builder(context), KoinComponent {

    private var subject: Subject? = null
    private var evaluation: Evaluation? = null
    private var callback: ((NewEvaluation) -> Unit) = { _ -> }

    private lateinit var positiveButton: Button
    private lateinit var datePicker: EvaluationDatePicker

    private val inputMethodManager by inject<InputMethodManager>()

    override fun create(): AlertDialog {
        val subject = subject ?: throw IllegalArgumentException("subject")

        val headerView = View.inflate(context, R.layout.dialog_evaluation_header, null)
        val contentView = View.inflate(context, R.layout.dialog_evaluation_content, null)

        with(headerView) {
            initHeaderView(subject, evaluation)
        }

        with(contentView) {
            initChipGroup()
            initContentView(evaluation)
            initListeners()
        }

        setCustomTitle(headerView)
        setView(contentView)

        setNegativeButton(R.string.cancel) { _, _ -> }

        return super.create().also { dialog ->
            dialog.setOnShowListener {
                positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                positiveButton.isEnabled = false
            }
        }
    }

    fun setSubject(value: Subject?) = apply {
        subject = value
    }

    fun setEvaluation(value: Evaluation?) = apply {
        evaluation = value
    }

    fun onDone(value: (NewEvaluation) -> Unit) = apply {
        callback = value
    }

    private fun View.initChipGroup() {
        EvaluationType.values().forEach { evaluationType ->
            View.inflate(context, R.layout.view_evaluation_chip, null).also { chip ->
                chip as Chip

                chip.text = context.getString(evaluationType.stringRes)
            }.also(cGroupEvaluation::addView)
        }
    }

    private fun View.initHeaderView(subject: Subject, evaluation: Evaluation?) {
        val evaluationHeader =
                if (evaluation != null) R.string.title_edit_evaluation else R.string.title_add_evaluation

        val subjectHeader =
                context.getString(R.string.label_evaluation_subject_header, subject.code, subject.name)

        tViewSubjectHeader.text = subjectHeader
        tViewEvaluationHeader.text = context.getString(evaluationHeader)
    }

    private fun View.initContentView(evaluation: Evaluation?) {
        datePicker = (tViewDate as TextView).wrapEvaluationDatePicker()

        sBarMaxGrade.max = MAX_EVALUATION_GRADE.toProgress()

        if (evaluation != null) {
            val grade = evaluation.grade
            val notes = evaluation.notes
            val date = evaluation.date
            val evaluationType = evaluation.type.ordinal

            sBarMaxGrade.progress = grade.toProgress()
            sEvaluationDate.isChecked = date.time != 0L
            cGroupEvaluation.checkedChipIndex = evaluationType

            datePicker.selectedDate = date
            datePicker.isDateSelectable = sEvaluationDate.isChecked

            eTextNotes.setText(notes)
            eTextNotes.setSelection(notes.length)
            eTextNotes.isVisible = notes.isNotEmpty()

            checkNotes()

            updateGradeValue(value = grade)

            setPositiveButton(R.string.edit) { _, _ -> collect() }
        } else {
            updateGradeValue(value = 0.0)

            setPositiveButton(R.string.add) { _, _ -> collect() }
        }
    }

    private fun View.initListeners() {
        tViewLabelNotes.onClickOnce { onNotesClicked() }
        tViewDate.onClickOnce { onDateClicked() }

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
                    updateGradeValue(value = progress.toGrade())

                    validateParams()
                }
            }
        }

        arrayOf(btnGradeUp, btnGradeDown).forEach { button ->
            button.setOnClickListener {
                val step = when (button) {
                    btnGradeUp -> DECIMALS_DIV
                    btnGradeDown -> -DECIMALS_DIV
                    else -> 0.0
                }

                val oldGrade = sBarMaxGrade.progress.toGrade()
                val newGrade = max(min(oldGrade + step, MAX_EVALUATION_GRADE), 0.0)

                sBarMaxGrade.progress = newGrade.toProgress()

                updateGradeValue(newGrade)

                validateParams()
            }
        }

        eTextNotes.onTextChanged { _, _, _, _ ->
            validateParams()
        }
    }

    private fun View.validateParams() {
        when {
            cGroupEvaluation.checkedChipId == -1 -> false
            sBarMaxGrade.progress == 0 -> false
            !datePicker.isValidState -> false
            else -> true
        }.let { isOk ->
            positiveButton.isEnabled = isOk
        }
    }

    private fun View.checkNotes() {
        if (eTextNotes.isVisible) {
            inputMethodManager.showSoftKeyboard(eTextNotes)

            tViewLabelNotes.drawables(end = R.drawable.ic_expand_less)
        } else {
            inputMethodManager.hideSoftKeyboard(eTextNotes)

            tViewLabelNotes.drawables(end = R.drawable.ic_expand_more)
        }
    }

    private fun View.updateGradeValue(value: Double) {
        tViewLabelGradeValue.text = context.getString(R.string.label_evaluation_grade_value, value)
    }

    private fun View.collect() {
        val subject = subject ?: throw IllegalArgumentException("subject")

        val evaluationType = cGroupEvaluation
                .checkedChipIndex
                .let { index ->
                    EvaluationType.values()[index]
                }

        val maxGrade = sBarMaxGrade.progress.toGrade()

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
    }

    private fun View.onDateClicked() {
        context.datePicker {
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

    private fun View.onNotesClicked() {
        eTextNotes.isVisible = !eTextNotes.isVisible

        checkNotes()
    }
}