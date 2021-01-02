package com.example.droidhub.screens

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.droidhub.R
import com.example.droidhub.databinding.FragmentLoginBinding
import com.example.droidhub.util.InputsValidation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginFragment : Fragment(R.layout.fragment_login) {

    companion object {
        private const val TAG = "LoginFragment"
    }

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            Toast.makeText(
                requireContext(),
                "Welcome back, ${currentUser.displayName}",
                Toast.LENGTH_LONG
            ).show()
            findNavController().navigate(R.id.action_loginFragment_to_filesFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        auth = FirebaseAuth.getInstance()

        binding.editEmail.addTextChangedListener(InputsValidation(binding.emailLayout))
        binding.editPassword.addTextChangedListener(InputsValidation(binding.passwordLayout))
        binding.textForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
        binding.buttonLogin.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonLogin.isEnabled = false
            binding.textForgotPassword.isEnabled = false
            binding.textSignUp.isEnabled = false
            loginUser()
        }

        binding.textSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }

    private fun loginUser() {
        when {
            binding.editEmail.text.toString().isEmpty() -> {
                binding.emailLayout.error = getString(R.string.field_required)
                binding.editEmail.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                binding.buttonLogin.isEnabled = true
                binding.textForgotPassword.isEnabled = true
                binding.textSignUp.isEnabled = true
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(binding.editEmail.text.toString()).matches() -> {
                binding.emailLayout.error = getString(R.string.invalid_email)
                binding.editEmail.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                binding.buttonLogin.isEnabled = true
                binding.textForgotPassword.isEnabled = true
                binding.textSignUp.isEnabled = true
                return
            }
            else -> {
                binding.emailLayout.error = null
            }
        }

        when {
            binding.editPassword.text.toString().isEmpty() -> {
                binding.passwordLayout.error = getString(R.string.field_required)
                binding.editPassword.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                binding.buttonLogin.isEnabled = true
                binding.textForgotPassword.isEnabled = true
                binding.textSignUp.isEnabled = true
                return
            }
            binding.editPassword.text.toString().length < 6 -> {
                binding.passwordLayout.error = getString(R.string.password_length_error)
                binding.editPassword.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                binding.buttonLogin.isEnabled = true
                binding.textForgotPassword.isEnabled = true
                binding.textSignUp.isEnabled = true
                return
            }
            else -> {
                binding.passwordLayout.error = null
            }
        }

        auth.signInWithEmailAndPassword(
            binding.editEmail.text.toString(),
            binding.editPassword.text.toString()
        )
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.buttonLogin.isEnabled = true
                    binding.textForgotPassword.isEnabled = true
                    binding.textSignUp.isEnabled = true

                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        if (currentUser.isEmailVerified) {
                            Toast.makeText(
                                requireContext(), "Login Successful.",
                                Toast.LENGTH_LONG
                            ).show()
                            findNavController().navigate(R.id.action_loginFragment_to_filesFragment)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Your email has not been verified. \nKindly check your inbox for the mail to verify your email",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    if (task.exception is FirebaseAuthInvalidUserException) {
                        Toast.makeText(
                            requireContext(), "User does not exist.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            requireContext(), "Authentication failed.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.buttonLogin.isEnabled = true
                    binding.textForgotPassword.isEnabled = true
                    binding.textSignUp.isEnabled = true
                }
            }
    }
}