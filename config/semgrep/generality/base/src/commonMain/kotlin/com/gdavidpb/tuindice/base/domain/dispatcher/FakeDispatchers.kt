package com.gdavidpb.tuindice.base.domain.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// Control negativo: la definición canónica de dispatchers vive aquí
// y está exenta de no-raw-dispatchers por paths.
val fakeDefault: CoroutineDispatcher = Dispatchers.Default
