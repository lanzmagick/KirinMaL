package com.axiel7.moelist.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserStats(
    val anime: AnimeStats? = null,
    val manga: MangaStats? = null,
) {

    @Serializable
    data class AnimeStats(
        @SerialName("days_watched")
        override val days: Float? = null,
        @SerialName("mean_score")
        override val meanScore: Float? = null,
        @SerialName("watching")
        override val current: Int = 0,
        @SerialName("completed")
        override val completed: Int = 0,
        @SerialName("on_hold")
        override val onHold: Int = 0,
        @SerialName("dropped")
        override val dropped: Int = 0,
        @SerialName("plan_to_watch")
        override val planned: Int = 0,
        @SerialName("total_entries")
        override val totalEntries: Int = 0,
        @SerialName("rewatched")
        override val repeat: Int = 0,
        @SerialName("episodes_watched")
        val episodesWatched: Int = 0,
    ) : MediaStats

    @Serializable
    data class MangaStats(
        @SerialName("days_read")
        override val days: Float? = null,
        @SerialName("mean_score")
        override val meanScore: Float? = null,
        @SerialName("reading")
        override val current: Int = 0,
        @SerialName("completed")
        override val completed: Int = 0,
        @SerialName("on_hold")
        override val onHold: Int = 0,
        @SerialName("dropped")
        override val dropped: Int = 0,
        @SerialName("plan_to_read")
        override val planned: Int = 0,
        @SerialName("total_entries")
        override val totalEntries: Int = 0,
        @SerialName("reread")
        override val repeat: Int = 0,
        @SerialName("chapters_read")
        val chaptersRead: Int = 0,
        @SerialName("volumes_read")
        val volumesRead: Int = 0,
    ) : MediaStats

    interface MediaStats {
        val days: Float?
        val meanScore: Float?
        val current: Int
        val completed: Int
        val onHold: Int
        val dropped: Int
        val planned: Int
        val totalEntries: Int
        val repeat: Int
    }
}
