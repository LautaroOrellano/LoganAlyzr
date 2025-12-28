package com.loganalyzr.core.rules;

import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.LogRule;

import java.sql.Struct;
import java.util.regex.Pattern;

public class KeywordRule implements LogRule {
    private final String keyword;
    private final boolean useRegex;
    private final boolean useLiteral;
    private final boolean caseSensitive;
    private final boolean useNegated;
    private final Pattern pattern;

    public KeywordRule(String keyword, boolean useRegex, boolean useLiteral,
                       boolean caseSensitive, boolean useNegated
    ) {
        this.keyword = keyword;
        this.useRegex = useRegex;
        this.useLiteral = useLiteral;
        this.caseSensitive = caseSensitive;
        this.useNegated = useNegated;

        if (useRegex) {
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
            this.pattern = Pattern.compile(keyword, flags);
        } else {
            this.pattern = null;
        }

    }

    @Override
    public boolean test(LogEvent event) {
        String message = event.getMessage();

        if(message == null) return false;
        boolean found = checkMatch(message);
        return useNegated ? !found : found;
    }

    @Override
    public boolean isMandatory() {
        return false;
    }

    private boolean checkMatch(String message) {
        if (useRegex) {
            return pattern.matcher(message). find();
        }

        String contentToCheck = caseSensitive ? message : message.toLowerCase();
        String keywordToCheck = caseSensitive ? keyword : keyword.toLowerCase();

        if (useLiteral) {
            return contentToCheck.equals(keywordToCheck);
        } else {
            return contentToCheck.contains(keywordToCheck);
        }
    }
}
