package cc.episodeMining.algorithm;

import static cc.recommenders.assertions.Asserts.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellCommand {

	private File eventsFolder;
	private File minerFolder;

	public ShellCommand(File eventsDir, File minerDir) {
		assertTrue(eventsDir.exists(), "Events folder does not exist");
		assertTrue(eventsDir.isDirectory(), "Events is not a folder, but a file");
		assertTrue(minerDir.exists(), "Episode miner folder does not exist");
		assertTrue(minerDir.isDirectory(), "Episode miner is not a folder, but a file");
		this.eventsFolder = eventsDir;
		this.minerFolder = minerDir;
	}

	public void execute(int frequency, double entropy, int breaker)
			throws IOException {
		String cmd = minerFolder.getAbsolutePath() + "/./n_graph_miner "
				+ getStreamPath() + " " + frequency + " "
				+ entropy + " " + breaker + " " + getEpisodePath();

		System.out.println("\nRunning the miner ...");
		String output = runCommand(cmd);
		System.out.println(output);
	}

	private String runCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output.toString();
	}

	private String getStreamPath() {
		String path = eventsFolder.getAbsolutePath() + "/stream.txt";
		return path;
	}

	private String getEpisodePath() {
		String path = eventsFolder.getAbsolutePath() + "/episodes.txt";
		return path;
	}
}
