package com.example.modernui.Api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlaceholderApi

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @MainApi
    fun provideApiService(): ApiService {
        return RetrofitInstance.api
    }
}
