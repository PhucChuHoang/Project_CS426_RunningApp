package com.example.project_cs426_runningapp.run

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Run(
    var ID : Long,
    var byteImgString: String,
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Double = 0.0,
    var distanceInMeters: Long = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Long = 0,
    var email : String? = null
){
    fun saveRun()
    {
        var db: FirebaseFirestore
        db = FirebaseFirestore.getInstance()
        val aRun = hashMapOf(
            "ID" to ID,
            "byteImgString" to byteImgString,
            "timestamp" to timestamp,
            "avgSpeedInKMH" to avgSpeedInKMH,
            "distanceInMeters" to distanceInMeters,
            "timeInMillis" to timeInMillis,
            "caloriesBurned" to caloriesBurned,
            "email" to email,
        )
// Add a new document with a generated ID
        db.collection("aRun")
            .document("$ID")
            .set(aRun)
            .addOnSuccessListener { documentReference ->
                Log.d("successonRunclass", "DocumentSnapshot added with ID: $ID")
            }
            .addOnFailureListener { e ->
                Log.w("failonRunclass", "Error adding document", e)
            }
    }
}
