package com.gdavidpb.tuindice.record.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
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
				recordReducer.reduce(
					action = action,
					stateProvider = ::getCurrentState,
					stateProducer = ::setState,
					eventProducer = ::sendEvent
				)

			is Record.Action.RemoveQuarter ->
				removeQuarterReducer.reduce(
					action = action,
					stateProvider = ::getCurrentState,
					stateProducer = ::setState,
					eventProducer = ::sendEvent
				)

			is Record.Action.UpdateSubject ->
				updateSubjectReducer.reduce(
					action = action,
					stateProvider = ::getCurrentState,
					stateProducer = ::setState,
					eventProducer = ::sendEvent
				)

			is Record.Action.WithdrawSubject ->
				withdrawSubjectReducer.reduce(
					action = action,
					stateProvider = ::getCurrentState,
					stateProducer = ::setState,
					eventProducer = ::sendEvent
				)

			is Record.Action.OpenEvaluationPlan ->
				sendEvent(Record.Event.NavigateToEvaluationPlan(action.item))

			is Record.Action.OpenEnrollmentProof ->
				sendEvent(Record.Event.NavigateToEnrollmentProof)
		}
	}
}