package com.example.imagetranslatorappxml

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.common.model.RemoteModelManager

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import com.google.mlkit.nl.translate.*

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.widget.ScrollView
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions

// Current app name: PicTranslate - Translation App for Photos

class MainActivity : AppCompatActivity() {

    private lateinit var selectImageButton: Button
    private lateinit var imageView: ImageView

    private lateinit var originalTextView: TextView
    private lateinit var translatedTextView: TextView

    private lateinit var copyOriginalButton: Button
    private lateinit var copyTranslatedButton: Button

    private lateinit var originalScroll: ScrollView
    private lateinit var translatedScroll: ScrollView

    private lateinit var coinTextView: TextView
    private var coins = 0

    private var selectedTargetLangCode: String = "en" // Default translation target to English

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var prefs : SharedPreferences

    private var lastExtractedText: String? = null

    private lateinit var loadingText: TextView

    private var imageHasBeenProcessed = false

    val badgeTitles = listOf(
        "Translation Pup 🐶",
        "Linguist Wolf 🐺",
        "Grammar Cat 🐱",
        "Roaring Polyglot 🦁",
        "Tiger Translator 🐯",
        "Giraffe Grammarian 🦒",
        "Clever Fox 🦊",
        "Translation Raccoon 🦝",
        "Moo-ving Translator 🐮",
        "Linguist Piglet 🐷",
        "Squeaky Wordsmith 🐭",
        "Hopping Interpreter 🐰",
        "Bear of Many Tongues 🐻",
        "Cool Koala 🐨",
        "Bamboo Translator 🐼",
        "Frog of Fluency 🐸",
        "Zebra Lexicographer 🦓",
        "Galloping Grammar Guru 🐴",
        "Majestic Moose 🫎",
        "Unicorn of Understanding 🦄",
        "Clucking Communicator 🐔",
        "Dragon of Dialects 🐲",
        "Wise Gorilla 🦍",
        "Chatterbox Orangutan 🦧",
        "Poodle Polyglot 🐩",
        "Loyal Translator 🐕",
        "Curious Kitten 🐈",
        "Leopard Linguist 🐆",
        "Stag Speaker 🦌",
        "Smart Bison 🦬",
        "Hippo of Hints 🦛",
        "Sheepish Interpreter 🐑",
        "Ram of Reason 🐏",
        "Mountain Goat Translator 🐐",
        "Camel of Conversation 🐪",
        "Linguistic Llama 🦙",
        "Kangaroo Communicator 🦘",
        "Slothful but Smart 🦥",
        "Skunk of Syntax 🦨",
        "Badger Earning Badges 🦡",
        "Elephant of Eloquence 🐘",
        "Woolly Mammoth Wordsmith 🦣",
        "Squirrel of Sentences 🐿️",
        "Porcupine Pro 🦔",
        "Otterly Fluent 🦦",
        "Sea Lion Speaker 🦭",
        "Dolphin of Dialogue 🐬",
        "Shark of Semantics 🦈",
        "Octopus of Oration 🐙",
        "Crabby Conversationalist 🦀",
        "Butterfly of Language 🦋",
        "Bee of Brilliance 🐝",
        "Firebird of Fluency 🐦‍🔥",
        "Wise Owl 🦉",
        "Flamingo of Phrases 🦩",
        "Penguin Polyglot 🐧",
        "Swift Swallow 🕊️",
        "Eagle of Expression 🦅",
        "Parrot of Pronunciation 🦜"
    )

    // Called when activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        coinTextView = findViewById(R.id.coinTextView)
        coins = prefs.getInt("coins", 0)
        updateCoinDisplay()

        mediaPlayer = MediaPlayer.create(this, R.raw.backgroundmusic)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        // One map that connects each language's display name to its ISO international language code!
        val languageMap = mapOf( //represents the user's options on the spinner!
            "English 🔃" to TranslateLanguage.ENGLISH,
            "Español 🔃" to TranslateLanguage.SPANISH,
            "中文 🔃" to TranslateLanguage.CHINESE,
            "Русский 🔃" to TranslateLanguage.RUSSIAN,
            "🔃 العربية" to TranslateLanguage.ARABIC,
            "हिंदी 🔃" to TranslateLanguage.HINDI,
            "বাংলা 🔃" to TranslateLanguage.BENGALI,
            "Українська 🔃" to TranslateLanguage.UKRAINIAN,
            "🔃 اردو" to TranslateLanguage.URDU,
            "Français 🔃" to TranslateLanguage.FRENCH,
            "Português 🔃" to TranslateLanguage.PORTUGUESE,
            "Deutsch 🔃" to TranslateLanguage.GERMAN,
            "한국어 🔃" to TranslateLanguage.KOREAN,
            "日本語 🔃" to TranslateLanguage.JAPANESE,
            "🔃 עברית" to TranslateLanguage.HEBREW,
            "Tiếng Việt 🔃" to TranslateLanguage.VIETNAMESE,
            "Türkçe 🔃" to TranslateLanguage.TURKISH,
            "Bahasa Indonesia 🔃" to TranslateLanguage.INDONESIAN,
            "Kiswahili 🔃" to TranslateLanguage.SWAHILI,
            "Filipino/Tagalog 🔃" to TranslateLanguage.TAGALOG,
            "🔃 فارسی" to TranslateLanguage.PERSIAN,
            "แบบไทย 🔃" to TranslateLanguage.THAI,
            "polski 🔃" to TranslateLanguage.POLISH,
            "biełaruskaja mova 🔃" to TranslateLanguage.BELARUSIAN,
            // Add more languages as needed!
        )

        val earth = findViewById<ImageView>(R.id.earthImage)
        earth.translationX = -450f //-500f

        loadingText = findViewById(R.id.loadingText)
        loadingText.visibility = View.INVISIBLE

        val languageList = languageMap.keys.toList()

        // Set up spinner for target translation language

        val targetSpinner: Spinner = findViewById(R.id.spinnerTargetLang)

        val targetAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageList)
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        targetSpinner.adapter = targetAdapter

        targetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                var selectedLang = parent.getItemAtPosition(position).toString()
                Log.d("Chosen language: ", selectedLang)
                selectedTargetLangCode = languageMap[selectedLang] ?: "en"

                //----

                // Test code (remove later):
                //Toast.makeText(this@MainActivity, "Selected language: $selectedTargetLangCode 👆🍿", Toast.LENGTH_SHORT).show()

                // ----

                if (!imageHasBeenProcessed) {
                    // Do nothing (or silently ignore) and leave the function instead of showing toast
                    return
                }

                if (lastExtractedText.isNullOrBlank()) {
                    Toast.makeText(this@MainActivity, "No text detected. Please try a clearer photo 😊", Toast.LENGTH_LONG).show()
                    return
                }

                // If we already have extracted text, re-detect language and re-translate
                if (!lastExtractedText.isNullOrEmpty()) {
                    val languageIdentifier = com.google.mlkit.nl.languageid.LanguageIdentification
                        .getClient(LanguageIdentificationOptions.Builder().setConfidenceThreshold(0.5f).build())

                    lastExtractedText?.let { text ->
                        languageIdentifier.identifyLanguage(text)
                            .addOnSuccessListener { languageCode ->
                                if (languageCode == "und") {
                                    // Try fallback
                                    lastExtractedText?.let { text ->
                                        languageIdentifier.identifyPossibleLanguages(text)
                                            .addOnSuccessListener { possibleLanguages ->
                                                if (possibleLanguages.isNotEmpty()) {
                                                    val bestGuess = possibleLanguages.maxByOrNull { it.confidence }
                                                    bestGuess?.let { guessedLang ->
                                                        val safeSourceLang =
                                                            TranslateLanguage.fromLanguageTag(
                                                                guessedLang.languageTag
                                                            )
                                                                ?: TranslateLanguage.ENGLISH
                                                        lastExtractedText?.let { mostRecentText ->
                                                            translateText(
                                                                mostRecentText,
                                                                safeSourceLang
                                                            )
                                                        }
                                                    }

                                                } else {
                                                    Toast.makeText(this@MainActivity, "Could not identify language 🤔", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    }
                                } else {
                                    val safeSourceLang = TranslateLanguage.fromLanguageTag(languageCode) ?: TranslateLanguage.ENGLISH
                                    translateText(text, safeSourceLang)
                                }
                            }
                    }


                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedTargetLangCode = "ru"
            }
        }


        // Optional: set default positions (like English to Russian)
        //targetSpinner.setSelection(languageList.indexOf("Russian"))

        //Text recognition!

        originalTextView = findViewById<TextView>(R.id.originalTextView)
        translatedTextView = findViewById<TextView>(R.id.translatedTextView)

        copyOriginalButton = findViewById<Button>(R.id.copyOriginalButton)
        copyTranslatedButton = findViewById<Button>(R.id.copyTranslatedButton)

        originalScroll = findViewById<ScrollView>(R.id.originalTextScroll)
        translatedScroll = findViewById<ScrollView>(R.id.translatedTextScroll)

        // Link UI button to code
        selectImageButton = findViewById(R.id.selectImageButton)
        imageView = findViewById(R.id.image_view)

        // Make semi-black screen overlay invisible at the start
        //findViewById<View>(R.id.dimOverlay).visibility = View.GONE

        // Set up button click listener
        selectImageButton.setOnClickListener {
            // Launch image picker (gallery)
            openGallery()
        }
    }

    // Launches an intent to pick an image from the gallery
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    // Handle image selection result
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedImageUri: Uri? = result.data?.data
            selectedImageUri?.let {
                // Now you have the image URI
                // You can display it, extract text from it, etc.

                processImage(it)
            }
        }
    }

    // Placeholder method for processing the selected image
    private fun processImage(imageUri: Uri) {
        imageView.setImageURI(imageUri)

        //text recognition!
        try {
            val image = InputImage.fromFilePath(this, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Extracted text is stored in visionText.text
                    lastExtractedText = visionText.text
                    //Log.d("OCR Result", extractedText)

                    if (!lastExtractedText.isNullOrBlank()) {
                        imageHasBeenProcessed = true
                    }

                    if (lastExtractedText.isNullOrBlank()) {
                        Toast.makeText(this@MainActivity, "No text detected. Please try a clearer photo 😊", Toast.LENGTH_LONG).show()
                    }

                    // Create Language Identifier client
                    val languageIdentifier = com.google.mlkit.nl.languageid.LanguageIdentification
                        .getClient(LanguageIdentificationOptions.Builder().setConfidenceThreshold(0.3f).build())

                    lastExtractedText?.let{ mostRecentText ->
                        languageIdentifier.identifyLanguage(mostRecentText)
                            .addOnSuccessListener { languageCode ->
                                if (languageCode == "und") {
                                    // Try fallback
                                    lastExtractedText?.let{ mostRecentText ->
                                        languageIdentifier.identifyPossibleLanguages(mostRecentText)
                                            .addOnSuccessListener { possibleLanguages ->
                                                if (possibleLanguages.isNotEmpty()) {
                                                    val bestGuess = possibleLanguages.maxByOrNull { it.confidence }
                                                    bestGuess?.let { inferredExtractedText ->
                                                        val safeSourceLang = TranslateLanguage.fromLanguageTag(inferredExtractedText.languageTag)
                                                            ?: TranslateLanguage.ENGLISH
                                                        lastExtractedText?.let { mostRecentText ->
                                                            translateText(mostRecentText, safeSourceLang)
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(this, "Could not identify language 🤔", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    }
                                } else {
                                    val safeSourceLang = TranslateLanguage.fromLanguageTag(languageCode) ?: TranslateLanguage.ENGLISH
                                    lastExtractedText?.let { mostRecentText ->
                                        translateText(mostRecentText, safeSourceLang)
                                    }
                                }
                            }
                    }
                }
                } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Image processing failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun translateText(textToTranslate: String, detectedSourceLang: String) {
        loadingText.visibility = View.VISIBLE
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(detectedSourceLang) // Start with algorithm's detected language
            .setTargetLanguage(TranslateLanguage.fromLanguageTag(selectedTargetLangCode) ?: TranslateLanguage.ENGLISH) // Translate to user's selected target language
            .build()
        Log.d("Target language: ", selectedTargetLangCode)

        //----

        // Test code (remove later):
        //Toast.makeText(this, "Target language: $selectedTargetLangCode 🎯🍿", Toast.LENGTH_SHORT).show()

        // ----

        val translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // ----

                loadingText.visibility = View.INVISIBLE
                // define celebration emoji and how it appears after translations
                val emojiView = findViewById<ImageView>(R.id.celebrationEmoji)
                fun showCelebrationEmoji() {
                    emojiView.alpha = 0f
                    emojiView.visibility = View.VISIBLE

                    emojiView.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .withEndAction {
                            emojiView.animate()
                                .alpha(0f)
                                .setDuration(600)
                                .withEndAction {
                                    emojiView.visibility = View.GONE
                                }
                        }
                }

                // store eagle image
                val eagle = findViewById<ImageView>(R.id.eagleImage)

                // Get screen width
                val screenWidth = Resources.getSystem().displayMetrics.widthPixels.toFloat()

                // Reset eagle to starting position and make visible
                eagle.translationX = -700f
                eagle.visibility = View.VISIBLE
                //make half-black screen overlay visible
                findViewById<View>(R.id.dimOverlay).visibility = View.VISIBLE

                // Animate eagle to fly across screen over 1 second
                eagle.animate()
                    .translationX(screenWidth-10)
                    .setDuration(1000)
                    .withEndAction {
                        val successSound = MediaPlayer.create(this, R.raw.success) //Store success sound effect
                        successSound.start() //Play success sound effect
                        eagle.visibility = View.INVISIBLE // Hide eagle after flying
                        findViewById<View>(R.id.dimOverlay).visibility = View.GONE //Remove half-black screen overlay
                        showCelebrationEmoji() //Show quick celebration emoji after translation
                    }
                    .start()

                // ----
                // Now it's safe to translate
                lastExtractedText?.let{ mostRecentText ->
                    translator.translate(mostRecentText)
                        .addOnSuccessListener { translatedText ->
                            originalTextView.text = lastExtractedText
                            translatedTextView.text = translatedText

                            // Add to coin count only if there are actual text results
                            if (originalTextView.text != "" && translatedTextView.text != "") {
                                coins += 1
                                prefs.edit().putInt("coins", coins).apply()
                                updateCoinDisplay()
                            }

                            // Every 10 coins, show a new achievement title message
                            if (coins % 10 == 0) {
                                val randomBadge = badgeTitles.random()
                                val tadaSound = MediaPlayer.create(this, R.raw.tada) //Store tada sound effect
                                tadaSound.start() //Play tada sound effect
                                AlertDialog.Builder(this)
                                    .setTitle("Achievement! 🎉")
                                    .setMessage("You've earned a title: $randomBadge!" +
                                            "\nYou'll see a new badge in 10 translations - who will it be? 🥇")
                                    .setPositiveButton("Let's find out! 🤓", null)
                                    .show()
                            }

                            // Make scrollers visible
                            originalScroll.alpha=1.0f
                            translatedScroll.alpha=1.0f

                            //showCelebrationEmoji() //Show quick celebration emoji after translation

                            // Scroll to bottom
                            originalScroll.post { originalScroll.fullScroll(View.FOCUS_DOWN) }
                            translatedScroll.post { translatedScroll.fullScroll(View.FOCUS_DOWN) }

                            copyOriginalButton.setOnClickListener {
                                //Copy extracted English text to clipboard
                                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("label", originalTextView.text.toString())
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(this, "Original text copied! 📲", Toast.LENGTH_SHORT).show()
                            }

                            copyTranslatedButton.setOnClickListener {
                                //Copy translated text to clipboard
                                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("label", translatedTextView.text.toString())
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(this, "Translated text copied! 📲", Toast.LENGTH_SHORT).show()
                            }

                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Translation failed: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Language translation didn't work. Please try again 😊",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
    }

    private fun updateCoinDisplay() {
        coinTextView.text = "🪙: $coins"
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}