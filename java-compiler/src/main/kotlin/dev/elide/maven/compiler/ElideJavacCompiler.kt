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
package dev.elide.maven.compiler

import org.codehaus.plexus.compiler.CompilerConfiguration
import org.codehaus.plexus.compiler.CompilerException
import org.codehaus.plexus.compiler.CompilerMessage
import org.codehaus.plexus.compiler.CompilerResult
import org.codehaus.plexus.compiler.javac.JavacCompiler
import org.codehaus.plexus.util.StringUtils
import org.codehaus.plexus.util.cli.CommandLineException
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.StringReader
import javax.inject.Named
import javax.inject.Singleton
import kotlin.io.path.absolutePathString

/**
 * Implements a Java compiler bridge from Maven to Elide.
 *
 * @author Lauri Heino <datafox>
 * @author Sam Gammon <sgammon>
 * @since 1.0.0
 */
@Named(ELIDE_COMPILER)
@Singleton
open class ElideJavacCompiler : JavacCompiler() {
    override fun getCompilerId(): String = "elide"

    override fun performCompile(config: CompilerConfiguration): CompilerResult {
        config.isFork = true

        val destinationDir = File(config.outputLocation)
        if (!destinationDir.exists()) destinationDir.mkdirs()

        val sourceFiles = getSourceFiles(config)
        if (sourceFiles == null || sourceFiles.size == 0) return CompilerResult()

        logCompiling(sourceFiles, config)

        val executable = getElideExecutable(config)
        val javacVersion = getElideJavacVersion(executable)
        val args = buildCompilerArguments(config, sourceFiles, javacVersion)

        return compileOutOfProcess(config, executable, args)
    }

    override fun compileOutOfProcess(
        config: CompilerConfiguration,
        executable: String,
        args: Array<out String>,
    ): CompilerResult {
        val cli = Commandline()

        cli.setWorkingDirectory(config.workingDirectory.absolutePath)
        cli.setExecutable(executable)

        try {
            val argumentsFile =
                JavacCompiler::class
                    .java
                    .getDeclaredMethod("createFileWithArguments", String::class.java.arrayType(), String::class.java)
                    .run {
                        isAccessible = true
                        invoke(this@ElideJavacCompiler, args, config.buildDirectory.absolutePath) as File
                    }
            cli.addArguments(
                arrayOf("javac", "--", "@" + argumentsFile.getCanonicalPath().replace(File.separatorChar, '/'))
            )

            if (!StringUtils.isEmpty(config.maxmem)) cli.addArguments(arrayOf("-J-Xmx${config.maxmem}"))
            if (!StringUtils.isEmpty(config.meminitial)) cli.addArguments(arrayOf("-J-Xms${config.meminitial}"))

            for (key in config.getCustomCompilerArgumentsAsMap().keys) {
                if (StringUtils.isNotEmpty(key) && key.startsWith("-J")) cli.addArguments(arrayOf<String>(key))
            }
        } catch (e: IOException) {
            throw CompilerException("Error creating file with javac arguments", e)
        }

        val out = CommandLineUtils.StringStreamConsumer()
        val returnCode: Int
        val messages: List<CompilerMessage>

        try {
            returnCode = CommandLineUtils.executeCommandLine(cli, out, out)

            if (log.isDebugEnabled) log.debug("Compiler output:{}{}", EOL, out.output)

            messages =
                JavacCompiler::class
                    .java
                    .getDeclaredMethod("parseModernStream", Int::class.java, BufferedReader::class.java)
                    .run {
                        isAccessible = true
                        @Suppress("UNCHECKED_CAST")
                        invoke(null, returnCode, BufferedReader(StringReader(out.output)))
                            as MutableList<CompilerMessage>
                    }
            val last =
                out.output.lines().reversed().let {
                    for (line in it) if (line.isNotBlank()) return@let line
                    ""
                }
            if (last.isNotBlank()) {
                if (last.contains("✅")) messages.add(CompilerMessage(last, CompilerMessage.Kind.NOTE))
                else if (last.contains("❌")) messages.add(CompilerMessage(last, CompilerMessage.Kind.ERROR))
            }
        } catch (e: CommandLineException) {
            throw CompilerException("Error while executing the Elide javac compiler.", e)
        } catch (e: IOException) {
            throw CompilerException("Error while executing the Elide javac compiler.", e)
        }

        val success = returnCode == 0
        return CompilerResult(success, messages)
    }

    private fun getElideExecutable(config: CompilerConfiguration): String =
        config.executable ?: ElideLocator.locate()?.absolutePathString() ?: ELIDE_EXECUTABLE

    internal fun getElideJavacVersion(executable: String): String? {
        val cli = Commandline()
        cli.setExecutable(executable)
        cli.addArguments(arrayOf("javac", "--", "-version"))
        val out = mutableListOf<String>()
        val err = mutableListOf<String>()
        try {
            val exitCode = CommandLineUtils.executeCommandLine(cli, out::add, err::add)
            if (exitCode != 0) {
                throw CompilerException(
                    buildString {
                        append("Could not retrieve version from ")
                        append(executable)
                        append(". Exit code ")
                        append(exitCode)
                        append(", Output: ")
                        append(out.joinToString(System.lineSeparator()))
                        append(", Error: ")
                        append(err.joinToString(System.lineSeparator()))
                    }
                )
            }
        } catch (e: CommandLineException) {
            throw CompilerException("Error while executing the external compiler $executable", e)
        }
        return tryParseVersion(out) ?: tryParseVersion(err)
    }

    private fun tryParseVersion(versions: List<String>): String? {
        for (version in versions) {
            if (version.matches(VERSION_RE)) {
                return version.substringBefore('.')
            }
        }
        return null
    }

    companion object {
        val VERSION_RE = "\\d+\\.\\d+\\.\\d+".toRegex()
    }
}
