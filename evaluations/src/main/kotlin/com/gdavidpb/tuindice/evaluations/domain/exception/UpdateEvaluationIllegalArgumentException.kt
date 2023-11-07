package com.gdavidpb.tuindice.evaluations.domain.exception

import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationError

class UpdateEvaluationIllegalArgumentException(
	val error: UpdateEvaluationError
) : IllegalArgumentException()