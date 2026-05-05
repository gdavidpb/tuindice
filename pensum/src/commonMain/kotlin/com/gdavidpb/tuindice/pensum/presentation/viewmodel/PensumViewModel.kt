package com.gdavidpb.tuindice.pensum.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.pensum.presentation.action.ObservePensumActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.action.RefreshPensumActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.action.SelectPensumActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.action.SelectPensumModalityActionProcessor
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import kotlinx.coroutines.flow.Flow

class PensumViewModel(
	private val observePensumActionProcessor: ObservePensumActionProcessor,
	private val refreshPensumActionProcessor: RefreshPensumActionProcessor,
	private val selectPensumActionProcessor: SelectPensumActionProcessor,
	private val selectPensumModalityActionProcessor: SelectPensumModalityActionProcessor
) : BaseViewModel<Pensum.State, Pensum.Action, Pensum.Effect>(
	initialState = Pensum.State.Loading,
	initialAction = Pensum.Action.ObservePensum
) {
	fun refreshPensumAction() {
		sendAction(Pensum.Action.RefreshPensum)
	}

	fun selectPensumAction(careerCode: Int, year: Int) {
		sendAction(Pensum.Action.SelectPensum(careerCode = careerCode, year = year))
	}

	fun selectModalityAction(modalityId: String) {
		sendAction(Pensum.Action.SelectModality(modalityId = modalityId))
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
		}
	}
}
