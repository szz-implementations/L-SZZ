package br.com.ufal.easy.control.diff;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Federal University of Alagoas - 2018
 *
 */

public class GNUDiffExecutor {

    private String fileLeft;
    private String fileRight;

    public GNUDiffExecutor(String fileLeft, String fileRight){
        this.fileRight = fileRight;
        this.fileLeft = fileLeft;
    }

    /**
     * Executes the GNUDiff toll to generate diffs between two files (fileBefore) and (fileAfter)
     * @return List<String>
     */
    public List<String> run() {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        String command  = "diff -a -d " + fileLeft + " " + fileRight;

        CommandLine commandLine = CommandLine.parse(command);
        DefaultExecutor defaultExecutor = new DefaultExecutor();
        defaultExecutor.setExitValue(0);

        try {
            defaultExecutor.setStreamHandler(streamHandler);
            defaultExecutor.execute(commandLine);
        } catch (ExecuteException e) {
			//System.err.println("Execution failed.");
		    //e.printStackTrace();
        } catch (IOException e) {
            System.err.println("permission denied.");
            e.printStackTrace();
        }

        return stringToList(outputStream.toString());
    }

    /**
     * Split a string by \n and returns it as a list
     * @param content
     * @return
     */
    private List<String> stringToList(String content) {
        List<String> result = new ArrayList<String>();
        String token[] = content.split("\n");
        Collections.addAll(result, token);

        return result;
    }
}
