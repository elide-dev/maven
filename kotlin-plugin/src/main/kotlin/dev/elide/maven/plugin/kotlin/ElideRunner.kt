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

import dev.elide.maven.compiler.ElideLocator
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.compiler.CompilerException
import org.codehaus.plexus.util.cli.CommandLineException
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import java.io.File
import java.io.IOException
import kotlin.io.path.absolutePathString

/**
 * Tools for running Elide's Kotlin compiler.
 *
 * @author Lauri Heino <datafox>
 * @since 1.0.0
 */
object ElideRunner {
    /**
     * Executes the Elide Kotlin compiler according to [arguments].
     */
    fun <A : CommonCompilerArguments> runCompiler(
        messageCollector: MessageCollector,
        arguments: A,
        sourceRoots: List<File>,
        project: MavenProject,
        executable: String?,
        compiler: String,
        java: Boolean,
    ): ExitCode {
        val freeArgs = arguments.freeArgs.toMutableList()
        for (sourceRoot in sourceRoots) {
            freeArgs.add(sourceRoot.path)
        }
        arguments.freeArgs = freeArgs
        val cli = Commandline()
        cli.workingDirectory = project.basedir
        cli.executable = executable ?: ElideLocator.locate()?.absolutePathString() ?: "elide"
        cli.addArguments(ArgumentParser.parseArguments(compiler, arguments, project, java))
        val out = CommandLineUtils.StringStreamConsumer()
        var returnCode: Int
        try {
            returnCode = CommandLineUtils.executeCommandLine(cli, out, out)
            out.output.lines().forEach {
                messageCollector.report(
                    if (returnCode == 0) CompilerMessageSeverity.INFO
                    else CompilerMessageSeverity.ERROR,
                    it,
                )
            }
        } catch (e: CommandLineException) {
            throw CompilerException("Error while executing Elide $compiler compiler.", e)
        } catch (e: IOException) {
            throw CompilerException("Error while executing Elide $compiler compiler.", e)
        }
        return if (returnCode == 0) ExitCode.OK else ExitCode.COMPILATION_ERROR
    }
}
