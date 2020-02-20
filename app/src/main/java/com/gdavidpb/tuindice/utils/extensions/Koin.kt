package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.di.modules.appModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

fun restartKoinModules() {
    unloadKoinModules(appModule)
    loadKoinModules(appModule)
}