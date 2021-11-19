package com.example.isthisahangout.ui.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.isthisahangout.MainActivity
import com.example.isthisahangout.R
import com.example.isthisahangout.databinding.FragmentRegistrationBinding
import com.example.isthisahangout.viewmodel.FirebaseAuthViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Named


@AndroidEntryPoint
class RegistrationFragment : Fragment(R.layout.fragment_registration) {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    @Inject
    @Named("UserRef")
    lateinit var userRef: DatabaseReference

    @Inject
    lateinit var mAuth: FirebaseAuth
    private val viewModel by activityViewModels<FirebaseAuthViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistrationBinding.bind(view)
        binding.apply {
            emailEditText.editText!!.setText(viewModel.registrationEmail)
            passwordEditText.editText!!.setText(viewModel.registrationPassword)
            usernameEditText.editText!!.setText(viewModel.registrationUsername)
            emailEditText.editText!!.addTextChangedListener { email ->
                Log.e("Register", email.toString())
                viewModel.registrationEmail = email.toString()
            }
            passwordEditText.editText!!.addTextChangedListener { password ->
                viewModel.registrationPassword = password.toString()
            }
            usernameEditText.editText!!.addTextChangedListener { username ->
                viewModel.registrationUsername = username.toString()
            }
            alreadyHaveAccountTextView.setOnClickListener {
                findNavController()
                    .navigate(
                        RegistrationFragmentDirections
                            .actionRegistrationFragmentToLoginFragment()
                    )
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.registrationFlow.collect { event ->
                    when (event) {
                        is FirebaseAuthViewModel.AuthEvent.RegistrationSuccess -> {
                            Snackbar.make(
                                requireView(),
                                event.message,
                                Snackbar.LENGTH_SHORT
                            ).show()
                            MainActivity.userId = mAuth.currentUser!!.uid
                            userRef.child(MainActivity.userId!!).child("userName")
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        MainActivity.username = snapshot.value.toString()
                                        findNavController().navigate(
                                            RegistrationFragmentDirections
                                                .actionRegistrationFragmentToHomeFragment2()
                                        )
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        mAuth.signOut()
                                        throw error.toException()
                                    }
                                })
                        }
                        is FirebaseAuthViewModel.AuthEvent.RegistrationFailure -> {
                            Snackbar.make(
                                requireView(),
                                "Error: ${event.message}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            progressBar.isVisible = false
                        }
                        else -> Unit
                    }
                }
            }
            registerButton.setOnClickListener {
                progressBar.isVisible = true
                hideKeyboard(requireContext())
                viewModel.onRegistrationClick()
            }
        }
    }

    private fun hideKeyboard(mContext: Context) {
        val imm = mContext
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            requireActivity().window
                .currentFocus!!.windowToken, 0
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}