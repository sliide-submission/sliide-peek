package com.sliide.useractivity.di

import app.cash.sqldelight.db.SqlDriver
import com.sliide.useractivity.data.auth.BuildSecretsBearerTokenProvider
import com.sliide.useractivity.data.local.DatabaseDriverFactory
import com.sliide.useractivity.data.local.SliidePeekDatabase
import com.sliide.useractivity.data.local.SqlDelightUserCacheDataSource
import com.sliide.useractivity.data.local.UserCacheDataSource
import com.sliide.useractivity.data.remote.GorestApiClient
import com.sliide.useractivity.data.remote.createGorestHttpClient
import com.sliide.useractivity.data.repository.UserRepositoryImpl
import com.sliide.useractivity.domain.auth.BearerTokenProvider
import com.sliide.useractivity.domain.connectivity.ConnectivityMonitor
import com.sliide.useractivity.domain.repository.UserRepository
import com.sliide.useractivity.domain.time.AppClock
import com.sliide.useractivity.domain.time.SystemAppClock
import com.sliide.useractivity.presentation.users.CreateUserUseCase
import com.sliide.useractivity.presentation.users.CreateUserUseCaseImpl
import com.sliide.useractivity.presentation.users.DeleteUserUseCase
import com.sliide.useractivity.presentation.users.DeleteUserUseCaseImpl
import com.sliide.useractivity.presentation.users.GetSmartUserFeedUseCase
import com.sliide.useractivity.presentation.users.LoadOlderUsersUseCase
import com.sliide.useractivity.presentation.users.LoadOlderUsersUseCaseImpl
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
    single<BearerTokenProvider> { BuildSecretsBearerTokenProvider() }
    single<SqlDriver> { databaseDriverFactory.createDriver() }
    single { SliidePeekDatabase(get()) }
    single<UserCacheDataSource> { SqlDelightUserCacheDataSource(get()) }
    single<ConnectivityMonitor> { databaseDriverFactory.createConnectivityMonitor() }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    factory<LoadUserFeedUseCase> {
        GetSmartUserFeedUseCase(
            userRepository = get(),
            userCacheDataSource = get(),
            clock = get(),
        )
    }
    factory<LoadOlderUsersUseCase> {
        LoadOlderUsersUseCaseImpl(
            userRepository = get(),
            userCacheDataSource = get(),
            clock = get(),
        )
    }
    factory<CreateUserUseCase> {
        CreateUserUseCaseImpl(
            userRepository = get(),
            userCacheDataSource = get(),
            clock = get(),
        )
    }
    factory<DeleteUserUseCase> {
        DeleteUserUseCaseImpl(
            userRepository = get(),
            userCacheDataSource = get(),
        )
    }
    factory { params ->
        UserFeedViewModel(
            loadUserFeed = get(),
            loadOlderUsers = get(),
            createUser = get(),
            deleteUser = get(),
            scope = params.get<CoroutineScope>(),
            clock = get(),
            connectivityMonitor = get(),
        )
    }
}
