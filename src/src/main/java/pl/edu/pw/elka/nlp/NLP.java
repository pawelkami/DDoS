package pl.edu.pw.elka.nlp;


import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.documentiterator.FileLabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.exception.ND4JIllegalStateException;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pw.elka.nlp.tools.LabelSeeker;
import pl.edu.pw.elka.nlp.tools.MeansBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NLP {
    private static ParagraphVectors vec;
    private TokenizerFactory tokenizerFactory;

    // model name
    private String modelName = "model1.zip";

    private static final Logger log = LoggerFactory.getLogger(NLP.class);

    public NLP() {
        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
    }

    private void createNewModel(String modelFileName) throws IOException {
        LabelAwareIterator iterator;
        File files = new File("/C:/Users/KUBA/Desktop/WEDT/DDoS/datasets/labeled_corpuses");

        iterator = new FileLabelAwareIterator.Builder()
                .addSourceFolder(files)
                .build();

        NLPUtils utils = new NLPUtils();

        // ParagraphVectors training configuration
        vec = new ParagraphVectors.Builder()
                .learningRate(0.025)
                .minLearningRate(0.001)
                .batchSize(1000)
                .epochs(60)
                .iterate(iterator)
                .trainWordVectors(true)
                .tokenizerFactory(tokenizerFactory)
                .stopWords(utils.getStopwords())
                .build();

        // Start model training
        vec.fit();

        // Save model
        final String path = new ClassPathResource(modelFileName).getFile().getAbsolutePath();
        WordVectorSerializer.writeParagraphVectors(vec, path);

        // Save labels of model
//        String aa = Objects.requireNonNull(getClass().getClassLoader().getResource("labels.txt")).getFile();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("labels.txt"))) {
            for (String label : iterator.getLabelsSource().getLabels()) {
                writer.write(label + "\n");
            }
        }
    }

    public double checkTwoTextsSimilarity(String text1, String text2) throws IOException {
        if (vec == null) {
            try {
                final String path = new ClassPathResource(modelName).getFile().getAbsolutePath();
                log.debug(path);
                vec = WordVectorSerializer.readParagraphVectors(path);
            } catch (IOException e) {
                createNewModel(modelName);
            }
        }

        MeansBuilder meansBuilder = new MeansBuilder(
                (InMemoryLookupTable<VocabWord>) vec.getLookupTable(),
                tokenizerFactory);

        LabelledDocument document3 = new LabelledDocument();
        document3.setContent(text1);
        INDArray documentAsCentroid3 = meansBuilder.documentAsVector(document3);

        LabelledDocument document4 = new LabelledDocument();
        document4.setContent(text2);
        INDArray documentAsCentroid4 = meansBuilder.documentAsVector(document4);
        try {
            return Transforms.cosineSim(documentAsCentroid3, documentAsCentroid4);
        }
        catch (ND4JIllegalStateException e) {
            return 0.0;
        }
    }

    public List<Pair<String, Double>> checkNewTextSimilarityToModel(String text) throws IOException {
        List<String> labels = new ArrayList<String>();

        try (Stream<String> stream = Files.lines(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("labels.txt")).toURI()))) {
            labels = stream.collect(Collectors.toList());

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        if (vec == null) {
            try {
                final String path = new ClassPathResource(modelName).getFile().getAbsolutePath();
                log.debug(path);
                vec = WordVectorSerializer.readParagraphVectors(path);
            } catch (IOException e) {
                createNewModel(modelName);
            }
        }

        MeansBuilder meansBuilder = new MeansBuilder(
                (InMemoryLookupTable<VocabWord>) vec.getLookupTable(),
                tokenizerFactory);
        LabelSeeker seeker = new LabelSeeker(labels, (InMemoryLookupTable<VocabWord>) vec.getLookupTable());

        LabelledDocument document = new LabelledDocument();
        document.setContent(text);
        try {
            INDArray documentAsCentroid = meansBuilder.documentAsVector(document);
            List<Pair<String, Double>> scores = seeker.getScores(documentAsCentroid);
            return scores;
        } catch (ND4JIllegalStateException e) {
            return null;
        }
//        log.debug("Document '" + document.getLabels() + "' falls into the following categories: ");
//        for (Pair<String, Double> score : scores) {
//            log.debug("        " + score.getFirst() + ": " + score.getSecond());
//        }
    }
}

