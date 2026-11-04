package com.styropyr0.prismal.internal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.styropyr0.prismal.canClipToPathOutline
import com.styropyr0.prismal.shapes.PrismalRoundedRectangularShape

@Immutable
internal class PrismalShapeLayout(val shapeBlock: () -> Shape) {
    private var _shape: Shape? = null
    private var _outline: Outline? = null
    private var _size: Size = Size.Unspecified
    private var _layoutDirection: LayoutDirection? = null
    private var _density: Float? = null

    val innerShape
        get() = shapeBlock()

    val shape = object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val shape = shapeBlock()
            if (_shape != shape) {
                _shape = shape
                _outline = null
            }
            if (_outline == null || _size != size || _layoutDirection != layoutDirection || _density != density.density) {
                _size = size
                _layoutDirection = layoutDirection
                _density = density.density
                _outline = shape.createOutline(size, layoutDirection, density)
                    .withFastClipFallback(shape, size, layoutDirection, density)
            }

            return _outline!!
        }
    }
}

/**
 * Below API 33 a path outline (continuous-curvature corners) can't be clipped natively, so
 * Compose clips with a non-antialiased `clipPath` on every draw. Swap it for the equivalent
 * circular rounded rectangle there, which uses the fast, antialiased outline clip. The
 * difference between the two corner curves is barely visible.
 */
private fun Outline.withFastClipFallback(
    shape: Shape,
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density
): Outline {
    if (this !is Outline.Generic || canClipToPathOutline()) return this
    if (shape !is PrismalRoundedRectangularShape) return this
    val maxRadius = size.minDimension / 2f
    val corners = shape.corners(size, layoutDirection, density)
    return Outline.Rounded(
        RoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            topLeftCornerRadius = CornerRadius(corners.topLeft.coerceAtMost(maxRadius)),
            topRightCornerRadius = CornerRadius(corners.topRight.coerceAtMost(maxRadius)),
            bottomRightCornerRadius = CornerRadius(corners.bottomRight.coerceAtMost(maxRadius)),
            bottomLeftCornerRadius = CornerRadius(corners.bottomLeft.coerceAtMost(maxRadius))
        )
    )
}
