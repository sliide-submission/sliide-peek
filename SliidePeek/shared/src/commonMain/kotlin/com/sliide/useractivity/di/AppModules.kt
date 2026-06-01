package com.sliide.useractivity.di

import app.cash.sqldelight.db.SqlDriver
import com.sliide.useractivity.data.local.DatabaseDriverFactory
import com.sliide.useractivity.data.local.SliidePeekDatabase
import com.sliide.useractivity.data.local.SqlDelightUserCacheDataSource
import com.sliide.useractivity.data.local.UserCacheDataSource
import com.sliide.useractivity.data.remote.GorestApiClient
import com.sliide.useractivity.data.remote.createGorestHttpClient
import com.sliide.useractivity.data.repository.PostRepositoryImpl
import com.sliide.useractivity.data.repository.TodoRepositoryImpl
import com.sliide.useractivity.data.repository.UserRepositoryImpl
import com.sliide.useractivity.domain.repository.PostRepository
import com.sliide.useractivity.domain.repository.TodoRepository
import com.sliide.useractivity.domain.repository.UserRepository
import com.sliide.useractivity.domain.time.AppClock
import com.sliide.useractivity.domain.time.SystemAppClock
import com.sliide.useractivity.presentation.users.GetSmartUserFeedUseCase
import com.sliide.useractivity.presentation.users.LoadUserFeedUseCase
import com.sliide.useractivity.presentation.users.UserFeedViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.Module
import org.koin.dsl.module

fun appModule(databaseDriverFactory: DatabaseDriverFactory): Module = module {
    single<AppClock> { SystemAppClock() }
    single<HttpClient> { createGorestHttpClient() }
    single { GorestApiClient(get()) }
    single<SqlDriver> { databaseDriverFactory.createDriver() }
    single { SliidePeekDatabase(get()) }
    single<UserCacheDataSource> { SqlDelightUserCacheDataSource(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<PostRepository> { PostRepositoryImpl(get()) }
    single<TodoRepository> { TodoRepositoryImpl(get()) }
    factory<LoadUserFeedUseCase> {
        GetSmartUserFeedUseCase(
            userRepository = get(),
            userCacheDataSource = get(),
            clock = get(),
        )
    }
    factory { params ->
        UserFeedViewModel(
            loadUserFeed = get(),
            scope = params.get<CoroutineScope>(),
            clock = get(),
        )
    }
}
