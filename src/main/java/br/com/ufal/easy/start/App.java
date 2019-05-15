package br.com.ufal.easy.start;
import br.com.ufal.easy.control.git.GitUtils;
import br.com.ufal.easy.control.Utils;
import br.com.ufal.easy.control.language.LanguageUtils;
import br.com.ufal.easy.model.*;
import br.com.ufal.easy.control.graph.GraphUtils;
import br.com.ufal.easy.model.commit.CommitFix;
import br.com.ufal.easy.model.commit.CommitReport;
import br.com.ufal.easy.model.diff.ModifiedFile;
import br.com.ufal.easy.model.graph.Vertex;
import br.com.ufal.easy.model.language.Language;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Federal University of Alagoas - 2018
 *
 */

public class App {

    public static void run(String repositoryPath, String bugsInfoPath, String outputPath, int language, int outlier) throws FileNotFoundException {

        //Get all json input files
        List<String> jsonFilePath = Utils.getInstance().getAllJsonFiles(new File(bugsInfoPath));

        int countOfBugsAnalysed = 0;
        try{
            countOfBugsAnalysed = Utils.getInstance().countLines(outputPath)-1;
        } catch (IOException e) {
            // do nothing
        }

        int i = 0;
        if (countOfBugsAnalysed == 0) {
            Utils.getInstance().FileWrite(outputPath, "[");
        }

        for (String json : jsonFilePath) {
            if (i<countOfBugsAnalysed) {
                i++;
                System.out.println("Skipping: "+ bugsInfoPath + json);
                continue;
            }

            System.out.println("Reading: "+ bugsInfoPath + json);
            //Read a json, corresponding to a bug
            Bug bug = Utils.getInstance().readJson(bugsInfoPath + json);

            //Load the fixes and report commit
            ArrayList<CommitFix> _commitFix = bug.getCommitFix();
            CommitReport commitReport = bug.getCommitReport();
            GitUtils.getInstance().clearRepo(repositoryPath);
            //For each fix commit, collect the insertion bug commits
            for (CommitFix commitFix : _commitFix) {
                Set<Vertex> vertexInsertionCommit = new HashSet<>();

                System.out.println("Bug: " + json);
                System.out.println("Fix: " + commitFix.getHash());
                System.out.println("Report: " + commitReport.getHash());

                //Get the files modified to fix the given bug
                List<ModifiedFile> modifiedFile = GitUtils.getInstance().getModifiedFiles(commitFix.getHash(), repositoryPath);

                if(modifiedFile.size() <= outlier) {

                    //For each modified file
                    for (ModifiedFile file : modifiedFile) {
                        System.out.println("Analysing file: " + file.getPath());

                        if(!LanguageUtils.getInstance().isACodeFile(language, file.getPath())){
                            System.out.println("It's not a code file");
                            continue;
                        }

                        Graph<Vertex, DefaultEdge> graph = null;
                        graph = GraphUtils.getInstance().buildAnnotationGraph(commitFix.getHash(), file, repositoryPath);

                        List<Vertex> fixVertex = new ArrayList<Vertex>();

                        boolean isBlockComment = false;

                        for (Vertex vertex : graph.vertexSet()) {
                            if (vertex.getHash().equals(commitFix.getHash()) && vertex.getLabel().equals("CHANGE")) {

                                int lineType = LanguageUtils.getInstance().getCommentType(file.getContent().get(vertex.getLine() - 1), language);

                                if(isBlockComment){
                                    if(lineType == Language.blockComment){
                                        isBlockComment = false;
                                        continue;
                                    }else{
                                        continue;
                                    }
                                }

                                if(lineType == Language.lineComment){
                                    continue;
                                }else if(lineType == Language.blockComment){
                                    isBlockComment = true;
                                    continue;
                                }

                                fixVertex.add(vertex);
                            }
                        }

                        for (Vertex vertex : fixVertex) {
                            vertexInsertionCommit.add(GraphUtils.getInstance().BreadthFirstSearch(graph, vertex));
                        }
                    }

                }

                //Print result into output file
                if (modifiedFile.size() != 0) {
                    Utils.getInstance().FileWrite(outputPath,
                            "{ \"issue_id\": "+json.replace(".json", "") +
                                    ", \"commit_fix\": \""+ commitFix.getHash() + "\", "+
                                    "\"commit_report\": \"" + commitReport.getHash() + "\", " +
                                    "\"bug_introducing_changes\": " + Utils.getInstance().largestVertex(vertexInsertionCommit, repositoryPath)
                                    + " },");
                } else {
                    Utils.getInstance().FileWrite(outputPath,
                            "{ \"issue_id\": "+json.replace(".json", "") +
                                    ", \"commit_fix\": \""+ commitFix.getHash() + "\", "+
                                    "\"commit_report\": \"" + commitReport.getHash() +
                                    "\", \"bug_introducing_changes\": []},");
                }
            }
        }
    }
}
