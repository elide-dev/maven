/*
 * Copyright (c) 2024-2026 Elide Technologies, Inc.
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
package dev.elide.maven.plugin.kotlin

import org.jetbrains.kotlin.maven.kapt.KaptJVMCompilerMojo

/** Whether this kapt execution declares any annotation processors. */
internal fun KaptJVMCompilerMojo.hasAnnotationProcessors(): Boolean {
    fun field(name: String): Any? =
        KaptJVMCompilerMojo::class.java.getDeclaredField(name).run {
            isAccessible = true
            get(this@hasAnnotationProcessors)
        }
    val processors = field("annotationProcessors") as Array<*>?
    val paths = field("annotationProcessorPaths") as List<*>?
    return !processors.isNullOrEmpty() || !paths.isNullOrEmpty()
}

/** Logs that this kapt execution is skipped because no annotation processors are configured. */
internal fun KaptJVMCompilerMojo.skipKapt() = log.info("No annotation processors configured, skipping kapt")
