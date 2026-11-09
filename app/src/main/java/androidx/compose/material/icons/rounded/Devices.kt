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

public val Icons.Rounded.Devices: ImageVector
    get() {
        if (_devices != null) {
            return _devices!!
        }
        _devices = materialIcon(name = "Rounded.Devices") {
            materialPath {
                moveTo(4.0f, 7.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(11.0f)
                horizontalLineToRelative(-0.5f)
                curveToRelative(-0.83f, 0.0f, -1.5f, 0.67f, -1.5f, 1.5f)
                reflectiveCurveTo(0.67f, 20.0f, 1.5f, 20.0f)
                lineTo(14.0f, 20.0f)
                verticalLineToRelative(-3.0f)
                lineTo(4.0f, 17.0f)
                lineTo(4.0f, 7.0f)
                close()
                moveTo(23.0f, 8.0f)
                horizontalLineToRelative(-6.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(24.0f, 9.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(22.0f, 17.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-7.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(7.0f)
                close()
            }
        }
        return _devices!!
    }

private var _devices: ImageVector? = null
