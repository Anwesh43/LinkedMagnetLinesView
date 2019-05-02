package com.anwesh.uiprojects.magnetlinesview

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Canvas
import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context

/**
 * Created by anweshmishra on 02/05/19.
 */

val lines : Int = 2
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val nodes : Int = 5
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val parts : Int = 2

fun Int.inverse() : Float = 1f / this
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.mirrorValue(a : Int, b : Int) : Float {
    val k : Float = scaleFactor()
    return (1 - k) * a.inverse() + k * b.inverse()
}
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawMagnetLine(i :Int, scale : Float, size : Float, sif : Float, paint : Paint) {
    val sf : Float = 1f - 2 * i
    save()
    translate(-size + i * 2 * size, 0f)
    rotate(-90f * sf * scale.divideScale(i, parts) * sif)
    drawLine(0f, 0f, size * sf, 0f, paint)
    restore()
}

fun Canvas.drawMLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (lines + 1)
    val size : Float = gap / sizeFactor
    val sif : Float = 1f - 2 * (i % 2)
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(gap * (i + 1), h / 2 - (h / 2) * sif * sc2)
    for (j in 0..(lines - 1)) {
        save()
        drawMagnetLine(j, sc1, size, sif, paint)
        restore()
    }
    restore()
}