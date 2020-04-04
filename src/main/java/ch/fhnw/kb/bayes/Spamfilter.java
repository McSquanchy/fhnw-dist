package ch.fhnw.kb.bayes;

import ch.fhnw.kb.bayes.util.EmailTypes;
import ch.fhnw.kb.bayes.util.IO;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spam filter Class
 * @version 1.0
 * @author Kevin Buman
 */
public class Spamfilter {
    public final Map<String, Word> CORPUS = new HashMap<>();

    private double SMOOTH_PAR = 0.5;
    private double MATH_THRESHOLD = 0.5;
    private final double MATH_STEPSIZE = 0.01;

    public int hamSetSize = 0;
    public int spamSetSize = 0;


    public Spamfilter() {}

    /**
     * Fills the CORPUS based on labelled e-mails (either HAM or SPAM).
     * @param type Dataset to learn from.
     * @return number of e-mails processed.
     */
    public int train(EmailTypes type) {
        for(File email : Objects.requireNonNull(IO.unpackedFiles.get(type).listFiles())) {
            Set<String> words = IO.readEmail(email);
            for(String word : words) {
                    if(CORPUS.containsKey(word)) {
                        CORPUS.get(word).increment(type);
                    } else {
                        Word unknown = new Word(word);
                        unknown.increment(type);
                        CORPUS.put(word, unknown);
                    }
                }
            hamSetSize += type == EmailTypes.HAM_TRAIN ? 1 : 0;
            spamSetSize += type == EmailTypes.SPAM_TRAIN ? 1 : 0;
            }
        return type.equals(EmailTypes.HAM_TRAIN) ? hamSetSize : spamSetSize;
    }

    /**
     * Simple method to calculate the probability (S | W) for each word in the CORPUS.
     */
    public void calculateProbabilites() {
        CORPUS.values().forEach(word -> word.calculateProbability(hamSetSize, spamSetSize, SMOOTH_PAR));
    }

    /**
     * Iterates over the given dataset and counts the number of emails marked as SPAM.
     * @param type Dataset to use.
     * @return number of e-mails labelled as SPAM.
     */
    public int predict(EmailTypes type) {
        int count = 0;
        for(File email : Objects.requireNonNull(IO.unpackedFiles.get(type).listFiles())) {
            if(isSpam(email)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Classifies each e-mail to be either SPAM or HAM according to the THRESHOLD.
     * @param f e-mail.
     * @return true/false if the e-mail's probability of being SPAM exceeds the THRESHOLD.
     */
    public boolean isSpam(File f) {
        final double ps = (double)spamSetSize / (spamSetSize + hamSetSize);
        final double hs = (double)hamSetSize / (spamSetSize + hamSetSize);
        double numerator = 0;
        double denominator = 0;
        HashSet<String> words = (HashSet<String>) IO.readEmail(f);
        words = words.stream().filter(CORPUS::containsKey).collect(Collectors.toCollection(HashSet::new));
        for(String w : words) {
            Word next = CORPUS.get(w);
            numerator += Math.log(next.getProbOfSpam());
            denominator += Math.log(next.getProbOfHam());
        }
        numerator += Math.log(ps);
        denominator += Math.log(hs);
        return numerator > ((1/MATH_THRESHOLD)-1)*denominator;
    }


    public void tuneParams() {
        int spamCalib = predict(EmailTypes.SPAM_CAL);
        int hamCalib = predict(EmailTypes.HAM_CAL);
        double initAccuracy = getCalibrationAccuracy(spamCalib, hamCalib);
        double thresh = MATH_THRESHOLD;
        System.out.println("TUNING PARAMETERS\n");
        double optimalLambda = SMOOTH_PAR;
        while(SMOOTH_PAR >  0.1) {
            calculateProbabilites();
            double newBest = getNewPrediction();
            setThreshold(newBest);
            spamCalib = predict(EmailTypes.SPAM_CAL);
            hamCalib = predict(EmailTypes.HAM_CAL);

                double bestAccuracy = getCalibrationAccuracy(spamCalib, hamCalib);
                if (bestAccuracy > initAccuracy) {
                    initAccuracy = bestAccuracy;
                    optimalLambda = SMOOTH_PAR;
                    thresh = newBest;
                }
            setSMOOTH_PAR(SMOOTH_PAR - (MATH_STEPSIZE * 5));
        }
        setThreshold(thresh);
        setSMOOTH_PAR(optimalLambda);
        calculateProbabilites();
        System.out.println("FOUND OPTIMAL SMOOTHING PARAMETER: " + optimalLambda + "\n");
        System.out.println("FOUND OPTIMAL THRESHOLD PARAMETER: " + MATH_THRESHOLD + "\n");
    }

    /**
     * Calibrates the THRESHOLD.
     */
    public double getNewPrediction() {
        int spamCalib = predict(EmailTypes.SPAM_CAL);
        int hamCalib = predict(EmailTypes.HAM_CAL);
        double initAccuracy = getCalibrationAccuracy(spamCalib, hamCalib);
        double optimalThreshold = 0.5;
        double optimalLambda = SMOOTH_PAR;
        setSMOOTH_PAR(optimalLambda);
        calculateProbabilites();
        while(MATH_THRESHOLD < 0.65) {
            setThreshold(MATH_THRESHOLD+MATH_STEPSIZE);
            spamCalib = predict(EmailTypes.SPAM_CAL);
            hamCalib = predict(EmailTypes.HAM_CAL);
            double bestAccuracy = getCalibrationAccuracy(spamCalib, hamCalib);
            if(bestAccuracy > initAccuracy) {
                initAccuracy = bestAccuracy;
                optimalThreshold = MATH_THRESHOLD;
            }
        }
        setThreshold(0.5);
        return optimalThreshold;
    }

    public void tuneSmoothParam() {
        System.out.println("OPTIMIZING SMOOTHING PARAMETER\n");
        int spamCalib = predict(EmailTypes.SPAM_CAL);
        int hamCalib = predict(EmailTypes.HAM_CAL);
        double initAccuracy = getCalibrationAccuracy(spamCalib, hamCalib);
        double optimalLambda = SMOOTH_PAR;
        while(SMOOTH_PAR > 0.1) {
            setSMOOTH_PAR(SMOOTH_PAR - (MATH_STEPSIZE*5));
            calculateProbabilites();
            spamCalib = predict(EmailTypes.SPAM_CAL);
            hamCalib = predict(EmailTypes.HAM_CAL);
            double bestAccuracy = getCalibrationAccuracy(spamCalib, hamCalib);
            if(bestAccuracy > initAccuracy) {
                initAccuracy = bestAccuracy;
                optimalLambda = SMOOTH_PAR;
            }
        }
        System.out.println("FOUND OPTIMAL SMOOTHING PARAMETER: " + optimalLambda + "\n");
        setSMOOTH_PAR(optimalLambda);
    }

    /**
     * Calibrates the THRESHOLD.
     */
    public void tuneThreshold() {
        int spamCalib = predict(EmailTypes.SPAM_CAL);
        int hamCalib = predict(EmailTypes.HAM_CAL);
        double initAccuracy = getCalibrationAccuracy(spamCalib, hamCalib);
        double optimalThreshold = 0.5;
        double optimalLambda = SMOOTH_PAR;
        setSMOOTH_PAR(optimalLambda);
        calculateProbabilites();
        System.out.println("OPTIMIZING FOR BEST OVERALL ACCURACY\n");
        while(MATH_THRESHOLD < 0.65) {
            setThreshold(MATH_THRESHOLD+MATH_STEPSIZE);
            spamCalib = predict(EmailTypes.SPAM_CAL);
            hamCalib = predict(EmailTypes.HAM_CAL);
            double bestAccuracy = getCalibrationAccuracy(spamCalib, hamCalib);
            if(bestAccuracy > initAccuracy) {
                initAccuracy = bestAccuracy;
                optimalThreshold = MATH_THRESHOLD;
            }
        }

        setThreshold(0.5);

        System.out.println("FOUND OPTIMAL THRESHOLD: " + optimalThreshold + "\n");
        setThreshold(optimalThreshold);
    }

    /**
     * Calculates the total calibration accuracy over both datasets.
     * @param spamCalib number of e-mails classified as SPAM in the SPAM-Calibration dataset.
     * @param hamCalib number of e-mails classified as SPAM in the HAM-Calibration dataset.
     * @return calibration accuracy.
     */
    private double getCalibrationAccuracy(int spamCalib, int hamCalib) {
        return (spamCalib + (IO.setSizes.get(EmailTypes.HAM_CAL) - hamCalib)) / (double)(IO.setSizes.get(EmailTypes.HAM_CAL) + IO.setSizes.get(EmailTypes.SPAM_CAL) );
    }

    /**
     * Caculates the individual accuracy of either of the calibration datasets.
     * @param type dataset used.
     * @param number number of e-mails classified as SPAM.
     * @return the individual prediction accuracy.
     */
    private double getIndividualAccuray(EmailTypes type, int number) {
        if(type.equals(EmailTypes.SPAM_CAL)) {
            return number / (double)IO.setSizes.get(EmailTypes.SPAM_CAL);
        } else {
            return  (IO.setSizes.get(EmailTypes.HAM_CAL) - number) / (double)(IO.setSizes.get(EmailTypes.HAM_CAL));
        }
    }

    public void setSMOOTH_PAR(double SMOOTH_PAR) {
        this.SMOOTH_PAR = SMOOTH_PAR;
    }

    public void setThreshold(double threshold) {
        MATH_THRESHOLD = threshold;
    }

    public double getSMOOTH_PAR() {
        return SMOOTH_PAR;
    }

    public double getMATH_THRESHOLD() {
        return MATH_THRESHOLD;
    }
}
