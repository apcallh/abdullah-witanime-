
package com.example

import com.lagradost.cloudstream3.AnimeLoadResponse
import com.lagradost.cloudstream3.DubStatus
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.ExtractorLink
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.addDubStatus
import com.lagradost.cloudstream3.addEpisodes
import com.lagradost.cloudstream3.newAnimeLoadResponse
import com.lagradost.cloudstream3.newAnimeSearchResponse
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.nodes.Element

class ExampleProvider : MainAPI() {
    override var mainUrl = "https://ristoanime.me"
    override var name = "Witanime"
    override var lang = "ar"
    override val supportedTypes = setOf(TvType.Anime)
    override val hasMainPage = false

    private val ajaxSearchUrl = "$mainUrl/wp-content/themes/TopAnime/Ajaxt/Searching.php"

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.post(
            ajaxSearchUrl,
            data = mapOf("search" to query),
            headers = mapOf(
                "X-Requested-With" to "XMLHttpRequest",
                "Referer" to "$mainUrl/"
            )
        ).document

        return document.select("li").mapNotNull { item ->
            val titleLink = item.selectFirst(".SearchResultInner h1 a") ?: return@mapNotNull null
            val poster = item.selectFirst(".SearchThumb img")?.absUrl("src")
            val year = item.selectFirst(".SearchResultInner ul a")?.text()?.trim()?.toIntOrNull()
            newAnimeSearchResponse(titleLink.text().trim(), titleLink.absUrl("href")) {
                this.posterUrl = poster
                this.year = year
                addDubStatus(DubStatus.Subbed)
            }
        }.distinctBy { it.url }
    }

    override suspend fun load(url: String): AnimeLoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst(".SinglePost h1.PostTitle")?.text()?.trim()
            ?: document.title().substringBefore("|").trim()
        val poster = document.selectFirst(".SinglePost .Poster img")?.absUrl("src")
        val plot = document.selectFirst(".StoryArea p")?.text()?.trim()
        val year = document.selectFirst(".TaxContent a[href*=/release-year/]")?.text()?.trim()?.toIntOrNull()
        val tags = document.select(".TaxContent a[href*=/genre/]").map { it.text().trim() }.distinct()
        val episodes = document.select(".EpisodesList a").mapNotNull { link ->
            val number = episodeNumber(link) ?: return@mapNotNull null
            Episode(
                data = link.absUrl("href"),
                name = "الحلقة $number",
                season = 1,
                episode = number.toIntOrNull(),
            )
        }.distinctBy { it.data }

        return newAnimeLoadResponse(title, url, TvType.Anime, comingSoonIfNone = false) {
            posterUrl = poster
            this.plot = plot
            this.year = year
            this.tags = tags
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val episodeUrl = data.trimEnd('/')
        val watchUrl = "$episodeUrl/watch/"
        val document = app.get(watchUrl, referer = episodeUrl).document
        val servers = document.select("ul#watch li[data-watch]")
            .mapNotNull { it.attr("data-watch").takeIf(String::isNotBlank) }
        if (servers.isEmpty()) return false

        var loaded = false
        servers.distinct().forEach { server ->
            loaded = loadExtractor(server, watchUrl, subtitleCallback, callback) || loaded
        }
        return loaded
    }

    private fun episodeNumber(element: Element): String? {
        val text = element.text().trim()
        return Regex("الحلقة\\s*([0-9]+(?:[.,][0-9]+)?)")
            .find(text)?.groupValues?.getOrNull(1)?.replace(',', '.')
            ?: Regex("([0-9]+(?:[.,][0-9]+)?)")
                .find(text)?.groupValues?.getOrNull(1)?.replace(',', '.')
    }
}
