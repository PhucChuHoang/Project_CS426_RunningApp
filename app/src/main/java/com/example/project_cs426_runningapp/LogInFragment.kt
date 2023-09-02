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
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult

class LogInFragment : Fragment() {
    private lateinit var binding: FragmentLogInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callBackManager: CallbackManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        callBackManager = CallbackManager.Factory.create()
        binding.loginScreenFacebookButton.registerCallback(callBackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                findNavController().navigate(R.id.action_logInFragment_to_homeFragment)
            }

            override fun onCancel() {
                Toast.makeText(requireContext(), "Log in failed.", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(requireContext(), "Log in failed.", Toast.LENGTH_SHORT).show()
            }
        })
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
                                findNavController().navigate(R.id.action_logInFragment_to_homeFragment)
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