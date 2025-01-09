package com.example.keyguardian

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("documents/get_user_documents")
    fun getUserDocuments(@Body requestBody: UserDocumentsRequest): Call<UserDocumentsResponse>

    @Headers("Content-Type: application/json")
    @POST("documents/get_document")
    fun getDocument(@Body requestBody: DocumentRequest): Call<DocumentResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/authenticate")
    fun authenticate(@Body requestBody: AuthRequest): Call<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/register")
    fun register(@Body requestBody: AuthRequest): Call<RegisterResponse>

    @Headers("Content-Type: application/json")
    @POST("documents/create_update_document")
    fun updateDocument(@Body requestBody: UpdateDocumentRequest): Call<String>

    @Headers("Content-Type: application/json")
    @POST("documents/delete_user_document")
    fun deleteDocument(@Body requestBody: DocumentRequest): Call<String>

    @Headers("Content-Type: application/json")
    @POST("documents/add_user_document")
    fun addDocument(@Body requestBody: DocumentRequest): Call<String>
}

data class UserDocumentsRequest(
    val user: String
)

data class DocumentRequest(
    val user: String,
    val document_name: String
)

data class UpdateDocumentRequest(
    val user: String,
    val document_name: String,
    val data: Map<String, String>
)

data class UserDocumentsResponse(
    val documents: List<String>
)

data class DocumentResponse(
    val response: Map<String, String>
)

data class AuthRequest(
    val email: String,
    val password: String
)

data class RegisterResponse(
    val message: String
)

data class AuthResponse(
    val token: String,
    val user_id: String
)
