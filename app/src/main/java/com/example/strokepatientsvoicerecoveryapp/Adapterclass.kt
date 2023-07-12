package com.example.strokepatientsvoicerecoveryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView

class Adapterclass(private var dataList: ArrayList<Dataclass>):RecyclerView.Adapter<Adapterclass.ViewHolerClass>() {

    private var onItemClickListener: ((Dataclass) -> Unit)? = null

    fun setOnItemClickListener(listener: (Dataclass) -> Unit) {
        onItemClickListener = listener
    }

    fun setData(data: ArrayList<Dataclass>) {
        dataList = data
        notifyDataSetChanged()
    }

    // var onItemClick: ((Dataclass) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolerClass {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolerClass(itemView)
    }//插入資料

    override fun onBindViewHolder(holder: ViewHolerClass, position: Int) {
        val currentItem = dataList[position]
        holder.rvImage.setImageResource(currentItem.dataImage)
        holder.rvTitle.text = currentItem.dataTitle
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(currentItem)
        }
    } //綁定資料

    override fun getItemCount(): Int {
        return dataList.size
    }//返回數目

    class ViewHolerClass(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rvImage: ImageView = itemView.findViewById(R.id.images)
        val rvTitle: TextView = itemView.findViewById(R.id.Title1)
    }
}