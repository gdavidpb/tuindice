package com.gdavidpb.tuindice.domain.repository

interface IdentifierRepository {
    suspend fun getIdentifier(): String?
}