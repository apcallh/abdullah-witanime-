// use an integer for version numbers
version = 3

android {
    namespace = "com.example"
}


cloudstream {
    // All of these properties are optional, you can safely remove them

    description = "مزود Witanime للبحث عن الأنمي العربي وعرض التفاصيل والحلقات وروابط المشاهدة المتاحة."
    authors = listOf("Abdullah")
    language = "ar"

    /**
    * Status int as the following:
    * 0: Down
    * 1: Ok
    * 2: Slow
    * 3: Beta only
    * */
    status = 1 // will be 3 if unspecified

    // List of video source types. Users are able to filter for extensions in a given category.
    // You can find a list of avaliable types here:
    // https://recloudstream.github.io/cloudstream/html/app/com.lagradost.cloudstream3/-tv-type/index.html
    tvTypes = listOf("Anime")
}
