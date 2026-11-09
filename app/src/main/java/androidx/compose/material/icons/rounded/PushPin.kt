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
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Rounded.PushPin: ImageVector
    get() {
        if (_pushPin != null) {
            return _pushPin!!
        }
        _pushPin = materialIcon(name = "Rounded.PushPin") {
            materialPath(pathFillType = EvenOdd) {
                moveTo(19.0f, 12.87f)
                curveToRelative(0.0f, -0.47f, -0.34f, -0.85f, -0.8f, -0.98f)
                curveTo(16.93f, 11.54f, 16.0f, 10.38f, 16.0f, 9.0f)
                verticalLineTo(4.0f)
                lineToRelative(1.0f, 0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(7.0f)
                curveTo(6.45f, 2.0f, 6.0f, 2.45f, 6.0f, 3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                lineToRelative(1.0f, 0.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 1.38f, -0.93f, 2.54f, -2.2f, 2.89f)
                curveTo(5.34f, 12.02f, 5.0f, 12.4f, 5.0f, 12.87f)
                verticalLineTo(13.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(4.98f)
                lineTo(11.0f, 21.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineToRelative(-0.02f, -7.0f)
                horizontalLineTo(18.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(12.87f)
                close()
            }
        }
        return _pushPin!!
    }

private var _pushPin: ImageVector? = null
