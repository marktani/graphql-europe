package views

import java.time.LocalDate

import sangria.macros.derive._
import graphql.customScalars._
import sangria.schema._

case class Speaker(
  name: String,
  photoUrl: Option[String],
  talkTitle: Option[String],
  company: Option[String],
  twitter: Option[String],
  github: Option[String],
  stub: Boolean = false)

object Speaker {
  implicit val graphqlType = deriveObjectType[Unit, Speaker](ExcludeFields("stub"))
}

object SponsorType extends Enumeration {
  val Organiser, Partner, Platinum, Gold, Silver, Bronze = Value

  implicit val graphqlType: EnumType[SponsorType.Value] = deriveEnumType[SponsorType.Value]()
}

case class Sponsor(
  name: String,
  sponsorType: SponsorType.Value,
  url: String,
  logoUrl: String,
  description: Option[String],
  twitter: Option[String],
  github: Option[String])

object Sponsor {
  implicit val graphqlType = deriveObjectType[Unit, Sponsor]()
}

object Edition extends Enumeration {
  val Berlin2017 = Value

  implicit val graphqlType: EnumType[Edition.Value] = deriveEnumType[Edition.Value]()
}

case class Ticket(
  name: String,
  price: String,
  availableUntil: LocalDate,
  availableUntilText: String,
  url: String,
  soldOut: Boolean,
  available: Boolean)

object Ticket {
  implicit val graphqlType = deriveObjectType[Unit, Ticket](
    ExcludeFields("availableUntilText"))
}

case class Address(
  country: String,
  city: String,
  zipCode: String,
  streetName: String,
  houseNumber: String,
  latitude: Double,
  longitude: Double)

object Address {
  implicit val graphqlType = deriveObjectType[Unit, Address]()
}

object DirectionType extends Enumeration {
  val Airport, TrainStation, Car = Value

  implicit val graphqlType: EnumType[DirectionType.Value] = deriveEnumType[DirectionType.Value]()
}

case class Direction(`type`: DirectionType.Value, from: String, description: String)

object Direction {
  implicit val graphqlType = deriveObjectType[Unit, Direction]()
}

case class Venue(
  name: String,
  url: String,
  phone: String,
  directions: List[Direction],
  address: Address)

object Venue {
  val DirectionTypeArg = Argument("type", OptionInputType(DirectionType.graphqlType))

  implicit val graphqlType = deriveObjectType[Unit, Venue](
    ReplaceField("directions", Field("directions", ListType(Direction.graphqlType),
      arguments = DirectionTypeArg :: Nil,
      resolve = c ⇒ c.arg(DirectionTypeArg).fold(c.value.directions)(d ⇒ c.value.directions.filter(_.`type` == d))))
  )
}

case class Conference(
  name: String,
  edition: Edition.Value,
  tagLine: String,
  year: Int,
  venue: Option[Venue],
  dateStart: Option[LocalDate],
  dateEnd: Option[LocalDate],
  speakers: List[Speaker],
  sponsors: List[Sponsor],
  team: List[TeamMember],
  tickets: List[Ticket],
  url: String)

object Conference {
  private val SectionArg = Argument("section", OptionInputType(TeamSection.graphqlType))
  private val SponsorTypeArg = Argument("type", OptionInputType(SponsorType.graphqlType))

  implicit val graphqlType = deriveObjectType[Unit, Conference](
    ReplaceField("speakers", Field("speakers", ListType(Speaker.graphqlType),
      resolve = _.value.speakers.filterNot(_.stub))),
    ReplaceField("team", Field("team", ListType(TeamMember.graphqlType),
      arguments = SectionArg :: Nil,
      resolve = c ⇒ c.withArgs(SectionArg)(_.fold(c.value.team)(s ⇒ c.value.team.filter(_.teamSection == s))))),
    ReplaceField("sponsors", Field("sponsors", ListType(Sponsor.graphqlType),
      arguments = SponsorTypeArg :: Nil,
      resolve = c ⇒ {
        val sponsors = c.value.sponsors.sortBy(_.name)

        c.withArgs(SponsorTypeArg)(_.fold(sponsors)(s ⇒ sponsors.filter(_.sponsorType == s)))
      }))
  )
}

object TeamSection extends Enumeration {
  val Core, SpecialThanks = Value

  implicit val graphqlType: EnumType[TeamSection.Value] = deriveEnumType[TeamSection.Value]()
}

case class TeamMember(
  name: String,
  photoUrl: Option[String],
  teamSection: TeamSection.Value,
  description: Option[String],
  twitter: Option[String],
  github: Option[String])

object TeamMember {
  implicit val graphqlType = deriveObjectType[Unit, TeamMember]()
}