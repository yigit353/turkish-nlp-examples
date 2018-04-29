package zemberek.examples.morphology;

import zemberek.core.logging.Log;
import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.morphology.analysis.WordAnalysis;

import java.io.IOException;

public class StemmingAndLemmatization {

  public static void main(String[] args) throws IOException {
    TurkishMorphology morphology = TurkishMorphology.createWithDefaults();

    String word = "kitabımızsa";

    Log.info("Word = " + word);

    Log.info("Results: ");
    WordAnalysis results = morphology.analyze(word);
    for (SingleAnalysis result : results) {
      Log.info(result.formatLong());
      Log.info("\tStems = " + result.getStems());
      Log.info("\tLemmas = " + result.getLemmas());
    }
  }

}
