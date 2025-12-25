package com.loganalyzr.core.rules;

import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.LogRule;

import java.util.regex.Pattern;

public class KeywordRule implements LogRule {
    private final String keyword;
    private final boolean useRegex;
    private final boolean caseSensitive;
    private final Pattern pattern;

    public KeywordRule(String keyword, boolean useRegex, boolean caseSensitive) {
        this.keyword = keyword;
        this.useRegex = useRegex;
        this.caseSensitive = caseSensitive;

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

        if (useRegex) {
            return pattern.matcher(message).find();
        }

        if (!caseSensitive) {
            return message.toLowerCase().contains(keyword.toLowerCase());
        }

        return message.contains(keyword);
    }
}
