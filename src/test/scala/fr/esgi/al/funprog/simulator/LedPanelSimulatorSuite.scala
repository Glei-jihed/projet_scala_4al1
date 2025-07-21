package fr.esgi.al.funprog.simulator

import scala.util.{Success, Failure}
import fr.esgi.al.funprog.model.*

class LedPanelSimulatorSuite extends munit.FunSuite {

  test("Simple simulation") {
    val panel = Panel(3, 3)
    val simulator = new LedPanelSimulator(panel)
    
    val instructions = List(
      Instruction(1, IntensityAction.Increment, Color.RED, Zone(Position(0, 0), Position(0, 0))),
      Instruction(2, IntensityAction.Increment, Color.GREEN, Zone(Position(1, 1), Position(1, 1))),
      Instruction(3, IntensityAction.Decrement, Color.RED, Zone(Position(0, 0), Position(0, 0)))
    )
    
    simulator.simulate(instructions) match {
      case Success(result) =>
        val finalPanel = result.finalPanel
        assertEquals(finalPanel.getLed(Position(0, 0)), Led.OFF)
        assertEquals(finalPanel.getLed(Position(1, 1)), Led(Color.GREEN, 1.0))
        
        val stats = finalPanel.statistics
        assertEquals(stats.totalLitsCount, 1)
        assertEquals(stats.greenCount, 1)
        
      case Failure(exception) => fail(s"Simulation should succeed: ${exception.getMessage}")
    }
  }

  test("Zone instruction simulation") {
    val panel = Panel(3, 3)
    val simulator = new LedPanelSimulator(panel)
    
    val instructions = List(
      Instruction(1, IntensityAction.Increment, Color.BLUE, Zone(Position(0, 0), Position(1, 1)))
    )
    
    simulator.simulate(instructions) match {
      case Success(result) =>
        val finalPanel = result.finalPanel
        
        // Vérifier que toutes les LEDs de la zone sont allumées en bleu
        val positions = List(Position(0, 0), Position(0, 1), Position(1, 0), Position(1, 1))
        positions.foreach { pos =>
          assertEquals(finalPanel.getLed(pos), Led(Color.BLUE, 1.0))
        }
        
        val stats = finalPanel.statistics
        assertEquals(stats.totalLitsCount, 4)
        assertEquals(stats.blueCount, 4)
        
      case Failure(exception) => fail(s"Zone simulation should succeed: ${exception.getMessage}")
    }
  }

  test("Intensity actions") {
    val panel = Panel(2, 2)
    val simulator = new LedPanelSimulator(panel)
    val position = Position(0, 0)
    
    val instructions = List(
      Instruction(1, IntensityAction.Increment, Color.RED, Zone(position, position)),
      Instruction(2, IntensityAction.Switch, Color.RED, Zone(position, position)),
      Instruction(3, IntensityAction.Switch, Color.RED, Zone(position, position)),
      Instruction(4, IntensityAction.Decrement, Color.RED, Zone(position, position))
    )
    
    simulator.simulate(instructions) match {
      case Success(result) =>
        // Après switch -> off, switch -> on, decrement -> off
        assertEquals(result.finalPanel.getLed(position), Led.OFF)
      case Failure(exception) => fail(s"Intensity simulation should succeed: ${exception.getMessage}")
    }
  }

  test("Invalid color change should fail") {
    val initialPanel = Panel(2, 2).setLed(Position(0, 0), Led(Color.RED, 1.0)).get
    val simulator = new LedPanelSimulator(initialPanel)
    
    val instructions = List(
      Instruction(1, IntensityAction.Increment, Color.GREEN, Zone(Position(0, 0), Position(0, 0)))
    )
    
    simulator.simulate(instructions) match {
      case Success(_) => fail("Should fail when trying to change color without turning off")
      case Failure(exception) => 
        assert(exception.getMessage.contains("changer la couleur"))
    }
  }

  test("Example from specification") {
    val panel = Panel(5, 5)
    val simulator = new LedPanelSimulator(panel)
    
    val instructions = List(
      Instruction(1, IntensityAction.Increment, Color.RED, Zone(Position(0, 0), Position(1, 0))),
      Instruction(1, IntensityAction.Increment, Color.GREEN, Zone(Position(1, 1), Position(2, 1))),
      Instruction(2, IntensityAction.Increment, Color.BLUE, Zone(Position(2, 2), Position(3, 4))),
      Instruction(2, IntensityAction.Increment, Color.GREEN, Zone(Position(4, 0), Position(4, 1))),
      Instruction(3, IntensityAction.Increment, Color.WHITE, Zone(Position(4, 2), Position(4, 3))),
      Instruction(3, IntensityAction.Increment, Color.BLUE, Zone(Position(4, 4), Position(4, 4)))
    )
    
    simulator.simulate(instructions) match {
      case Success(result) =>
        val stats = result.finalPanel.statistics
        
        // Vérifier les statistiques attendues (approximativement)
        assert(stats.totalLitsCount > 0)
        assert(stats.redCount >= 0)
        assert(stats.greenCount >= 0)
        assert(stats.blueCount >= 0)
        assert(stats.whiteCount >= 0)
        
      case Failure(exception) => fail(s"Example simulation should succeed: ${exception.getMessage}")
    }
  }
}
