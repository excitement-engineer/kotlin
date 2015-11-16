/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.jps.build

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.containers.MultiMap
import gnu.trove.THashSet
import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.builders.BuildTarget
import org.jetbrains.jps.builders.DirtyFilesHolder
import org.jetbrains.jps.builders.impl.BuildTargetRegistryImpl
import org.jetbrains.jps.builders.impl.TargetOutputIndexImpl
import org.jetbrains.jps.builders.java.JavaBuilderUtil
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor
import org.jetbrains.jps.incremental.*
import org.jetbrains.jps.incremental.ModuleLevelBuilder.ExitCode.*
import org.jetbrains.jps.incremental.fs.CompilationRound
import org.jetbrains.jps.incremental.java.JavaBuilder
import org.jetbrains.jps.incremental.messages.BuildMessage
import org.jetbrains.jps.incremental.messages.CompilerMessage
import org.jetbrains.jps.incremental.storage.BuildDataManager
import org.jetbrains.jps.model.JpsProject
import org.jetbrains.jps.model.JpsSimpleElement
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase
import org.jetbrains.kotlin.cli.common.KotlinVersion
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.*
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.OutputMessageUtil
import org.jetbrains.kotlin.compilerRunner.CompilerEnvironment
import org.jetbrains.kotlin.compilerRunner.KotlinCompilerRunner
import org.jetbrains.kotlin.compilerRunner.OutputItemsCollectorImpl
import org.jetbrains.kotlin.config.CompilerRunnerConstants
import org.jetbrains.kotlin.config.CompilerRunnerConstants.INTERNAL_ERROR_PREFIX
import org.jetbrains.kotlin.config.IncrementalCompilation
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.daemon.common.isDaemonEnabled
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.jps.JpsKotlinCompilerSettings
import org.jetbrains.kotlin.jps.incremental.*
import org.jetbrains.kotlin.load.kotlin.ModuleMapping
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCache
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCompilationComponents
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.progress.CompilationCanceledException
import org.jetbrains.kotlin.progress.CompilationCanceledStatus
import org.jetbrains.kotlin.utils.LibraryUtils
import org.jetbrains.kotlin.utils.PathUtil
import org.jetbrains.kotlin.utils.keysToMap
import org.jetbrains.kotlin.utils.sure
import org.jetbrains.org.objectweb.asm.ClassReader
import java.io.File
import java.util.*

public class KotlinBuilder : ModuleLevelBuilder(BuilderCategory.SOURCE_PROCESSOR) {
    companion object {
        // TODO add description to string
        private val TARGETS_WITH_CLEARED_CACHES = Key<Set<ModuleBuildTarget>>("")

        public val KOTLIN_BUILDER_NAME: String = "Kotlin Builder"
        public val LOOKUP_TRACKER: JpsElementChildRoleBase<JpsSimpleElement<out LookupTracker>> = JpsElementChildRoleBase.create("lookup tracker")
        val LOG = Logger.getInstance("#org.jetbrains.kotlin.jps.build.KotlinBuilder")

        private fun registerTargetsWithClearedCaches(context: CompileContext, targets: Set<ModuleBuildTarget>) {
            synchronized(TARGETS_WITH_CLEARED_CACHES) {
                val data = (context.getUserData(TARGETS_WITH_CLEARED_CACHES) ?: setOf()) + targets
                context.putUserData(TARGETS_WITH_CLEARED_CACHES, data)
            }
        }

        private fun unregisterTargetsWithClearedCaches(context: CompileContext, targets: Set<ModuleBuildTarget>) {
            synchronized(TARGETS_WITH_CLEARED_CACHES) {
                val data = (context.getUserData(TARGETS_WITH_CLEARED_CACHES) ?: setOf()) - targets
                context.putUserData(TARGETS_WITH_CLEARED_CACHES, data)
            }
        }
    }

    private val statisticsLogger = TeamcityStatisticsLogger()

    override fun getPresentableName() = KOTLIN_BUILDER_NAME

    override fun getCompilableFileExtensions() = arrayListOf("kt")

    override fun buildStarted(context: CompileContext) {
        LOG.debug("==========================================")
        LOG.info("is Kotlin incremental compilation enabled: ${IncrementalCompilation.isEnabled()}")
        LOG.info("is Kotlin experimental incremental compilation enabled: ${IncrementalCompilation.isExperimental()}")
        LOG.info("is Kotlin compiler daemon enabled: ${isDaemonEnabled()}")

        val historyLabel = context.getBuilderParameter("history label")
        if (historyLabel != null) {
            LOG.info("Label in local history: $historyLabel")
        }
    }

    override fun build(
            context: CompileContext,
            chunk: ModuleChunk,
            dirtyFilesHolder: DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget>,
            outputConsumer: ModuleLevelBuilder.OutputConsumer
    ): ModuleLevelBuilder.ExitCode {
        LOG.debug("------------------------------------------")
        val messageCollector = MessageCollectorAdapter(context)

        try {
            val exitCode = doBuild(chunk, context, dirtyFilesHolder, messageCollector, outputConsumer)
            LOG.debug("Build result: " + exitCode)

            if (exitCode != ExitCode.CHUNK_REBUILD_REQUIRED && exitCode != ABORT) {
                saveVersions(context, chunk)
            }

            return exitCode
        }
        catch (e: StopBuildException) {
            LOG.debug("Caught exception: " + e)
            throw e
        }
        catch (e: Throwable) {
            LOG.debug("Caught exception: " + e)

            messageCollector.report(
                    CompilerMessageSeverity.EXCEPTION,
                    OutputMessageUtil.renderException(e),
                    CompilerMessageLocation.NO_LOCATION
            )
            return ABORT
        }
    }

    private fun doBuild(
            chunk: ModuleChunk,
            context: CompileContext,
            dirtyFilesHolder: DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget>,
            messageCollector: MessageCollectorAdapter, outputConsumer: ModuleLevelBuilder.OutputConsumer
    ): ModuleLevelBuilder.ExitCode {
        // Workaround for Android Studio
        if (!JavaBuilder.IS_ENABLED[context, true] && !JpsUtils.isJsKotlinModule(chunk.representativeTarget())) {
            messageCollector.report(INFO, "Kotlin JPS plugin is disabled", CompilerMessageLocation.NO_LOCATION)
            return NOTHING_DONE
        }

        val projectDescriptor = context.projectDescriptor
        val dataManager = projectDescriptor.dataManager
        val targets = chunk.targets
        val requestedToRebuild = context.getUserData(TARGETS_WITH_CLEARED_CACHES) ?: setOf()
        val isFullRebuild = JavaBuilderUtil.isForcedRecompilationAllJavaModules(context)

        if (!isFullRebuild && !requestedToRebuild.containsAll(targets)) {
            val exitCode = checkVersions(context, dataManager, targets)

            if (exitCode != null) return exitCode
        }

        if (!dirtyFilesHolder.hasDirtyFiles() &&
            !dirtyFilesHolder.hasRemovedFiles() ||
            !hasKotlinDirtyOrRemovedFiles(dirtyFilesHolder, chunk)
        ) {
            return NOTHING_DONE
        }

        messageCollector.report(INFO, "Kotlin JPS plugin version " + KotlinVersion.VERSION, CompilerMessageLocation.NO_LOCATION)

        val project = projectDescriptor.project
        val lookupTracker = getLookupTracker(project)
        val incrementalCaches = getIncrementalCaches(chunk, context)
        val environment = createCompileEnvironment(incrementalCaches, lookupTracker, context)
        if (!environment.success()) {
            environment.reportErrorsTo(messageCollector)
            return ABORT
        }

        val commonArguments = JpsKotlinCompilerSettings.getCommonCompilerArguments(project)
        commonArguments.verbose = true // Make compiler report source to output files mapping

        val allCompiledFiles = getAllCompiledFilesContainer(context)
        val filesToCompile = KotlinSourceFileCollector.getDirtySourceFiles(dirtyFilesHolder)

        LOG.debug("Compiling files: ${filesToCompile.values()}")

        val start = System.nanoTime()
        val outputItemCollector = doCompileModuleChunk(allCompiledFiles, chunk, commonArguments, context, dirtyFilesHolder,
                                                       environment, filesToCompile, incrementalCaches, messageCollector, project)

        statisticsLogger.registerStatistic(chunk, System.nanoTime() - start)

        if (outputItemCollector == null) {
            return NOTHING_DONE
        }

        val compilationErrors = Utils.ERRORS_DETECTED_KEY[context, false]
        if (compilationErrors) {
            LOG.info("Compiled with errors")
            return ABORT
        }
        else {
            LOG.info("Compiled successfully")
        }

        val generatedFiles = getGeneratedFiles(chunk, outputItemCollector)

        registerOutputItems(outputConsumer, generatedFiles)

        if (JpsUtils.isJsKotlinModule(chunk.representativeTarget())) {
            copyJsLibraryFilesIfNeeded(chunk, project)
            return OK
        }

        val generatedClasses = generatedFiles.filterIsInstance<GeneratedJvmClass>()
        updateJavaMappings(chunk, compilationErrors, context, dirtyFilesHolder, filesToCompile, generatedClasses)

        if (!IncrementalCompilation.isEnabled()) {
            return OK
        }

        context.checkCanceled()

        val changesInfo = updateKotlinIncrementalCache(compilationErrors, incrementalCaches, generatedFiles)
        updateLookupStorage(chunk, lookupTracker, dataManager, dirtyFilesHolder, filesToCompile)

        if (isFullRebuild) {
            return OK
        }

        val caches = filesToCompile.keySet().map { incrementalCaches[it]!! }
        processChanges(context, chunk, filesToCompile.values(), allCompiledFiles, dataManager, caches, changesInfo)

        return ADDITIONAL_PASS_REQUIRED
    }

    private fun processChanges(
            context: CompileContext,
            chunk: ModuleChunk,
            compiledFiles: Collection<File>,
            allCompiledFiles: MutableSet<File>,
            dataManager: BuildDataManager,
            caches: List<IncrementalCacheImpl>,
            compilationResult: CompilationResult
    ) {
        fun recompileInlined() {
            for (cache in caches) {
                val filesToReinline = cache.getFilesToReinline()

                filesToReinline.forEach {
                    FSOperations.markDirty(context, CompilationRound.NEXT, it)
                }
            }
        }

        fun CompilationResult.doProcessChanges() {
            fun isKotlin(file: File) = KotlinSourceFileCollector.isKotlinSourceFile(file)
            fun isNotCompiled(file: File) = file !in allCompiledFiles

            LOG.debug("compilationResult = $this")

            when {
                inlineAdded -> {
                    allCompiledFiles.clear()
                    FSOperations.markDirtyRecursively(context, CompilationRound.NEXT, chunk, ::isKotlin)
                    return
                }
                constantsChanged -> {
                    FSOperations.markDirtyRecursively(context, CompilationRound.NEXT, chunk, ::isNotCompiled)
                    return
                }
                protoChanged -> {
                    FSOperations.markDirty(context, CompilationRound.NEXT, chunk, { isKotlin(it) && isNotCompiled(it) })
                }
            }

            if (inlineChanged) {
                recompileInlined()
            }
        }

        fun CompilationResult.doProcessChangesUsingLookups() {
            val lookupStorage = dataManager.getStorage(KotlinDataContainerTarget, LookupStorageProvider)

            LOG.debug("Start processing changes")

            // TODO group by fqName?
            for (change in changes) {
                LOG.debug("Process $change")

                if (change !is ChangeInfo.MembersChanged) continue

                val files = change.names
                        .flatMap { lookupStorage.get(LookupSymbol(it, change.fqName.asString())) }
                        .asSequence()
                        .map { File(it) }
                        .filter { it !in compiledFiles && it.exists() }
                        .toList()

                LOG.debug("Mark dirty files: $files")

                files.forEach {
                    FSOperations.markDirty(context, CompilationRound.NEXT, it)
                }
            }

            LOG.debug("End of processing changes")

            caches.forEach { it.cleanDirtyInlineFunctions() }
        }

        if (IncrementalCompilation.isExperimental()) {
            compilationResult.doProcessChangesUsingLookups()
        }
        else {
            compilationResult.doProcessChanges()
        }
    }


    private fun checkVersions(context: CompileContext, dataManager: BuildDataManager, targets: MutableSet<ModuleBuildTarget>): ExitCode? {
        val cacheVersionsProvider = CacheVersionProvider(dataManager.dataPaths)
        val allVersions = cacheVersionsProvider.allVersions(targets)
        val actions = allVersions.map { it.checkVersion() }.toSet().sorted()

        fun cleanNormalCaches() {
            LOG.info("Clearing caches for " + targets.joinToString { it.presentableName })
            targets.forEach { dataManager.getKotlinCache(it).clean() }
        }

        for (status in actions) {
            when (status) {
                CacheVersion.Action.REBUILD_ALL_KOTLIN -> {
                    LOG.info("Kotlin global lookup map format changed, so rebuild all kotlin")
                    val project = context.projectDescriptor.project
                    val sourceRoots = project.modules.flatMap { it.sourceRoots }

                    for (sourceRoot in sourceRoots) {
                        val ktFiles = sourceRoot.file.walk().filter { KotlinSourceFileCollector.isKotlinSourceFile(it) }
                        ktFiles.forEach { kt ->
                            FSOperations.markDirty(context, CompilationRound.NEXT, kt)
                        }
                    }

                    val buildTargetIndex = context.projectDescriptor.buildTargetIndex
                    val allTargets = buildTargetIndex.allTargets.filterIsInstance<ModuleBuildTarget>().toSet()

                    for (target in targets) {
                        dataManager.getKotlinCache(target).clean()
                    }

                    dataManager.getStorage(KotlinDataContainerTarget, LookupStorageProvider).clean()
                    registerTargetsWithClearedCaches(context, allTargets)

                    return CHUNK_REBUILD_REQUIRED
                }
                CacheVersion.Action.REBUILD_CHUNK -> {
                    cleanNormalCaches()
                    registerTargetsWithClearedCaches(context, targets)
                    return CHUNK_REBUILD_REQUIRED
                }
                CacheVersion.Action.CLEAN_NORMAL_CACHES -> {
                    cleanNormalCaches()
                }
                CacheVersion.Action.CLEAN_EXPERIMENTAL_CACHES -> {
                    LOG.info("Clearing experimental caches for " + targets.joinToString { it.presentableName })
                    targets.forEach { dataManager.getKotlinCache(it).cleanExperimental() }
                }
                CacheVersion.Action.CLEAN_DATA_CONTAINER -> {
                    LOG.info("Clearing lookup cache")
                    dataManager.getStorage(KotlinDataContainerTarget, LookupStorageProvider).clean()
                    cacheVersionsProvider.dataContainerVersion().clean()
                }
                else -> {
                    assert(status == CacheVersion.Action.DO_NOTHING) { "Unknown version status $status" }
                }
            }
        }

        return null
    }

    private fun saveVersions(context: CompileContext, chunk: ModuleChunk) {
        val dataManager = context.projectDescriptor.dataManager
        val targets = chunk.targets
        val cacheVersionsProvider = CacheVersionProvider(dataManager.dataPaths)
        cacheVersionsProvider.allVersions(targets).forEach { it.saveIfNeeded() }
        unregisterTargetsWithClearedCaches(context, targets)
    }

    private fun doCompileModuleChunk(
            allCompiledFiles: MutableSet<File>, chunk: ModuleChunk, commonArguments: CommonCompilerArguments, context: CompileContext,
            dirtyFilesHolder: DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget>, environment: CompilerEnvironment,
            filesToCompile: MultiMap<ModuleBuildTarget, File>, incrementalCaches: Map<ModuleBuildTarget, IncrementalCacheImpl>,
            messageCollector: MessageCollectorAdapter, project: JpsProject
    ): OutputItemsCollectorImpl? {

        if (JpsUtils.isJsKotlinModule(chunk.representativeTarget())) {
            LOG.debug("Compiling to JS ${filesToCompile.values().size()} files in ${filesToCompile.keySet().joinToString { it.presentableName }}")
            return compileToJs(chunk, commonArguments, environment, null, messageCollector, project)
        }

        if (IncrementalCompilation.isEnabled()) {
            for (target in chunk.targets) {
                val cache = incrementalCaches[target]!!
                val removedAndDirtyFiles = filesToCompile[target] + dirtyFilesHolder.getRemovedFiles(target).map { File(it) }
                cache.markOutputClassesDirty(removedAndDirtyFiles)
            }
        }

        val representativeTarget = chunk.representativeTarget()

        fun concatenate(strings: Array<String>?, cp: List<String>) = arrayOf(*(strings ?: emptyArray()), *cp.toTypedArray())

        for (argumentProvider in ServiceLoader.load(javaClass<KotlinJpsCompilerArgumentsProvider>())) {
            // appending to pluginOptions
            commonArguments.pluginOptions = concatenate(commonArguments.pluginOptions,
                                                        argumentProvider.getExtraArguments(representativeTarget, context))
            // appending to classpath
            commonArguments.pluginClasspaths = concatenate(commonArguments.pluginClasspaths,
                                                           argumentProvider.getClasspath(representativeTarget, context))

            messageCollector.report(
                    INFO,
                    "Plugin loaded: ${argumentProvider.javaClass.getSimpleName()}",
                    CompilerMessageLocation.NO_LOCATION
            )
        }

        return compileToJvm(allCompiledFiles, chunk, commonArguments, context, dirtyFilesHolder, environment, filesToCompile, messageCollector)
    }

    private fun createCompileEnvironment(
            incrementalCaches: Map<ModuleBuildTarget, IncrementalCache>,
            lookupTracker: LookupTracker,
            context: CompileContext
    ): CompilerEnvironment {
        val compilerServices = Services.Builder()
                .register(javaClass<IncrementalCompilationComponents>(), IncrementalCompilationComponentsImpl(incrementalCaches, lookupTracker))
                .register(javaClass<CompilationCanceledStatus>(), object : CompilationCanceledStatus {
                    override fun checkCanceled() {
                        if (context.getCancelStatus().isCanceled()) throw CompilationCanceledException()
                    }
                })
                .build()

        return CompilerEnvironment.getEnvironmentFor(
                PathUtil.getKotlinPathsForJpsPluginOrJpsTests(),
                { className ->
                    className.startsWith("org.jetbrains.kotlin.load.kotlin.incremental.components.")
                    || className.startsWith("org.jetbrains.kotlin.incremental.components.")
                    || className == "org.jetbrains.kotlin.config.Services"
                    || className.startsWith("org.apache.log4j.") // For logging from compiler
                    || className == "org.jetbrains.kotlin.progress.CompilationCanceledStatus"
                    || className == "org.jetbrains.kotlin.progress.CompilationCanceledException"
                    || className == "org.jetbrains.kotlin.modules.TargetId"
                },
                compilerServices
        )
    }

    private fun getGeneratedFiles(
            chunk: ModuleChunk,
            outputItemCollector: OutputItemsCollectorImpl
    ): List<GeneratedFile> {
        // If there's only one target, this map is empty: get() always returns null, and the representativeTarget will be used below
        val sourceToTarget = HashMap<File, ModuleBuildTarget>()
        if (chunk.getTargets().size() > 1) {
            for (target in chunk.getTargets()) {
                for (file in KotlinSourceFileCollector.getAllKotlinSourceFiles(target)) {
                    sourceToTarget.put(file, target)
                }
            }
        }

        val result = ArrayList<GeneratedFile>()

        val representativeTarget = chunk.representativeTarget()
        for (outputItem in outputItemCollector.getOutputs()) {
            val sourceFiles = outputItem.sourceFiles
            val outputFile = outputItem.outputFile
            val target =
                    sourceFiles.firstOrNull()?.let { sourceToTarget[it] } ?:
                    chunk.targets.filter { it.outputDir?.let { outputFile.startsWith(it) } ?: false }.singleOrNull() ?:
                    representativeTarget

            if (outputFile.getName().endsWith(".class")) {
                result.add(GeneratedJvmClass(target, sourceFiles, outputFile))
            }
            else {
                result.add(GeneratedFile(target, sourceFiles, outputFile))
            }
        }
        return result
    }

    private fun updateJavaMappings(
            chunk: ModuleChunk,
            compilationErrors: Boolean,
            context: CompileContext,
            dirtyFilesHolder: DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget>,
            filesToCompile: MultiMap<ModuleBuildTarget, File>,
            generatedClasses: List<GeneratedJvmClass>
    ) {
        val previousMappings = context.getProjectDescriptor().dataManager.getMappings()
        val delta = previousMappings.createDelta()
        val callback = delta.getCallback()

        for (generatedClass in generatedClasses) {
            callback.associate(
                    FileUtil.toSystemIndependentName(generatedClass.outputFile.getAbsolutePath()),
                    generatedClass.sourceFiles.map { FileUtil.toSystemIndependentName(it.getAbsolutePath()) },
                    ClassReader(generatedClass.outputClass.getFileContents())
            )
        }

        val allCompiled = filesToCompile.values()
        val compiledInThisRound = if (compilationErrors) listOf<File>() else allCompiled
        JavaBuilderUtil.updateMappings(context, delta, dirtyFilesHolder, chunk, allCompiled, compiledInThisRound)
    }

    private fun registerOutputItems(outputConsumer: ModuleLevelBuilder.OutputConsumer, generatedFiles: List<GeneratedFile>) {
        for (generatedFile in generatedFiles) {
            outputConsumer.registerOutputFile(generatedFile.target, generatedFile.outputFile, generatedFile.sourceFiles.map { it.getPath() })
        }
    }

    private fun updateKotlinIncrementalCache(
            compilationErrors: Boolean,
            incrementalCaches: Map<ModuleBuildTarget, IncrementalCacheImpl>,
            generatedFiles: List<GeneratedFile>
    ): CompilationResult {

        assert(IncrementalCompilation.isEnabled()) { "updateKotlinIncrementalCache should not be called when incremental compilation disabled" }

        var changesInfo = CompilationResult.NO_CHANGES
        for (generatedFile in generatedFiles) {
            val ic = incrementalCaches[generatedFile.target]!!
            val newChangesInfo =
                    if (generatedFile is GeneratedJvmClass) {
                        ic.saveFileToCache(generatedFile)
                    }
                    else if (generatedFile.outputFile.isModuleMappingFile()) {
                        ic.saveModuleMappingToCache(generatedFile.sourceFiles, generatedFile.outputFile)
                    }
                    else {
                        continue
                    }

            changesInfo += newChangesInfo
        }

        if (!compilationErrors) {
            incrementalCaches.values().forEach {
                val newChangesInfo = it.clearCacheForRemovedClasses()
                changesInfo += newChangesInfo
            }
        }

        return changesInfo
    }

    private fun updateLookupStorage(
            chunk: ModuleChunk,
            lookupTracker: LookupTracker,
            dataManager: BuildDataManager,
            dirtyFilesHolder: DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget>,
            filesToCompile: MultiMap<ModuleBuildTarget, File>
    ) {
        if (!IncrementalCompilation.isExperimental()) return

        if (lookupTracker !is LookupTrackerImpl) throw AssertionError("Lookup tracker is expected to be LookupTrackerImpl, got ${lookupTracker.javaClass}")

        val lookupStorage = dataManager.getStorage(KotlinDataContainerTarget, LookupStorageProvider)

        filesToCompile.values().forEach { lookupStorage.removeLookupsFrom(it) }
        val removedFiles = chunk.targets.flatMap { KotlinSourceFileCollector.getRemovedKotlinFiles(dirtyFilesHolder, it) }
        removedFiles.forEach { lookupStorage.removeLookupsFrom(it) }

        lookupTracker.lookups.entrySet().forEach { lookupStorage.add(it.key, it.value) }
    }

    private fun File.isModuleMappingFile() = extension == ModuleMapping.MAPPING_FILE_EXT && parentFile.name == "META-INF"

    // if null is returned, nothing was done
    private fun compileToJs(chunk: ModuleChunk,
                            commonArguments: CommonCompilerArguments,
                            environment: CompilerEnvironment,
                            incrementalCaches: MutableMap<TargetId, IncrementalCache>?,
                            messageCollector: MessageCollectorAdapter, project: JpsProject
    ): OutputItemsCollectorImpl? {
        val outputItemCollector = OutputItemsCollectorImpl()

        val representativeTarget = chunk.representativeTarget()
        if (chunk.getModules().size() > 1) {
            // We do not support circular dependencies, but if they are present, we do our best should not break the build,
            // so we simply yield a warning and report NOTHING_DONE
            messageCollector.report(
                    WARNING,
                    "Circular dependencies are not supported. The following JS modules depend on each other: "
                    + chunk.getModules().map { it.getName() }.joinToString(", ") + ". "
                    + "Kotlin is not compiled for these modules",
                    CompilerMessageLocation.NO_LOCATION
            )
            return null
        }

        val sourceFiles = KotlinSourceFileCollector.getAllKotlinSourceFiles(representativeTarget)
        if (sourceFiles.isEmpty()) {
            return null
        }

        val outputDir = KotlinBuilderModuleScriptGenerator.getOutputDirSafe(representativeTarget)

        val moduleName = representativeTarget.getModule().getName()
        val outputFile = JpsJsModuleUtils.getOutputFile(outputDir, moduleName)
        val libraryFiles = JpsJsModuleUtils.getLibraryFilesAndDependencies(representativeTarget)
        val compilerSettings = JpsKotlinCompilerSettings.getCompilerSettings(project)
        val k2JsArguments = JpsKotlinCompilerSettings.getK2JsCompilerArguments(project)

        KotlinCompilerRunner.runK2JsCompiler(commonArguments, k2JsArguments, compilerSettings, messageCollector, environment, outputItemCollector, sourceFiles, libraryFiles, outputFile)
        return outputItemCollector
    }

    private fun copyJsLibraryFilesIfNeeded(chunk: ModuleChunk, project: JpsProject) {
        val representativeTarget = chunk.representativeTarget()
        val outputDir = KotlinBuilderModuleScriptGenerator.getOutputDirSafe(representativeTarget)
        val compilerSettings = JpsKotlinCompilerSettings.getCompilerSettings(project)
        if (compilerSettings.copyJsLibraryFiles) {
            val outputLibraryRuntimeDirectory = File(outputDir, compilerSettings.outputDirectoryForJsLibraryFiles).getAbsolutePath()
            val libraryFilesToCopy = arrayListOf<String>()
            JpsJsModuleUtils.getLibraryFiles(representativeTarget, libraryFilesToCopy)
            LibraryUtils.copyJsFilesFromLibraries(libraryFilesToCopy, outputLibraryRuntimeDirectory)
        }
    }

    // if null is returned, nothing was done
    private fun compileToJvm(allCompiledFiles: MutableSet<File>,
                             chunk: ModuleChunk,
                             commonArguments: CommonCompilerArguments,
                             context: CompileContext,
                             dirtyFilesHolder: DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget>,
                             environment: CompilerEnvironment,
                             filesToCompile: MultiMap<ModuleBuildTarget, File>, messageCollector: MessageCollectorAdapter
    ): OutputItemsCollectorImpl? {
        val outputItemCollector = OutputItemsCollectorImpl()

        if (chunk.getModules().size() > 1) {
            messageCollector.report(
                    WARNING,
                    "Circular dependencies are only partially supported. The following modules depend on each other: "
                    + chunk.getModules().map { it.getName() }.joinToString(", ") + ". "
                    + "Kotlin will compile them, but some strange effect may happen",
                    CompilerMessageLocation.NO_LOCATION
            )
        }

        allCompiledFiles.addAll(filesToCompile.values())

        val processedTargetsWithRemoved = getProcessedTargetsWithRemovedFilesContainer(context)

        var totalRemovedFiles = 0
        for (target in chunk.getTargets()) {
            val removedFilesInTarget = KotlinSourceFileCollector.getRemovedKotlinFiles(dirtyFilesHolder, target)
            if (!removedFilesInTarget.isEmpty()) {
                if (processedTargetsWithRemoved.add(target)) {
                    totalRemovedFiles += removedFilesInTarget.size()
                }
            }
        }

        val moduleFile = KotlinBuilderModuleScriptGenerator.generateModuleDescription(context, chunk, filesToCompile, totalRemovedFiles != 0)
        if (moduleFile == null) {
            KotlinBuilder.LOG.debug("Not compiling, because no files affected: " + filesToCompile.keySet().joinToString { it.presentableName })
            // No Kotlin sources found
            return null
        }

        val project = context.getProjectDescriptor().getProject()
        val k2JvmArguments = JpsKotlinCompilerSettings.getK2JvmCompilerArguments(project)
        val compilerSettings = JpsKotlinCompilerSettings.getCompilerSettings(project)

        KotlinBuilder.LOG.debug("Compiling to JVM ${filesToCompile.values().size()} files"
                                + (if (totalRemovedFiles == 0) "" else " ($totalRemovedFiles removed files)")
                                + " in " + filesToCompile.keySet().joinToString { it.presentableName })

        KotlinCompilerRunner.runK2JvmCompiler(commonArguments, k2JvmArguments, compilerSettings, messageCollector, environment, moduleFile, outputItemCollector)
        moduleFile.delete()

        return outputItemCollector
    }

    public class MessageCollectorAdapter(private val context: CompileContext) : MessageCollector {

        override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation) {
            var prefix = ""
            if (severity == EXCEPTION) {
                prefix = INTERNAL_ERROR_PREFIX
            }
            context.processMessage(CompilerMessage(
                    CompilerRunnerConstants.KOTLIN_COMPILER_NAME,
                    kind(severity),
                    prefix + message + renderLocationIfNeeded(location),
                    location.path,
                    -1, -1, -1,
                    location.line.toLong(), location.column.toLong()
            ))
        }

        private fun renderLocationIfNeeded(location: CompilerMessageLocation): String {
            if (location == CompilerMessageLocation.NO_LOCATION) return ""

            // Sometimes we report errors in JavaScript library stubs, i.e. files like core/javautil.kt
            // IDEA can't find these files, and does not display paths in Messages View, so we add the position information
            // to the error message itself:
            val pathname = "" + location.path
            return if (File(pathname).exists()) "" else " (" + location + ")"
        }

        private fun kind(severity: CompilerMessageSeverity): BuildMessage.Kind {
            return when (severity) {
                INFO -> BuildMessage.Kind.INFO
                ERROR, EXCEPTION -> BuildMessage.Kind.ERROR
                WARNING -> BuildMessage.Kind.WARNING
                LOGGING -> BuildMessage.Kind.PROGRESS
                else -> throw IllegalArgumentException("Unsupported severity: " + severity)
            }
        }
    }

    override fun buildFinished(context: CompileContext?) {
        statisticsLogger.reportTotal()
    }
}

private val Iterable<BuildTarget<*>>.moduleTargets: Iterable<ModuleBuildTarget>
    get() = filterIsInstance(javaClass<ModuleBuildTarget>())

private fun getLookupTracker(project: JpsProject): LookupTracker {
    var lookupTracker = LookupTracker.DO_NOTHING

    if ("true".equals(System.getProperty("kotlin.jps.tests"), ignoreCase = true)) {
        val testTracker = project.container.getChild(KotlinBuilder.LOOKUP_TRACKER)?.data

        if (testTracker != null) {
            lookupTracker = testTracker
        }
    }

    if (IncrementalCompilation.isExperimental()) return LookupTrackerImpl(lookupTracker)

    return lookupTracker
}

private fun getIncrementalCaches(chunk: ModuleChunk, context: CompileContext): Map<ModuleBuildTarget, IncrementalCacheImpl> {
    val dataManager = context.projectDescriptor.dataManager
    val targets = chunk.targets

    val buildRegistry = BuildTargetRegistryImpl(context.projectDescriptor.model)
    val outputIndex = TargetOutputIndexImpl(targets, context)

    val allTargets = buildRegistry.allTargets.moduleTargets
    val allDependencies = allTargets.keysToMap { target ->
        target.computeDependencies(buildRegistry, outputIndex).moduleTargets
    }

    val dependents = targets.keysToMap { hashSetOf<ModuleBuildTarget>() }
    val targetsWithDependents = HashSet<ModuleBuildTarget>(targets)

    for ((target, dependencies) in allDependencies) {
        for (dependency in dependencies) {
            if (dependency !in targets) continue

            dependents[dependency]!!.add(target)
            targetsWithDependents.add(target)
        }
    }

    val caches = targetsWithDependents.keysToMap { dataManager.getKotlinCache(it) }

    for ((target, cache) in caches) {
        dependents[target]?.forEach {
            cache.addDependentCache(caches[it]!!)
        }
    }

    return caches
}

// TODO: investigate thread safety
private val ALL_COMPILED_FILES_KEY = Key.create<MutableSet<File>>("_all_kotlin_compiled_files_")
private fun getAllCompiledFilesContainer(context: CompileContext): MutableSet<File> {
    var allCompiledFiles = ALL_COMPILED_FILES_KEY.get(context)
    if (allCompiledFiles == null) {
        allCompiledFiles = THashSet(FileUtil.FILE_HASHING_STRATEGY)
        ALL_COMPILED_FILES_KEY.set(context, allCompiledFiles)
    }
    return allCompiledFiles
}

// TODO: investigate thread safety
private val PROCESSED_TARGETS_WITH_REMOVED_FILES = Key.create<MutableSet<ModuleBuildTarget>>("_processed_targets_with_removed_files_")
private fun getProcessedTargetsWithRemovedFilesContainer(context: CompileContext): MutableSet<ModuleBuildTarget> {
    var set = PROCESSED_TARGETS_WITH_REMOVED_FILES.get(context)
    if (set == null) {
        set = HashSet<ModuleBuildTarget>()
        PROCESSED_TARGETS_WITH_REMOVED_FILES.set(context, set)
    }
    return set
}

private fun hasKotlinDirtyOrRemovedFiles(
        dirtyFilesHolder: DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget>,
        chunk: ModuleChunk
): Boolean {
    if (!KotlinSourceFileCollector.getDirtySourceFiles(dirtyFilesHolder).isEmpty()) {
        return true
    }

    return chunk.getTargets().any { !KotlinSourceFileCollector.getRemovedKotlinFiles(dirtyFilesHolder, it).isEmpty() }
}

public open class GeneratedFile internal constructor(
        val target: ModuleBuildTarget,
        val sourceFiles: Collection<File>,
        val outputFile: File
)

class GeneratedJvmClass (
        target: ModuleBuildTarget,
        sourceFiles: Collection<File>,
        outputFile: File
) : GeneratedFile(target, sourceFiles, outputFile) {
    val outputClass = LocalFileKotlinClass.create(outputFile).sure {
        "Couldn't load KotlinClass from $outputFile; it may happen because class doesn't have valid Kotlin annotations"
    }
}
