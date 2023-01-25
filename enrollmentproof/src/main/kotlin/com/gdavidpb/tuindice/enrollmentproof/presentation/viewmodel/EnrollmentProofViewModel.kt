package com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.LiveEvent
import com.gdavidpb.tuindice.base.utils.extension.execute
import com.gdavidpb.tuindice.enrollmentproof.domain.error.GetEnrollmentError
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.GetEnrollmentProofUseCase

class EnrollmentProofViewModel(
	private val getEnrollmentProofUseCase: GetEnrollmentProofUseCase
) : ViewModel() {
	val enrollment = LiveEvent<String, GetEnrollmentError>()

	fun tryFetchEnrollmentProof() =
		execute(useCase = getEnrollmentProofUseCase, params = Unit, liveData = enrollment)
}