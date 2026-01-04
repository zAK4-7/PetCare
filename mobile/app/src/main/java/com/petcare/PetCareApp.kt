package com.petcare

import android.app.Application
import androidx.room.Room
import com.petcare.data.local.database.PetCareDatabase
import com.petcare.data.repository.LocalDataRepository
import com.petcare.data.remote.ApiClient
import com.petcare.data.remote.TokenStore
import com.petcare.sync.SyncManager

class PetCareApp : Application() {

    lateinit var database: PetCareDatabase
        private set

    lateinit var localRepository: LocalDataRepository
        private set

    // Remote (Salma API)
    lateinit var tokenStore: TokenStore
        private set

    lateinit var syncManager: SyncManager
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            PetCareDatabase::class.java,
            "petcare_local.db"
        )
            .fallbackToDestructiveMigration()
            .build()

        localRepository = LocalDataRepository(database)

        // Remote integration
        tokenStore = TokenStore(applicationContext)
        val api = ApiClient.create(tokenStore)
        syncManager = SyncManager(api, localRepository)
    }
}
