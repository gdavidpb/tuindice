package com.gdavidpb.tuindice.domain.mapper

interface Mapper<in I, out O> {
    fun map(value: I): O
}