package cc.episodeMining.algorithm;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import cc.kave.episodes.io.EpisodeParser;
import cc.recommenders.exceptions.AssertionException;

public class ShellCommandTest {

	@Rule
	public TemporaryFolder rf1 = new TemporaryFolder();
	@Rule
	public TemporaryFolder rf2 = new TemporaryFolder();
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private ShellCommand sut;
	
	@Before
	public void setup() {
		sut = new ShellCommand(rf1.getRoot(), rf2.getRoot());
	}
	
	@Test
	public void cannotBeInitializedWithNonExistingFolder1() {
		thrown.expect(AssertionException.class);
		thrown.expectMessage("Events folder does not exist");
		sut = new ShellCommand(new File("does not exist"), rf2.getRoot());
	}

	@Test
	public void cannotBeInitializedWithFile1() throws IOException {
		File file = rf1.newFile("a");
		thrown.expect(AssertionException.class);
		thrown.expectMessage("Events is not a folder, but a file");
		sut = new ShellCommand(file, rf2.getRoot());
	}
	
	@Test
	public void cannotBeInitializedWithNonExistingFolder2() {
		thrown.expect(AssertionException.class);
		thrown.expectMessage("Episode miner folder does not exist");
		sut = new ShellCommand(rf1.getRoot(), new File("does not exist"));
	}

	@Test
	public void cannotBeInitializedWithFile2() throws IOException {
		File file = rf1.newFile("a");
		thrown.expect(AssertionException.class);
		thrown.expectMessage("Episode miner is not a folder, but a file");
		sut = new ShellCommand(rf1.getRoot(), file);
	}
}
