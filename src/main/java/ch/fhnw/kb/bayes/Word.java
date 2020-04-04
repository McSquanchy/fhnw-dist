package ch.fhnw.kb.bayes;

import ch.fhnw.kb.bayes.util.EmailTypes;

public class Word {
    private final String word;
    private double spamCount;
    private double hamCount;
    private double probOfSpam;
    private double probOfHam;


    public Word(String w) {
        word = w;
        hamCount = 0;
        spamCount = 0;
        probOfSpam = 0.0;
    }

    public void increment(EmailTypes type) {
        if (type.equals(EmailTypes.HAM_TRAIN)) {
            hamCount++;
        } else {
            spamCount++;
        }
    }

    public String getWord() {
        return word;
    }

    public void calculateProbability(int hamSize, int spamSize, double alpha) {
        probOfHam =  (hamCount+alpha) / (hamSize+(2*(alpha)));
        probOfSpam = (spamCount+alpha) / (spamSize+(2*(alpha)));
    }

    public double getProbOfHam() {
        return probOfHam;
    }

    public double getProbOfSpam() {
        return probOfSpam;
    }
}
