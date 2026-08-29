package hu.patrikos.tv

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * Lightweight, fully static child-room background for Elena TV.
 * Drawn with Canvas primitives so it adds no bitmap decode or animation cost.
 */
class BlueyBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val s = w / 1920f

        // Sky
        paint.color = Color.rgb(91, 180, 241)
        canvas.drawRect(0f, 0f, w, h * 0.68f, paint)
        paint.color = Color.rgb(151, 214, 247)
        canvas.drawRect(0f, h * 0.38f, w, h * 0.68f, paint)

        drawCloud(canvas, 260f * s, 185f * s, 1.05f * s)
        drawCloud(canvas, 970f * s, 245f * s, 0.7f * s)
        drawCloud(canvas, 1530f * s, 170f * s, 0.9f * s)

        // Distant hills
        paint.color = Color.rgb(120, 194, 133)
        val hills = Path().apply {
            moveTo(0f, h * 0.68f)
            cubicTo(w * 0.16f, h * 0.52f, w * 0.34f, h * 0.66f, w * 0.5f, h * 0.58f)
            cubicTo(w * 0.68f, h * 0.49f, w * 0.83f, h * 0.65f, w, h * 0.54f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        canvas.drawPath(hills, paint)

        // Grass foreground
        paint.color = Color.rgb(190, 219, 91)
        canvas.drawRect(0f, h * 0.70f, w, h, paint)
        paint.color = Color.rgb(160, 205, 76)
        canvas.drawOval(w * 0.18f, h * 0.82f, w * 0.46f, h * 0.96f, paint)
        canvas.drawOval(w * 0.62f, h * 0.80f, w * 0.93f, h * 0.96f, paint)

        // Simple playful flowers/toys
        drawFlower(canvas, 520f * s, h - 82f * s, 10f * s, Color.rgb(255, 149, 190))
        drawFlower(canvas, 760f * s, h - 110f * s, 8f * s, Color.rgb(250, 207, 76))
        drawBall(canvas, 930f * s, h - 92f * s, 30f * s)

        // Bluey and Bingo frame the launcher without sitting behind the app cards.
        drawBluey(canvas, 165f * s, h - 32f * s, 1.0f * s)
        drawBingo(canvas, 1755f * s, h - 32f * s, 0.96f * s)
    }

    private fun drawCloud(canvas: Canvas, x: Float, y: Float, scale: Float) {
        paint.color = Color.argb(210, 255, 255, 255)
        canvas.drawCircle(x, y, 34f * scale, paint)
        canvas.drawCircle(x + 40f * scale, y - 16f * scale, 44f * scale, paint)
        canvas.drawCircle(x + 84f * scale, y, 32f * scale, paint)
        canvas.drawRoundRect(
            x - 28f * scale,
            y,
            x + 112f * scale,
            y + 34f * scale,
            18f * scale,
            18f * scale,
            paint
        )
    }

    private fun drawFlower(canvas: Canvas, x: Float, y: Float, r: Float, color: Int) {
        paint.color = color
        repeat(5) { i ->
            val angle = Math.toRadians((i * 72.0) - 90.0)
            canvas.drawCircle(
                x + kotlin.math.cos(angle).toFloat() * r * 1.2f,
                y + kotlin.math.sin(angle).toFloat() * r * 1.2f,
                r,
                paint
            )
        }
        paint.color = Color.rgb(255, 224, 91)
        canvas.drawCircle(x, y, r * 0.7f, paint)
    }

    private fun drawBall(canvas: Canvas, x: Float, y: Float, r: Float) {
        paint.color = Color.rgb(158, 108, 211)
        canvas.drawCircle(x, y, r, paint)
        paint.color = Color.rgb(238, 219, 250)
        canvas.drawCircle(x - r * 0.35f, y - r * 0.30f, r * 0.18f, paint)
        canvas.drawCircle(x + r * 0.30f, y + r * 0.15f, r * 0.22f, paint)
    }

    private fun drawBluey(canvas: Canvas, cx: Float, bottom: Float, scale: Float) {
        canvas.save()
        canvas.translate(cx, bottom)
        canvas.scale(scale, scale)

        val navy = Color.rgb(38, 61, 105)
        val blue = Color.rgb(73, 157, 216)
        val lightBlue = Color.rgb(128, 202, 235)
        val cream = Color.rgb(238, 197, 101)

        // Tail
        paint.color = navy
        canvas.drawOval(-118f, -102f, -42f, -58f, paint)

        // Body
        paint.color = blue
        canvas.drawOval(-78f, -160f, 78f, -18f, paint)
        paint.color = lightBlue
        canvas.drawOval(-48f, -118f, 46f, -28f, paint)

        // Legs
        paint.color = blue
        canvas.drawRoundRect(-68f, -62f, -24f, 0f, 20f, 20f, paint)
        canvas.drawRoundRect(28f, -62f, 72f, 0f, 20f, 20f, paint)

        // Head
        paint.color = blue
        canvas.drawRoundRect(-78f, -282f, 78f, -118f, 55f, 55f, paint)

        // Ears
        paint.color = navy
        val leftEar = Path().apply {
            moveTo(-70f, -250f); lineTo(-52f, -348f); lineTo(-10f, -270f); close()
        }
        val rightEar = Path().apply {
            moveTo(20f, -272f); lineTo(54f, -350f); lineTo(72f, -245f); close()
        }
        canvas.drawPath(leftEar, paint)
        canvas.drawPath(rightEar, paint)
        paint.color = cream
        canvas.drawPath(Path().apply {
            moveTo(-56f, -280f); lineTo(-49f, -322f); lineTo(-25f, -278f); close()
        }, paint)
        canvas.drawPath(Path().apply {
            moveTo(34f, -283f); lineTo(52f, -326f); lineTo(61f, -274f); close()
        }, paint)

        // Face patch / muzzle
        paint.color = lightBlue
        canvas.drawOval(-66f, -260f, 18f, -158f, paint)
        paint.color = cream
        canvas.drawOval(-12f, -205f, 80f, -135f, paint)

        // Eyes
        paint.color = Color.WHITE
        canvas.drawOval(-45f, -245f, -2f, -178f, paint)
        canvas.drawOval(-2f, -242f, 39f, -176f, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(-19f, -203f, 8f, paint)
        canvas.drawCircle(19f, -201f, 8f, paint)

        // Nose + smile
        paint.color = navy
        canvas.drawOval(51f, -194f, 84f, -163f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.color = Color.rgb(80, 58, 50)
        canvas.drawArc(12f, -174f, 62f, -132f, 15f, 130f, false, paint)
        paint.style = Paint.Style.FILL

        // Arms waving
        paint.color = blue
        canvas.drawRoundRect(-118f, -170f, -58f, -137f, 16f, 16f, paint)
        canvas.drawRoundRect(58f, -170f, 118f, -137f, 16f, 16f, paint)

        canvas.restore()
    }

    private fun drawBingo(canvas: Canvas, cx: Float, bottom: Float, scale: Float) {
        canvas.save()
        canvas.translate(cx, bottom)
        canvas.scale(scale, scale)

        val orange = Color.rgb(231, 118, 52)
        val darkOrange = Color.rgb(184, 74, 42)
        val lightOrange = Color.rgb(248, 174, 93)
        val cream = Color.rgb(255, 218, 155)

        // Tail
        paint.color = darkOrange
        canvas.drawOval(45f, -110f, 126f, -55f, paint)

        // Body
        paint.color = orange
        canvas.drawOval(-80f, -158f, 80f, -18f, paint)
        paint.color = cream
        canvas.drawOval(-46f, -116f, 46f, -28f, paint)

        // Legs
        paint.color = orange
        canvas.drawRoundRect(-70f, -62f, -26f, 0f, 20f, 20f, paint)
        canvas.drawRoundRect(26f, -62f, 70f, 0f, 20f, 20f, paint)

        // Head
        paint.color = orange
        canvas.drawRoundRect(-78f, -282f, 78f, -118f, 55f, 55f, paint)

        // Ears
        paint.color = darkOrange
        canvas.drawPath(Path().apply {
            moveTo(-70f, -250f); lineTo(-52f, -348f); lineTo(-10f, -270f); close()
        }, paint)
        canvas.drawPath(Path().apply {
            moveTo(20f, -272f); lineTo(54f, -350f); lineTo(72f, -245f); close()
        }, paint)
        paint.color = cream
        canvas.drawPath(Path().apply {
            moveTo(-56f, -280f); lineTo(-49f, -322f); lineTo(-25f, -278f); close()
        }, paint)
        canvas.drawPath(Path().apply {
            moveTo(34f, -283f); lineTo(52f, -326f); lineTo(61f, -274f); close()
        }, paint)

        // Face + muzzle
        paint.color = lightOrange
        canvas.drawOval(-68f, -260f, 18f, -158f, paint)
        paint.color = cream
        canvas.drawOval(-15f, -205f, 80f, -135f, paint)

        // Eyes
        paint.color = Color.WHITE
        canvas.drawOval(-45f, -245f, -2f, -178f, paint)
        canvas.drawOval(-2f, -242f, 39f, -176f, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(-18f, -202f, 8f, paint)
        canvas.drawCircle(18f, -200f, 8f, paint)

        // Nose + smile
        paint.color = darkOrange
        canvas.drawOval(50f, -194f, 83f, -163f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.color = Color.rgb(97, 55, 42)
        canvas.drawArc(10f, -175f, 63f, -132f, 15f, 130f, false, paint)
        paint.style = Paint.Style.FILL

        // Arms waving
        paint.color = orange
        canvas.drawRoundRect(-120f, -174f, -60f, -140f, 16f, 16f, paint)
        canvas.drawRoundRect(60f, -174f, 120f, -140f, 16f, 16f, paint)

        canvas.restore()
    }
}
