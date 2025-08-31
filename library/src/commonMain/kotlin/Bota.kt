package com.eltonkola.bota

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.min

// A helper data class to hold the pre-parsed, renderable path information.
private data class RenderableCountry(
    val id: String,
    val name: String,
    val renderablePaths: List<Path>
)

// Truly pixel-perfect path hit detection without bounds checking
private fun isPointInPath(point: Offset, path: Path): Boolean {
    // Get bounds only to determine bitmap size, not for hit testing
    val bounds = path.getBounds()

    // Create a bitmap that's large enough to contain the path
    val padding = 10f // Extra padding to ensure we capture the full path
    val bitmapLeft = bounds.left - padding
    val bitmapTop = bounds.top - padding
    val bitmapWidth = (bounds.width + padding * 2).toInt().coerceAtLeast(1)
    val bitmapHeight = (bounds.height + padding * 2).toInt().coerceAtLeast(1)

    // Convert our test point to bitmap coordinates
    val bitmapX = (point.x - bitmapLeft).toInt()
    val bitmapY = (point.y - bitmapTop).toInt()

    // If point is outside our bitmap area, it's definitely outside the path
    if (bitmapX < 0 || bitmapX >= bitmapWidth || bitmapY < 0 || bitmapY >= bitmapHeight) {
        return false
    }

    // Create bitmap and canvas
    val bitmap = ImageBitmap(bitmapWidth, bitmapHeight)
    val canvas = Canvas(bitmap)

    // Create draw scope and render the path
    val drawScope = CanvasDrawScope()
    drawScope.draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = androidx.compose.ui.geometry.Size(bitmapWidth.toFloat(), bitmapHeight.toFloat())
    ) {
        // Fill background with black
        drawRect(
            color = Color.Black,
            size = androidx.compose.ui.geometry.Size(bitmapWidth.toFloat(), bitmapHeight.toFloat())
        )

        // Translate so the path is positioned correctly in our bitmap
        translate(-bitmapLeft, -bitmapTop) {
            // Draw the path in white
            drawPath(path, Color.White)
        }
    }

    // Read the pixel at the test point
    val pixelBuffer = IntArray(1)
    bitmap.readPixels(pixelBuffer, bitmapX, bitmapY, 1, 1)
    val pixelColor = Color(pixelBuffer[0])

    // If pixel is white (or close to white), point is inside the path
    // Check for white or near-white to handle anti-aliasing
    val red = pixelColor.red
    val green = pixelColor.green
    val blue = pixelColor.blue

    return red > 0.5f || green > 0.5f || blue > 0.5f
}

/**
 * A zoomable and pannable Jetpack Compose composable that renders a world map.
 * This version uses pixel-perfect hit detection by rendering paths to bitmaps.
 *
 * @param modifier The modifier to be applied to this composable.
 * @param highlightedCountryIds A Set of country IDs to be highlighted.
 * @param onCountryClick A lambda invoked with the `Country` object when clicked.
 * @param defaultColor The default color for countries.
 * @param highlightColor The color for highlighted countries.
 * @param strokeColor The color for country borders.
 */
@Composable
fun WorldMap(
    modifier: Modifier = Modifier,
    highlightedCountryIds: Set<String>,
    onCountryClick: (Country) -> Unit,
    defaultColor: Color = Color(0xFFECECEC),
    highlightColor: Color = Color(0xFFC8A2C8), // Lilac
    strokeColor: Color = Color.Black
) {
    val renderableCountries = remember(WorldMapPaths.data) {
        WorldMapPaths.data.map { countryPath ->
            RenderableCountry(
                id = countryPath.id,
                name = countryPath.name,
                renderablePaths = countryPath.paths.map { PathParser().parsePathString(it).toPath() }
            )
        }
    }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val svgWidth = 2000f
    val svgHeight = 857f

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(0.5f, 1000f)

                    // Calculate the zoom pivot point correctly
                    val pivotX = centroid.x - canvasSize.width / 2f
                    val pivotY = centroid.y - canvasSize.height / 2f

                    // Apply zoom scaling to current offset, then adjust for pivot
                    val newOffset = Offset(
                        x = offset.x * zoom - pivotX * (zoom - 1f) + pan.x,
                        y = offset.y * zoom - pivotY * (zoom - 1f) + pan.y
                    )

                    // Calculate bounds for the new scale
                    val contentWidth = canvasSize.width * newScale
                    val contentHeight = canvasSize.height * newScale

                    val maxOffsetX = (contentWidth - canvasSize.width).coerceAtLeast(0f) / 2f
                    val maxOffsetY = (contentHeight - canvasSize.height).coerceAtLeast(0f) / 2f

                    offset = Offset(
                        x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                        y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                    )
                    scale = newScale
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        if (canvasSize == IntSize.Zero) return@detectTapGestures

                        // Pixel-perfect hit detection
                        val clickedCountry = renderableCountries.asReversed().firstOrNull { country ->
                            country.renderablePaths.any { path ->
                                // Convert tap coordinates to SVG space
                                val fitScale = min(canvasSize.width / svgWidth, canvasSize.height / svgHeight)
                                val scaledSvgWidth = svgWidth * fitScale
                                val scaledSvgHeight = svgHeight * fitScale
                                val paddingX = (canvasSize.width - scaledSvgWidth) / 2f
                                val paddingY = (canvasSize.height - scaledSvgHeight) / 2f

                                // Convert tap to SVG coordinates
                                val svgPoint = Offset(
                                    x = (tapOffset.x - paddingX) / fitScale,
                                    y = (tapOffset.y - paddingY) / fitScale
                                )

                                // Use pixel-perfect path hit detection
                                isPointInPath(svgPoint, path)
                            }
                        }

                        clickedCountry?.let {
                            onCountryClick(Country(id = it.id, name = it.name))
                        }
                    }
                }
        ) {
            // Calculate the fit scale to maintain aspect ratio
            val fitScale = min(size.width / svgWidth, size.height / svgHeight)

            // Calculate centering offsets
            val scaledSvgWidth = svgWidth * fitScale
            val scaledSvgHeight = svgHeight * fitScale
            val paddingX = (size.width - scaledSvgWidth) / 2f
            val paddingY = (size.height - scaledSvgHeight) / 2f

            withTransform({
                translate(left = paddingX, top = paddingY)
                scale(fitScale, fitScale, pivot = Offset.Zero)
            }) {
                renderableCountries.forEach { country ->
                    val fillColor = if (country.id in highlightedCountryIds) {
                        highlightColor
                    } else {
                        defaultColor
                    }
                    country.renderablePaths.forEach { path ->
                        drawPath(path = path, color = fillColor, style = Fill)
                        drawPath(path = path, color = strokeColor, style = Stroke(width = 0.5f / fitScale))
                    }
                }
            }
        }
    }
}