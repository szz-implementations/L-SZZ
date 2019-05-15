package br.com.ufal.easy.control;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import br.com.ufal.easy.control.diff.GNUDiffExecutor;
import br.com.ufal.easy.control.git.GitUtils;
import br.com.ufal.easy.model.graph.Vertex;
import com.google.gson.Gson;
import br.com.ufal.easy.model.Bug;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import br.com.ufal.easy.model.diff.Hunk;

import static java.lang.Double.max;

/**
 * Federal University of Alagoas - 2018
 *
 */

public class Utils {
	//Singleton
	private static Utils instance = null;

	private Utils() {
	}

	public static Utils getInstance() {
		if (instance == null) {
			instance = new Utils();
		}
		return instance;
	}

	/**
	 * Returns a list of removed lines from a file in a commit
	 * @param hashLeft
	 * @param hashRight
	 * @param filePath
	 * @param repositoryPath
	 * @return List<String>
	 */
	public List<Hunk> getHunks(String hashLeft, String hashRight, String filePath, String repositoryPath) {
		List<Hunk> hunks = new ArrayList<Hunk>();
		Hunk hunk = null;

		try {
			Map<String, String> filesToDiff = retrieveTempFilesToDiff(hashLeft, hashRight, filePath, repositoryPath);

			if(filesToDiff == null){
				return null;
			}

			GNUDiffExecutor executor = new GNUDiffExecutor(filesToDiff.keySet().iterator().next(), filesToDiff.values().iterator().next());
			List<String> diff = executor.run();

			for (String line : diff) {
				//System.out.println(line);
				Map<String, Integer> hunkLimits = getHunkLimits(line);

				if (hunkLimits != null) {
					hunk = new Hunk();
					hunk.setLeftFrom(hunkLimits.get("lineLeftFrom"));
					hunk.setLeftTo(hunkLimits.get("lineLeftTo"));
					hunk.setRightFrom(hunkLimits.get("lineRightFrom"));
					hunk.setRightTo(hunkLimits.get("lineRightTo"));
					hunk.setKind(hunkLimits.get("kind"));

					hunks.add(hunk);
				}
			}

		} catch (FileNotFoundException | IllegalArgumentException e){
			return hunks;
		}

		return hunks;
	}

	/**
	 * Check for larger modifications
	 * @param hunk
	 * @param fileLeftSize
	 * @param fileRightSize
	 * @return
	 */
	public boolean isLargerModification(Hunk hunk, int fileLeftSize, int fileRightSize){
		double alfa = 0.1;
		double beta = 4;

		int hunkLeftSize = (hunk.getLeftTo() - hunk.getLeftFrom() == 0 ? 1 : hunk.getLeftTo() - hunk.getLeftFrom());
		int hunkRightSize = (hunk.getRightTo() - hunk.getRightFrom() == 0 ? 1 : hunk.getRightTo() - hunk.getRightFrom());

		if(hunkLeftSize > max(alfa*fileLeftSize, beta) || hunkRightSize > max(alfa*fileRightSize, beta) ){
			return true;
		}

		if((hunkLeftSize / hunkRightSize) < (1/beta) || beta < (hunkLeftSize/hunkRightSize)) {
			return true;
		}

		return false;
	}

	/**
	 * Computes the limits in a hunk
	 * @param line
	 * @return
	 * @throws FileNotFoundException
	 */
	public Map<String, Integer> getHunkLimits(String line) throws FileNotFoundException {
		Map<String, Integer> limits = null;

		int lineLeftFrom = -1;
		int lineLeftTo = -1;

		int lineRightFrom = -1;
		int lineRightTo = -1;

		int kind = -1;

		// GNUDiff informs the modifications interval in three ways
		Pattern one = Pattern.compile("(^)(\\d+)(,\\d+)?(c)(\\d+)(,\\d+)?"); //^(\d+,?\d+)c(\d+,?\d+) changes
		Pattern two = Pattern.compile("(^)(\\d+)(,\\d+)?(a)(\\d+)(,\\d+)?"); // ^(\d+,?\d+)a(\d+,?\d+) additions
		Pattern three = Pattern.compile("(^)(\\d+)(,\\d+)?(d)(\\d+)(,\\d+)?"); // ^(\d+,?\d+)d(\d+,?\d+) deletions

		Matcher mOne = one.matcher(line);
		Matcher mTwo = two.matcher(line);
		Matcher mThree = three.matcher(line);

		if (mOne.find()) {
			try {
				//Be here means the line has the type 524,525c524,526 or 524c524,526 or 524,525c526 or 524c526
				//We must get the line intervals

				String[] token = line.replace(" ", "").split("[c]");

				String[] _tokenLeft = token[0].split(",");
				String[] _tokenRight = token[1].split(",");

				if(_tokenLeft.length == 2){
					lineLeftFrom = Integer.parseInt(_tokenLeft[0]);
					lineLeftTo = Integer.parseInt(_tokenLeft[1]);
				}else{
					lineLeftFrom = Integer.parseInt(token[0]);
					lineLeftTo = lineLeftFrom;
				}

				if(_tokenRight.length == 2){
					lineRightFrom = Integer.parseInt(_tokenRight[0]);
					lineRightTo = Integer.parseInt(_tokenRight[1]);
				}else{
					lineRightFrom = Integer.parseInt(token[1]);
					lineRightTo = lineRightFrom;
				}

				kind = Hunk.MODIFICATION;
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (mTwo.find()) {
			try {
				//Be here means the line has the type 524,525a524,526 or 524a524,526 or 524,525a526 or 524a526
				//We must get the line intervals

				String[] token = line.replace(" ", "").split("[a]");

				String[] _tokenLeft = token[0].split(",");
				String[] _tokenRight = token[1].split(",");

				if(_tokenLeft.length == 2){
					lineLeftFrom = Integer.parseInt(_tokenLeft[0]);
					lineLeftTo = Integer.parseInt(_tokenLeft[1]);
				}else{
					lineLeftFrom = lineLeftTo = Integer.parseInt(token[0]);
				}

				if(_tokenRight.length == 2){
					lineRightFrom = Integer.parseInt(_tokenRight[0]);
					lineRightTo = Integer.parseInt(_tokenRight[1]);
				}else{
					lineRightFrom = lineRightTo = Integer.parseInt(token[1]);
				}
			kind = Hunk.ADDITION;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (mThree.find()) {
			try {
				//Be here means the line has the type 524,525d524,526 or 524d524,526 or 524,525d526 or 524d526
				//We must get the line intervals

				String[] token = line.replace(" ", "").split("[d]");

				String[] _tokenLeft = token[0].split(",");
				String[] _tokenRight = token[1].split(",");

				if(_tokenLeft.length == 2){
					lineLeftFrom = Integer.parseInt(_tokenLeft[0]);
					lineLeftTo = Integer.parseInt(_tokenLeft[1]);
				}else{
					lineLeftFrom = lineLeftTo =  Integer.parseInt(token[0]);
				}

				if(_tokenRight.length == 2){
					lineRightFrom = Integer.parseInt(_tokenRight[0]);
					lineRightTo = Integer.parseInt(_tokenRight[1]);
				}else{
					lineRightFrom = lineRightTo = Integer.parseInt(token[1]);
				}
			kind = Hunk.DELETION;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(lineLeftFrom != -1 && lineLeftTo != -1 && lineRightFrom != -1 && lineRightTo != -1){
			limits = new HashMap<String, Integer>();

			limits.put("lineLeftFrom", lineLeftFrom);
			limits.put("lineLeftTo", lineLeftTo);
			limits.put("lineRightFrom", lineRightFrom);
			limits.put("lineRightTo", lineRightTo);
			limits.put("kind",kind);
		}

		return limits;
	}

	/**
	 * Returns the largest insertion bug commit (vertex) in json format
	 * @param insertionBugCommit
	 * @param repositoryPath
	 * @return
	 */
	public static String largestVertex(Set<Vertex> insertionBugCommit, String repositoryPath) {
		String ret = "[";

		int auxSize = 0;
		Vertex auxVertex = null;

		for (Vertex currentVertex : insertionBugCommit) {
			int size = GitUtils.getInstance().getSizeOfCommit(currentVertex.getHash(), repositoryPath);

			if(size > auxSize){
				auxSize = size;
				auxVertex = currentVertex;
			}
		}

		if (auxVertex != null) {

			SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String date = s.format(GitUtils.getInstance().getDateTime(auxVertex.getHash(), repositoryPath).getTime());

			ret += "{ \"hash\": \"" + auxVertex.getHash() + "\", \"line\": \"" + auxVertex.getLine() + "\", \"commit_date\": \"" + date + "\" },";
		}

		if (ret.equals("[")) {
			return "[]";
		} else {
			return ret.substring(0, ret.length() - 1) + "]";
		}
	}

	/**
	 * Returns a Map<String, String> containing a file content in its atual and previous revision
	 * @param hashLeft
	 * @param hashRight
	 * @param file
	 * @param repositoryPath
	 * @return Map<String, String>
	 * @throws FileNotFoundException
	 * @throws IllegalArgumentException
	 */

	public Map<String, String> retrieveTempFilesToDiff(final String hashLeft, final String hashRight, final String file, final String repositoryPath) throws FileNotFoundException, IllegalArgumentException {
		String leftFileContent;
		String rightFileContent;

		try {
			leftFileContent = GitUtils.getInstance().retrieveFileContentFromCommit(hashLeft, file, repositoryPath);
			rightFileContent = GitUtils.getInstance().retrieveFileContentFromCommit(hashRight, file, repositoryPath);
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException();
		}

		Map<String, String> tempFilesPath = new HashMap<>();

		if ("".equals(leftFileContent.trim()) || "".equals(rightFileContent.trim())) {
			return tempFilesPath;
		}

		try {
			File leftTempFile = File.createTempFile(file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".")), ".txt");
			BufferedWriter bw = new BufferedWriter(new FileWriter(leftTempFile));
			bw.write(leftFileContent);
			bw.close();
			File rightTempFile = File.createTempFile(file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".")), ".txt");
			bw = new BufferedWriter(new FileWriter(rightTempFile));
			bw.write(rightFileContent);
			bw.close();

			tempFilesPath.put(leftTempFile.getAbsolutePath(), rightTempFile.getAbsolutePath());

		} catch (StringIndexOutOfBoundsException | IllegalArgumentException e) {
			System.err.println("Invalid file format name: " + file);
			throw new IllegalArgumentException();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tempFilesPath;
	}

	/**
	 * Returns List<String> containing all .json files in a folder
	 * @param folder
	 * @return List<String>
	 */
	public List<String> getAllJsonFiles(final File folder){
		List<String> files = new ArrayList<>();

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				getAllJsonFiles(fileEntry);
			} else {
				if(fileEntry.getAbsolutePath().substring(fileEntry.getAbsolutePath().lastIndexOf("." )+1).equals("json"))
					files.add(fileEntry.getName());
			}
		}
		return files;
	}

	/**
	 * Returns a Bug containing data about a bug
	 * @param file
	 * @return Bug
	 */
	public Bug readJson(String file) {
		try {
			Reader reader = new FileReader(file);
			Gson gson = new Gson();

			return gson.fromJson(reader, Bug.class);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Write a content in a file
	 * @param fileName
	 * @param content
	 */
	public void FileWrite(String fileName, String content) {
		File file = new File(fileName);

		try {
			PrintWriter writer = new PrintWriter(new FileOutputStream(new File(fileName), true));
			writer.println(content);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a String containing the output for a command execution
	 * @param arguments
	 * @return String
	 */
	public String executeCommand(final String... arguments) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
		CommandLine commandLine = CommandLine.parse("git");

		if (arguments != null) {
			commandLine.addArguments(arguments);
		}

		DefaultExecutor defaultExecutor = new DefaultExecutor();
		defaultExecutor.setExitValue(0);

		try {
			defaultExecutor.setStreamHandler(streamHandler);
			defaultExecutor.execute(commandLine);
		} catch (ExecuteException e) {
			System.err.println("Execution failed.");
			System.err.println(commandLine);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("permission denied.");
			e.printStackTrace();
		}

		return outputStream.toString();
	}

	/**
	 *  Returns a String containing the output for a command execution in a repository
	 * @param comando
	 * @param repository
	 * @return String
	 */
	public String executeCommand(String comando, String repository) {
		File f = new File(repository);
		Process process = null;
		StringBuilder output = new StringBuilder();

		try {
			process = Runtime.getRuntime().exec(comando, null,f);
		} catch (IOException e) {
			e.printStackTrace();
		}

		InputStream inputStream = process.getInputStream(); {
			int n;

			try {
				while ((n = inputStream.read()) != -1) {
					output.append((char) n);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return output.toString();
	}

	/**
	 * Count the number of lines in a file
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public int countLines(String filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean endsWithoutNewLine = false;
			while ((readChars = is.read(c)) != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n')
						++count;
				}
				endsWithoutNewLine = (c[readChars - 1] != '\n');
			}
			if(endsWithoutNewLine) {
				++count;
			}
			return count;
		} finally {
			is.close();
		}
	}
}