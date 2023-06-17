package steam01.model


case class ReleaseDate(
    coming_soon: Boolean,
    date: String
)

case class SteamStore(
    steam_appid: Long,
    release_date: ReleaseDate
)

case class SteamStoreRow(
    key: String,
    value: SteamStore
)
