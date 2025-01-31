package com.example.keyguardian

object SensitiveDataStore {
    private var documentPassword: String? = null

    fun setPassword(password: String) {
        documentPassword = password
    }

    fun getPassword(): String? {
        return documentPassword
    }

    fun clearPassword() {
        documentPassword = null
    }
}
