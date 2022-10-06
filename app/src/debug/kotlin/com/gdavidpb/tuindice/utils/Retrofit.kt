package com.gdavidpb.tuindice.utils

import retrofit2.Retrofit
import retrofit2.mock.MockRetrofit
import retrofit2.mock.create

inline fun <reified T, reified Q : T> Retrofit.createMockService(): T {
    val delegate = MockRetrofit.Builder(this).build().create<T>()
    val constructor = Q::class.constructors.first()

    return constructor.call(delegate)
}