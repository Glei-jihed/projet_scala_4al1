package fr.esgi.al.funprog

import scala.util.{Try, Success, Failure}
import fr.esgi.al.funprog.parser.InstructionParser
import fr.esgi.al.funprog.simulator.LedPanelSimulator
import fr.esgi.al.funprog.tiling.TilingCalculator

@main
def Main(args: String*): Unit = {
  println("=== Simulateur de Panneau LED FunProg ===")
  
  args.toList match {
    case List("gui") =>
      // Mode interface graphique
      runGUIMode()
      
    case List("console", filePath) =>
      // Mode simulation console avec fichier spécifié
      runSimulationMode(filePath)
      
    case List("tiling", widthStr, heightStr) =>
      // Mode pavage (Partie 2)
      runTilingMode(widthStr, heightStr)
      
    case List(filePath) =>
      // Mode simulation (Partie 1) - fichier direct
      runSimulationMode(filePath)
      
    case Nil =>
      // Mode par défaut avec fichier d'exemple
      val defaultFile = "example_input.txt"
      println(s"Aucun argument fourni, utilisation du fichier d'exemple: $defaultFile")
      runSimulationMode(defaultFile)
      
    case _ =>
      printUsage()
  }
}

/** Mode simulation du panneau LED */
private def runSimulationMode(filePath: String): Unit = {
  println(s"Mode simulation - Fichier: $filePath")
  
  val result = for {
    parsed <- InstructionParser.parseFile(filePath)
    simulator = new LedPanelSimulator(parsed.panel)
    result <- simulator.simulate(parsed.instructions)
  } yield result
  
  result match {
    case Success(simulationResult) =>
      println("\n=== Résumé d'activité ===")
      println(simulationResult.displayReport)
      
    case Failure(exception) =>
      println(s"Erreur lors de la simulation: ${exception.getMessage}")
      sys.exit(1)
  }
}

/** Mode calcul de pavage */
private def runTilingMode(widthStr: String, heightStr: String): Unit = {
  Try {
    val width = widthStr.toInt
    val height = heightStr.toInt
    (width, height)
  } match {
    case Success((width, height)) =>
      println(s"Mode pavage - Panneau: ${width}x${height}")
      println(TilingCalculator.displayResult(width, height))
      
    case Failure(_) =>
      println("Erreur: dimensions invalides pour le mode pavage")
      printUsage()
      sys.exit(1)
  }
}

/** Mode interface graphique */
private def runGUIMode(): Unit = {
  println("Lancement de l'interface graphique...")
  try {
    // Créer dynamiquement la classe GUI
    val guiClass = Class.forName("fr.esgi.al.funprog.gui.LedPanelGUI$")
    val guiObject = guiClass.getField("MODULE$").get(null)
    val launchMethod = guiClass.getMethod("launch")
    launchMethod.invoke(guiObject): Unit
  } catch {
    case e: Exception =>
      println(s"Erreur lors du lancement de l'interface graphique: ${e.getMessage}")
      println("L'interface graphique n'est pas disponible. Utilisez le mode console.")
      println("Exemples d'utilisation:")
      println("  - Mode console: scala-cli run . -- console example_input.txt")
      println("  - Mode tiling: scala-cli run . -- tiling example_input.txt 10 8")
      printUsage()
  }
}

/** Affiche l'usage du programme */
private def printUsage(): Unit = {
  println("""
Usage:
  sbt "run gui"                             # Mode interface graphique
  sbt "run <fichier_instructions>"          # Mode simulation console
  sbt "run tiling <largeur> <hauteur>"     # Mode pavage
  sbt run                                  # Mode simulation avec fichier par défaut

Exemples:
  sbt "run gui"
  sbt "run example_input.txt"
  sbt "run /tmp/input.txt"
  sbt "run tiling 4 4"
  """)
}
