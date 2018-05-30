package cc.episodeMining.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellCommand {

	private File eventsFolder;
	private File minerFolder;

	public ShellCommand(File eventsDir, File minerDir) {
		this.eventsFolder = eventsDir;
		this.minerFolder = minerDir;
	}

	public void execute(int frequency, double entropy, int breaker)
			throws IOException {
		String cmd = minerFolder.getAbsolutePath() + "/./n_graph_miner "
				+ getStreamPath(frequency) + " " + frequency * 10 + " "
				+ entropy + " " + breaker + " " + getFreqPath(frequency)
				+ "/episodes.txt";

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

	private String getFreqPath(int frequency) {
		String path = eventsFolder.getAbsolutePath() + "/freq" + frequency;
		return path;
	}

	private String getStreamPath(int frequency) {
		String path = getFreqPath(frequency) + "/stream.txt";
		return path;
	}
}
