package com.movizland

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.getQualityFromName
import com.lagradost.cloudstream3.utils.Qualities

open class JWPlayer : ExtractorApi() {
    override val name = "JWPlayer"
    override val mainUrl = "https://www.jwplayer.com"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val sources = mutableListOf<ExtractorLink>()
        with(app.get(url).document) {
            val data = this.select("script").mapNotNull { script ->
                if (script.data().contains("sources: [")) {
                    script.data().substringAfter("sources: [")
                        .substringBefore("],").replace("file", "\"file\"").replace("label", "\"label\"")
                } else {
                    null
                }
            }

            tryParseJson<List<ResponseSource>>("$data")?.map {
                sources.add(
                    ExtractorLink(
                        name,
                        name,
                        it.file,
                        referer = url,
                        quality = getQualityFromName(it.label) ?: Qualities.Unknown.value,
                        isM3u8 = if(it.file.endsWith(".m3u8")) true else false
                        )
                    )
            }
        }
        return sources
    }

    private data class ResponseSource(
        @JsonProperty("file") val file: String,
        @JsonProperty("type") val type: String?,
        @JsonProperty("label") val label: String?
    )

}

class Vadbam : JWPlayer() {
    override val name = "Vadbam"
    override val mainUrl = "https://vadbam.com/"
}
class Vidshar : JWPlayer() {
    override val name = "Vidshar"
    override val mainUrl = "https://vidshar.org/"
}
