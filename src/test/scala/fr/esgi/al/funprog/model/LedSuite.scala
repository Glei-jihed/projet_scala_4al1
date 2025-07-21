package fr.esgi.al.funprog.model

class LedSuite extends munit.FunSuite {

  test("Color validation") {
    assert(Color.isValid(Color.BLACK))
    assert(Color.isValid(Color.RED))
    assert(Color.isValid(Color.GREEN))
    assert(Color.isValid(Color.BLUE))
    assert(Color.isValid(Color.WHITE))
    
    // Couleur invalide
    assert(!Color.isValid(Color(1, 1, 0))) // jaune non autorisé
  }

  test("Led state validation") {
    val offLed = Led.OFF
    assert(offLed.isOff)
    assert(!offLed.isOn)
    
    val redLed = Led(Color.RED, 0.5)
    assert(redLed.isOn)
    assert(!redLed.isOff)
    
    val redLedOff = Led(Color.RED, 0.0)
    assert(redLedOff.isOff)
  }

  test("Led color change validation") {
    val offLed = Led.OFF
    assert(offLed.canChangeColor(Color.RED))
    assert(offLed.canChangeColor(Color.GREEN))
    
    val redLed = Led(Color.RED, 0.5)
    assert(redLed.canChangeColor(Color.RED)) // même couleur OK
    assert(!redLed.canChangeColor(Color.GREEN)) // changement interdit si allumée
    
    val redLedOff = Led(Color.RED, 0.0)
    assert(redLedOff.canChangeColor(Color.GREEN)) // changement OK si éteinte
  }

  test("Position validation") {
    val pos = Position(2, 3)
    assert(pos.isValid(5, 5))
    assert(!pos.isValid(2, 3)) // position 2,3 invalide pour panneau 2x3
    assert(!pos.isValid(1, 1)) // position 2,3 invalide pour panneau 1x1
  }

  test("Zone positions generation") {
    val zone = Zone(Position(0, 0), Position(1, 1))
    val expectedPositions = List(
      Position(0, 0), Position(0, 1),
      Position(1, 0), Position(1, 1)
    )
    assertEquals(zone.positions.toSet, expectedPositions.toSet)
  }
}
