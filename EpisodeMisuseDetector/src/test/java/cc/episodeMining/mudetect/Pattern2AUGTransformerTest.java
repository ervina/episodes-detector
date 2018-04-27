package cc.episodeMining.mudetect;

import cc.kave.episodes.mining.reader.EpisodeParser;
import cc.kave.episodes.mining.reader.FileReader;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.events.Fact;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNodes;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.methodCall;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class Pattern2AUGTransformerTest {
    @Rule
    public TemporaryFolder rootFolder = new TemporaryFolder();
    private FileReader reader = new FileReader();
    private static final int NUMREPOS = 2;
    private EpisodeParser parser;

    @Before
    public void setUp() throws Exception {
        rootFolder.create();
        parser = new EpisodeParser(rootFolder.getRoot(), reader);
    }

    @Test
    public void transformsEpisode() {
        File file = createFileWithContent(
                "1-NOde Episodes = 6\n" +
                "1 .	: 3	: 1	:. \n"
        );

        Map<Integer, Set<Episode>> actual = parser.parse(NUMREPOS);

        Set<APIUsagePattern> patterns = transform(actual);

        assertThat(patterns, hasSize(1));
        assertThat(patterns.iterator().next(), hasNodes(methodCall("1", "1")));
    }

    private Set<APIUsagePattern> transform(Map<Integer, Set<Episode>> episodes) {
        Set<APIUsagePattern> patterns = new HashSet<>();
        for (Set<Episode> episodeSet : episodes.values()) {
            for (Episode episode : episodeSet) {
                patterns.add(transform(episode));
            }
        }
        return patterns;
    }

    private APIUsagePattern transform(Episode episode) {
        APIUsagePattern pattern = new APIUsagePattern(episode.getFrequency(), new HashSet<>());
        for (Fact fact : episode.getFacts()) {
            if (!fact.isRelation()) {
                String id = String.valueOf(fact.getFactID());
                pattern.addVertex(new MethodCallNode(id, id));
            }
        }
        return pattern;
    }

    private File createFileWithContent(String content) {
        File file = getFilePath();
        try {
            FileUtils.writeStringToFile(file, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    private File getFilePath() {
        return new File(rootFolder.getRoot().getAbsolutePath() + "/" + NUMREPOS + "Repos/episodes.txt");
    }
}
