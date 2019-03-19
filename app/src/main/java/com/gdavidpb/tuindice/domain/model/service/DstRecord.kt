package com.gdavidpb.tuindice.domain.model.service

data class DstRecord(
        val stats: DstRecordStats,
        val quarters: List<DstQuarter> = listOf()
) : DstData