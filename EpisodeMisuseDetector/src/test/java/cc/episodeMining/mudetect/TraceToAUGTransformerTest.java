package cc.episodeMining.mudetect;

import cc.episodeMining.data.StreamGenerator;
import cc.kave.episodes.model.events.Event;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasOrderEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.methodCall;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TraceToAUGTransformerTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void transformsInvocationsToNodes() throws IOException {
        String code = "class C {\n" +
                "  void m(Object o) {\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}";

        APIUsageExample aug = generateAUG(code);

        assertThat(aug.getNodeSize(), is(1));
        assertThat(aug, hasNode(methodCall("Object" ,"hashCode()")));
    }

    @Test
    public void encodesCallOrder() throws IOException {
        String code = "class C {\n" +
                "  void m(Object o) {\n" +
                "    o.hashCode();\n" +
                "    o.wait();\n" +
                "  }\n" +
                "}";

        APIUsageExample aug = generateAUG(code);

        assertThat(aug, hasOrderEdge(methodCall("Object", "hashCode()"), methodCall("Object", "wait()")));
    }

    @Test
    public void encodesTransitiveOrder() throws IOException {
        String code = "class C {\n" +
                "  void m(Object o) {\n" +
                "    o.hashCode();\n" +
                "    o.wait();\n" +
                "    o.toString();\n" +
                "  }\n" +
                "}";

        APIUsageExample aug = generateAUG(code);

        assertThat(aug, hasOrderEdge(methodCall("Object", "hashCode()"), methodCall("Object", "toString()")));
    }

    @Test
    public void capturesLocationSignature() throws IOException {
        String code = "class C {\n" +
                "  void m() {\n" +
                "    Object o = new Object();\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}";

        APIUsageExample aug = generateAUG(code);

        assertThat(aug.getLocation().getMethodSignature(), is("hashCode()"));
    }

    @Test
    public void capturesLocationSignatureWithParameter() throws IOException {
        String code = "class C {\n" +
                "  void m(Object o) {\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}";

        APIUsageExample aug = generateAUG(code);

        assertThat(aug.getLocation().getMethodSignature(), is("m(Object)"));
    }

    @Test
    public void capturesLocationSignatureWithParameters() throws IOException {
        String code = "class C {\n" +
                "  void m(Object o, int i) {\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}";

        APIUsageExample aug = generateAUG(code);

        assertThat(aug.getLocation().getMethodSignature(), is("m(Object, int)"));
    }

    @Test
    public void capturesLocationFilePath() throws IOException {
        String code = "class C {\n" +
                "  void m(Object o) {\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "}";

        File sourceFile = tmpFolder.newFile("test.java");
        FileUtils.writeStringToFile(sourceFile, code);

        APIUsageExample aug = generateAUG(sourceFile);

        assertThat(aug.getLocation().getFilePath(), is(sourceFile.getAbsolutePath()));
    }

    private APIUsageExample generateAUG(String code) throws IOException {
        File sourceFile = tmpFolder.newFile("test.java");
        FileUtils.writeStringToFile(sourceFile, code);
        return generateAUG(sourceFile);
    }

    private APIUsageExample generateAUG(File sourceFile) {
        String[] classpath = new String[0];
        List<Event> trace = new StreamGenerator().generateMethodTraces(sourceFile, classpath);
        return TraceToAUGTransformer.transform(trace);
    }

}
