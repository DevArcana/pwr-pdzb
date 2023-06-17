package steam01.model

case class SteamSpy(
    appid: Long,
    name: String,
    developer: String,
    publisher: String,
    score_rank: String,
    positive: Long,
    negative: Long,
    userscore: Long,
    owners: String,
    average_forever: Long,
    average_2weeks: Long,
    median_forever: Long,
    median_2weeks: Long,
    price: Option[String],
    initialprice: Option[String],
    discount: Option[String],
    ccu: Long
) 

case class SteamSpyRow(
    key: String,
    value: SteamSpy
)