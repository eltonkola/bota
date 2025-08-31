package com.eltonkola.bota
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

// A helper data class to hold the pre-parsed, renderable path information.
private data class RenderableCountry(
    val id: String,
    val name: String,
    val renderablePaths: List<Path>
)

/**
 * A zoomable and pannable Jetpack Compose composable that renders a world map.
 * This version uses Modifier.graphicsLayer for high-performance transformations
 * and correctly handles coordinate space transformations for accurate interactions.
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
    val renderableCountries = remember {
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
            .aspectRatio(svgWidth / svgHeight)
            .clipToBounds() // <-- THE FIX IS HERE
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 5f)

                    val xBound = (canvasSize.width * (newScale - 1)) / 2f
                    val yBound = (canvasSize.height * (newScale - 1)) / 2f

                    val newOffset = offset + pan

                    offset = Offset(
                        x = newOffset.x.coerceIn(-xBound, xBound),
                        y = newOffset.y.coerceIn(-yBound, yBound)
                    )
                    scale = newScale
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    if (canvasSize == IntSize.Zero) return@detectTapGestures

                    // Correctly transform the tap coordinates from screen space to SVG space
                    val transformedX = (tapOffset.x - canvasSize.width / 2f - offset.x) / scale + canvasSize.width / 2f
                    val transformedY = (tapOffset.y - canvasSize.height / 2f - offset.y) / scale + canvasSize.height / 2f

                    val initialFitScale = canvasSize.width / svgWidth
                    val svgTapOffset = Offset(
                        x = transformedX / initialFitScale,
                        y = transformedY / initialFitScale
                    )

                    val clickedCountry = renderableCountries.lastOrNull { country ->
                        country.renderablePaths.any { path ->
                            path.getBounds().contains(svgTapOffset)
                        }
                    }

                    clickedCountry?.let {
                        onCountryClick(Country(id = it.id, name = it.name))
                    }
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
        ) {
            val canvasToSvgScale = size.width / svgWidth
            withTransform({
                scale(canvasToSvgScale, canvasToSvgScale, pivot = Offset.Zero)
            }) {
                renderableCountries.forEach { country ->
                    val fillColor = if (country.id in highlightedCountryIds) {
                        highlightColor
                    } else {
                        defaultColor
                    }
                    country.renderablePaths.forEach { path ->
                        drawPath(path = path, color = fillColor, style = Fill)
                        drawPath(path = path, color = strokeColor, style = Stroke(width = 0.5f / canvasToSvgScale))
                    }
                }
            }
        }
    }
}