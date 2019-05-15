package br.com.ufal.easy.control.graph;

import br.com.ufal.easy.control.git.GitUtils;
import br.com.ufal.easy.control.Utils;
import br.com.ufal.easy.model.diff.Hunk;
import br.com.ufal.easy.model.diff.ModifiedFile;
import br.com.ufal.easy.model.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;

public class GraphUtils {
    //Singleton
    private static GraphUtils instance = null;

    private GraphUtils() {
    }

    public static GraphUtils getInstance() {
        if (instance == null) {
            instance = new GraphUtils();
        }
        return instance;
    }

    /**
     * Replace a vertex in a graph
     * @param graph
     * @param vertex
     * @param replace
     * @param <V>
     * @param <E>
     * @return
     */
    public <V, E> Graph<V, E> replaceVertex(Graph<V, E> graph, V vertex, V replace) {
        graph.addVertex(replace);

        List<V> toAdd = new ArrayList<V>();
        for (E edge : graph.outgoingEdgesOf(vertex)) {
            toAdd.add(graph.getEdgeSource(edge));
        }

        for (V auxVertex : toAdd) {
            graph.addEdge(replace, auxVertex);
        }

        graph.removeVertex(vertex);
        return graph;
    }

    /**
     * Executes the breadth first search to indentify the last change in a line
     * @param graph
     * @param start
     * @return
     */
    public Vertex BreadthFirstSearch(Graph graph, Vertex start) {

    Iterator<Vertex> iter = new BreadthFirstIterator<>(graph, start);
        Vertex aux = null;
        while(iter.hasNext()) {
            aux = iter.next();
            //System.out.println(aux.toString());
            if(aux.getLabel().equals("CHANGE") && !aux.getHash().equals(start.getHash())) {
                break;
            }
        }

        return aux;
    }

    /**
     * Build the annotation graph
     * @param hash
     * @param modifiedFile
     * @param repositoryPath
     * @return
     */
    public Graph buildAnnotationGraph(String hash, ModifiedFile modifiedFile, String repositoryPath ) {

        //Get the commit who inserts the file
        String insertionFileCommit = GitUtils.getInstance().getFileInsertionCommit(hash, modifiedFile.getPath(), repositoryPath);
        //Get all commits between the insertion and fix who changed the file
        List<String> commitsTouchFile = GitUtils.getInstance().getCommitsTouchFile(insertionFileCommit, hash, modifiedFile.getPath(),repositoryPath);

        Graph<Vertex, DefaultEdge> graph = new Multigraph<>(DefaultEdge.class);
        try {

            Map<String, Integer> fileSizeAtCommit = new HashMap<>();

            // 1: Create nodes
            //For each commit who change the file
            for (int i = 0; i < commitsTouchFile.size(); i++) {
                List<String> fileLines = null;

                if(!hash.equals(commitsTouchFile.get(i))){
                    fileLines = GitUtils.getInstance().retrieveFileContentFromCommitList(commitsTouchFile.get(i), modifiedFile.getPath(), repositoryPath);
                }else {
                    fileLines = modifiedFile.getContent();
                }

                fileSizeAtCommit.put(commitsTouchFile.get(i), fileLines.size());

                // 2: Create edges
                //For each line in the file
                for (int j = 1; j <= fileLines.size(); j++) {
                    Vertex vertex = new Vertex(commitsTouchFile.get(i), j);

                    //Add a node in the graph
                    graph.addVertex(vertex);
                }
            }

            //For each commit who touch the file
            for(int i = 1; i <  commitsTouchFile.size(); i++){
                String commitLeft = commitsTouchFile.get(i-1);
                String commitRight = commitsTouchFile.get(i);

                //System.out.println(commitLeft + " --> "+ commitRight);

                // 3: Compute difference between revisions
                List<Hunk> hunks = Utils.getInstance().getHunks(commitLeft, commitRight, modifiedFile.getPath(), repositoryPath);

                // 4: Sort hunks ascending by R_from
                // Default

                // 5: Iterate over all hunks
                int posL = 1, posR = 1;
                for(int j = 0; j < hunks.size(); j++){
                    Hunk hunk = hunks.get(j);

                    // 6: Create edges for unchanged lines
                    while(posL <= hunk.getLeftFrom() && posR <= hunk.getRightFrom()){
                        int finalPosL = posL;
                        int finalPosR = posR;
                        Vertex vertexLeft = graph.vertexSet().stream().filter(vertex -> (vertex.getHash().equals(commitLeft) && vertex.getLine() == finalPosL)).findAny().get();
                        Vertex vertexRight = graph.vertexSet().stream().filter(vertex -> (vertex.getHash().equals(commitRight) && vertex.getLine() == finalPosR)).findAny().get();

                        graph.addEdge(vertexLeft, vertexRight);
                        //System.out.println("ADDING "+commitLeft+"="+posL+ " <--> " + commitRight+"="+posR);

                        posL++;posR++;
                    }

                    //Check for larger modifications
                    boolean isLargetModification = Utils.getInstance().isLargerModification(hunk, fileSizeAtCommit.get(commitLeft), fileSizeAtCommit.get(commitRight));

                    // 7: Create edges for modified lines
                    if(hunk.isChange()){

                        if(!isLargetModification) {
                            for (int l = hunk.getLeftFrom(); l <= hunk.getLeftTo(); l++) {
                                for (int r = hunk.getRightFrom(); r <= hunk.getRightTo(); r++) {

                                    int finalL = l;
                                    int finalR = r;
                                    Vertex vertexLeft = graph.vertexSet().stream().filter(vertex -> (vertex.getHash().equals(commitLeft) && vertex.getLine() == finalL)).findAny().get();
                                    Vertex vertexRight = graph.vertexSet().stream().filter(vertex -> (vertex.getHash().equals(commitRight) && vertex.getLine() == finalR)).findAny().get();

                                    graph.addEdge(vertexLeft, vertexRight);
                                    //System.out.println("CHANGE "+commitLeft+"="+l + " <--> " + commitRight+"="+r);
                                }
                            }
                        }
                    }


                    //Check for larger modifications
                    if(!isLargetModification) {

                        // 8: Set labels for changed and inserted lines
                        if (hunk.isChange() || hunk.isDeletion()) {
                            for (int r = hunk.getRightFrom(); r <= hunk.getRightTo(); r++) {

                                int finalR = r;
                                Vertex vertexRigh = graph.vertexSet().stream().filter(vertex -> (vertex.getHash().equals(commitRight) && vertex.getLine() == finalR)).findAny().get();
                                Vertex vertexRighReplace = new Vertex(commitRight, r, "CHANGE");

                                graph = replaceVertex(graph, vertexRigh, vertexRighReplace);
                            }
                        }

                    }else{
                        //System.out.println("Larger modification: " + hunk.toString());
                    }

                    // 9: Update positions
                    if (hunk.isChange() || hunk.isDeletion()) {
                        posL = hunk.getLeftTo() + 1;
                    }
                    if (hunk.isChange() || hunk.isAddition()) {
                        posR = hunk.getRightTo() + 1;
                    }
                }

                // 10: Create edges for unchanged lines
                while(posL <= fileSizeAtCommit.get(commitLeft) && posR <= fileSizeAtCommit.get(commitRight)){
                    int finalPosL = posL;
                    int finalPosR = posR;

                    Vertex vertex1 = graph.vertexSet().stream().filter(vertex -> (vertex.getHash().equals(commitLeft) && vertex.getLine() == finalPosL)).findAny().get();
                    Vertex vertex2 = graph.vertexSet().stream().filter(vertex -> (vertex.getHash().equals(commitRight) && vertex.getLine() == finalPosR)).findAny().get();

                    //System.out.println("ADDING "+commitLeft+"="+posL+ " <--> " + commitRight+"="+posR);

                    graph.addEdge(vertex1, vertex2);
                    posL++;posR++;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            System.err.println("This file can't be reached in the repository: "+modifiedFile.getPath()+" "+ hash);
        }

        return graph;
    }

}
