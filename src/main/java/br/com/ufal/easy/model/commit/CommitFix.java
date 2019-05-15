package br.com.ufal.easy.model.commit;

/**
 * Federal University of Alagoas - 2018
 *
 */

public class CommitFix {

    private String hash;
    private String nameFix;
    private String userFix;
    private String emailFix;

    public CommitFix() {
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getNameFix() {
        return nameFix;
    }

    public void setNameFix(String nameFix) {
        this.nameFix = nameFix;
    }

    public String getUserFix() {
        return userFix;
    }

    public void setUserFix(String userFix) {
        this.userFix = userFix;
    }

    public String getEmailFix() {
        return emailFix;
    }

    public void setEmailFix(String emailFix) {
        this.emailFix = emailFix;
    }

}
