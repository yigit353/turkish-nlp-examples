package zemberek.examples.morphology;

import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.SentenceAnalysis;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.normalization.TurkishSentenceNormalizer;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class DisambiguateSentencesMultiple {

    private static String fixSpacingOfCharInText(String c, String text) {
        if (text.contains(c)) {
            for (int idx = text.indexOf(c); idx != -1; idx = text.indexOf(c, idx + 1)) {
                if (idx > 0 && text.length() > 1 && text.charAt(idx - 1) != ' ') {
                    StringBuilder str = new StringBuilder(text);
                    str.insert(idx - 1, ' ');
                    idx++;
                    text = str.toString();
                }
                if (text.length() > idx + 1 && text.charAt(idx + 1) != ' ') {
                    StringBuilder str = new StringBuilder(text);
                    str.insert(idx + 1, ' ');
                    idx++;
                    text = str.toString();
                }
            }
        }
        return text;
    }

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

            Path zemberekDataRoot = Paths.get("data");
            Path lookupRoot = zemberekDataRoot.resolve("normalization");
            Path lmPath = zemberekDataRoot.resolve("lm/lm.2gram.slm");
            TurkishMorphology morphology = TurkishMorphology.createWithDefaults();
            TurkishSentenceNormalizer normalizer = new
                    TurkishSentenceNormalizer(morphology, lookupRoot, lmPath);

            Scanner sc = new Scanner(fis);
            int numberOfSentences = 0;
            int numberOfUnknownContainingSentences = 0;
            int numberOfWords = 0;
            int numberOfUnknownWords = 0;

            while (sc.hasNextLine()) {
                String sentence = sc.nextLine();
                sentence = sentence.replace('=', ' ');
                sentence = sentence.replace('|', ' ');
                sentence = sentence.replace('^', ' ');
                if (sentence.contains("<") && sentence.contains(">")) {
                    sentence = sentence.replace(">", " ");
                    sentence = sentence.replace("<", " ");
                } else {
                    sentence = sentence.replace(">", " büyüktür ");
                    sentence = sentence.replace("<", " küçüktür ");
                }
                sentence = sentence.strip();

                if (sentence.length() == 0) {
                    if (writeTags) {
                        System.out.println();
                    }
                    continue;
                }
                numberOfSentences++;

                sentence = fixSpacingOfCharInText("-", sentence);
                sentence = fixSpacingOfCharInText("*", sentence);

                String beforeNormalization = sentence;
                try {
                    sentence = normalizer.normalize(sentence);
                } catch (Exception e){
                    sentence = beforeNormalization;
                }

                List<WordAnalysis> analyses = morphology.analyzeSentence(sentence);
                SentenceAnalysis result = morphology.disambiguate(sentence, analyses);
                List<SingleAnalysis> bestAnalysis = result.bestAnalysis();
                numberOfWords += bestAnalysis.size();

                boolean unknownInThisSentence = false;

                for (SingleAnalysis analysis : bestAnalysis) {
                    String lemma = analysis.getLemmas().get(0);
                    boolean unknownResolved = false;
                    String surfaceWord = analysis.surfaceForm();

                    if (writeUnks && lemma.equals("UNK")) {
                        if (Pattern.matches("\\p{IsPunctuation}", surfaceWord)) {
                            if (writeTags) {
                                System.out.print(surfaceWord);
                                System.out.print("+Punc ");
                            }
                            unknownResolved = true;
                        } else if (surfaceWord.equals("x")) {
                            if (writeTags) {
                                System.out.print("*+Punc ");
                            }
                            unknownResolved = true;
                        } else {
                            numberOfUnknownWords++;
                            unknownInThisSentence = true;
                            fw_unk.write(surfaceWord + "\n");
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
                fw_stats.write("Number of Words:\t");
                fw_stats.write(numberOfWords + "");
                fw_stats.write("\n");
                if (writeUnks) {
                    fw_stats.write("Number of Unknown Containing Sentences:\t");
                    fw_stats.write(numberOfUnknownContainingSentences + "");
                    fw_stats.write("\n");
                    fw_stats.write("Rate of Unknown Containing Sentences:\t");
                    fw_stats.write(numberOfUnknownContainingSentences * 100 / (float) numberOfSentences + "");
                    fw_stats.write("\n");
                    fw_stats.write("Number of Unknown Words:\t");
                    fw_stats.write(numberOfUnknownWords + "");
                    fw_stats.write("\n");
                    fw_stats.write("Rate of Unknown Words:\t");
                    fw_stats.write(numberOfUnknownWords * 100 / (float) numberOfWords + "");
                    fw_stats.write("\n");
                }
                fw_stats.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
