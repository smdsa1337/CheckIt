package com.smdsa.checkit.fragments.tasks

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.smdsa.checkit.R
import com.smdsa.checkit.adapters.RegDataClass
import com.smdsa.checkit.adapters.TasksDataClass
import com.smdsa.checkit.databinding.FragmentEditTaskBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditTaskFragment : Fragment() {

    private lateinit var binding: FragmentEditTaskBinding
    private lateinit var database : DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var arrayAdapterResponsible: ArrayAdapter<String>
    private lateinit var arrayAdapterPriority: ArrayAdapter<String>
    private lateinit var auth : FirebaseAuth
    private var arrayList : ArrayList<RegDataClass> = ArrayList()
    private var arrayListString : ArrayList<String> = ArrayList()
    private var arrayListTask : ArrayList<TasksDataClass> = ArrayList()
    private var arrayListKeys : ArrayList<String> = ArrayList()
    private var priority : Array<String> = arrayOf("Высокий","Средний","Низкий")
    private var index = 0
    private var fio : String = ""
    private var role : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditTaskBinding.inflate(inflater)
        auth = Firebase.auth
        sharedPreferences = requireContext().getSharedPreferences("smdsa", Context.MODE_PRIVATE)
        index = sharedPreferences.getInt("position",0)
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_editTaskFragment_to_oneTaskFragment)
            }
        })
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arrayAdapterPriority = ArrayAdapter(requireContext(),R.layout.spinner_item,priority)
        arrayAdapterPriority.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.priority.adapter = arrayAdapterPriority

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
                        arrayAdapterResponsible = ArrayAdapter(requireContext(),R.layout.spinner_item,arrayListString)
                        arrayAdapterResponsible.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.responsible.adapter = arrayAdapterResponsible
                    }
                }
            }
            else{
                arrayListString.clear()
                arrayListString.add(fio)
                arrayAdapterResponsible = ArrayAdapter(requireContext(),R.layout.spinner_item,arrayListString)
                arrayAdapterResponsible.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.responsible.adapter = arrayAdapterResponsible
            }
            database = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Tasks")
            database.get().addOnSuccessListener { snapshot2 ->
                if(snapshot2.exists()){
                    try{
                        arrayListTask.clear()
                        arrayListKeys.clear()
                        for(snap in snapshot2.children){
                            arrayListTask.add(snap.getValue(TasksDataClass::class.java)!!)
                            arrayListKeys.add(snap.key.toString())
                        }
                        binding.header.setText(arrayListTask[index].header.toString())
                        binding.description.setText(arrayListTask[index].description.toString())
                        val a = arrayListTask[index].expirationDate?.split('.')
                        binding.expirationDate.updateDate(Integer.parseInt(a!![2]),Integer.parseInt(a[1]),Integer.parseInt(a[0]))
                        binding.priority.setSelection(arrayAdapterPriority.getPosition(arrayListTask[index].priority))
                    }
                    catch (ex : Exception){
                        findNavController().navigate(R.id.action_editTaskFragment_to_listOfTasksFragment)
                    }
                }
            }
        }

        binding.editButton.setOnClickListener {
            val dateTodayString = SimpleDateFormat("dd.M.yyyy", Locale.getDefault()).format(Date())
            val expirationDateString = "${binding.expirationDate.dayOfMonth}.${binding.expirationDate.month + 1}.${binding.expirationDate.year}"
            val expirationDate = SimpleDateFormat("dd.M.yyyy").parse(expirationDateString)
            val dateToday = SimpleDateFormat("dd.M.yyyy").parse(dateTodayString)
            if(dateToday!! < expirationDate){
                if(binding.header.text.toString().isNotEmpty() && binding.description.text.toString().isNotEmpty()){
                    val taskDataClass = TasksDataClass(
                        createdBy = role,
                        dateOfCreation = arrayListTask[index].dateOfCreation,
                        dateOfUpdater = dateTodayString,
                        description = binding.description.text.toString(),
                        expirationDate = expirationDateString,
                        forWho = binding.responsible.selectedItem.toString(),
                        header = binding.header.text.toString(),
                        owner = arrayListTask[index].owner,
                        priority = binding.priority.selectedItem.toString(),
                        responsible = binding.responsible.selectedItem.toString(),
                        status = arrayListTask[index].status)
                    database = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Tasks")
                    database.child(arrayListKeys[index]).setValue(taskDataClass).addOnCompleteListener {
                        if(it.isSuccessful){
                            findNavController().navigate(R.id.action_editTaskFragment_to_oneTaskFragment)
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
}