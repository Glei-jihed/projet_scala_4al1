package fr.esgi.al.funprog.integration

import scala.util.{Success, Failure}
import fr.esgi.al.funprog.parser.InstructionParser
import fr.esgi.al.funprog.simulator.LedPanelSimulator

class IntegrationSuite extends munit.FunSuite {

  test("Complete simulation workflow with example data") {
    val instructionLines = List(
      "5 x 5",
      "1 | + | 1,0,0 | 0,0 - 1,0",
      "1 | + | 0,1,0 | 1,1 - 2,1", 
      "2 | + | 0,0,1 | 2,2 - 3,4",
      "2 | + | 0,1,0 | 4,0 - 4,1",
      "3 | + | 1,1,1 | 4,2 - 4,3",
      "3 | + | 0,0,1 | 4,4",
      "4 | - | 1,0,0 | 1,0",
      "5 | % | 1,0,0 | 1,0",
      "6 | % | 1,0,0 | 1,0",
      "7 | + | 1,1,1 | 1,0"
    )
    
    val result = for {
      parsed <- InstructionParser.parseLines(instructionLines)
      simulator = new LedPanelSimulator(parsed.panel)
      simulationResult <- simulator.simulate(parsed.instructions)
    } yield simulationResult
    
    result match {
      case Success(simulationResult) =>
        val stats = simulationResult.finalPanel.statistics
        
        // Vérifications basiques
        assert(stats.totalLitsCount > 0, "Au moins une LED doit être allumée")
        assert(simulationResult.cumulativeTime >= 0, "Le temps cumulé doit être positif")
        
        // Vérification que le rapport s'affiche correctement
        val report = simulationResult.displayReport
        assert(report.contains("allumées:"))
        assert(report.contains("couleurs:"))
        assert(report.contains("cumul:"))
        
        println("✅ Simulation complète réussie")
        println(report)
        
      case Failure(exception) => 
        fail(s"La simulation complète devrait réussir: ${exception.getMessage}")
    }
  }

  test("Error handling - invalid dimensions") {
    val invalidLines = List("invalid dimensions", "1 | + | 1,0,0 | 0,0")
    
    InstructionParser.parseLines(invalidLines) match {
      case Success(_) => fail("Devrait échouer avec des dimensions invalides")
      case Failure(exception) => 
        assert(exception.getMessage.contains("invalide"))
        println("✅ Gestion d'erreur dimensions invalides : OK")
    }
  }

  test("Error handling - position out of bounds") {
    val outOfBoundsLines = List(
      "3 x 3",
      "1 | + | 1,0,0 | 5,5"  // Position hors limites
    )
    
    InstructionParser.parseLines(outOfBoundsLines) match {
      case Success(_) => fail("Devrait échouer avec position hors limites")
      case Failure(exception) => 
        assert(exception.getMessage.contains("invalide"))
        println("✅ Gestion d'erreur position hors limites : OK")
    }
  }

  test("Error handling - temporal conflicts") {
    val conflictLines = List(
      "3 x 3",
      "1 | + | 1,0,0 | 0,0",
      "1 | + | 0,1,0 | 0,0"  // Même position, même instant
    )
    
    InstructionParser.parseLines(conflictLines) match {
      case Success(_) => fail("Devrait échouer avec conflit temporel")
      case Failure(exception) => 
        assert(exception.getMessage.contains("Conflits temporels"))
        println("✅ Gestion d'erreur conflits temporels : OK")
    }
  }

  test("Error handling - invalid color") {
    val invalidColorLines = List(
      "3 x 3",
      "1 | + | 1,1,0 | 0,0"  // Couleur jaune non autorisée
    )
    
    InstructionParser.parseLines(invalidColorLines) match {
      case Success(_) => fail("Devrait échouer avec couleur invalide")
      case Failure(exception) => 
        assert(exception.getMessage.contains("invalide"))
        println("✅ Gestion d'erreur couleur invalide : OK")
    }
  }

  test("Error handling - invalid action") {
    val invalidActionLines = List(
      "3 x 3",
      "1 | x | 1,0,0 | 0,0"  // Action 'x' invalide
    )
    
    InstructionParser.parseLines(invalidActionLines) match {
      case Success(_) => fail("Devrait échouer avec action invalide")
      case Failure(exception) => 
        assert(exception.getMessage.contains("invalide"))
        println("✅ Gestion d'erreur action invalide : OK")
    }
  }

  test("Stress test - large panel") {
    val largePanel = List(
      "10 x 10",
      "1 | + | 1,0,0 | 0,0 - 9,9",  // Allumer tout le panneau en rouge
      "2 | % | 1,0,0 | 0,0 - 9,9",  // Éteindre tout
      "3 | + | 0,0,1 | 0,0 - 9,9"   // Allumer tout en bleu
    )
    
    val result = for {
      parsed <- InstructionParser.parseLines(largePanel)
      simulator = new LedPanelSimulator(parsed.panel)
      simulationResult <- simulator.simulate(parsed.instructions)
    } yield simulationResult
    
    result match {
      case Success(simulationResult) =>
        val stats = simulationResult.finalPanel.statistics
        assertEquals(stats.totalLitsCount, 100) // 10x10 = 100 LEDs
        assertEquals(stats.blueCount, 100)
        assertEquals(stats.redCount, 0)
        println("✅ Test de stress panneau 10x10 : OK")
        
      case Failure(exception) => 
        fail(s"Le test de stress devrait réussir: ${exception.getMessage}")
    }
  }
}
