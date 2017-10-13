/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.gradle.incremental.multiproject
import java.io.File

internal class ArtifactDifferenceRegistryImpl(
        @Suppress("UNUSED_PARAMETER") storageFile: File
) : ArtifactDifferenceRegistry {
    override fun get(artifact: File): Iterable<ArtifactDifference>? =
            null

    override fun add(artifact: File, difference: ArtifactDifference) {
    }

    override fun remove(artifact: File) {
    }

    override fun flush(memoryCachesOnly: Boolean) {
    }

    override fun close() {
    }
}