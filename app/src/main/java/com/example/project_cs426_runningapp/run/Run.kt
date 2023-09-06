package com.example.project_cs426_runningapp.run

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class Run(
    var ID : Long,
    var Img: Bitmap,
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

        val storage = Firebase.storage("gs://cs426-project.appspot.com")
        var storageRef = storage.reference
        var runImageRef = storageRef.child("runs/" + email + "_" + "$ID" + ".PNG")
        Log.d("updateImg", "successful")
        var uploadTask = runImageRef.putBytes(fromBitmap(Img))
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            Log.d("Image run upload", "It's up bro")
        }
    }
    fun fromBitmap(bmp: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}
