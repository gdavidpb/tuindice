package com.gdavidpb.tuindice.record.domain.exception

import com.gdavidpb.tuindice.record.domain.error.SubjectError

class SubjectIllegalArgumentException(
	val error: SubjectError
) : IllegalArgumentException()