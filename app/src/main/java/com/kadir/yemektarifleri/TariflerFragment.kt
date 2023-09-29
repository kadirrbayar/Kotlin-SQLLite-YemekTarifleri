package com.kadir.yemektarifleri

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.kadir.yemektarifleri.databinding.FragmentTariflerBinding
import java.io.ByteArrayOutputStream

class TariflerFragment : Fragment() {
    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null
    private lateinit var binding: FragmentTariflerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTariflerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button.setOnClickListener {
            kaydet(it)
        }
        binding.imageView.setOnClickListener {
            gorselSec(it)
        }
        arguments?.let {
            var gelenBilgi = TariflerFragmentArgs.fromBundle(it).bilgi
            if(gelenBilgi.equals("menudengeldim")) {
                binding.yemekismi.setText("")
                binding.yemekmalzemeismi.setText("")
                binding.button.visibility = View.VISIBLE
                var gorselSecmeArkaPlani = BitmapFactory.decodeResource(context?.resources,R.drawable.image)
                binding.imageView.setImageBitmap(gorselSecmeArkaPlani)
            } else {
                binding.button.visibility = View.INVISIBLE
                val secilenId = TariflerFragmentArgs.fromBundle(it).Id
                context?.let {
                    try {
                        val db = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                        val cursor = db.rawQuery("SELECT * FROM yemekler WHERE id = ?", arrayOf(secilenId.toString()))
                        val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                        val yemekMalzemeIndex = cursor.getColumnIndex("yemekmalzemesi")
                        val yemekGorseli = cursor.getColumnIndex("gorsel")
                        while (cursor.moveToNext()){
                            binding.yemekismi.setText(cursor.getString(yemekIsmiIndex))
                            binding.yemekmalzemeismi.setText(cursor.getString(yemekMalzemeIndex))
                            val byteDizi = cursor.getBlob(yemekGorseli)
                            val bitmap = BitmapFactory.decodeByteArray(byteDizi,0,byteDizi.size)
                            binding.imageView.setImageBitmap(bitmap)
                        }
                        cursor.close()
                    }catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun kaydet(view: View){
        //SQL Lite Kaydet
        val isim = binding.yemekismi.text.toString()
        val malzeme = binding.yemekmalzemeismi.text.toString()
        if(secilenBitmap != null){
            val gorsel = kucukBitmap(secilenBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            gorsel.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY, yemekismi VARCHAR, yemekmalzemesi VARCHAR, gorsel BLOB)")
                    val sqlString = "INSERT INTO yemekler (yemekismi, yemekmalzemesi, gorsel) VALUES (?,?,?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,isim)
                    statement.bindString(2,malzeme)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()
                }
            } catch (e: java.lang.Exception){
                e.printStackTrace()
            }
            val action = TariflerFragmentDirections.actionTariflerFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

    fun gorselSec(view: View){
        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmedi. izin iste.
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
            } else {
                //izin verildi. izin isteme.
                val galeri = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeri,2)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1){
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val galeri = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeri,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            secilenGorsel = data.data
            try {
                context?.let {
                    if(secilenGorsel != null) {
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        } else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    }
                }
            } catch (e: java.lang.Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun kucukBitmap(kullanicininBitmapi : Bitmap, maximumBoyut : Int): Bitmap {
        var width = kullanicininBitmapi.width
        var height = kullanicininBitmapi.height

        val bitmapOrani = width.toDouble() / height.toDouble()
        if(bitmapOrani > 1) {
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        } else {
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()
        }
        return  Bitmap.createScaledBitmap(kullanicininBitmapi,width,height,true)
    }
}