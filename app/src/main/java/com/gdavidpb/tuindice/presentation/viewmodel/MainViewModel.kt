package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.utils.LiveCompletable
import com.gdavidpb.tuindice.utils.LiveResult
import com.gdavidpb.tuindice.utils.execute

class MainViewModel(
        private val startUpUseCase: StartUpUseCase,
        private val setLastScreenUseCase: SetLastScreenUseCase
) : ViewModel() {

    val fetchStartUpAction = LiveResult<StartUpAction>()
    val lastScreen = LiveCompletable()

    fun fetchStartUpAction(dataString: String) =
            execute(useCase = startUpUseCase, params = dataString, liveData = fetchStartUpAction)


    fun setLastScreen(@IdRes navId: Int) =
            execute(useCase = setLastScreenUseCase, params = navId, liveData = lastScreen)
}