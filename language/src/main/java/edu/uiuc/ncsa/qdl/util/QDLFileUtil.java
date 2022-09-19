package edu.uiuc.ncsa.qdl.util;

import edu.uiuc.ncsa.qdl.evaluate.AbstractEvaluator;
import edu.uiuc.ncsa.qdl.exceptions.QDLFileAccessException;
import edu.uiuc.ncsa.qdl.exceptions.QDLFileNotFoundException;
import edu.uiuc.ncsa.qdl.exceptions.QDLServerModeException;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.qdl.variables.StemUtility;
import edu.uiuc.ncsa.qdl.vfs.FileEntry;
import edu.uiuc.ncsa.qdl.vfs.VFSEntry;
import edu.uiuc.ncsa.qdl.vfs.VFSFileProvider;
import edu.uiuc.ncsa.qdl.vfs.VFSPaths;
import edu.uiuc.ncsa.qdl.workspace.WorkspaceCommands;
import edu.uiuc.ncsa.security.core.configuration.XProperties;
import edu.uiuc.ncsa.security.core.util.FileUtil;
import edu.uiuc.ncsa.security.core.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Bunch of file reading and writing utilities so I don't have to boiler plate this stuff
 * Note that these are all VFS aware and server mode aware, so exceptions are
 * thrown if, e.g., a read attempt is made of a native file in server mode.
 * <h2>Main functions</h2>
 * <ul>
 *     <li>{@link #readBinaryFile(State, String)}  - returns byte array</li>
 *     <li>{@link #writeBinaryFile(State, String, byte[])}  -- writes a byte array</li>
 *     <li>{@link #readTextFile(State, String)}  - reads a text file as a string</li>
 *     <li>{@link #readTextFileAsLines(State, String)}  - reads a text file as an array of strings. </li>
 *     <li>{@link #readTextFileAsStem(State, String)}  - reads a text file as a stem list. </li>
 *     <li>{@link #writeTextFile(State, String, String)}</li>
 *     <li>{@link #readAttributes(State, String)} - get the attributes for a file (length, name, path...)</li>
 *     
 * </ul>
 * These are a facade for several calls. Note that for all workspace programming you should use these
 * <p>Created by Jeff Gaynor<br>
 * on 1/29/20 at  9:52 AM
 */
public class QDLFileUtil extends FileUtil {

    public static QDLStem readFileAsStem(String fileName) throws Throwable {
        checkFile(fileName);
        QDLStem out = new QDLStem();
        Path path = Paths.get(fileName);

        List<String> contents = Files.readAllLines(path);
        int i = 0;
        //Read from the stream
        for (String content : contents) {
            out.put(Integer.toString(i++), content);
        }

        return out;
    }

    /**
     * Main entry point for reading a text file as lines. This is VFS aware.
     *
     * @param state
     * @param fullPath
     * @return
     * @throws Throwable
     */
    public static List<String> readTextFileAsLines(State state, String fullPath) throws Throwable {
        if (isVFSPath(fullPath)) {
            String x = readTextVFS(state, fullPath);
            if(x == null){
                return null;
            }
          return  StringUtils.stringToList(x);
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("unsupported in server mode");
        }
        return readFileAsLines(fullPath);
    }

    /**
     * Main entry point for reading a text file as lines
     * @param state
     * @param fullPath
     * @return
     * @throws Throwable
     */
    public static String readTextFile(State state, String fullPath) throws Throwable {
        if (isVFSPath(fullPath)) {
            readTextVFS(state, fullPath);
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("unsupported in server mode");
        }
        return readFileAsString(fullPath);
    }

    /**
     * main entry point for reading a text file as a stem.
     * @param state
     * @param fullPath
     * @return
     * @throws Throwable
     */
    public static QDLStem readTextFileAsStem(State state, String fullPath) throws Throwable {
        QDLStem stem = new QDLStem();
        stem.addList(readTextFileAsLines(state, fullPath));
        return stem;
    }


    /**
     * Main entry point for reading a binary file.
     * @param state
     * @param fullPath
     * @return
     * @throws Throwable
     */
    public static byte[] readBinaryFile(State state, String fullPath) throws Throwable {
        if (isVFSPath(fullPath)) {
            readBinaryVFS(state, fullPath);
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("unsupported in server mode");
        }
        return Files.readAllBytes(Paths.get(fullPath));
    }

    /**
     * Main entry point for writing a binary file
     * @param state
     * @param fullPath
     * @param bytes
     * @throws Throwable
     */
    public static void writeBinaryFile(State state, String fullPath, byte[] bytes) throws Throwable {
        if (isVFSPath(fullPath)) {
            writeBinaryVFS(state, fullPath, bytes);
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("unsupported in server mode");
        }
        Files.write(Paths.get(fullPath), bytes);
    }

    public static void writeTextFile(State state, String fullPath, String contents) throws Throwable{
        if (isVFSPath(fullPath)) {
            writeTextVFS(state, fullPath, contents);
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("unsupported in server mode");
        }
        Files.writeString(new File(fullPath).toPath(), contents);
    }


    public static void writeTextFile(State state, String fullPath, List<String> contents) throws Throwable{
        if (isVFSPath(fullPath)) {
            writeTextVFS(state, fullPath, StringUtils.listToString(contents));
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("unsupported in server mode");
        }
        Files.write(new File(fullPath).toPath(), contents);
    }

    /**
     * Note that this is a stem list or the ouotput is random.
     *
     * @param filename
     */
    public static void writeStemToFile(String filename, QDLStem contents) throws Throwable {
        FileWriter fileWriter = new FileWriter(new File(filename));
        fileWriter.write(StemUtility.stemListToString(contents, true));
        fileWriter.flush();
        fileWriter.close();
    }

    /**
     * Write the set of bytes to the given VFS file. Unlike using the file_write API, this handles
     * the bytes directly -- no base64 encoding, hence allows for dealing with things like pass through
     * files that were written by another application and are really just blobs of bytes.
     *
     * @param state
     * @param path
     * @param bytes
     */
    public static void writeBinaryVFS(State state, String path, byte[] bytes) throws Throwable {
        VFSFileProvider vfs = getVfsFileProvider(state, path);
        FileEntry fileEntry = new FileEntry(bytes);
        vfs.put(path, fileEntry);
    }

    private static VFSFileProvider getVfsFileProvider(State state, String path) throws Throwable {
        VFSFileProvider vfs = state.getVFS(path);
        if (vfs == null) {
            throw new QDLFileNotFoundException("no VFS associated with file '" + path + "'");
        }
        return vfs;
    }

    public static void writeTextVFS(State state, String path, String content) throws Throwable {
        VFSFileProvider vfs = getVfsFileProvider(state, path);
        FileEntry fileEntry = new FileEntry(content);
        vfs.put(path, fileEntry);
    }

    /**
     * Read the bytes from the VFS file. Unlike the file_read API, this just writes
     * the bytes directly -- no base 64 encoding.
     *
     * @param state
     * @param path
     * @return
     */
    public static byte[] readBinaryVFS(State state, String path) throws Throwable {
        VFSFileProvider vfs = getVfsFileProvider(state, path);
        VFSEntry vfsEntry = vfs.get(path, AbstractEvaluator.FILE_OP_BINARY);
        if(vfsEntry == null){
            throw new QDLFileNotFoundException();
        }
        return vfsEntry.getBytes();
    }

    public static String readTextVFS(State state, String path) throws Throwable {
        VFSFileProvider vfs = getVfsFileProvider(state, path);
        VFSEntry vfsEntry = vfs.get(path, AbstractEvaluator.FILE_OP_TEXT_STRING);
        if(vfsEntry == null){
            throw new QDLFileNotFoundException();
        }
        return vfsEntry.getText();
    }

    public static boolean isAbsolute(String path) {
        if (VFSPaths.isVFSPath(path)) {
            return VFSPaths.isAbsolute(path);
        }
        return (new File(path)).isAbsolute();
    }

    public static boolean isVFSPath(String path) {
        return VFSPaths.isVFSPath(path);
    }

    public static boolean exists(State state, String path) throws Throwable {
        if (isVFSPath(path)) {
            VFSFileProvider vfs = getVfsFileProvider(state, path);
            if (vfs == null) {
                return false;
            }
            return vfs.contains(path);
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("This operation is unsupported in server mode.");
        }

        return (new File(path)).exists();
    }

    /**
     * Resolves paths against a parent if the target is relative.
     *
     * @param parent
     * @param target
     * @return
     */
    public static String resolvePath(String parent, String target) {
        if (isAbsolute(target)) {
            return target;
        }
        if (isVFSPath(parent)) {
            return VFSPaths.resolve(parent, target);
        } else {
            if (isVFSPath(target)) {
                throw new QDLFileAccessException("Cannot resolve VFS path " + target + " against " + parent);
            }

            return (new File(parent, target)).getAbsolutePath();
        }
    }

    public static boolean isDirectory(State state, String path) throws Throwable {
        if (isVFSPath(path)) {
            VFSFileProvider vfs = getVfsFileProvider(state, path);
            if (vfs == null) {
                return false;
            }
            return vfs.isDirectory(path); // if it contains the path
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("This operation is unsupported in server mode.");
        }
        return (new File(path)).isDirectory();
    }

    public static boolean canRead(State state, String path) throws Throwable {
        if (isVFSPath(path)) {
            VFSFileProvider vfs = getVfsFileProvider(state, path);
            if (vfs == null) {
                return false;
            }
            return vfs.canRead(); // if it contains the path
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("This operation is unsupported in server mode.");
        }
        return (new File(path)).canRead();
    }

    public static long length(State state, String path) throws Throwable {
        if (isVFSPath(path)) {
            VFSFileProvider vfs = getVfsFileProvider(state, path);
            if (vfs == null) {
                return 0;
            }
            return vfs.length(path); // if it contains the path
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("This operation is unsupported in server mode.");
        }
        return (new File(path)).length();
    }

    public static String[] dir(State state, String path, WorkspaceCommands.RegexFileFilter regexFileFilter) throws Throwable {
        if (isVFSPath(path)) {
            VFSFileProvider vfs = getVfsFileProvider(state, path);
            if (vfs == null) {
                return new String[]{};
            }

            String[] elements = vfs.dir(path); // if it contains the path
            if (regexFileFilter == null) {
                return elements;
            }
            ArrayList<String> found = new ArrayList<>();
            for (String x : elements) {
                if (regexFileFilter.accept(null, x)) {    // NOTE this really only uses a regex filter, so first arg is ignored.
                    found.add(x);
                }
            }

            String[] out = new String[found.size()];
            found.toArray(out);
            return out;
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("This operation is unsupported in server mode.");
        }
        if (regexFileFilter == null) {
            return (new File(path)).list();
        } else {
            return (new File(path)).list(regexFileFilter);
        }
    }

    public static String[] dir(State state, String path) throws Throwable {
        return dir(state, path, null);
    }

    public static class FileAttributes {
        public long timestamp = 0L;
        public long length = 0L;
        public String name;
        public String parent;
        /*
                        currentFile = new File(fullPath);
                wsLibEntry.ts = new Date(currentFile.lastModified());
                wsLibEntry.lastSaved_ts = wsLibEntry.ts;
                wsLibEntry.fileFormat = "QDL";
                wsLibEntry.length = currentFile.length();
                wsLibEntry.filename = currentFile.getName();
                wsLibEntry.filepath = currentFile.getParent();
         */
    }

    public static FileAttributes readAttributes(State state, String fullPath) throws Throwable {
        if (isVFSPath(fullPath)) {
            VFSFileProvider vfs = getVfsFileProvider(state, fullPath);
            if (vfs == null) {
                throw new QDLFileNotFoundException(fullPath + " not found");
            }
            FileAttributes fileAttributes = new FileAttributes();
            fileAttributes.length = vfs.length(fullPath);
            fileAttributes.name = VFSPaths.getFileName(fullPath);
            fileAttributes.parent = VFSPaths.getParentPath(fullPath);
            VFSEntry vfsEntry = vfs.get(fullPath, 0);
            XProperties xp = vfsEntry.getProperties();
            //fileAttributes.timestamp = vfs.
            return fileAttributes;
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("This operation is unsupported in server mode.");
        }
        FileAttributes fileAttributes = new FileAttributes();
        File f = new File(fullPath);
        fileAttributes.length = f.length();
        fileAttributes.name = f.getName();
        fileAttributes.parent = f.getParent();
        fileAttributes.timestamp = f.lastModified();
        return fileAttributes;


    }

    public static InputStream readFileAsInputStream(State state, String fullPath) throws Throwable{
        if(isVFSPath(fullPath)){
            return new ByteArrayInputStream(readBinaryVFS(state, fullPath));
        }
        if (state.isServerMode()) {
             throw new QDLServerModeException("This operation is unsupported in server mode.");
         }
        return new FileInputStream(new File(fullPath));
    }

      public static void copy(State state, String source, String target) throws Throwable {
        writeBinaryFile(state, target, readBinaryFile(state, source));
      }
    public static void main(String[] args) {
        System.out.println(resolvePath("/a/b/c", "p/q"));
        System.out.println(resolvePath("vfs#/a/b/c", "p/q"));
        System.out.println(resolvePath("vfs#/a/b/c", "abc#p/q"));
        System.out.println(resolvePath("/a/b/c", "abc#p/q"));
    }
}
