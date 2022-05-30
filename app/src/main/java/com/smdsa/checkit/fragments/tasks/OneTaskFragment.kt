package com.smdsa.checkit.fragments.tasks

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.smdsa.checkit.databinding.FragmentOneTaskBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OneTaskFragment : Fragment() {

    private lateinit var binding: FragmentOneTaskBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database : DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val arrayList : ArrayList<TasksDataClass> = ArrayList()
    private val arrayListString : ArrayList<RegDataClass> = ArrayList()
    private val arrayListKeys : ArrayList<String> = ArrayList()
    private val arrayListTasks = ArrayList<TasksDataClass>()
    private var index = 0
    private var fio = ""
    private var role = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOneTaskBinding.inflate(inflater)
        sharedPreferences = requireContext().getSharedPreferences("smdsa", Context.MODE_PRIVATE)
        index = sharedPreferences.getInt("position",0)
        auth = Firebase.auth
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_oneTaskFragment_to_listOfTasksFragment)
            }
        })
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Users/${auth.uid.toString()}")
        database.get().addOnSuccessListener { snapshot ->
            if(snapshot.exists()){
                arrayListString.clear()
                arrayListString.add(snapshot.getValue(RegDataClass::class.java)!!)
                fio = "${arrayListString[0].Name} ${arrayListString[0].MiddleName} ${arrayListString[0].LastName}"
                role = arrayListString[0].Role.toString()
            }
            database = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Tasks")
            database.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        arrayList.clear()
                        arrayListKeys.clear()
                        for (snap in snapshot.children){
                            arrayList.add(snap.getValue(TasksDataClass::class.java)!!)
                            arrayListKeys.add(snap.key.toString())
                        }
                        for((i, _) in arrayList.withIndex()){
                            if((role == "Пользователь" && arrayList[i].forWho == fio) || role == "Руководитель"){
                                arrayListTasks.add(arrayList[i])
                            }
                        }
                        binding.dateOfCreation.text = "Дата создания: ${arrayListTasks[index].dateOfCreation}"
                        binding.dateOfUpdater.text = "Дата обновления: ${arrayListTasks[index].dateOfUpdater}"
                        binding.description.text = "${arrayListTasks[index].description}"
                        binding.expirationDate.text = "Дата завершения: ${arrayListTasks[index].expirationDate}"
                        binding.header.text = "${arrayListTasks[index].header}"
                        binding.owner.text = "Создатель: ${arrayListTasks[index].owner}"
                        binding.priority.text = "Приоритет: ${arrayListTasks[index].priority}"
                        binding.responsible.text = "Ответственный: ${arrayListTasks[index].responsible}"
                        binding.status.text = "Статус: ${arrayListTasks[index].status}"
                        if(role == arrayListTasks[index].createdBy || role == "Руководитель"){
                            binding.editTaskButton.visibility = View.VISIBLE
                            binding.deleteButton.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError){}
            })
        }

        binding.editStatusButton.setOnClickListener {
            val array = arrayOf("К выполнению","Выполняется","Выполнена","Отменена")
            var status = ""
            AlertDialog.Builder(requireContext()).setTitle("Статус")
                .setItems(array) { _, p2 ->
                    when (p2) {
                        0 -> status = "К выполнению"
                        1 -> status = "Выполняется"
                        2 -> status = "Выполнена"
                        3 -> status = "Отменена"
                    }
                    val b = SimpleDateFormat("dd.M.yyyy", Locale.getDefault()).format(Date())
                    val taskDataClass = TasksDataClass(
                        createdBy = arrayList[index].createdBy,
                        dateOfCreation = arrayList[index].dateOfCreation,
                        dateOfUpdater = b,
                        description = arrayList[index].description,
                        expirationDate = arrayList[index].expirationDate,
                        forWho = arrayList[index].responsible,
                        header = arrayList[index].header,
                        owner = arrayList[index].owner,
                        priority = arrayList[index].priority,
                        responsible = arrayList[index].responsible,
                        status = status
                    )
                    database = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Tasks")
                    database.child(arrayListKeys[index]).setValue(taskDataClass)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    activity,
                                    "Статус изменен успешно",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
                .show()
        }
        binding.editTaskButton.setOnClickListener {
            findNavController().navigate(R.id.action_oneTaskFragment_to_editTaskFragment)
        }
        binding.deleteButton.setOnClickListener {
            database = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Tasks")
            database.child(arrayListKeys[index]).removeValue().addOnCompleteListener {
                if(it.isSuccessful){
                    findNavController().navigate(R.id.action_oneTaskFragment_to_listOfTasksFragment)
                }
            }
        }
    }
}