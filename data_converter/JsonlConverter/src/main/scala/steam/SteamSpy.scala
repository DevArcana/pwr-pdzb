package steam

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class ScoreRank(raw: String) {}

object ScoreRank   {
  implicit val decoder: JsonDecoder[ScoreRank] = JsonDecoder[String].orElse(JsonDecoder[Int].map(_.toString)).map(x => ScoreRank(x))
  implicit val encoder: JsonEncoder[ScoreRank] = JsonEncoder[String].contramap(_.raw)
}

case class SteamSpy(
                     appid: Int,
                     name: String,
                     developer: String,
                     publisher: String,
                     score_rank: ScoreRank,
                     positive: Int,
                     negative: Int,
                     userscore: Int,
                     owners: String,
                     average_forever: Int,
                     average_2weeks: Int,
                     median_forever: Int,
                     median_2weeks: Int,
                     price: Option[String],
                     initialprice: Option[String],
                     discount: Option[String],
                     ccu: Int
                   ) {}

object SteamSpy    {
  implicit val decoder: JsonDecoder[SteamSpy] = DeriveJsonDecoder.gen[SteamSpy]
  implicit val encoder: JsonEncoder[SteamSpy] = DeriveJsonEncoder.gen[SteamSpy]
}

case class SteamSpyBig(key: String, value:SteamSpy) {}

object SteamSpyBig    {
  implicit val decoder: JsonDecoder[SteamSpyBig] = DeriveJsonDecoder.gen[SteamSpyBig]
  implicit val encoder: JsonEncoder[SteamSpyBig] = DeriveJsonEncoder.gen[SteamSpyBig]
}