package com.gdavidpb.tuindice.migrations

import android.content.Context

object MigrationManager {
    fun execute(context: Context) {
        ApiMigration(context).applyIfRequired()
    }
}