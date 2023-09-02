package com.example.project_cs426_runningapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.findNavController
import com.example.project_cs426_runningapp.databinding.FragmentRegisterBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                            if (task.isSuccessful) {
                                val user = hashMapOf(
                                    "fullname" to fullname,
                                    "email" to email,
                                    "password" to password
                                )
                                db.collection("users")
                                    .document(email)
                                    .set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Register successfully.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Register failed.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                val bundle = Bundle()
                                bundle.putString("email", email)
                                findNavController().navigate(R.id.action_registerFragment_to_homeFragment, bundle)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Register failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }
        binding.registerScreenRegisterButton.setOnClickListener(clickListener)
    }
}