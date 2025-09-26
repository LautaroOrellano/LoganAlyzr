package com.loganalizyr.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordCriteria {
    private String keyword;
    private boolean useRegex;
    private boolean useLiteral;
    private boolean caseSensitive;

    public KeywordCriteria(String keyword, boolean useRegex, boolean useLiteral, boolean caseSensitive) {
        this.keyword = keyword;
        this.useRegex = useRegex;
        this.useLiteral = useLiteral;
        this.caseSensitive = caseSensitive;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public boolean isUseRegex() {
        return useRegex;
    }

    public void setUseRegex(boolean useRegex) {
        this.useRegex = useRegex;
    }

    public boolean isUseLiteral() {
        return useLiteral;
    }

    public void setUseLiteral(boolean useLiteral) {
        this.useLiteral = useLiteral;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean matches(String logMessage) {
        if (logMessage != null && !logMessage.isEmpty()) {
            if (isUseLiteral()) {
                return logMessage.contains(keyword);
            } else if (isUseRegex()) {
                Pattern pattern = Pattern.compile(keyword);
                Matcher matcher = pattern.matcher(logMessage);
                return matcher.find();
            }
        }
        return false;
    }
}
