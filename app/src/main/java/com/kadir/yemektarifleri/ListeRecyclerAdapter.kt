package com.kadir.yemektarifleri

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

class ListeRecyclerAdapter(val yemekListesi : ArrayList<String>, val yemekId : ArrayList<Int>) : RecyclerView.Adapter<ListeRecyclerAdapter.YemekHolder>() {
    class YemekHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YemekHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recyler_row,parent,false)
        return YemekHolder(view)
    }

    override fun getItemCount(): Int {
        return yemekListesi.size
    }

    override fun onBindViewHolder(holder: YemekHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.recyler_row_text).text = yemekListesi[position]
        holder.itemView.setOnClickListener {
            val action = ListeFragmentDirections.actionListeFragmentToTariflerFragment("recyclerdangeldim",yemekId[position])
            Navigation.findNavController(it).navigate(action)
        }
    }
}