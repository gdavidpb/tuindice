package com.gdavidpb.tuindice.domain.mapper

interface BidirectionalMapper<A, B> {
    fun mapTo(from: A): B
    fun mapFrom(to: B): A
}