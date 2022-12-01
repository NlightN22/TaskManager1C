package space.active.taskmanager1c.di

import android.app.Application
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.data.repository.TaskApi
import space.active.taskmanager1c.data.remote.TaskApiImpl
import space.active.taskmanager1c.data.remote.test.GetFromFile
import space.active.taskmanager1c.data.utils.GsonParserImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TaskModule {



}