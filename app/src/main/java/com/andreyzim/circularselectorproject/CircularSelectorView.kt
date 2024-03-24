package com.andreyzim.circularselectorproject

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

class CircularSelectorView(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    private val paint = Paint()
    private val colorGenerator = RandomColorGenerator()
    private val safeRect = Rect()
    private val unselectedRect = Rect()
    private var selectedOption = -1
        set(value) {
            field = value
            invalidate()
        }
    var options: List<SelectionItem> = emptyList()
        set(value) {
            field = value
            invalidate()
        }
    private var selectedOptionRadiusDiff = 0F

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attributeSet,
        defStyleAttr,
        0
    )

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    init {
        paint.style = Paint.Style.FILL
        paint.color = Color.LTGRAY
        if (isInEditMode) {
            options = listOf(
                SelectionItem(R.drawable.baseline_10k_24, colorGenerator.generate()),
                SelectionItem(R.drawable.baseline_123_24, colorGenerator.generate()),
                SelectionItem(R.drawable.baseline_16mp_24, colorGenerator.generate()),
                SelectionItem(R.drawable.baseline_1k_24, colorGenerator.generate()),
                SelectionItem(R.drawable.baseline_app_registration_24, colorGenerator.generate()),
            )
        }
    }

    private fun updateViewSize() {
        val safeWidth = width - paddingLeft - paddingRight
        val safeHeight = height - paddingTop - paddingBottom

        val radiusDiff = SELECTED_ARC_RADIUS_DIFF.toDP()

        if (safeWidth > safeHeight) {
            safeRect.left = paddingLeft + (safeWidth - safeHeight) / 2
            safeRect.top = paddingTop
            safeRect.bottom = paddingTop + safeHeight
            safeRect.right = paddingLeft + (safeWidth / 2 + safeHeight / 2)
            unselectedRect.left = safeRect.left + radiusDiff
            unselectedRect.top = safeRect.top + radiusDiff
            unselectedRect.bottom = safeRect.bottom - radiusDiff
            unselectedRect.right = safeRect.right - radiusDiff
        } else {
            safeRect.left = paddingLeft
            safeRect.top = paddingTop + (safeHeight - safeWidth) / 2
            safeRect.bottom = paddingTop + (safeHeight / 2 + safeWidth / 2)
            safeRect.right = paddingLeft + safeWidth
            unselectedRect.left = safeRect.left + radiusDiff
            unselectedRect.top = safeRect.top + radiusDiff
            unselectedRect.bottom = safeRect.bottom - radiusDiff
            unselectedRect.right = safeRect.right - radiusDiff
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewSize()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (widthSize == 0 && heightSize == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            val minSize = min(measuredWidth, measuredHeight)
            setMeasuredDimension(minSize, minSize)
            return
        }
        val size = if (widthSize == 0 || heightSize == 0) max(widthSize, heightSize)
        else min(widthSize, heightSize)

        val newMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        super.onMeasure(newMeasureSpec, newMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (options.size < 2 || options.size > 20) return
        val angle = 360F / options.size
        options.forEachIndexed { index, item ->
            paint.color = item.color
            val rect = if (index == selectedOption) safeRect else unselectedRect
            canvas.drawArc(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                angle * index,
                angle,
                true,
                paint
            )


        }
    }


    data class SelectionItem(
        @DrawableRes val image: Int,
        val color: Int,
    )

    private fun log(message: String) {
        Log.d("CustomView", message)
    }

    private fun Float.toDP() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        resources.displayMetrics
    ).toInt()

    companion object {
        const val SELECTED_ARC_RADIUS_DIFF = 24F
        const val ICON_SIZE = 16F
    }
}