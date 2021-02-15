package com.example.aimsandroid.utils

import android.animation.ValueAnimator
import android.widget.TextView

class NumberAnimatorUtil {
    companion object NumberAnimator {
        fun animateCoordinates(view: TextView, begin: Double, end: Double) {
            val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(begin.toFloat(), end.toFloat())
            valueAnimator.duration =1000 //in millis
            valueAnimator.addUpdateListener{
                    animation ->  view.text = String.format("%.08f", animation.animatedValue)
            }
            valueAnimator.start()
        }
    }
}