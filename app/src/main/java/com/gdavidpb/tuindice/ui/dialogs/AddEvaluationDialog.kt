package com.gdavidpb.tuindice.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.EvaluationType
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.presentation.model.NewEvaluation
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.formatEvaluationDate
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.dialog_add_evaluation.*
import org.koin.android.ext.android.inject
import java.util.*

open class AddEvaluationDialog(
        val subject: Subject,
        val callback: (NewEvaluation) -> Unit
) : DialogFragment() {

    private val inputMethodManager by inject<InputMethodManager>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_add_evaluation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initData()
        initChipGroup()
        initListeners()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireActivity(), R.style.AppTheme_FullScreenDialog)
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, AddEvaluationDialog::class.java.name)
    }

    private fun updateGradeValue(value: Int) {
        tViewLabelGradeValue.text = getString(R.string.label_add_evaluation_grade_value, value)
    }

    private fun initData() {
        val headerText = getString(R.string.label_add_evaluation_subject_header, subject.code, subject.name)

        tViewSubjectHeader.text = headerText

        updateGradeValue(value = 0)

        eTextNotes.showSoftInputOnFocus = true
    }

    private fun initChipGroup() {
        EvaluationType.values().forEach { evaluationType ->
            LayoutInflater
                    .from(context)
                    .inflate(R.layout.view_chip, cGroupEvaluation, false).also { chip ->
                        chip as Chip

                        chip.text = getString(evaluationType.stringRes)
                    }.also(cGroupEvaluation::addView)
        }
    }

    private fun initListeners() {
        tViewLabelNotes.onClickOnce(::onNotesClicked)
        btnEvaluationAdd.onClickOnce(::onAddClicked)
        btnEvaluationCancel.onClickOnce(::onCancelClicked)
        tViewDate.onClickOnce(::onDateClicked)

        cGroupEvaluation.setOnCheckedChangeListener { _, _ ->
            validateParams()
        }

        sEvaluationDate.onCheckedChange { isChecked ->
            tViewDate.isEnabled = isChecked

            tViewDate.text = when {
                !isChecked -> getString(R.string.label_add_evaluation_no_date)
                isValidDate() -> getDate().formatEvaluationDate()
                else -> getString(R.string.label_add_evaluation_select_date)
            }

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
    }

    private fun validateParams() {
        when {
            cGroupEvaluation.checkedChipId == -1 -> false
            sBarMaxGrade.progress == 0 -> false
            !isValidDate() -> false
            else -> true
        }.let { isOk ->
            btnEvaluationAdd.isEnabled = isOk
        }
    }

    private fun isValidDate() =
            !sEvaluationDate.isChecked || tViewDate.tag != null

    private fun getDate() =
            if (sEvaluationDate.isChecked) tViewDate.tag as? Date ?: Date() else Date(0)

    private fun setDate(value: Date?) {
        tViewDate.tag = value
        tViewDate.text = value?.formatEvaluationDate()
    }

    private fun onAddClicked() {
        val evaluationType = cGroupEvaluation
                .getCheckedChipIndex()
                .let { index ->
                    EvaluationType.values()[index]
                }

        val evaluation = NewEvaluation(
                sid = subject.id,
                type = evaluationType,
                maxGrade = sBarMaxGrade.progress,
                date = getDate(),
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
            selectedDate = getDate()

            onDateSelected { selectedDate ->
                setDate(selectedDate)

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

        if (eTextNotes.isVisible) {
            inputMethodManager.showSoftKeyboard(eTextNotes)

            tViewLabelNotes.drawables(right = R.drawable.ic_expand_less)
        } else {
            inputMethodManager.hideSoftKeyboard(eTextNotes)

            tViewLabelNotes.drawables(right = R.drawable.ic_expand_more)
        }
    }
}