package com.smdsa.checkit.fragments.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smdsa.checkit.R
import com.smdsa.checkit.databinding.FragmentAuthBinding

class AuthFragment : Fragment() {

    private lateinit var binding: FragmentAuthBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthBinding.inflate(inflater)
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.authButton.setOnClickListener {
            if(binding.login.text.toString().isNotEmpty() && binding.login.text.toString().isNotEmpty()){
                auth.signInWithEmailAndPassword(binding.login.text.toString(),binding.login.text.toString()).addOnCompleteListener {
                    if(it.isSuccessful){
                        findNavController().navigate(R.id.action_authFragment_to_listOfTasksFragment)
                    }
                    else{
                        when(it.exception.toString()){
                            "com.google.firebase.auth.FirebaseAuthInvalidUserException: There is no user record corresponding to this identifier. The user may have been deleted." ->
                                Toast.makeText(activity,"Аккаунт не найден", Toast.LENGTH_SHORT).show()

                            "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The email address is badly formatted." ->
                                    Toast.makeText(activity,"Неверный логин или пароль", Toast.LENGTH_SHORT).show()

                            "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The password is invalid or the user does not have a password." ->
                                    Toast.makeText(activity,"Неверный логин или пароль", Toast.LENGTH_SHORT).show()

                            else -> Toast.makeText(activity,"Проверьте подключение к интернету и повторите попытку позже", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            else{
                Toast.makeText(activity,"Вы оставили какое-то поле пустым", Toast.LENGTH_SHORT).show()
            }
        }
        binding.textToReg.setOnClickListener {
            findNavController().navigate(R.id.action_authFragment_to_regFragment)
        }
    }
}