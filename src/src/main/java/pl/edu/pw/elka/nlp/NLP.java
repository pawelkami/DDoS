package pl.edu.pw.elka.nlp;

import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.primitives.Pair;
import pl.edu.pw.elka.nlp.NLPUtils;

import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.documentiterator.FileLabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.nd4j.linalg.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pw.elka.nlp.tools.LabelSeeker;
import pl.edu.pw.elka.nlp.tools.MeansBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NLP {
    private ParagraphVectors vec;
    private TokenizerFactory tokenizerFactory;

    private static final Logger log = LoggerFactory.getLogger(NLP.class);

    public NLP() {
        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
    }

    public static void main(String[] args) throws Exception {
        NLP nlp = new NLP();
        nlp.createNewModel("model1.zip");


        Path unlabeled_path = Paths.get("C:\\Users\\KUBA\\Desktop\\WEDT\\DDoS\\datasets\\test_corpuses\\c1.txt");
        String stringFromFile = java.nio.file.Files.lines(unlabeled_path).collect(Collectors.joining());
        nlp.checkNewTextSimilarityToModel("model1.zip", stringFromFile);
    }

    void createNewModel(String modelFileName) throws IOException {
        LabelAwareIterator iterator;
        File files = new File("/C:/Users/KUBA/Desktop/WEDT/DDoS/datasets/labeled_corpuses");

        iterator = new FileLabelAwareIterator.Builder()
                .addSourceFolder(files)
                .build();

        // ParagraphVectors training configuration
        vec = new ParagraphVectors.Builder()
                .learningRate(0.025)
                .minLearningRate(0.001)
                .batchSize(1000)
                .epochs(1)
                .iterate(iterator)
                .trainWordVectors(true)
                .tokenizerFactory(tokenizerFactory)
                .build();

        // Start model training
        vec.fit();

        // Save model
        final String path = new ClassPathResource("model1.zip").getFile().getAbsolutePath();
        WordVectorSerializer.writeParagraphVectors(vec, path);

        // Save labels of model
//        String aa = Objects.requireNonNull(getClass().getClassLoader().getResource("labels.txt")).getFile();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("labels.txt"))) {
            for (String label : iterator.getLabelsSource().getLabels()) {
                writer.write(label + "\n");
            }
        }


    }

    void checkNewTextSimilarityToModel(String locationToLoadModel, String text) throws Exception {
        List<String> labels = new ArrayList<String>();

        try (Stream<String> stream = Files.lines(Paths.get("labels.txt"))) {
            labels = stream.collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (vec == null) {
            try {
                vec = WordVectorSerializer.readParagraphVectors(locationToLoadModel);
            } catch (IOException e) {
                createNewModel(locationToLoadModel);
            }
        }

        MeansBuilder meansBuilder = new MeansBuilder(
                (InMemoryLookupTable<VocabWord>) vec.getLookupTable(),
                tokenizerFactory);
        LabelSeeker seeker = new LabelSeeker(labels, (InMemoryLookupTable<VocabWord>) vec.getLookupTable());

        LabelledDocument document = new LabelledDocument();
        document.setContent(text);
        INDArray documentAsCentroid = meansBuilder.documentAsVector(document);
        List<Pair<String, Double>> scores = seeker.getScores(documentAsCentroid);


        log.info("Document '" + document.getLabels() + "' falls into the following categories: ");
        for (Pair<String, Double> score : scores) {
            log.info("        " + score.getFirst() + ": " + score.getSecond());
        }
    }
}
