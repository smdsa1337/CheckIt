package com.smdsa.checkit.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smdsa.checkit.R
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TasksRecycleViewAdapter(private val data: ArrayList<TasksDataClass>, private val context: Context) :
RecyclerView.Adapter<TasksRecycleViewAdapter.VH>() {

    private lateinit var mListener: OnRecycleViewListener

    interface OnRecycleViewListener{
        fun onRecycleViewClick (position: Int)
    }

    fun setOnRecycleViewClick(listener: OnRecycleViewListener){
        mListener = listener
    }

    class VH(itemView: View, listener: OnRecycleViewListener) : RecyclerView.ViewHolder(itemView){
        var expirationDate: TextView = itemView.findViewById(R.id.expirationDate)
        var header: TextView = itemView.findViewById(R.id.header)
        var priority: TextView = itemView.findViewById(R.id.priority)
        var responsible: TextView = itemView.findViewById(R.id.responsible)
        var status: TextView = itemView.findViewById(R.id.status)
        init {
            itemView.setOnClickListener {
                listener.onRecycleViewClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(context).inflate(R.layout.task_item, parent, false), mListener)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.header.text = data[holder.adapterPosition].header
        holder.priority.text = "Приоритет: ${data[holder.adapterPosition].priority}"
        holder.expirationDate.text = "Дата завершения: ${data[holder.adapterPosition].expirationDate}"
        holder.responsible.text = "Ответственный: ${data[holder.adapterPosition].responsible}"
        holder.status.text = "Статус: ${data[holder.adapterPosition].status}"
        try{
            val dateTodayString = SimpleDateFormat("dd.M.yyyy", Locale.getDefault()).format(Date())
            val expirationDateString = data[holder.adapterPosition].expirationDate.toString()
            val expirationDate = SimpleDateFormat("dd.M.yyyy").parse(expirationDateString)
            val dateToday = SimpleDateFormat("dd.M.yyyy").parse(dateTodayString)
            if(holder.status.text.toString() == "Статус: Выполнена"){
                holder.header.setTextColor(Color.GREEN)
            }
            else if(dateToday!! > expirationDate || holder.status.text.toString() == "Статус: Отменена"){
                holder.header.setTextColor(Color.RED)
            }
            else{
                holder.header.setTextColor(Color.BLACK)
            }
        } catch (ex: Exception){}
    }

    override fun getItemCount(): Int {
        return data.size
    }
}