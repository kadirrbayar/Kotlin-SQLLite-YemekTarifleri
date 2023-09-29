package com.kadir.yemektarifleri

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.kadir.yemektarifleri.databinding.FragmentListeBinding

class ListeFragment : Fragment() {

    var yemekListesi = ArrayList<String>()
    var yemekIdListesi = ArrayList<Int>()
    private lateinit var listeAdapter : ListeRecyclerAdapter
    private lateinit var binding: FragmentListeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentListeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeAdapter = ListeRecyclerAdapter(yemekListesi,yemekIdListesi)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = listeAdapter
        sqlVeriAlma()
    }

    fun sqlVeriAlma() {
        try {
            activity?.let {
                val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                val cursor = database.rawQuery("SELECT * FROM yemekler", null)
                val yemekAdiIndex = cursor.getColumnIndex("yemekismi")
                val yemekIdIndex = cursor.getColumnIndex("id")
                yemekListesi.clear()
                yemekIdListesi.clear()
                while (cursor.moveToNext()){
                    yemekListesi.add(cursor.getString(yemekAdiIndex))
                    yemekIdListesi.add(cursor.getInt(yemekIdIndex))
                }
                listeAdapter.notifyDataSetChanged()
                cursor.close()
            }
        } catch (e:java.lang.Exception) {

        }
    }
}