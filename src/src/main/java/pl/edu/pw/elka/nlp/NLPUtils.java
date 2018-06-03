package pl.edu.pw.elka.nlp;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class NLPUtils {

    private List<String> stopwords;

    public List<String> getStopwords(){
        return stopwords;
    }

    private void loadStopwords()
    {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("stopwords.txt")).getFile());

        if(file.exists())
        {
            try {
                stopwords = Files.readAllLines(file.toPath(), Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public NLPUtils()
    {
        loadStopwords();
    }

    /**
     * Funkcja konwertująca string do wektora. Najpierw następuje tokenizacja, potem wyrzucane są stopwords a na końcu następuje stemizacja.
     * @param text
     * @return
     */

    private Stemmer stemmer = new PorterStemmer();

    public List<String> convertStringToVector(String text)
    {
        List<String> tokens = removeStopWords(tokenize(text.toLowerCase()));
        stem(tokens);

        return tokens;
    }

    public List<String> removeStopWords(String[] tokens) {
        List<String> clearedTokens = new ArrayList<>();
        for(String s : tokens)
        {
            if(stopwords.contains(s.toLowerCase()))
                continue;

            clearedTokens.add(s);
        }

        return clearedTokens;
    }

    public String[] tokenize(String text)
    {
        Tokenizer tokenizer = SimpleTokenizer.INSTANCE;
        return tokenizer.tokenize(text);
    }

    private void stem(List<String> words)
    {
        for(int i = 0; i < words.size(); ++i)
        {
            words.set(i, ((PorterStemmer) stemmer).stem(words.get(i)));
        }
    }
    public Set<String> stem(Set<String> words)
    {
        Set<String> result = new HashSet<>();
        for(String word : words)
        {
            result.add(((PorterStemmer) stemmer).stem(word));
        }
        return result;
    }
    public String stem(String word) {
        return ((PorterStemmer) stemmer).stem(word);
    }
}
