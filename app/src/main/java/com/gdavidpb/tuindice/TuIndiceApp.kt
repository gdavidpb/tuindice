package com.gdavidpb.tuindice

import android.app.Application
import com.gdavidpb.tuindice.di.modules.appModule
import org.koin.android.ext.android.startKoin

open class TuIndiceApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(appModule))
    }
}