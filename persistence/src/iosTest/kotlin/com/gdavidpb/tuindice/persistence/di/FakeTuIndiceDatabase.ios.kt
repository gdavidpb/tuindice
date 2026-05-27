package com.gdavidpb.tuindice.persistence.di

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase

internal actual fun createFakeTuIndiceDatabase(): TuIndiceDatabase = IosFakeTuIndiceDatabase()

private class IosFakeTuIndiceDatabase : FakeTuIndiceDatabase()
