package com.example.project_cs426_runningapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.project_cs426_runningapp.databinding.FragmentLogInBinding
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.loginScreenLoginButton -> {
                    val email = binding.loginScreenEmailEditText.text.toString()
                    val password = binding.loginScreenPasswordEditText.text.toString()
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    requireContext(),
                                    "Log in successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                CoroutineScope(Dispatchers.Main).launch {
                                    val name = db.collection("users")
                                        .document(email)
                                        .get()
                                        .await()
                                        .get("fullname")
                                        .toString()
                                    val sharedPreferences = requireActivity().getSharedPreferences(
                                        "sharedPrefs",
                                        0
                                    )
                                    val editor = sharedPreferences.edit()
                                    editor.putString("name", name)
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