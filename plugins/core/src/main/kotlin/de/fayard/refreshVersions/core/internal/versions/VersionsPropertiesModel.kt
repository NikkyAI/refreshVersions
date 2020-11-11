package de.fayard.refreshVersions.core.internal.versions

internal actual data class VersionsPropertiesModel(
    actual val preHeaderContent: String,
    actual val generatedByVersion: String,
    actual val sections: List<Section>,
    actual val pluginResolution: (id: String, String) -> String?
) {
    init {
        if (preHeaderContent.isNotEmpty()) require(preHeaderContent.endsWith('\n'))
        preHeaderContent.lineSequence().forEach { if (it.isNotBlank()) it.mustBeACommentLine() }
    }

    actual sealed class Section {

        actual data class Comment(actual val lines: String) : Section() {
            init {
                lines.lineSequence().forEach { it.mustBeACommentLine() }
            }
        }

        actual data class VersionEntry(
            actual val leadingCommentLines: List<String> = emptyList(),
            actual val key: String,
            actual val currentVersion: String,
            actual val availableUpdates: List<String>,
            actual val trailingCommentLines: List<String> = emptyList()
        ) : Section() {
            actual val metadataLines: List<String> by lazy {
                leadingCommentLines.mapNotNull {
                    it.substringAfter("## ", missingDelimiterValue = "").ifEmpty { null }
                }
            }

            init {
                leadingCommentLines.forEach { it.mustBeACommentLine() }
                trailingCommentLines.forEach {
                    it.mustBeACommentLine()
                    require(it.startsWith("##").not()) {
                        "Double hashtags are reserved for available update comments and metadata " +
                                "(before the version).\n" +
                                "Problematic line: $it"
                    }
                }
            }
        }
    }

    actual companion object {

        /**
         * We use 4 hashtags to simplify parsing as we can have up to 3 contiguous hashtags in the
         * version availability comments
         * (and just 2 are needed for metadata comments and only 1 for user comments).
         */
        const val headerLinesPrefix = "####"
        const val generatedByLineStart = "#### Generated by `./gradlew refreshVersions` version "

        const val availableComment = "# available"

        val versionKeysPrefixes = listOf("plugin", "version")

        fun versionsPropertiesHeader(version: String) = """
            |#### Dependencies and Plugin versions with their available updates.
            |$generatedByLineStart$version
            |####
            |#### Don't manually edit or split the comments that start with four hashtags (####),
            |#### they will be overwritten by refreshVersions.
            |####
            |#### suppress inspection "SpellCheckingInspection" for whole file
            |#### suppress inspection "UnusedProperty" for whole file
            """.trimMargin().also { headerText ->
            assert(headerText.lineSequence().all { it.startsWith(headerLinesPrefix) })
        }

        private fun String.mustBeACommentLine() {
            require(startsWith("#")) { "Expected a comment but found random text: $this" }
        }
    }
}
