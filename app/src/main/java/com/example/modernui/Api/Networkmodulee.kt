package com.example.modernui.Api
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Networkmodulee {
    @Provides
    @Singleton
    @PlaceholderApi
    fun provideApiService(): ApiService {
        return RetrofitInstance.apii
    }
}