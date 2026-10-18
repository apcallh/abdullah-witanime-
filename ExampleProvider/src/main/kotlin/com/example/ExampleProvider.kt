
package com.example

import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse

class ExampleProvider : MainAPI() {
    override var mainUrl = "https://example.com/"
    override var name = "Abdullah Anime"
    override val supportedTypes = setOf(TvType.Anime)
    override var lang = "ar"
    override val hasMainPage = true

    override suspend fun search(query: String): List<SearchResponse> {
        return emptyList()
    }
}
