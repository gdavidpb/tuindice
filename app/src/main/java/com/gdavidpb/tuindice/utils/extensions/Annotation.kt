package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.usecase.coroutines.BaseUseCase
import com.gdavidpb.tuindice.ui.activities.NavigationActivity
import com.gdavidpb.tuindice.utils.annotations.AllowDisabledAccount
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import kotlin.reflect.full.findAnnotation

fun BaseUseCase<*, *>.ignoredException(throwable: Throwable) =
        this::class.findAnnotation<IgnoredExceptions>()
                ?.run { exceptions.any { it.isInstance(throwable) } }
                ?: false

fun NavigationActivity.allowDisabledAccount() =
        this::class.findAnnotation<AllowDisabledAccount>() != null