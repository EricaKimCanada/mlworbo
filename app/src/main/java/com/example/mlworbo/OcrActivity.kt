package com.example.mlworbo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.google.mlkit.nl.translate.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.android.synthetic.main.activity_ocr.*
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.util.*


class OcrActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "OCRActivity "
    }

    private lateinit var savedUri: Uri
    private lateinit var textViewPhotoText: TextView
    private lateinit var imageViewPhoto: ImageView

    // private var result: MutableLiveData<String>? = null
    private var recognizedResult: String = ""

    // the on-device model for text recognition
    private val recognizer = TextRecognition.getClient()

    // Instantiate LanguageIdentification
    private lateinit var languageIdentification: LanguageIdentifier

    //--Translate variables-----
    private lateinit var textViewOutputText: TextView
    private lateinit var buttonTranslate: Button
    private lateinit var buttonSave: Button
    private lateinit var englishTranslator: Translator
    private val modelManager: RemoteModelManager = RemoteModelManager.getInstance()
    var flagTranslate: Boolean = false
    var sourceText: String = "No Word"
    var sourceLangCode: String = "en"
    var savedTranslatedText: String? = ""

    //--saved word list
    var wordList = mutableListOf<Word>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr)

        textViewPhotoText = findViewById(R.id.textViewPhotoText)
        imageViewPhoto = findViewById(R.id.imageViewPhoto)
        textViewOutputText = findViewById(R.id.textViewOutputText)
        buttonTranslate = findViewById(R.id.buttonTranslate)
        buttonSave = findViewById(R.id.buttonSave)
        buttonTranslate.isEnabled = false;
        buttonSave.isEnabled = false;


        languageIdentification = LanguageIdentification.getClient()
        lifecycle.addObserver(languageIdentification)

        val prefsEditor = getSharedPreferences(CameraActivity.SHARED_PREFS_KEY, Context.MODE_PRIVATE)
        var savedUriStr = prefsEditor.getString(CameraActivity.PHOTO_URI_KEY, "")

        if (savedUriStr == "") {
            imageViewPhoto.setImageResource(R.drawable.no_image)
        }
        else {
            savedUri = Uri.parse(savedUriStr)
            var myBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, savedUri)
            imageViewPhoto.setImageBitmap(myBitmap)
            val textImage = InputImage.fromBitmap(myBitmap, 0)
            recognizeText(textImage).addOnCompleteListener { }
        }

        //--translate button
        buttonTranslate.setOnClickListener {
            if(flagTranslate){
                englishTranslator.translate(sourceText)
                .addOnSuccessListener { translatedText ->
                    // Translation successful.
                    textViewOutputText.text = translatedText
                    savedTranslatedText = translatedText

                    buttonSave.isEnabled = true;
                }
                .addOnFailureListener { exception ->
                    // Error.
                    Log.e(TAG, "Translate error", exception)
                }
            }
        }

        //--save button
        buttonSave.setOnClickListener {
            if(flagTranslate){
                addDataToJson()
                Toast.makeText(
                    this, "Word Saved",
                    Toast.LENGTH_SHORT
                ).show()
            }
            val prefsEditor = getSharedPreferences(CameraActivity.SHARED_PREFS_KEY, Context.MODE_PRIVATE).edit()
            prefsEditor.putString(CameraActivity.PHOTO_URI_KEY, "")
            prefsEditor.apply()
            finish()
        }

        buttonOcrGomain.setOnClickListener {
            finish()
        }
    }

    //--OCR methods-----------------------
    fun recognizeText(image: InputImage): Task<Text> {
        // pass the image to the process
        return recognizer.process(image)
            .addOnSuccessListener { visionText -> // Task completed successfully
                recognizedResult = visionText.text

                // identifyLanguage(recognizedResult)
                identifyPossibleLanguages(recognizedResult)
            }
            .addOnFailureListener { exception ->
                // Task failed with an exception
                val message = getErrorMessage(exception)
                message?.let {
                    Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getErrorMessage(exception: Exception): String? {
        val mlKitException = exception as? MlKitException ?: return exception.message
        return if (mlKitException.errorCode == MlKitException.UNAVAILABLE) {
            "Waiting for text recognition model to be downloaded"
        } else exception.message
    }

    //--language Identification methods ------------------------
    private fun identifyPossibleLanguages(inputText: String) {
        languageIdentification
            .identifyPossibleLanguages(inputText)
            .addOnSuccessListener(this@OcrActivity) { identifiedLanguages ->
                val detectedLanguages = ArrayList<String>(identifiedLanguages.size)
                var savedMax = 0.0f
                var savedLangCode = "en"
                for (language in identifiedLanguages) {
                    detectedLanguages.add(
                        String.format(
                            Locale.CANADA,
                            "%s (%3f)",
                            language.languageTag,
                            language.confidence
                        )
                    )
                    if(savedMax < language.confidence) {
                        savedMax = language.confidence
                        savedLangCode = language.languageTag
                    }
                }
                textViewPhotoText?.append(
                    String.format(
                        Locale.CANADA,
                        "\n%s\n%s",
                        "Word: $inputText",
                        "Language: " + Language(savedLangCode).toString()
                    )
                )
                textViewOutputText.text = "Preparing translation..."
                sourceLangCode = savedLangCode
                sourceText = inputText
                getTranslator(savedLangCode)
            }
            .addOnFailureListener(this@OcrActivity) { e ->
                Log.e(TAG, "Language identification error", e)
                Toast.makeText(
                    this@OcrActivity, "language_id_error",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    //--Translate methods-----------------------
    private fun getTranslator(langCode: String) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(langCode)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        englishTranslator = Translation.getClient(options)
        lifecycle.addObserver(englishTranslator)

        var conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        englishTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // Model downloaded successfully. Okay to start translating.
                flagTranslate = true
                buttonTranslate.isEnabled = true;
                textViewOutputText.text = ""
                Log.e(TAG, "model download okay")
            }
            .addOnFailureListener { exception ->
                // Model couldn’t be downloaded or other internal error.
                Log.e(TAG, "model download error", exception)
                sourceLangCode = ""
                textViewPhotoText.text = "Sorry. Translation Failed"
            }
    }

    private fun getModel(languageCode: String): TranslateRemoteModel {
        return TranslateRemoteModel.Builder(languageCode).build()
    }

    // Deletes a locally stored translation model.
    private fun deleteLanguage(language: Language) {
        val model = getModel(TranslateLanguage.fromLanguageTag(language.code)!!)
        modelManager.deleteDownloadedModel(model).addOnCompleteListener { }
    }

    /**
     * Holds the language code (i.e. "en") and the corresponding localized full language name
     * (i.e. "English")
     */
    class Language(val code: String) : Comparable<Language> {
        private val displayName: String
            get() = Locale(code).displayName

        override fun equals(other: Any?): Boolean {
            if (other === this) {
                return true
            }
            if (other !is Language) {
                return false
            }
            val otherLang = other as Language?
            return otherLang!!.code == code
        }

        override fun toString(): String {
            return "$displayName"
        }

        override fun compareTo(other: Language): Int {
            return this.displayName.compareTo(other.displayName)
        }

        override fun hashCode(): Int {
            return code.hashCode()
        }
    }

    //--JSON methods -- methods for saving word to word list
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

    private fun updateWordJson(list: MutableList<Word>) {
        try{
            val ofile = openFileOutput("wordList.json", MODE_PRIVATE)
            val osw = OutputStreamWriter(ofile)
            var jsonList = Gson().toJson(list)
            for(word in jsonList)
            {
                osw.write(word.toString())
            }
            osw.flush()
            osw.close()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
    }

    private fun addDataToJson() {
        val jsonFileString = getJSONData(this,"wordList.json")

        if (jsonFileString == null) {
            if (savedUri.toString() != "") {
                wordList.add(Word(sourceText, Language(sourceLangCode).toString(), savedUri.toString(), savedTranslatedText))
                updateWordJson(wordList)
            }
        }
        else {
            if (savedUri.toString() != "") {
                val listWordType = object : TypeToken<List<Word>>() {}.type
                wordList = Gson().fromJson(jsonFileString, listWordType)
                wordList.add(Word(sourceText, Language(sourceLangCode).toString(), savedUri.toString(), savedTranslatedText))
                updateWordJson(wordList)
            }
        }
    }

    //--override methods-------------------------
    override fun onResume() {
        super.onResume()
        //Load data from shared prefs
        val prefsEditor = getSharedPreferences(CameraActivity.SHARED_PREFS_KEY, Context.MODE_PRIVATE)
        savedUri = Uri.parse(prefsEditor.getString(CameraActivity.PHOTO_URI_KEY, ""))

        if (savedUri.toString() == "") {
            imageViewPhoto.setImageResource(R.drawable.no_image)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteLanguage(Language("en"))
        if (sourceLangCode != "") {
            deleteLanguage(Language(sourceLangCode))
        }
    }
}