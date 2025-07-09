/*
 * Copyright (c) 2024-2025 Elide Technologies, Inc.
 *
 * Licensed under the MIT license (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   https://opensource.org/license/mit/
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package dev.elide.maven.compiler

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Locates the Elide binary via the user's PATH.
 *
 * @author Sam Gammon <sgammon>
 * @since 1.0.0
 */
object ElideLocator {
    // Checks if a path is a valid binary.
    private fun isValidElideBinary(path: Path): Boolean {
        return path.toFile().exists() && path.toFile().isFile && path.toFile().canExecute()
    }

    /**
     * Attempts to locate the Elide binary in the user's PATH.
     *
     * @return The path to the Elide binary, or null if not found.
     */
    fun locate(): Path? {
        val path = System.getenv("PATH") ?: ""
        for (pathCandidate in path.split(File.separatorChar)) {
            val candidate = Paths.get(pathCandidate, "elide")
            if (isValidElideBinary(candidate)) {
                return candidate
            }
        }
        return null
    }
}
