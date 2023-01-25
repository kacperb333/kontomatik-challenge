package com.kontomatik.lib.pko

import java.nio.file.Files
import java.nio.file.Path

class ManualTestUtils {
  static String readFromFile(String fileName) {
    return Files.readString(Path.of("src/manualTest/resources/", fileName)).trim()
  }
}
