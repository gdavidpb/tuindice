package com.gdavidpb.tuindice.persistence.di

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.QuarterDao
import com.gdavidpb.tuindice.persistence.data.room.daos.SubjectDao
import com.gdavidpb.tuindice.persistence.data.room.daos.UserDao
import org.koin.dsl.module

val persistenceModule = module {
	single<UserDao> { get<TuIndiceDatabase>().users }
	single<QuarterDao> { get<TuIndiceDatabase>().quarters }
	single<SubjectDao> { get<TuIndiceDatabase>().subjects }
	single<EvaluationDao> { get<TuIndiceDatabase>().evaluations }
	single<PendingMutationDao> { get<TuIndiceDatabase>().pendingMutations }
}
