package com.tlh.bluetooth.di

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Provides
    @Singleton
    fun provideParametersRepo(): ParametersRepo =
        ParametersRepoImpl()

    @Provides
    @Singleton
    fun provideDataCarrierRepo(): DataCarrierRepo =
        DataCarrierRepoImpl()


}