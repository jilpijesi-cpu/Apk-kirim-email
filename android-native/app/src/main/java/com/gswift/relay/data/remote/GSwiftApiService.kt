package com.gswift.relay.data.remote

import com.gswift.relay.data.model.AiProvidersResponse
import com.gswift.relay.data.model.ChatAiRequest
import com.gswift.relay.data.model.ChatAiResponse
import com.gswift.relay.data.model.DomainLookupResponse
import com.gswift.relay.data.model.HealthResponse
import com.gswift.relay.data.model.KeyRotationResetResponse
import com.gswift.relay.data.model.KeyRotationStatusResponse
import com.gswift.relay.data.model.SendEmailRequest
import com.gswift.relay.data.model.SendEmailResponse
import com.gswift.relay.data.model.VerifySmtpRequest
import com.gswift.relay.data.model.VerifySmtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GSwiftApiService {

    @GET("api/health")
    suspend fun getHealth(): Response<HealthResponse>

    @GET("api/ai-providers")
    suspend fun getAiProviders(): Response<AiProvidersResponse>

    @GET("api/key-rotation-status")
    suspend fun getKeyRotationStatus(): Response<KeyRotationStatusResponse>

    @POST("api/key-rotation-reset")
    suspend fun resetKeyRotation(): Response<KeyRotationResetResponse>

    @GET("api/lookup-domain")
    suspend fun lookupDomain(
        @Query("domain") domain: String
    ): Response<DomainLookupResponse>

    @POST("api/send-email")
    suspend fun sendEmail(
        @Body request: SendEmailRequest
    ): Response<SendEmailResponse>

    @POST("api/verify-smtp")
    suspend fun verifySmtp(
        @Body request: VerifySmtpRequest
    ): Response<VerifySmtpResponse>

    @POST("api/chat-ai")
    suspend fun chatAi(
        @Body request: ChatAiRequest
    ): Response<ChatAiResponse>
}
