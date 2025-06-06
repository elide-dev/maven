package dev.elide.maven.compiler

import org.codehaus.plexus.compiler.*
import org.codehaus.plexus.util.cli.CommandLineException
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
import java.io.File
import java.io.IOException
import javax.inject.Named
import javax.inject.Singleton

/**
 * @author Lauri "datafox" Heino
 */
@Named("elide")
@Singleton
open class ElideJavacCompiler() : AbstractCompiler(
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
        val args = buildElideArgs(config, sources)
        val cli = Commandline()
        cli.setWorkingDirectory(config.workingDirectory.absolutePath)
        cli.executable = executable
        cli.addArguments(args)
        config.maxmem?.apply { if(isNotEmpty()) "-J-Xmx$this" }
        config.meminitial?.apply { if(isNotEmpty()) "-J-Xms$this" }
        config.getCustomCompilerArgumentsAsMap().keys
            .filter { it != null && it.startsWith("-J") }
            .apply { if(isNotEmpty()) cli.addArguments(toTypedArray()) }
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

    private fun getElideExecutable(config: CompilerConfiguration): String {
        if(config.executable != null && config.executable.isNotBlank()) return config.executable
        return "elide"
    }

    override fun createCommandLine(config: CompilerConfiguration): Array<String> {
        return buildElideArgs(config, getSourceFiles(config))
    }

    private fun buildElideArgs(config: CompilerConfiguration, sources: Array<String>): Array<String> {
        val args: MutableList<String> = ArrayList<String>()
        args.add("javac")
        args.add("--")
        val destinationDir = File(config.outputLocation)
        args.add("-d")
        args.add(destinationDir.absolutePath)
        config.classpathEntries?.apply { if(isNotEmpty()) {
            args.add("-classpath")
            args.add(getPathString(this))
        } }
        config.modulepathEntries?.apply { if(isNotEmpty()) {
            args.add("--module-path")
            args.add(getPathString(this))
        } }
        config.sourceLocations?.apply { if(isNotEmpty()) {
            args.add("-sourcepath")
            args.add(getPathString(this))
        } }
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
        config.processorPathEntries?.apply { if(isNotEmpty()) {
            args.add("-processorpath")
            args.add(getPathString(this))
        } }
        config.processorModulePathEntries?.apply { if(isNotEmpty()) {
            args.add("--processor-module-path")
            args.add(getPathString(this))
        } }
        if(config.isOptimize) args.add("-O")
        if(config.isDebug) {
            if(config.debugLevel?.isNotEmpty() ?: false) {
                args.add("-g:" + config.debugLevel)
            } else {
                args.add("-g")
            }
        }
        if(config.isVerbose) args.add("-verbose")
        if(config.isParameters) args.add("-parameters")
        if(config.isEnablePreview) args.add("--enable-preview")
        config.implicitOption?.apply { args.add("-implicit:$this") }
        if(config.isShowDeprecation) {
            args.add("-deprecation")
            config.isShowWarnings = true
        }
        if(!config.isShowWarnings) {
            args.add("-nowarn")
        } else {
            val warnings = config.warnings
            if(config.isShowLint) {
                if(warnings.isNotEmpty()) {
                    args.add("-Xlint:$warnings")
                } else {
                    args.add("-Xlint")
                }
            }
        }
        if(config.isFailOnWarning) args.add("-Werror")
        if(config.releaseVersion?.isNotEmpty() ?: false) {
            args.add("--release")
            args.add(config.releaseVersion)
        } else {
            if(config.targetVersion?.isEmpty() ?: true) {
                args.add("-target")
                args.add("1.1")
            } else {
                args.add("-target")
                args.add(config.targetVersion)
            }
            if(config.sourceVersion?.isEmpty() ?: true) {
                args.add("-source")
                args.add("1.3")
            } else {
                args.add("-source")
                args.add(config.sourceVersion)
            }
        }
        config.sourceEncoding?.apply { if(isNotEmpty()) {
            args.add("-encoding")
            args.add(this)
        } }
        config.moduleVersion?.apply { if(isNotEmpty()) {
            args.add("--module-version")
            args.add(this)
        } }
        config.customCompilerArgumentsEntries?.forEach { (key, value) ->
            if(key.isEmpty() || key.startsWith("-J")) return@forEach
            args.add(key)
            if(value.isNotEmpty()) args.add(value)
        }
        return args.toTypedArray()
    }

    @Throws(IOException::class)
    fun parseOutput(exitCode: Int, input: List<String>): List<CompilerMessage> {
        //very lazy for now
        val kind = if(exitCode == 0) CompilerMessage.Kind.NOTE else CompilerMessage.Kind.ERROR
        return input.map { CompilerMessage(it, kind) }
    }
}