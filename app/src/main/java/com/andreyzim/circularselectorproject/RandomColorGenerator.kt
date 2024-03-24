package com.andreyzim.circularselectorproject

import android.graphics.Color
import kotlin.random.Random

class RandomColorGenerator {
    private val random = Random
    fun generate(): Int =
        Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
}