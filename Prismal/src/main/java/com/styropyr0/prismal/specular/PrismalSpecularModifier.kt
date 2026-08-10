package com.styropyr0.prismal.specular

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastCoerceAtMost
import com.styropyr0.prismal.PrismalShaderCacheImpl
import com.styropyr0.prismal.internal.PrismalShapeLayout
import com.styropyr0.prismal.internal.blur
import com.styropyr0.prismal.internal.clipOutline
import com.styropyr0.prismal.internal.setAGSLShader
import com.styropyr0.prismal.isAGSLShaderSupported
import kotlin.math.ceil

internal class PrismalSpecularElement(
    val shapeProvider: PrismalShapeLayout,
    val specular: () -> PrismalSpecular?
) : ModifierNodeElement<PrismalSpecularNode>() {

    override fun create(): PrismalSpecularNode {
        return PrismalSpecularNode(shapeProvider, specular)
    }

    override fun update(node: PrismalSpecularNode) {
        node.shapeProvider = shapeProvider
        node.specular = specular
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "prismalSpecular"
        properties["shapeProvider"] = shapeProvider
        properties["specular"] = specular
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrismalSpecularElement) return false

        if (shapeProvider != other.shapeProvider) return false
        if (specular != other.specular) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shapeProvider.hashCode()
        result = 31 * result + specular.hashCode()
        return result
    }
}

internal class PrismalSpecularNode(
    var shapeProvider: PrismalShapeLayout,
    var specular: () -> PrismalSpecular?
) : DrawModifierNode, Modifier.Node() {

    override val shouldAutoInvalidate: Boolean = false

    private var specularLayer: GraphicsLayer? = null

    private val paint =
        Paint().apply {
            style = PaintingStyle.Stroke
        }
    private var clipPath: Path? = null

    private val agslShaderCache = PrismalShaderCacheImpl()

    private var prevStyle: PrismalSpecularStyle? = null

    override fun ContentDrawScope.draw() {
        val specular = specular()
        if (specular == null || specular.width.value <= 0f) {
            return drawContent()
        }

        drawContent()

        val specularLayer = specularLayer
        if (specularLayer != null) {
            val size = size
            val density: Density = this
            val layoutDirection = layoutDirection

            val safeSize =
                IntSize(
                    ceil(size.width).toInt() + 2,
                    ceil(size.height).toInt() + 2
                )

            val outline = shapeProvider.innerShape.createOutline(size, layoutDirection, density)
            val clipPath =
                if (outline is Outline.Rounded) {
                    clipPath ?: Path().also { clipPath = it }
                } else {
                    null
                }

            configurePaint(specular)

            specularLayer.alpha = specular.alpha
            specularLayer.blendMode = specular.style.blendMode
            specularLayer.record(safeSize) {
                translate(1f, 1f) {
                    val canvas = drawContext.canvas
                    canvas.save()
                    canvas.clipOutline(outline, clipPath)
                    canvas.drawOutline(outline, paint)
                    canvas.restore()
                }
            }

            translate(-1f, -1f) {
                drawLayer(specularLayer)
            }
        }
    }

    override fun onAttach() {
        val graphicsContext = requireGraphicsContext()
        specularLayer = graphicsContext.createGraphicsLayer()
    }

    override fun onDetach() {
        val graphicsContext = requireGraphicsContext()
        specularLayer?.let { layer ->
            graphicsContext.releaseGraphicsLayer(layer)
            specularLayer = null
        }
        clipPath = null
        agslShaderCache.clear()
        prevStyle = null
    }

    private fun DrawScope.configurePaint(specular: PrismalSpecular) {
        paint.color = specular.style.color
        paint.strokeWidth = ceil(specular.width.toPx().fastCoerceAtMost(size.minDimension / 2f)) * 2f
        paint.blur(specular.blurRadius.toPx())
        if (isAGSLShaderSupported()) {
            val shader =
                with(specular.style) {
                    createShader(
                        shape = shapeProvider.innerShape,
                        runtimeShaderCache = agslShaderCache
                    )
                }
            paint.setAGSLShader(shader)
        }
    }
}
