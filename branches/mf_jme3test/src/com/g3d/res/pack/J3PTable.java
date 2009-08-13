package com.g3d.res.pack;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class J3PTable implements Map<NamedEntry, J3PEntry> {

    private J3PEntry[] entries;

    J3PTable(J3PEntry[] entries){
        if (entries == null)
            this.entries = new J3PEntry[0];
        else
            this.entries = entries;
    }

    J3PTable(){
        entries = null;
    }

    void init(int entryCount){
        entries = new J3PEntry[entryCount];
    }

    void setEntry(int index, J3PEntry val){
        entries[index] = val;
    }
    
    public int size() {
        return entries.length;
    }

    public boolean isEmpty() {
        return entries.length > 0;
    }

    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public J3PEntry get(Object key) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public J3PEntry put(NamedEntry key, J3PEntry value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public J3PEntry remove(Object key) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public void putAll(Map<? extends NamedEntry, ? extends J3PEntry> m) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Not supported.");
    }

    private class KeySet implements Set<NamedEntry> {

        public int size() {
            return entries.length;
        }

        public boolean isEmpty() {
            return entries.length > 0;
        }

        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public Iterator<NamedEntry> iterator() {
            return new Iterator<NamedEntry>(){
                private int next = 0;
                public boolean hasNext() {
                    return next <= entries.length - 1;
                }
                public NamedEntry next() {
                    return entries[next];
                }
                public void remove() {
                    throw new UnsupportedOperationException("Not supported.");
                }
            };
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported.");
        }
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public boolean add(NamedEntry e) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public boolean addAll(Collection<? extends NamedEntry> c) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public void clear() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    private class Values implements Collection<J3PEntry> {

        public int size() {
            return entries.length;
        }

        public boolean isEmpty() {
            return entries.length > 0;
        }

        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public Iterator<J3PEntry> iterator() {
            return new Iterator<J3PEntry>(){
                private int next = 0;
                public boolean hasNext() {
                    return next <= entries.length - 1;
                }
                public J3PEntry next() {
                    return entries[next];
                }
                public void remove() {
                    throw new UnsupportedOperationException("Not supported.");
                }
            };
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported.");
        }
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public boolean add(J3PEntry e) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public boolean addAll(Collection<? extends J3PEntry> c) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }
        public void clear() {
            throw new UnsupportedOperationException("Not supported.");
        }

    }

    private class J3PTableEntry implements Entry<Integer, J3PEntry> {

        private J3PEntry entry;

        public Integer getKey() {
            return entry.hash;
        }

        public J3PEntry getValue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public J3PEntry setValue(J3PEntry value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private class EntrySet implements Set<Entry<NamedEntry, J3PEntry>> {

        public int size() {
            return entries.length;
        }

        public boolean isEmpty() {
            return entries.length > 0;
        }

        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public Iterator<Entry<NamedEntry, J3PEntry>> iterator() {
            return new Iterator<Entry<NamedEntry, J3PEntry>>(){
                private int next = 0;
                public boolean hasNext() {
                    return next <= entries.length - 1;
                }
                public Entry<NamedEntry, J3PEntry> next() {
                    return new Entry<NamedEntry, J3PEntry>() {
                        public NamedEntry getKey() {
                            return entries[next];
                        }
                        public J3PEntry getValue() {
                            return entries[next];
                        }
                        public J3PEntry setValue(J3PEntry value) {
                            throw new UnsupportedOperationException("Not supported.");
                        }
                    };
                }
                public void remove() {
                    throw new UnsupportedOperationException("Not supported.");
                }
            };
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported.");
        }

        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public boolean add(Entry<NamedEntry, J3PEntry> e) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public boolean addAll(Collection<? extends Entry<NamedEntry, J3PEntry>> c) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public void clear() {
            throw new UnsupportedOperationException("Not supported.");
        }

    }

    public Set<NamedEntry> keySet() {
        return new KeySet();
    }

    public Collection<J3PEntry> values() {
        return new Values();
    }

    public Set<Entry<NamedEntry, J3PEntry>> entrySet() {
        return new EntrySet();
    }

}
