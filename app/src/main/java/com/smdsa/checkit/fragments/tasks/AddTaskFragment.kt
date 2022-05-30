package com.smdsa.checkit.fragments.tasks

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.smdsa.checkit.R
import com.smdsa.checkit.adapters.RegDataClass
import com.smdsa.checkit.adapters.TasksDataClass
import com.smdsa.checkit.databinding.FragmentAddTaskBinding
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddTaskFragment : Fragment() {

    private lateinit var binding: FragmentAddTaskBinding
    private lateinit var database : DatabaseReference
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var auth : FirebaseAuth
    private var arrayList : ArrayList<RegDataClass> = ArrayList()
    private var arrayListString : ArrayList<String> = ArrayList()
    private var priority : Array<String> = arrayOf("Высокий","Средний","Низкий")
    private var fio : String = ""
    private var role : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTaskBinding.inflate(inflater)
        auth = Firebase.auth
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_addTaskFragment_to_listOfTasksFragment)
            }
        })
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arrayAdapter = ArrayAdapter(requireContext(),R.layout.spinner_item,priority)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.priority.adapter = arrayAdapter

        database = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Users/${auth.uid.toString()}")
        database.get().addOnSuccessListener { snapshot ->
            if(snapshot.exists()){
                arrayList.clear()
                arrayList.add(snapshot.getValue(RegDataClass::class.java)!!)
                fio = "${arrayList[0].Name.toString()} ${arrayList[0].MiddleName.toString()} ${arrayList[0].LastName.toString()}"
                role = arrayList[0].Role.toString()
            }
            if(role == "Руководитель"){
                database = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Users")
                database.get().addOnSuccessListener { snapshot2 ->
                    if(snapshot2.exists()){
                        arrayListString.clear()
                        arrayList.clear()
                        for((i, snap) in snapshot2.children.withIndex()){
                            arrayList.add(snap.getValue(RegDataClass::class.java)!!)
                            arrayListString.add("${arrayList[i].Name} ${arrayList[i].MiddleName} ${arrayList[i].LastName}")
                        }
                        arrayAdapter = ArrayAdapter(requireContext(),R.layout.spinner_item,arrayListString)
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.responsible.adapter = arrayAdapter
                    }
                }
            }
            else{
                arrayListString.clear()
                arrayListString.add(fio)
                arrayAdapter = ArrayAdapter(requireContext(),R.layout.spinner_item,arrayListString)
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.responsible.adapter = arrayAdapter
            }
        }

        binding.addButton.setOnClickListener {
            val dateTodayString = SimpleDateFormat("dd.M.yyyy", Locale.getDefault()).format(Date())
            val expirationDateString = "${binding.expirationDate.dayOfMonth}.${binding.expirationDate.month + 1}.${binding.expirationDate.year}"
            val expirationDate = SimpleDateFormat("dd.M.yyyy").parse(expirationDateString)
            val dateToday = SimpleDateFormat("dd.M.yyyy").parse(dateTodayString)
            if(dateToday!! < expirationDate){
                if(binding.header.text.toString().isNotEmpty() && binding.description.text.toString().isNotEmpty()){
                    val taskDataClass = TasksDataClass(
                        createdBy = role,
                        dateOfCreation = dateTodayString,
                        dateOfUpdater = dateTodayString,
                        description = binding.description.text.toString(),
                        expirationDate = expirationDateString,
                        forWho = binding.responsible.selectedItem.toString(),
                        header = binding.header.text.toString(),
                        owner = fio,
                        priority = binding.priority.selectedItem.toString(),
                        responsible = binding.responsible.selectedItem.toString(),
                        status = "К выполнению")
                    database = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Tasks")
                    database.child(getKey().push().key.toString()).setValue(taskDataClass).addOnCompleteListener {
                        if(it.isSuccessful){
                            findNavController().navigate(R.id.action_addTaskFragment_to_listOfTasksFragment)
                        }
                    }
                }
                else{
                    Toast.makeText(activity,"Какое-то поле осталось пустым", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(activity,"Дата выбрана неправильно", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getKey(): DatabaseReference {
        return FirebaseDatabase
            .getInstance("https://checkit-63120-default-rtdb.firebaseio.com/")
            .reference
    }
}