package com.andreyzim.circularselectorproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.andreyzim.circularselectorproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val options = listOf(
        CircularSelectorView.SelectionItem(R.drawable.baseline_10k_24, R.color.color4),
        CircularSelectorView.SelectionItem(R.drawable.baseline_123_24, R.color.color7),
        CircularSelectorView.SelectionItem(R.drawable.baseline_123_24, R.color.color13),
        CircularSelectorView.SelectionItem(R.drawable.baseline_16mp_24, R.color.color16),
        CircularSelectorView.SelectionItem(R.drawable.baseline_16mp_24, R.color.color19),
        CircularSelectorView.SelectionItem(R.drawable.baseline_16mp_24, R.color.color21),
    )

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

        binding.circularView.options = this.options

        binding.circularView.addOnOptionSelectedListener {
            val id = this.options.indexOf(it)
            binding.optionId.text = if(id >= 0) "ID = $id" else "ID = "
            if(it != null) binding.optionIcon.setImageResource(it.image)
            else binding.optionIcon.setImageDrawable(null)
        }
    }
}