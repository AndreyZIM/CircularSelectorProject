package com.andreyzim.circularselectorproject

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
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
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin


class CircularSelectorView(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    private val paint = Paint()
    private val iconsPaint = Paint()
    private val safeRect = Rect()
    private val unselectedRect = Rect()
    private val listeners = mutableListOf<OnOptionSelectedListener>()
    private var selectedOptionId = -1
        set(value) {
            field = value
            invokeListeners(field)
        }
    var options: List<SelectionItem> = emptyList()
        set(value) {
            field = value
            invalidate()
        }
    private val animatedRectMap = mutableMapOf<Int, Rect>()
    private val valueAnimatorsMap = mutableMapOf<Int, ValueAnimator>()
    private val animationInterpolator = FastOutSlowInInterpolator()

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
        iconsPaint.style = Paint.Style.FILL
        iconsPaint.color = Color.BLACK

        if (isInEditMode) {
            options = listOf(
                SelectionItem(R.drawable.baseline_10k_24, R.color.color4),
                SelectionItem(R.drawable.baseline_123_24, R.color.color5),
                SelectionItem(R.drawable.baseline_16mp_24, R.color.color6),
                SelectionItem(R.drawable.baseline_1k_24, R.color.color7),
                SelectionItem(R.drawable.baseline_app_registration_24, R.color.color8),
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
        val iconSize = ICON_SIZE.toDP()

        options.forEachIndexed { index, item ->
            paint.color = context.getColor(item.color)
            val rect = getRectByPosition(index)
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
            val sectorCenterAngle = (angle * index) + (angle / 2)
            val coordinatesPair = getSectorCenterCoordinates(sectorCenterAngle, rect)
            val drawable = ContextCompat.getDrawable(context, item.image)
            drawable?.let {
                it.setBounds(
                    (coordinatesPair.first - (iconSize / 2)).toInt(),
                    (coordinatesPair.second - (iconSize / 2)).toInt(),
                    (coordinatesPair.first + (iconSize / 2)).toInt(),
                    (coordinatesPair.second + (iconSize / 2)).toInt(),
                )
                it.draw(canvas)
            }
        }
    }

    private fun getRectByPosition(position: Int): Rect =
        animatedRectMap[position] ?: if (position == selectedOptionId) safeRect else unselectedRect

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || options.size !in 2..20) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handleTouchEvent(event)
            }
        }
        return false
    }

    private fun handleTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        if (safeRect.contains(touchX.toInt(), touchY.toInt())) {
            val angle = (Math.toDegrees(
                atan2(
                    touchY - safeRect.centerY(),
                    touchX - safeRect.centerX()
                ).toDouble()
            ) + 360) % 360
            val position = (angle / (360 / options.size)).toInt()
            val rect = getRectByPosition(position)
            return if (isTouchIsInsideArc(touchX, touchY, rect)) {
                if (selectedOptionId >= 0) startDecreasingAnimation(selectedOptionId)
                selectedOptionId = if (position == selectedOptionId) -1 else position
                if (selectedOptionId >= 0) startIncreasingAnimation(selectedOptionId)
                true
            } else false
        } else return false
    }

    private fun isTouchIsInsideArc(touchX: Float, touchY: Float, arcRect: Rect): Boolean {
        val x = touchX - arcRect.centerX()
        val y = touchY - arcRect.centerY()
        val radius = (arcRect.right - arcRect.left) / 2
        return x.toDouble().pow(2) + y.toDouble().pow(2) <= radius.toDouble().pow(2)
    }

    private fun getSectorCenterCoordinates(angle: Float, rect: Rect): Pair<Float, Float> {
        val radius = (rect.right - rect.left) / 2
        val curveCenterX = rect.centerX() + radius * cos(angle * Math.PI / 180)
        val curveCenterY = rect.centerY() + radius * sin(angle * Math.PI / 180)

        val x = rect.centerX() + (curveCenterX - rect.centerX()) / 2
        val y = rect.centerY() + (curveCenterY - rect.centerY()) / 2
        return Pair(x.toFloat(), y.toFloat())
    }

    private fun startIncreasingAnimation(index: Int) {
        if (valueAnimatorsMap.contains(index)) {
            valueAnimatorsMap[index]?.reverse()
            return
        }
        val animatedRect = Rect()
        animatedRect.copyFrom(unselectedRect)
        animatedRectMap[index] = animatedRect
        valueAnimatorsMap[index] = ValueAnimator
            .ofFloat(0F, SELECTED_ARC_RADIUS_DIFF)
            .apply {
                duration = 300
                interpolator = animationInterpolator
                addUpdateListener {
                    val value = it.animatedValue as Float
                    animatedRectMap[index]?.increase(value)
                    invalidate()
                }
                addEndListener {
                    animatedRectMap.remove(index)
                    valueAnimatorsMap.remove(index)
                }
                start()
            }
    }

    private fun startDecreasingAnimation(index: Int) {
        if (valueAnimatorsMap.contains(index)) {
            valueAnimatorsMap[index]?.reverse()
            return
        }
        val animatedRect = Rect()
        animatedRect.copyFrom(unselectedRect)
        animatedRectMap[index] = animatedRect
        valueAnimatorsMap[index] = ValueAnimator
            .ofFloat(SELECTED_ARC_RADIUS_DIFF, 0F)
            .apply {
                duration = 300
                interpolator = animationInterpolator
                addUpdateListener {
                    val value = it.animatedValue as Float
                    animatedRectMap[index]?.increase(value)
                    invalidate()
                }
                addEndListener {
                    animatedRectMap.remove(index)
                    valueAnimatorsMap.remove(index)
                }
                start()
            }
    }

    interface OnOptionSelectedListener {
        fun invoke(option: SelectionItem?)
    }

    fun addOnOptionSelectedListener(block: (SelectionItem?) -> Unit) {
        listeners.add(object : OnOptionSelectedListener {
            override fun invoke(option: SelectionItem?) {
                block.invoke(option)
            }
        })
    }

    private fun invokeListeners(selectedOptionId: Int) {
        listeners.forEach {
            if (selectedOptionId >= 0) it.invoke(options[selectedOptionId])
            else it.invoke(null)
        }
    }

    interface AnimatorEndListener: Animator.AnimatorListener {
        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
        override fun onAnimationStart(animation: Animator) {}
    }

    private fun ValueAnimator.addEndListener(block:() -> Unit) {
        this.addListener(object: AnimatorEndListener {
            override fun onAnimationEnd(animation: Animator) {
                block.invoke()
            }
        })
    }

    // TODO save instance state

    data class SelectionItem(
        @DrawableRes val image: Int,
        @ColorRes val color: Int,
    )

    private fun Float.toDP() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        resources.displayMetrics
    ).toInt()

    private fun Rect.copyFrom(anotherRect: Rect) {
        this.left = anotherRect.left
        this.top = anotherRect.top
        this.right = anotherRect.right
        this.bottom = anotherRect.bottom
    }

    private fun Rect.increase(diff: Float) {
        val diffInDP = diff.toDP()
        this.left = unselectedRect.left - diffInDP
        this.top = unselectedRect.top - diffInDP
        this.right = unselectedRect.right + diffInDP
        this.bottom = unselectedRect.bottom + diffInDP
    }

    companion object {
        const val SELECTED_ARC_RADIUS_DIFF = 42F
        const val ICON_SIZE = 24F
    }
}