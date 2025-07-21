package fr.esgi.al.funprog.tiling

class TilingCalculatorSuite extends munit.FunSuite {

  test("Basic tiling calculations") {
    // Cas simples
    assertEquals(TilingCalculator.calculateTilingCombinations(1, 1), 0L) // Impossible
    assertEquals(TilingCalculator.calculateTilingCombinations(2, 1), 1L) // Une seule façon
    assertEquals(TilingCalculator.calculateTilingCombinations(1, 2), 1L) // Une seule façon
    assertEquals(TilingCalculator.calculateTilingCombinations(2, 2), 2L) // Deux façons
  }

  test("Invalid dimensions") {
    assertEquals(TilingCalculator.calculateTilingCombinations(0, 5), 0L)
    assertEquals(TilingCalculator.calculateTilingCombinations(-1, 3), 0L)
    assertEquals(TilingCalculator.calculateTilingCombinations(3, 3), 0L) // Nombre impair de cases
  }

  test("Larger grids") {
    // Test avec des grilles plus grandes
    val result3x2 = TilingCalculator.calculateTilingCombinations(3, 2)
    assert(result3x2 > 0L, "3x2 grid should have valid tilings")
    
    val result4x2 = TilingCalculator.calculateTilingCombinations(4, 2)
    assert(result4x2 > 0L, "4x2 grid should have valid tilings")
    
    val result2x4 = TilingCalculator.calculateTilingCombinations(2, 4)
    assert(result2x4 > 0L, "2x4 grid should have valid tilings")
  }

  test("Example from specification - 4x4") {
    val result = TilingCalculator.calculateTilingCombinations(4, 4)
    assertEquals(result, 36L) // Résultat attendu selon la spécification
  }

  test("Symmetry property") {
    // Le nombre de pavages doit être le même pour WxH et HxW
    assertEquals(
      TilingCalculator.calculateTilingCombinations(3, 4),
      TilingCalculator.calculateTilingCombinations(4, 3)
    )
    
    assertEquals(
      TilingCalculator.calculateTilingCombinations(2, 6),
      TilingCalculator.calculateTilingCombinations(6, 2)
    )
  }

  test("Display result format") {
    val result = TilingCalculator.displayResult(4, 4)
    assert(result.contains("Possibilités d'affichage pour le panneau 4 x 4"))
    assert(result.contains("36"))
  }

  test("Edge cases") {
    // Grilles où une dimension est 1
    assertEquals(TilingCalculator.calculateTilingCombinations(1, 4), 1L) // Colonne unique, hauteur paire
    assertEquals(TilingCalculator.calculateTilingCombinations(1, 3), 0L) // Colonne unique, hauteur impaire
    assertEquals(TilingCalculator.calculateTilingCombinations(4, 1), 1L) // Ligne unique, largeur paire
    assertEquals(TilingCalculator.calculateTilingCombinations(3, 1), 0L) // Ligne unique, largeur impaire
  }
}
