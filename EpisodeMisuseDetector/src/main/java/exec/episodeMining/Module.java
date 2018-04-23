package exec.episodeMining;
import java.io.File;
import java.util.Map;

import cc.recommenders.io.Directory;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class Module extends AbstractModule{

	private final String rootFolder;
	
	public Module(String rootFolder) {
		this.rootFolder = rootFolder;
	}
	
	@Override
	protected void configure() {
		File rootFile = new File(rootFolder + "/");
		Directory rootDir = new Directory(rootFile.getAbsolutePath());
		File projectsFile = new File(rootFolder + "checkouts/");
		Directory projectsDir = new Directory(projectsFile.getAbsolutePath());
		
		Map<String, Directory> dirs = Maps.newHashMap();
		dirs.put("root", rootDir);
		dirs.put("projects", projectsDir);
		bindInstances(dirs);
		
		bind(File.class).annotatedWith(Names.named("root")).toInstance(rootFile);
		bind(File.class).annotatedWith(Names.named("projects")).toInstance(projectsFile);
	}

	private void bindInstances(Map<String, Directory> dirs) {
		for (String name : dirs.keySet()) {
			Directory dir = dirs.get(name);
			bind(Directory.class).annotatedWith(Names.named(name)).toInstance(dir);
		}
	}
}
