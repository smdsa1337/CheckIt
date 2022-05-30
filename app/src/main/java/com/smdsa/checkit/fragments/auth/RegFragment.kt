package com.smdsa.checkit.fragments.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.smdsa.checkit.R
import com.smdsa.checkit.adapters.RegDataClass
import com.smdsa.checkit.databinding.FragmentRegBinding

class RegFragment : Fragment() {

    private lateinit var binding: FragmentRegBinding
    private lateinit var auth: FirebaseAuth
    private var database : DatabaseReference = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegBinding.inflate(inflater)
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.regButton.setOnClickListener {
            if(binding.login.text.toString().isNotEmpty() && binding.password.text.toString().isNotEmpty()
                && binding.name.text.toString().isNotEmpty() && binding.middleName.text.toString().isNotEmpty()
                && binding.lastName.text.toString().isNotEmpty()){
                auth.createUserWithEmailAndPassword(binding.login.text.toString(),binding.login.text.toString()).addOnCompleteListener { it1 ->
                    if(it1.isSuccessful){
                        val regDataClass = RegDataClass(
                            LastName = binding.lastName.text.toString(),
                            MiddleName = binding.middleName.text.toString(),
                            Name = binding.name.text.toString(),
                            Role = "Пользователь"
                        )
                        database.child(auth.uid.toString()).setValue(regDataClass).addOnCompleteListener { it2 ->
                            if(it2.isSuccessful){
                                findNavController().navigate(R.id.action_regFragment_to_listOfTasksFragment)
                            }
                            else{
                                auth.currentUser?.delete()
                            }
                        }
                    }
                    else{
                        when(it1.exception.toString()){
                            "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The email address is badly formatted." ->
                                Toast.makeText(activity,"Неправильно набрана почта", Toast.LENGTH_SHORT).show()

                            "com.google.firebase.auth.FirebaseAuthWeakPasswordException: The given password is invalid. [ Password should be at least 6 characters ]" ->
                                Toast.makeText(activity,"Неподходящий пароль", Toast.LENGTH_SHORT).show()

                            "com.google.firebase.auth.FirebaseAuthUserCollisionException: The email address is already in use by another account." ->
                                Toast.makeText(activity,"Данная почта уже используется", Toast.LENGTH_SHORT).show()

                            else -> Toast.makeText(activity,"Проверьте подключение к интернету и повторите попытку позже", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            else{
                Toast.makeText(activity,"Вы оставили какое-то поле пустым", Toast.LENGTH_SHORT).show()
            }
        }
    }
}