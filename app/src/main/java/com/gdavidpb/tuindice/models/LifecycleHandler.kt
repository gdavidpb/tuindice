package com.gdavidpb.tuindice.models

import android.app.Activity
import android.os.Bundle
import android.app.Application.ActivityLifecycleCallbacks

class LifecycleHandler : ActivityLifecycleCallbacks {

    companion object {
        private var started = 0
        private var stopped = 0

        fun isAppVisible(): Boolean = started > stopped
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { }

    override fun onActivityDestroyed(activity: Activity) { }

    override fun onActivityResumed(activity: Activity) { }

    override fun onActivityPaused(activity: Activity) { }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { }

    override fun onActivityStarted(activity: Activity) {
        ++started
    }

    override fun onActivityStopped(activity: Activity) {
        ++stopped
    }
}