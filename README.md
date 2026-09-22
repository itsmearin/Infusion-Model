# 3D Model Viewer - Infusory Screening Task

A high-performance 3D model viewer built with Jetpack Compose and SceneView (Filament). This application allows simultaneous rendering of multiple 3D models with independent manipulation, gesture isolation, and dynamic part labeling.

## Technical Choices
- **3D Library:** [SceneView](https://github.com/SceneView/sceneview-android) (v2.2.1).
    - **Why:** It is built on Google's Filament engine, which is optimized for mobile GPUs. It provides out-of-the-box lifecycle management and a simplified API for World-to-Screen projection, which was critical for the labeling requirement.
- **UI Toolkit:** Jetpack Compose.
    - **Why:** State management in Compose makes handling multiple independent model containers and their UI overlays (labels/buttons) much more efficient than traditional XML Views.

## Main Performance Optimizations
To meet the requirement of 30+ FPS on low-end devices (2-3GB RAM):
1. **Resource Sharing:** Used a single `ModelLoader` instance via `SceneView`. When multiple instances of the same model (e.g., two Microscopes) are loaded, the engine shares the underlying geometry and texture memory.
2. **2D Label Projection:** Instead of rendering heavy 3D text nodes inside the GLB scene, I projected 3D coordinates to a 2D Compose Canvas. Drawing 2D text and lines is significantly cheaper for the GPU.
3. **Explicit Cleanup:** Implemented `view.destroy()` inside the `onRelease` block of the `AndroidView`. This ensures that when a model is closed, Filament immediately releases GPU buffers and textures rather than waiting for the GC.
4. **Gesture Isolation:** Gestures are handled in a separate Compose layer. This prevents the 3D engine from constantly recalculating camera matrices unless the model is explicitly in "Interaction Mode."

## Trade-offs & Decisions
- **JSON Parsing:** To extract labels, I read the GLB JSON chunk manually. This avoids loading the entire model structure twice and allows for instantaneous label availability before the model fully renders.
- **Compose Layering:** Each model sits in its own `Box`. While this adds a small amount of overhead to the Compose UI tree, it ensures that moving one model doesn't trigger a recomposition of the other 3D views.

## Known Limitations / Future Improvements
- **Shadows:** Dynamic shadows are disabled to maintain high FPS on older GPUs. With more time, I would implement baked shadows or simplified contact shadows.
- **Depth Sorting:** Since labels are 2D overlays, they currently do not "hide" behind the model if the part is on the opposite side. I would implement an occlusion check using raycasting if given more time.

## Testing Devices
- **Tested on:** [Medium Phone API 35]
- **RAM:** [5GB]
- **Performance:** Maintained a steady 60fps with 5 models loaded.
