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
import com.gdavidpb.tuindice.utils.DECIMALS_DIV
import com.gdavidpb.tuindice.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.fragment_evaluation.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EvaluationFragment : NavigationFragment() {

    private val viewModel by viewModel<EvaluationViewModel>()

    private val args by navArgs<EvaluationFragmentArgs>()

    private val isNewEvaluation by lazy { (args.evaluationId == null) }

    private val inputMethodManager by inject<InputMethodManager>()

    private val validations by lazy {
        arrayOf<Validation<*>>(
                `when`(tInputEvaluationName) { !isValid() } `do` { snackBar(R.string.toast_evaluation_name_missed) },
                `when`(tInputEvaluationGrade) { getGrade() > MAX_EVALUATION_GRADE } `do` { snackBar(R.string.toast_evaluation_grade_invalid_max) },
                `when`(tInputEvaluationGrade) { getGrade() % DECIMALS_DIV != 0.0 } `do` { snackBar(R.string.toast_evaluation_grade_invalid_step) },
                `when`(tInputEvaluationGrade) { !isValid() } `do` { snackBar(R.string.toast_evaluation_grade_missed) },
                `when`(dPickerEvaluationDate) { !isValid() } `do` { snackBar(R.string.toast_evaluation_date_missed) },
                `when`(cGroupEvaluation) { checkedChipId == -1 } `do` { snackBar(R.string.toast_evaluation_type_missed) }
        )
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
        val notes = evaluation.notes
        val grade = evaluation.grade
        val date = evaluation.date
        val evaluationType = evaluation.type.ordinal

        eTextEvaluationName.setText(notes)

        eTextEvaluationGrade.setText(grade.formatGrade())
        dPickerEvaluationDate.isChecked = (date.time != 0L)
        cGroupEvaluation.checkedChipIndex = evaluationType

        dPickerEvaluationDate.selectedDate = date
    }

    private fun initListeners() {
        btnEvaluationSave.onClickOnce(::onSaveClick)

        tInputEvaluationName.onValidate {
            getName().isNotBlank()
        }

        tInputEvaluationGrade.onValidate {
            val grade = getGrade()

            (grade % DECIMALS_DIV == 0.0) && (grade in (0.0..MAX_EVALUATION_GRADE))
        }

        dPickerEvaluationDate.onShowDatePicker {
            inputMethodManager.hideSoftKeyboard(requireActivity())
        }

        eTextEvaluationGrade.setOnEditorActionListener { _, actionId, _ ->
            when {
                actionId != EditorInfo.IME_ACTION_NEXT -> true
                !dPickerEvaluationDate.isChecked -> true
                else -> {
                    dPickerEvaluationDate.performClick()
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

                    animateLookAtMe(5f)
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
                notes = getName(),
                grade = maxGrade,
                maxGrade = maxGrade,
                date = dPickerEvaluationDate.selectedDate,
                type = getType(),
                isDone = false
        )
    }

    private fun collectUpdateEvaluation(): UpdateEvaluationRequest {
        val maxGrade = getGrade()

        val evaluationId = args.evaluationId ?: ""

        val update = EvaluationUpdate(
                notes = getName(),
                grade = maxGrade,
                maxGrade = maxGrade,
                date = Timestamp(dPickerEvaluationDate.selectedDate),
                type = getType().ordinal,
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