package space.active.taskmanager1c.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val BASE_URL = "https://taskmgr.komponent-m.ru/torg_develop/hs/taskmgr/"

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(provideMoshi()))
        .client(provideOkHttpClient())
        .baseUrl(BASE_URL)
        .build()

    fun provideMoshi(): Moshi =
        Moshi.Builder().build() // todo replace by Kotlin Serialization Library

    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(provideLogginInterceptor())
            .build()

    fun provideLogginInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.NONE)

}