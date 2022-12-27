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
import space.active.taskmanager1c.data.remote.BasicAuthInterceptor

private const val BASE_URL = "http://172.16.17.242/torg_develop/hs/taskmgr/"
private const val API_USERNAME = "Михайлов Олег Федорович"
private const val API_PASSWORD = "test"

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
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient  = OkHttpClient().newBuilder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(BasicAuthInterceptor(API_USERNAME, API_PASSWORD))
        .build()

    @Provides
    fun provideLogginInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
    .setLevel(HttpLoggingInterceptor.Level.NONE)
}