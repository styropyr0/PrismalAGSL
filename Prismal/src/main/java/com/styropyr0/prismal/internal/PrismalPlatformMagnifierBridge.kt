package com.styropyr0.prismal.internal

import android.util.Log
import android.view.View
import androidx.annotation.Keep
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.node.RootForTest
import androidx.compose.ui.semantics.SemanticsNode
import java.lang.reflect.Field
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.Collections
import java.util.WeakHashMap

/**
 * Bridge to the platform magnifier that Compose text fields show while a cursor / selection
 * handle is dragged (API 28+).
 *
 * Compose exposes no public API for it, so this relies on two internals, both looked up
 * defensively so any change in Compose degrades to "system magnifier stays" rather than a crash:
 *
 * 1. While a handle is dragged, the text field's magnifier node publishes its source position via
 *    the `MagnifierPositionInRoot` semantics property (found by key name).
 * 2. That property's value is a lambda capturing the `MagnifierNode`; swapping the node's
 *    `platformMagnifierFactory` for a no-op one stops the framework magnifier from appearing.
 *
 * Release builds keep those names through the library's consumer ProGuard rules.
 */

@Keep
internal object PrismalPlatformMagnifierBridge {
    private const val TAG = "PrismalMagnifier"
    private const val POSITION_KEY_NAME = "MagnifierPositionInRoot"

    class Source(private val position: () -> Offset) {
        fun position(): Offset =
            try {
                position.invoke()
            } catch (_: Throwable) {
                Offset.Unspecified
            }
    }

    private val silencedNodes: MutableSet<Any> = Collections.newSetFromMap(WeakHashMap())
    private var loggedFailure = false

    /**
     * Finds the magnifier source of the text field whose bounds overlap [boundsInRoot].
     * When [silenceSystemMagnifier] is true, also disables the framework magnifier for it.
     */
    fun find(view: View, boundsInRoot: Rect, silenceSystemMagnifier: Boolean): Source? {
        return try {
            val root = (view as? RootForTest)?.semanticsOwner?.unmergedRootSemanticsNode ?: return null
            val lambda = findPositionLambda(root, boundsInRoot) ?: return null
            if (silenceSystemMagnifier) silence(lambda)
            @Suppress("UNCHECKED_CAST")
            Source(lambda as () -> Offset)
        } catch (t: Throwable) {
            logOnce("Could not locate the text field magnifier", t)
            null
        }
    }

    private fun findPositionLambda(node: SemanticsNode, boundsInRoot: Rect): Any? {
        for ((key, value) in node.config) {
            if (key.name == POSITION_KEY_NAME && value is Function0<*> &&
                node.boundsInRoot.overlaps(boundsInRoot)
            ) {
                return value
            }
        }
        for (child in node.children) {
            findPositionLambda(child, boundsInRoot)?.let { return it }
        }
        return null
    }

    private fun silence(positionLambda: Any) {
        val magnifierNode = capturedMagnifierNode(positionLambda) ?: run {
            logOnce("Magnifier node not found; system magnifier stays", null)
            return
        }
        if (magnifierNode in silencedNodes) return

        val factoryField = magnifierNode.javaClass.findField("platformMagnifierFactory") ?: run {
            logOnce("platformMagnifierFactory not found; system magnifier stays", null)
            return
        }
        factoryField.set(magnifierNode, noOpFactory(factoryField.type))

        magnifierNode.javaClass.findField("magnifier")?.let { field ->
            field.get(magnifierNode)?.let { existing ->
                existing.javaClass.methods.firstOrNull { it.name == "dismiss" && it.parameterCount == 0 }
                    ?.invoke(existing)
            }
            field.set(magnifierNode, null)
        }
        silencedNodes += magnifierNode
    }

    private fun capturedMagnifierNode(lambda: Any): Any? {
        var cls: Class<*>? = lambda.javaClass
        while (cls != null && cls != Any::class.java) {
            for (field in cls.declaredFields) {
                if (field.type.isPrimitive) continue
                field.isAccessible = true
                val captured = field.get(lambda) ?: continue
                if (captured.javaClass.findField("platformMagnifierFactory") != null) return captured
            }
            cls = cls.superclass
        }
        return null
    }

    private fun noOpFactory(factoryType: Class<*>): Any {
        val magnifierType = factoryType.methods
            .firstOrNull { it.name.startsWith("create") }
            ?.returnType
            ?: error("PlatformMagnifierFactory.create not found")
        val noOpMagnifier = noOpProxy(magnifierType, null)
        return noOpProxy(factoryType, magnifierType to noOpMagnifier)
    }

    private fun noOpProxy(type: Class<*>, returns: Pair<Class<*>, Any>?): Any {
        val handler = InvocationHandler { proxy, method, args ->
            when {
                method.declaringClass == Any::class.java -> when (method.name) {
                    "equals" -> proxy === args?.getOrNull(0)
                    "hashCode" -> System.identityHashCode(proxy)
                    else -> "PrismalNoOp${type.simpleName}"
                }
                returns != null && method.returnType == returns.first -> returns.second
                method.returnType == java.lang.Boolean.TYPE -> true
                method.returnType == java.lang.Long.TYPE -> 0L
                method.returnType == java.lang.Integer.TYPE -> 0
                method.returnType == java.lang.Float.TYPE -> 0f
                else -> null
            }
        }
        return Proxy.newProxyInstance(type.classLoader, arrayOf(type), handler)
    }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null && cls != Any::class.java) {
            cls.declaredFields.firstOrNull { it.name == name }?.let {
                it.isAccessible = true
                return it
            }
            cls = cls.superclass
        }
        return null
    }

    private fun logOnce(message: String, t: Throwable?) {
        if (loggedFailure) return
        loggedFailure = true
        Log.w(TAG, message, t)
    }
}
