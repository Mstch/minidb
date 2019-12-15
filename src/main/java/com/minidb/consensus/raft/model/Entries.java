package com.minidb.consensus.raft.model;

import java.util.Map;

public class Entries {

    public Map<Integer, Entry> indexedEntries;
    public Integer lastIndex;

    public Entries(Map<Integer, Entry> indexedEntries, Integer lastIndex) {
        this.indexedEntries = indexedEntries;
        this.lastIndex = lastIndex;
    }

    public Log append(Log log) {
        Entry entry = log.store();
        indexedEntries.put(entry.index, entry);
        return log;
    }

    //TODO O1 的fetch
    public Entry[] fetchLogs(Integer start, Integer end) {
        return indexedEntries.values()
                .stream()
                .filter(entry -> entry.index >= start && entry.index <= end)
                .toArray(Entry[]::new);
    }

    public class Entry {
        public Integer index;
        public Integer term;
        public Log log;

        public Entry(Integer index, Integer term, Log log) {
            this.index = index;
            this.term = term;
            this.log = log;
        }
    }
}
