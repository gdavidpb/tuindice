package com.gdavidpb.tuindice.pensum.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.pensum.presentation.action.ObservePensumActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.action.RefreshPensumActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.action.SelectPensumActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.action.SelectPensumModalityActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.action.SelectPensumSelectionActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import kotlinx.coroutines.flow.Flow

class PensumViewModel(
	private val observePensumActionProcessor: ObservePensumActionProcessor,
	private val refreshPensumActionProcessor: RefreshPensumActionProcessor,
	private val selectPensumActionProcessor: SelectPensumActionProcessor,
	private val selectPensumModalityActionProcessor: SelectPensumModalityActionProcessor,
	private val selectPensumSelectionActionProcessor: SelectPensumSelectionActionProcessor,
	override val eventPublisher: EventPublisher
) : BaseViewModel<Pensum.State, Pensum.Action, Pensum.Effect>(
	name = "pensum",
	initialState = Pensum.State.Idle,
	initialAction = Pensum.Action.ObservePensum
) {
	fun refreshPensumAction() {
		sendAction(Pensum.Action.RefreshPensum)
	}

	fun selectPensumAction(year: Int) {
		sendAction(Pensum.Action.SelectPensum(year = year))
	}

	fun selectModalityAction(modalityId: String) {
		sendAction(Pensum.Action.SelectModality(modalityId = modalityId))
	}

	fun selectSelectionAction(year: Int, modalityId: String) {
		sendAction(
			Pensum.Action.SelectSelection(
				year = year,
				modalityId = modalityId
			)
		)
	}

	override suspend fun processAction(
		action: Pensum.Action,
		sideEffect: (Pensum.Effect) -> Unit
	): Flow<Mutation<Pensum.State>> {
		return when (action) {
			is Pensum.Action.ObservePensum -> observePensumActionProcessor.process(action, sideEffect)
			is Pensum.Action.RefreshPensum -> refreshPensumActionProcessor.process(action, sideEffect)
			is Pensum.Action.SelectPensum -> selectPensumActionProcessor.process(action, sideEffect)
			is Pensum.Action.SelectModality -> selectPensumModalityActionProcessor.process(action, sideEffect)
			is Pensum.Action.SelectSelection -> selectPensumSelectionActionProcessor.process(action, sideEffect)
		}
	}
}
