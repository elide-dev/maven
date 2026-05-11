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

import org.codehaus.plexus.testing.PlexusTest
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.assertDoesNotThrow
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for the Elide Java Plexus Compiler.
 *
 * @author Lauri Heino <datafox>
 */
@PlexusTest
class ElideJavacCompilerTest {
    @Test
    fun `can retrieve elide javac version`() {
        val compiler = ElideJavacCompiler()
        val version = assertNotNull(compiler.getElideJavacVersion(ELIDE_PATH.absolutePathString()), "Elide javac version could not be retrieved")
        val versionInt = assertDoesNotThrow("Retrieved version should be an integer") { version.toInt() }
        assertTrue(versionInt >= 25, "Retrieved version should be 25 or higher")
    }

    companion object {
        private lateinit var elidePath: Path

        val ELIDE_PATH by lazy { elidePath }

        val ELIDE_VERSION_RE = "\\d+\\.\\d+\\.\\d+(-.*)?".toRegex()

        @BeforeAll
        @JvmStatic
        fun `elide should be present for tests`() {
            elidePath = assertNotNull(ElideLocator.locate(), "Elide binary was not found")
            val cli = Commandline()
            cli.executable = elidePath.absolutePathString()
            cli.addArguments(arrayOf("--version"))
            val out = CommandLineUtils.StringStreamConsumer()
            assertDoesNotThrow("Could not run elide binary") {
                val code = CommandLineUtils.executeCommandLine(cli, out, out)
                assertEquals(0, code, "Elide binary produced an error: ${out.output}")
            }
            val lines = out.output.lineSequence().filter(String::isNotBlank).toList()
            assertEquals(1, lines.size, "Output should contain a single non-blank line")
            assertTrue(lines.first().trim().matches(ELIDE_VERSION_RE), "Output should be a valid Elide version")
        }
    }
}
