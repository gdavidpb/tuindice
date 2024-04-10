package com.gdavidpb.tuindice.base.domain.repository

interface IdentifierRepository {
	fun generateRandomIdentifier(): String
}