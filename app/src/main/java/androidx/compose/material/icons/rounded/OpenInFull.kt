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

public val Icons.Rounded.OpenInFull: ImageVector
    get() {
        if (_openInFull != null) {
            return _openInFull!!
        }
        _openInFull = materialIcon(name = "Rounded.OpenInFull") {
            materialPath {
                moveTo(21.0f, 8.59f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-4.59f)
                curveToRelative(-0.89f, 0.0f, -1.34f, 1.08f, -0.71f, 1.71f)
                lineToRelative(1.59f, 1.59f)
                lineToRelative(-10.0f, 10.0f)
                lineToRelative(-1.59f, -1.59f)
                curveTo(4.08f, 14.08f, 3.0f, 14.52f, 3.0f, 15.41f)
                verticalLineTo(20.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(4.59f)
                curveToRelative(0.89f, 0.0f, 1.34f, -1.08f, 0.71f, -1.71f)
                lineToRelative(-1.59f, -1.59f)
                lineToRelative(10.0f, -10.0f)
                lineToRelative(1.59f, 1.59f)
                curveTo(19.92f, 9.92f, 21.0f, 9.48f, 21.0f, 8.59f)
                close()
            }
        }
        return _openInFull!!
    }

private var _openInFull: ImageVector? = null
