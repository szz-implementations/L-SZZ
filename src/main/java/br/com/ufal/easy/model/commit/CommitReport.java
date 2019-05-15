package br.com.ufal.easy.model.commit;

/**
 * Federal University of Alagoas - 2018
 *
 */

public class CommitReport {
    private String hash;
    private String nameReport;
    private String userReport;
    private String emailReport;

    public CommitReport() {
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getNameReport() {
        return nameReport;
    }

    public void setNameReport(String nameReport) {
        this.nameReport = nameReport;
    }

    public String getUserReport() {
        return userReport;
    }

    public void setUserReport(String userReport) {
        this.userReport = userReport;
    }

    public String getEmailReport() {
        return emailReport;
    }

    public void setEmailReport(String emailReport) {
        this.emailReport = emailReport;
    }
}
