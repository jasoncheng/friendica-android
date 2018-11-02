package link.mawa.android.util

import android.util.Log
import com.google.gson.GsonBuilder
import link.mawa.android.bean.Profile
import link.mawa.android.bean.Status
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {

    @GET("statuses/public_timeline?count=5")
    fun statusPublicTimeline(@Query("since_id") since_id: String,
                             @Query("max_id") max_id: String): Call<List<Status>>

    @GET("friendica/profile/show")
    fun friendicaProfileShow(): Call<Profile>

    companion object Factory {
        private val client: OkHttpClient
        get() {
            Log.i("ApiService", "=========> ${PrefUtil.getUsername()}, ${PrefUtil.getPassword()}")
            val clientBuilder = OkHttpClient.Builder()
            val authToken = Credentials.basic(PrefUtil.getUsername(), PrefUtil.getPassword())
            val headerAuthorizationInterceptor = Interceptor { chain ->
                var request = chain.request()
                val headers = request.headers().newBuilder().add("Authorization", authToken).build()
                request = request.newBuilder().headers(headers).build()
                chain.proceed(request)
            }
            clientBuilder.addInterceptor(headerAuthorizationInterceptor)
            return clientBuilder.build()
        }

        fun create(): ApiService {
            val gson = GsonBuilder().serializeNulls().create()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl("${PrefUtil.getApiUrl()}/api/")
                .client(client)
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}