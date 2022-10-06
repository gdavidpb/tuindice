package com.gdavidpb.tuindice.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class IdempotentLocker {
    private val locker = AtomicBoolean(false)

    fun lock(UnlockIn: Long): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            delay(UnlockIn)
            locker.compareAndSet(true, false)
        }

        return locker.compareAndSet(false, true)
    }
}