package com.example.tiendita.di

import com.example.tiendita.auth.AccountProvisioningRepository
import com.example.tiendita.repository.*
import com.example.tiendita.session.SessionActions
interface AppDependencies {
    val provisioning: AccountProvisioningRepository
    val session: SessionActions
    val bootstrap: BootstrapRepository
    val status: DatabaseStatusRepository
    val directories: DirectoryRepository
}
