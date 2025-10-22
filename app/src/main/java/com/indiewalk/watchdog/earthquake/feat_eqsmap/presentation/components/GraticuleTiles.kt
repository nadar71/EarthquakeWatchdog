package com.indiewalk.watchdog.earthquake.feat_eqsmap.presentation.components


import android.graphics.*
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import kotlin.math.*
import androidx.core.graphics.createBitmap

private const val TILE_SIZE = 256

private fun worldSize(zoom: Int) = TILE_SIZE * (1 shl zoom)

private fun lonToWorldX(lon: Double, zoom: Int): Double {
    val size = worldSize(zoom).toDouble()
    return (lon + 180.0) / 360.0 * size
}

private fun latToWorldY(lat: Double, zoom: Int): Double {
    val siny = sin(Math.toRadians(lat)).coerceIn(-0.9999, 0.9999)
    val y = 0.5 - (ln((1 + siny) / (1 - siny)) / (4 * Math.PI))
    return y * worldSize(zoom)
}



class GraticuleTileProvider(
    private val stepDegrees: Int = 10,        // grid degrees pace
    private val lineColor: Int = 0x66FFFFFF,  // ARGB for lines
    private val lineWidthPx: Float = 2f,
    private val labelColor: Int = 0xFFFFFFFF.toInt(),
    private val debugTextBg: Boolean = false   // set true to verify label rects
) : TileProvider {

    override fun getTile(x: Int, y: Int, zoom: Int): Tile {
        val bmp = createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineColor
            style = Paint.Style.STROKE
            strokeWidth = lineWidthPx
        }

        // ----- Zoom-aware label sizing & density -----
        // Never zero while testing --> labels are visible
        val textSizePx = when {
            zoom >= 9  -> 18f
            zoom >= 7  -> 16f
            zoom >= 5  -> 14f
            zoom >= 3  -> 12f
            else       -> 10f
        }

        // Label every N degrees zoom based, to avoid cluttering:
        val labelEvery = when {
            zoom >= 8 -> stepDegrees          // label every grid line
            zoom >= 6 -> stepDegrees * 2      // every 20°
            else      -> stepDegrees * 4      // every 40°
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = labelColor
            style = Paint.Style.FILL
            textAlign = Paint.Align.LEFT
            textSize = textSizePx
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            // Outline/shadow to keep readable on any map type:
            setShadowLayer(2f, 0f, 0f, 0xAA000000.toInt())
        }
        val fm = textPaint.fontMetrics // negative up,  positive down


        // padding avoiding labels get cut at edges
        val pad = 6f
        // Helpers to clamp inside tile
        fun clampX(xPos: Float, label: String): Float {
            val w = textPaint.measureText(label)
            return xPos.coerceIn(pad, 256f - pad - w)
        }
        fun topSafeBaseline(): Float {
            // Baseline so that top of text (baseline + ascent) is >= pad
            return pad - fm.ascent
        }
        fun baselineNear(y: Float): Float {
            // Place baseline just above y by pad, but keep full glyph within tile
            val desired = y - pad
            val minBaseline = pad - fm.ascent                // avoid top clipping
            val maxBaseline = 256f - pad - fm.descent        // avoid bottom clipping
            return desired.coerceIn(minBaseline, maxBaseline)
        }

        val tileLeft   = x * 256
        val tileTop    = y * 256
        val tileRight  = tileLeft + 256
        val tileBottom = tileTop + 256

        // ----- Meridians (constant longitude) -----
        // iterating by degrees and drawing vertical lines that intersect each tile
        for (lon in -180..180 step stepDegrees) {
            val wx = lonToWorldX(lon.toDouble(), zoom)
            if (wx >= tileLeft && wx <= tileRight) {
                val px = (wx - tileLeft).toFloat()
                canvas.drawLine(px, 0f, px, 256f, linePaint)

                if (lon % labelEvery == 0 && textSizePx > 0f) {
                    val label = if (lon == 0) "0°" else "${kotlin.math.abs(lon)}°" + if (lon < 0) "W" else "E"

                    // Baseline chosen to avoid clipping at top
                    val baseline = topSafeBaseline()
                    val xText = clampX(px + pad, label)

                    if (debugTextBg) {
                        val bounds = Rect()
                        textPaint.getTextBounds(label, 0, label.length, bounds)
                        val rectPaint = Paint().apply { color = 0x55000000 }
                        canvas.drawRect(
                            xText, baseline + fm.ascent,
                            xText + bounds.width(), baseline + fm.descent,
                            rectPaint
                        )
                    }
                    canvas.drawText(label, xText, baseline, textPaint)
                }
            }
        }

        // ----- Parallels (constant latitude) -----
        // Avoid exact poles where Mercator explodes; drawing lines that intersect this tile
        for (lat in -80..80 step stepDegrees) {
            val wy = latToWorldY(lat.toDouble(), zoom)
            if (wy >= tileTop && wy <= tileBottom) {
                val py = (wy - tileTop).toFloat()
                canvas.drawLine(0f, py, 256f, py, linePaint)

                if (lat % labelEvery == 0 && textSizePx > 0f) {
                    val label = if (lat == 0) "0°" else "${kotlin.math.abs(lat)}°" + if (lat < 0) "S" else "N"

                    // Baseline placed near the line but clamped to avoid top/bottom clipping
                    val baseline = baselineNear(py)
                    val xText = clampX(pad, label) // pin to left edge with clamp

                    if (debugTextBg) {
                        val bounds = Rect()
                        textPaint.getTextBounds(label, 0, label.length, bounds)
                        val rectPaint = Paint().apply { color = 0x55000000 }
                        canvas.drawRect(
                            xText, baseline + fm.ascent,
                            xText + bounds.width(), baseline + fm.descent,
                            rectPaint
                        )
                    }
                    canvas.drawText(label, xText, baseline, textPaint)
                }
            }
        }

        val stream = java.io.ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return Tile(256, 256, stream.toByteArray())
    }
}

