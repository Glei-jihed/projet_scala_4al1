package fr.esgi.al.funprog.model

/** Représente une couleur LED avec les composantes RGB */
case class Color(red: Int, green: Int, blue: Int) {
  require(red == 0 || red == 1, "La composante rouge doit être 0 ou 1")
  require(green == 0 || green == 1, "La composante verte doit être 0 ou 1")
  require(blue == 0 || blue == 1, "La composante bleue doit être 0 ou 1")
  
  def isOff: Boolean = red == 0 && green == 0 && blue == 0
}

object Color {
  val BLACK: Color = Color(0, 0, 0)
  val RED: Color = Color(1, 0, 0)
  val GREEN: Color = Color(0, 1, 0)
  val BLUE: Color = Color(0, 0, 1)
  val WHITE: Color = Color(1, 1, 1)
  
  val validColors: Set[Color] = Set(BLACK, RED, GREEN, BLUE, WHITE)
  
  def isValid(color: Color): Boolean = validColors.contains(color)
}

/** Représente l'état d'une LED */
case class Led(color: Color, intensity: Double) {
  require(intensity >= 0.0 && intensity <= 1.0, "L'intensité doit être entre 0 et 1")
  
  def isOn: Boolean = !color.isOff && intensity > 0.0
  def isOff: Boolean = !isOn
  
  def canChangeColor(newColor: Color): Boolean = 
    if (newColor == color) true
    else intensity == 0.0
}

object Led {
  val OFF: Led = Led(Color.BLACK, 0.0)
  
  def apply(color: Color): Led = Led(color, if (color.isOff) 0.0 else 1.0)
}

/** Représente une position sur le panneau */
case class Position(x: Int, y: Int) {
  def isValid(maxX: Int, maxY: Int): Boolean = 
    x >= 0 && x < maxX && y >= 0 && y < maxY
}

/** Représente une zone rectangulaire */
case class Zone(from: Position, to: Position) {
  def positions: List[Position] = {
    val minX = math.min(from.x, to.x)
    val maxX = math.max(from.x, to.x)
    val minY = math.min(from.y, to.y)
    val maxY = math.max(from.y, to.y)
    
    (for {
      x <- minX to maxX
      y <- minY to maxY
    } yield Position(x, y)).toList
  }
  
  def isValid(panelWidth: Int, panelHeight: Int): Boolean =
    from.isValid(panelWidth, panelHeight) && to.isValid(panelWidth, panelHeight)
}
