package dev.elide.maven.plugin.kotlin

import org.apache.maven.AbstractMavenLifecycleParticipant
import org.apache.maven.MavenExecutionException
import org.apache.maven.execution.MavenSession
import org.apache.maven.model.Dependency
import org.apache.maven.model.Plugin
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.logging.LogEnabled
import org.codehaus.plexus.logging.Logger
import org.codehaus.plexus.util.xml.Xpp3Dom
import java.io.File

/** @author Lauri Heino <datafox> */
class ElideKotlinLifecycleParticipant : AbstractMavenLifecycleParticipant(), LogEnabled {
    private var logger: Logger? = null

    override fun enableLogging(logger: Logger?) {
        this.logger = logger
    }

    @Throws(MavenExecutionException::class)
    override fun afterProjectsRead(session: MavenSession) {
        for (project in session.projects) {
            val plugin = getKotlinMavenPlugin(project)
            if (plugin != null && isExtensionsEnabled(plugin) && isSmartDefaultsEnabled(project)) {
                configureSmartDefaults(project, plugin)
            }
        }
    }

    private fun isSmartDefaultsEnabled(project: MavenProject): Boolean {
        // System property has priority
        val sysProp = System.getProperty(SMART_DEFAULTS_ENABLED_PROPERTY)
        if (sysProp != null) {
            return sysProp.toBoolean()
        }

        // Then check project properties
        val projectProp = project.properties.getProperty(SMART_DEFAULTS_ENABLED_PROPERTY)
        if (projectProp != null) {
            return projectProp.toBoolean()
        }

        // Enabled by default (when extensions=true)
        return true
    }

    private fun configureSmartDefaults(project: MavenProject, plugin: Plugin) {
        logger?.info("Kotlin smart defaults are enabled for " + project.artifactId)

        addSourceRoots(project, plugin)
        addStdlibDependency(project)
    }

    private fun addSourceRoots(project: MavenProject, plugin: Plugin) {
        if (hasUserDefinedSourceDirs(plugin)) {
            return
        }

        val baseDir = project.basedir

        val mainKotlinSource = File(baseDir, "src/main/kotlin")
        if (mainKotlinSource.exists()) {
            project.addCompileSourceRoot(mainKotlinSource.absolutePath)
        }

        val testKotlinSource = File(baseDir, "src/test/kotlin")
        if (testKotlinSource.exists()) {
            project.addTestCompileSourceRoot(testKotlinSource.absolutePath)
        }
    }

    private fun hasUserDefinedSourceDirs(plugin: Plugin): Boolean {
        val configuration = plugin.configuration
        if (configuration is Xpp3Dom) {
            val sourceDirs = configuration.getChild("sourceDirs")
            return sourceDirs != null && sourceDirs.getChildCount() > 0
        }
        return false
    }

    private fun addStdlibDependency(project: MavenProject) {
        if (hasStdlibDependency(project)) {
            return
        }

        val stdlib = Dependency()
        stdlib.groupId = KOTLIN_GROUP_ID
        stdlib.artifactId = KOTLIN_STDLIB_ARTIFACT_ID
        stdlib.version = KOTLIN_VERSION
        project.dependencies.add(stdlib)

        logger?.info("Added kotlin-stdlib dependency, version $KOTLIN_VERSION")
    }

    private fun hasStdlibDependency(project: MavenProject): Boolean {
        for (dependency in project.dependencies) {
            if (KOTLIN_GROUP_ID == dependency.groupId && KOTLIN_STDLIB_ARTIFACT_ID == dependency.artifactId) {
                return true
            }
        }
        return false
    }

    private fun getKotlinMavenPlugin(project: MavenProject): Plugin? {
        for (plugin in project.getBuildPlugins()) {
            if (PLUGIN_GROUP_ID == plugin.groupId && PLUGIN_ARTIFACT_ID == plugin.artifactId) {
                return plugin
            }
        }
        return null
    }

    private fun isExtensionsEnabled(plugin: Plugin): Boolean {
        return plugin.extensions.toString().toBoolean()
    }

    companion object {
        private const val PLUGIN_GROUP_ID = "dev.elide"
        private const val PLUGIN_ARTIFACT_ID = "elide-kotlin-maven-plugin"
        private const val KOTLIN_GROUP_ID = "org.jetbrains.kotlin"
        private const val KOTLIN_STDLIB_ARTIFACT_ID = "kotlin-stdlib"
        private const val SMART_DEFAULTS_ENABLED_PROPERTY = "kotlin.smart.defaults.enabled"
        private const val KOTLIN_VERSION = "2.3.21"
    }
}
