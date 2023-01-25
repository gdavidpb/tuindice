package com.gdavidpb.tuindice.base.utils.extension

operator fun <E> Collection<E>.component6(): E = elementAt(5)
operator fun <E> Collection<E>.component7(): E = elementAt(6)
operator fun <E> Collection<E>.component8(): E = elementAt(7)

inline fun <reified T> Collection<*>.find() = find { it is T } as? T
inline fun <reified T> Collection<*>.any() = any { it is T }

