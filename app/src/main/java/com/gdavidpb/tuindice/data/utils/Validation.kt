package com.gdavidpb.tuindice.data.utils

data class Validation<T : Any>(
        val `object`: T,
        val validator: () -> Boolean,
        val operator: () -> Unit
)

fun Array<Validation<*>>.firstInvalid(onFound: Any.() -> Unit) = firstOrNull { set ->
    set.run {
        validator().also { invalid ->
            if (invalid) {
                operator()

                onFound(`object`)
            }
        }
    }
}

infix fun <T : Any> Pair<T, () -> Boolean>.`do`(operator: T.() -> Unit) = Validation(first, second, { operator(first) })

fun <T : Any> `when`(`object`: T, validator: T.() -> Boolean) = `object` to { validator(`object`) }

