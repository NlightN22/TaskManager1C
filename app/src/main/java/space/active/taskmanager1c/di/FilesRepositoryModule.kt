package space.active.taskmanager1c.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Retrofit
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.cache_storage.CachedFilesRepository
import space.active.taskmanager1c.data.local.cache_storage.CachedFilesRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
class FilesRepositoryModule {

    @Provides
    @ViewModelScoped
    fun provideFilesRepository(
        context: Application,
        retrofit: Retrofit,
        logger: Logger,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): CachedFilesRepository = CachedFilesRepositoryImpl(context, retrofit, logger, ioDispatcher)
}