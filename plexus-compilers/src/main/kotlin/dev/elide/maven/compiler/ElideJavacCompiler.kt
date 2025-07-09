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

import org.codehaus.plexus.compiler.*
import org.codehaus.plexus.util.cli.CommandLineException
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
import java.io.File
import java.io.IOException
import java.util.LinkedList
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
public open class ElideJavacCompiler() : AbstractCompiler(
    CompilerOutputStyle.ONE_OUTPUT_FILE_PER_INPUT_FILE,
    ".java",
    ".class",
    null
) {
    override fun getCompilerId(): String = "elide"

    override fun performCompile(config: CompilerConfiguration): CompilerResult {
        val dest = File(config.outputLocation)
        if(!dest.exists()) dest.mkdirs()
        val sources = getSourceFiles(config) ?: return CompilerResult()
        if(sources.isEmpty()) return CompilerResult()
        logCompiling(sources, config)
        val executable = getElideExecutable(config)
        val args = buildList<String> {
            addAll(buildElideArgs(config, sources))
            config.maxmem?.ifEmpty { null }?.let { add("-J-Xmx$this") }
            config.meminitial?.ifEmpty { null }?.let { add("-J-Xms$this") }

            config.customCompilerArgumentsAsMap.entries
                .filter { it.value != null && it.key.startsWith("-J") }
                .ifEmpty { null }
                ?.forEach { add("${it.key}=${it.value}") }
        }

        val cli = Commandline()
        cli.setWorkingDirectory(config.workingDirectory.absolutePath)
        cli.executable = executable
        config.customCompilerArgumentsAsMap.keys
            .filter { it != null && it.startsWith("-J") }
            .apply { if(isNotEmpty()) cli.addArguments(toTypedArray()) }

        cli.addArguments(args.toTypedArray())
        val out = CommandLineUtils.StringStreamConsumer()
        var returnCode: Int
        val messages: List<CompilerMessage>
        try {
            returnCode = CommandLineUtils.executeCommandLine(cli, out, out)
            messages = parseOutput(returnCode, out.output.lines())
        } catch(e: CommandLineException) {
            throw CompilerException("Error while executing Elide javac compiler.", e)
        } catch(e: IOException) {
            throw CompilerException("Error while executing Elide javac compiler.", e)
        }
        return CompilerResult(returnCode == 0, messages)
    }

    private fun getElideExecutable(config: CompilerConfiguration): String = when (val executable = config.executable) {
        null -> ElideLocator.locate()?.absolutePathString()
        else -> executable
    } ?: "elide"

    override fun createCommandLine(config: CompilerConfiguration): Array<String> {
        return buildElideArgs(config, getSourceFiles(config)).toTypedArray()
    }

    private fun buildElideArgs(config: CompilerConfiguration, sources: Array<String>): MutableList<String> {
        val args: MutableList<String> = LinkedList<String>()
        args.add("javac")
        args.add("--")
        val destinationDir = File(config.outputLocation)
        args.add("-d")
        args.add(destinationDir.absolutePath)
        config.classpathEntries?.ifEmpty { null }?.let {
            args.add("-classpath")
            args.add(getPathString(it))
        }
        config.modulepathEntries?.ifEmpty { null }?.let {
            args.add("--module-path")
            args.add(getPathString(it))
        }
        config.sourceLocations?.ifEmpty { null }?.let {
            args.add("-sourcepath")
            args.add(getPathString(it))
        }
        args.addAll(listOf(*sources))

        config.generatedSourcesDirectory.apply {
            mkdirs()
            args.add("-s")
            args.add(absolutePath)
        }
        config.proc?.apply { args.add("-proc:$this") }
        config.annotationProcessors?.apply {
            args.add("-processor")
            args.add(indices.joinToString(",") { this[it] })
        }
        config.processorPathEntries?.ifEmpty { null }?.let {
            args.add("-processorpath")
            args.add(getPathString(it))
        }
        config.processorModulePathEntries?.ifEmpty { null }?.let {
            args.add("--processor-module-path")
            args.add(getPathString(it))
        }
        if (config.isOptimize) args.add("-O")
        if (config.isDebug) {
            if (config.debugLevel?.isNotEmpty() == true) {
                args.add("-g:" + config.debugLevel)
            } else {
                args.add("-g")
            }
        }
        if (config.isVerbose) args.add("-verbose")
        if (config.isParameters) args.add("-parameters")
        if (config.isEnablePreview) args.add("--enable-preview")
        config.implicitOption?.apply { args.add("-implicit:$this") }
        if (config.isShowDeprecation) {
            args.add("-deprecation")
            config.isShowWarnings = true
        }
        if (!config.isShowWarnings) {
            args.add("-nowarn")
        } else {
            val warnings = config.warnings
            if (config.isShowLint) {
                if (warnings.isNotEmpty()) {
                    args.add("-Xlint:$warnings")
                } else {
                    args.add("-Xlint")
                }
            }
        }
        if (config.isFailOnWarning) args.add("-Werror")
        if (config.releaseVersion?.isNotEmpty() == true) {
            args.add("--release")
            args.add(config.releaseVersion)
        } else {
            if (config.targetVersion?.isEmpty() == true) {
                args.add("-target")
                args.add("1.1")
            } else {
                args.add("-target")
                args.add(config.targetVersion)
            }
            if (config.sourceVersion?.isEmpty() == true) {
                args.add("-source")
                args.add("1.3")
            } else {
                args.add("-source")
                args.add(config.sourceVersion)
            }
        }
        config.sourceEncoding?.ifEmpty { null }?.let {
            args.add("-encoding")
            args.add(it)
        }
        config.moduleVersion?.ifEmpty { null }?.let {
            args.add("--module-version")
            args.add(it)
        }
        config.customCompilerArgumentsEntries?.forEach { (key, value) ->
            if (key.isEmpty() || key.startsWith("-J")) return@forEach
            args.add(key)
            if (value.isNotEmpty()) args.add(value)
        }
        return args
    }

    @Throws(IOException::class)
    private fun parseOutput(exitCode: Int, input: List<String>): List<CompilerMessage> {
        //very lazy for now
        val kind = if(exitCode == 0) CompilerMessage.Kind.NOTE else CompilerMessage.Kind.ERROR
        return input.map { CompilerMessage(it, kind) }
    }
}
