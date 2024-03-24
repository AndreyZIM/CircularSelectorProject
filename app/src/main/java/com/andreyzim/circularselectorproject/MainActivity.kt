package com.andreyzim.circularselectorproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.andreyzim.circularselectorproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val colorGenerator = RandomColorGenerator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.circularView.options = listOf(
            CircularSelectorView.SelectionItem(R.drawable.baseline_10k_24, colorGenerator.generate()),
            CircularSelectorView.SelectionItem(R.drawable.baseline_123_24, colorGenerator.generate()),
            CircularSelectorView.SelectionItem(R.drawable.baseline_16mp_24, colorGenerator.generate()),
            CircularSelectorView.SelectionItem(R.drawable.baseline_1k_24, colorGenerator.generate()),
            CircularSelectorView.SelectionItem(R.drawable.baseline_app_registration_24, colorGenerator.generate()),
        )
    }
}