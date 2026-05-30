package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermValidationError
import com.gdavidpb.tuindice.record.domain.usecase.CreateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.param.CreateSyntheticTermParams
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_error_duplicate_subject
import tuindice.record.generated.resources.create_term_error_generic
import tuindice.record.generated.resources.create_term_error_period_in_past
import tuindice.record.generated.resources.create_term_error_record_unavailable
import tuindice.record.generated.resources.create_term_error_subject_already_planned
import tuindice.record.generated.resources.create_term_error_subject_already_taken
import tuindice.record.generated.resources.create_term_error_term_already_exists
import tuindice.record.generated.resources.create_term_error_term_must_be_after_latest

class CreateSyntheticTermActionProcessor(
	private val createSyntheticTermUseCase: CreateSyntheticTermUseCase,
	private val updateSyntheticTermUseCase: UpdateSyntheticTermUseCase
) : ActionProcessor<
	CreateSyntheticTerm.State,
	CreateSyntheticTerm.Action.CreateTerm,
	CreateSyntheticTerm.Effect
	>() {
	override suspend fun process(
		action: CreateSyntheticTerm.Action.CreateTerm,
		sideEffect: (CreateSyntheticTerm.Effect) -> Unit
	): Flow<Mutation<CreateSyntheticTerm.State>> {
		return flow {
			val params = CreateSyntheticTermParams(
				editingTermId = action.editingTermId,
				editingTermKey = action.editingTermKey,
				period = action.period,
				subjects = action.subjects
			)
			val result = if (action.editingTermId == null) {
				createSyntheticTermUseCase.execute(params)
			} else {
				updateSyntheticTermUseCase.execute(params)
			}

			result.collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading ->
						emit(
							suspend { state: CreateSyntheticTerm.State ->
								state.copy(
									isSubmitting = true,
									submitError = UiText.Empty
								)
							}
						)

					is UseCaseState.Data -> {
						emit(
							suspend { state: CreateSyntheticTerm.State ->
								state.copy(
									isSubmitting = false,
									submitError = UiText.Empty
								)
							}
						)
						sideEffect(CreateSyntheticTerm.Effect.NavigateBack)
					}

					is UseCaseState.Error ->
						emit(
							suspend { state: CreateSyntheticTerm.State ->
								state.copy(
									isSubmitting = false,
									submitError = useCaseState.error.toSubmitErrorText()
								)
							}
						)
				}
			}
		}
	}
}

private fun RecordUseCaseError?.toSubmitErrorText(): UiText {
	return when (this) {
		is RecordUseCaseError.SyntheticTermValidation -> UiText.Resource(
			when (reason) {
				SyntheticTermValidationError.RECORD_UNAVAILABLE,
				SyntheticTermValidationError.TERM_NOT_FOUND,
				-> Res.string.create_term_error_record_unavailable

				SyntheticTermValidationError.PERIOD_IN_PAST -> Res.string.create_term_error_period_in_past
				SyntheticTermValidationError.TERM_ALREADY_EXISTS -> Res.string.create_term_error_term_already_exists
				SyntheticTermValidationError.TERM_MUST_BE_AFTER_LATEST -> Res.string.create_term_error_term_must_be_after_latest
				SyntheticTermValidationError.DUPLICATE_SUBJECT -> Res.string.create_term_error_duplicate_subject
				SyntheticTermValidationError.SUBJECT_ALREADY_TAKEN -> Res.string.create_term_error_subject_already_taken
				SyntheticTermValidationError.SUBJECT_ALREADY_PLANNED -> Res.string.create_term_error_subject_already_planned
			}
		)

		RecordUseCaseError.NoConnection,
		RecordUseCaseError.Timeout,
		RecordUseCaseError.Unauthorized,
		RecordUseCaseError.Unavailable,
		null,
		-> UiText.Resource(Res.string.create_term_error_generic)
	}
}
