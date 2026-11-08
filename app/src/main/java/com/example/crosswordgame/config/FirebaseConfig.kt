package com.example.crosswordgame.config

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase

object FirebaseConfig {
    private const val TAG = "FirebaseConfig"

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy {
        Firebase.firestore.apply {
            firestoreSettings = firestoreSettings {
                setPersistenceEnabled(true)
            }
        }
    }

    object Collections {
        const val PLAYERS = "players"
    }

    fun init() {
        Log.d(TAG, "Firebase initialized: ${auth.app.options.projectId}")
    }
}