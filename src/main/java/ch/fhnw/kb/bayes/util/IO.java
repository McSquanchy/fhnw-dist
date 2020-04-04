package ch.fhnw.kb.bayes.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class IO {
    public static final Map<EmailTypes, File> sourceFiles = new HashMap<>();
    public static final Map<EmailTypes, File> unpackedFiles = new HashMap<>();
    public static final Map<EmailTypes, Integer> setSizes = new HashMap<>();

    private static final String destDir = "src/main/resources/tmp";

    static {
        sourceFiles.put(EmailTypes.SPAM_TRAIN, new File("src/main/resources/train/spam-train.zip"));
        sourceFiles.put(EmailTypes.SPAM_CAL, new File("src/main/resources/calibrate/spam-calibrate.zip"));
        sourceFiles.put(EmailTypes.SPAM_TEST, new File("src/main/resources/test/spam-test.zip"));
        sourceFiles.put(EmailTypes.HAM_TRAIN, new File("src/main/resources/train/ham-train.zip"));
        sourceFiles.put(EmailTypes.HAM_CAL, new File("src/main/resources/calibrate/ham-calibrate.zip"));
        sourceFiles.put(EmailTypes.HAM_TEST, new File("src/main/resources/test/ham-test.zip"));

        unpackedFiles.put(EmailTypes.SPAM_TRAIN, new File(destDir + File.separator + EmailTypes.SPAM_TRAIN.getCollection()));
        unpackedFiles.put(EmailTypes.SPAM_CAL, new File(destDir + File.separator + EmailTypes.SPAM_CAL.getCollection()));
        unpackedFiles.put(EmailTypes.SPAM_TEST, new File(destDir + File.separator + EmailTypes.SPAM_TEST.getCollection()));
        unpackedFiles.put(EmailTypes.HAM_TRAIN, new File(destDir + File.separator + EmailTypes.HAM_TRAIN.getCollection()));
        unpackedFiles.put(EmailTypes.HAM_CAL, new File(destDir + File.separator + EmailTypes.HAM_CAL.getCollection()));
        unpackedFiles.put(EmailTypes.HAM_TEST, new File(destDir + File.separator + EmailTypes.HAM_TEST.getCollection()));
    }

    /**
     * Unpacks a specific collection.
     * @param collection
     */
    public static void unzipFiles(EmailTypes collection) {
        unzip(collection);
    }

    /**
     * Unpacks all collections.
     */
    public static void unzipFiles() {
        for(EmailTypes key : sourceFiles.keySet()) {
            if(!new File(destDir + File.separator + key).exists()) {
                unzip(key);
            }
        }
    }

    /**
     * Unzips a file on the harddrive and copies its contents to the location given by the map unpackedFiles.
     * @param collection
     */
    private static void unzip(EmailTypes collection) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) {
            dir.mkdirs();
        }
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(sourceFiles.get(collection).getPath());
            ZipInputStream is = new ZipInputStream(fis);
            ZipEntry zipEntry = is.getNextEntry();
            System.out.println("Unzipping set: " + collection.getCollection() + ".");
            while(zipEntry != null){
                String fileName = zipEntry.getName();
                File newFile = new File(unpackedFiles.get(collection) + File.separator + fileName);
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                is.closeEntry();
                zipEntry = is.getNextEntry();
            }
            is.closeEntry();
            is.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads all words in a given e-mail.
     * @param f e-mail to be read.
     * @return Set of distinct words that were found in the e-mail.
     */
    public static Set<String> readEmail(File f) {
        Set<String> mail = new HashSet<>();
        Scanner scanner;
        try {
            scanner = new Scanner(f);
        } catch (IOException e) {
            return mail;
        }
        while(scanner.hasNext()) {
            String extracted = sanitize(scanner.next());
            mail.add(extracted);
        }
        scanner.close();
        return mail;
    }

    /**
     * Cleans the input, i.e. removes non-critical characters as well as some html-specific tags.
     * @param input String read from an email.
     * @return Sanitized string.
     */
    private static String sanitize(String input) {
        input = input.replaceAll("[^A-Za-z0-9_]", "").toLowerCase();
        if (input.matches("[0-9]+") || input.startsWith("href")) {
            return "";
        }
        return input;
    }

    /**
     * Unpacks all the files into their respective folder.
     */
    public static void unpackFiles() throws IOException {
        if(!new File("src/main/resources/tmp").exists() || (Objects.requireNonNull(new File("src/main/resources/tmp").list()).length != IO.sourceFiles.size())) {
            IO.unzipFiles();
        } else if(Objects.requireNonNull(new File("src/main/resources/tmp/ham_Calibration").list()).length != new ZipFile(IO.sourceFiles.get(EmailTypes.HAM_CAL)).size()) {
            IO.unzipFiles(EmailTypes.HAM_CAL);
        } else if(Objects.requireNonNull(new File("src/main/resources/tmp/ham_Test").list()).length !=  new ZipFile(IO.sourceFiles.get(EmailTypes.HAM_TEST)).size()) {
            IO.unzipFiles(EmailTypes.HAM_TEST);
        } else if(Objects.requireNonNull(new File("src/main/resources/tmp/ham_Train").list()).length !=  new ZipFile(IO.sourceFiles.get(EmailTypes.HAM_TRAIN)).size()) {
            IO.unzipFiles(EmailTypes.HAM_TRAIN);
        } else if(Objects.requireNonNull(new File("src/main/resources/tmp/spam_Calibration").list()).length !=  new ZipFile(IO.sourceFiles.get(EmailTypes.SPAM_CAL)).size()) {
            IO.unzipFiles(EmailTypes.SPAM_CAL);
        } else if(Objects.requireNonNull(new File("src/main/resources/tmp/spam_Test").list()).length != new ZipFile(IO.sourceFiles.get(EmailTypes.SPAM_TEST)).size()) {
            IO.unzipFiles(EmailTypes.SPAM_TEST);
        } else if(Objects.requireNonNull(new File("src/main/resources/tmp/spam_Train").list()).length !=  new ZipFile(IO.sourceFiles.get(EmailTypes.SPAM_TRAIN)).size()) {
            IO.unzipFiles(EmailTypes.SPAM_TRAIN);
        } else {
            System.out.println("Files already unpacked - skipping\n");
        }
        calculateSizes();
    }

    /**
     * Calculates the number of files for each dataset.
     */
    private static void calculateSizes() {
        for(EmailTypes type : EmailTypes.values()){
           File f = unpackedFiles.get(type);
           setSizes.put(type, Objects.requireNonNull(f.listFiles()).length);
        }
    }

}
