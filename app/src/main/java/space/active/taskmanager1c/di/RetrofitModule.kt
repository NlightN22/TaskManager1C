package space.active.taskmanager1c.di

import com.google.gson.Gson
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

/**
 * Used in FragmentDeepLinks too
 *  With "/" at the end
 */
const val BASE_URL = "https://taskmgr.komponent-m.ru/"

/**
 *  Any string of start path after URL host.
 *  Example "your_db_name/hs/taskmgr/"
 *  With "/" at the end
 */
const val START_PATH = "taskmgr/hs/taskmgr/"

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(provideMoshi()))
        .client(provideOkHttpClient())
        .baseUrl(BASE_URL + START_PATH)
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

    @Provides
    fun provideGson(): Gson = Gson()

}