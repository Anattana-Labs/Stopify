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

public val Icons.Rounded.Shuffle: ImageVector
    get() {
        if (_shuffle != null) {
            return _shuffle!!
        }
        _shuffle = materialIcon(name = "Rounded.Shuffle") {
            materialPath {
                moveTo(10.59f, 9.17f)
                lineTo(6.12f, 4.7f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(4.46f, 4.46f)
                lineToRelative(1.42f, -1.4f)
                close()
                moveTo(15.35f, 4.85f)
                lineToRelative(1.19f, 1.19f)
                lineTo(4.7f, 17.88f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(17.96f, 7.46f)
                lineToRelative(1.19f, 1.19f)
                curveToRelative(0.31f, 0.31f, 0.85f, 0.09f, 0.85f, -0.36f)
                lineTo(20.0f, 4.5f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                horizontalLineToRelative(-3.79f)
                curveToRelative(-0.45f, 0.0f, -0.67f, 0.54f, -0.36f, 0.85f)
                close()
                moveTo(14.83f, 13.41f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(3.13f, 3.13f)
                lineToRelative(-1.2f, 1.2f)
                curveToRelative(-0.31f, 0.31f, -0.09f, 0.85f, 0.36f, 0.85f)
                horizontalLineToRelative(3.79f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-3.79f)
                curveToRelative(0.0f, -0.45f, -0.54f, -0.67f, -0.85f, -0.35f)
                lineToRelative(-1.19f, 1.19f)
                lineToRelative(-3.13f, -3.14f)
                close()
            }
        }
        return _shuffle!!
    }

private var _shuffle: ImageVector? = null
