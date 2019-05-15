package br.com.ufal.easy.start;

import br.com.ufal.easy.model.language.Language;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Federal University of Alagoas - 2018
 *
 */

public class Main {

        //Local path to the repository
        public static String repositoryPath = "/Users/project/";

        //Local path to the folder containing the .json input files
        public static String bugPath = "/Users/bugs/";

        //Local path to the .json output file
        public static String output = "/Users/output.json";

        //The fix commit is an outlier when the number of changed files is greater than
        public static int outlierCommit = 4;

        //Specify the language in the repository to be analysed
        public static int langauge =  Language.JAVA;

        public static void main(String[] args) throws FileNotFoundException {

            App.run(repositoryPath, bugPath, output, langauge, outlierCommit);

        }



}
