package fr.esgi.al.funprog.model

/** Actions possibles sur l'intensité d'une LED */
enum IntensityAction {
  case Increment // +
  case Decrement // -
  case Switch    // %
}

object IntensityAction {
  def fromString(action: String): Option[IntensityAction] = action match {
    case "+" => Some(Increment)
    case "-" => Some(Decrement) 
    case "%" => Some(Switch)
    case _   => None
  }
}

/** Représente une instruction pour le panneau LED */
case class Instruction(
  time: Int,
  action: IntensityAction,
  color: Color,
  zone: Zone
) {
  require(time >= 0, "Le temps doit être positif ou nul")
  require(Color.isValid(color), "La couleur doit être valide")
}

/** Cible d'une instruction : position unique ou zone */
enum Target {
  case SinglePosition(position: Position)
  case ZoneTarget(zone: Zone)
  
  def positions: List[Position] = this match {
    case SinglePosition(pos) => List(pos)
    case ZoneTarget(zone) => zone.positions
  }
}

object Target {
  def fromString(target: String): Option[Target] = {
    if (target.contains(" - ")) {
      // Format zone: "0,0 - 9,9"
      target.split(" - ").toList match {
        case List(from, to) =>
          for {
            fromPos <- parsePosition(from.trim)
            toPos <- parsePosition(to.trim)
          } yield ZoneTarget(Zone(fromPos, toPos))
        case _ => None
      }
    } else {
      // Format position unique: "0,0"
      parsePosition(target.trim).map(SinglePosition.apply)
    }
  }
  
  private def parsePosition(pos: String): Option[Position] = {
    pos.split(",").toList match {
      case List(x, y) =>
        try {
          Some(Position(x.trim.toInt, y.trim.toInt))
        } catch {
          case _: NumberFormatException => None
        }
      case _ => None
    }
  }
}
