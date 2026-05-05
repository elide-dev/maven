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

import org.apache.maven.execution.MavenSession
import org.apache.maven.model.Plugin
import org.apache.maven.project.MavenProject
import org.jetbrains.kotlin.maven.KotlinLifecycleParticipant

/**
 * Map [KotlinLifecycleParticipant] to work as intended but use this plugin instead.
 *
 * @author Lauri Heino <datafox>
 * @since 1.0.0
 */
open class ElideKotlinLifecycleParticipant : KotlinLifecycleParticipant() {
    var artifact: String = ELIDE_KOTLIN_PLUGIN_ARTIFACT_ID

    override fun afterProjectsRead(session: MavenSession) {
        // using reflection here to avoid copying the entire parent class
        for (project in session.projects) {
            val plugin = getElideKotlinMavenPlugin(project)
            if (
                plugin != null &&
                    callPrivateFuncBoolean<KotlinLifecycleParticipant>("isExtensionsEnabled", plugin.callArg()) &&
                    callPrivateFuncBoolean<KotlinLifecycleParticipant>("isSmartDefaultsEnabled", project.callArg())
            ) {
                callPrivateFunc<KotlinLifecycleParticipant>(
                    "configureSmartDefaults",
                    project.callArg(),
                    plugin.callArg(),
                )
            }
        }
    }

    fun getElideKotlinMavenPlugin(project: MavenProject): Plugin? {
        for (plugin in project.getBuildPlugins()) {
            if (ELIDE_GROUP_ID == plugin.groupId && artifact == plugin.artifactId) {
                return plugin
            }
        }
        return null
    }

    inline fun <reified T> T.callPrivateFuncBoolean(
        name: String,
        vararg args: Pair<Class<out Any>, out Any?>,
    ): Boolean =
        T::class
            .java
            .getDeclaredMethod(name, *args.map { it.first }.toTypedArray())
            .apply { isAccessible = true }
            .invoke(this, *args.map { it.second }.toTypedArray())
            .let { it as Boolean }

    inline fun <reified T> T.callPrivateFunc(name: String, vararg args: Pair<Class<out Any>, out Any?>) {
        T::class
            .java
            .getDeclaredMethod(name, *args.map { it.first }.toTypedArray())
            .apply { isAccessible = true }
            .invoke(this, *args.map { it.second }.toTypedArray())
    }

    inline fun <reified T> T.callArg() = Pair(T::class.java, this)

    companion object {
        const val ELIDE_GROUP_ID = "dev.elide"
        const val ELIDE_KOTLIN_PLUGIN_ARTIFACT_ID = "elide-kotlin-maven-plugin"
    }
}
