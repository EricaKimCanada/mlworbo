package com.example.mlworbo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_word.*
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter


class WordActivity : AppCompatActivity() {

    private lateinit var imageViewSourcePic: ImageView
    private lateinit var textViewSourceWord: TextView
    private lateinit var textViewSourceLang: TextView
    private lateinit var textViewTranslatedWord: TextView

    var words = mutableListOf<Word>()
    var sourcePic: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word)

        imageViewSourcePic = findViewById(R.id.sourcePic)
        textViewSourceWord = findViewById(R.id.sourceWord)
        textViewSourceLang = findViewById(R.id.sourceLang)
        textViewTranslatedWord = findViewById(R.id.translatedWord)

        // Recieve data
        val intent = intent
        sourcePic = intent.extras!!.getString("sourcePic")
        val sourceWord = intent.extras!!.getString("sourceWord")
        val sourceLang = intent.extras!!.getString("sourceLang")
        val translatedWord = intent.extras!!.getString("translatedWord")

        // Setting values
        imageViewSourcePic.setImageURI(Uri.parse(sourcePic))
        textViewSourceWord.text = "Word: $sourceWord"
        textViewSourceLang.text = "Language: $sourceLang"
        textViewTranslatedWord.text = "$translatedWord"

        // Set words
        val jsonFileString = getJSONData(this,"wordList.json")
        val listPersonType = object: TypeToken<List<Word>>() {}.type
        words = Gson().fromJson(jsonFileString, listPersonType)
    }

    //--methods for saving word to word list
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

    fun onButtonClick(view: View) {
        when(view.id) {
            R.id.buttonDeleteWord -> {
                var deletedSourcePic = sourcePic

                words.removeAll{ word -> word.sourcePic == deletedSourcePic }

                try{
                    val ofile = openFileOutput("wordList.json", MODE_PRIVATE)
                    val osw = OutputStreamWriter(ofile)
                    var jsonList = Gson().toJson(words)
                    for(word in jsonList)
                    {
                        osw.write(word.toString())
                    }
                    osw.flush()
                    osw.close()

                    Toast.makeText(
                            this, "Word Deleted",
                            Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } catch (ioe: IOException) {
                    ioe.printStackTrace()
                    Toast.makeText(
                            this, "Word Not Deleted",
                            Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
            R.id.buttonWordGomain -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            else -> throw Exception()
        }
    }
}