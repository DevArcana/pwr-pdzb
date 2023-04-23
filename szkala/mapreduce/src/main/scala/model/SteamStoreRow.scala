package model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class ReleaseDate(
    coming_soon: Boolean,
    date: String
)
object ReleaseDate   {
  implicit val decoder: JsonDecoder[ReleaseDate] = DeriveJsonDecoder.gen[ReleaseDate]
  implicit val encoder: JsonEncoder[ReleaseDate] = DeriveJsonEncoder.gen[ReleaseDate]
}

case class SteamStoreRow(
    steam_appid: String,
    release_date: ReleaseDate
)
object SteamStoreRow {
  implicit val decoder: JsonDecoder[SteamStoreRow] = DeriveJsonDecoder.gen[SteamStoreRow]
  implicit val encoder: JsonEncoder[SteamStoreRow] = DeriveJsonEncoder.gen[SteamStoreRow]
}
