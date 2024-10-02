package org.qdl_lang.vfs;

import org.qdl_lang.config.QDLConfigurationConstants;
import org.qdl_lang.exceptions.QDLIOException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A VFS backed by a hash table. NOTE that the entries to this are the complete path. So
 * creating a directory really has no effect.
 * <p>Created by Jeff Gaynor<br>
 * on 3/4/20 at  11:31 AM
 */
public class VFSMemoryFileProvider extends AbstractVFSFileProvider {
    @Override
    public String getType() {
        return QDLConfigurationConstants.VFS_TYPE_MEMORY;
    }

    public VFSMemoryFileProvider(String scheme, String mountPoint, boolean canRead, boolean canWrite) {
        super(scheme, mountPoint, canRead, canWrite);
    }


    VFSMemoryDirectoryEntry map = new VFSMemoryDirectoryEntry();

    @Override
    public VFSEntry get(String path, int type) throws Throwable {
        super.get(path, type);
        VFSMetaEntry metaEntry = map.getEntry(getRealPath(path));
        if (metaEntry.isDirectory()) {
            throw new QDLIOException(path + " is a directory");
        }
        return (VFSEntry) metaEntry;
    }

    @Override
    public void put(String newPath, VFSEntry entry) throws Throwable {
        super.put(newPath, entry);
        String rPath = getRealPath(newPath);
        entry.setPath(VFSPaths.normalize(newPath));
        map.putEntry(rPath, entry);
    }

    @Override
    public void delete(String path) throws Throwable {
        super.delete(path);
        map.rm(getRealPath(path));
    }

    @Override
    public boolean contains(String path) throws Throwable {
        super.contains(path);
        VFSMetaEntry entry = map.getEntry(getRealPath(path));
        return entry != null;
        //return map.containsKey(getRealPath(path));
    }

    @Override
    public String[] dir(String path) throws Throwable {
        super.dir(path);
        VFSMetaEntry metaEntry = map.getEntry(getRealPath(path));
        if(!metaEntry.isDirectory()) {
            throw new QDLIOException(path + " is not a directory");
        }
        VFSMemoryDirectoryEntry dir = (VFSMemoryDirectoryEntry) metaEntry;
        ArrayList<String> fileList = new ArrayList<>();
        for(String key : dir.keySet()){
            VFSMetaEntry x = dir.getEntry(key);
            if(x.isDirectory()){
                fileList.add(key);
            }else{
                fileList.add(key + (key.endsWith("/") ? "" : "/"));
            }
        }
        String[] output = new String[fileList.size()];
        for (int i = 0; i < output.length; i++) {
            output[i] = fileList.get(i);
        }
        return output;
    }
    public String[] OLDdir(String path) throws Throwable {
        super.dir(path);

        ArrayList<String> fileList = new ArrayList<>();
        String realPath = getRealPath(path);

        for (String key : map.keySet()) {
            if (VFSPaths.startsWith(key, realPath)) {
                fileList.add(key);
            }
        }
        String[] output = new String[fileList.size()];
        for (int i = 0; i < output.length; i++) {
            output[i] = fileList.get(i);
        }
        return output;
    }

    @Override
    public boolean mkdir(String path) {
        super.mkdir(path);
        map.mkdirs(getRealPath(path));
        return true;
    }

    @Override
    public boolean rmdir(String path) throws Throwable {
        super.rmdir(path);

        String realPath = getRealPath(path);
        map.rmDirectory(realPath);
        // The logic is that entries in the map are of the form /a/b/c/.../file
        // so you cannot remove a directory since it doesn't really exist. Best you can do
        // is tell if there are no more files with that path.
        //return !map.containsKey(realPath);
   return true;
    }

    @Override
    public void rm(String path) throws Throwable {
        super.rm(path);
        String realPath = getRealPath(path);
        map.rm(realPath);
    }

    @Override
    public boolean isDirectory(String path) {
        super.isDirectory(path);
        String relativePath = getRealPath(path);
        return map.isDirectory(relativePath);
/*
        relativePath =relativePath + (relativePath.endsWith(VFSPaths.PATH_SEPARATOR)?"":VFSPaths.PATH_SEPARATOR);
        for(String key : map.keySet()) {
            if(key.startsWith(relativePath)){return true;}
        }
        return false;
*/
        //return map.containsKey(getRealPath(path));
    }
}
/*
   cfg.type :='memory';
   cfg.scheme := 'ram-disk';
   cfg.mount_point := '/vfs/cache';
   cfg.access := 'rw';
   vfs_mount(cfg.);

    file_write('ram-disk#/vfs/cache/foo.txt', 'woof')
    dir('ram-disk#/vfs/cache/')
    file_read('ram-disk#/vfs/cache/foo.txt')

  )save ram-disk#/vfs/cache/a/b/c/test         0

 */
