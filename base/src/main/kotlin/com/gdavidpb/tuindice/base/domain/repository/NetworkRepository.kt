package com.gdavidpb.tuindice.base.domain.repository

interface NetworkRepository {
    fun isAvailable(): Boolean
}