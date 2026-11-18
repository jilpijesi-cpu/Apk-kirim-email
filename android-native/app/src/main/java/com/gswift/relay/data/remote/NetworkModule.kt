package com.gswift.relay.data.remote

import com.google.gson.Gson
import com.gswift.relay.BuildConfig
import com.gswift.relay.data.local.SecureStorageManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun createOkHttpClient(storage: SecureStorageManager): OkHttpClient {
        val dynamicHostInterceptor = Interceptor { chain ->
            val original = chain.request()
            val targetBase = storage.loadServerBaseUrl().toHttpUrlOrNull()

            val rewrittenRequest = if (targetBase != null && original.url.encodedPath.startsWith("/api/")) {
                val baseSegments = targetBase.encodedPath.trimEnd('/')
                val newUrl = original.url.newBuilder()
                    .scheme(targetBase.scheme)
                    .host(targetBase.host)
                    .port(targetBase.port)
                    .encodedPath("$baseSegments${original.url.encodedPath}")
                    .build()
                original.newBuilder()
                    .url(newUrl)
                    .header("Accept", "application/json")
                    .build()
            } else {
                original.newBuilder()
                    .header("Accept", "application/json")
                    .build()
            }

            val response = chain.proceed(rewrittenRequest)

            // Guard against AI Studio ais-dev __cookie_check.html / HTML gateway responses
            val contentType = response.header("Content-Type").orEmpty().lowercase()
            val locationHeader = response.header("Location").orEmpty()
            val isRedirectToAuth = response.code in 300..399 &&
                (locationHeader.contains("cookie_check", true) || locationHeader.contains("accounts.google", true))

            if (isRedirectToAuth) {
                response.close()
                return@Interceptor buildJsonErrorResponse(
                    rewrittenRequest,
                    401,
                    "Server '${rewrittenRequest.url.host}' meminta login browser (__cookie_check). Deploy aplikasi atau masukkan URL server publik Anda di tab Akun -> Server Relay API."
                )
            }

            val bodyString = response.peekBody(4096).string()
            val looksLikeHtml = contentType.contains("text/html") ||
                bodyString.trimStart().startsWith("<!DOCTYPE", ignoreCase = true) ||
                bodyString.trimStart().startsWith("<html", ignoreCase = true)

            if (looksLikeHtml) {
                response.close()
                val msg = if (bodyString.contains("cookie_check", true) || rewrittenRequest.url.host.startsWith("ais-dev-")) {
                    "URL dev AI Studio (${rewrittenRequest.url.host}) memblokir akses API tanpa cookie browser. Silakan Deploy/Publish server Anda dan ubah URL Server Relay di tab Akun."
                } else {
                    "Server (${rewrittenRequest.url.host}) mengembalikan halaman HTML (HTTP ${response.code}), bukan API JSON."
                }
                return@Interceptor buildJsonErrorResponse(rewrittenRequest, 502, msg)
            }

            response
        }

        val builder = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(65, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(dynamicHostInterceptor)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }
        return builder.build()
    }

    private fun buildJsonErrorResponse(
        request: okhttp3.Request,
        code: Int,
        message: String
    ): Response {
        val payload = gson.toJson(mapOf("success" to false, "connected" to false, "error" to message))
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .header("Content-Type", "application/json; charset=utf-8")
            .body(payload.toResponseBody(jsonMediaType))
            .build()
    }

    fun createApiService(client: OkHttpClient): GSwiftApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GSwiftApiService::class.java)
    }
}
