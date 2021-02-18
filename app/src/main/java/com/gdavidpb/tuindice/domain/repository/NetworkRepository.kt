package com.gdavidpb.tuindice.domain.repository

interface NetworkRepository {
    fun isAvailable(): Boolean
}