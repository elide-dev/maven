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
package dev.elide.maven.plugin.kotlin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Elide Kotlin to JVM test compiler Mojo.
 *
 * @author Lauri Heino <datafox>
 * @since 1.0.0
 */
@Mojo(name = "test-compile",
        defaultPhase = LifecyclePhase.TEST_COMPILE,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true)
public class ElideKotlinJVMTestCompileMojo extends ElideKotlinJVMTestCompileMojoImpl {
}
