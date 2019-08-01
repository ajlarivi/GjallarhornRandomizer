package com.example.gjallarhornrandomizer

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var storage: FirebaseStorage
    lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun individualPull(view: View) {
        val mediaPlayer = MediaPlayer()
        pullRandomSound() {
            try {
                val localFile = it
                localFile.deleteOnExit()
                mediaPlayer.reset()
                val fis = FileInputStream(localFile)
                mediaPlayer.setDataSource(fis.getFD())
                mediaPlayer.prepare()
                mediaPlayer.start()
            } catch (ex: IOException) {
                val s = ex.toString()
                ex.printStackTrace()
            }
        }
    }


    //retrieve an individual file from firebase
    fun pullRandomSound(fileCallBack: (File) -> Unit){
        val database = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        database.collection("Alarms")
            .get()
            .addOnSuccessListener{ documents ->
                val listURL: MutableList<String> = mutableListOf()
                val listName: MutableList<String> = mutableListOf()

                for (document in documents){
                    listURL.add(document.getString("URL").toString())
                    listName.add(document.getString("AlarmName").toString())
                    Log.d("Firebase: ", "${document.id} => ${document.data}")

                }

                val randNum = (0..listURL.size-1).random()
                val storageURL = listURL[randNum]
                val selectedName = listName[randNum]
                Log.d("Firebase: ", storageURL)

                val httpsReference = storage.getReferenceFromUrl(storageURL)
                val localFile = File.createTempFile(selectedName, "m4a")

                httpsReference.getFile(localFile).addOnSuccessListener {
                    fileCallBack(localFile)
                }.addOnFailureListener {
                    //dont worry about it
                }
            }
    }
}
