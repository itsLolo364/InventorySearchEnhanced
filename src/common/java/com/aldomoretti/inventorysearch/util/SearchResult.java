package com.aldomoretti.inventorysearch.util;

import java.util.Collections;
import java.util.List;

public class SearchResult {
    public final int slotIndex;
    public final boolean isContainer;
    public final boolean isDirectMatch;
    public final ContainerType containerType;
    public final List<String> containedItemNames;
    public final List<Integer> matchedSlotIndices;
    public final int matchCount;

    public SearchResult(
            int slotIndex,
            boolean isContainer,
            boolean isDirectMatch,
            ContainerType containerType,
            List<String> containedItemNames,
            List<Integer> matchedSlotIndices,
            int matchCount
    ) {
        this.slotIndex = slotIndex;
        this.isContainer = isContainer;
        this.isDirectMatch = isDirectMatch;
        this.containerType = containerType;
        this.containedItemNames = containedItemNames;
        this.matchedSlotIndices = matchedSlotIndices == null ? List.of() : List.copyOf(matchedSlotIndices);
        this.matchCount = matchCount;
    }

    public static SearchResult directMatch(int slotIndex) {
        return new SearchResult(slotIndex, false, true, ContainerType.NONE, List.of(), List.of(), 0);
    }

    public static SearchResult containerMatch(
            int slotIndex,
            ContainerType containerType,
            List<String> containedItemNames,
            List<Integer> matchedSlotIndices,
            int matchCount
    ) {
        return new SearchResult(slotIndex, true, false, containerType, containedItemNames, matchedSlotIndices, matchCount);
    }

    public static SearchResult directAndContainerMatch(
            int slotIndex,
            ContainerType containerType,
            List<String> containedItemNames,
            List<Integer> matchedSlotIndices,
            int matchCount
    ) {
        return new SearchResult(slotIndex, true, true, containerType, containedItemNames, matchedSlotIndices, matchCount);
    }
}
