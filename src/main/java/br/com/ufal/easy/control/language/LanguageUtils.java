package br.com.ufal.easy.control.language;

import br.com.ufal.easy.model.language.Language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageUtils {



    //Singleton
    private static LanguageUtils instance = null;

    private LanguageUtils() {

    }

    public static LanguageUtils getInstance() {
        if (instance == null) {
            instance = new LanguageUtils();
        }
        return instance;
    }

    public int getCommentType(String line, int language) {
        switch (language) {
            case Language.JAVA: {
                Pattern patternLine = Pattern.compile("(^\\s*//.*)|(\\s*/\\*.*\\*/)|(^\\s*$)");
                Pattern patternBlock = Pattern.compile("(\\s*/\\*.*)|(.*\\*/.*)");

                Matcher matcherLine = patternLine.matcher(line);
                Matcher matcherBlock = patternBlock.matcher(line);

                if (matcherLine.find()){
                    return Language.lineComment;
                }else if (matcherBlock.find()){
                    return Language.blockComment;
                }else{
                    return Language.notAComment;
                }
            }
            case Language.CPP: {
                Pattern patternLine = Pattern.compile("(^\\s*//.*)|(\\s*/\\*.*\\*/)|(^\\s*$)");
                Pattern patternBlock = Pattern.compile("(\\s*/\\*.*)|(.*\\*/.*)");

                Matcher matcherLine = patternLine.matcher(line);
                Matcher matcherBlock = patternBlock.matcher(line);

                if (matcherLine.find()){
                    return Language.lineComment;
                }else if (matcherBlock.find()){
                    return Language.blockComment;
                }else{
                    return Language.notAComment;
                }
            }
            case Language.JAVASCRIPT: {
                Pattern patternLine = Pattern.compile("(^\\s*//.*)|(\\s*/\\*.*\\*/)|(^\\s*$)");
                Pattern patternBlock = Pattern.compile("(\\s*/\\*.*)|(.*\\*/.*)");

                Matcher matcherLine = patternLine.matcher(line);
                Matcher matcherBlock = patternBlock.matcher(line);

                if (matcherLine.find()){
                    return Language.lineComment;
                }else if (matcherBlock.find()){
                    return Language.blockComment;
                }else{
                    return Language.notAComment;
                }
            }
            case Language.TYPESCRIPT: {
                Pattern patternLine = Pattern.compile("(^\\s*//.*)|(\\s*/\\*.*\\*/)|(^\\s*$)");
                Pattern patternBlock = Pattern.compile("(\\s*/\\*.*)|(.*\\*/.*)");

                Matcher matcherLine = patternLine.matcher(line);
                Matcher matcherBlock = patternBlock.matcher(line);

                if (matcherLine.find()){
                    return Language.lineComment;
                }else if (matcherBlock.find()){
                    return Language.blockComment;
                }else{
                    return Language.notAComment;
                }
            }
            case Language.GO: {
                Pattern patternLine = Pattern.compile("(^\\s*//.*)|(^\\s*$)");

                Matcher matcherLine = patternLine.matcher(line);

                if (matcherLine.find()){
                    return Language.lineComment;
                }else{
                    return Language.notAComment;
                }
            }
            case Language.CSHARP: {
                Pattern patternLine = Pattern.compile("(^\\s*//.*)|(\\s*/\\*.*\\*/)|(^\\s*$)");
                Pattern patternBlock = Pattern.compile("(\\s*/\\*.*)|(.*\\*/.*)");

                Matcher matcherLine = patternLine.matcher(line);
                Matcher matcherBlock = patternBlock.matcher(line);

                if (matcherLine.find()){
                    return Language.lineComment;
                }else if (matcherBlock.find()){
                    return Language.blockComment;
                }else{
                    return Language.notAComment;
                }
            }
            case Language.CLOJURE: {
                Pattern patternLine = Pattern.compile("(^\\s*;.*)|(^\\s*$)");

                Matcher matcherLine = patternLine.matcher(line);

                if (matcherLine.find()){
                    return Language.lineComment;
                }else{
                    return Language.notAComment;
                }
            }
            case Language.JULIA: {
                Pattern patternLine = Pattern.compile("(^\\s*\".*\")|(\\s*\"\"\".*\"\"\")|(^\\s*$)");
                Pattern patternBlock = Pattern.compile("(\\s*\"\"\").*");

                Matcher matcherLine = patternLine.matcher(line);
                Matcher matcherBlock = patternBlock.matcher(line);

                if (matcherLine.find()){
                    return Language.lineComment;
                }else if (matcherBlock.find()){
                    return Language.blockComment;
                }else{
                    return Language.notAComment;
                }
            }
            case Language.PHP: {
                Pattern patternLine = Pattern.compile("(^\\s*//.*)|(^\\s*#.*)|(\\s*/\\*.*\\*/)|(^\\s*$)");
                Pattern patternBlock = Pattern.compile("(\\s*/\\*.*)|(.*\\*/.*)");

                Matcher matcherLine = patternLine.matcher(line);
                Matcher matcherBlock = patternBlock.matcher(line);

                if (matcherLine.find()){
                    return Language.lineComment;
                }else if (matcherBlock.find()){
                    return Language.blockComment;
                }else{
                    return Language.notAComment;
                }
            }
            case Language.RUST: {
                Pattern patternLine = Pattern.compile("(^\\s*//.*)|(^\\s*$)");
                Matcher matcherLine = patternLine.matcher(line);

                if (matcherLine.find()){
                    return Language.lineComment;
                }else{
                    return Language.notAComment;
                }
            }
            case Language.RUBY: {
                Pattern patternLine = Pattern.compile("(^\\s*#.*)|(^\\s*$)");
                Matcher matcherLine = patternLine.matcher(line);

                if (matcherLine.find()){
                    return Language.lineComment;
                }else{
                    return Language.notAComment;
                }
            }
            case Language.PYTHON: {

                Pattern patternLine = Pattern.compile("(^\\s*#.*)|(\\s*\"\"\".*\"\"\")|(^\\s*$)");
                Pattern patternBlock = Pattern.compile("(\\s*\"\"\".*)");

                Matcher matcherLine = patternLine.matcher(line);
                Matcher matcherBlock = patternBlock.matcher(line);

                if (matcherLine.find()){
                    return Language.lineComment;
                }else if (matcherBlock.find()){
                    return Language.blockComment;
                }else{
                    return Language.notAComment;
                }
            }
        }
        return Language.notAComment;
    }

    private ArrayList<String> getFileExtensionByLanguage(int language) {
        switch (language) {
            case Language.JAVA: {
                return new ArrayList<String>(Arrays.asList(".java"));
            }
            case Language.CPP: {
                return new ArrayList<String>(Arrays.asList(".cc", ".cpp", ".h"));
            }
            case Language.JAVASCRIPT: {
                return new ArrayList<String>(Arrays.asList(".js"));
            }
            case Language.TYPESCRIPT: {
                return new ArrayList<String>(Arrays.asList(".ts"));
            }
            case Language.GO: {
                return new ArrayList<String>(Arrays.asList(".go"));
            }
            case Language.CSHARP: {
                return new ArrayList<String>(Arrays.asList(".cs"));
            }
            case Language.CLOJURE: {
                return new ArrayList<String>(Arrays.asList(".clj", ".cljs", ".cljc", ".edn"));
            }
            case Language.JULIA: {
                return new ArrayList<String>(Arrays.asList(".jl"));
            }
            case Language.PHP: {
                return new ArrayList<String>(Arrays.asList(".php", ".php3", ".php4", ".php5", ".phtml"));
            }
            case Language.RUST: {
                return new ArrayList<String>(Arrays.asList(".rs", ".rlib"));
            }
            case Language.RUBY: {
                return new ArrayList<String>(Arrays.asList(".rb"));
            }
            case Language.PYTHON: {
                return new ArrayList<String>(Arrays.asList(".py"));
            }
        }
        return null;
    }

    public boolean isACodeFile(int language, String fileName){

        ArrayList<String> extension = getFileExtensionByLanguage(language);
        for (String ext: extension) {
            if(fileName.contains(ext)){
                return true;
            }
        }
        return false;
    }
}
