package com.example.project_cs426_runningapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.project_cs426_runningapp.databinding.FragmentLogInBinding
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import com.example.project_cs426_runningapp.R

class LogInFragment : Fragment() {
    private lateinit var binding: FragmentLogInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.loginScreenLoginButton -> {
                    val email = binding.loginScreenEmailEditText.text.toString()
                    val password = binding.loginScreenPasswordEditText.text.toString()

                    val storageReference = Firebase.storage.reference

                    var profileRef = storageReference.child("images/" + email + "_profile.jpg")

                    val localFilePath = File(requireContext().filesDir, "local_image.jpg").absolutePath

                    // Create parent directories if they don't exist
                    val parentDir = File(localFilePath).parentFile
                    if (!parentDir.exists()) {
                        parentDir.mkdirs()
                    }

                    val localFile = File(localFilePath)

                    if (localFile.exists()) {
                        // If it exists, delete it
                        localFile.delete()
                        Log.d("Login Delete","Image deleted")
                    }

                    profileRef.getFile(localFile)
                        .addOnSuccessListener { taskSnapshot ->

                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors that occurred during the download
                        }

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    requireContext(),
                                    "Log in successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                CoroutineScope(Dispatchers.Main).launch {
                                    val documentSnapshot = db.collection("users")
                                        .document(email)
                                        .get()
                                        .await()

                                    val fieldNames = listOf("fullname", "address", "country", "email", "password", "phone", "sex")
                                    val sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", 0)
                                    val editor = sharedPreferences.edit()

                                    for (fieldName in fieldNames) {
                                        val value = documentSnapshot.getString(fieldName)
                                        editor.putString(fieldName, value)
                                    }

                                    editor.apply()
                                    findNavController().navigate(R.id.action_logInFragment_to_homeFragment)
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Log in failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }
        binding.loginScreenLoginButton.setOnClickListener(clickListener)
    }
}