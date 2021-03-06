package com.gdavidpb.tuindice.migrations

abstract class Migration {
    abstract fun isRequired(): Boolean
    abstract fun onFailure(throwable: Throwable)
    abstract fun onMigrate()

    fun applyIfRequired() {
        if (isRequired())
            runCatching {
                onMigrate()
            }.onFailure { throwable ->
                onFailure(throwable)
            }
    }
}