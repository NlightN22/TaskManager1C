package space.active.taskmanager1c.data.remote

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import javax.inject.Inject

class RetrofitConfig @Inject constructor(
    val retrofit: Retrofit,
    val moshi: Moshi
        )