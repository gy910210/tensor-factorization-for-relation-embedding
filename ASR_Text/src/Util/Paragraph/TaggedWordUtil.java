package Util.Paragraph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * Created by elva on 14-1-15.
 */
public final class TaggedWordUtil {
   // private static final Set<String> ALL_NOUNS = ImmutableSet.of("NN", "NNS", "NNP", "NNPS", "FW", "PRP", "PRP$");
    private static final Set<String> ALL_PRONOUNS = ImmutableSet.of("FW", "PRP", "PRP$");
    private static final Set<String> ALL_NOUNS = ImmutableSet.of("NN", "NNS", "NNP", "NNPS");
    private static final Set<String> ALL_VERBS = ImmutableSet.of("VB", "VBD", "VBG", "VBN", "VBP", "VBZ");
    private static final Set<String> ALL_TAGS = ImmutableSet.<String>builder().addAll(ALL_NOUNS).addAll(ALL_VERBS).build();

    private TaggedWordUtil() {
    }

    public static boolean isNoun(String tag) {
        return ALL_NOUNS.contains(tag);
    }

    public static boolean isVerb(String tag) {
        return ALL_VERBS.contains(tag);
    }

    public static boolean isPronoun(String tag) {
        return ALL_PRONOUNS.contains(tag);
    }

    public static boolean isValidTag(String tag) {
        return ALL_TAGS.contains(tag);
    }
}
