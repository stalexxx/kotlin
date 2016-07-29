/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.script

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.parsing.KotlinParserDefinition
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtScript
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import java.io.File
import java.util.concurrent.Future
import kotlin.reflect.KClass
import kotlin.reflect.memberFunctions
import kotlin.reflect.primaryConstructor

data class KotlinScriptDefinitionFromTemplate(val template: KClass<out Any>,
                                              val resolver: ScriptDependenciesResolverEx? = null,
                                              val environment: Map<String, Any?>? = null
) : KotlinScriptDefinition {

    private val log = Logger.getInstance(KotlinScriptDefinitionFromTemplate::class.java)

    // TODO: remove this and simplify definitionAnnotation as soon as deprecated annotations will be removed
    internal class ScriptTemplateDefinitionData(val resolverClass: KClass<out ScriptDependenciesResolverEx>,
                                                val resolver: ScriptDependenciesResolverEx?,
                                                val scriptFilePattern: String?) {
        val acceptedAnnotations: List<KClass<out Annotation>> by lazy {
            val resolveMethod = ScriptDependenciesResolverEx::resolve
            val resolverMethodAnnotations =
                    resolverClass.memberFunctions.find {
                        it.name == resolveMethod.name &&
                        sameSignature(it, resolveMethod)
                    }
                            ?.annotations
                            ?.filterIsInstance<AcceptedAnnotations>()
            resolverMethodAnnotations?.flatMap {
                val v = it.supportedAnnotationClasses
                v.toList() // TODO: inline after KT-9453 is resolved (now it fails with "java.lang.Class cannot be cast to kotlin.reflect.KClass")
            }
            ?: emptyList()
        }
    }

    internal class ObsoleteResolverProxy(val resolverAnn: ScriptDependencyResolver?) : ScriptDependenciesResolverEx {
        private val resolver by lazy { resolverAnn?.resolver?.primaryConstructor?.call() }
        override fun resolve(script: ScriptContents,
                             environment: Map<String, Any?>?,
                             report: (ScriptDependenciesResolverEx.ReportSeverity, String, ScriptContents.Position?) -> Unit,
                             previousDependencies: KotlinScriptExternalDependencies?
        ): Future<KotlinScriptExternalDependencies?> =
                resolver?.resolve(
                        environment?.get("projectRoot") as? File?,
                        script.file,
                        emptyList(),
                        environment as Any?
                )
                .asFuture()
    }

    private val definitionData by lazy {
        val defAnn = template.annotations.firstIsInstanceOrNull<ScriptTemplateDefinition>()
        val obsoleteResolverAnn = template.annotations.firstIsInstanceOrNull<ScriptDependencyResolver>()
        val filePattern = defAnn?.scriptFilePattern ?:
                          template.annotations.firstIsInstanceOrNull<ScriptFilePattern>()?.pattern ?:
                          DEFAULT_SCRIPT_FILE_PATTERN
        when {
            resolver != null -> ScriptTemplateDefinitionData(resolver.javaClass.kotlin, resolver, filePattern)
            defAnn != null -> ScriptTemplateDefinitionData(defAnn.resolver, defAnn.resolver.primaryConstructor?.call(), filePattern)
            obsoleteResolverAnn != null -> ScriptTemplateDefinitionData(ObsoleteResolverProxy::class, ObsoleteResolverProxy(obsoleteResolverAnn), filePattern)
            else -> ScriptTemplateDefinitionData(BasicScriptDependenciesResolver::class, BasicScriptDependenciesResolver(), filePattern)
        }
    }

    override val name = template.simpleName!!

    override fun getScriptParameters(scriptDescriptor: ScriptDescriptor): List<ScriptParameter> =
            template.primaryConstructor!!.parameters.map { ScriptParameter(Name.identifier(it.name!!), getKotlinTypeByFqName(scriptDescriptor, it.type.toString())) }

    override fun getScriptSupertypes(scriptDescriptor: ScriptDescriptor): List<KotlinType> =
            listOf(getKotlinTypeByFqName(scriptDescriptor, template.qualifiedName!!))

    override fun getScriptParametersToPassToSuperclass(scriptDescriptor: ScriptDescriptor): List<Name> =
            getScriptParameters(scriptDescriptor).map { it.name }

    override fun <TF> isScript(file: TF): Boolean =
            definitionData.scriptFilePattern?.let { Regex(it).matches(getFileName(file)) } ?: false

    // TODO: implement other strategy - e.g. try to extract something from match with ScriptFilePattern
    override fun getScriptName(script: KtScript): Name = ScriptNameUtil.fileNameWithExtensionStripped(script, KotlinParserDefinition.STD_SCRIPT_EXT)

    override fun <TF> getDependenciesFor(file: TF, project: Project, previousDependencies: KotlinScriptExternalDependencies?): KotlinScriptExternalDependencies? {

        val script = BasicScriptContents(file) {
            val classLoader = (template as Any).javaClass.classLoader
            getAnnotationEntries(file, project)
                    .mapNotNull { psiAnn ->
                        // TODO: consider advanced matching using semantic similar to actual resolving
                        definitionData.acceptedAnnotations.find { ann ->
                            psiAnn.typeName.let { it == ann.simpleName || it == ann.qualifiedName }
                        }?.let { KtAnnotationWrapper(psiAnn, classLoader.loadClass(it.qualifiedName).kotlin as KClass<out Annotation>) }
                    }
                    .map { it.getProxy(classLoader) }
        }
        val reportFn = { reportSeverity: ScriptDependenciesResolverEx.ReportSeverity, s: String, position: ScriptContents.Position? ->
            val msg = (position?.run { "[at $line:$col]" } ?: "") + s
            when (reportSeverity) {
                ScriptDependenciesResolverEx.ReportSeverity.ERROR -> log.error(msg)
                ScriptDependenciesResolverEx.ReportSeverity.WARNING -> log.warn(msg)
                ScriptDependenciesResolverEx.ReportSeverity.INFO -> log.info(msg)
                ScriptDependenciesResolverEx.ReportSeverity.DEBUG -> log.debug(msg)
            }
        }

        val fileDeps = definitionData.resolver?.resolve(script, environment, reportFn, previousDependencies)
        return fileDeps?.get()
    }

    private fun <TF> getAnnotationEntries(file: TF, project: Project): Iterable<KtAnnotationEntry> = when (file) {
        is PsiFile -> getAnnotationEntriesFromPsiFile(file)
        is VirtualFile -> getAnnotationEntriesFromVirtualFile(file, project)
        is File -> {
            val virtualFile = (StandardFileSystems.local().findFileByPath(file.canonicalPath)
                               ?: throw IllegalArgumentException("Unable to find file ${file.canonicalPath}"))
            getAnnotationEntriesFromVirtualFile(virtualFile, project)
        }
        else -> throw IllegalArgumentException("Unsupported file type $file")
    }

    private fun getAnnotationEntriesFromPsiFile(file: PsiFile) =
            if (file is KtFile) file.annotationEntries
            else throw IllegalArgumentException("Unable to extract kotlin annotations from ${file.name} (${file.fileType})")

    private fun getAnnotationEntriesFromVirtualFile(file: VirtualFile, project: Project): Iterable<KtAnnotationEntry> {
        val psiFile: PsiFile = PsiManager.getInstance(project).findFile(file)
                                                ?: throw IllegalArgumentException("Unable to load PSI from ${file.canonicalPath}")
        return getAnnotationEntriesFromPsiFile(psiFile)
    }

    class BasicScriptContents<out TF>(myFile: TF, getAnnotations: () -> Iterable<Annotation>) : ScriptContents {
        override val file: File? by lazy { getFile(myFile) }
        override val annotations: Iterable<Annotation> by lazy { getAnnotations() }
        override val text: CharSequence? by lazy { getFileContents(myFile) }
    }
}