package com.gdavidpb.tuindice.persistence.domain.mutation

enum class MutationFailureKind {
	Conflict,
	PreconditionFailed,
	NotFound,
	Terminal
}
