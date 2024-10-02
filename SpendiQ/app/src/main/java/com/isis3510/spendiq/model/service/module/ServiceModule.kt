package com.isis3510.spendiq.model.service.module

import com.isis3510.spendiq.model.service.AccountService
import com.isis3510.spendiq.model.service.impl.LogInServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds abstract fun provideAccountService(imp: LogInServiceImpl): AccountService
}