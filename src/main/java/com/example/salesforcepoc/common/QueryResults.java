package com.example.salesforcepoc.common;

import java.util.List;

public final class QueryResults {
    private final List<String> productIds;
    private final Integer matchingResultsCount;

    public QueryResults(List<String> productIds, Integer matchingResultsCount) {
        this.productIds = productIds;
        this.matchingResultsCount = matchingResultsCount;
    }

    public List<String> getProductIds() {
        return productIds;
    }

    public Integer getMatchingResultsCount() {
        return matchingResultsCount;
    }
}