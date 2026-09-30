package com.example.tiendita.auth

interface AccountProvisioningRepository {
    suspend fun createInitialAdmin(request: InitialAdminRequest): AccountProvisioningResult
    suspend fun createAccount(actorAccountId: Long, request: AccountRegistrationRequest): AccountProvisioningResult
}
