import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cc.episodeMining.data.StreamGenerator;
import cc.kave.episodes.model.events.Event;

import com.google.common.collect.Lists;

import de.tu_darmstadt.stg.mubench.cli.DetectionStrategy;
import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.DetectorFinding;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput.Builder;
import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;

public class runner {

	public static void main(String[] args) throws Exception {
		new MuBenchRunner().withMineAndDetectStrategy(new Strategy()).run(args);
	}

	static class Strategy implements DetectionStrategy {

		public DetectorOutput detectViolations(DetectorArgs args, Builder output)
				throws Exception {
			parser(args.getTargetSrcPaths(), args.getDependencyClassPath());
			return output.withFindings(new ArrayList<DetectorFinding>());
		}

		public void parser(String[] srcPaths, String[] classpaths) {
			List<Event> sequences = Lists.newLinkedList();
			for (String srcPath : srcPaths) {
				StreamGenerator generator = new StreamGenerator();
				sequences.addAll(generator.generateMethodTraces(new File(
						srcPath), classpaths));
			}
		}

		// private void addEnclosingMethodIfAvailable() {
		// if (elementCtx != null) {
		// eventStream.add(Events.newElementContext(elementCtx
		// .getFullyQualifiedName()));
		// elementCtx = null;
		// }
		// if (firstCtx != null) {
		// eventStream.add(Events.newFirstContext(firstCtx
		// .getFullyQualifiedName()));
		// firstCtx = null;
		// }
		// if (superCtx != null) {
		// eventStream.add(Events.newSuperContext(superCtx
		// .getFullyQualifiedName()));
		// superCtx = null;
		// }
		// }
	}
}
