package com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.stateInEagerly
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.GetEnrollmentProofUseCase
import kotlinx.coroutines.flow.MutableSharedFlow

class EnrollmentProofViewModel(
	getEnrollmentProofUseCase: GetEnrollmentProofUseCase
) : ViewModel() {
	val enrollmentProofParams = MutableSharedFlow<Unit>()

	val enrollmentProof =
		stateInEagerly(useCase = getEnrollmentProofUseCase, paramsFlow = enrollmentProofParams)
}