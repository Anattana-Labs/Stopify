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

public val Icons.Rounded.Key: ImageVector
    get() {
        if (_key != null) {
            return _key!!
        }
        _key = materialIcon(name = "Rounded.Key") {
            materialPath {
                moveTo(20.59f, 10.0f)
                horizontalLineToRelative(-7.94f)
                curveTo(11.7f, 7.31f, 8.89f, 5.5f, 5.77f, 6.12f)
                curveToRelative(-2.29f, 0.46f, -4.15f, 2.3f, -4.63f, 4.58f)
                curveTo(0.32f, 14.58f, 3.26f, 18.0f, 7.0f, 18.0f)
                curveToRelative(2.61f, 0.0f, 4.83f, -1.67f, 5.65f, -4.0f)
                horizontalLineTo(13.0f)
                lineToRelative(1.29f, 1.29f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(17.0f, 14.0f)
                lineToRelative(1.29f, 1.29f)
                curveToRelative(0.39f, 0.39f, 1.03f, 0.39f, 1.42f, 0.0f)
                lineToRelative(2.59f, -2.61f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.03f, -0.01f, -1.42f)
                lineToRelative(-0.99f, -0.97f)
                curveTo(21.1f, 10.1f, 20.85f, 10.0f, 20.59f, 10.0f)
                close()
                moveTo(7.0f, 15.0f)
                curveToRelative(-1.65f, 0.0f, -3.0f, -1.35f, -3.0f, -3.0f)
                curveToRelative(0.0f, -1.65f, 1.35f, -3.0f, 3.0f, -3.0f)
                reflectiveCurveToRelative(3.0f, 1.35f, 3.0f, 3.0f)
                curveTo(10.0f, 13.65f, 8.65f, 15.0f, 7.0f, 15.0f)
                close()
            }
        }
        return _key!!
    }

private var _key: ImageVector? = null
