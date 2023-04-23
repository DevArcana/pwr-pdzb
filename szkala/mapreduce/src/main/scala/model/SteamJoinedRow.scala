package model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class SteamJoined(
    game_id: Int,
    name: String,
    positive: Int,
    negative: Int,
    owners: String,
    ccu: Int,
    release_date: String
)
object SteamJoined {
  implicit val decoder: JsonDecoder[SteamJoined] = DeriveJsonDecoder.gen[SteamJoined]
  implicit val encoder: JsonEncoder[SteamJoined] = DeriveJsonEncoder.gen[SteamJoined]
}

case class SteamJoinedRow(
    key: String,
    value: SteamJoined
)
object SteamJoinedRow {
  implicit val decoder: JsonDecoder[SteamJoinedRow] = DeriveJsonDecoder.gen[SteamJoinedRow]
  implicit val encoder: JsonEncoder[SteamJoinedRow] = DeriveJsonEncoder.gen[SteamJoinedRow]
}
