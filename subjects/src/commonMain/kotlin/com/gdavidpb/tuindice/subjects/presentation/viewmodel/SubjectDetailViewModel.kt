package com.gdavidpb.tuindice.subjects.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.action.LoadSubjectDetailActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.action.RefreshSubjectDetailActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.action.SelectSubjectSegmentTabActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import kotlinx.coroutines.flow.Flow

class SubjectDetailViewModel(
	private val loadSubjectDetailActionProcessor: LoadSubjectDetailActionProcessor,
	private val refreshSubjectDetailActionProcessor: RefreshSubjectDetailActionProcessor,
	private val selectSubjectSegmentTabActionProcessor: SelectSubjectSegmentTabActionProcessor
) : BaseViewModel<
		SubjectDetail.State,
		SubjectDetail.Action,
		SubjectDetail.Effect
		>(initialState = SubjectDetail.State.Loading) {

	fun loadSubjectDetailAction(subjectCode: String) {
		sendAction(SubjectDetail.Action.LoadSubjectDetail(subjectCode = subjectCode))
	}

	fun refreshSubjectDetailAction(subjectCode: String) {
		sendAction(SubjectDetail.Action.RefreshSubjectDetail(subjectCode = subjectCode))
	}

	fun selectSubjectSegmentTabAction(tab: SubjectSegmentTab) {
		sendAction(SubjectDetail.Action.SelectSubjectSegmentTab(tab = tab))
	}

	override suspend fun processAction(
		action: SubjectDetail.Action,
		sideEffect: (SubjectDetail.Effect) -> Unit
	): Flow<Mutation<SubjectDetail.State>> {
		return when (action) {
			is SubjectDetail.Action.LoadSubjectDetail ->
				loadSubjectDetailActionProcessor.process(action, sideEffect)

			is SubjectDetail.Action.RefreshSubjectDetail ->
				refreshSubjectDetailActionProcessor.process(action, sideEffect)

			is SubjectDetail.Action.SelectSubjectSegmentTab ->
				selectSubjectSegmentTabActionProcessor.process(action, sideEffect)
		}
	}
}
