package com.example.project_cs426_runningapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.example.project_cs426_runningapp.R
import com.example.project_cs426_runningapp.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {
    private lateinit var binding: FragmentForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.fade)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val clickListener = View.OnClickListener { v ->
            when (v) {
                binding.forgotPasswordReturnButton -> {
                    findNavController().popBackStack()
                }
                binding.forgotPasswordSendEmailButton -> {
                    if (binding.resetEmailEditText.text.toString().isEmpty()) {
                        createDialog("Reset password error!!!", "Reset email must not be empty.")
                        return@OnClickListener
                    }
                    auth.sendPasswordResetEmail(binding.resetEmailEditText.text.toString())
                        .addOnCompleteListener { task ->
                            hideKeyboard()
                            if (task.isSuccessful) {
                                createDialog("Reset password successfully!!!", "Please check your email.")
                            } else {
                                createDialog("Reset password error!!!", "Email does not exist.")
                            }
                        }
                }
            }
        }
        binding.forgotPasswordReturnButton.setOnClickListener(clickListener)
        binding.forgotPasswordSendEmailButton.setOnClickListener(clickListener)
    }

    private fun createDialog(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                if (message != "Reset email must not be empty.") {
                    findNavController().popBackStack()
                }
            }
        alertDialogBuilder.show()
    }

    private fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val imm =
                requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}