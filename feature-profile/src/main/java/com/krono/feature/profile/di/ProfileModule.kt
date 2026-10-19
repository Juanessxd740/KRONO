package com.krono.feature.profile.di

import com.krono.feature.profile.data.FirestoreProfileDataSource
import com.krono.feature.profile.data.ProfileRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de inyección de dependencias para el módulo `:feature-profile`.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindProfileRemoteDataSource(
        impl: FirestoreProfileDataSource,
    ): ProfileRemoteDataSource
}
