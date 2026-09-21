package com.example.infusion.glb

import com.example.infusion.model.LabelData
import org.json.JSONObject
import android.util.Log

object GlbLabelParser {
    fun parse(jsonString: String): List<LabelData> {
        val labels = mutableListOf<LabelData>()
        try {
            val json = JSONObject(jsonString)
            if (!json.has("nodes")) return emptyList()

            val nodes = json.getJSONArray("nodes")
            for (i in 0 until nodes.length()) {
                val node = nodes.getJSONObject(i)
                if (node.has("extras")) {
                    val extras = node.getJSONObject("extras")
                    if (extras.has("prop")) {
                        labels.add(
                            LabelData(
                                nodeName = node.optString("name", "Unknown"),
                                label = extras.getString("prop"),
                                x = 0f,
                                y = 0f
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GlbLabelParser", "Error parsing GLB extras: ${e.message}")
        }
        return labels
    }
}