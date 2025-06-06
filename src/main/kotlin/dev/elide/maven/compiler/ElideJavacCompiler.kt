package dev.elide.maven.compiler

import org.codehaus.plexus.compiler.AbstractCompiler
import org.codehaus.plexus.compiler.CompilerConfiguration
import org.codehaus.plexus.compiler.CompilerException
import org.codehaus.plexus.compiler.CompilerMessage
import org.codehaus.plexus.compiler.CompilerOutputStyle
import org.codehaus.plexus.compiler.CompilerResult
import org.codehaus.plexus.util.StringUtils
import org.codehaus.plexus.util.cli.CommandLineException
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.util.ArrayList
import java.util.NoSuchElementException
import java.util.StringTokenizer
import java.util.regex.Pattern
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
        val version = getElideJavacVersion(executable)
        val args = buildElideArgs(config, sources, version)
        val cli = Commandline()
        cli.setWorkingDirectory(config.workingDirectory.absolutePath)
        cli.executable = executable
        cli.addArguments(args)
        if(!StringUtils.isEmpty(config.maxmem)) {
            cli.addArguments(arrayOf("-J-Xmx" + config.maxmem))
        }

        if(!StringUtils.isEmpty(config.meminitial)) {
            cli.addArguments(arrayOf("-J-Xms" + config.meminitial))
        }

        for(key in config.getCustomCompilerArgumentsAsMap().keys) {
            if(StringUtils.isNotEmpty(key) && key.startsWith("-J")) {
                cli.addArguments(arrayOf(key))
            }
        }
        val out = CommandLineUtils.StringStreamConsumer()
        var returnCode: Int
        val messages: List<CompilerMessage>
        try {
            returnCode = CommandLineUtils.executeCommandLine(cli, out, out)
            messages = parseModernStream(returnCode, BufferedReader(StringReader(out.output)))
        } catch(e: Exception) {
            throw CompilerException("Error while executing Elide javac compiler.", e)
        }
        return CompilerResult(returnCode == 0, messages)
    }

    private fun getElideExecutable(config: CompilerConfiguration): String {
        if(config.executable != null && config.executable.isNotBlank()) return config.executable
        return "elide"
    }

    override fun createCommandLine(config: CompilerConfiguration): Array<String> {
        val executable = getElideExecutable(config)
        val version = getElideJavacVersion(executable)
        return buildElideArgs(config, getSourceFiles(config), version)
    }

    private fun getElideJavacVersion(executable: String): String {
        val cli = Commandline()
        cli.executable = executable
        cli.addArguments(arrayOf("javac", "--version"))
        val out = CommandLineUtils.StringStreamConsumer()
        try {
            val exitCode = CommandLineUtils.executeCommandLine(cli, out, out)
            if(exitCode != 0) throw CompilerException("Could not retrieve version from $executable. Exit code $exitCode, Output: ${out.output}")
        } catch(e: CommandLineException) {
            throw CompilerException("Error while executing Elide javac compiler.", e)
        }
        return extractMajorAndMinorVersion(out.output)
    }

    fun extractMajorAndMinorVersion(text: String): String {
        val matcher = JAVA_MAJOR_AND_MINOR_VERSION_PATTERN.matcher(text)
        require(matcher.find()) { "Could not extract version from \"$text\"" }
        return matcher.group()
    }

    private fun buildElideArgs(config: CompilerConfiguration, sources: Array<String>, version: String): Array<String> {
        val args: MutableList<String> = ArrayList<String>()

        args.add("javac")
        args.add("--")
        // ----------------------------------------------------------------------
        // Set output
        // ----------------------------------------------------------------------
        val destinationDir = File(config.outputLocation)

        args.add("-d")

        args.add(destinationDir.absolutePath)


        // ----------------------------------------------------------------------
        // Set the class and source paths
        // ----------------------------------------------------------------------
        val classpathEntries = config.classpathEntries
        if(classpathEntries != null && !classpathEntries.isEmpty()) {
            args.add("-classpath")

            args.add(getPathString(classpathEntries))
        }

        val modulepathEntries = config.modulepathEntries
        if(modulepathEntries != null && !modulepathEntries.isEmpty()) {
            args.add("--module-path")

            args.add(getPathString(modulepathEntries))
        }

        val sourceLocations = config.sourceLocations
        if(sourceLocations != null && !sourceLocations.isEmpty()) {
            // always pass source path, even if sourceFiles are declared,
            // needed for jsr269 annotation processing, see MCOMPILER-98
            args.add("-sourcepath")

            args.add(getPathString(sourceLocations))
        }
        args.addAll(listOf(*sources))

        if(config.generatedSourcesDirectory != null) {
            config.generatedSourcesDirectory.mkdirs()

            args.add("-s")
            args.add(config.generatedSourcesDirectory.absolutePath)
        }
        if(config.proc != null) {
            args.add("-proc:" + config.proc)
        }
        if(config.annotationProcessors != null) {
            args.add("-processor")
            val procs = config.annotationProcessors
            val buffer = java.lang.StringBuilder()
            for(i in procs.indices) {
                if(i > 0) {
                    buffer.append(",")
                }

                buffer.append(procs[i])
            }
            args.add(buffer.toString())
        }
        if(config.processorPathEntries != null
            && !config.processorPathEntries.isEmpty()
        ) {
            args.add("-processorpath")
            args.add(getPathString(config.processorPathEntries))
        }
        if(config.processorModulePathEntries != null
            && !config.processorModulePathEntries.isEmpty()
        ) {
            args.add("--processor-module-path")
            args.add(getPathString(config.processorModulePathEntries))
        }

        if(config.isOptimize) {
            args.add("-O")
        }

        if(config.isDebug) {
            if(StringUtils.isNotEmpty(config.debugLevel)) {
                args.add("-g:" + config.debugLevel)
            } else {
                args.add("-g")
            }
        }

        if(config.isVerbose) {
            args.add("-verbose")
        }

        if(config.isParameters) {
            args.add("-parameters")
        }

        if(config.isEnablePreview) {
            args.add("--enable-preview")
        }

        if(config.implicitOption != null) {
            args.add("-implicit:" + config.implicitOption)
        }

        if(config.isShowDeprecation) {
            args.add("-deprecation")

            // This is required to actually display the deprecation messages
            config.isShowWarnings = true
        }

        if(!config.isShowWarnings) {
            args.add("-nowarn")
        } else {
            val warnings = config.warnings
            if(config.isShowLint) {
                if(config.isShowWarnings && StringUtils.isNotEmpty(warnings)) {
                    args.add("-Xlint:$warnings")
                } else {
                    args.add("-Xlint")
                }
            }
        }

        if(config.isFailOnWarning) {
            args.add("-Werror")
        }

        if(!StringUtils.isEmpty(config.releaseVersion)) {
            args.add("--release")
            args.add(config.releaseVersion)
        } else {
            // TODO: this could be much improved
            if(StringUtils.isEmpty(config.targetVersion)) {
                // Required, or it defaults to the target of your JDK (eg 1.5)
                args.add("-target")
                args.add("1.1")
            } else {
                args.add("-target")
                args.add(config.targetVersion)
            }

            if(StringUtils.isEmpty(config.sourceVersion)) {
                // If omitted, later JDKs complain about a 1.1 target
                args.add("-source")
                args.add("1.3")
            } else {
                args.add("-source")
                args.add(config.sourceVersion)
            }
        }

        if(!StringUtils.isEmpty(config.sourceEncoding)) {
            args.add("-encoding")
            args.add(config.sourceEncoding)
        }

        if(!StringUtils.isEmpty(config.moduleVersion)) {
            args.add("--module-version")
            args.add(config.moduleVersion)
        }

        for(entry in config.customCompilerArgumentsEntries) {
            val key = entry.key

            if(StringUtils.isEmpty(key) || key!!.startsWith("-J")) {
                continue
            }

            args.add(key)

            val value = entry.value

            if(StringUtils.isEmpty(value)) {
                continue
            }

            args.add(value)
        }

        return args.toTypedArray()
    }

    @Throws(IOException::class)
    fun parseModernStream(exitCode: Int, input: BufferedReader): List<CompilerMessage> {
        val errors: MutableList<CompilerMessage> = mutableListOf()

        var line: String?

        var buffer = StringBuilder()

        var hasPointer = false
        var stackTraceLineCount = 0

        while(true) {
            line = input.readLine()

            if(line == null) {
                // javac output not detected by other parsing
                // maybe better to ignore only the summary and mark the rest as error
                val bufferAsString = buffer.toString()
                if(buffer.isNotEmpty()) {
                    if(JAVAC_OR_JVM_ERROR.matcher(bufferAsString).matches()) {
                        errors.add(CompilerMessage(bufferAsString, CompilerMessage.Kind.ERROR))
                    } else if(hasPointer) {
                        // A compiler message remains in buffer at end of parse stream
                        errors.add(parseModernError(exitCode, bufferAsString))
                    } else if(stackTraceLineCount > 0) {
                        // Extract stack trace from end of buffer
                        val lines = bufferAsString.split("\\R".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val linesTotal = lines.size
                        buffer = StringBuilder()
                        var firstLine = linesTotal - stackTraceLineCount

                        // Salvage Javac localized message 'javac.msg.bug' ("An exception has occurred in the
                        // compiler ... Please file a bug")
                        if(firstLine > 0) {
                            val lineBeforeStackTrace = lines[firstLine - 1]
                            // One of those two URL substrings should always appear, without regard to JVM locale.
                            // TODO: Update, if the URL changes, last checked for JDK 21.
                            if(lineBeforeStackTrace.contains("java.sun.com/webapps/bugreport")
                                || lineBeforeStackTrace.contains("bugreport.java.com")
                            ) {
                                firstLine--
                            }
                        }

                        // Note: For message 'javac.msg.proc.annotation.uncaught.exception' ("An annotation processor
                        // threw an uncaught exception"), there is no locale-independent substring, and the header is
                        // also multi-line. It was discarded in the removed method 'parseAnnotationProcessorStream',
                        // and we continue to do so.
                        for(i in firstLine..<linesTotal) {
                            buffer.append(lines[i]).append(EOL)
                        }
                        errors.add(CompilerMessage(buffer.toString(), CompilerMessage.Kind.ERROR))
                    }
                }
                return errors
            }

            if(stackTraceLineCount == 0 && STACK_TRACE_FIRST_LINE.matcher(line).matches()
                || STACK_TRACE_OTHER_LINE.matcher(line).matches()
            ) {
                stackTraceLineCount++
            } else {
                stackTraceLineCount = 0
            }

            // new error block?
            if(!line.startsWith(" ") && hasPointer) {
                // add the error bean
                errors.add(parseModernError(exitCode, buffer.toString()))

                // reset for next error block
                buffer = StringBuilder() // this is quicker than clearing it

                hasPointer = false
            }

            // TODO: there should be a better way to parse these
            if((buffer.isEmpty()) && line.startsWith("error: ")) {
                errors.add(CompilerMessage(line, CompilerMessage.Kind.ERROR))
            } else if((buffer.isEmpty()) && line.startsWith("warning: ")) {
                errors.add(CompilerMessage(line, CompilerMessage.Kind.WARNING))
            } else if((buffer.isEmpty()) && isNote(line)) {
                // skip, JDK 1.5 telling us deprecated APIs are used but -Xlint:deprecation isn't set
            } else if((buffer.isEmpty()) && isMisc(line)) {
                // verbose output was set
                errors.add(CompilerMessage(line, CompilerMessage.Kind.OTHER))
            } else {
                buffer.append(line)

                buffer.append(EOL)
            }

            if(line.endsWith("^")) {
                hasPointer = true
            }
        }
    }

    private fun isMisc(line: String): Boolean {
        return startsWithPrefix(line, MISC_PREFIXES)
    }

    private fun isNote(line: String): Boolean {
        return startsWithPrefix(line, NOTE_PREFIXES)
    }

    private fun startsWithPrefix(line: String, prefixes: Array<String>): Boolean {
        for(prefix in prefixes) {
            if(line.startsWith(prefix)) {
                return true
            }
        }
        return false
    }

    fun parseModernError(exitCode: Int, error: String): CompilerMessage {
        val tokens = StringTokenizer(error, ":")

        var isError = exitCode != 0

        try {
            // With Java 6 error output lines from the compiler got longer. For backward compatibility
            // .. and the time being, we eat up all (if any) tokens up to the erroneous file and source
            // .. line indicator tokens.

            var tokenIsAnInteger: Boolean

            var file: java.lang.StringBuilder? = null

            var currentToken: String? = null

            do {
                if(currentToken != null) {
                    if(file == null) {
                        file = java.lang.StringBuilder(currentToken)
                    } else {
                        file.append(':').append(currentToken)
                    }
                }

                currentToken = tokens.nextToken()

                // Probably the only backward compatible means of checking if a string is an integer.
                tokenIsAnInteger = true

                try {
                    currentToken.toInt()
                } catch(e: NumberFormatException) {
                    tokenIsAnInteger = false
                }
            } while(!tokenIsAnInteger)

            val lineIndicator = currentToken

            val startOfFileName = file.toString().lastIndexOf(']')

            if(startOfFileName > -1) {
                file = java.lang.StringBuilder(file!!.substring(startOfFileName + 1 + EOL.length))
            }

            val line = lineIndicator!!.toInt()

            val msgBuffer = java.lang.StringBuilder()

            var msg = tokens.nextToken(EOL).substring(2)

            // Remove the 'warning: ' prefix
            val warnPrefix = getWarnPrefix(msg)
            if(warnPrefix != null) {
                isError = false
                msg = msg.substring(warnPrefix.length)
            } else {
                isError = exitCode != 0
            }

            msgBuffer.append(msg)

            msgBuffer.append(EOL)

            var context = tokens.nextToken(EOL)

            var pointer: String? = null

            do {
                val msgLine = tokens.nextToken(EOL)

                if(pointer != null) {
                    msgBuffer.append(msgLine)

                    msgBuffer.append(EOL)
                } else if(msgLine.endsWith("^")) {
                    pointer = msgLine
                } else {
                    msgBuffer.append(context)

                    msgBuffer.append(EOL)

                    context = msgLine
                }
            } while(tokens.hasMoreTokens())

            msgBuffer.append(EOL)

            val message = msgBuffer.toString()

            val startcolumn = pointer!!.indexOf("^")

            var endcolumn = context?.indexOf(" ", startcolumn) ?: startcolumn

            if(endcolumn == -1) {
                endcolumn = context!!.length
            }

            return CompilerMessage(
                file.toString(),
                isError,
                line,
                startcolumn,
                line,
                endcolumn,
                message.trim { it <= ' ' })
        } catch(e: NoSuchElementException) {
            return CompilerMessage("no more tokens - could not parse error message: $error", isError)
        } catch(e: java.lang.Exception) {
            return CompilerMessage("could not parse error message: $error", isError)
        }
    }

    fun getWarnPrefix(msg: String): String? {
        for(warningPrefix in WARNING_PREFIXES) {
            if(msg.startsWith(warningPrefix)) {
                return warningPrefix
            }
        }
        return null
    }

    enum class JavaVersion(vararg versionPrefixes: String) {
        JAVA_1_3_OR_OLDER("1.3", "1.2", "1.1", "1.0"),
        JAVA_1_4("1.4"),
        JAVA_1_5("1.5"),
        JAVA_1_6("1.6"),
        JAVA_1_7("1.7"),
        JAVA_1_8("1.8"),
        JAVA_9("9"); // since Java 9 a different versioning scheme was used (https://openjdk.org/jeps/223)

        val versionPrefixes: Set<String> = setOf(*versionPrefixes)

        /**
         * The internal logic checks if the given version starts with the prefix of one of the enums preceding the current one.
         *
         * @param version the version to check
         * @return `true` if the version represented by this enum is older than or equal (in its minor and major version) to a given version
         */
        fun isOlderOrEqualTo(version: String): Boolean {
            // go through all previous enums
            val allJavaVersionPrefixes: Array<JavaVersion?> = entries.toTypedArray()
            for(n in ordinal - 1 downTo -1 + 1) {
                if(allJavaVersionPrefixes[n]!!.versionPrefixes.stream()
                        .anyMatch { prefix: String? -> version.startsWith(prefix!!) }
                ) {
                    return false
                }
            }
            return true
        }
    }

    companion object {
        @JvmStatic
        private val WARNING_PREFIXES: Array<String> = arrayOf<String>("warning: ", "\u8b66\u544a: ", "\u8b66\u544a\uff1a ")

        // see compiler.note.note in compiler.properties of javac sources
        @JvmStatic
        private val NOTE_PREFIXES: Array<String> = arrayOf<String>("Note: ", "\u6ce8: ", "\u6ce8\u610f\uff1a ")

        // see compiler.misc.verbose in compiler.properties of javac sources
        @JvmStatic
        private val MISC_PREFIXES: Array<String> = arrayOf<String>("[")

        @JvmStatic
        private val JAVA_MAJOR_AND_MINOR_VERSION_PATTERN: Pattern = Pattern.compile("\\d+(\\.\\d+)?")

        @JvmStatic
        private val STACK_TRACE_FIRST_LINE: Pattern = Pattern.compile(
            ("^(?:[\\w+.-]+\\.)[\\w$]*?(?:"
                    + "Exception|Error|Throwable|Failure|Result|Abort|Fault|ThreadDeath|Overflow|Warning|"
                    + "NotSupported|NotFound|BadArgs|BadClassFile|Illegal|Invalid|Unexpected|Unchecked|Unmatched\\w+"
                    + ").*$")
        )

        // Match exception causes, existing and omitted stack trace elements
        @JvmStatic
        private val STACK_TRACE_OTHER_LINE: Pattern =
            Pattern.compile("^(?:Caused by:\\s.*|\\s*at .*|\\s*\\.\\.\\.\\s\\d+\\smore)$")

        // Match generic javac errors with 'javac:' prefix, JMV init and boot layer init errors
        @JvmStatic
        private val JAVAC_OR_JVM_ERROR: Pattern =
            Pattern.compile("^(?:javac:|Error occurred during initialization of (?:boot layer|VM)).*", Pattern.DOTALL)
    }
}