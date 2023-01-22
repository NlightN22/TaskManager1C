package space.active.taskmanager1c.di

import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetrofitProvider @AssistedInject constructor(
    @Assisted private val serverAddress: String
) {

    val retrofit: Retrofit by lazy {
        createRetrofit(serverAddress)
    }

    private val moshi = Moshi.Builder().build()

    private fun createRetrofit(serverAddress: String): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(provideOkHttpClient())
            .baseUrl(serverAddress)
            .build()

    private fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient().newBuilder()
            .addInterceptor(provideLogginInterceptor())
            .build()

    private fun provideLogginInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.NONE)
}

@AssistedFactory
interface RetrofitProviderFactory {
    fun create(serverAddress: String): RetrofitProvider
}