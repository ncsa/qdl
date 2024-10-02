package org.qdl_lang.vfs;

import org.qdl_lang.exceptions.QDLIOException;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class VFSMemoryDirectoryEntry extends HashMap<String, VFSMetaEntry> implements VFSMetaEntry {
    @Override
    public boolean isDirectory() {
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    /**
     * Takes a relative path, e.g. a/b/c/foo.txt and returns the entry <b>or</b>
     * if the entry is a directory, returns that.
     *
     * @param relativePath
     * @return
     */
    public VFSMetaEntry getEntry(String relativePath) {
        StringTokenizer stringTokenizer = new StringTokenizer(relativePath, VFSPaths.PATH_SEPARATOR);
        // Assumption is that this relative
        VFSMemoryDirectoryEntry entry = this;
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            if (entry.containsKey(token)) {
                VFSMetaEntry metaEntry = entry.get(token);
                if (!metaEntry.isDirectory()) {
                    return metaEntry;
                }
                entry = (VFSMemoryDirectoryEntry) metaEntry;
            }
        }
        return entry;
    }

    public VFSMetaEntry putEntry(String relativePath, VFSMetaEntry value) {
        if(value.isDirectory()){
            throw new QDLIOException("cannot put a directory '" + relativePath + "'");
        }
        StringTokenizer stringTokenizer = new StringTokenizer(relativePath, VFSPaths.PATH_SEPARATOR);
        VFSMemoryDirectoryEntry entry = this;
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            if (stringTokenizer.hasMoreTokens()) {
                // then this is not the last one
                VFSMetaEntry metaEntry = entry.get(token);
                if (metaEntry == null || !metaEntry.isDirectory()) {
                    throw new QDLIOException("directory '" + token + "' in '" + relativePath + "' does not exist");
                }
                entry = (VFSMemoryDirectoryEntry) metaEntry;
            } else {
                return entry.put(token, value);
            }
        }
        throw new QDLIOException("No path for '" + relativePath + "'");
    }

    public void mkdirs(String relativePath) {
        StringTokenizer stringTokenizer = new StringTokenizer(relativePath, VFSPaths.PATH_SEPARATOR);
        VFSMemoryDirectoryEntry entry = this;
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            if (entry.containsKey(token)) {
                VFSMetaEntry metaEntry = entry.get(token);
                if (!metaEntry.isDirectory()) {
                    throw new QDLIOException("directory entry '" + token + "' already exists");
                }
                entry = (VFSMemoryDirectoryEntry) metaEntry;
            } else {
                VFSMemoryDirectoryEntry x = new VFSMemoryDirectoryEntry();
                x.setName(token);
                entry.put(token, x);
                entry = x;
            }
        }
    }

    public boolean isDirectory(String relativePath) {
        StringTokenizer stringTokenizer = new StringTokenizer(relativePath, VFSPaths.PATH_SEPARATOR);
        VFSMemoryDirectoryEntry entry = this;
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            if (entry.containsKey(token)) {
                VFSMetaEntry metaEntry = entry.get(token);
                if (!metaEntry.isDirectory()) {
                    return false;
                }
                entry = (VFSMemoryDirectoryEntry) metaEntry;
            }else{
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the file from the store
     * @param relativePath
     */
    public void rm(String relativePath) {
        StringTokenizer stringTokenizer = new StringTokenizer(relativePath, VFSPaths.PATH_SEPARATOR);
        VFSMemoryDirectoryEntry entry = this;
        String token = stringTokenizer.nextToken();
        if (stringTokenizer.hasMoreTokens()) {
            throw new QDLIOException(relativePath + " is not a file");
        }
       if (entry.containsKey(token)) {
           entry.remove(token);
       }
    }
    public void rmDirectory(String relativePath) {
        StringTokenizer stringTokenizer = new StringTokenizer(relativePath, VFSPaths.PATH_SEPARATOR);
        VFSMemoryDirectoryEntry entry = this;
        String token = stringTokenizer.nextToken();
        if (stringTokenizer.hasMoreTokens()) {
            throw new QDLIOException(relativePath + " is not a single directory");
        }
        if (entry.containsKey(token)) {
            VFSMetaEntry metaEntry = entry.get(token);
            if(!metaEntry.isDirectory()) {
                throw new QDLIOException(relativePath + " is not a directory");
            }
            VFSMemoryDirectoryEntry x = (VFSMemoryDirectoryEntry) metaEntry;
            if(!x.isEmpty()){
                throw new QDLIOException(relativePath + " is not empty");
            }
            x.remove(token);
        }
    }
}
