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
package dev.elide.maven.plugin.kotlin

import org.apache.maven.plugins.annotations.Parameter
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JSCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.maven.KotlinTestJSCompilerMojo
import java.io.File

/**
 * Elide Kotlin to Javascript test compiler.
 *
 * @author Lauri Heino <datafox>
 * @since 1.0.0
 */
internal open class ElideKotlinJSTestCompileMojoImpl : KotlinTestJSCompilerMojo() {
    /**
     * Elide executable location.
     */
    @Parameter(name = "executable") var executable: String? = null

    override fun execCompiler(
        compiler: CLICompiler<K2JSCompilerArguments>?,
        messageCollector: MessageCollector,
        arguments: K2JSCompilerArguments,
        sourceRoots: List<File>,
    ): ExitCode =
        ElideRunner.runCompiler(
            messageCollector,
            arguments,
            sourceRoots,
            project,
            executable,
            "kotlinc-js",
            false,
        )
}
