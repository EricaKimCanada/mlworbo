package com.example.mlworbo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    private lateinit var cardPhoto: CardView
    private lateinit var cardTranslate: CardView
    private lateinit var cardWordList: CardView
    private lateinit var cardHelp: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardPhoto = findViewById(R.id.cardPhoto)
        cardTranslate = findViewById(R.id.cardTranslate)
        cardWordList = findViewById(R.id.cardWordList)
        cardHelp = findViewById(R.id.cardHelp)
    }

    fun onCardClick(view: View) {
        when(view.id)
        {
            R.id.cardPhoto -> {
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
            }
            R.id.cardTranslate -> {
                val intent = Intent(this, OcrActivity::class.java)
                startActivity(intent)
            }
            R.id.cardWordList -> {
                val intent = Intent(this, WordListActivity::class.java)
                startActivity(intent)
            }
            R.id.cardHelp -> {
                val intent = Intent(this, HelpActivity::class.java)
                startActivity(intent)
            }
            else -> throw Exception()
        }
    }
}