package com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.emptyStateFlow
import com.gdavidpb.tuindice.base.utils.extension.stateInAction
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.GetEnrollmentProofUseCase

class EnrollmentProofViewModel(
	getEnrollmentProofUseCase: GetEnrollmentProofUseCase
) : ViewModel() {
	val enrollmentProofParams = emptyStateFlow<Unit>()

	val enrollmentProof =
		stateInAction(useCase = getEnrollmentProofUseCase, paramsFlow = enrollmentProofParams)
}