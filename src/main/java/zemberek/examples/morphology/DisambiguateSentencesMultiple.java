package zemberek.examples.morphology;

import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.SentenceAnalysis;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.morphology.analysis.WordAnalysis;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class DisambiguateSentencesMultiple {

    public static void main(String[] args) {
        PrintStream ps = null;
        FileWriter fw_unk = null;
        FileWriter fw_stats = null;

        try {
            boolean writeTags = args[1].toLowerCase().equals("true");
            boolean writeUnks = args[2].toLowerCase().equals("true");
            boolean writeStats = args[3].toLowerCase().equals("true");

            if (!writeTags && !writeUnks) {
                return;
            }

            FileInputStream fis = new FileInputStream(args[0]);

            if (writeTags) {
                ps = new PrintStream(args[0] + ".tags");
                System.setOut(ps);
            }

            if (writeUnks) {
                fw_unk = new FileWriter(args[0] + ".unk.txt");
            }

            if (writeStats) {
                fw_stats = new FileWriter(args[0] + ".stats.txt");
            }

            TurkishMorphology morphology = TurkishMorphology.createWithDefaults();
            Scanner sc = new Scanner(fis);
            int numberOfSentences = 0;
            int numberOfUnknownContainingSentences = 0;

            while (sc.hasNextLine()) {
                String sentence = sc.nextLine();

                if (sentence.length() == 0) {
                    if (writeTags) {
                        System.out.println();
                    }
                    continue;
                }

                numberOfSentences++;

                List<WordAnalysis> analyses = morphology.analyzeSentence(sentence);
                SentenceAnalysis result = morphology.disambiguate(sentence, analyses);
                boolean unknownInThisSentence = false;

                for (SingleAnalysis analysis : result.bestAnalysis()) {
                    String lemma = analysis.getLemmas().get(0);
                    boolean unknownResolved = false;

                    if (writeUnks && lemma.equals("UNK")) {
                        String[] punctuation = new String[]{"'", "&", "="};
                        if (analysis.surfaceForm().equals("'")) {
                            if (writeTags) {
                                System.out.print("'+Punc ");
                            }
                            unknownResolved = true;
                        } else {
                            unknownInThisSentence = true;
                            fw_unk.write(analysis.surfaceForm() + "\n");
                        }
                    }

                    if (writeTags && !unknownResolved) {
                        System.out.print(lemma);
                        System.out.print("+");
                        String lexical = analysis.formatLexical();
                        lexical = lexical.replace("→", "+");
                        lexical = lexical.replace("|", "^DB+");
                        lexical = lexical.split(" ")[1];
                        System.out.print(lexical);
                        System.out.print(" ");
                    }
                }

                if (writeTags) {
                    System.out.println();
                }

                if (writeUnks && unknownInThisSentence) {
                    numberOfUnknownContainingSentences++;
                }
            }

            sc.close();     //closes the scanner

            if (writeTags) {
                ps.close();
            }

            if (writeUnks) {
                fw_unk.close();
            }

            if (writeStats) {
                fw_stats.write("***** STATS *****\n");
                fw_stats.write("Number of Sentences:\t");
                fw_stats.write(numberOfSentences + "");
                fw_stats.write("\n");
                if (writeUnks) {
                    fw_stats.write("Number of Unknown Containing Sentences:\t");
                    fw_stats.write(numberOfUnknownContainingSentences + "");
                    fw_stats.write("\n");
                    fw_stats.write("Rate of Unknown Containing Sentences:\t");
                    fw_stats.write(numberOfUnknownContainingSentences * 100 / (float) numberOfSentences + "");
                    fw_stats.write("\n");
                }
                fw_stats.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
