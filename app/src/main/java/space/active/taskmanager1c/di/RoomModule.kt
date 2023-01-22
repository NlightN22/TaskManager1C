package space.active.taskmanager1c.di

import android.app.Application
import androidx.room.Room
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.local.db.tasks_room_db.*
import space.active.taskmanager1c.data.utils.GsonParserImpl
import space.active.taskmanager1c.data.utils.JsonParser
import javax.inject.Singleton

private const val TAG = "RoomModule"
private const val DB_NAME = "tasks_db"

@Module
@InstallIn(SingletonComponent::class)
class RoomModule {

    @Provides
    @Singleton
    fun provideSettingsDao(db: TaskWithUsersDatabase): SettingsDao = db.settingsDao

    @Provides
    @Singleton
    fun provideTaskReadingDao(db: TaskWithUsersDatabase): TaskReadingDao = db.readingDao

    @Provides
    @Singleton
    fun provideTaskInputDao(db: TaskWithUsersDatabase): TaskInputDao = db.inputDao

    @Provides
    @Singleton
    fun provideTaskOutputDao(db: TaskWithUsersDatabase): TaskOutputDao = db.outputDao

    @Provides
    @Singleton
    fun provideTaskWithUsersDatabase(
        app: Application,
        converters: Converters,
    ): TaskWithUsersDatabase =
        Room.databaseBuilder(
            app,
            TaskWithUsersDatabase::class.java,
            DB_NAME
        )
            .fallbackToDestructiveMigration()
            .addTypeConverter(converters)
            .build()

    @Provides
    @Singleton
    fun provideJsonParser(): JsonParser = GsonParserImpl(Gson())

    @Provides
    @Singleton
    fun provideConverters(
        jsonParser: JsonParser
    ) = Converters(jsonParser)

}