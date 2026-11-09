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

public val Icons.Rounded.Lyrics: ImageVector
    get() {
        if (_lyrics != null) {
            return _lyrics!!
        }
        _lyrics = materialIcon(name = "Rounded.Lyrics") {
            materialPath {
                moveTo(14.0f, 9.0f)
                curveToRelative(0.0f, -2.04f, 1.24f, -3.79f, 3.0f, -4.57f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 2.0f, 2.01f, 2.9f, 2.01f, 4.0f)
                lineTo(2.0f, 22.0f)
                lineToRelative(4.0f, -4.0f)
                horizontalLineToRelative(9.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-2.42f)
                curveTo(15.24f, 12.8f, 14.0f, 11.05f, 14.0f, 9.0f)
                close()
                moveTo(10.0f, 14.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(13.0f, 11.0f)
                horizontalLineTo(6.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(13.0f, 8.0f)
                horizontalLineTo(6.0f)
                verticalLineTo(6.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(8.0f)
                close()
            }
            materialPath {
                moveTo(20.0f, 6.18f)
                curveTo(19.69f, 6.07f, 19.35f, 6.0f, 19.0f, 6.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(1.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(6.18f)
                close()
            }
        }
        return _lyrics!!
    }

private var _lyrics: ImageVector? = null
