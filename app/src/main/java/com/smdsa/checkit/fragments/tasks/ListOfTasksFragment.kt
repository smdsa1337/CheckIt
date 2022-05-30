package com.smdsa.checkit.fragments.tasks

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.smdsa.checkit.R
import com.smdsa.checkit.adapters.RegDataClass
import com.smdsa.checkit.adapters.TasksDataClass
import com.smdsa.checkit.adapters.TasksRecycleViewAdapter
import com.smdsa.checkit.databinding.FragmentListOfTasksBinding
import java.lang.Exception
import kotlin.collections.ArrayList

class ListOfTasksFragment : Fragment() {

    private lateinit var binding: FragmentListOfTasksBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var role: String
    private lateinit var fio : String
    private lateinit var adapter: TasksRecycleViewAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val arrayListUser = ArrayList<RegDataClass>()
    private val arrayListTasks = ArrayList<TasksDataClass>()
    private val arrayList = ArrayList<TasksDataClass>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListOfTasksBinding.inflate(inflater)
        auth = Firebase.auth
        requireActivity().onBackPressedDispatcher.addCallback(object:OnBackPressedCallback(true){override fun handleOnBackPressed(){}})
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolBar.inflateMenu(R.menu.menu)
        binding.toolBar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.setAdmin -> setAdminDialog()
                R.id.sortBy -> sortBy()
            }
            true
        }
        database = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Users/${auth.uid.toString()}")
        database.get().addOnSuccessListener { snapshot ->
            arrayListUser.clear()
            if(snapshot.exists()){
                arrayListUser.add(snapshot.getValue(RegDataClass::class.java)!!)
                role = arrayListUser[0].Role.toString()
                fio = "${arrayListUser[0].Name} ${arrayListUser[0].MiddleName} ${arrayListUser[0].LastName}"
            }
            database = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Tasks")
            database.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    try{
                        arrayListTasks.clear()
                        if(snapshot.exists()){
                            for(snap in snapshot.children){
                                arrayListTasks.add(snap.getValue(TasksDataClass::class.java)!!)
                            }
                        }

                        for((i, _) in arrayListTasks.withIndex()){
                            if((role == "Пользователь" && arrayListTasks[i].forWho == fio) || role == "Руководитель"){
                                arrayList.add(arrayListTasks[i])
                            }
                        }
                        adapter = TasksRecycleViewAdapter(arrayList,requireContext())
                        adapter.setOnRecycleViewClick(object : TasksRecycleViewAdapter.OnRecycleViewListener{
                            override fun onRecycleViewClick(position: Int) {
                                sharedPreferences = requireContext().getSharedPreferences("smdsa", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putInt("position",position).apply()
                                findNavController().navigate(R.id.action_listOfTasksFragment_to_oneTaskFragment)
                            }
                        })
                        binding.recycleView.adapter = adapter
                    } catch (ex: Exception){}
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        binding.addButton.setOnClickListener {
            findNavController().navigate(R.id.action_listOfTasksFragment_to_addTaskFragment)
        }
    }

    private fun sortBy(){
        val sortArrayUser = arrayOf("По дате завершения","Без сортировки")
        val sortArrayAdmin = arrayOf("По дате завершения","По ответственным","Без сортировки")
        if(role == "Пользователь"){
            AlertDialog.Builder(requireContext()).setTitle("Сортировка по?")
                .setItems(sortArrayUser) { _, p2 ->
                    when (p2) {
                        0 -> arrayList.sortBy { it.expirationDate }
                        1 -> arrayList.sortBy { it.dateOfUpdater }
                    }
                }
                .show()
        }
        else{
            AlertDialog.Builder(requireContext()).setTitle("Сортировка по?")
                .setItems(sortArrayAdmin) { _, p2 ->
                    when (p2) {
                        0 -> arrayList.sortBy { it.expirationDate }
                        1 -> arrayList.sortBy { it.responsible }
                        2 -> arrayList.sortBy { it.dateOfUpdater }
                    }
                }
                .show()
        }

        adapter = TasksRecycleViewAdapter(arrayList, requireContext())
        adapter.setOnRecycleViewClick(object : TasksRecycleViewAdapter.OnRecycleViewListener{
            override fun onRecycleViewClick(position: Int) {

            }
        })
        binding.recycleView.adapter = adapter
    }

    private fun setAdminDialog(){
        database = FirebaseDatabase.getInstance("https://checkit-63120-default-rtdb.firebaseio.com/").getReference("Users/${auth.uid.toString()}")
        database.get().addOnSuccessListener { snapshot ->
            arrayListUser.clear()
            if(snapshot.exists()){
                arrayListUser.add(snapshot.getValue(RegDataClass::class.java)!!)
                role = arrayListUser[0].Role.toString()
                if(role == "Пользователь"){
                    val view = requireActivity().layoutInflater.inflate(R.layout.set_admin_item, null)
                    AlertDialog.Builder(requireContext())
                        .setView(view)
                        .setCancelable(false)
                        .setPositiveButton("Активировать") { _, _ ->
                            val adminPassword =
                                view?.findViewById<EditText>(R.id.passwordAdmin)?.text.toString()
                            if (adminPassword == "123123") {
                                val regDataClass = RegDataClass(
                                    LastName = arrayListUser[0].LastName,
                                    MiddleName = arrayListUser[0].MiddleName,
                                    Name = arrayListUser[0].Name,
                                    Role = "Руководитель"
                                )
                                database.setValue(regDataClass).addOnCompleteListener {
                                    if(it.isSuccessful){
                                        Toast.makeText(activity, "Вы стали руководителем", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(activity, "Пароль неверный", Toast.LENGTH_SHORT).show()
                            }
                        }.show()
                }
                else{
                    Toast.makeText(activity,"Вы уже руководитель",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}