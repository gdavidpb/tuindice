package com.gdavidpb.tuindice.models

import android.app.Application
import android.support.v7.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import com.gdavidpb.tuindice.BuildConfig
import io.fabric.sdk.android.Fabric

class FabricApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        /* Set up vector compatibility */
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        /* Set up Google Fabric */
        if (!BuildConfig.DEBUG)
            Fabric.with(this, Crashlytics())

        registerActivityLifecycleCallbacks(LifecycleHandler())
    }
}