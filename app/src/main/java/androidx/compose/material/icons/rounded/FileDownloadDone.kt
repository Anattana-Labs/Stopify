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

public val Icons.Rounded.FileDownloadDone: ImageVector
    get() {
        if (_fileDownloadDone != null) {
            return _fileDownloadDone!!
        }
        _fileDownloadDone = materialIcon(name = "Rounded.FileDownloadDone") {
            materialPath {
                moveTo(19.42f, 4.71f)
                lineTo(19.42f, 4.71f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-8.48f, 8.49f)
                lineTo(5.99f, 9.66f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(4.24f, 4.24f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(9.19f, -9.19f)
                curveTo(19.82f, 5.73f, 19.82f, 5.1f, 19.42f, 4.71f)
                close()
            }
            materialPath {
                moveTo(6.0f, 20.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(6.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveTo(5.0f, 19.55f, 5.45f, 20.0f, 6.0f, 20.0f)
                close()
            }
        }
        return _fileDownloadDone!!
    }

private var _fileDownloadDone: ImageVector? = null
