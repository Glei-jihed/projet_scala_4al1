package fr.esgi.al.funprog.gui

import javax.swing._
import java.awt.{BorderLayout, FlowLayout, GridLayout, Dimension, Color}
import java.awt.event.ActionEvent
import javax.swing.filechooser.FileNameExtensionFilter
import scala.util.{Success, Failure}
import java.io.File

import fr.esgi.al.funprog.model.{Panel => LedPanel, Color => LedColor, Position}
import fr.esgi.al.funprog.parser.InstructionParser
import fr.esgi.al.funprog.simulator.LedPanelSimulator

/**
 * Interface graphique simple pour la simulation de panneau LED
 */
class LedPanelGUI extends JFrame("LED Panel Simulator") {
  private var selectedFile: Option[File] = None
  private var currentPanel: Option[LedPanel] = None
  private var ledGrid: Array[Array[JPanel]] = Array.empty
  
  // Couleurs des LEDs
  private val ledColors = Map[LedColor, Color](
    LedColor.BLACK -> Color.BLACK,
    LedColor.RED -> Color.RED,
    LedColor.GREEN -> Color.GREEN,
    LedColor.BLUE -> Color.BLUE,
    LedColor.WHITE -> Color.WHITE
  )
  
  setSize(800, 600)
  setLocationRelativeTo(this.getParent)
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  
  // Composants
  private val fileLabel = new JLabel("Aucun fichier sélectionné")
  private val selectFileButton = new JButton("Sélectionner fichier...")
  private val executeButton = new JButton("Exécuter simulation")
  private val resetButton = new JButton("Reset")
  private val resultArea = new JTextArea(10, 30)
  private val ledDisplay = new JPanel()
  
  executeButton.setEnabled(false)
  resetButton.setEnabled(false)
  resultArea.setEditable(false)
  
  setupLayout()
  setupEventHandlers()
  
  private def setupLayout(): Unit = {
    setLayout(new BorderLayout())
    
    // Panneau de contrôle en haut
    val controlPanel = new JPanel(new FlowLayout())
    controlPanel.add(fileLabel)
    controlPanel.add(selectFileButton)
    controlPanel.add(executeButton)
    controlPanel.add(resetButton)
    
    // Panneau central avec LED display et résultats
    val centerPanel = new JPanel(new BorderLayout())
    
    // Zone LED à gauche
    val ledPanel = new JPanel(new BorderLayout())
    ledPanel.add(new JLabel("Panneau LED:"), BorderLayout.NORTH)
    ledPanel.add(ledDisplay, BorderLayout.CENTER)
    ledPanel.setPreferredSize(new Dimension(400, 400))
    
    // Zone résultats à droite
    val resultPanel = new JPanel(new BorderLayout())
    resultPanel.add(new JLabel("Résultats:"), BorderLayout.NORTH)
    resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER)
    
    centerPanel.add(ledPanel, BorderLayout.WEST)
    centerPanel.add(resultPanel, BorderLayout.CENTER)
    
    add(controlPanel, BorderLayout.NORTH)
    add(centerPanel, BorderLayout.CENTER)
  }
  
  private def setupEventHandlers(): Unit = {
    selectFileButton.addActionListener((_: ActionEvent) => selectFile())
    executeButton.addActionListener((_: ActionEvent) => executeSimulation())
    resetButton.addActionListener((_: ActionEvent) => resetSimulation())
  }
  
  private def selectFile(): Unit = {
    val fileChooser = new JFileChooser()
    fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers texte (*.txt)", "txt"))
    fileChooser.setCurrentDirectory(new File("."))
    
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      selectedFile = Some(fileChooser.getSelectedFile)
      fileLabel.setText(s"Fichier: ${selectedFile.get.getName}")
      executeButton.setEnabled(true)
      resetButton.setEnabled(true)
    }
  }
  
  private def executeSimulation(): Unit = {
    selectedFile match {
      case Some(file) =>
        executeButton.setEnabled(false)
        resultArea.setText("Simulation en cours...\n")
        
        try {
          val parseResult = InstructionParser.parseFile(file.getAbsolutePath)
          
          parseResult match {
            case Success(parsed) =>
              val simulator = new LedPanelSimulator(parsed.panel)
              val simulationResult = simulator.simulate(parsed.instructions)
              
              simulationResult match {
                case Success(simResult) =>
                  currentPanel = Some(simResult.finalPanel)
                  
                  val output = new StringBuilder()
                  output.append("✓ Simulation réussie!\n\n")
                  output.append(s"Instructions: ${parsed.instructions.length}\n")
                  output.append(s"Panneau: ${simResult.finalPanel.width}x${simResult.finalPanel.height}\n")
                  
                  val stats = simResult.finalPanel.statistics
                  output.append(s"LEDs allumées: ${stats.totalLitsCount}\n")
                  output.append(s"LEDs éteintes: ${(simResult.finalPanel.width * simResult.finalPanel.height) - stats.totalLitsCount}\n\n")
                  
                  output.append("État du panneau:\n")
                  output.append(simResult.finalPanel.toString)
                  
                  resultArea.setText(output.toString())
                  updateLedDisplay(simResult.finalPanel)
                  
                case Failure(exception) =>
                  resultArea.setText(s"❌ Erreur simulation: ${exception.getMessage}")
              }
              
            case Failure(exception) =>
              resultArea.setText(s"❌ Erreur parsing: ${exception.getMessage}")
          }
        } catch {
          case e: Exception =>
            resultArea.setText(s"❌ Erreur: ${e.getMessage}")
        } finally {
          executeButton.setEnabled(true)
        }
        
      case None =>
        JOptionPane.showMessageDialog(this, "Sélectionnez d'abord un fichier!")
    }
  }
  
  private def resetSimulation(): Unit = {
    currentPanel = None
    resultArea.setText("")
    ledDisplay.removeAll()
    ledDisplay.revalidate()
    ledDisplay.repaint()
  }
  
  private def updateLedDisplay(panel: LedPanel): Unit = {
    ledDisplay.removeAll()
    
    val gridPanel = new JPanel(new GridLayout(panel.height, panel.width, 1, 1))
    ledGrid = Array.ofDim[JPanel](panel.height, panel.width)
    
    for (y <- 0 until panel.height) {
      for (x <- 0 until panel.width) {
        val ledPanel = new JPanel()
        ledPanel.setPreferredSize(new Dimension(20, 20))
        
        val position = Position(x, y)
        val led = panel.getLed(position)
        
        if (led.isOn) {
          ledPanel.setBackground(ledColors(led.color))
        } else {
          ledPanel.setBackground(Color.GRAY)
        }
        
        ledPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK))
        ledGrid(y)(x) = ledPanel
        gridPanel.add(ledPanel)
      }
    }
    
    ledDisplay.setLayout(new BorderLayout())
    ledDisplay.add(gridPanel, BorderLayout.CENTER)
    ledDisplay.revalidate()
    ledDisplay.repaint()
  }
}

object LedPanelGUI {
  def main(args: Array[String]): Unit = {
    SwingUtilities.invokeLater(() => {
      new LedPanelGUI().setVisible(true)
    })
  }
  
  def launch(): Unit = main(Array.empty)
}
