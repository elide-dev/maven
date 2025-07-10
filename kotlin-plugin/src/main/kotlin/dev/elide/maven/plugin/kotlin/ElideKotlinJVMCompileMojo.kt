package dev.elide.maven.plugin.kotlin

import dev.elide.maven.compiler.ElideLocator
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.ResolutionScope
import org.codehaus.plexus.compiler.CompilerException
import org.codehaus.plexus.util.cli.CommandLineException
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.Argument
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.maven.K2JVMCompileMojo
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.util.*
import kotlin.io.path.absolutePathString

/** @author Lauri Heino <datafox> */
@Mojo(
    name = "compile",
    defaultPhase = LifecyclePhase.COMPILE,
    requiresDependencyResolution = ResolutionScope.COMPILE,
    threadSafe = true
)
class ElideKotlinJVMCompileMojo : K2JVMCompileMojo() {
    override fun execCompiler(
        compiler: CLICompiler<K2JVMCompilerArguments>?,
        messageCollector: MessageCollector,
        arguments: K2JVMCompilerArguments,
        sourceRoots: List<File>
    ): ExitCode {
        val freeArgs = arguments.freeArgs.toMutableList()
        for(sourceRoot in sourceRoots) {
            freeArgs.add(sourceRoot.path)
        }
        arguments.freeArgs = freeArgs
        val cli = Commandline()
        cli.workingDirectory = project.basedir
        cli.executable = getElideExecutable()
        cli.addArguments(parseArguments(arguments))
        val out = CommandLineUtils.StringStreamConsumer()
        var returnCode: Int
        try {
            returnCode = CommandLineUtils.executeCommandLine(cli, out, out)
            out.output.lines().forEach { messageCollector.report(if(returnCode == 0) CompilerMessageSeverity.INFO else CompilerMessageSeverity.ERROR, it) }
        } catch(e: CommandLineException) {
            throw CompilerException("Error while executing Elide javac compiler.", e)
        } catch(e: IOException) {
            throw CompilerException("Error while executing Elide javac compiler.", e)
        }
        return if(returnCode == 0) ExitCode.OK else ExitCode.COMPILATION_ERROR
    }

    private fun getElideExecutable(): String = ElideLocator.locate()?.absolutePathString() ?: "elide"

    fun parseArguments(arguments: K2JVMCompilerArguments): Array<String> {
        val list: MutableList<String> = LinkedList()
        list.add("kotlinc")
        list.add("--")
        arguments::class.java.fields.forEach { list.parseArgument(arguments, it) }
        if("-d" !in list) {
            list.add("-d")
            list.add(project.build.outputDirectory)
        }
        list.add("--")
        list.addAll(arguments.freeArgs)
        return list.toTypedArray()
    }

    private fun MutableList<String>.parseArgument(arguments: K2JVMCompilerArguments, field: Field) {
        val argument = field.getAnnotation(Argument::class.java) ?: return
        val element: Any = field.get(arguments) ?: return
        when(element) {
            is Boolean -> if(element) add(argument.value)
            is String -> {
                add(argument.value)
                add(element)
            }
            is Array<*> -> {
                element.forEach {
                    if(it !is String) throw IllegalArgumentException()
                    add(argument.value)
                    add(it)
                }
            }
            else -> throw IllegalArgumentException()
        }
    }
}
