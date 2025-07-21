package fr.esgi.al.funprog.model

import scala.util.{Try, Success, Failure}

/** Représente un panneau LED */
case class Panel(width: Int, height: Int, leds: Map[Position, Led] = Map.empty) {
  require(width > 0 && height > 0, "Les dimensions du panneau doivent être positives")
  
  /** Obtient l'état d'une LED à une position donnée */
  def getLed(position: Position): Led = 
    leds.getOrElse(position, Led.OFF)
  
  /** Met à jour une LED à une position donnée */
  def setLed(position: Position, led: Led): Try[Panel] = {
    if (!position.isValid(width, height)) {
      Failure(new IllegalArgumentException(s"Position $position invalide pour panneau ${width}x${height}"))
    } else {
      Success(copy(leds = leds.updated(position, led)))
    }
  }
  
  /** Applique une instruction à une position spécifique */
  def applyInstructionAt(position: Position, action: IntensityAction, color: Color): Try[Led] = {
    val currentLed = getLed(position)
    
    // Vérification : pas de changement de couleur et d'intensité simultané avec %
    if (action == IntensityAction.Switch && color != currentLed.color && !currentLed.isOff) {
      Failure(new IllegalArgumentException("Impossible de changer couleur et intensité simultanément avec %"))
    } else if (!currentLed.canChangeColor(color)) {
      Failure(new IllegalArgumentException(s"Impossible de changer la couleur sans éteindre la LED à $position"))
    } else {
      val newIntensity = action match {
        case IntensityAction.Increment => 
          if (currentLed.intensity >= 1.0) currentLed.intensity else 1.0
        case IntensityAction.Decrement => 
          if (currentLed.intensity <= 0.0) currentLed.intensity else 0.0
        case IntensityAction.Switch => 
          if (currentLed.intensity == 0.0) 1.0 else 0.0
      }
      
      val finalColor = if (newIntensity == 0.0) Color.BLACK else color
      Success(Led(finalColor, newIntensity))
    }
  }
  
  /** Statistiques du panneau */
  def statistics: PanelStatistics = {
    val onLeds = leds.values.filter(_.isOn).toList
    val colorCounts = onLeds.groupBy(_.color).view.mapValues(_.size).toMap
    
    PanelStatistics(
      totalLitsCount = onLeds.size,
      redCount = colorCounts.getOrElse(Color.RED, 0),
      greenCount = colorCounts.getOrElse(Color.GREEN, 0),
      blueCount = colorCounts.getOrElse(Color.BLUE, 0),
      whiteCount = colorCounts.getOrElse(Color.WHITE, 0)
    )
  }
  
  /** Toutes les positions valides du panneau */
  def allPositions: List[Position] = 
    (for {
      x <- 0 until width
      y <- 0 until height
    } yield Position(x, y)).toList
}

object Panel {
  def apply(width: Int, height: Int): Panel = Panel(width, height, Map.empty)
  
  /** Parse les dimensions depuis une chaîne "n x m" */
  def parseDimensions(dimensionsStr: String): Try[Panel] = {
    dimensionsStr.trim.split("\\s*x\\s*").toList match {
      case List(widthStr, heightStr) =>
        Try {
          val width = widthStr.toInt
          val height = heightStr.toInt
          Panel(width, height)
        }.recoverWith {
          case _: NumberFormatException => 
            Failure(new IllegalArgumentException(s"Dimensions invalides: $dimensionsStr"))
        }
      case _ => 
        Failure(new IllegalArgumentException(s"Format de dimensions invalide: $dimensionsStr"))
    }
  }
}

/** Statistiques d'un panneau LED */
case class PanelStatistics(
  totalLitsCount: Int,
  redCount: Int,
  greenCount: Int,
  blueCount: Int,
  whiteCount: Int
) {
  def display: String = {
    s"""- allumées: $totalLitsCount
       |- couleurs:
       |  - rouge: $redCount
       |  - vert: $greenCount
       |  - bleu: $blueCount
       |  - blanc: $whiteCount""".stripMargin
  }
}
