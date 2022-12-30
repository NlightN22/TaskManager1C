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
import space.active.taskmanager1c.data.local.db.retrofit.BasicAuthInterceptor

//todo delete
private const val BASE_URL = "http://172.16.17.242/torg_develop/hs/taskmgr/"

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .build()

    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder().build() // todo replace by Kotlin Serialization Library

    @Provides
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient  = OkHttpClient().newBuilder()
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    fun provideLogginInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
    .setLevel(HttpLoggingInterceptor.Level.NONE)
}