package fr.esgi.al.funprog.parser

import better.files._
import scala.util.{Try, Success, Failure}
import fr.esgi.al.funprog.model.*

/** Parser pour les fichiers d'instructions LED */
object InstructionParser {
  
  /** Parse un fichier d'instructions complet */
  def parseFile(filePath: String): Try[ParsedInstructions] = {
    Try {
      val file = File(filePath)
      if (!file.exists) {
        Failure(new IllegalArgumentException(s"Fichier non trouvé: $filePath"))
      } else {
        val lines = file.lines.toList.filter(_.trim.nonEmpty)
        Success(lines)
      }
    }.flatten.flatMap(parseLines)
  }
  
  /** Parse les lignes d'un fichier d'instructions */
  def parseLines(lines: List[String]): Try[ParsedInstructions] = {
    lines match {
      case Nil => 
        Failure(new IllegalArgumentException("Fichier vide"))
      case dimensionsLine :: instructionLines =>
        for {
          panel <- Panel.parseDimensions(dimensionsLine)
          instructions <- parseInstructionLines(instructionLines, panel)
        } yield ParsedInstructions(panel, instructions)
    }
  }
  
  /** Parse les lignes d'instructions */
  private def parseInstructionLines(lines: List[String], panel: Panel): Try[List[Instruction]] = {
    val parsedInstructions = lines.map(parseInstructionLine(_, panel))
    
    // Vérifier s'il y a des erreurs
    parsedInstructions.find(_.isFailure) match {
      case Some(failure) => failure.asInstanceOf[Try[List[Instruction]]]
      case None => 
        val instructions = parsedInstructions.collect { case Success(instr) => instr }
        validateInstructions(instructions)
    }
  }
  
  /** Parse une ligne d'instruction */
  private def parseInstructionLine(line: String, panel: Panel): Try[Instruction] = {
    line.split("\\|").map(_.trim).toList match {
      case List(timeStr, actionStr, colorStr, targetStr) =>
        for {
          time <- parseTime(timeStr)
          action <- parseAction(actionStr)
          color <- parseColor(colorStr)
          target <- parseTarget(targetStr, panel)
          instruction <- createInstruction(time, action, color, target, panel)
        } yield instruction
      case _ => 
        Failure(new IllegalArgumentException(s"Format d'instruction invalide: $line"))
    }
  }
  
  /** Parse le temps */
  private def parseTime(timeStr: String): Try[Int] = {
    Try(timeStr.toInt).recoverWith {
      case _: NumberFormatException => 
        Failure(new IllegalArgumentException(s"Temps invalide: $timeStr"))
    }
  }
  
  /** Parse l'action sur l'intensité */
  private def parseAction(actionStr: String): Try[IntensityAction] = {
    IntensityAction.fromString(actionStr) match {
      case Some(action) => Success(action)
      case None => Failure(new IllegalArgumentException(s"Action invalide: $actionStr"))
    }
  }
  
  /** Parse la couleur */
  private def parseColor(colorStr: String): Try[Color] = {
    colorStr.split(",").toList match {
      case List(rStr, gStr, bStr) =>
        Try {
          val r = rStr.trim.toInt
          val g = gStr.trim.toInt
          val b = bStr.trim.toInt
          val color = Color(r, g, b)
          
          if (!Color.isValid(color)) {
            Failure(new IllegalArgumentException(s"Couleur invalide: $colorStr"))
          } else {
            Success(color)
          }
        }.flatten.recoverWith {
          case _: NumberFormatException => 
            Failure(new IllegalArgumentException(s"Format de couleur invalide: $colorStr"))
        }
      case _ => 
        Failure(new IllegalArgumentException(s"Format de couleur invalide: $colorStr"))
    }
  }
  
  /** Parse la cible (position ou zone) */
  private def parseTarget(targetStr: String, panel: Panel): Try[Target] = {
    Target.fromString(targetStr) match {
      case Some(target) =>
        // Vérifier que toutes les positions sont valides
        val positions = target.positions
        val invalidPositions = positions.filterNot(_.isValid(panel.width, panel.height))
        if (invalidPositions.nonEmpty) {
          Failure(new IllegalArgumentException(s"Positions invalides: ${invalidPositions.mkString(", ")}"))
        } else {
          Success(target)
        }
      case None => 
        Failure(new IllegalArgumentException(s"Format de cible invalide: $targetStr"))
    }
  }
  
  /** Crée une instruction en convertissant la cible en zone */
  private def createInstruction(time: Int, action: IntensityAction, color: Color, target: Target, panel: Panel): Try[Instruction] = {
    val zone = target match {
      case Target.SinglePosition(pos) => Zone(pos, pos)
      case Target.ZoneTarget(zone) => zone
    }
    
    if (!zone.isValid(panel.width, panel.height)) {
      Failure(new IllegalArgumentException(s"Zone invalide pour panneau ${panel.width}x${panel.height}"))
    } else {
      Success(Instruction(time, action, color, zone))
    }
  }
  
  /** Valide les instructions (pas de conflit temporel) */
  private def validateInstructions(instructions: List[Instruction]): Try[List[Instruction]] = {
    val conflicts = findTimeConflicts(instructions)
    if (conflicts.nonEmpty) {
      val conflictMsg = conflicts.map { case (time, positions) =>
        s"Instant $time: ${positions.mkString(", ")}"
      }.mkString("; ")
      Failure(new IllegalArgumentException(s"Conflits temporels détectés: $conflictMsg"))
    } else {
      Success(instructions)
    }
  }
  
  /** Trouve les conflits temporels */
  private def findTimeConflicts(instructions: List[Instruction]): Map[Int, List[Position]] = {
    val timePositions = instructions.flatMap { instr =>
      instr.zone.positions.map(pos => (instr.time, pos))
    }
    
    timePositions
      .groupBy(identity)
      .view
      .mapValues(_.length)
      .filter(_._2 > 1)
      .keys
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._2).toList)
      .toMap
  }
}

/** Résultat du parsing d'un fichier d'instructions */
case class ParsedInstructions(panel: Panel, instructions: List[Instruction])
