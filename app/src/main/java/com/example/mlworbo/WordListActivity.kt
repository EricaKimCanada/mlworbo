package com.example.mlworbo

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class WordListActivity : AppCompatActivity() {
    companion object {
        var wordDataset = ArrayList<Word>()
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)

        showWordList()
    }

    private fun showWordList() {
        val jsonFileString = getJSONData(this,"wordList.json")

        if (jsonFileString != null) {
            //TypeToken to get the type of the object
            val listWordType = object: TypeToken<List<Word>>() {}.type
            wordDataset = Gson().fromJson(jsonFileString, listWordType)
        }

        viewManager = GridLayoutManager(applicationContext, 3, LinearLayoutManager.VERTICAL, false)
        viewAdapter = MyRecyclerAdapter(this, wordDataset)

        recyclerView = findViewById<RecyclerView>(R.id.word_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun getJSONData(context:Context, filename:String):String? {
        val jsonString:String

        try {
            val isr = InputStreamReader(openFileInput(filename))
            jsonString = isr.buffered().use { it.readText() }
        } catch(ioException: java.lang.Exception) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    override fun onResume() {
        super.onResume()
        showWordList()
    }
}