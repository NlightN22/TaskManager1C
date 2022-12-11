package space.active.taskmanager1c.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.presentation.utils.Toasts
import space.active.taskmanager1c.presentation.utils.ToastsImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainActivityModule {

    @Provides
    @Singleton
    fun provideToasts( app: Application): Toasts = ToastsImpl(app)
}