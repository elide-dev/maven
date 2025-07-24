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

import org.apache.maven.project.MavenProject
import org.jetbrains.kotlin.cli.common.arguments.Argument
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import java.lang.reflect.Field
import java.util.*

/**
 * Parser for Kotlin compiler arguments.
 *
 * @author Lauri Heino <datafox>
 * @since 1.0.0
 */
object ArgumentParser {
    /**
     * Parses [A] into a list of Elide Kotlin compiler command line arguments.
     */
    fun <A : CommonCompilerArguments> parseArguments(
        compiler: String,
        arguments: A,
        project: MavenProject,
        java: Boolean,
    ): Array<String> {
        val list: MutableList<String> = LinkedList()
        list.add(compiler)
        list.add("--")
        getAllFields(arguments::class.java).forEach { list.parseArgument(arguments, it) }
        if (java && "-d" !in list) {
            list.add("-d")
            list.add(project.build.outputDirectory)
        }
        list.add("--")
        list.addAll(arguments.freeArgs)
        return list.toTypedArray()
    }

    private fun <A : CommonCompilerArguments> getAllFields(type: Class<out A>): List<Field> {
        var type: Class<out CommonCompilerArguments> = type
        val fields: MutableList<Field> = LinkedList()
        fields.addAll(type.declaredFields.filter { it.trySetAccessible() && it.isAnnotationPresent(Argument::class.java) })
        while(type != CommonCompilerArguments::class.java) {
            type = type.superclass as Class<out CommonCompilerArguments>
            fields.addAll(type.declaredFields.filter { it.trySetAccessible() && it.isAnnotationPresent(Argument::class.java) })
        }
        return fields
    }

    private fun <A : CommonCompilerArguments> MutableList<String>.parseArgument(
        arguments: A,
        field: Field,
    ) {
        val argument = field.getAnnotation(Argument::class.java) ?: return
        val element: Any = field.get(arguments) ?: return
        when (element) {
            is Boolean -> if (element) add(argument.value)
            is String -> {
                add(argument.value + "=" + element)
            }
            is Array<*> -> {
                element.forEach {
                    if (it !is String) throw IllegalArgumentException()
                    add(argument.value + "=" + it)
                }
            }
            else -> throw IllegalArgumentException()
        }
    }
}
