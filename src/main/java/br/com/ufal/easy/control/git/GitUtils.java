package br.com.ufal.easy.control.git;

import br.com.ufal.easy.control.Utils;
import br.com.ufal.easy.model.diff.ModifiedFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
* Federal University of Alagoas - 2018
* This class contains util methods to manipulate Git repositories
*/

public class GitUtils {

    //Singleton
    private static GitUtils instance = null;

    private GitUtils() {
    }

    public static GitUtils getInstance() {
        if (instance == null) {
            instance = new GitUtils();
        }
        return instance;
    }

    /**
     * Returns a Calendar object containing date and time from a commit
     * @param hash
     * @param repositoryPath
     * @return Calendar
     */
    public Calendar getDateTime(String hash, String repositoryPath) {
        String command = "git show " + hash + " -s --date=iso --format=\"%cd\"";
        String output = null;

        try {
            Process process = Runtime.getRuntime().exec(command, null, new File(repositoryPath));
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));

            output = input.readLine();
            String tokens[] = output.replace("\"", "").split(" ");

            output = tokens[0] + " " + tokens[1];
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            cal.setTime(sdf.parse(output));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cal;
    }

    /**
     * Returns a String containing a commit where a file was inserted
     * @param filePath
     * @param repository
     * @return String
     */
    public String getFileInsertionCommit(String hash, String filePath, String repository) {
        //Caution --file-renames
        String command = "git log --follow --diff-filter=A --find-renames=90% --pretty=%H "+ hash + " -- "+ filePath;
        String result = "";

        String output;
        try {
            Process process = Runtime.getRuntime().exec(command, null, new File(repository));
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((output = input.readLine()) != null) {
                result += output;
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
     * Returns a List<String> containing all commits between two given commits who touch a file
     * @param hashOne
     * @param hashTwo
     * @param repository
     * @return List<String>
     */
    public List<String> getCommitsTouchFile(String hashOne, String hashTwo, String filePath, String repository) {
        List<String> results = new ArrayList<>();

        String command = "git rev-list "+ hashOne +"^.."+ hashTwo +" -- "+ filePath;
        String output;
        try {
            Process process = Runtime.getRuntime().exec(command, null, new File(repository));
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((output = input.readLine()) != null) {
                results.add(output);
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.reverse(results);
        return results;
    }

    /**
     * Returns a List<ModifiedFile> containing all modified files in a commit
     * @param hash
     * @param repository
     * @return List<ModifiedFile>
     */
    public List<ModifiedFile> getModifiedFiles(String hash, String repository) {
        ArrayList<ModifiedFile> results = new ArrayList<>();
        String commitHead = Utils.getInstance().executeCommand("git rev-list HEAD | tail -n 1", repository);

        String command;
        if(hash.equals(commitHead)){
            command = "git diff-tree --no-commit-id --name-only -r " + hash + " HEAD";
        } else {
            command = "git log -m -1 --name-only --pretty=" + "format:"+ " " + hash;
        }

        String output;
        try {
            Process process = Runtime.getRuntime().exec(command, null, new File(repository));
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((output = input.readLine()) != null) {

                ModifiedFile file = new ModifiedFile(hash, output, repository);
                results.add(file);
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    /*
     * Returns the size of a modification in a commit
     * @param hashOne
     * @param hashTwo
     * @param repository
     * @return List<String>
     */
    public int getSizeOfCommit(String hashOne, String repository) {
        int size = 0;

        String command = "git diff "+ hashOne +"^ "+ hashOne + " --shortstat";
        String output = "";
        try {
            Process process = Runtime.getRuntime().exec(command, null, new File(repository));
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((output = input.readLine()) != null) {
                //System.out.println(output);
                String insertion = output.split(",")[1];
                String deletions = output.split(",")[2];

                size = Integer.parseInt(insertion.split(" ")[1]) + Integer.parseInt(deletions.split(" ")[1]);

            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println("Commit: "+ hashOne + " Size: " + size);
        return size;
    }

    /**
     * Returns a String containing a file content in the immediately previous commit
     * @param hash
     * @param filePath
     * @param repositoryPath
     * @return String
     * @throws FileNotFoundException
     */
    public String retrieveFileContentFromCommit(final String hash, final String filePath, final String repositoryPath) throws FileNotFoundException {
        final String lastCommit = Utils.getInstance().executeCommand("--work-tree=" + repositoryPath, "--git-dir=" + repositoryPath + ".git", "rev-parse", "HEAD");
        Utils.getInstance().executeCommand("--work-tree=" + repositoryPath, "--git-dir=" + repositoryPath + ".git", "checkout", hash);
        StringBuilder fileContentBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(repositoryPath + filePath))) {
            String lineContent = null;

            while ((lineContent = br.readLine()) != null) {
                fileContentBuilder.append(lineContent + "\n");
            }

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utils.getInstance().executeCommand("--work-tree=" + repositoryPath, "--git-dir=" + repositoryPath + ".git", "checkout", lastCommit);
        return fileContentBuilder.toString();
    }

    /**
     * Given a commit and a file, returns the content of the file at that commit
     * @param hash
     * @param filePath
     * @param repositoryPath
     * @return
     * @throws FileNotFoundException
     */
    public List<String> retrieveFileContentFromCommitList(final String hash, final String filePath, final String repositoryPath) throws FileNotFoundException {
        final String lastCommit = Utils.getInstance().executeCommand("--work-tree=" + repositoryPath, "--git-dir=" + repositoryPath + ".git", "rev-parse", "HEAD");
        Utils.getInstance().executeCommand("--work-tree=" + repositoryPath, "--git-dir=" + repositoryPath + ".git", "checkout", hash);

        List<String> result = new ArrayList<String>();

        try (BufferedReader br = new BufferedReader(new FileReader(repositoryPath + filePath))) {
            String lineContent = null;

            while ((lineContent = br.readLine()) != null) {
                result.add(lineContent);
            }

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utils.getInstance().executeCommand("--work-tree=" + repositoryPath, "--git-dir=" + repositoryPath + ".git", "checkout", lastCommit);
        return result;
    }

    /**
     * Clear the repository
     * @param repositoryPath
     */
    public void clearRepo(final String repositoryPath) {
        Utils.getInstance().executeCommand("--work-tree=" + repositoryPath, "--git-dir=" + repositoryPath + ".git", "checkout", "origin/master", "-f");
    }
}
