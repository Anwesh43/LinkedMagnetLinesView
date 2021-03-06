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
val delay : Long = 20

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
    rotate(-90f * sf * scale.divideScale(i, lines) * sif)
    drawLine(0f, 0f, size * sf, 0f, paint)
    restore()
}

fun Canvas.drawMLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sif : Float = 1f - 2 * (i % 2)
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(gap * (i + 1), h / 2 - (h / 2 - size - paint.strokeWidth / 2) * sif * sc2)
    drawLine(-size, 0f, size, 0f, paint)
    for (j in 0..(lines - 1)) {
        save()
        drawMagnetLine(j, sc1, size, sif, paint)
        restore()
    }
    restore()
}

class MagnetLinesView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class MLNode(var i : Int, val state : State = State()) {

        private var next : MLNode? = null
        private var prev : MLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = MLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawMLNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : MLNode {
            var curr : MLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class MagnetLine(var i : Int) {

        private val root : MLNode = MLNode(0)
        private var curr : MLNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : MagnetLinesView) {

        private val animator : Animator = Animator(view)
        private val ml : MagnetLine = MagnetLine(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            ml.draw(canvas, paint)
            animator.animate {
                ml.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ml.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : MagnetLinesView {
            val view : MagnetLinesView = MagnetLinesView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
