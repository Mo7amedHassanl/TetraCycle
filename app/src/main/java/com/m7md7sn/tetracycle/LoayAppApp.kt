package com.m7md7sn.tetracycle

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LoayAppApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true) // Enable offline capabilities
        } catch (e: Exception) {
            // Persistence can only be enabled once, if this is called twice it will throw an exception
            // Just catch and ignore
        }
    }
}