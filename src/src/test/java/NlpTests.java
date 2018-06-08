
import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.primitives.Pair;
import pl.edu.pw.elka.nlp.NLP;
import pl.edu.pw.elka.nlp.NLPUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NlpTests {

//    @Test
//    public void StringToVectorTester() {
//        NLPUtils converter = new NLPUtils();
//        List<String> tokens = converter.convertStringToVector("The easy trails, small clearances, magnificent panoramas, large spaces in the Jizera Mountains");
//        assertEquals(10, tokens.size()); // "easy trails small clearances magnificent panoramas large spaces Jizera Mountains" + stemmer
//    }

    private class LabelledAccuracy {
        private List<String> labels;
        private Map<String, Map<String, Integer>> hitMap;

        public LabelledAccuracy(List<String> labels) {
            this.labels = labels;
            this.hitMap = new HashMap<>();
            for (String outterLabel : labels) {
                HashMap<String, Integer> innerMap = new HashMap<>();
                for (String innerLabel : labels) {
                    innerMap.put(innerLabel, 0);
                }
                this.hitMap.put(outterLabel, innerMap);
            }
        }

        public void addHit(String first, String second) {
            assert labels.contains(first);
            assert labels.contains(second);
            hitMap.get(first).put(second, getHit(first, second) + 1);
        }

        public int getHit(String first, String second) {
            return hitMap.get(first).get(second);
        }

        void printAccuracyTable() {
            System.out.print("\t\t");
            for (String label : labels) {
                System.out.print(label + "\t");
            }
            System.out.print("\n");
            for (String outerLabel : labels) {
                System.out.print(outerLabel + "\t");
                for (String innerLabel : labels) {
                    System.out.print(getHit(innerLabel, outerLabel) + "\t\t");
                }
                System.out.print("\n");
            }
        }
    }


    @Test
    void labelsTest() throws IOException {
        List<String> labelsFromResources = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get("src/main/resources/labels.txt"))) {
            lines.forEach(labelsFromResources::add);
        }
        File resourcesDirectory[] = new File("../datasets/labeled_corpuses").listFiles();
        int labelledCorpusCounter = 0;
        assert resourcesDirectory != null;
        for (File labelledDirectory : resourcesDirectory) {
            if (labelledDirectory.isDirectory()) {
                assert labelsFromResources.contains(labelledDirectory.getName());
                labelledCorpusCounter++;
            }
        }
        assert labelledCorpusCounter == labelsFromResources.size();
    }

    @Test
    void LabelledAccuracyTest() throws IOException {
        List<String> labelsFromResources = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get("src/main/resources/labels.txt"))) {
            lines.forEach(labelsFromResources::add);
        }
        LabelledAccuracy labelledAccuracy = new LabelledAccuracy(labelsFromResources);
        assert labelsFromResources.size() > 0;
        labelledAccuracy.addHit(labelsFromResources.get(0), labelsFromResources.get(0));
        int hits = labelledAccuracy.getHit(labelsFromResources.get(0), labelsFromResources.get(0));
        assert hits == 1;
    }

    @Test
    void modelTestTrainingData() throws IOException {
        NLP nlp = new NLP();

        List<String> labelsFromResources = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get("src/main/resources/labels.txt"))) {
            lines.forEach(labelsFromResources::add);
        }
        LabelledAccuracy labelledAccuracy = new LabelledAccuracy(labelsFromResources);

        File resourcesDirectory[] = new File("../datasets/labeled_corpuses").listFiles();
        assert resourcesDirectory != null;
        for (File labelledDirectory : resourcesDirectory) {
            if (labelledDirectory.isDirectory()) {
                File labelledFiles[] = new File(labelledDirectory.getPath()).listFiles();
                String label = labelledDirectory.getName();
                assert labelledFiles != null;
                for (File labelledFile : labelledFiles) {
                    String fileContent = FileUtils.readFileToString(labelledFile, StandardCharsets.UTF_8);
                    List<Pair<String, Double>> classifierOutput = nlp.checkNewTextSimilarityToModel(fileContent);
                    Pair<String, Double> maxPair = getMaxPairFromList(classifierOutput);
                    labelledAccuracy.addHit(label, maxPair.getFirst());
                }
            }
        }
        labelledAccuracy.printAccuracyTable();
    }

    @Test
    void modelTestScrapedData() throws IOException {
        NLP nlp = new NLP();

        List<String> labelsFromResources = new ArrayList<>();
        try (Stream<String> lines = Files.lines(Paths.get("src/main/resources/labels.txt"))) {
            lines.forEach(labelsFromResources::add);
        }
        LabelledAccuracy labelledAccuracy = new LabelledAccuracy(labelsFromResources);

        File resourcesDirectory[] = new File("../datasets/scraped_corpuses").listFiles();
        assert resourcesDirectory != null;
        for (File labelledDirectory : resourcesDirectory) {
            if (labelledDirectory.isDirectory()) {
                File labelledFiles[] = new File(labelledDirectory.getPath()).listFiles();
                String label = labelledDirectory.getName();
                assert labelledFiles != null;
                for (File labelledFile : labelledFiles) {
                    String fileContent = FileUtils.readFileToString(labelledFile, StandardCharsets.UTF_8);
                    List<Pair<String, Double>> classifierOutput = nlp.checkNewTextSimilarityToModel(fileContent);
                    Pair<String, Double> maxPair = getMaxPairFromList(classifierOutput);
                    String classifierLabel = maxPair.getFirst();
                    labelledAccuracy.addHit(label, classifierLabel);
                    if (((label.equals("cycling") || label.equals("hiking") || label.equals("running")) &&
                            (classifierLabel.equals("finance") || classifierLabel.equals("science") || classifierLabel.equals("health")))
                            ||
                            ((label.equals("cycling") || label.equals("hiking") || label.equals("running")) &&
                            (classifierLabel.equals("finance") || classifierLabel.equals("science") || classifierLabel.equals("health")))) {
                                System.out.println("Serious model error: " + labelledFile.getName());
                    }
                }
            }
        }
        labelledAccuracy.printAccuracyTable();
    }

    private Pair<String, Double> getMaxPairFromList(List<Pair<String, Double>> list) {
        Pair<String, Double> maxValue;
        maxValue = new Pair<>("temporary", Double.MIN_VALUE);
        for (Pair<String, Double> pair : list) {
            if (pair.getSecond() > maxValue.getSecond())
                maxValue = pair;
        }
        return maxValue;
    }
}
