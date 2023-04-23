import model.{SteamSpy, SteamSpyRow, SteamStore, SteamStoreRow}
import zio.{Chunk, ZIOAppDefault}
import zio.json._
import zio.stream.{ZPipeline, ZStream}
import zio.*

import java.nio.charset.Charset
import java.nio.file.Path

case class Input(game_id: Int, name: String)
object Input {
  implicit val decoder: JsonDecoder[Input] = DeriveJsonDecoder.gen[Input]
  implicit val encoder: JsonEncoder[Input] = DeriveJsonEncoder.gen[Input]
}

type ApiResult = List[List[Int]]

case class Result(game_id: Int, timestamp: Long, player_count: Long)
object Result {
  implicit val decoder: JsonDecoder[Result] = DeriveJsonDecoder.gen[Result]
  implicit val encoder: JsonEncoder[Result] = DeriveJsonEncoder.gen[Result]
}

object TestMain extends ZIOAppDefault {

  def downloadTimestamps(id: Int) = for {
    res  <- zio.http.Client.request(f"""https://steamcharts.com/app/570/chart-data.json""")
    // res  <- zio.http.Client.request(f"""https://jsonplaceholder.typicode.com/todos/1""")
    data <- res.body.asString
    json  = data.fromJson[ApiResult].getOrElse(List.empty)
  } yield data

  val value    =
    """10	{"game_id":10,"name":"Counter-Strike"}
      |1000000	{"game_id":1000000,"name":"ASCENXION"}
      |1000010	{"game_id":1000010,"name":"Crown Trick"}
      |1000030	{"game_id":1000030,"name":"Cook, Serve, Delicious! 3?!"}
      |1000040	{"game_id":1000040,"name":"细胞战争"}
      |1000080	{"game_id":1000080,"name":"Zengeon"}
      |1000100	{"game_id":1000100,"name":"干支セトラ　陽ノ卷｜干支etc.　陽之卷"}
      |1000110	{"game_id":1000110,"name":"Jumping Master(跳跳大咖)"}
      |1000130	{"game_id":1000130,"name":"Cube Defender"}
      |1000280	{"game_id":1000280,"name":"Tower of Origin2-Worm's Nest"}
      |1000310	{"game_id":1000310,"name":"人气动漫大乱斗"}
      |1000360	{"game_id":1000360,"name":"Hellish Quart"}
      |1000370	{"game_id":1000370,"name":"SurReal Subway"}
      |1000380	{"game_id":1000380,"name":"Rogue Reaper"}
      |1000410	{"game_id":1000410,"name":"WRATH: Aeon of Ruin"}
      |1000460	{"game_id":1000460,"name":"Arsonist"}
      |1000470	{"game_id":1000470,"name":"Drawngeon: Dungeons of Ink and Paper"}
      |1000480	{"game_id":1000480,"name":"Battle Motion"}
      |1000500	{"game_id":1000500,"name":"Elden Gunfire : The Undisputables"}
      |1000510	{"game_id":1000510,"name":"The Marvellous Machine"}
      |1000540	{"game_id":1000540,"name":"Tactical Control"}
      |1000550	{"game_id":1000550,"name":"Love Hentai: Endgame"}
      |1000600	{"game_id":1000600,"name":"The ScreaMaze"}
      |1000650	{"game_id":1000650,"name":"Fist Of Heaven & Hell"}
      |1000750	{"game_id":1000750,"name":"Rotund Rebound"}
      |1000760	{"game_id":1000760,"name":"Foregone"}
      |1000770	{"game_id":1000770,"name":"Urban Tale"}
      |1000790	{"game_id":1000790,"name":"Flex Apocalypse Racing"}
      |1000810	{"game_id":1000810,"name":"RogueCraft Squadron"}
      |1000830	{"game_id":1000830,"name":"Gachi Finder 3000"}
      |1000860	{"game_id":1000860,"name":"Allspace"}
      |1000870	{"game_id":1000870,"name":"On Target VR Darts"}
      |1000880	{"game_id":1000880,"name":"REPTOMOM"}
      |1000900	{"game_id":1000900,"name":"Zero spring episode 3"}
      |1000980	{"game_id":1000980,"name":"Kill Tiger"}
      |1000990	{"game_id":1000990,"name":"Gun Beat"}
      |1001010	{"game_id":1001010,"name":"雪·疑 Sknow"}
      |1001040	{"game_id":1001040,"name":"Glorious Companions"}
      |1001100	{"game_id":1001100,"name":"Kooring VR Wonderland:Mecadino's Attack"}
      |1001140	{"game_id":1001140,"name":"Angry Birds VR: Isle of Pigs"}
      |1001170	{"game_id":1001170,"name":"Master Of Secrets: Dark Europe"}
      |1001200	{"game_id":1001200,"name":"ROWROW"}
      |1001230	{"game_id":1001230,"name":"Partial Control"}
      |1001240	{"game_id":1001240,"name":"VR Flight Simulator New York - Cessna"}
      |1001380	{"game_id":1001380,"name":"SNAILS"}
      |1001390	{"game_id":1001390,"name":"VireFit"}
      |1001430	{"game_id":1001430,"name":"Next Stop 3"}
      |1001450	{"game_id":1001450,"name":"The Town"}
      |1001490	{"game_id":1001490,"name":"Tower Behind the Moon"}
      |1001500	{"game_id":1001500,"name":"Chronicon Apocalyptica"}
      |1001550	{"game_id":1001550,"name":"In Orbit"}
      |1001570	{"game_id":1001570,"name":"GUIDE"}
      |1001590	{"game_id":1001590,"name":"Grab Lab"}
      |1001600	{"game_id":1001600,"name":"Diggerman"}
      |1001640	{"game_id":1001640,"name":"Where the Bees Make Honey"}
      |1001660	{"game_id":1001660,"name":"The Demon Crystal"}
      |1001690	{"game_id":1001690,"name":"Mars Flight VR"}
      |1001730	{"game_id":1001730,"name":"Twisting Mower"}
      |1001740	{"game_id":1001740,"name":"Diamonds"}
      |1001760	{"game_id":1001760,"name":"Xtreme League"}
      |1001800	{"game_id":1001800,"name":"KUNAI"}
      |1001810	{"game_id":1001810,"name":"Virus Expansion"}
      |1001860	{"game_id":1001860,"name":"Casual Desktop Game"}
      |1001870	{"game_id":1001870,"name":"RoboVirus"}
      |1001880	{"game_id":1001880,"name":"aMAZE Valentine"}
      |1001910	{"game_id":1001910,"name":"Fused"}
      |1001950	{"game_id":1001950,"name":"There Was A Dream"}
      |1001960	{"game_id":1001960,"name":"Zibbs - Alien Survival"}
      |1001970	{"game_id":1001970,"name":"Rhythmy"}
      |1001980	{"game_id":1001980,"name":"Archeo: Shinar"}
      |1001990	{"game_id":1001990,"name":"WARED"}
      |1002	{"game_id":1002,"name":"Rag Doll Kung Fu"}
      |1002000	{"game_id":1002000,"name":"SPITLINGS"}
      |1002020	{"game_id":1002020,"name":"INTERPOINT"}
      |1002030	{"game_id":1002030,"name":"Hentai beautiful girls"}
      |1002180	{"game_id":1002180,"name":"AUSSIE BATTLER TANKS CHRISTMAS FUN"}
      |1002200	{"game_id":1002200,"name":"Vasilis"}
      |1002210	{"game_id":1002210,"name":"孤岛(Isolated Island)"}
      |1002230	{"game_id":1002230,"name":"War of Three Kingdoms"}
      |1002270	{"game_id":1002270,"name":"PiiSim"}
      |1002280	{"game_id":1002280,"name":"The Far Kingdoms: Elements"}
      |1002290	{"game_id":1002290,"name":"某1种青春"}
      |1002310	{"game_id":1002310,"name":"The Childs Sight"}
      |1002360	{"game_id":1002360,"name":"バグダス - デバッガー検定 -"}
      |1002410	{"game_id":1002410,"name":"The Five Cores Remastered"}
      |1002420	{"game_id":1002420,"name":"Agartha"}
      |1002430	{"game_id":1002430,"name":"Victory Road"}
      |1002440	{"game_id":1002440,"name":"Fantastic Creatures"}
      |1002480	{"game_id":1002480,"name":"Condors Vs Ocelots"}
      |1002490	{"game_id":1002490,"name":"Roulette Simulator 2"}
      |1002500	{"game_id":1002500,"name":"M.C.I. Escapes"}
      |1002510	{"game_id":1002510,"name":"The Spell - A Kinetic Novel"}
      |1002520	{"game_id":1002520,"name":"Eggys Games Flash Collection"}
      |1002540	{"game_id":1002540,"name":"HA/CK"}
      |1002560	{"game_id":1002560,"name":"Tiny Snow"}
      |1002580	{"game_id":1002580,"name":"Cell Defender"}
      |1002600	{"game_id":1002600,"name":"Temple of Pizza"}
      |1002630	{"game_id":1002630,"name":"AWAKE - Definitive Edition"}
      |1002650	{"game_id":1002650,"name":"Puttin' Around"}
      |1002670	{"game_id":1002670,"name":"SOF - RAIDERS"}
      |1002690	{"game_id":1002690,"name":"Hand of Horzasha"}
      |1002750	{"game_id":1002750,"name":"Ordo Et Chao: New World"}
      |1002800	{"game_id":1002800,"name":"Tsumi"}
      |1002830	{"game_id":1002830,"name":"Latte Stand Tycoon"}
      |1002850	{"game_id":1002850,"name":"古战三国 Ancient War: Three Kingdoms"}
      |1002860	{"game_id":1002860,"name":"Magus Over Fool"}
      |1002890	{"game_id":1002890,"name":"Galaxity"}
      |1002910	{"game_id":1002910,"name":"Twisty Puzzle Simulator"}
      |1002920	{"game_id":1002920,"name":"The Last Dinner"}
      |1002930	{"game_id":1002930,"name":"War of Tanks: Blitzkrieg"}
      |1002950	{"game_id":1002950,"name":"Gravity Panda"}
      |1002960	{"game_id":1002960,"name":"Hentai NetWalk"}
      |1002970	{"game_id":1002970,"name":"Freefall 3050AD"}
      |1002980	{"game_id":1002980,"name":"Symploke: Legend of Gustavo Bueno (Chapter 3)"}
      |1003000	{"game_id":1003000,"name":"Punishment Darkness Online"}
      |1003010	{"game_id":1003010,"name":"Clicker Warriors"}
      |1003020	{"game_id":1003020,"name":"Alluna and Brie"}
      |1003070	{"game_id":1003070,"name":"Groomer"}
      |1003090	{"game_id":1003090,"name":"Through the Darkest of Times"}
      |1003120	{"game_id":1003120,"name":"Hitchhiker - A Mystery Game"}
      |1003150	{"game_id":1003150,"name":"TRANSIT"}
      |1003190	{"game_id":1003190,"name":"Alpacapaca Double Dash"}
      |1003230	{"game_id":1003230,"name":"AIRA VR"}
      |1003320	{"game_id":1003320,"name":"Nya Nya Nya Girls 2 (ʻʻʻ)_(=^･ω･^=)_(ʻʻʻ)"}
      |1003360	{"game_id":1003360,"name":"ШХД: ЗИМА / IT'S WINTER"}
      |1003370	{"game_id":1003370,"name":"Graywalkers: Purgatory"}
      |1003400	{"game_id":1003400,"name":"The Lord of the Rings: Journeys in Middle-earth"}
      |1003450	{"game_id":1003450,"name":"Terrorarium"}
      |1003480	{"game_id":1003480,"name":"Fireboy & Watergirl: Elements"}
      |1003490	{"game_id":1003490,"name":"cat notebook"}
      |1003520	{"game_id":1003520,"name":"Hentai Crush"}
      |1003530	{"game_id":1003530,"name":"学院英雄梦 HeroDreamOfSchool"}
      |1003560	{"game_id":1003560,"name":"Crumbling World"}
      |1003600	{"game_id":1003600,"name":"NashBored"}
      |1003650	{"game_id":1003650,"name":"Abandonment"}
      |1003670	{"game_id":1003670,"name":"Vacation Adventures: Park Ranger"}
      |1003690	{"game_id":1003690,"name":"CrossTrix"}
      |1003730	{"game_id":1003730,"name":"Soko Loco Deluxe"}
      |1003750	{"game_id":1003750,"name":"她2 : 我还想再见到你   Her2 : I Want To See You Again"}
      |1003760	{"game_id":1003760,"name":"Aether Way"}
      |1003830	{"game_id":1003830,"name":"Cold Silence"}
      |1003840	{"game_id":1003840,"name":"ШПМЛ5 (ShPML5)"}
      |1003850	{"game_id":1003850,"name":"Dark Sun Pictures' Dark Sun - The Space Shooter"}
      |1003860	{"game_id":1003860,"name":"Gravity Ace"}
      |1003880	{"game_id":1003880,"name":"Sky of Destruction"}
      |1003890	{"game_id":1003890,"name":"Blacksad: Under the Skin"}
      |1003960	{"game_id":1003960,"name":"De Profundis"}
      |100400	{"game_id":100400,"name":"Silo 2"}
      |1004000	{"game_id":1004000,"name":"Card Brawl"}
      |1004030	{"game_id":1004030,"name":"Rhythm Overdrive"}
      |1004050	{"game_id":1004050,"name":"time of the zombies"}
      |1004100	{"game_id":1004100,"name":"Chaos Caves"}
      |1004150	{"game_id":1004150,"name":"Madorica Real Estate"}
      |1004170	{"game_id":1004170,"name":"The Truck Game"}
      |1004180	{"game_id":1004180,"name":"Boundary VR"}
      |1004190	{"game_id":1004190,"name":"Flowers in Dark"}
      |1004200	{"game_id":1004200,"name":"Biathlon Battle VR"}
      |1004210	{"game_id":1004210,"name":"Pandamonia 潘德莫尼亚"}
      |1004230	{"game_id":1004230,"name":"The Revenge of Johnny Bonasera: Episode 3"}
      |1004240	{"game_id":1004240,"name":"Hentai Girl Karen"}
      |1004270	{"game_id":1004270,"name":"My Island"}
      |1004310	{"game_id":1004310,"name":"Maze 3D"}
      |1004320	{"game_id":1004320,"name":"Sick Love - An RPG Maker Novel"}
      |1004330	{"game_id":1004330,"name":"My Exercise"}
      |1004350	{"game_id":1004350,"name":"Area Cooperation Economic Simulation: North Korea (ACES)"}
      |1004490	{"game_id":1004490,"name":"Tools Up!"}
      |1004500	{"game_id":1004500,"name":"The Chronicles of Jonah and the Whale"}
      |1004510	{"game_id":1004510,"name":"DownStream: VR Whitewater Kayaking"}
      |1004550	{"game_id":1004550,"name":"Stickman Racer Road Draw 2"}
      |1004560	{"game_id":1004560,"name":"Everpath"}
      |1004570	{"game_id":1004570,"name":"blocks"}
      |1004600	{"game_id":1004600,"name":"Find-Life EP1"}
      |1004610	{"game_id":1004610,"name":"Roombo: First Blood"}
      |1004620	{"game_id":1004620,"name":"KungFu Kickball"}
      |1004650	{"game_id":1004650,"name":"Unlock Me"}
      |1004710	{"game_id":1004710,"name":"POCKET CAR : VRGROUND"}
      |1004740	{"game_id":1004740,"name":"iDancer"}
      |1004750	{"game_id":1004750,"name":"WRC 8 FIA World Rally Championship"}
      |1004770	{"game_id":1004770,"name":"Maiden and Spell"}
      |1004780	{"game_id":1004780,"name":"Noel's Hope"}
      |1004790	{"game_id":1004790,"name":"PsyOps Solutions"}
      |1005000	{"game_id":1005000,"name":"Argonauts Agency: Golden
      |""".stripMargin
  val jsons    = value.toString.split("\n").map(x => x.dropWhile(!_.isWhitespace)).toList
  val mapped   = jsons.flatMap(x => Input.decoder.decodeJson(x).toOption).take(5)
  val workflow = ZIO.foreach(mapped)(x => downloadTimestamps(x.game_id).map(res => (x, res)))
  val result   = runZIO(workflow.provide(zio.http.Client.default))

  override def run = for {
    res <- workflow.provide(zio.http.Client.default)
    _   <- Console.printLine(res)
    /*    res <- mapped.runCollect
    _   <- ZIO.foreach(res)(row => Console.printLine(row))

    res2 <- mapped2.runCollect
    _    <- ZIO.foreach(res2)(row => Console.printLine(row))*/
  } yield ()
}
