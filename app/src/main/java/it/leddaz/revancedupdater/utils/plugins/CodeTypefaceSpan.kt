package it.leddaz.revancedupdater.utils.plugins

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class CodeTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
    override fun updateMeasureState(textPaint: TextPaint) {
        applyTypeface(textPaint)
    }

    override fun updateDrawState(textPaint: TextPaint) {
        applyTypeface(textPaint)
    }

    private fun applyTypeface(paint: TextPaint) {
        val oldTypeface = paint.typeface
        val oldStyle = oldTypeface?.style ?: 0
        val fakeStyle = oldStyle and typeface.style.inv()

        if ((fakeStyle and Typeface.BOLD) != 0) {
            paint.isFakeBoldText = true
        }
        if ((fakeStyle and Typeface.ITALIC) != 0) {
            paint.textSkewX = -0.25f
        }

        paint.setTypeface(typeface)
    }
}
