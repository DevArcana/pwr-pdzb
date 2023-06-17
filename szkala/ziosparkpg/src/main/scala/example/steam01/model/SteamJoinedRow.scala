package steam01.model

case class SteamJoined(
    game_id: Long,
    name: String,
    positive: Long,
    negative: Long,
    owners: String,
    ccu: Long,
    release_date: String
)

case class SteamJoinedRow(
    key: String,
    value: SteamJoined
)