package com.loganalyzr.infrastructure.config.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordCriteriaDTO {
    private String keyword;
    private boolean useRegex;
    private boolean useLiteral;
    private boolean caseSensitive;
    private boolean useNegated;

    public KeywordCriteriaDTO() {
    }

    public KeywordCriteriaDTO(String keyword, boolean useRegex, boolean useLiteral,
                              boolean caseSensitive, boolean useNegated) {
        this.keyword = keyword;
        this.useRegex = useRegex;
        this.useLiteral = useLiteral;
        this.caseSensitive = caseSensitive;
        this.useNegated = useNegated;
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

    public boolean isUseNegated() {
        return useNegated;
    }

    public void setUseNegated(boolean useNegated) {
        this.useNegated = useNegated;
    }
}
