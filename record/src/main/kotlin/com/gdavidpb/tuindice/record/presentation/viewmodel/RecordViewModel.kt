package com.gdavidpb.tuindice.record.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.reducer.collect
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSubjectUseCase
import com.gdavidpb.tuindice.record.domain.usecase.WithdrawSubjectUseCase
import com.gdavidpb.tuindice.record.domain.usecase.param.RemoveQuarterParams
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.usecase.param.WithdrawSubjectParams
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.presentation.reducer.RecordReducer
import com.gdavidpb.tuindice.record.presentation.reducer.RemoveQuarterReducer
import com.gdavidpb.tuindice.record.presentation.reducer.UpdateSubjectReducer
import com.gdavidpb.tuindice.record.presentation.reducer.WithdrawSubjectReducer

class RecordViewModel(
	private val getQuartersUseCase: GetQuartersUseCase,
	private val removeQuarterUseCase: RemoveQuarterUseCase,
	private val updateSubjectUseCase: UpdateSubjectUseCase,
	private val withdrawSubjectUseCase: WithdrawSubjectUseCase,
	private val recordReducer: RecordReducer,
	private val removeQuarterReducer: RemoveQuarterReducer,
	private val updateSubjectReducer: UpdateSubjectReducer,
	private val withdrawSubjectReducer: WithdrawSubjectReducer
) : BaseViewModel<Record.State, Record.Action, Record.Event>(initialViewState = Record.State.Loading) {

	fun loadQuartersAction() =
		emitAction(Record.Action.LoadQuarters)

	fun removeQuarterAction(params: RemoveQuarterParams) =
		emitAction(Record.Action.RemoveQuarter(params))

	fun updateSubjectAction(params: UpdateSubjectParams) =
		emitAction(Record.Action.UpdateSubject(params))

	fun withdrawSubjectAction(params: WithdrawSubjectParams) =
		emitAction(Record.Action.WithdrawSubject(params))

	fun openEvaluationPlanAction(item: SubjectItem) =
		emitAction(Record.Action.OpenEvaluationPlan(item))

	fun openEnrollmentProofAction() =
		emitAction(Record.Action.OpenEnrollmentProof)

	override suspend fun reducer(action: Record.Action) {
		when (action) {
			is Record.Action.LoadQuarters ->
				getQuartersUseCase
					.execute(params = Unit)
					.collect(viewModel = this, reducer = recordReducer)

			is Record.Action.RemoveQuarter ->
				removeQuarterUseCase
					.execute(params = action.params)
					.collect(viewModel = this, reducer = removeQuarterReducer)

			is Record.Action.UpdateSubject ->
				updateSubjectUseCase
					.execute(params = action.params)
					.collect(viewModel = this, reducer = updateSubjectReducer)

			is Record.Action.WithdrawSubject ->
				withdrawSubjectUseCase
					.execute(params = action.params)
					.collect(viewModel = this, reducer = withdrawSubjectReducer)

			is Record.Action.OpenEvaluationPlan ->
				sendEvent(Record.Event.NavigateToEvaluationPlan(action.item))

			is Record.Action.OpenEnrollmentProof ->
				sendEvent(Record.Event.NavigateToEnrollmentProof)
		}
	}
}