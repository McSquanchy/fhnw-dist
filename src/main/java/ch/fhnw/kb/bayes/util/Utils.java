package ch.fhnw.kb.bayes.util;

/**
 * Helper Methods
 */
public class Utils {

    /**
     * Caculates the individual accuracy of either of the calibration datasets.
     * @param type dataset used.
     * @param rate number of e-mails classified as SPAM.
     * @return the individual prediction accuracy.
     */
    public static String getAccuracy(EmailTypes type, int rate) {
        double accuracy = type.getCollection().contains("Spam") ? (rate / (double) IO.setSizes.get(type)) : 1 - (rate / (double) IO.setSizes.get(type));
        return Utils.getPercentage(accuracy);
    }

    /**
     * Calculates the total calibration accuracy over both datasets.
     * @param properSpam of e-mails classified as SPAM in the SPAM-Calibration dataset.
     * @param falsePositives number of e-mails classified as SPAM in the HAM-Calibration dataset.
     * @return calibration accuracy.
     */
    public static String getOverallAccuracy(int properSpam, int falsePositives) {
        double accuracy = (properSpam+ (IO.setSizes.get(EmailTypes.HAM_TEST) - falsePositives)) /(double)(IO.setSizes.get(EmailTypes.HAM_TEST) + IO.setSizes.get(EmailTypes.SPAM_TEST));
        return Utils.getPercentage(accuracy);
    }

    /**
     * Formats a value to a percentage.
     * @param val value to process.
     * @return percentage string.
     */
    public static String getPercentage(double val) {
        return String.format("%.2f", val*100).concat("%");
    }
}
