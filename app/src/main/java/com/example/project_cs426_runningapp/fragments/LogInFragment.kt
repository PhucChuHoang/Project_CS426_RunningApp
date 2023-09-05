package com.example.project_cs426_runningapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.databinding.FragmentLogInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private var isNavigationInProgress = false // Add this flag
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.loginScreenLoginButton -> {
                    val email = binding.loginScreenEmailEditText.text.toString()
                    val password = binding.loginScreenPasswordEditText.text.toString()

                    val storageReference = Firebase.storage("gs://cs426-project.appspot.com").reference

                    var profileRef = storageReference.child("images/" + email + "_profile.jpg")

                    val localFilePath = File(requireContext().filesDir, "local_image.jpg").absolutePath

                    // Create parent directories if they don't exist
                    val parentDir = File(localFilePath).parentFile
                    if (!parentDir.exists()) {
                        parentDir.mkdirs()
                    }
                    else if (binding.loginScreenPasswordEditText.text.toString().isEmpty()) {
                        createDialog("Login Error!!!", "Please enter your password.")
                        return@OnClickListener
                    }
                    if (!isNavigationInProgress) { // Check if navigation is in progress
                        isNavigationInProgress = true // Set the flag to true
                        val email = binding.loginScreenEmailEditText.text.toString()
                        val password = binding.loginScreenPasswordEditText.text.toString()
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(requireActivity()) { task ->
                                val view = requireActivity().currentFocus
                                if (view != null) {
                                    val imm =
                                        requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                                }
                                if (task.isSuccessful) {
                                    if (!auth.currentUser!!.isEmailVerified) {
                                        createDialog("Login Error!!!", "Please verify your email.")
                                        auth.signOut()
                                        binding.loginScreenPasswordEditText.clearFocus()
                                        binding.loginScreenPasswordEditText.text?.clear()
                                        isNavigationInProgress = false // Reset the flag
                                        return@addOnCompleteListener
                                    }
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
                                    val view = requireActivity().currentFocus
                                    if (view != null) {
                                        val imm =
                                            requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                                    }
                                    createDialog("Login Error!!!", "Email or password is incorrect.")
                                    binding.loginScreenPasswordEditText.clearFocus()
                                    binding.loginScreenPasswordEditText.text?.clear()
                                    isNavigationInProgress = false // Reset the flag
                                }
                            }
                    }
                }
                binding.loginScreenReturnButton -> {
                    findNavController().popBackStack()
                }
                binding.forgotPasswordButton -> {
                    findNavController().navigate(R.id.action_logInFragment_to_forgotPasswordFragment)
                }
            }
        }
        binding.loginScreenLoginButton.setOnClickListener(clickListener)
        binding.loginScreenReturnButton.setOnClickListener(clickListener)
        binding.forgotPasswordButton.setOnClickListener(clickListener)
    }

    private fun createDialog(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->

            }
        alertDialogBuilder.show()
    }
}