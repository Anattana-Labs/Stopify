/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.material.icons.rounded

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Rounded.SwapVert: ImageVector
    get() {
        if (_swapVert != null) {
            return _swapVert!!
        }
        _swapVert = materialIcon(name = "Rounded.SwapVert") {
            materialPath {
                moveTo(16.0f, 17.01f)
                verticalLineTo(11.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(6.01f)
                horizontalLineToRelative(-1.79f)
                curveToRelative(-0.45f, 0.0f, -0.67f, 0.54f, -0.35f, 0.85f)
                lineToRelative(2.79f, 2.78f)
                curveToRelative(0.2f, 0.19f, 0.51f, 0.19f, 0.71f, 0.0f)
                lineToRelative(2.79f, -2.78f)
                curveToRelative(0.32f, -0.31f, 0.09f, -0.85f, -0.35f, -0.85f)
                horizontalLineTo(16.0f)
                close()
                moveTo(8.65f, 3.35f)
                lineTo(5.86f, 6.14f)
                curveToRelative(-0.32f, 0.31f, -0.1f, 0.85f, 0.35f, 0.85f)
                horizontalLineTo(8.0f)
                verticalLineTo(13.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(6.99f)
                horizontalLineToRelative(1.79f)
                curveToRelative(0.45f, 0.0f, 0.67f, -0.54f, 0.35f, -0.85f)
                lineTo(9.35f, 3.35f)
                curveToRelative(-0.19f, -0.19f, -0.51f, -0.19f, -0.7f, 0.0f)
                close()
            }
        }
        return _swapVert!!
    }

private var _swapVert: ImageVector? = null
