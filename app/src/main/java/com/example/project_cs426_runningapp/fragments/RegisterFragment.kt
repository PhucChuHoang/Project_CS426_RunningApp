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

        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.registerScreenRegisterButton -> {
                    val fullname = binding.registerScreenFullNameEditText.text.toString()
                    val email = binding.registerScreenEmailEditText.text.toString()
                    val password = binding.registerScreenPasswordEditText.text.toString()
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            val view = requireActivity().currentFocus
                            if (view != null) {
                                val imm =
                                    requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                                imm.hideSoftInputFromWindow(view.windowToken, 0)
                            }
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
                                // TODO: Move this activity code to start place
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
                                    .document("activity1")  //TODO: change to proper activity id
                                    .set(activity)
                                CoroutineScope(Dispatchers.Main).launch {
                                    val name = fullname
                                    val sharedPreferences = requireActivity().getSharedPreferences(
                                        "sharedPrefs",
                                        0
                                    )
                                    val editor = sharedPreferences.edit()
                                    editor.putString("name", name)
                                    editor.apply()
                                }
                                val alertDialogBuilder = AlertDialog.Builder(requireContext())
                                alertDialogBuilder.setTitle("Registration Successful!")
                                    .setMessage("Please check your email to verify your account.")
                                    .setPositiveButton("OK") { _, _ ->
                                        findNavController().popBackStack()
                                    }
                                alertDialogBuilder.show()
                            } else {
                                val alertDialogBuilder = AlertDialog.Builder(requireContext())
                                alertDialogBuilder.setTitle("Registration Error!!!")
                                    .setMessage("Email is already in use.")
                                    .setPositiveButton("OK") { _, _ ->
                                        binding.registerScreenPasswordEditText.clearFocus()
                                        binding.registerScreenPasswordEditText.text?.clear()
                                    }
                                alertDialogBuilder.show()
                            }
                        }
                }
                binding.registerScreenReturnButton -> {
                    findNavController().popBackStack()
                }
            }
        }
        binding.registerScreenRegisterButton.setOnClickListener(clickListener)
        binding.registerScreenReturnButton.setOnClickListener(clickListener)
    }

}