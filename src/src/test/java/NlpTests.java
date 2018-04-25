
import opennlp.tools.tokenize.SimpleTokenizer;
import org.junit.jupiter.api.Test;
import pl.edu.pw.elka.nlp.NLPUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NlpTests {

    @Test
    public void StringToVectorTester()
    {
        NLPUtils converter = new NLPUtils();
        List<String> tokens = converter.convertStringToVector("The easy trails, small clearances, magnificent panoramas, large spaces in the Jizera Mountains");
        assertEquals(10, tokens.size()); // "easy trails small clearances magnificent panoramas large spaces Jizera Mountains" + stemmer
    }

}
