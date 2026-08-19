package com.rahmat.testapp.di

import com.rahmat.testapp.data.repository.AuthRepositoryImpl
import com.rahmat.testapp.data.repository.MenuRepositoryImpl
import com.rahmat.testapp.data.repository.NotificationRepositoryImpl
import com.rahmat.testapp.data.repository.OrderRepositoryImpl
import com.rahmat.testapp.domain.repository.AuthRepository
import com.rahmat.testapp.domain.repository.MenuRepository
import com.rahmat.testapp.domain.repository.NotificationRepository
import com.rahmat.testapp.domain.repository.OrderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindMenuRepository(
        menuRepositoryImpl: MenuRepositoryImpl
    ): MenuRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository

}