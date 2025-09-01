package com.composables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ZoomIn: ImageVector
    get() {
        if (_ZoomIn != null) return _ZoomIn!!

        _ZoomIn = ImageVector.Builder(
            name = "ZoomIn",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19f, 11f)
                arcTo(8f, 8f, 0f, false, true, 11f, 19f)
                arcTo(8f, 8f, 0f, false, true, 3f, 11f)
                arcTo(8f, 8f, 0f, false, true, 19f, 11f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(21f, 21f)
                lineToRelative(-4.35f, -4.35f)
                moveTo(11f, 8f)
                verticalLineToRelative(6f)
                moveToRelative(-3f, -3f)
                horizontalLineToRelative(6f)
            }
        }.build()

        return _ZoomIn!!
    }

private var _ZoomIn: ImageVector? = null


