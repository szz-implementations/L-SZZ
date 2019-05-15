package br.com.ufal.easy.model.commit;

import br.com.ufal.easy.control.git.GitUtils;
import br.com.ufal.easy.model.diff.ModifiedFile;

import java.util.Calendar;
import java.util.List;

/**
 * Federal University of Alagoas - 2018
 *
 */

public class Commit {

	private String hash;
	private Calendar date;

	private List<ModifiedFile> modifiedFiles;

	public Commit(String hash, String repositoryPath) {
		super();
		this.hash = hash;
		date = GitUtils.getInstance().getDateTime(hash, repositoryPath);
		modifiedFiles = GitUtils.getInstance().getModifiedFiles(hash, repositoryPath);
	}

	public String getHash() {
		return hash;
	}

	public List<ModifiedFile> getModifiedFiles() {
		return modifiedFiles;
	}
}