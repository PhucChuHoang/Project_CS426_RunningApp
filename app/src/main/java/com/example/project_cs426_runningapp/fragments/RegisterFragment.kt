package com.example.project_cs426_runningapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_cs426_runningapp.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerScreenRegisterButton.setOnClickListener {
            handleRegisterUser()
        }
        binding.registerScreenReturnButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleRegisterUser() {
        val fullname = binding.registerScreenFullNameEditText.text.toString()
        val email = binding.registerScreenEmailEditText.text.toString()
        val password = binding.registerScreenPasswordEditText.text.toString()

        when {
            fullname.isEmpty() -> createDialog("Registration Error!!!", "Please enter your full name.")
            email.isEmpty() -> createDialog("Registration Error!!!", "Please enter your email.")
            password.isEmpty() -> createDialog("Registration Error!!!", "Please enter your password.")
            password.length < 6 -> createDialog("Registration Error!!!", "Password must be at least 6 characters.")
            else -> registerUser(fullname, email, password)
        }
    }

    private fun registerUser(fullname: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()
                    val user = hashMapOf(
                        "fullname" to fullname,
                        "email" to email,
                        "password" to password,
                        "phone" to "",
                        "country" to "",
                        "sex" to "",
                        "address" to "",
                    )
                    db.collection("users")
                        .document(email)
                        .set(user)

                    // Initialize activity data
                    val activity = hashMapOf(
                        "ac_id" to "activity1",
                        "distance" to 0,
                        "screenShot" to 0,
                        "duration" to 0,
                        "calories" to 0,
                    )
                    db.collection("users")
                        .document(email)
                        .collection("activities")
                        .document("activity1")
                        .set(activity)

                    // Save user's name to SharedPreferences
                    CoroutineScope(Dispatchers.Main).launch {
                        saveUserName(fullname)
                    }

                    createDialog("Registration Successful!", "Please check your email for verification.")
                } else {
                    createDialog("Registration Error!!!", "Email has been used.")
                }
            }
    }

    private fun createDialog(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                if (title == "Registration Successful!")
                    findNavController().popBackStack()
            }
        alertDialogBuilder.show()
        hideKeyboard()
        clearPasswordAndFocus()
    }

    private fun clearPasswordAndFocus() {
        binding.registerScreenEmailEditText.clearFocus()
        binding.registerScreenFullNameEditText.clearFocus()
        binding.registerScreenPasswordEditText.clearFocus()
        binding.registerScreenPasswordEditText.text?.clear()
    }

    private fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val imm =
                requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun saveUserName(fullname: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("sharedPrefs", 0)
        val editor = sharedPreferences.edit()
        editor.putString("name", fullname)
        editor.apply()
    }
}