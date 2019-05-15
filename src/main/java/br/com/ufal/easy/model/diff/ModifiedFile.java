package br.com.ufal.easy.model.diff;

import br.com.ufal.easy.control.git.GitUtils;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Federal University of Alagoas - 2018
 *
 */


public class ModifiedFile {

	private String path;
	private List<String> content = null;

	public ModifiedFile(String hash, String path, String repositoryPath) throws FileNotFoundException {
		super();
		this.path = path;
		this.content = GitUtils.getInstance().retrieveFileContentFromCommitList(hash, path, repositoryPath);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<String> getContent() {
		return content;
	}

	public void setContent(List<String> content) {
		this.content = content;
	}
}
