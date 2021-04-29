package com.gdavidpb.tuindice.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.navArgs
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.model.database.EvaluationUpdate
import com.gdavidpb.tuindice.data.utils.Validation
import com.gdavidpb.tuindice.data.utils.`do`
import com.gdavidpb.tuindice.data.utils.`when`
import com.gdavidpb.tuindice.data.utils.firstInvalid
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.EvaluationType
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.ui.customs.EvaluationDatePicker
import com.gdavidpb.tuindice.utils.MAX_EVALUATION_GRADE
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

    private val isNewEvaluation by lazy { (args.evaluationId == null) }

    private val inputMethodManager by inject<InputMethodManager>()

    private val validations by lazy {
        arrayOf<Validation<*>>(
                `when`(tInputEvaluationName) { !isValid() } `do` { snackBar(R.string.toast_evaluation_name_missed) },
                `when`(tInputEvaluationGrade) { getGrade() > MAX_EVALUATION_GRADE } `do` { snackBar(R.string.toast_evaluation_grade_invalid) },
                `when`(tInputEvaluationGrade) { !isValid() } `do` { snackBar(R.string.toast_evaluation_grade_missed) },
                `when`(datePicker) { !isValidState } `do` { snackBar(R.string.toast_evaluation_date_missed) },
                `when`(cGroupEvaluation) { checkedChipId == -1 } `do` { snackBar(R.string.toast_evaluation_type_missed) }
        )
    }

    private val datePicker by lazy {
        EvaluationDatePicker(tViewEvaluationDate)
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

            if (evaluationId != null) getEvaluation(eid = evaluationId)
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

    private fun initEvaluation(evaluation: Evaluation) {
        val grade = evaluation.grade
        val notes = evaluation.notes
        val date = evaluation.date
        val evaluationType = evaluation.type.ordinal

        eTextEvaluationGrade.setText(grade.formatGrade())
        sEvaluationDate.isChecked = (date.time != 0L)
        cGroupEvaluation.checkedChipIndex = evaluationType

        datePicker.selectedDate = date
        datePicker.isDateSelectable = sEvaluationDate.isChecked

        eTextEvaluationName.setText(notes)
        eTextEvaluationName.setSelection(notes.length)
    }

    private fun initListeners() {
        btnEvaluationSave.onClickOnce(::onSaveClick)
        tViewEvaluationDate.onClickOnce(::onDateClick)

        sEvaluationDate.onCheckedChange { isChecked ->
            datePicker.isDateSelectable = isChecked
            tViewEvaluationDate.isClickable = isChecked
        }

        tInputEvaluationName.onValidate {
            getName().isNotBlank()
        }

        tInputEvaluationGrade.onValidate {
            getGrade() in (0.0..MAX_EVALUATION_GRADE)
        }

        eTextEvaluationGrade.setOnEditorActionListener { _, actionId, _ ->
            when {
                actionId != EditorInfo.IME_ACTION_NEXT -> true
                !sEvaluationDate.isChecked -> true
                else -> {
                    tViewEvaluationDate.performClick()
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

    private fun onSaveClick() {
        validations.firstInvalid {
            when (this) {
                is View -> {
                    requestFocus()

                    animateLookAtMe()
                }
            }
        }.isNull {
            if (isNewEvaluation)
                viewModel.addEvaluation(evaluation = collectAddEvaluation())
            else
                viewModel.updateEvaluation(request = collectUpdateEvaluation())

            navigateUp()
        }
    }

    private fun onDateClick() {
        inputMethodManager.hideSoftKeyboard(eTextEvaluationGrade)

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

    private fun getName(): String {
        return "${eTextEvaluationName.text}"
    }

    private fun getGrade(): Double {
        return "${eTextEvaluationGrade.text}".toDoubleOrNull() ?: 0.0
    }

    private fun getType(): EvaluationType {
        return cGroupEvaluation
                .checkedChipIndex
                .let { index -> EvaluationType.values()[index] }
    }

    private fun collectAddEvaluation(): Evaluation {
        val maxGrade = getGrade()

        return Evaluation(
                id = args.evaluationId ?: "",
                sid = args.subjectId,
                subjectCode = args.subjectCode,
                type = getType(),
                grade = maxGrade,
                maxGrade = maxGrade,
                date = datePicker.selectedDate,
                notes = getName(),
                isDone = false
        )
    }

    private fun collectUpdateEvaluation(): UpdateEvaluationRequest {
        val maxGrade = getGrade()

        val evaluationId = args.evaluationId ?: ""

        val update = EvaluationUpdate(
                type = getType().ordinal,
                grade = maxGrade,
                maxGrade = maxGrade,
                date = Timestamp(datePicker.selectedDate),
                notes = getName(),
                isDone = false
        )

        return UpdateEvaluationRequest(eid = evaluationId, update = update, dispatchChanges = true)
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