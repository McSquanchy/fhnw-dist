package ch.fhnw.kb.bayes.util;

/**
 * ENUM to simplify training and prediction.
 */
public enum EmailTypes {
    HAM_TRAIN("Ham_Train"),
    HAM_CAL("Ham_Calibration"),
    HAM_TEST("Ham_Test"),
    SPAM_TRAIN("Spam_Train"),
    SPAM_CAL("Spam_Calibration"),
    SPAM_TEST("Spam_Test");

    private String collection;

    public String getCollection() {
        return collection;
    }

    EmailTypes(String col) {
        collection = col;
    }
}
