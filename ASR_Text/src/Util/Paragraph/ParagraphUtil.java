package Util.Paragraph;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Elva on 6/7/2014.
 */
public class ParagraphUtil {

    private static final Integer LENGTH_THRESHOLD = 15;
    private static final String CONVERSATION_REGEX = ".*[\\W]" +
            "([h][e][y]" +
            "|[m]+" +
            "|[u]([m]+|[h]+)" +
            "|[i]" +
            "|[y][o][u]([r][s]?)?" +
            "|[h][e][l][l][o]" +
            "|[h][i]" +
            "|[m][y]" +
            "|[w][o][w]" +
            "|[a][h][a]*]" +
            "|[o][h]" +
            "|[o][o][p][s]" +
            "|[w][e])" +
            "[\\W].*";

    private ParagraphUtil() {
    }

    public static String conversationFilter(String plot) {
        List<String> sentenceList = Arrays.asList(plot.trim().split("\\r\\n"));
        List<String> validSentenceList = Lists.newArrayList();
        String paragraph = "";
        boolean pre = true;
        for (String sentence : sentenceList) {
            if (sentence.matches("[1-9:\\(\\) \\?]*[A-Za-z1-9\\. #]*[\\s]*:.*")) {
                continue;
            }
            sentence = sentence.replaceAll("\\s\\s+", " ");
            if (sentence.trim().replaceAll("\\(.+\\)", "").length() > LENGTH_THRESHOLD && pre && !sentence.toLowerCase().matches(CONVERSATION_REGEX)) {
                validSentenceList.add(sentence);
            } else if (0 == sentence.trim().length()) {
                pre = true;
            } else {
                pre = false;
            }
        }
        int i = 0;
        for (String sentence : validSentenceList) {
            if  (0 == i++) {
                paragraph += sentence + "\r\n";
            } else {
                paragraph += sentence.trim() + "\r\n";
            }
        }
        return paragraph;
    }
}
