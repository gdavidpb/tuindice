package com.gdavidpb.tuindice.ui.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.fragment_evaluation.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class EvaluationFragment : NavigationFragment() {

    private val viewModel by viewModel<EvaluationViewModel>()

    private val args by navArgs<EvaluationFragmentArgs>()

    private val isNewEvaluation by lazy { args.evaluationId == null }

    private val inputMethodManager by inject<InputMethodManager>()

    private val datePicker by lazy {
        EvaluationDatePicker(tViewEvaluationDate)
    }

    private val primaryTint by lazy {
        ColorStateList.valueOf(requireContext().getCompatColor(R.color.color_primary))
    }

    private val secondaryTint by lazy {
        ColorStateList.valueOf(requireContext().getCompatColor(R.color.color_secondary_text))
    }

    override fun onCreateView() = R.layout.fragment_evaluation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(false)

        initChipGroup()
        initListeners()

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
        if (evaluation != null) {
            val grade = evaluation.grade
            val notes = evaluation.notes
            val date = evaluation.date
            val evaluationType = evaluation.type.ordinal

            eTextEvaluationGrade.setText("$grade")
            sEvaluationDate.isChecked = date.time != 0L
            cGroupEvaluation.checkedChipIndex = evaluationType

            datePicker.selectedDate = date
            datePicker.isDateSelectable = sEvaluationDate.isChecked

            eTextEvaluationName.setText(notes)
            eTextEvaluationName.setSelection(notes.length)
        }
    }

    private fun initListeners() {
        btnEvaluationSave.onClickOnce(::onSaveClick)
        tViewEvaluationDate.onClickOnce(::onDateClick)

        sEvaluationDate.onCheckedChange { isChecked ->
            datePicker.isDateSelectable = isChecked
        }

        eTextEvaluationName.setOnFocusChangeListener { _, hasFocus ->
            val tint = if (hasFocus) primaryTint else secondaryTint

            tInputEvaluationName.setStartIconTintList(tint)
        }

        eTextEvaluationGrade.setOnFocusChangeListener { _, hasFocus ->
            val tint = if (hasFocus) primaryTint else secondaryTint

            tInputEvaluationGrade.setStartIconTintList(tint)
        }

        eTextEvaluationGrade.setOnEditorActionListener { _, actionId, _ ->
            when {
                actionId != EditorInfo.IME_ACTION_NEXT -> true
                !sEvaluationDate.isChecked -> true
                else -> {
                    tViewEvaluationDate.performClick()
                    inputMethodManager.hideSoftKeyboard(eTextEvaluationGrade)
                    false
                }
            }
        }
    }

    private fun initChipGroup() {
        EvaluationType.values().forEach { evaluationType ->
            View.inflate(context, R.layout.view_evaluation_chip, null).also { chip ->
                chip as Chip

                chip.text = getString(evaluationType.stringRes)
            }.also(cGroupEvaluation::addView)
        }
    }

    private fun checkChanges(): Boolean {
        return when {
            eTextEvaluationName.text.isNullOrBlank() -> {
                tInputEvaluationName.animateLookAtMe()
                R.string.toast_evaluation_name_missed
            }
            eTextEvaluationGrade.text.toString().toDoubleOrNull() ?: 0.0 == 0.0 -> {
                tInputEvaluationGrade.animateLookAtMe()
                R.string.toast_evaluation_grade_missed
            }
            !datePicker.isValidState -> {
                tViewEvaluationDate.animateLookAtMe()
                R.string.toast_evaluation_date_missed
            }
            cGroupEvaluation.checkedChipId == -1 -> {
                cGroupEvaluation.animateLookAtMe()
                R.string.toast_evaluation_type_missed
            }
            else -> -1
        }.let { resource ->
            (resource == -1).also { isOk -> if (!isOk) snackBar { messageResource = resource } }
        }
    }

    private fun collectAddEvaluation(): Evaluation {
        val evaluationType = cGroupEvaluation
                .checkedChipIndex
                .let { index -> EvaluationType.values()[index] }

        val maxGrade = eTextEvaluationGrade.text.toString().toDoubleOrNull() ?: 0.0

        return Evaluation(
                id = args.evaluationId ?: "",
                sid = args.subjectId,
                subjectCode = args.subjectCode,
                type = evaluationType,
                grade = maxGrade,
                maxGrade = maxGrade,
                date = datePicker.selectedDate,
                notes = "${eTextEvaluationName.text}",
                isDone = false
        )
    }

    private fun collectUpdateEvaluation(): UpdateEvaluationRequest {
        val evaluationType = cGroupEvaluation
                .checkedChipIndex
                .let { index -> EvaluationType.values()[index] }

        val maxGrade = eTextEvaluationGrade.text.toString().toDoubleOrNull() ?: 0.0

        val evaluationId = args.evaluationId ?: ""

        val update = EvaluationUpdate(
                type = evaluationType.ordinal,
                grade = maxGrade,
                maxGrade = maxGrade,
                date = Timestamp(datePicker.selectedDate),
                notes = "${eTextEvaluationName.text}",
                isDone = false
        )

        return UpdateEvaluationRequest(eid = evaluationId, update = update, dispatchChanges = true)
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

    private fun subjectObserver(result: Result<Subject, Nothing>?) {
        when (result) {
            is Result.OnSuccess -> {
                val subject = result.value

                val subjectHeader = getString(
                        R.string.label_evaluation_plan_header,
                        subject.code, subject.name
                )

                tViewEvaluationHeader.text = subjectHeader
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