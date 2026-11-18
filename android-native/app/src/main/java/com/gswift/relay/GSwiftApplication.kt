package com.gswift.relay

import android.app.Application
import com.gswift.relay.data.local.SecureStorageManager
import com.gswift.relay.data.network.ConnectivityObserver
import com.gswift.relay.data.remote.NetworkModule
import com.gswift.relay.data.repository.GSwiftRepository

class GSwiftApplication : Application() {

    lateinit var repository: GSwiftRepository
        private set

    lateinit var connectivityObserver: ConnectivityObserver
        private set

    override fun onCreate() {
        super.onCreate()
        val okHttp = NetworkModule.createOkHttpClient()
        val apiService = NetworkModule.createApiService(okHttp)
        val storage = SecureStorageManager(applicationContext)
        repository = GSwiftRepository(apiService, storage)
        connectivityObserver = ConnectivityObserver(applicationContext)
    }
}
