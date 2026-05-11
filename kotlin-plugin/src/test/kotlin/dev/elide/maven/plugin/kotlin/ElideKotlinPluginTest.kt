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

import org.apache.maven.project.MavenProject
import org.jetbrains.kotlin.cli.common.arguments.Argument
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArgumentsConfigurator
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests for the Elide Kotlin Maven Plugin.
 *
 * @author Lauri Heino <datafox>
 */
class ElideKotlinPluginTest {
    @Test
    fun `argument parser should parse arguments`() {
        val arguments = MockCompilerArguments(CommonCompilerArgumentsConfigurator())
        arguments.someSpecialArgument = "gargantuan"
        val project = MavenProject()
        project.build.outputDirectory = "output/directory"
        val parsedArguments = ArgumentParser.parseArguments("test", arguments, project)
        assertEquals("test", parsedArguments[0])
        assertEquals("--", parsedArguments[1])
        assertContains(parsedArguments, "-XXspecial-argument=gargantuan")
        val dIndex = parsedArguments.indexOf("-d")
        assertNotEquals(-1, dIndex)
        assertEquals("output/directory", parsedArguments[dIndex + 1])
    }

    class MockCompilerArguments(override val configurator: CommonCompilerArgumentsConfigurator) :
        CommonCompilerArguments() {
        @Argument(value = "-XXspecial-argument", valueDescription = "<str>", description = "This is just for testing.")
        var someSpecialArgument: String? = null
            set(value) {
                checkFrozen()
                field = if (value.isNullOrEmpty()) null else value
            }
    }
}
