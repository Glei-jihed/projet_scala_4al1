package fr.esgi.al.funprog.simulator

import scala.util.{Try, Success, Failure}
import fr.esgi.al.funprog.model.*

/** Simulateur du panneau LED */
class LedPanelSimulator(initialPanel: Panel) {
  
  /** Exécute une liste d'instructions et retourne le résultat final */
  def simulate(instructions: List[Instruction]): Try[SimulationResult] = {
    val sortedInstructions = instructions.sortBy(_.time)
    val executionSteps = groupInstructionsByTime(sortedInstructions)
    
    executeSteps(initialPanel, executionSteps, 0, Map.empty)
  }
  
  /** Groupe les instructions par instant d'exécution */
  private def groupInstructionsByTime(instructions: List[Instruction]): List[(Int, List[Instruction])] = {
    instructions
      .groupBy(_.time)
      .toList
      .sortBy(_._1)
  }
  
  /** Exécute les étapes de simulation */
  private def executeSteps(
    panel: Panel, 
    steps: List[(Int, List[Instruction])], 
    currentTime: Int,
    ledHistory: Map[Position, List[LedEvent]]
  ): Try[SimulationResult] = {
    
    steps match {
      case Nil => 
        val cumulativeTime = calculateCumulativeTime(ledHistory, currentTime)
        Success(SimulationResult(panel, cumulativeTime))
        
      case (stepTime, stepInstructions) :: remainingSteps =>
        // Mettre à jour l'historique pour le temps écoulé
        val updatedHistory = updateHistoryForTime(ledHistory, panel, currentTime, stepTime)
        
        // Exécuter les instructions de cette étape
        executeStepInstructions(panel, stepInstructions) match {
          case Success(newPanel) =>
            val newHistory = recordLedChanges(updatedHistory, panel, newPanel)
            executeSteps(newPanel, remainingSteps, stepTime, newHistory)
          case Failure(exception) => Failure(exception)
        }
    }
  }
  
  /** Exécute toutes les instructions d'une étape temporelle */
  private def executeStepInstructions(panel: Panel, instructions: List[Instruction]): Try[Panel] = {
    instructions.foldLeft(Try(panel)) { (panelTry, instruction) =>
      panelTry.flatMap(executeInstruction(_, instruction))
    }
  }
  
  /** Exécute une instruction sur le panneau */
  private def executeInstruction(panel: Panel, instruction: Instruction): Try[Panel] = {
    val positions = instruction.zone.positions
    
    positions.foldLeft(Try(panel)) { (panelTry, position) =>
      panelTry.flatMap { currentPanel =>
        currentPanel.applyInstructionAt(position, instruction.action, instruction.color)
          .flatMap(newLed => currentPanel.setLed(position, newLed))
      }
    }
  }
  
  /** Met à jour l'historique des LEDs pour une période de temps */
  private def updateHistoryForTime(
    history: Map[Position, List[LedEvent]], 
    panel: Panel, 
    fromTime: Int, 
    toTime: Int
  ): Map[Position, List[LedEvent]] = {
    
    if (fromTime >= toTime) history
    else {
      panel.allPositions.foldLeft(history) { (acc, position) =>
        val led = panel.getLed(position)
        if (led.isOn) {
          val currentEvents = acc.getOrElse(position, List.empty)
          val duration = toTime - fromTime
          val newEvent = LedEvent(fromTime, duration, led)
          acc.updated(position, newEvent :: currentEvents)
        } else {
          acc
        }
      }
    }
  }
  
  /** Enregistre les changements d'état des LEDs */
  private def recordLedChanges(
    history: Map[Position, List[LedEvent]], 
    oldPanel: Panel, 
    newPanel: Panel
  ): Map[Position, List[LedEvent]] = {
    
    newPanel.allPositions.foldLeft(history) { (acc, position) =>
      val oldLed = oldPanel.getLed(position)
      val newLed = newPanel.getLed(position)
      
      if (oldLed != newLed) {
        // LED a changé d'état
        acc.updated(position, List.empty) // Reset l'historique pour cette position
      } else {
        acc
      }
    }
  }
  
  /** Calcule le temps cumulé d'allumage */
  private def calculateCumulativeTime(
    history: Map[Position, List[LedEvent]], 
    finalTime: Int
  ): Int = {
    history.values.flatten.map(_.duration).sum + 
    history.keys.map(pos => 
      if (initialPanel.getLed(pos).isOn) finalTime else 0
    ).sum
  }
}

/** Événement d'allumage d'une LED */
case class LedEvent(startTime: Int, duration: Int, led: Led)

/** Résultat d'une simulation */
case class SimulationResult(finalPanel: Panel, cumulativeTime: Int) {
  def displayReport: String = {
    val stats = finalPanel.statistics
    s"""${stats.display}
       |- cumul: $cumulativeTime""".stripMargin
  }
}
