package com.gdavidpb.tuindice.record.domain.exception

import com.gdavidpb.tuindice.record.domain.model.SyntheticTermValidationError

class SyntheticTermValidationException(
	val reason: SyntheticTermValidationError
) : IllegalArgumentException(reason.name)
