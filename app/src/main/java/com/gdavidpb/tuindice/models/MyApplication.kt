package com.gdavidpb.tuindice.models

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(LifecycleHandler())
    }
}