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

import org.apache.maven.model.Plugin
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.logging.AbstractLogger
import org.codehaus.plexus.logging.Logger
import org.jetbrains.kotlin.cli.common.arguments.Argument
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArgumentsConfigurator
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.maven.kapt.KaptJVMCompilerMojo
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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

    @Test
    fun `smart defaults add the stdlib at the plugin kotlin version`(@TempDir dir: File) {
        val project = MavenProject()
        project.file = File(dir, "pom.xml")
        val participant = ElideKotlinLifecycleParticipant()
        participant.enableLogging(SilentLogger)
        participant.configureSmartDefaults(project, Plugin())
        val stdlib = project.dependencies.single { it.artifactId == "kotlin-stdlib" }
        assertEquals("org.jetbrains.kotlin", stdlib.groupId)
        assertEquals(KotlinCompilerVersion.VERSION, stdlib.version)
    }

    @Test
    fun `kapt runs only when annotation processors are configured`() {
        val mojo = ElideKotlinKaptJVMCompileMojoImpl()
        assertFalse(mojo.hasAnnotationProcessors())
        KaptJVMCompilerMojo::class.java.getDeclaredField("annotationProcessors").apply {
            isAccessible = true
            set(mojo, arrayOf("com.example.Processor"))
        }
        assertTrue(mojo.hasAnnotationProcessors())
    }

    object SilentLogger : AbstractLogger(Logger.LEVEL_DISABLED, "test") {
        override fun debug(message: String?, throwable: Throwable?) = Unit

        override fun info(message: String?, throwable: Throwable?) = Unit

        override fun warn(message: String?, throwable: Throwable?) = Unit

        override fun error(message: String?, throwable: Throwable?) = Unit

        override fun fatalError(message: String?, throwable: Throwable?) = Unit

        override fun getChildLogger(name: String?): Logger = this
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
