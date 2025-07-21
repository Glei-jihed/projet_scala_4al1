package fr.esgi.al.funprog.parser

import scala.util.{Success, Failure}
import fr.esgi.al.funprog.model.*

class InstructionParserSuite extends munit.FunSuite {

  test("Parse complete instruction file") {
    val lines = List(
      "5 x 5",
      "1 | + | 1,0,0 | 0,0",
      "1 | + | 0,1,0 | 1,1 - 2,1",
      "2 | - | 1,0,0 | 0,0"
    )
    
    InstructionParser.parseLines(lines) match {
      case Success(parsed) =>
        assertEquals(parsed.panel.width, 5)
        assertEquals(parsed.panel.height, 5)
        assertEquals(parsed.instructions.length, 3)
      case Failure(exception) => fail(s"Parsing should succeed: ${exception.getMessage}")
    }
  }

  test("Parse panel dimensions") {
    Panel.parseDimensions("10 x 8") match {
      case Success(panel) =>
        assertEquals(panel.width, 10)
        assertEquals(panel.height, 8)
      case Failure(_) => fail("Dimension parsing should succeed")
    }
    
    Panel.parseDimensions("invalid dimensions") match {
      case Success(_) => fail("Invalid dimension parsing should fail")
      case Failure(_) => // Expected
    }
  }

  test("Parse instruction components") {
    val panel = Panel(5, 5)
    val instructionLine = "1 | + | 1,0,0 | 0,0 - 1,1"
    
    // This test would require access to private methods or refactoring
    // For now, we test through the public parseLines method
    val lines = List("5 x 5", instructionLine)
    
    InstructionParser.parseLines(lines) match {
      case Success(parsed) =>
        val instruction = parsed.instructions.head
        assertEquals(instruction.time, 1)
        assertEquals(instruction.action, IntensityAction.Increment)
        assertEquals(instruction.color, Color.RED)
        assertEquals(instruction.zone.from, Position(0, 0))
        assertEquals(instruction.zone.to, Position(1, 1))
      case Failure(exception) => fail(s"Instruction parsing should succeed: ${exception.getMessage}")
    }
  }

  test("Detect time conflicts") {
    val lines = List(
      "3 x 3",
      "1 | + | 1,0,0 | 0,0",
      "1 | + | 0,1,0 | 0,0"  // Conflit : même position au même instant
    )
    
    InstructionParser.parseLines(lines) match {
      case Success(_) => fail("Should detect time conflict")
      case Failure(exception) => 
        assert(exception.getMessage.contains("Conflits temporels"))
    }
  }

  test("Validate positions against panel size") {
    val lines = List(
      "3 x 3",
      "1 | + | 1,0,0 | 5,5"  // Position invalide
    )
    
    InstructionParser.parseLines(lines) match {
      case Success(_) => fail("Should detect invalid position")
      case Failure(exception) => 
        assert(exception.getMessage.contains("invalide"))
    }
  }

  test("Parse different target formats") {
    val panel = Panel(5, 5)
    
    // Test single position
    Target.fromString("2,3") match {
      case Some(Target.SinglePosition(Position(2, 3))) => // Success
      case _ => fail("Should parse single position")
    }
    
    // Test zone
    Target.fromString("0,0 - 2,2") match {
      case Some(Target.ZoneTarget(zone)) =>
        assertEquals(zone.from, Position(0, 0))
        assertEquals(zone.to, Position(2, 2))
      case _ => fail("Should parse zone")
    }
    
    // Test invalid format
    Target.fromString("invalid") match {
      case Some(_) => fail("Should not parse invalid format")
      case None => // Expected
    }
  }
}
