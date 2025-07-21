package fr.esgi.al.funprog.model

import scala.util.{Success, Failure}

class PanelSuite extends munit.FunSuite {

  test("Panel creation and dimensions") {
    val panel = Panel(5, 3)
    assertEquals(panel.width, 5)
    assertEquals(panel.height, 3)
    assert(panel.leds.isEmpty)
  }

  test("Panel dimensions parsing") {
    Panel.parseDimensions("5 x 3") match {
      case Success(panel) =>
        assertEquals(panel.width, 5)
        assertEquals(panel.height, 3)
      case Failure(_) => fail("Parsing should succeed")
    }
    
    Panel.parseDimensions("invalid") match {
      case Success(_) => fail("Parsing should fail")
      case Failure(_) => // Expected
    }
  }

  test("LED management in panel") {
    val panel = Panel(3, 3)
    val position = Position(1, 1)
    val redLed = Led(Color.RED, 1.0)
    
    // Test setting LED
    panel.setLed(position, redLed) match {
      case Success(newPanel) =>
        assertEquals(newPanel.getLed(position), redLed)
        assertEquals(newPanel.getLed(Position(0, 0)), Led.OFF)
      case Failure(_) => fail("Setting LED should succeed")
    }
    
    // Test invalid position
    panel.setLed(Position(5, 5), redLed) match {
      case Success(_) => fail("Setting LED at invalid position should fail")
      case Failure(_) => // Expected
    }
  }

  test("Panel instruction application") {
    val panel = Panel(3, 3)
    val position = Position(1, 1)
    
    // Test increment action
    panel.applyInstructionAt(position, IntensityAction.Increment, Color.RED) match {
      case Success(newLed) =>
        assertEquals(newLed.color, Color.RED)
        assertEquals(newLed.intensity, 1.0)
      case Failure(_) => fail("Increment should succeed")
    }
    
    // Test invalid color change
    val redPanel = panel.setLed(position, Led(Color.RED, 0.5)).get
    redPanel.applyInstructionAt(position, IntensityAction.Increment, Color.GREEN) match {
      case Success(_) => fail("Color change without turning off should fail")
      case Failure(_) => // Expected
    }
  }

  test("Panel statistics") {
    val panel = Panel(3, 3)
      .setLed(Position(0, 0), Led(Color.RED, 1.0)).get
      .setLed(Position(0, 1), Led(Color.GREEN, 1.0)).get
      .setLed(Position(1, 0), Led(Color.RED, 1.0)).get
      .setLed(Position(1, 1), Led(Color.WHITE, 1.0)).get
    
    val stats = panel.statistics
    assertEquals(stats.totalLitsCount, 4)
    assertEquals(stats.redCount, 2)
    assertEquals(stats.greenCount, 1)
    assertEquals(stats.whiteCount, 1)
    assertEquals(stats.blueCount, 0)
  }

  test("All positions generation") {
    val panel = Panel(2, 3)
    val allPos = panel.allPositions
    val expected = List(
      Position(0, 0), Position(0, 1), Position(0, 2),
      Position(1, 0), Position(1, 1), Position(1, 2)
    )
    assertEquals(allPos.toSet, expected.toSet)
  }
}
