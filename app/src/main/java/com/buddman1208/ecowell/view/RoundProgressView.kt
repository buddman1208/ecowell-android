package com.buddman1208.ecowell.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.buddman1208.ecowell.R

class RoundProgressView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var progressWidth : Int = 10
    private var progressColor : Int = ContextCompat.getColor(context, R.color.colorPrimary)
    private var centerColor : Int = Color.WHITE

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RoundProgressView,
            0, 0).apply {

            try {
                progressWidth = getDimensionPixelSize(R.styleable.RoundProgressView_progressWidth, 10)
                progressColor = getColor(R.styleable.RoundProgressView_progressColor, progressColor)
                centerColor = getColor(R.styleable.RoundProgressView_centerColor, centerColor)
            } finally {
                recycle()
            }
        }
    }


}