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
package dev.elide.maven.plugin

import org.apache.maven.plugin.compiler.AbstractCompilerMojo
import org.apache.maven.plugin.compiler.TestCompilerMojo
import org.apache.maven.plugins.annotations.Parameter

/**
 * Elide Java test compiler.
 *
 * @author Lauri Heino <datafox>
 * @since 1.0.0
 */
open class ElideTestCompileMojoImpl : TestCompilerMojo() {
    /** Elide executable location. */
    @Parameter(name = "executable") var executable: String? = null

    override fun execute() {
        AbstractCompilerMojo::class.java.getDeclaredField("compilerId").apply {
            isAccessible = true
            set(this@ElideTestCompileMojoImpl, "elide")
        }
        executable?.let {
            AbstractCompilerMojo::class.java.getDeclaredField("executable").apply {
                isAccessible = true
                set(this@ElideTestCompileMojoImpl, it)
            }
        }
        super.execute()
    }
}
