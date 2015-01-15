package Util.Paragraph;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static java.util.Arrays.*;

/**
 * Created by elva on 14-1-11.
 */
public class Paragraph {
    private static final String TAGGER_PATH = ".\\english-bidirectional-distsim.tagger";

    private List<TaggedWord> wordList = Lists.newArrayList();
    private List<SemanticGraph> dependencyList = Lists.newArrayList();
    private String paragraph;
    private String naiveParagraph;
    public Paragraph(String paragraph) {
        this.paragraph = paragraph;
        tagWord();
        naiveParagraph= "";
        for (TaggedWord word : getWordList()) {
            naiveParagraph += word.word() + " ";
            //naiveParagraph += word.value() + " ";
        }
        naiveParagraph = paragraph.trim();
    }
    private final void tagWord() {
        Properties properties = new Properties();
        properties.put("annotators", "tokenize,ssplit,pos,lemma,parse");
       // properties.put("annotators", "tokenize,ssplit,pos,lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
        Annotation annotatedParagraph = new Annotation(this.paragraph);
        pipeline.annotate(annotatedParagraph);
        List<CoreMap> sentenceList = annotatedParagraph.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentenceList) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                wordList.add(new TaggedWord(token.get(CoreAnnotations.LemmaAnnotation.class), token.get(CoreAnnotations.PartOfSpeechAnnotation.class)));
                //wordList.add(new TaggedWord(token.get( CoreAnnotations.StemAnnotation.class), token.get(CoreAnnotations.PartOfSpeechAnnotation.class)));
            }
            dependencyList.add(sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class));
        }
        /***************** TEST ****************/
         for (TaggedWord taggedWord : wordList) {
            System.out.println(taggedWord);
         }
         //************** TEST END ***************/
        Collections2.filter(wordList, new Predicate<TaggedWord>() {
            @Override
            public boolean apply(TaggedWord taggedWord) {
                return TaggedWordUtil.isValidTag(taggedWord.tag());
            }
        });
    }
    public List<TaggedWord> getWordList() {
        return wordList;
    }
    public String getParagraph() {
        return paragraph;
    }
    public String getNaiveParagraph() {
        return naiveParagraph;
    }
    public List<SemanticGraph> getDependencyList() {
        return dependencyList;
    }
}
