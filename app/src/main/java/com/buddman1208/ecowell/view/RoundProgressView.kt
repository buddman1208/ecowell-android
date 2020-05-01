package com.buddman1208.ecowell.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.buddman1208.ecowell.R


class RoundProgressView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // Config
    private var progress: Int = 50
        set(value) {
            field = if (value > 100) 100 else value
            invalidate()
        }

    // View Options
    private var strokeWidth: Int = 10
        set(value) {
            field = value
            invalidate()
        }
    private var strokeColor: Int = Color.parseColor("#EEEEEE")
        set(value) {
            field = value
            invalidate()
        }
    private var progressColor: Int = ContextCompat.getColor(context, R.color.colorAccent)
        set(value) {
            field = value
            invalidate()
        }

    private val progressPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = progressColor
            style = Paint.Style.STROKE
            strokeWidth = this@RoundProgressView.strokeWidth.toFloat()
            isAntiAlias = true
        }
    }

    private val strokePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokeColor
            style = Paint.Style.STROKE
            strokeWidth = this@RoundProgressView.strokeWidth.toFloat()
            isAntiAlias = true
        }
    }

    private val progressRect = RectF()

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RoundProgressView,
            0, 0
        ).apply {

            try {
                strokeWidth = getDimensionPixelSize(R.styleable.RoundProgressView_strokeWidth, 10)
                progress = getInteger(R.styleable.RoundProgressView_progressPercent, 50)
                progressColor = getColor(R.styleable.RoundProgressView_progressColor, progressColor)
                strokeColor = getColor(R.styleable.RoundProgressView_strokeColor, strokeColor)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = View.resolveSizeAndState(minw, widthMeasureSpec, 1)
        val minh: Int = View.MeasureSpec.getSize(w) + paddingBottom + paddingTop
        val h: Int = View.resolveSizeAndState(
            View.MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )

        val pad = strokeWidth.toFloat() / 2
        progressRect.set(
            pad + paddingLeft,
            pad + paddingTop,
            w.toFloat() - pad - paddingRight,
            h.toFloat() - pad - paddingBottom
        )
        setMeasuredDimension(w, h)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            drawArc(progressRect, 0f, 360f, false, strokePaint)
            drawArc(progressRect, 270f, -(360 * (progress / 100f)), false, progressPaint)
        }

    }


}