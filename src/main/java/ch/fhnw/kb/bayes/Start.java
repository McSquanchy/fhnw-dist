package ch.fhnw.kb.bayes;

import ch.fhnw.kb.bayes.util.EmailTypes;
import ch.fhnw.kb.bayes.util.IO;
import ch.fhnw.kb.bayes.util.Utils;

import java.io.IOException;

/**
 * E-mail filter based on naive Bayes classifiers.
 * @version 1.0
 * @author Kevin Buman
 */
public class Start {
    static Spamfilter mp = new Spamfilter();

    public static void main(String[] args) {
        try {
            IO.unpackFiles();
        } catch (IOException e) {
            System.out.println("ERROR UNPACKING FILES");
        }
        System.out.println();
        System.out.println("---------- TRAINING PHASE ----------");

        int hamTestSize = mp.train(EmailTypes.HAM_TRAIN); // training on HAM_TRAIN

        System.out.println("Processed " + String.format("%4s", hamTestSize) + " HAM-Emails.");

        int spamTestSize = mp.train(EmailTypes.SPAM_TRAIN); // training on SPAM_TRAIN

        System.out.println("Processed " + String.format("%4s", spamTestSize) + " SPAM-Emails.\n");
        System.out.println("CORPUS-SIZE: " + mp.CORPUS.size() + " WORDS.\n");

        mp.calculateProbabilites(); // Calculating (w|S) and (w|H)

        System.out.println("---------- CALIBRATION PHASE ----------");
        System.out.println("Processing " + String.format("%4s", IO.setSizes.get(EmailTypes.HAM_CAL)) + " HAM-Emails.");
        System.out.println("Processing " + String.format("%4s", IO.setSizes.get(EmailTypes.SPAM_CAL)) + " SPAM-Emails.");


        System.out.println("Calibrating, this might take a while...\n");

        // You can use either one of these optimizations.
//        mp.tuneSmoothParam();
//        mp.tuneThreshold();
//        mp.tuneParams();

        System.out.println("---------- PREDICTION PHASE ----------");

        int falseClass = mp.predict(EmailTypes.HAM_TEST); // predicting HAM_TEST

        System.out.println("USING THRESHOLD: " + mp.getMATH_THRESHOLD());
        System.out.println("USING SMOOTHING PARAMETER: " + mp.getSMOOTH_PAR() + "\n");

        System.out.println(falseClass + " out of " + IO.setSizes.get(EmailTypes.HAM_TEST) + " HAM e-mails were falsely labelled as spam.");

        System.out.println("ACCURACY: " + Utils.getAccuracy(EmailTypes.HAM_TEST, falseClass) + "\n");

        int properSpam = mp.predict(EmailTypes.SPAM_TEST); // predicting SPAM_TEST

        System.out.println(properSpam+ " out of " + IO.setSizes.get(EmailTypes.SPAM_TEST) + " SPAM e-mails were correctly labelled as spam.");
        System.out.println("ACCURACY: " + Utils.getAccuracy(EmailTypes.SPAM_TEST, properSpam) + "\n");
        System.out.println("OVERALL ACCURACY: " + Utils.getOverallAccuracy(properSpam, falseClass));
    }
}
