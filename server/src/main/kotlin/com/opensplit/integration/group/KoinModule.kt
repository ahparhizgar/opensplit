package com.opensplit.integration.group

import org.koin.dsl.module

fun groupKoinModule() = module {
  single<GroupRepository> { GroupRepositoryImpl(get(), get()) }
  single { GroupService(get()) }
}
