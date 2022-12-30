package space.active.taskmanager1c.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.coreutils.CryptoManager
import space.active.taskmanager1c.domain.models.UserSettings
import space.active.taskmanager1c.domain.models.UserSettingsSerializer

private val Context.dataStore by dataStore(
    "user-settings.json",
    serializer = UserSettingsSerializer(CryptoManager())
)

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {

    @Provides
    fun providesDataStoreRepository(
        application: Application,
    ): DataStore<UserSettings> = application.dataStore
}