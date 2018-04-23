package cc.episodeMining.IO;

import java.util.Map;
import java.util.Set;

import cc.recommenders.io.Directory;
import cc.recommenders.io.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CodeParser {

	private Directory dir;
	
	@Inject
	public CodeParser(@Named("projects") Directory directory) {
		this.dir = directory;
	}
	
	public Map<String, Set<String>> parse() {
		Map<String, Set<String>> allProjects = Maps.newLinkedHashMap();
		Set<String> oneProject = Sets.newLinkedHashSet();
		
		for (String path : findPaths(dir)) {
			Logger.log("%s", getProjectName(path));
		}
		return allProjects;
	}
	
	private Set<String> findPaths(Directory projectsDir) {
		Set<String> paths = projectsDir.findFiles(new Predicate<String>() {

			public boolean apply(String arg0) {
				return arg0.endsWith(".java");
			}
		});
		return paths;
	}
	
	private String getProjectName(String fileName) {
		int index = fileName.indexOf("/" + 1);
		Logger.log("%s", fileName);
		String startPrefix = fileName.substring(0, index);

		return startPrefix;
	}
}
