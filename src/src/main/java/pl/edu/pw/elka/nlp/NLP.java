package pl.edu.pw.elka.nlp;

//import net.sf.extjwnl.data.IndexWord;
//import net.sf.extjwnl.data.POS;
//import net.sf.extjwnl.data.PointerType;
//import net.sf.extjwnl.data.Synset;
//import net.sf.extjwnl.data.relationship.Relationship;
//import net.sf.extjwnl.data.relationship.RelationshipFinder;
//import net.sf.extjwnl.data.relationship.RelationshipList;;
//import net.sf.extjwnl.dictionary.Dictionary;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import org.bytedeco.javacv.FrameFilter;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.text.documentiterator.FileLabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.exception.ND4JIllegalStateException;
import org.nd4j.linalg.factory.Nd4j;
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NLP {
    private ParagraphVectors vec;
    private TokenizerFactory tokenizerFactory;
    private NLPUtils nlpUtils;
    private WordVectors gloveWordVectors;
    private IDictionary dict;
    private int maxDepth = 2;

    // model name
    private String modelName = "model1.zip";

    private static final Logger log = LoggerFactory.getLogger(NLP.class);
//    private static volatile NLP instance = null;

//    public static NLP getInstance() throws IOException {
//        if (instance == null) {
//            synchronized (NLP.class) {
//                if (instance == null) {
//                    instance = new NLP();
//                }
//            }
//        }
//        return instance;
//    }


    private class MyDocumentCounter {
        int wordCounter;
        int documentCounter;

        @Override
        public String toString() {
            return "wordCounter = " + wordCounter + " \t with documentCounter = " + documentCounter;
        }
    }

    public NLP() {
        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        nlpUtils = new NLPUtils();

//        File resource = new ClassPathResource("glove.6B.50d.txt").getFile();
//        gloveWordVectors = WordVectorSerializer.loadTxtVectors(resource);
//
//        // construct the URL to the Wordnet dictionary directory
//        String path = "C:\\WordNet\\2.1\\dict";
//        URL url = null;
//        url = new URL("file", null, path);
//
//        // construct the dictionary object and open it
//        dict = new Dictionary(url);
//        dict.open();
    }

    public static void main(String[] args) throws Exception {
        NLP nlp = new NLP();
//
////
////        HashMap<String, Double> similarWords = nlp.findSimilarWords(s, 15);
////        System.out.println(similarWords);
//
////        nlp.checkTwoTextsSimilarity("aaa", "cycle");
//
//
////        Set<String> sss = new HashSet<>();
////        String asd = nlp.nlpUtils.stem("cycle");
////        System.out.println(asd);
////        sss.add(asd);
////
////        similarWords = nlp.findSimilarWords(sss, 15);
////        System.out.println(similarWords);
////
////        synonyms = nlp.findSynonyms(sss);
////        System.out.println(synonyms);
//
//        nlp.createNewModel(nlp.modelName);
//
//
        Path unlabeled_path = Paths.get("C:\\Users\\KUBA\\Desktop\\WEDT\\DDoS\\src\\records\\szklarskaporeba.pl\\7V7u74T1byTDCwhG_5Jun2018220953GMT_1528236593056.txt");
        String stringFromFile = java.nio.file.Files.lines(unlabeled_path).collect(Collectors.joining());
//        Path unlabeled_path2 = Paths.get("C:\\Users\\KUBA\\Desktop\\WEDT\\DDoS\\datasets\\test_corpuses\\c2.txt");
//        String stringFromFile2 = java.nio.file.Files.lines(unlabeled_path2).collect(Collectors.joining());
//        nlp.checkNewTextSimilarityToModel(stringFromFile);
//////
//////        Path baseText = Paths.get("C:\\Users\\KUBA\\Desktop\\WEDT\\DDoS\\datasets\\labeled_corpuses\\cycling\\col-de-crozet.txt");
//////        String stringFromFile2 = java.nio.file.Files.lines(baseText).collect(Collectors.joining());
//////        Double result = nlp.checkTwoTextsSimilarity(stringFromFile2, "cycling near Col de la Faucille");
//////        System.out.println(result);
////
////
////        HashMap<String, Integer> a = nlp.countWordsInDoc(stringFromFile);
////        double sim = nlp.checkTwoTextsSimilarity(stringFromFile, "Cycling paths col de crozet Zmutt");
//
//
//        List<String> d = new ArrayList<>();
//        d.add(stringFromFile2);
//        d.add(stringFromFile);
//        List<MyDocument> sim = nlp.checkTextsSimilarity(d, "cycling in zmutt");
//        System.out.println(sim);
        System.out.println(nlp.checkTwoTextsSimilarity_v2(stringFromFile, "cycling near Zermatt"));
    }

    void createNewModel(String modelFileName) throws IOException {
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

    public double checkTwoTextsSimilarity_v2(String text1, String text2) throws IOException {
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


    public List<MyDocument> checkTextsSimilarity(List<String> documents, String searchedQuery) throws IOException {
        log.debug("Start checking similarity of all texts");

        Set<String> tokenizedQuery = new HashSet<>(nlpUtils.removeStopWords(nlpUtils.tokenize(searchedQuery.toLowerCase())));
        Set<String> tokenizedQuerySynonyms = findSynonyms(tokenizedQuery);
        HashMap<String, Double> tokenizedQuerySimilarWords = findSimilarWords(tokenizedQuery, 15);
        tokenizedQuery = nlpUtils.stem(tokenizedQuery);
        tokenizedQuerySynonyms = nlpUtils.stem(tokenizedQuerySynonyms);
        // remove doubles
        tokenizedQuerySynonyms.removeAll(tokenizedQuery);
        for (String toRemove : tokenizedQuery) {
            tokenizedQuerySimilarWords.remove(toRemove);
        }
        for (String toRemove : tokenizedQuerySynonyms) {
            tokenizedQuerySimilarWords.remove(toRemove);
        }

        List<MyDocument> listOfTextsWords = new ArrayList<>();


        for (String document : documents) {
            MyDocument newDoc = new MyDocument();
            newDoc.document = document;
            newDoc.wordsInDocument = countWordsInDoc(document.toLowerCase());
            newDoc.wordsInDocument.forEach((key, value) -> {
                newDoc.wordCounter += value;
            });

            List<Pair<String, Double>> classify = checkNewTextSimilarityToModel(document);
            Optional<Pair<String, Double>> max = classify.stream().findFirst();
            for (Pair<String, Double> pair : classify) {
                if (pair.getSecond() > max.get().getSecond()) {
                    max = Optional.of(pair);
                }
            }
            if (max.isPresent()) {
                Pair<String, Double> maxValue = max.get();
                switch (maxValue.getFirst()) {
                    case "cycling":
                        if (tokenizedQuery.contains("cycl") || tokenizedQuery.contains("bike")) {
                            newDoc.isInCategory = true;
                        }
                        break;
                    case "hiking":
                        if (tokenizedQuery.contains("hike")) {
                            newDoc.isInCategory = true;
                        }
                        break;
                    case "running":
                        if (tokenizedQuery.contains("run")) {
                            newDoc.isInCategory = true;
                        }
                        break;
                }
            }
            listOfTextsWords.add(newDoc);
        }

//        log.debug("listOfTextsWords = " + listOfTextsWords);

        HashMap<String, MyDocumentCounter> wordsInAllDocuments = new HashMap<>();

        for (MyDocument documentWordMap : listOfTextsWords) {
            for (String word : documentWordMap.wordsInDocument.keySet()) {
                if (wordsInAllDocuments.containsKey(word)) {
                    MyDocumentCounter myDocumentCounter = wordsInAllDocuments.get(word);
                    myDocumentCounter.documentCounter += 1;
                    myDocumentCounter.wordCounter += documentWordMap.wordsInDocument.get(word);
                    wordsInAllDocuments.put(word, myDocumentCounter);
                } else {
                    MyDocumentCounter myDocumentCounter = new MyDocumentCounter();
                    myDocumentCounter.documentCounter = 1;
                    myDocumentCounter.wordCounter += documentWordMap.wordsInDocument.get(word);
                    wordsInAllDocuments.put(word, myDocumentCounter);
                }
            }
        }

//        log.debug("wordsInAllDocuments = " + wordsInAllDocuments);

        for (String tokenQuery : tokenizedQuery) {
            for (MyDocument document : listOfTextsWords) {
                if (document.wordsInDocument.keySet().contains(tokenQuery)) {
//                    double tf = document.wordsInDocument.get(tokenQuery) / document.wordCounter;
                    double tf = document.wordsInDocument.get(tokenQuery);
                    int wordExistsInDocsCounter = wordsInAllDocuments.get(tokenQuery).documentCounter;
                    double idf = Math.log(listOfTextsWords.size() / wordExistsInDocsCounter);
                    double tfIdf = tf * idf;
                    document.rating += tfIdf;
                }
            }
        }

        for (String tokenQuerySynonyms : tokenizedQuerySynonyms) {
            for (MyDocument document : listOfTextsWords) {
                if (document.wordsInDocument.keySet().contains(tokenQuerySynonyms)) {
//                    double tf = document.wordsInDocument.get(tokenQuery) / document.wordCounter;
                    double tf = document.wordsInDocument.get(tokenQuerySynonyms);
                    int wordExistsInDocsCounter = wordsInAllDocuments.get(tokenQuerySynonyms).documentCounter;
                    double idf = Math.log(listOfTextsWords.size() / wordExistsInDocsCounter);
                    double tfIdf = tf * idf;
                    document.rating += 0.5 * tfIdf;
                }
            }
        }

        for (String tokenQuerySimilarWords : tokenizedQuerySimilarWords.keySet()) {
            for (MyDocument document : listOfTextsWords) {
                if (document.wordsInDocument.keySet().contains(tokenQuerySimilarWords)) {
//                    double tf = document.wordsInDocument.get(tokenQuery) / document.wordCounter;
                    double tf = document.wordsInDocument.get(tokenQuerySimilarWords);
                    int wordExistsInDocsCounter = wordsInAllDocuments.get(tokenQuerySimilarWords).documentCounter;
                    double idf = Math.log(listOfTextsWords.size() / wordExistsInDocsCounter);
                    double tfIdf = tf * idf;
                    document.rating += 0.3 * tfIdf * tokenizedQuerySimilarWords.get(tokenQuerySimilarWords);
                }
            }
        }


        return listOfTextsWords;
    }

    public double checkTwoTextsSimilarity(String baseText, String searchedQuery) {
        log.debug("Start checking similarity of two texts");
//        Collection<String> label1 = nlpUtils.convertStringToVector(baseText);
//        Collection<String> label2 = nlpUtils.convertStringToVector(searchedQuery);
//
//        return Transforms.cosineSim(gloveWordVectors.getWordVectorsMean(label1), gloveWordVectors.getWordVectorsMean(label2));


        Set<String> tokenizedQuery = new HashSet<>(nlpUtils.removeStopWords(nlpUtils.tokenize(searchedQuery.toLowerCase())));
        Set<String> tokenizedQuerySynonyms = findSynonyms(tokenizedQuery);
        HashMap<String, Double> tokenizedQuerySimilarWords = findSimilarWords(tokenizedQuery, 15);
        tokenizedQuery = nlpUtils.stem(tokenizedQuery);
        tokenizedQuerySynonyms = nlpUtils.stem(tokenizedQuerySynonyms);
        // remove doubles
        tokenizedQuerySynonyms.removeAll(tokenizedQuery);
        for (String toRemove : tokenizedQuery) {
            tokenizedQuerySimilarWords.remove(toRemove);
        }
        for (String toRemove : tokenizedQuerySynonyms) {
            tokenizedQuerySimilarWords.remove(toRemove);
        }

        HashMap<String, Integer> documentWordsMap = countWordsInDoc(baseText.toLowerCase());

        HashMap<String, Integer> foundTokensQuery = new HashMap<>();
        for (String tokenInQuery : tokenizedQuery) {
            if (documentWordsMap.containsKey(tokenInQuery)) {
                foundTokensQuery.put(tokenInQuery, documentWordsMap.get(tokenInQuery));
            }
        }
        log.debug("foundTokensQuery = " + foundTokensQuery);


        HashMap<String, Integer> foundTokensQuerySynonyms = new HashMap<>();
        for (String tokenInQuery : tokenizedQuerySynonyms) {
            if (documentWordsMap.containsKey(tokenInQuery)) {
                foundTokensQuerySynonyms.put(tokenInQuery, documentWordsMap.get(tokenInQuery));
            }
        }
        log.debug("foundTokensQuerySynonyms  = " + foundTokensQuerySynonyms);

        HashMap<String, Integer> foundTokensQuerySimilarWords = new HashMap<>();
        for (String tokenInQuery : tokenizedQuerySimilarWords.keySet()) {
            if (documentWordsMap.containsKey(tokenInQuery)) {
                foundTokensQuerySimilarWords.put(tokenInQuery, documentWordsMap.get(tokenInQuery));
            }
        }
        log.debug("foundTokensQuerySimilarWords = " + foundTokensQuerySimilarWords);

        final double[] sumTokensQuery = {0};
        foundTokensQuery.forEach((key, value) -> {
            sumTokensQuery[0] += Math.log1p(value);
        });

        final double[] sumTokensQuerySynonyms = {0};
        foundTokensQuerySynonyms.forEach((key, value) -> {
            sumTokensQuerySynonyms[0] += Math.log1p(value);
        });

        final double[] sumTokensQuerySimilarWords = {0};
        foundTokensQuerySimilarWords.forEach((key, value) -> {
            sumTokensQuerySimilarWords[0] += Math.log1p(value) * tokenizedQuerySimilarWords.get(key);
        });

        double result = sumTokensQuery[0] + 0.4 * sumTokensQuerySynonyms[0] + 0.4 * sumTokensQuerySimilarWords[0];
        return result;
    }

    private HashMap<String, Integer> countWordsInDoc(String document) {
        return countWordsInDoc(nlpUtils.convertStringToVector(document));
    }

    private HashMap<String, Integer> countWordsInDoc(List<String> document) {
        HashMap<String, Integer> result = new HashMap<>();
        for (String word : document) {
            if (result.containsKey(word)) {
                result.put(word, result.get(word) + 1);
            } else {
                result.put(word, 1);
            }
        }
        return result;
    }

    //    private Set<String> findSynonyms(String word, Set<String> alreadyInList, int depth) {
    private Set<String> findSynonyms(String word) {
        Set<String> lexicon = new HashSet<>();

        for (POS p : POS.values()) {
            IIndexWord idxWord = dict.getIndexWord(word, p);
            if (idxWord != null) {
                List<IWordID> allWordIDS = idxWord.getWordIDs();
                for (IWordID wordID : allWordIDS) {
                    IWord tmpWord = dict.getWord(wordID);
                    ISynset synset = tmpWord.getSynset();
                    for (IWord w : synset.getWords()) {
                        String newWord = w.getLemma();
                        lexicon.add(newWord);
                    }
                    List<IWordID> relatedWords = tmpWord.getRelatedWords();
                    for (IWordID wID : relatedWords) {
                        tmpWord = dict.getWord(wID);
                        String newWord = tmpWord.getLemma();
                        lexicon.add(newWord);
                    }
                }
            }
        }
        Set<String> lexiconsToAdd = new HashSet<>();
        Iterator<String> lexiconItr = lexicon.iterator();
        while (lexiconItr.hasNext()) {
            String value = lexiconItr.next();
            if (value.matches(".*[_/-].*")) {
                lexiconItr.remove();
                List<String> synonyms = Arrays.asList(value.split("[_/-]"));
                lexiconsToAdd.addAll(synonyms);
            }
        }
        lexicon.addAll(lexiconsToAdd);

        return lexicon;
    }

    private Set<String> findSynonyms(Set<String> words) {
        Set<String> lexicon = new HashSet<>();
        for (String word : words) {
            lexicon.addAll(findSynonyms(word));
        }
        Set<String> stopWords = new HashSet<>(nlpUtils.getStopwords());
        lexicon.removeAll(stopWords);
        return lexicon;
    }

    private HashMap<String, Double> findSimilarWords(Set<String> words, int howManyWords) {
        HashMap<String, Double> result = new HashMap<>();
        for (String word : words) {
            Collection<String> similarWords = gloveWordVectors.wordsNearest(word, howManyWords);
            for (String similarWord : similarWords) {
                String similarWordStemmed = nlpUtils.stem(similarWord);
                double similarity = gloveWordVectors.similarity(word, similarWord);
//                double similarityStemmed = gloveWordVectors.similarity(word, similarWordStemmed);
//                double max = (similarity > similarityStemmed) ? similarity : similarityStemmed;
                if (result.containsKey(similarWordStemmed)) {
                    if (result.get(similarWordStemmed) <= similarity) {
                        result.replace(similarWordStemmed, similarity);
                    }
                } else {
                    result.put(similarWordStemmed, similarity);
                }
            }
        }
        return result;
    }
}

