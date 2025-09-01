package com.eltonkola.bota

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.composables.ChevronDown
import com.composables.ZoomIn
import com.composables.ZoomOut

/**
 * A set of controls for panning and zooming a map.
 * This version uses directional callbacks for a more robust API.
 *
 * @param onZoomIn Callback for the zoom in button.
 * @param onZoomOut Callback for the zoom out button.
 * @param onPanUp Callback for the pan up button.
 * @param onPanDown Callback for the pan down button.
 * @param onPanLeft Callback for the pan left button.
 * @param onPanRight Callback for the pan right button.
 */
@Composable
fun MapControls(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onPanUp: () -> Unit,
    onPanDown: () -> Unit,
    onPanLeft: () -> Unit,
    onPanRight: () -> Unit
) {
    // A decorative surface to make the controls stand out
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
     //   shadowElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Pan Controls ---
            IconButton(onClick = onPanUp) {
                Icon(ChevronDown, contentDescription = "Pan Up", Modifier.rotate(180f))
            }
            Row {
                IconButton(onClick = onPanLeft) {
                    Icon(ChevronDown, contentDescription = "Pan Left", Modifier.rotate(90f))
                }
                IconButton(onClick = onPanRight) {
                    Icon(ChevronDown, contentDescription = "Pan Right", Modifier.rotate(-90f))
                }
            }
            IconButton(onClick = onPanDown) {
                Icon(ChevronDown, contentDescription = "Pan Down")
            }

            // --- Zoom Controls ---
            IconButton(onClick = onZoomIn) {
                Icon(ZoomIn, contentDescription = "Zoom In")
            }
            IconButton(onClick = onZoomOut) {
                Icon(ZoomOut, contentDescription = "Zoom Out")
            }
        }
    }
}