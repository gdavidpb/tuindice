package com.gdavidpb.tuindice.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.model.database.EvaluationUpdate
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.EvaluationType
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.ui.customs.EvaluationDatePicker
import com.gdavidpb.tuindice.utils.DECIMALS_DIV
import com.gdavidpb.tuindice.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.fragment_evaluation.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.math.max
import kotlin.math.min

class EvaluationFragment : NavigationFragment() {

    private val viewModel by viewModel<EvaluationViewModel>()

    private val args by navArgs<EvaluationFragmentArgs>()

    private val isNewEvaluation by lazy { args.evaluationId == null }

    private val inputMethodManager by inject<InputMethodManager>()

    private lateinit var datePicker: EvaluationDatePicker

    override fun onCreateView() = R.layout.fragment_evaluation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(false)

        initChipGroup()
        initListeners()

        btnSave.onClickOnce(::onSaveClick)

        with(viewModel) {
            getSubject(sid = args.subjectId)

            val evaluationId = args.evaluationId

            if (evaluationId != null)
                getEvaluation(eid = evaluationId)
            else
                initEvaluation(evaluation = null)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        with(viewModel) {
            observe(subject, ::subjectObserver)
            observe(evaluation, ::evaluationObserver)
            observe(add, ::addEvaluationObserver)
        }
    }

    private fun onSaveClick() {
        val isOk = checkChanges()

        if (isOk) {
            if (isNewEvaluation)
                viewModel.addEvaluation(evaluation = collectAddEvaluation())
            else
                viewModel.updateEvaluation(request = collectUpdateEvaluation())

            navigateUp()
        }
    }

    private fun initEvaluation(evaluation: Evaluation?) {
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

            syncChipGroup()

            checkNotes()

            updateGradeValue(value = grade)
        } else {
            updateGradeValue(value = 0.0)
        }
    }

    private fun initListeners() {
        tViewLabelNotes.onClickOnce(::onNotesClick)
        tViewDate.onClickOnce(::onDateClick)

        cGroupEvaluation.setOnCheckedChangeListener { _, _ ->
            syncChipGroup()
        }

        sEvaluationDate.onCheckedChange { isChecked ->
            datePicker.isDateSelectable = isChecked
        }

        sBarMaxGrade.onSeekBarChange {
            onProgressChanged { progress, fromUser ->
                if (fromUser) updateGradeValue(value = progress.toGrade())
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
            }
        }
    }

    private fun initChipGroup() {
        EvaluationType.values().forEach { evaluationType ->
            View.inflate(context, R.layout.view_evaluation_chip, null).also { chip ->
                chip as Chip

                chip.text = getString(evaluationType.stringRes)

                chip.setOnCloseIconClickListener {
                    cGroupEvaluation.clearCheck()
                    syncChipGroup()
                }
            }.also(cGroupEvaluation::addView)
        }
    }

    private fun syncChipGroup() {
        val isAnyChipChecked = cGroupEvaluation.checkedChipIndex != -1

        cGroupEvaluation.forEach { chip ->
            chip as Chip
            chip.isVisible = !isAnyChipChecked || chip.isChecked
            chip.isCloseIconVisible = isAnyChipChecked && chip.isChecked
        }
    }

    private fun checkChanges(): Boolean {
        return when {
            cGroupEvaluation.checkedChipId == -1 -> {
                cGroupEvaluation.animateLookAtMe()
                R.string.toast_evaluation_type_missed
            }
            sBarMaxGrade.progress == 0 -> {
                sBarMaxGrade.animateLookAtMe()
                R.string.toast_evaluation_value_missed
            }
            !datePicker.isValidState -> {
                tViewDate.animateLookAtMe()
                R.string.toast_evaluation_date_missed
            }
            else -> -1
        }.let { resource ->
            (resource == -1).also { isOk -> if (!isOk) snackBar { messageResource = resource } }
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

    private fun collectAddEvaluation(): Evaluation {
        val evaluationType = cGroupEvaluation
                .checkedChipIndex
                .let { index -> EvaluationType.values()[index] }

        val maxGrade = sBarMaxGrade.progress.toGrade()

        return Evaluation(
                id = args.evaluationId ?: "",
                sid = args.subjectId,
                subjectCode = args.subjectCode,
                type = evaluationType,
                grade = maxGrade,
                maxGrade = maxGrade,
                date = datePicker.selectedDate,
                notes = "${eTextNotes.text}",
                isDone = false
        )
    }

    private fun collectUpdateEvaluation(): UpdateEvaluationRequest {
        val evaluationType = cGroupEvaluation
                .checkedChipIndex
                .let { index -> EvaluationType.values()[index] }

        val maxGrade = sBarMaxGrade.progress.toGrade()

        val evaluationId = args.evaluationId ?: ""

        val update = EvaluationUpdate(
                type = evaluationType.ordinal,
                grade = maxGrade,
                maxGrade = maxGrade,
                date = Timestamp(datePicker.selectedDate),
                notes = "${eTextNotes.text}",
                isDone = false
        )

        return UpdateEvaluationRequest(eid = evaluationId, update = update, dispatchChanges = true)
    }

    private fun updateGradeValue(value: Double) {
        tViewLabelGradeValue.text = getString(R.string.label_evaluation_grade_value, value)
    }

    private fun onDateClick() {
        requireActivity().datePicker {
            if (datePicker.selectedDate.time != 0L)
                selectedDate = datePicker.selectedDate

            onDateSelected { selectedDate ->
                datePicker.selectedDate = selectedDate
            }

            setUpDatePicker {
                val startDate = Date().add(Calendar.YEAR, -1).time
                val endDate = Date().add(Calendar.YEAR, 1).time

                minDate = startDate
                maxDate = endDate
            }
        }
    }

    private fun onNotesClick() {
        eTextNotes.isVisible = !eTextNotes.isVisible

        checkNotes()
    }

    private fun subjectObserver(result: Result<Subject, Nothing>?) {
        when (result) {
            is Result.OnSuccess -> {
                val subject = result.value

                val subjectHeader = getString(
                        R.string.label_evaluation_plan_header,
                        subject.code, subject.name
                )

                tViewSubjectHeader.text = subjectHeader
            }
        }
    }

    private fun evaluationObserver(result: Result<Evaluation, Nothing>?) {
        when (result) {
            is Result.OnSuccess -> {
                val evaluation = result.value

                initEvaluation(evaluation)
            }
        }
    }

    private fun addEvaluationObserver(result: Result<Evaluation, Nothing>?) {
        when (result) {
            is Result.OnSuccess -> {
                val evaluation = result.value

                initEvaluation(evaluation)
            }
        }
    }
}