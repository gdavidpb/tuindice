package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.di.modules.appModule
import com.gdavidpb.tuindice.modules.mockModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

fun restartKoinModules() {
    if (BuildConfig.DEBUG) {
        unloadKoinModules(mockModule)
        loadKoinModules(mockModule)
    } else {
        unloadKoinModules(appModule)
        loadKoinModules(appModule)
    }
}