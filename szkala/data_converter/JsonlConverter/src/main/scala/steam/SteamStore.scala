package steam

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class ReleaseDate(coming_soon: Boolean, date: String)

object ReleaseDate {
  implicit val decoder: JsonDecoder[ReleaseDate] = DeriveJsonDecoder.gen[ReleaseDate]
  implicit val encoder: JsonEncoder[ReleaseDate] = DeriveJsonEncoder.gen[ReleaseDate]
}

case class SteamStore(steam_appid: Int, release_date: ReleaseDate)

object SteamStore {
  implicit val decoder: JsonDecoder[SteamStore] = DeriveJsonDecoder.gen[SteamStore]
  implicit val encoder: JsonEncoder[SteamStore] = DeriveJsonEncoder.gen[SteamStore]
}

case class SteamStoreBig(key: String, value: SteamStore)

object SteamStoreBig {
  implicit val decoder: JsonDecoder[SteamStoreBig] = DeriveJsonDecoder.gen[SteamStoreBig]
  implicit val encoder: JsonEncoder[SteamStoreBig] = DeriveJsonEncoder.gen[SteamStoreBig]
}