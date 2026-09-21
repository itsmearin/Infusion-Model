package com.example.infusion.ui.theme

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.children
import com.example.infusion.glb.GlbLabelParser
import com.example.infusion.model.LabelData
import com.example.infusion.model.ModelItem
import io.github.sceneview.SceneView
import io.github.sceneview.collision.Vector3
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Composable
fun ModelContainer(
    modelItem: ModelItem,
    onDelete: () -> Unit,
    onUpdate: (ModelItem) -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableStateOf(1f) }
    val projectedLabels = remember { mutableStateListOf<LabelData>() }

    LaunchedEffect(modelItem.modelFile) {
        val json = extractGlbJsonChunk(context, "models/${modelItem.modelFile}")
        json?.let {
            val labels = GlbLabelParser.parse(it)
            projectedLabels.clear()
            projectedLabels.addAll(labels)
        }
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(modelItem.x.toInt(), modelItem.y.toInt()) }
            .size((modelItem.width * scale).dp, (modelItem.height * scale).dp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val sceneView = SceneView(ctx)

                val modelInstance = try {
                    sceneView.modelLoader.createModelInstance("models/${modelItem.modelFile.trim()}")
                } catch (e: Exception) {
                    Log.e("ModelContainer", "Asset not found: models/${modelItem.modelFile}", e)
                    null
                }

                modelInstance?.let { instance ->
                    val modelNode = ModelNode(instance).apply {
                        position = Position(0f, 0f, 0f)
                    }
                    sceneView.addChildNode(modelNode)

                    sceneView.onFrame = { _ ->
                        if (modelItem.showLabels) {
                            projectedLabels.forEachIndexed { index, label ->
                                val target = findNodeByName(modelNode, label.nodeName)
                                if (target != null) {
                                    val worldPos = Vector3(
                                        target.position.x,
                                        target.position.y,
                                        target.position.z
                                    )
                                    val screenPoint = sceneView.cameraNode.worldToScreenPoint(worldPos)
                                    projectedLabels[index] = label.copy(
                                        x = screenPoint.x,
                                        y = screenPoint.y
                                    )
                                }
                            }
                        }
                    }
                }

                sceneView
            },
            update = { view ->
            },
            onRelease = { view ->
                view.destroy()
            }
        )

        if (!modelItem.interactionMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 5.0f)
                            onUpdate(modelItem.copy(
                                x = modelItem.x + pan.x,
                                y = modelItem.y + pan.y
                            ))
                        }
                    }
            )
        }

        if (modelItem.showLabels) {
            LabelOverlay(labels = projectedLabels)
        }

        ControlButtons(
            modelItem = modelItem,
            onToggleInteraction = { onUpdate(modelItem.copy(interactionMode = !modelItem.interactionMode)) },
            onToggleLabels = { onUpdate(modelItem.copy(showLabels = !modelItem.showLabels)) },
            onClose = onDelete
        )
    }
}

// Custom local recursive member helper function bypassing broken package extensions
private fun findNodeByName(root: Node, name: String): Node? {
    if (root.name == name) return root
    val childrenList = root.childNodes
    for (child in childrenList) {
        val found = findNodeByName(child, name)
        if (found != null) return found
    }
    return null
}

@Composable
fun LabelOverlay(labels: List<LabelData>) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            labels.forEach { label ->
                if (label.x != 0f || label.y != 0f) {
                    drawLine(
                        color = Color.Cyan,
                        start = Offset(label.x, label.y),
                        end = Offset(label.x + 30f, label.y - 30f),
                        strokeWidth = 2f
                    )
                }
            }
        }
        labels.forEach { label ->
            if (label.x != 0f || label.y != 0f) {
                Text(
                    text = label.label,
                    fontSize = 9.sp,
                    color = Color.White,
                    modifier = Modifier
                        .offset { IntOffset(label.x.toInt() + 35, label.y.toInt() - 50) }
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(2.dp)
                )
            }
        }
    }
}

@Composable
fun ControlButtons(
    modelItem: ModelItem,
    onToggleInteraction: () -> Unit,
    onToggleLabels: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(Color.DarkGray.copy(alpha = 0.6f))
            .padding(2.dp)
    ) {
        IconButton(onClick = onToggleInteraction) {
            Text(if (modelItem.interactionMode) "MOVE" else "3D", color = Color.White, fontSize = 10.sp)
        }
        IconButton(onClick = onToggleLabels) {
            Text("LBL", color = if (modelItem.showLabels) Color.Green else Color.White, fontSize = 10.sp)
        }
        IconButton(onClick = onClose) {
            Text("X", color = Color.Red, fontSize = 10.sp)
        }
    }
}

private fun extractGlbJsonChunk(context: Context, assetPath: String): String? {
    return try {
        context.assets.open(assetPath).use { stream ->
            val header = ByteArray(20)
            stream.read(header)
            val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
            buffer.position(12)
            val jsonLength = buffer.int
            val jsonBytes = ByteArray(jsonLength)
            stream.read(jsonBytes)
            String(jsonBytes, Charsets.UTF_8)
        }
    } catch (e: Exception) {
        null
    }
}