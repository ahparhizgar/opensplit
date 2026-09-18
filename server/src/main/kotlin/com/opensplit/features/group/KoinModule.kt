package com.opensplit.features.group

import org.koin.dsl.module

fun groupKoinModule() = module {
  single<GroupRepository> { GroupRepositoryImpl(get(), get()) }
  single { GroupService(get()) }
}
