import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class EmailAnalyzer {
    private static final String FILE_PATH = "emails.txt";
    private static final String OUTPUT_FILE_PATH = "email_analysis_report.txt";
    private static final String LOG_FILE_PATH = "email_analysis.log";

    public static void main(String[] args) {
        Logger logger = Logger.getLogger("EmailAnalyzer");
        FileHandler fileHandler;

        try {
            fileHandler = new FileHandler(LOG_FILE_PATH);
            logger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error initializing log file handler", e);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH));
             BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE_PATH))) {

            String line;
            int totalEmails = 0;
            Map<String, Integer> domainCounts = new HashMap<>();
            Map<String, Integer> subjectKeywordCounts = new HashMap<>();

            Pattern linePattern = Pattern.compile("\\[(.*?)\\]");
            Pattern emailPattern = Pattern.compile("\\[\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b\\]");
            Pattern subjectPattern = Pattern.compile("\\[\\b([A-Za-z]+)\\b\\]");

            while ((line = reader.readLine()) != null) {
                Matcher lineMatcher = linePattern.matcher(line);
                Matcher emailMatcher = emailPattern.matcher(line);
                Matcher subjectMatcher = subjectPattern.matcher(line);

                if (lineMatcher.find() && emailMatcher.find() && subjectMatcher.find()) {
                    String sender = emailMatcher.group();
                    String recipient = lineMatcher.group(1);
                    String domain = getEmailDomain(recipient);
                    String subjectKeyword = subjectMatcher.group(1);

                    totalEmails++;
                    domainCounts.put(domain, domainCounts.getOrDefault(domain, 0) + 1);
                    subjectKeywordCounts.put(subjectKeyword, subjectKeywordCounts.getOrDefault(subjectKeyword, 0) + 1);
                } else {
                    logger.log(Level.WARNING, "Invalid email format found: " + line);
                }
            }

            writer.write("Email Analysis Report\n");
            writer.write("Total Emails: " + totalEmails + "\n\n");

            writer.write("Domain Counts:\n");
            for (Map.Entry<String, Integer> entry : domainCounts.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
            }
            writer.write("\n");

            writer.write("Subject Keyword Counts:\n");
            for (Map.Entry<String, Integer> entry : subjectKeywordCounts.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
            }

            logger.log(Level.INFO, "Email analysis report generated successfully.");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading or writing file", e);
        }
    }

    private static String getEmailDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        return email.substring(atIndex + 1);
    }
}