package org.qdl_lang.evaluate;

import org.qdl_lang.exceptions.*;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.parsing.IniParserDriver;
import org.qdl_lang.state.State;
import org.qdl_lang.util.InputFormUtil;
import org.qdl_lang.util.QDLFileUtil;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLList;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.QDLStem;
import edu.uiuc.ncsa.security.core.configuration.XProperties;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import org.qdl_lang.variables.values.BooleanValue;
import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.vfs.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.qdl_lang.config.QDLConfigurationConstants.*;
import static org.qdl_lang.config.QDLConfigurationLoaderUtils.setupMySQLDatabase;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;
import static org.qdl_lang.vfs.VFSPaths.PATH_SEPARATOR;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  9:18 AM
 */
public class IOEvaluator extends AbstractEvaluator {

    public static final String IO_NAMESPACE = "io";
    public static final int IO_FUNCTION_BASE_VALUE = 4000;


    public static final String SCAN_FUNCTION = "scan";
    public static final int SCAN_TYPE = 2 + IO_FUNCTION_BASE_VALUE;

    public static final String READ_FILE = "file_read";
    public static final int READ_FILE_TYPE = 3 + IO_FUNCTION_BASE_VALUE;

    public static final String WRITE_FILE = "file_write";
    public static final int WRITE_FILE_TYPE = 4 + IO_FUNCTION_BASE_VALUE;

    public static final String DIR = "dir";
    public static final int DIR_TYPE = 5 + IO_FUNCTION_BASE_VALUE;

    public static final String MKDIR = "mkdir";
    public static final int MKDIR_TYPE = 6 + IO_FUNCTION_BASE_VALUE;

    public static final String RMDIR = "rmdir";
    public static final int RMDIR_TYPE = 7 + IO_FUNCTION_BASE_VALUE;

    public static final String RM_FILE = "rm";
    public static final int RM_FILE_TYPE = 8 + IO_FUNCTION_BASE_VALUE;

    public static final String VFS_MOUNT = "vfs_mount";
    public static final int VFS_MOUNT_TYPE = 100 + IO_FUNCTION_BASE_VALUE;


    public static final String VFS_UNMOUNT = "vfs_unmount";
    public static final int VFS_UNMOUNT_TYPE = 101 + IO_FUNCTION_BASE_VALUE;

    @Override
    public String getNamespace() {
        return IO_NAMESPACE;
    }


    @Override
    public String[] getFunctionNames() {

        if (fNames == null) {
            fNames = new String[]{
                    SCAN_FUNCTION,
                    READ_FILE,
                    WRITE_FILE,
                    RM_FILE,
                    MKDIR,
                    RMDIR,
                    DIR,
                    VFS_MOUNT,
                    VFS_UNMOUNT};
        }
        return fNames;
    }


    @Override
    public int getType(String name) {
        switch (name) {

            case SCAN_FUNCTION:
                return SCAN_TYPE;
            case DIR:
                return DIR_TYPE;
            case MKDIR:
                return MKDIR_TYPE;
            case RM_FILE:
                return RM_FILE_TYPE;
            case RMDIR:
                return RMDIR_TYPE;
            case READ_FILE:
                return READ_FILE_TYPE;
            case WRITE_FILE:
                return WRITE_FILE_TYPE;
            case VFS_MOUNT:
                return VFS_MOUNT_TYPE;
            case VFS_UNMOUNT:
                return VFS_UNMOUNT_TYPE;
        }
        return EvaluatorInterface.UNKNOWN_VALUE;
    }


    public boolean dispatch(Polyad polyad, State state) {
        switch (polyad.getName()) {

            case SCAN_FUNCTION:
                doScan(polyad, state);
                return true;
            case DIR:
                doDir(polyad, state);
                return true;
            case MKDIR:
                doMkDir(polyad, state);
                return true;
            case RM_FILE:
                doRMFile(polyad, state);
                return true;
            case RMDIR:
                doRMDir(polyad, state);
                return true;
            case READ_FILE:
                doReadFile(polyad, state);
                return true;
            case WRITE_FILE:
                doWriteFile(polyad, state);
                return true;
            case VFS_MOUNT:
                vfsMount(polyad, state);
                return true;
            case VFS_UNMOUNT:
                vfsUnmount(polyad, state);
                return true;
        }
        return false;
    }


    protected void doScan(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("scan is not allowed in server mode.");
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(SCAN_FUNCTION + " requires at most 1 argument", polyad.getArgAt(1));
        }
        /*
        while[true][if[scan('ok?') == 'yes'][return();];]
         */
        if (polyad.getArgCount() != 0) {
            // This is the prompt.
            state.getIoInterface().print(polyad.evalArg(0, state));
            //System.out.print(polyad.evalArg(0, state));
        }
        //BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String result = "";
        try {
            result = state.getIoInterface().readline();
            state.getIoInterface().flush(); // Fixes Github issue #12.
        } catch (IOException e) {
            result = "(error)";
        }
        polyad.setResult(result);
        polyad.setEvaluated(true);
    }


    protected void doRMDir(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(RMDIR + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(RMDIR + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Object obj = polyad.evalArg(0, state);
        checkNull(obj, polyad.getArgAt(0));
        if (!isString(obj)) {
            throw new BadArgException(RMDIR + " requires a string for its first argument.", polyad.getArgAt(0));
        }
        String fileName = obj.toString();

        VFSFileProvider vfs = null;
        Boolean rc = false;
        if (state.isVFSFile(fileName)) {
            try {
                vfs = state.getVFS(fileName);
                if (vfs != null) {
                    rc = vfs.rmdir(fileName);
                }

            } catch (Throwable throwable) {
                throw new QDLIOException("Error; Could not resolve virtual file system for '" + fileName + "'");
            }
        } else {
            // So its just a file.
            if (state.isServerMode()) {
                throw new QDLServerModeException("File system operations not permitted in server mode.");
            }
            File f = new File(fileName);
            if (!f.isDirectory()) {
                throw new QDLIOException("the requested object '" + f + "' is not a directory on this system.");
            }
            if (f.list() != null && f.list().length != 0) {
                throw new QDLIOException("The directory '" + f + "' is not empty.");
            }
            rc = f.delete();
        }
        polyad.setEvaluated(true);
        polyad.setResult(rc);
    }

    protected void doRMFile(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(RM_FILE + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(RM_FILE + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Object obj = polyad.evalArg(0, state);
        checkNull(obj, polyad.getArgAt(0));
        if (!isString(obj)) {
            throw new BadArgException(RM_FILE + " requires a string for its argument.", polyad.getArgAt(0));
        }
        String fileName = obj.toString();

        VFSFileProvider vfs = null;
        Boolean rc = false;
        if (state.isVFSFile(fileName)) {
            try {
                vfs = state.getVFS(fileName);
                if (vfs != null) {
                    vfs.rm(fileName);
                    rc = true;
                }

            } catch (Throwable throwable) {
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }
                throw new QDLIOException("Error; Could not resolve virtual file system for '" + fileName + "'");
            }
        } else {
            // So its just a file.
            if (state.isServerMode()) {
                throw new QDLServerModeException("File system operations not permitted in server mode.");
            }
            File f = new File(fileName);
            if (f.exists()) {
                if (!f.isFile()) {
                    throw new QDLIOException("the requested object '" + f + "' is not a file on this system.");
                }
            } else {
                // Contract is that if it does not exist, return true.
                polyad.setEvaluated(true);
                polyad.setResult(rc);
                return;
                // it exists and is not some sort of file, so an error is warranted.
            }
            rc = f.delete();
        }
        polyad.setEvaluated(true);
        polyad.setResult(rc);
    }

    protected void doMkDir(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(MKDIR + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(MKDIR + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Object obj = polyad.evalArg(0, state);
        checkNull(obj, polyad.getArgAt(0));
        if (!isString(obj)) {
            throw new BadArgException(MKDIR + " requires a string for its argument.", polyad.getArgAt(0));
        }
        DebugUtil.trace(this, "in " + MKDIR + ": starting, arg = " + obj);
        String fileName = obj.toString();

        VFSFileProvider vfs = null;
        Boolean rc = false;
        boolean hasVF = false;
        if (state.isVFSFile(fileName)) {
            try {
                vfs = state.getVFS(fileName);
                if (vfs != null) {
                    rc = vfs.mkdir(fileName);
                } else {
                    DebugUtil.trace(this, "in " + MKDIR + ": NO VFS");

                }

            } catch (Throwable throwable) {
                DebugUtil.trace(this, "in " + MKDIR + ": got exception \"" + throwable.getMessage() + "\"");
                state.getLogger().error("in " + MKDIR + ", got exception", throwable);

                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }
                throw new QDLIOException("Error; Could not resolve virtual file system for '" + fileName + "'");
            }
        } else {
            // So its just a file.
            if (state.isServerMode()) {
                throw new QDLServerModeException("File system operations not permitted in server mode.");
            }
            File f = new File(fileName);
            rc = f.mkdirs();
        }
        polyad.setEvaluated(true);
        polyad.setResult(rc);

    }

    /**
     * Quick note. This is a directory command. That means that it will list the things in a directory.
     * This is not like, e.g., the unix ls command. The ls command <i>lists</i> information so<br/><br/>
     * <code>ls filename</code>
     * <br/><br/>
     * returns the file name. This command returns what is in the directory. So <code>dir(filename)</code>
     * returns null since the name is not a directory.
     *
     * @param polyad
     * @param state
     */
    protected void doDir(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(DIR + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(DIR + " requires at most 1 argument", polyad.getArgAt(1));
        }

        DebugUtil.trace(this, "starting " + DIR + " command");
        Object obj = polyad.evalArg(0, state);
        checkNull(obj, polyad.getArgAt(0));
        if (!isString(obj)) {
            throw new BadArgException(DIR + " requires a string for its first argument.", polyad.getArgAt(0));
        }
        String fileName = obj.toString();
        DebugUtil.trace(this, "in " + DIR + " command: file name =" + fileName);

        int op = -1; // default

        VFSFileProvider vfs = null;
        String[] entries = null;
        boolean hasVF = false;
        if (state.isVFSFile(fileName)) {
            try {
                // Add in a final path separator if needed so it looks for a directory with this name.
                vfs = state.getVFS(fileName + (fileName.endsWith(PATH_SEPARATOR) ? "" : PATH_SEPARATOR));
                if (vfs != null) {
                    DebugUtil.trace(this, "in " + DIR + " command: got a VFS=" + vfs);

                    entries = vfs.dir(fileName);
                } else {
                    DebugUtil.trace(this, "in " + DIR + " command: NO VFS");

                }

            } catch (Throwable throwable) {
                DebugUtil.trace(this, "Got an exception:" + throwable.getMessage());
                state.getLogger().error("Error accessing VFS!", throwable);
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }
                throw new QDLIOException("Error; Could not resolve virtual file system for '" + fileName + "'");
            }
        } else {
            // So it's just a file.
            if (state.isServerMode()) {
                throw new QDLServerModeException("File system operations not permitted in server mode.");
            }
            File f = new File(fileName);
            entries = f.list();
            for (int i = 0; i < entries.length; i++) {

                File ff = new File(f, entries[i]);
                if (ff.isDirectory()) {
                    entries[i] = entries[i] + "/";
                }
            }
        }
        DebugUtil.trace(this, "in " + DIR + " command: entries =" + entries);

        if (entries == null) {
            // Then this is not a directory the request was made for a file.
            // The result should be null
            polyad.setEvaluated(true);
            polyad.setResult(QDLNull.getInstance());
            return;
        }
        QDLStem dir = new QDLStem();
        for (String x : entries) {
            dir.getQDLList().append(x);
        }
        polyad.setEvaluated(true);
        polyad.setResult(dir);
    }

    protected void vfsUnmount(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("Unmounting virtual file systems is not permitted in server mode.");
        }

        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(VFS_UNMOUNT + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(VFS_UNMOUNT + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Object arg = polyad.evalArg(0, state);

        checkNull(arg, polyad.getArgAt(0), state);
        if (!isString(arg)) {
            throw new BadArgException(VFS_UNMOUNT + " requires a string as its argument", polyad.getArgAt(0));
        }
        String mountPoint = (String) arg;
        if (!state.hasMountPoint(mountPoint)) {
            throw new BadArgException("the mount point '" + mountPoint + "' is not valid.", polyad.getArgAt(0));
        }
        state.removeVFSProvider(mountPoint);

        polyad.setEvaluated(true);
        polyad.setResult(Boolean.TRUE);
    }

    protected void vfsMount(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException("Mounting virtual file systems is not permitted in server mode.");
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(VFS_MOUNT + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(VFS_MOUNT + " requires at most 1 argument", polyad.getArgAt(1));
        }

        QDLValue arg1 = polyad.evalArg(0, state);
        checkNull(arg1, polyad.getArgAt(0));
        if (!arg1.isStem()) {
            throw new BadArgException("The argument must be a stem", polyad.getArgAt(0));
        }
        QDLStem cfg = arg1.asStem();
        if (!cfg.containsKey(VFS_ATTR_TYPE)) {
            // create a memory store
            cfg.put(VFS_ATTR_TYPE, asQDLValue(VFS_TYPE_MEMORY));
        }
        // Now grab defaults
        String mountPoint = null;
        if (!cfg.containsKey(VFS_MOUNT_POINT_TAG)) {
            throw new QDLExceptionWithTrace("No " + VFS_MOUNT_POINT_TAG + "  specified for VFS", polyad.getArgAt(0));
        } else {
            mountPoint = cfg.getString(VFS_MOUNT_POINT_TAG);
        }
        String scheme = null;
        if (!cfg.containsKey(VFS_SCHEME_TAG)) {
            throw new QDLExceptionWithTrace("No " + VFS_SCHEME_TAG + " specified for VFS", polyad.getArgAt(0));
        } else {
            scheme = cfg.getString(VFS_SCHEME_TAG);
        }
        String access = "r";
        if (cfg.containsKey(VFS_ATTR_ACCESS)) {
            access = cfg.getString(VFS_ATTR_ACCESS);
        }

        VFSFileProvider vfs = null;
        switch (cfg.getString(VFS_ATTR_TYPE)) {
            case VFS_TYPE_MEMORY:
                vfs = new VFSMemoryFileProvider(scheme, mountPoint, access.contains("r"), access.contains("w"));
                break;
            case VFS_TYPE_PASS_THROUGH:
                if (!cfg.containsKey(VFS_ROOT_DIR_TAG)) {
                    throw new QDLExceptionWithTrace("VFS type of " + VFS_TYPE_PASS_THROUGH + " requires a " + VFS_ROOT_DIR_TAG, polyad.getArgAt(0));
                }
                vfs = new VFSPassThruFileProvider(cfg.getString(VFS_ROOT_DIR_TAG),
                        scheme,
                        mountPoint,
                        access.contains("r"),
                        access.contains("w"));
                break;
            case VFS_TYPE_MYSQL:
                Map<String, String> myCfg = new HashMap<>();
                for (Object key : cfg.keySet()) {
                    Object v = cfg.get(key);
                    myCfg.put(String.valueOf(key), v == null ? null : v.toString());
                }
                VFSDatabase db = setupMySQLDatabase(null, myCfg);
                vfs = new VFSMySQLProvider(db,
                        scheme, mountPoint, access.contains("r"), access.contains("w"));
                break;

            case VFS_TYPE_ZIP:
                if (!cfg.containsKey(VFS_ZIP_FILE_PATH)) {
                    throw new QDLExceptionWithTrace("VFS type of " + VFS_TYPE_ZIP + " requires a " + VFS_ZIP_FILE_PATH, polyad.getArgAt(0));
                }
                vfs = new VFSZipFileProvider(cfg.getString(VFS_ZIP_FILE_PATH),
                        scheme,
                        mountPoint,
                        access.contains("r"),
                        access.contains("w"));
                break;
            default:
                throw new QDLExceptionWithTrace("unknown VFS type '" + cfg.getString(VFS_ATTR_TYPE) + "'", polyad.getArgAt(0));

        }
        state.addVFSProvider(vfs);
        polyad.setResult(QDLNull.getInstance());
        polyad.setResult(Constant.NULL_TYPE);
        polyad.setEvaluated(true);
        return;
    }

    protected void doWriteFile(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2, 3});
            polyad.setEvaluated(true);
            return;
        }

        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(WRITE_FILE + " requires at least 1 argument", polyad);
        }

        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(WRITE_FILE + " requires at most 3 arguments", polyad.getArgAt(3));
        }

        QDLValue obj = polyad.evalArg(0, state);
        checkNull(obj, polyad.getArgAt(0));
        if (polyad.getArgCount() == 1) {
            if (!obj.isStem()) {
                throw new BadArgException(WRITE_FILE + " must have a stem as its only argument.", polyad.getArgAt(0));
            }
            polyad.setEvaluated(true);
            polyad.setResult(processWriteFileStem(obj.asStem(), state, polyad));
            return;
        }
        QDLValue obj2 = polyad.evalArg(1, state);
        checkNull(obj2, polyad.getArgAt(1));
        if (!obj.isString()) {
            throw new BadArgException("The first argument to '" + WRITE_FILE + "' must be a string that is the file name.", polyad.getArgAt(1));
        }
        String fileName = obj.asString();
        //boolean isBase64 = false;
        int fileType = FILE_OP_TEXT_STRING;// default
        if (polyad.getArgCount() == 3) {
            QDLValue obj3 = polyad.evalArg(2, state);
            checkNull(obj3, polyad.getArgAt(2));
            if (obj3.isBoolean()) {
                // Allow to send true = base64 or false (default
                if (obj3.asBoolean()) {
                    fileType = FILE_OP_BINARY;
                }
            } else {
                if (!obj3.isLong()) {
                    throw new BadArgException("The third argument to '" + WRITE_FILE + "' must be an integer.", polyad.getArgAt(2));
                }
                fileType = obj3.asLong().intValue();
            }
        }
        writeSingleFile(fileName, obj2, fileType, state, polyad);


        polyad.setResult(Boolean.TRUE);
        polyad.setEvaluated(true);

    }

    public static final String FILE_WRITE_PATH_KEY = "path";
    public static final String FILE_WRITE_CONTENT_KEY = "content";
    public static final String FILE_WRITE_TYPE_KEY = "type";

    protected QDLStem processWriteFileStem(QDLStem input, State state, Polyad polyad) {
        QDLStem output = new QDLStem();
        for (Object key : input.keySet()) {
            QDLValue value = input.get(key);
            if (value.isStem()) {
                QDLStem p = value.asStem();
                if (p.containsKey(FILE_WRITE_PATH_KEY) && p.containsKey(FILE_WRITE_CONTENT_KEY)) {
                    QDLValue path = p.get(FILE_WRITE_PATH_KEY);
                    if (!path.isString()) {
                        throw new BadArgException("The path '" + path + "' for " + WRITE_FILE + " must be a string", polyad.getArgAt(0));
                    }
                    QDLValue content = p.get(FILE_WRITE_CONTENT_KEY);
                    int fileType = FILE_OP_TEXT_STRING;// default
                    if (p.containsKey(FILE_WRITE_TYPE_KEY)) {
                        QDLValue obj = p.get(FILE_WRITE_TYPE_KEY);
                        if (obj.isLong()) {
                            fileType = obj.asLong().intValue();
                        } else {
                            if (obj.isBoolean()) {
                                fileType = obj.asBoolean() ? FILE_OP_BINARY : fileType;
                            }
                        }
                    }
                    writeSingleFile(path.asString(), content, fileType, state, polyad);
                    output.putLongOrString(key, BooleanValue.True);
                } else {
                    output.putLongOrString(key, asQDLValue(value));
                }
            } else {
                output.putLongOrString(key, asQDLValue(value)); // return argument unchanged
            }
        }
        return output;
    }

    protected void writeSingleFile(String fileName, QDLValue content, int fileType, State state, Polyad polyad) {
        boolean allowListEntriesInIniFiles = true;
        if (state.isVFSFile(fileName)) {
            try {
                VFSFileProvider vfs = state.getVFS(fileName);
                XProperties xProperties = new XProperties();
                ArrayList<String> lines = new ArrayList<>();

                switch (fileType) {
                    case FILE_OP_TEXT_WITHOUT_LIST_INI:
                        allowListEntriesInIniFiles = false;
                    case FILE_OP_TEXT_INI:
                        if (!isStem(content)) {
                            throw new BadArgException(WRITE_FILE + " requires a stem for ini output", polyad.getArgAt(1));
                        }
                        xProperties.put(FileEntry.CONTENT_TYPE, FileEntry.TEXT_TYPE);
                        lines.add(convertToIni(content.asStem(), allowListEntriesInIniFiles));
                        break;
                    case FILE_OP_BINARY:
                        xProperties.put(FileEntry.CONTENT_TYPE, FileEntry.BINARY_TYPE);
                        lines.add(content.asString());  // in VFS stores we store a binary string
                        break;
                    case FILE_OP_TEXT_STRING:
                    case FILE_OP_TEXT_STEM:
                        if (content.isStem()) {
                            QDLStem contents = content.asStem();

                            xProperties.put(FileEntry.CONTENT_TYPE, FileEntry.TEXT_TYPE);
                            // allow for writing empty files. Edge case but happens.
                            if (!contents.containsKey("0") && !contents.isEmpty()) {
                                throw new BadArgException("The given stem is not a list. It must be a list to use this function.", polyad.getArgAt(1));
                            }
                            for (int i = 0; i < contents.size(); i++) {
                                lines.add(contents.getString(Integer.toString(i)));
                            }
                        }
                        if (content.isString()) {
                            xProperties.put(FileEntry.CONTENT_TYPE, FileEntry.TEXT_TYPE);
                            lines.add(content.asString());
                        }
                        break;
                    default:
                        throw new BadArgException("unknown file type '" + fileType + "'", polyad.getArgAt(2));
                }

                VFSEntry entry = new FileEntry(lines, xProperties);
                vfs.put(fileName, entry);
            } catch (Throwable t) {
                throw new QDLExceptionWithTrace("Could not write file to store." + t.getMessage(), polyad);
            }
        } else {
            if (state.isServerMode()) {
                throw new QDLServerModeException("File operations are not permitted in server mode");
            }

            try {
                switch (fileType) {
                    case FILE_OP_BINARY:
                        QDLFileUtil.writeFileAsBinary(fileName, content.asString());
                        break;
                    case FILE_OP_TEXT_WITHOUT_LIST_INI:
                        allowListEntriesInIniFiles = false;
                    case FILE_OP_TEXT_INI:
                        if (!isStem(content)) {
                            throw new BadArgException(WRITE_FILE + " requires a stem for ini output", polyad.getArgAt(1));
                        }
                        QDLFileUtil.writeStringToFile(fileName, convertToIni(content.asStem(), allowListEntriesInIniFiles));
                        break;
                    case FILE_OP_TEXT_STEM:
                    case FILE_OP_TEXT_STRING:
                        if (isStem(content)) {
                            QDLFileUtil.writeStemToFile(fileName, content.asStem() );
                        }
                        if (isString(content)) {
                            QDLFileUtil.writeStringToFile(fileName, content.asString());
                        }
                        break;
                    default:
                        throw new BadArgException("unknown file type '" + fileType + "'", polyad.getArgAt(2));
                }
            } catch (Throwable t) {
                if (t instanceof QDLException) {
                    throw (RuntimeException) t;
                }
                throw new QDLException("could not write file '" + fileName + "':" + t.getMessage());
            }
        }
    }

    public static String convertToIni(QDLStem obj2, boolean allowListEntries) {
        return convertToIni(obj2, 0, allowListEntries); // default is no indent
    }

    public static String convertToIni(QDLStem obj2, int indentFactor, boolean allowListEntries) {
        StringBuilder stringBuilder = new StringBuilder();
        String indent = ""; // top level
        for (Object key0 : obj2.keySet()) {
            List<String> sectionNames = new ArrayList<>();
            if (key0 instanceof Long) {
                if (allowListEntries) {
                    sectionNames.add(INI_LIST_ENTRY_START + key0);
                } else {
                    throw new IllegalArgumentException("list entries not allowed for sections");

                }
            } else {
                sectionNames.add((String) key0);
            }
            QDLValue value = obj2.get(key0);
            if (!value.isStem()) {
                throw new IllegalArgumentException("sections of an ini file must be stems");
            }
            QDLStem stem = value.asStem();
            convertToIni(sectionNames, stem, stringBuilder, indent, indentFactor, allowListEntries);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

/*
    public static String convertToIniOLD(QDLStem obj2) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object key0 : obj2.keySet()) {
            stringBuilder.append("[" + key0 + "]\n");
            Object obj = obj2.get(key0);
            if (!(obj instanceof QDLStem)) {
                throw new IllegalArgumentException("ini files must have stems as entries.");
            }
            QDLStem innerStem = (QDLStem) obj;
            for (Object key1 : innerStem.keySet()) {
                Object innerObject = innerStem.get(key1);
                if (!Constant.isStem(innerObject)) {
                    stringBuilder.append(key1 + " := " + InputFormUtil.inputForm(innerObject) + "\n");
                } else {
                    QDLStem innerInnerObject = (QDLStem) innerObject;
                    String out = "";
                    boolean isFirst = true;
                    for (Object key2 : innerInnerObject.keySet()) {
                        Object object2 = innerInnerObject.get(key2);
                        if (Constant.isStem(object2)) {
                            throw new IllegalArgumentException("Ini files do not support nested stems");
                        }
                        out = out + (isFirst ? "" : ",") + InputFormUtil.inputForm(object2);
                        if (isFirst) isFirst = false;
                    }

                    stringBuilder.append(key1 + " := " + out + "\n");
                }
            }
            stringBuilder.append("\n"); // helps with readability.
        }
        return stringBuilder.toString();
    }
*/

    public static final String INI_LIST_ENTRY_START = "_";

    protected static void convertToIni(List<String> names,
                                       QDLStem stem,
                                       StringBuilder sb,
                                       String initialIndent,
                                       int indentFactor,
                                       boolean allowListEntries) {
        String indent = initialIndent;
        sb.append(indent + "[" + toSectionHeader(names) + "]\n");
        Map<String, QDLStem> postProcessStems = new HashMap();
        for (Object key : stem.keySet()) {
            QDLValue value = stem.get(key);
            if (value.isStem()) {
                QDLStem s = value.asStem();
                if (s.isList()) {
                    sb.append(indent + key + " := " + toIniList(s.getQDLList()) + "\n");
                } else {
                    if (key instanceof Long) {
                        if (allowListEntries) {
                            postProcessStems.put(INI_LIST_ENTRY_START + key, s);
                        } else {
                            throw new IllegalArgumentException("list entries not allowed for sections");
                        }
                    } else {
                        postProcessStems.put((String) key, s);
                    }
                }
            } else {
                if (key instanceof Long) {
                    if (allowListEntries) {
                        sb.append(indent + INI_LIST_ENTRY_START + key + " := " + InputFormUtil.inputForm(value) + "\n");
                    } else {
                        throw new IllegalArgumentException("list entries not allowed for sections");
                    }
                } else {
                    sb.append(indent + key + " := " + InputFormUtil.inputForm(value) + "\n");
                }
            }
        }
        // now we do the next set of embedded stems
        for (String key : postProcessStems.keySet()) {
            List<String> newNames = new ArrayList<>();
            newNames.addAll(names);
            newNames.add(key);
            convertToIni(newNames, postProcessStems.get(key), sb, indent + StringUtils.getBlanks(indentFactor), indentFactor, allowListEntries);
        }
    }

    /*
    j_use('convert')
      ini.:=ini_in(file_read('/tmp/eg.ini'))
ini.
print(ini.)
a : {b:{r:s, t:v}, p:q}
z : {"m": 123}
print({'m':123})
m : 123
print({'a':{'m':123}})
a : {"m": 123}
)r
a : {"m": 123}
{a:{b:{r:s, t:v}, p:q}, z:{m:123}}
print(ini.)
a : {b:{r:s, t:v}, p:q}
z : {"m": 123}
ini_out(ini.)
     */
    protected static String toIniList(QDLList list) {
        if (list.isEmpty()) return null;
        String out = "";
        boolean firstPass = true;
        for (Object key : list.orderedKeys()) {
            Object value = list.get((Long) key);
            if (Constant.isStem(value)) {
                throw new IllegalArgumentException("nested stems in lists are not allowed in ini files");
            }
            if (firstPass) {
                firstPass = false;
                out = InputFormUtil.inputForm(value);
            } else {
                out = out + "," + InputFormUtil.inputForm(value);
            }
        }
        return out;
    }

    protected static String toSectionHeader(List<String> names) {
        String out = names.get(0);
        for (int i = 1; i < names.size(); i++) {
            out = out + QDLStem.STEM_INDEX_MARKER + names.get(i);
        }
        return out;
    }

    protected void doReadFile(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(READ_FILE + " requires at least 1 argument", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(READ_FILE + " requires at most 2 arguments", polyad.getArgAt(2));
        }

        QDLValue arg0 = polyad.evalArg(0, state);
        checkNull(arg0, polyad.getArgAt(0));
        QDLStem input;
        boolean isScalar = false;
        if (arg0.isStem()) {
            input = arg0.asStem();
            isScalar = false;
        } else {
            if (arg0.isString()) {
                input = new QDLStem();
                input.getQDLList().add(asQDLValue(arg0));
                isScalar = true;
            } else {
                throw new BadArgException(READ_FILE + " requires a string for its first argument.", polyad.getArgAt(0));
            }
        }
        QDLStem types;
        Long defaultOp = (long) FILE_OP_AUTO;
        if (polyad.getArgCount() == 2) {
            QDLValue arg1 = polyad.evalArg(1, state);
            checkNull(arg1, polyad.getArgAt(1));
            if (arg1.isStem()) {
                types =  arg1.asStem();
            } else {
                if (arg1.isLong()) {
                    types = new QDLStem();
                    types.setDefaultValue(arg1);
                    defaultOp = arg1.asLong();
                } else {
                    throw new BadArgException(READ_FILE + " requires its second argument be an integer.", polyad.getArgAt(1));
                }
            }
        } else {
            types = new QDLStem();
            types.setDefaultValue(defaultOp); // default
        }
        //String fileName = arg0.toString();
        QDLStem output = readFileStem(input, types, defaultOp, state, polyad);
        if (isScalar) {
            polyad.setResult(output.get(0L));
        } else {
            polyad.setResult(output);
        }
        polyad.setEvaluated(true); // or reading the result to set the type fails.

    }

    protected QDLStem readFileStem(
            QDLStem input,
            QDLStem types,
            Long defaultType,
            State state,
            Polyad polyad) {  // polyad for messages only
        QDLStem output = new QDLStem();
        for (Object key : input.keySet()) {
            QDLValue value = input.get(key);
            QDLStem newTypes = null;
            QDLStem newInputs = null;
            if (value.isStem()) {
                newInputs =  value.asStem();
                if (types == null) {

                } else {
                    QDLValue type = types.get(key);
                    if (type.isStem()) {
                        newTypes = type.asStem();
                    } else {
                        throw new BadArgException("unknown file type '" + type + "'", polyad.getArgCount() == 2 ? polyad.getArgAt(1) : polyad.getArgAt(0));
                    }
                }
                QDLStem stem = readFileStem(newInputs, newTypes, defaultType, state, polyad);
                output.putLongOrString(key, asQDLValue(stem));

            } else {
                if (!(value.isString())) {
                    throw new BadArgException(READ_FILE + " requires only strings as argument. Got '" + value + "'", polyad.getArgAt(0));
                }
                Long type = defaultType;
                if (types != null) {
                    QDLValue x = types.get(key);
                    if (x.isLong()) {
                        type =  x.asLong();
                    } else {
                        throw new BadArgException(READ_FILE + " requires file type as argument. Got '" + x + "'", polyad.getArgCount() == 2 ? polyad.getArgAt(1) : polyad.getArgAt(0));
                    }
                }
                output.putLongOrString(key, asQDLValue(readSingleFile(value.asString(), type, state, polyad)));
            }

        }
        return output;
    }

    protected Object readSingleFile(String fileName, Long type, State state, Polyad polyad) { // polyad for messages only
        Object out = null;
        int op = type.intValue();
        VFSEntry vfsEntry = null;
        boolean hasVF = false;
        if (state.isVFSFile(fileName)) {
            vfsEntry = resolveResourceToFile(fileName, op, state);
            if (vfsEntry == null) {
                throw new QDLException("The resource '" + fileName + "' was not found in the virtual file system");
            }
            hasVF = true;
        } else {
            // https://localhost:9443/oauth2/authorize?scope=org.cilogon.userinfo+openid+profile+email+read%3A+x.y%3A+x.z+write%3A&response_type=code&redirect_uri=https%3A%2F%2Flocalhost%3A9443%2Fclient42%2Fready&state=BvOuWCHp4uiUP9X3LNNUIq06hCNV1T_sXPfapRhCXJw&nonce=sfXKMepNO3OWNyDfBGsiacXQ1euR67fdtqYXJSybjLI&prompt=login&client_id=localhost%3Atest%2Fno_qdl
            // https://localhost:9443/oauth2/authorize?scope=org.cilogon.userinfo+openid+profile+email+read%3A+x.y%3A+x.z+write%3A&response_type=code&redirect_uri=https%3A%2F%2Flocalhost%3A9443%2Fclient42%2Fready&state=zGI8jV81kmiAGhMnVcr2yUDfcAbhp3y2Xbu3e5J7Dzs&nonce=Pf5ziEOsms3VjIlas9uBf3wrJp2If-ZT1h8STwmRM64&prompt=login&client_id=localhost%3Atest%2Fno_qdlvirtual file reads in server mode.
            // If the file does not live in a VFS throw an exception.
            if (state.isServerMode()) {
                throw new QDLServerModeException("File system operations not permitted in server mode.");
            }
        }
        boolean allowListEntriesInIniFiles = true;
        try {
            switch (op) {
                case FILE_OP_BINARY:
                    // binary file. Read it and base64 encode it
                    if (hasVF) {
                        out = vfsEntry.getText();// if this is binary, the contents are a single base64 encoded string.
                    } else {
                        out = QDLFileUtil.readFileAsBinary(fileName);
                    }
                    return out;
                case FILE_OP_TEXT_STEM:
                    if (hasVF) {
                        out = vfsEntry.convertToStem();// if this is binary, the contents are a single base64 encoded string.
                    } else {
                        out = QDLFileUtil.readTextFileAsStem(state, fileName);
                    }
                    // Read as lines, put in a stem
                    return out;
                case FILE_OP_TEXT_WITHOUT_LIST_INI:
                    allowListEntriesInIniFiles = false;
                case FILE_OP_TEXT_INI:
                    // read it as a stem and start parsing
                    String content;
                    if (hasVF) {
                        content = vfsEntry.getText();
                    } else {
                        content = QDLFileUtil.readTextFile(state, fileName);
                    }
                    if (StringUtils.isTrivial(content)) {
                        out = new QDLStem();
                        return out;
                    }
                    IniParserDriver iniParserDriver = new IniParserDriver();
                    StringReader stringReader = new StringReader(content);
                    out = iniParserDriver.parse(stringReader, allowListEntriesInIniFiles);

                    return out;
                default:
                    throw new BadArgException(" unknown file type '" + op + "'", polyad);
                case FILE_OP_TEXT_STRING:
                case FILE_OP_AUTO:
                    // read it as a long string.
                    if (hasVF) {
                        out = vfsEntry.getText();
                    } else {
                        out = QDLFileUtil.readFileAsString(fileName);
                    }
                    return out;

            }
        } catch (Throwable t) {
            if (t instanceof QDLException) {
                throw (RuntimeException) t;
            }
            if (t instanceof FileNotFoundException) {
                throw new QDLFileNotFoundException("'" + fileName + "' not found:" + t.getMessage());
            }
            if (t instanceof IllegalAccessException) {
                throw new QDLFileAccessException("access denied to '" + fileName + "':" + t.getMessage());
            }
            throw new QDLException("Error reading file '" + fileName + "'" + t.getMessage());
        }
    }
}
