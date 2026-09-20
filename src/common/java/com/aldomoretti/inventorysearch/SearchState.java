package com.aldomoretti.inventorysearch;

public final class SearchState {
    private static final SearchState INSTANCE = new SearchState();

    private String currentQuery = "";

    private SearchState() {}

    public static SearchState getInstance() {
        return INSTANCE;
    }

    public void setQuery(String query) {
        this.currentQuery = query == null ? "" : query;
    }

    public String getQuery() {
        return this.currentQuery;
    }

    public boolean isActive() {
        return !this.currentQuery.isBlank();
    }

    public void clear() {
        this.currentQuery = "";
    }
}
