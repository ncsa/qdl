package org.qdl_lang.state;

import org.qdl_lang.config.QDLEnvironment;
import org.qdl_lang.expressions.module.*;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.convert.QDLConvertLoader;
import org.qdl_lang.extensions.crypto.CryptoLoader;
import org.qdl_lang.extensions.database.QDLDBLoader;
import org.qdl_lang.extensions.dynamodb.QDLDynamoDBLoader;
import org.qdl_lang.extensions.examples.basic.EGLoader;
import org.qdl_lang.extensions.http.QDLHTTPLoader;
import org.qdl_lang.extensions.inputLine.QDLCLIToolsLoader;
import org.qdl_lang.extensions.mail.QDLMailLoader;
import org.qdl_lang.extensions.examples.stateful.StatefulLoader;
import org.qdl_lang.scripting.QDLScript;
import org.qdl_lang.scripting.Scripts;
import org.qdl_lang.statements.TryCatch;
import org.qdl_lang.util.ModuleUtils;
import org.qdl_lang.util.QDLFileUtil;
import org.qdl_lang.util.QDLVersion;
import org.qdl_lang.variables.*;
import org.qdl_lang.vfs.VFSEntry;
import org.qdl_lang.vfs.VFSFileProvider;
import org.qdl_lang.vfs.VFSPaths;
import org.qdl_lang.workspace.QDLWorkspace;
import org.qdl_lang.workspace.WorkspaceCommands;
import org.qdl_lang.xml.SerializationState;
import org.qdl_lang.xml.XMLMissingCloseTagException;
import org.qdl_lang.xml.XMLUtils;
import org.qdl_lang.xml.XMLUtilsV2;
import edu.uiuc.ncsa.security.core.configuration.XProperties;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.core.util.Iso8601;
import edu.uiuc.ncsa.security.core.util.MetaDebugUtil;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.qdl_lang.evaluate.*;
import org.qdl_lang.functions.FKey;
import org.qdl_lang.functions.FStack;
import org.qdl_lang.functions.FTable;
import org.qdl_lang.functions.FunctionRecordInterface;
import org.qdl_lang.statements.ExpressionInterface;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Logger;

import static org.qdl_lang.evaluate.ModuleEvaluator.*;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;
import static org.qdl_lang.xml.SerializationConstants.*;

/**
 * This is a facade for the various stateful components we have to track.
 * Represents the internal state of the system.
 * <p>Created by Jeff Gaynor<br>
 * on 1/21/20 at  7:25 AM
 */
public class State extends FunctionState implements QDLConstants {
    private static final long serialVersionUID = 0xcafed00d1L;

    /**
     * The internal id of the state object is needed in serialization and other operations.
     * Every instance of a state object has a unique id.
     *
     * @return
     */
    public String getInternalID() {
        if (internalID == null) {
            internalID = getUuid().toString(); // basically just cache it.
        }
        return internalID;
    }

    String internalID = null;

    /**
     * This is used in the debugger and refers to paused vs running processes. It does
     * not refer to process ids or the internal id of the state object.
     *
     * @return
     */
    public int getStateID() {
        return stateID;
    }

    public void setStateID(int pid) {
        this.stateID = pid;
    }

    int stateID = 0;

    public Map<Integer, QDLThreadRecord> getThreadTable() {
        return threadTable;
    }

    public void setThreadTable(Map<Integer, QDLThreadRecord> threadTable) {
        this.threadTable = threadTable;
    }

    Map<Integer, QDLThreadRecord> threadTable = new HashMap<>();

    /**
     * A new instance with the default components.
     *
     * @return
     */
    public State newInstance() {
        return newInstance(new VStack(),
                new OpEvaluator(),
                MetaEvaluator.getInstance(),
                new FStack(),
                new MTStack(),
                new MIStack(),
                new MyLoggingFacade((Logger) null),
                false,
                false,
                true);

    }

    public static State getFactory() {
        if (factory == null) {
            factory = new State(); // just take it with the defaults
        }
        return factory;
    }

    public static void setFactory(State factory) {
        State.factory = factory;
    }

    static State factory = null;

    /**
     * If you extend this class, you must override this method to return a new instance
     * of your state with everything in it you want or need. Then set your {@link #setFactory(State)}
     *
     * @param opEvaluator
     * @param metaEvaluator
     * @param ftStack
     * @param mtStack
     * @param miStack
     * @param myLoggingFacade
     * @param isServerMode
     * @param isRestrictedIO
     * @param assertionsOn
     * @return
     */
    public State newInstance(VStack symbolStack,
                             OpEvaluator opEvaluator,
                             MetaEvaluator metaEvaluator,
                             FStack<? extends FTable<? extends FKey, ? extends FunctionRecordInterface>> ftStack,
                             MTStack mtStack,
                             MIStack miStack,
                             MyLoggingFacade myLoggingFacade,
                             boolean isServerMode,
                             boolean isRestrictedIO,
                             boolean assertionsOn) {
        return new State(symbolStack,
                opEvaluator,
                metaEvaluator,
                ftStack,
                mtStack,
                miStack,
                myLoggingFacade,
                isServerMode,
                isRestrictedIO,
                assertionsOn);
    }


    public State(
            VStack vStack,
            OpEvaluator opEvaluator,
            MetaEvaluator metaEvaluator,
            FStack<? extends FTable<? extends FKey, ? extends FunctionRecordInterface>> ftStack,
            MTStack mtStack,
            MIStack miStack,
            MyLoggingFacade myLoggingFacade,
            boolean isServerMode,
            boolean isRestrictedIO,
            boolean assertionsOn) {
        super(vStack,
                opEvaluator,
                metaEvaluator,
                ftStack,
                mtStack,
                miStack,
                myLoggingFacade);
        this.serverMode = isServerMode;
        this.assertionsOn = assertionsOn;
        this.restrictedIO = isRestrictedIO;
    }

    public QDLStem getSystemConstants() {
        if (systemConstants == null) {
            createSystemConstants();
        }
        return systemConstants;
    }

    public void setSystemConstants(QDLStem systemConstants) {
        this.systemConstants = systemConstants;
    }

    QDLStem systemConstants = null;

    public QDLStem getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(QDLStem systemInfo) {
        this.systemInfo = systemInfo;
    }

    QDLStem systemInfo = null;

    public void createSystemInfo(QDLEnvironment qe) {
        if (systemInfo != null) {
            return;
        }
        systemInfo = new QDLStem();
        // Add some from Java, if not in server mode.
        if (!isServerMode()) {
            HashMap<String, Object> ccc = new HashMap<>();
            ccc.put(SYS_INFO_OS_VERSION, System.getProperty("os.version"));
            ccc.put(SYS_INFO_OS_NAME, System.getProperty("os.name"));
            ccc.put(SYS_INFO_OS_ARCHITECTURE, System.getProperty("os.arch"));
            QDLStem os = new QDLStem();
            setStemValue(os, ccc);
            // Take a stab at the class path. This usually works, but not as reliably as it once did
            systemInfo.put(SYS_INFO_OS, asQDLValue(os));
            ccc.clear();
            ccc.put(SYS_INFO_JVM_VERSION, System.getProperty("java.version"));
            ccc.put(SYS_INFO_INIT_MEMORY, (Runtime.getRuntime().totalMemory() / (1024 * 1024)) + " MB");
            ccc.put(SYS_INFO_SYSTEM_PROCESSORS, Runtime.getRuntime().availableProcessors());
            QDLStem system = new QDLStem();
            setStemValue(system, ccc);
            QDLList classPath = new QDLList();
            String ccp = System.getProperty("java.class.path");
            if (StringUtils.isTrivial(ccp)) {
                StringTokenizer st = new StringTokenizer(ccp, File.pathSeparator);
                while (st.hasMoreTokens()) {
                    classPath.add(asQDLValue(st.nextToken()));
                }
            }
            // Only show the class path if there is something there.
            // it can be hit or miss any more if this actually works.
            if (!classPath.isEmpty()) {
                QDLStem cp = new QDLStem(classPath);
                system.put(SYS_INFO_CLASS_PATH, asQDLValue(cp));
            }
            systemInfo.put(SYS_INFO_SYSTEM, asQDLValue(system));
            ccc.clear();
            ccc.put(SYS_INFO_USER_INVOCATION_DIR, System.getProperty("user.dir"));
            ccc.put(SYS_INFO_USER_HOME_DIR, System.getProperty("user.home"));
            ccc.put(SYS_INFO_QDL_HOME_DIR, System.getenv("QDL_HOME") == null ? "(not set)" : System.getenv("QDL_HOME"));
            QDLStem user = new QDLStem();
            setStemValue(user, ccc);
            systemInfo.put(SYS_INFO_USER, asQDLValue(user));
        }


        QDLStem qdl_props = new QDLStem();
        QDLStem buildInfo;
        // get modules to list in the "lib" entry
        QDLStem libStem = new QDLStem();
        libStem.put("tools", asQDLValue(getLibMap()));
        systemInfo.put("lib", asQDLValue(libStem));
        if (qe != null && qe.isEnabled()) {
            HashMap<String, Object> ccc = new HashMap<>();
            // means this was started from a config file, not the command line
            ccc.put(SYS_BOOT_QDL_HOME, qe.getWSHomeDir());
            if (!qe.getBootScript().isEmpty()) {
                ccc.put(SYS_BOOT_BOOT_SCRIPT, qe.getBootScript());
            }
            ccc.put(SYS_BOOT_CONFIG_NAME, qe.getName());
            ccc.put(SYS_BOOT_CONFIG_FILE, qe.getCfgFile());
            if (qe.getMyLogger().getFileName() != null) {
                ccc.put(SYS_BOOT_LOG_FILE, qe.getMyLogger().getFileName());
                ccc.put(SYS_BOOT_LOG_NAME, qe.getMyLogger().getClassName());
            }
            ccc.put(SYS_BOOT_SERVER_MODE, isServerMode());
            ccc.put(SYS_BOOT_RESTRICTED_IO_MODE, isRestrictedIO());
            ccc.put(SYS_SCRIPTS_PATH, qe.getScriptPath());
            setStemValue(qdl_props, ccc);
            systemInfo.put(SYS_BOOT, asQDLValue(qdl_props));
            // Fix https://github.com/ncsa/qdl/issues/115
            String buildInfoPath = System.getenv("QDL_HOME");
            if (buildInfoPath == null) {
                buildInfoPath = qe.getWSHomeDir();
                if (buildInfoPath == null) {
                    buildInfoPath = System.getProperty("user.dir"); // last ditch effort...
                }
            }
            buildInfo = addManifestConstants(buildInfoPath);
            if (buildInfo != null) {
                systemInfo.put(SYS_QDL_BUILD, asQDLValue(buildInfo));
            }
            if (qe.hasLibLoader()) {
                qe.getLibLoader().add(this);
            }

        } else {
            // started from the command line.
            if (getLogger() != null) {
                if(getLogger().getFileName()!= null) {
                    qdl_props.put(SYS_BOOT_LOG_FILE, asQDLValue(getLogger().getFileName()));
                }
                qdl_props.put(SYS_BOOT_LOG_NAME, asQDLValue(getLogger().getClassName()));
            }
            qdl_props.put(SYS_BOOT_SERVER_MODE, asQDLValue(isServerMode()));
            qdl_props.put(SYS_BOOT_RESTRICTED_IO_MODE, asQDLValue(isRestrictedIO()));
            QDLStem scriptPath = new QDLStem();
            scriptPath.addList(getScriptPaths());
            qdl_props.put(SYS_SCRIPTS_PATH, asQDLValue(scriptPath));
            systemInfo.put(SYS_BOOT, asQDLValue(qdl_props));
            String buildInfoPath = System.getenv("QDL_HOME");
            if (buildInfoPath == null) {
                buildInfoPath = System.getProperty("user.dir");
                if (buildInfoPath == null) {
                    buildInfoPath = System.getProperty("user.home"); // last ditch effort...
                }
            }
            buildInfo = addManifestConstants(buildInfoPath);
            if (buildInfo != null) {
                systemInfo.put(SYS_QDL_BUILD, asQDLValue(buildInfo));
            }
        }

    }

    /**
     * Adds a list of classpath to the info().lib key entry. This allows modules to add their classes
     * to the library so users can find them. The argument is a stem with key values pairs:<br/>
     * name : classpath<br/><br/>
     * and will be added to an existing entry with the same key or a new one.
     * <p>E.g. calling</p>
     * addLibEntries("oa2", {"store":"path.to.store"})
     * <p>would result in the entry info().'lib'.'oa2'.'store' <br/>
     * returning the path.to.store</p>
     *
     * @param libraryKey
     * @param classPaths
     */
    public void addLibEntries(String libraryKey, QDLStem classPaths) {
        // systemInfo cannot be null since it should be created on boot.
        if (systemInfo == null) {
            createSystemInfo(null);// no other choice really.
        }
        QDLStem entry;
        QDLStem lib = systemInfo.getStem("lib");
        if (lib.containsKey(libraryKey)) {
            entry = lib.getStem(libraryKey);
        } else {
            entry = new QDLStem();
        }
        entry = entry.union(classPaths); // new stem with everything in it
        lib.put(libraryKey, asQDLValue(entry));
    }

    /**
     * Add a single entry to a given library
     *
     * @param libraryKey
     * @param moduleKey
     * @param className
     */
    public void addLibEntry(String libraryKey, String moduleKey, String className) {
        if (systemInfo == null) {
            createSystemInfo(null);// no other choice really.
        }
        QDLStem entry;
        QDLStem lib = systemInfo.getStem("lib");
        if (lib.containsKey(libraryKey)) {
            entry = lib.getStem(libraryKey);
        } else {
            entry = new QDLStem();
            lib.put(libraryKey, asQDLValue(entry));
        }
        entry.put(moduleKey, asQDLValue(className));

    }


    public QDLStem getLibMap() {
        HashMap<String, Object> ccc = new HashMap<>();
        ccc.put("description", "System tools for http, conversions and other very useful things.");
        ccc.put("http", QDLHTTPLoader.class.getCanonicalName());
        ccc.put("db", QDLDBLoader.class.getCanonicalName());
        ccc.put("dynamo", QDLDynamoDBLoader.class.getCanonicalName());
        ccc.put("crypto", CryptoLoader.class.getCanonicalName());
        //map.put("x509", X509Loader.class.getCanonicalName());
        ccc.put("convert", QDLConvertLoader.class.getCanonicalName());
        ccc.put("cli", QDLCLIToolsLoader.class.getCanonicalName());
        ccc.put("mail", QDLMailLoader.class.getCanonicalName());

        QDLStem map = new QDLStem();
        setStemValue(map, ccc);
        ccc.clear();
        ccc.put("basic", EGLoader.class.getCanonicalName());
        ccc.put("stateful", StatefulLoader.class.getCanonicalName());
        QDLStem egMap = new QDLStem();
        setStemValue(egMap, ccc);
        map.put("eg", asQDLValue(egMap));
        return map;
    }

    public void createSystemConstants() {
        if (systemConstants != null) {
            return;
        }
        // Start off with the actual constants that the system must have
        systemConstants = new QDLStem();

        HashMap<String, Object> ccc = new HashMap<>();
        ccc.put("00ac", OpEvaluator.NOT2);
        ccc.put("00af", OpEvaluator.MINUS2);
        ccc.put("00b7", QDLConstants.STEM_PATH_MARKER2);
        ccc.put("00bf", "¿");
        ccc.put("00d7", OpEvaluator.TIMES2);
        ccc.put("00f7", OpEvaluator.DIVIDE2);
        ccc.put("207a", OpEvaluator.PLUS2);
        ccc.put("2192", "→");
        ccc.put("2205", "∅");
        ccc.put("221a", "√");
        ccc.put("2227", OpEvaluator.AND2);
        ccc.put("2228", OpEvaluator.OR2);
        ccc.put("2248", "≈");
        ccc.put("2254", "≔");
        ccc.put("2255", "≕");
        ccc.put("2260", OpEvaluator.NOT_EQUAL2);
        ccc.put("2261", OpEvaluator.EQUALS2);
        ccc.put("2264", OpEvaluator.LESS_THAN_EQUAL3);
        ccc.put("2265", OpEvaluator.MORE_THAN_EQUAL3);
        ccc.put("22a8", "⊨");
        ccc.put("2241", "≁");
        ccc.put("2297", "⊗");
        ccc.put("2308", "⌈");
        ccc.put("230a", "⌊");
        ccc.put("27e6", "⟦");
        ccc.put("27e7", "⟧");
        ccc.put("22a2", OpEvaluator.TO_SET);
        ccc.put("2299", OpEvaluator.REDUCE_OP_KEY);
        ccc.put("2295", OpEvaluator.EXPAND_OP_KEY);
        ccc.put("2306", OpEvaluator.MASK_OP_KEY);
        ccc.put("00B5", OpEvaluator.TRANSPOSE_OP_KEY);
        ccc.put("03c0", TMathEvaluator.PI2);
        ccc.put("221A", "√");
        ccc.put("2229", "∩");
        ccc.put("222a", "∪");
        ccc.put("2208", "∈");
        ccc.put("2209", "∉");
        ccc.put("2203", "∃");
        ccc.put("2204", "∄");
        ccc.put("220b", "∋");
        ccc.put("220c", "∌");
        ccc.put("2200", "∀");
        ccc.put("21d2", "⇒");
        ccc.put("2202", OpEvaluator.APPLY_OP_KEY);
        QDLStem characterMap = new QDLStem();
        setStemValue(characterMap, ccc);

        systemConstants.put(SYS_VAR_TYPE_CHARACTER_MAP, asQDLValue(characterMap));
        ccc.clear();
        ccc.put("alphanumeric", ALPHA_CHARS);
        ccc.put("all", ALL_CHARS);
        ccc.put("ascii", ASCII_CHARS);
        ccc.put("unicode", UNICODE_CHARS);
        ccc.put("greek", GREEK_CHARS);
        QDLStem characters = new QDLStem();
        setStemValue(characters, ccc);

        systemConstants.put(SYS_VAR_TYPE_CHARACTERS, asQDLValue(characters));
        systemConstants.put(SYS_VAR_TYPE_RESERVED, asQDLValue(getQDLReservedNames()));
        ccc.clear();
        ccc.put(SYS_VAR_TYPE_STRING, (long) Constant.STRING_TYPE);
        ccc.put(SYS_VAR_TYPE_STEM, (long) Constant.STEM_TYPE);
        ccc.put(SYS_VAR_TYPE_BOOLEAN, (long) Constant.BOOLEAN_TYPE);
        ccc.put(SYS_VAR_TYPE_NULL, (long) Constant.NULL_TYPE);
        ccc.put(SYS_VAR_TYPE_INTEGER, (long) Constant.LONG_TYPE);
        ccc.put(SYS_VAR_TYPE_DECIMAL, (long) Constant.DECIMAL_TYPE);
        ccc.put(SYS_VAR_TYPE_UNDEFINED, (long) Constant.UNKNOWN_TYPE);
        ccc.put(SYS_VAR_TYPE_SET, (long) Constant.SET_TYPE);
        QDLStem varTypes = new QDLStem();
        setStemValue(varTypes, ccc);
        systemConstants.put(SYS_VAR_TYPES, asQDLValue(varTypes));

        ccc.clear();
        ccc.put(SYS_DETOKENIZE_PREPEND, StringEvaluator.DETOKENIZE_PREPEND_VALUE);
        ccc.put(SYS_DETOKENIZE_OMIT_DANGLING_DELIMITER, StringEvaluator.DETOKENIZE_OMIT_DANGLING_DELIMITER_VALUE);
        QDLStem detokenizeTypes = new QDLStem();
        setStemValue(detokenizeTypes, ccc);
        systemConstants.put(SYS_DETOKENIZE_TYPE, asQDLValue(detokenizeTypes));

        ccc.clear();
        ccc.put(MathEvaluator.HASH_ALGORITHM_MD2, MathEvaluator.HASH_ALGORITHM_MD2);
        ccc.put(MathEvaluator.HASH_ALGORITHM_MD5, MathEvaluator.HASH_ALGORITHM_MD5);
        ccc.put(MathEvaluator.HASH_ALGORITHM_SHA1, MathEvaluator.HASH_ALGORITHM_SHA1);
        ccc.put(MathEvaluator.HASH_ALGORITHM_SHA2, MathEvaluator.HASH_ALGORITHM_SHA2);
        ccc.put(MathEvaluator.HASH_ALGORITHM_SHA_256, MathEvaluator.HASH_ALGORITHM_SHA_256);
        ccc.put(MathEvaluator.HASH_ALGORITHM_SHA_384, MathEvaluator.HASH_ALGORITHM_SHA_384);
        ccc.put(MathEvaluator.HASH_ALGORITHM_SHA_512, MathEvaluator.HASH_ALGORITHM_SHA_512);
        ccc.put(MathEvaluator.HASH_ALGORITHM_SHA_512, MathEvaluator.HASH_ALGORITHM_SHA_512);
        QDLStem hashAlgorithms = new QDLStem();
        setStemValue(hashAlgorithms, ccc);
        systemConstants.put(SYS_HASH_ALGORITHMS, asQDLValue(hashAlgorithms));

        ccc.clear();
        ccc.put(SYS_CODEC_VENCODE, (long) MetaCodec.ALGORITHM_VENCODE);
        ccc.put(SYS_CODEC_URLCODE, (long) MetaCodec.ALGORITHM_URLCODE);
        ccc.put(SYS_CODEC_B16CODE, (long) MetaCodec.ALGORITHM_BASE16);
        ccc.put(SYS_CODEC_B32CODE, (long) MetaCodec.ALGORITHM_BASE32);
        ccc.put(SYS_CODEC_B64CODE, (long) MetaCodec.ALGORITHM_BASE64);
        ccc.put(SYS_CODEC_HTML3, (long) MetaCodec.ALGORITHM_HTML3);
        ccc.put(SYS_CODEC_HTML4, (long) MetaCodec.ALGORITHM_HTML4);
        ccc.put(SYS_CODEC_XML_1_0, (long) MetaCodec.ALGORITHM_XML_1_0);
        ccc.put(SYS_CODEC_XML_1_1, (long) MetaCodec.ALGORITHM_XML_1_1);
        ccc.put(SYS_CODEC_JAVA, (long) MetaCodec.ALGORITHM_JAVA);
        ccc.put(SYS_CODEC_JSON, (long) MetaCodec.ALGORITHM_JSON);
        ccc.put(SYS_CODEC_CSV, (long) MetaCodec.ALGORITHM_CSV);
        ccc.put(SYS_CODEC_ECMA, (long) MetaCodec.ALGORITHM_ECMA);
        ccc.put(SYS_CODEC_XSI, (long) MetaCodec.ALGORITHM_XSI);
        QDLStem intCodecs = new QDLStem();
        setStemValue(intCodecs, ccc);

        ccc.clear();
        ccc.put(SYS_CODEC_VENCODE, MetaCodec.ALGORITHM_VENCODE_NAME);
        ccc.put(SYS_CODEC_URLCODE, MetaCodec.ALGORITHM_URLCODE_NAME);
        ccc.put(SYS_CODEC_B16CODE, MetaCodec.ALGORITHM_BASE16_NAME);
        ccc.put(SYS_CODEC_B32CODE, MetaCodec.ALGORITHM_BASE32_NAME);
        ccc.put(SYS_CODEC_B64CODE, MetaCodec.ALGORITHM_BASE64_NAME);
        ccc.put(SYS_CODEC_HTML3, MetaCodec.ALGORITHM_HTML3_NAME);
        ccc.put(SYS_CODEC_HTML4, MetaCodec.ALGORITHM_HTML4_NAME);
        ccc.put(SYS_CODEC_XML_1_0, MetaCodec.ALGORITHM_XML_1_0_NAME);
        ccc.put(SYS_CODEC_XML_1_1, MetaCodec.ALGORITHM_XML_1_1_NAME);
        ccc.put(SYS_CODEC_JAVA, MetaCodec.ALGORITHM_JAVA_NAME);
        ccc.put(SYS_CODEC_JSON, MetaCodec.ALGORITHM_JSON_NAME);
        ccc.put(SYS_CODEC_CSV, MetaCodec.ALGORITHM_CSV_NAME);
        ccc.put(SYS_CODEC_ECMA, MetaCodec.ALGORITHM_ECMA_NAME);
        ccc.put(SYS_CODEC_XSI, MetaCodec.ALGORITHM_XSI_NAME);
        QDLStem stringCodecs = new QDLStem();
        setStemValue(stringCodecs, ccc);
        QDLStem codecs = new QDLStem();
        codecs.put("int.", asQDLValue(intCodecs));
        codecs.put("string.", asQDLValue(stringCodecs));
        systemConstants.put(SYS_CODEC_ALGORITHMS, asQDLValue(codecs));

        ccc.clear();
        ccc.put(SYS_ERROR_CODE_SYSTEM_ERROR, TryCatch.RESERVED_SYSTEM_ERROR_CODE);
        ccc.put(SYS_ASSERT_CODE_SYSTEM_ERROR, TryCatch.RESERVED_ASSERTION_CODE);
        ccc.put(SYS_ERROR_CODE_DEFAULT_USER_ERROR, TryCatch.RESERVED_USER_ERROR_CODE);
        QDLStem errorCodes = new QDLStem();
        setStemValue(errorCodes, ccc);
        systemConstants.put(SYS_ERROR_CODES, asQDLValue(errorCodes));

        ccc.clear();
        ccc.put(SYS_FILE_TYPE_BINARY, (long) IOEvaluator.FILE_OP_BINARY);
        ccc.put(SYS_FILE_TYPE_STEM, (long) IOEvaluator.FILE_OP_TEXT_STEM);
        ccc.put(SYS_FILE_TYPE_STRING, (long) IOEvaluator.FILE_OP_TEXT_STRING);
        ccc.put(SYS_FILE_TYPE_INIT, (long) IOEvaluator.FILE_OP_TEXT_INI);
        ccc.put(SYS_FILE_TYPE_NO_LIST_INIT, (long) IOEvaluator.FILE_OP_TEXT_WITHOUT_LIST_INI);
        QDLStem fileTypes = new QDLStem();
        setStemValue(fileTypes, ccc);
        systemConstants.put(SYS_FILE_TYPES, asQDLValue(fileTypes));


        ccc.clear();
        ccc.put(URI_AUTHORITY, URI_AUTHORITY);
        ccc.put(URI_HOST, URI_HOST);
        ccc.put(URI_FRAGMENT, URI_FRAGMENT);
        ccc.put(URI_QUERY, URI_QUERY);
        ccc.put(URI_PORT, URI_PORT);
        ccc.put(URI_PATH, URI_PATH);
        ccc.put(URI_SCHEME, URI_SCHEME);
        ccc.put(URI_SCHEME_SPECIFIC_PART, URI_SCHEME_SPECIFIC_PART);
        ccc.put(URI_USER_INFO, URI_USER_INFO);
        QDLStem uriFields = new QDLStem();
        setStemValue(uriFields, ccc);

        systemConstants.put(URI_FIELDS, asQDLValue(uriFields));

        ccc.clear();
        ccc.put(SYS_LOG_NONE, SystemEvaluator.LOG_LEVEL_NONE);
        ccc.put(SYS_LOG_TRACE, SystemEvaluator.LOG_LEVEL_TRACE);
        ccc.put(SYS_LOG_INFO, SystemEvaluator.LOG_LEVEL_INFO);
        ccc.put(SYS_LOG_WARN, SystemEvaluator.LOG_LEVEL_WARN);
        ccc.put(SYS_LOG_ERROR, SystemEvaluator.LOG_LEVEL_ERROR);
        ccc.put(SYS_LOG_SEVERE, SystemEvaluator.LOG_LEVEL_SEVERE);
        QDLStem logLevels = new QDLStem();
        setStemValue(logLevels, ccc);
        systemConstants.put(SYS_LOG_LEVELS, asQDLValue(logLevels));

        ccc.clear();
        ccc.put(IMPORT_STATE_SNAPSHOT, (long) IMPORT_STATE_SNAPSHOT_VALUE);
        ccc.put(IMPORT_STATE_SHARE, (long) IMPORT_STATE_SHARE_VALUE);
        ccc.put(IMPORT_STATE_NONE, (long) IMPORT_STATE_NONE_VALUE);
        QDLStem moduleImportModes = new QDLStem();
        setStemValue(moduleImportModes, ccc);
        systemConstants.put(SYS_MODULE_IMPORT_MODES, asQDLValue(moduleImportModes));

    }

    protected void setStemValue(QDLStem stem, Map<String, Object> values) {
        StemUtility.setStemValue(stem, values);
    }

    /**
     * Get a listing of all the functions, operators and keywords for QDL.
     *
     * @return
     */
    protected QDLStem getQDLReservedNames() {
        // See also $NCSA_DEV/qdl/language/src/main/resources/all_ops.txt
        QDLStem out = new QDLStem();
        QDLStem keywords = new QDLStem();
        keywords.getQDLList().addAll(Arrays.asList(QDLConstants.KEYWORDS));
        out.put("keywords", asQDLValue(keywords));
        QDLStem funcs = new QDLStem();
        funcs.getQDLList().addAll(getMetaEvaluator().listFunctions(false));
        out.put("functions", asQDLValue(funcs));
        // Note that since the parser intercepts several of these, they cannot be
        // directly accessed
        QDLStem ops = new QDLStem();
        ArrayList<String> allOps = new ArrayList<>(OpEvaluator.ALL_MATH_OPS.length + OpEvaluator.OTHER_MATH_OPS.length);
        List<String> x = Arrays.asList(OpEvaluator.ALL_MATH_OPS);
        allOps.addAll(x);
        allOps.addAll(Arrays.asList(OpEvaluator.OTHER_MATH_OPS));
        HashSet<String> opsSet = new HashSet<>();
        opsSet.addAll(allOps); // uniquefy them
        ops.getQDLList().addAll(opsSet);
        out.put("operators", asQDLValue(ops));
        return out;
    }

    /**
     * Debug utility for QDL. Note that this is completely independent of the {@link edu.uiuc.ncsa.security.core.util.DebugUtil}
     * for the JVM, which can be toggled with the WS variable 'debug'
     *
     * @return
     */
    public MetaDebugUtil getDebugUtil() {
        if (debugUtil == null) {
            debugUtil = new MetaDebugUtil(QDLWorkspace.class.getSimpleName(),
                    MetaDebugUtil.DEBUG_LEVEL_OFF, // no debugging by default
                    true // print time stamps by default
            );
        }
        return debugUtil;
    }

    public void setDebugUtil(MetaDebugUtil debugUtil) {
        this.debugUtil = debugUtil;
    }

    MetaDebugUtil debugUtil = null;

    /**
     * If this is packaged in a jar, read off the information from the manifest file.
     * If no manifest, skip this.
     *
     * @return
     */
    protected QDLStem addManifestConstants(String path) {
        QDLStem versionInfo = new QDLStem();

        versionInfo.put(SYS_QDL_VERSION, asQDLValue(QDLVersion.VERSION));
        if (path == null) {
            return versionInfo;
        }
        ArrayList<String> manifest;
        try {
            List<String> temp = QDLFileUtil.readFileAsLines(path + (path.endsWith("/") ? "" : "/") + "lib/build-info.txt");
            if (temp instanceof ArrayList) {
                manifest = (ArrayList<String>) temp;
            } else {
                manifest = new ArrayList<>();
                manifest.addAll(temp);
            }
        } catch (Throwable throwable) {
            if (getLogger() != null) {
                getLogger().info("Could not find the build info file. Looked in " + path + (path.endsWith("/") ? "" : "/") + "lib/build-info.txt");
            }
            // At the least return the current version
            return versionInfo;
        }

        /*
            public static void main(String[] args) {
        State state = new State();
        MyLoggingFacade logger = new MyLoggingFacade("State");
        state.setLogger(logger);
        QDLStem stem =state.addManifestConstants("/home/ncsa/apps/qdl");
        System.out.println(stem);
    }


         */

        return versionInfo = processManifest2(versionInfo, manifest);
    }

    // Old version was too restrictive. Should just read all manifest constants
    // and have special handling of the few items like class path that are constant.
    protected QDLStem processManifest(QDLStem versionInfo, ArrayList<String> manifest) {
        for (int i = 0; i < manifest.size(); i++) {
            String linein = manifest.get(i);

            if (linein.startsWith("application-version:")) {
                // e.g.  application-version: 1.0.1
                versionInfo.put(SYS_QDL_BUILD_VERSION, asQDLValue(truncateLine("application-version:", linein)));
            }
            if (linein.startsWith("Build-Jdk:")) {
                // e.g. Build-Jdk: 1.8.0_231
                versionInfo.put(SYS_QDL_VERSION_BUILD_JDK, asQDLValue(truncateLine("Build-Jdk:", linein)));
            }
            if (linein.startsWith("build-time:")) {
                // e.g. build-time: 1586726889841
                try {
                    Long ts = Long.parseLong(truncateLine("build-time:", linein));
                    versionInfo.put(SYS_QDL_VERSION_BUILD_TIME, asQDLValue(Iso8601.date2String(ts)));
                } catch (Throwable t) {
                    versionInfo.put(SYS_QDL_VERSION_BUILD_TIME, asQDLValue("?"));
                }
            }
            if (linein.startsWith("Created-By:")) {
                // e.g. Created-By: Apache Maven 3.6.0
                versionInfo.put(SYS_QDL_VERSION_CREATED_BY, asQDLValue(truncateLine("Created-By:", linein)));
            }

            if (linein.startsWith("Class-Path:")) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(truncateLine("Class-Path:", linein));
                int j;

                for (j = i + 1; j < manifest.size(); j++) {
                    String currentLine = manifest.get(j);
                    if (currentLine.startsWith(" ")) {
                        stringBuffer.append(currentLine.substring(1)); // starts with a single added blank.
                    } else {
                        i = j - 1;
                        break;
                    }
                }

                versionInfo.put(SYS_QDL_BUILD_CLASS_PATH, asQDLValue(stringBuffer.toString()));
            }
            // There are some instances where this is munged. Only stick something there
            // if you can make sense of it.
            if (linein.startsWith("implementation-build:")) {
                // https://github.com/ncsa/qdl/issues/19
                try {
                    // e.g.     implementation-build: Build: #21 (2020-04-12T16:28:09.841-05:00)
                    String build = truncateLine("implementation-build:", linein);
                    build = build.substring(0, build.indexOf("("));
                    build = truncateLine("Build:", build);
                    if (build.startsWith("#")) {
                        build = build.substring(1);
                    }
                    versionInfo.put(SYS_QDL_VERSION_BUILD_NUMBER, asQDLValue(build));
                } catch (Throwable t) {
                    versionInfo.put(SYS_QDL_VERSION_BUILD_NUMBER, asQDLValue("(unknown)"));
                }
            }

        }
        return versionInfo;
    }

    protected QDLStem processManifest2(QDLStem versionInfo, ArrayList<String> manifest) {
        for (int i = 0; i < manifest.size(); i++) {
            String linein = manifest.get(i);
            if (StringUtils.isTrivial(linein)) {
                continue;
            }
            String head = linein.substring(0, linein.indexOf(":")).trim();
            switch (head) {
                case "Class-Path":
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(truncateLine("Class-Path:", linein));
                    int j;

                    for (j = i + 1; j < manifest.size(); j++) {
                        String currentLine = manifest.get(j);
                        if (currentLine.startsWith(" ")) {
                            stringBuffer.append(currentLine.substring(1)); // starts with a single added blank.
                        } else {
                            i = j - 1;
                            break;
                        }
                    }
                    versionInfo.put(SYS_QDL_BUILD_CLASS_PATH, asQDLValue(stringBuffer.toString()));
                    break;
                case "build-time":
                    try {
                        Long ts = Long.parseLong(truncateLine("build-time:", linein));
                        versionInfo.put(SYS_QDL_VERSION_BUILD_TIME, asQDLValue(Iso8601.date2String(ts)));
                    } catch (Throwable t) {
                        versionInfo.put(SYS_QDL_VERSION_BUILD_TIME,asQDLValue( "?"));
                    }
                    break;
                default:
                    versionInfo.put(head, asQDLValue(truncateLine(head + ":", linein)));


            }

        }
        return versionInfo;
    }

        /*
          OA4MP
                                    <manifestEntries>
                                    <application-version>${project.version}</application-version>
                                    <application-name>${project.name}</application-name>
                                    <application-title>OA4MP</application-title>
                                    <build-time>${timestamp}</build-time>
                                    <implementation-version>1.6-QDL-SNAPSHOT</implementation-version>
                                    <implementation-build>${buildNumber}</implementation-build>
                                    <implementation-title>QDL</implementation-title>
                                </manifestEntries>

         QDL                                 <manifestEntries>
                                             <application-version>${project.version}</application-version>
                                             <application-name>${project.name}</application-name>
                                             <build-time>${timestamp}</build-time>
                                             <implementation-version>${project.version}</implementation-version>
                                             <implementation-build>${buildNumber}</implementation-build>
                                         </manifestEntries>
         */

    /**
     * Truncate the line by dropping the head from it.
     *
     * @param head
     * @param line
     * @return
     */
    String truncateLine(String head, String line) {
        if (line.startsWith(head)) {
            return line.substring(head.length()).trim();
        }
        return line;
    }

    public boolean isServerMode() {
        return serverMode;
    }

    public void setServerMode(boolean serverMode) {
        this.serverMode = serverMode;
    }

    boolean serverMode = false;

    /**
     * In server mode, some IO for debugging with debugger is still be allowed. If this flag
     * is set true, then printing is not allowed nor is saving the workspace.
     *
     * @return
     */
    public boolean isRestrictedIO() {
        return restrictedIO;
    }

    public void setRestrictedIO(boolean restrictedIO) {
        this.restrictedIO = restrictedIO;
    }

    boolean restrictedIO = false;

    public HashMap<String, VFSFileProvider> getVfsFileProviders() {
        return vfsFileProviders;
    }

    public void setVfsFileProviders(HashMap<String, VFSFileProvider> vfsFileProviders) {
        this.vfsFileProviders = vfsFileProviders;
    }

    transient HashMap<String, VFSFileProvider> vfsFileProviders = new HashMap<>();

    public void addVFSProvider(VFSFileProvider scriptProvider) {
        vfsFileProviders.put(scriptProvider.getScheme() + VFSPaths.SCHEME_DELIMITER + scriptProvider.getMountPoint(), scriptProvider);
    }

    public boolean hasMountPoint(String mountPoint) {
        return vfsFileProviders.containsKey(mountPoint);
    }

    public void removeVFSProvider(String mountPoint) {
        vfsFileProviders.remove(mountPoint);
    }

    public void removeScriptProvider(String scheme) {
        vfsFileProviders.remove(scheme);
    }

    /**
     * Convenience to get a script from the VFS. This takes any file and tries to turn it in to a script,
     * so the "onus is on the app" to make sure this is a script.
     *
     * @param fqName
     * @return
     */
    public QDLScript getScriptFromVFS(String fqName) throws Throwable {
        VFSEntry entry = getFileFromVFS(fqName, AbstractEvaluator.FILE_OP_TEXT_STRING);
        if (entry == null) {
            return null;
        }
        if (entry.getType().equals(Scripts.SCRIPT)) {
            return (QDLScript) entry;
        }
        QDLScript s = new QDLScript(entry.getLines(), entry.getProperties());
        return s;
    }

    /**
     * Given a fully qualified path, find the VFS corresponding to the mount point and
     * return it or null if no such mount point exists
     *
     * @param fqName
     * @return
     * @throws Throwable
     */
    public VFSFileProvider getVFS(String fqName) throws Throwable {
        if (vfsFileProviders.isEmpty()) return null;
        VFSFileProvider vfsFileProvider = null;

        for (String mountPoint : vfsFileProviders.keySet()) {
            // key is of the form scheme#/mounpoint/ -- note trailing slash! This lets us tell
            // things like A#/a/b from A#/abc
            if (fqName.startsWith(mountPoint)) {
                return vfsFileProviders.get(mountPoint);
            }
        }
        return null;
    }

    public VFSEntry getFileFromVFS(String fqName, int type) throws Throwable {
        if (vfsFileProviders.isEmpty()) return null;
        VFSFileProvider vfsFileProvider = null;
        for (String key : vfsFileProviders.keySet()) {
            // key is of the form scheme#/mounpoint/ -- note trailing slash! This lets us tell
            // things like A#/a/b from A#/abc
            if (fqName.startsWith(key)) {
                vfsFileProvider = vfsFileProviders.get(key);
                break;
            }
        }
        if (vfsFileProvider == null) {
            return null;
        }
        return vfsFileProvider.get(fqName, type);
    }

    public boolean hasVFSProviders() {
        if (vfsFileProviders == null) return false;
        return !vfsFileProviders.isEmpty();
    }

    /**
     * Returns if the {@link VFSPaths#SCHEME_DELIMITER} is part of this path, while means it
     * is intended for the virtual file system.
     * @param path
     * @return
     */
    public boolean isVFSFile(String path) {
        if (path.startsWith(VFSPaths.SCHEME_DELIMITER) || path.indexOf(VFSPaths.SCHEME_DELIMITER) == -1) {
            return false;
        } // legit this is a file uri, not a virtual one
        return 0 < path.indexOf(VFSPaths.SCHEME_DELIMITER);
    }

/*
    public State newStateNoImports() {
        SymbolStack newStack = new SymbolStack(symbolStack.getParentTables());
        MIStack miStack = new MIStack();
        State newState = newInstance(newStack,
                getOpEvaluator(),
                getMetaEvaluator(),
                getFTStack(),
                getMTemplates(),
                miStack,
                getLogger(),
                isServerMode(),
                isRestrictedIO(),
                isAssertionsOn());
        newState.setScriptArgs(getScriptArgs());
        newState.setScriptPaths(getScriptPaths());
        newState.setModulePaths(getModulePaths());
        return newState;

    }
*/

    /**
     * Convenience method for {@link #newLocalState(State)} with a null argument
     *
     * @return State
     */
    public State newLocalState() {
        return newLocalState(null);
    }

    /**
     * Creates a new state object and pushes the moduleState's stacks onto
     * the current one. This means the resulting state inherits everything.
     *
     * @param moduleState
     * @return
     */
    public State newLocalState(State moduleState) {
        //   return newStateWithImportsOLD(moduleState);
        return newSelectiveState(moduleState);
    }

    public State newSelectiveState(State moduleState) {
        return newSelectiveState(moduleState, true);
    }

    /**
     * Create a clean state, taking the old modules from moduleState, all the functions and allowing
     * inheritance of the current variable stack
     *
     * @param moduleState
     * @param inheritVariables
     * @return
     */
    public State newSelectiveState(State moduleState, boolean inheritVariables) {
        return newSelectiveState(moduleState, true, inheritVariables);
    }

    /**
     * This will clone the current state and will add the modules (templates and instances -- old modules)
     * from moduleState. If pushFunctions is
     *
     * @param moduleState
     * @param inheritFunctions
     * @param inheritVariables
     * @return
     */
    public State newSelectiveState(State moduleState, boolean inheritFunctions, boolean inheritVariables) {
        return newSelectiveState(moduleState, true, inheritFunctions, inheritVariables, true);
    }

    /**
     * Create a new state based on the current state and choosing what to inherit. In this case, inherited
     * objects come first, items in current state are appended. This allows for overrides in
     * {@link ExpressionInterface#evaluate(State)} calls.
     *
     * @param moduleState
     * @param inheritModules
     * @param inheritFunctions
     * @param inheritVariables
     * @return
     */
    public State newSelectiveState(State moduleState,
                                   boolean inheritModules,
                                   boolean inheritFunctions,
                                   boolean inheritVariables,
                                   boolean inheritIntrinsics) {
        VStack newStack = new VStack(); // always creates an empty symbol table, replace it
        if (inheritVariables) {
            if (moduleState != null && !moduleState.vStack.isEmpty()) {
                //newStack.addAll(moduleState.symbolStack.getParentTables());
                newStack.appendTables(moduleState.vStack);
            }
            if (!vStack.isEmpty()) {
                newStack.appendTables(vStack);
            }
        }
/*
  Start here 8/21/2024: Converting old intrinsic vars to use new stack. Need to
  figure out how to push module intrinsic vars on. The next snippet is wrong, since
  it is boilerplated from the previous block and still has references to newStack
  directly. What is the right logic? This method is used in the import() function.
 */
        VStack iStack = new VStack(); // always creates an empty symbol table, replace it
        FStack<? extends FTable<? extends FKey, ? extends FunctionRecordInterface>> iftStack = new FStack();

        if (inheritIntrinsics) {
            if (moduleState != null && !moduleState.getIntrinsicVariables().isEmpty()) {
                //newStack.addAll(moduleState.symbolStack.getParentTables());
                iStack.appendTables(moduleState.getIntrinsicVariables());
            }
            if (!getIntrinsicVariables().isEmpty()) {
                iStack.appendTables(getIntrinsicVariables());
            }
            //and now functions
            if (moduleState != null && !moduleState.getIntrinsicFunctions().isEmpty()) {
                iftStack.appendTables(moduleState.getIntrinsicFunctions());
            }
            if (!getIntrinsicFunctions().isEmpty()) {
                iftStack.appendTables(getIntrinsicFunctions()); // pushes elements in reverse order
            }
        }

        FStack<? extends FTable<? extends FKey, ? extends FunctionRecordInterface>> ftStack = new FStack();
        if (inheritFunctions) {
            if (moduleState != null && !moduleState.getFTStack().isEmpty()) {
                ftStack.appendTables(moduleState.getFTStack());
            }
            if (!getFTStack().isEmpty()) {
                ftStack.appendTables(getFTStack()); // pushes elements in reverse order
            }
        }


        MTStack mtStack = new MTStack();
        MIStack miStack = new MIStack();

        if (inheritModules) {
            if (moduleState != null && !moduleState.getMTemplates().isEmpty()) {
                mtStack.appendTables(moduleState.getMTemplates());
            }

            if (!getMTemplates().isEmpty()) {
                mtStack.appendTables(getMTemplates());
            }

            if (moduleState != null && !moduleState.getMTemplates().isEmpty()) {
                mtStack.appendTables(moduleState.getMTemplates());
            }

            if (moduleState != null && !moduleState.getMInstances().isEmpty()) {
                miStack.appendTables(moduleState.getMInstances());
            }

            if (!getMInstances().isEmpty()) {
                miStack.appendTables(getMInstances());
            }
        }

        State newState = newInstance(
                newStack,
                getOpEvaluator(),
                getMetaEvaluator(),
                ftStack,
                mtStack,
                miStack,
                getLogger(),
                isServerMode(),
                isRestrictedIO(),
                isAssertionsOn());
        newState.setScriptArgStem(getScriptArgStem());
        newState.setIntrinsicVariables(iStack);
        newState.setIntrinsicFunctions(iftStack);
        newState.setScriptName(getScriptName());
        newState.setScriptPaths(getScriptPaths());
        newState.setModulePaths(getModulePaths());
        newState.setVfsFileProviders(getVfsFileProviders());
        newState.setIoInterface(getIoInterface());
        newState.setWorkspaceCommands(getWorkspaceCommands());
        newState.systemInfo = systemInfo;
        newState.setDebugUtil(getDebugUtil()); // share the debugger.
        newState.setSuperState(moduleState);
        if (moduleState != null) {
            newState.setModuleState(moduleState.isModuleState());
        }

        return newState;
    }


    /**
     * For the case where this has been deserialized and needs to have its transient
     * fields initialized. These are things like the {@link MetaEvaluator} that
     * should not be serialized or current mount points (which can't be serialized
     * because you'd have to serialize the entire backing file system to satisfy
     * the contract of serialization!)
     *
     * @param oldState
     */
    public void injectTransientFields(State oldState) {
        if (getMetaEvaluator() != null && getOpEvaluator() != null) {
            // This is effectively a check if this has been done. If so, don't re-inject state.
            return;
        }
        setLogger(oldState.getLogger()); // set the logger to whatever the current one is
        setMetaEvaluator(oldState.getMetaEvaluator());
        setOpEvaluator(oldState.getOpEvaluator());
        if (oldState.hasVFSProviders()) {
            setVfsFileProviders(new HashMap<>()); // Make sure something is in the current state before we muck with it.
            for (String name : oldState.getVfsFileProviders().keySet()) {
                addVFSProvider(oldState.getVfsFileProviders().get(name));
            }
        }
        // Each imported module has its state serialized too. Hence each has to have current
        // transient fields updated. This will act recursively (so imports in imports in imports etc.)

        for (Object mod : getMInstances().keySet()) {
            XKey xKey = (XKey) mod;
            getMInstances().getModule(xKey).getState().injectTransientFields(oldState);
        }
        setIoInterface(oldState.getIoInterface());
    }

    /**
     * This creates a completely clean state, using the current environment
     * (so modules and script paths, but not variables, modules etc.)
     * and preserves debugging
     *
     * @return
     */
    public State newCleanState() {
        // NOTE this has no parents. Modules have completely clear state when starting!
        State newState = newInstance(
                new VStack(),
                getOpEvaluator(),
                getMetaEvaluator(),
                new FStack(),
                new MTStack(),
                new MIStack(),
                getLogger(),
                isServerMode(),
                isRestrictedIO(),
                isAssertionsOn());
        newState.setScriptArgStem(getScriptArgStem());
        newState.setIoInterface(getIoInterface());
        newState.setScriptName(getScriptName());
        newState.setScriptPaths(getScriptPaths());
        newState.setModulePaths(getModulePaths());
        newState.setVfsFileProviders(getVfsFileProviders());
        newState.setDebugUtil(getDebugUtil());
        newState.systemInfo = systemInfo;//systemInfo can only be be created at startup.
        newState.setWorkspaceCommands(getWorkspaceCommands());
        return newState;
    }

    /**
     * Carries over modules and functions, but <b>not</b> variables.
     *
     * @return
     */
    public State newFunctionState() {
        return newSelectiveState(null, false);
    }

    /**
     * This is needed for XML deserialization and makes dummy state for everything, assuming the deserializer will
     * replace it all. Generally do not use outside of XML deserialization.
     */
    public State() {
        super(new VStack(), new OpEvaluator(), MetaEvaluator.getInstance(), new FStack(), new MTStack(), new MIStack(), new MyLoggingFacade((Logger) null));
    }

    /**
     * Add the module under the default alias
     *
     * @param module
     */
    public void addModule(Module module) {
        if (module instanceof JavaModule) {
            ((JavaModule) module).init(this.newCleanState());
        }
        getMTemplates().put(module);
    }

    public int getStackSize() {
        return getVStack().size();
    }

    public void toXML(XMLStreamWriter xsw, SerializationState serializationState) throws XMLStreamException {
        xsw.writeStartElement(STATE_TAG);
        xsw.writeAttribute(UUID_TAG, getInternalID());
        writeExtraXMLAttributes(xsw);
        //getSymbolStack().toXML(xsw);
        getVStack().toXML(xsw, serializationState);
        getFTStack().toXML(xsw, serializationState);
        if (!getIntrinsicVariables().isEmpty()) {
            getIntrinsicVariables().toXML(xsw, serializationState);
        }
        if (!getIntrinsicFunctions().isEmpty()) {
            getIntrinsicFunctions().toXML(xsw, serializationState);
        }
        getMTemplates().toXML(xsw, serializationState);
        getMInstances().toXML(xsw, serializationState);
        writeExtraXMLElements(xsw);
        xsw.writeEndElement(); // end state tag

    }

    public void toXML(XMLStreamWriter xsw) throws XMLStreamException {
        xsw.writeStartElement(STATE_TAG);
        xsw.writeAttribute(STATE_INTERNAL_ID_TAG, getInternalID());
        writeExtraXMLAttributes(xsw);
        // The list of aliases and their corresponding modules
        // NEXT BIT IS DONE IN NEW MODULE STACK, SEE BELOW
/*        if (!getMInstances().isEmpty()) {
            xsw.writeStartElement(OLD_IMPORTED_MODULES_TAG);
            xsw.writeComment("The imported modules with their state and alias.");
            for (String alias : getMInstances().keySet()) {
                Module module = getMInstances().get(alias);
                module.toXML(xsw, alias);
            }
            xsw.writeEndElement(); //end imports
        }*/
        // NOTE that the order in which things are serialized matters! Do module declarations first
        // then variables and functions. This means that on deserialization (which follows document order exactly)
        // any modules are prcocessed (including the setting of variables and functions) then the user's modifications
        // overwrite what is there. This way if the user has modified things, it is preserved.

        // Templates
        getMTemplates().toXML(xsw, null);

        // imports
        getMInstances().toXML(xsw, null);

        // Symbol stack has the variables
        getVStack().toXML(xsw, null);
        // Function table has the functions
        getFTStack().toXML(xsw, null);
        writeExtraXMLElements(xsw);
        xsw.writeEndElement(); // end state tag
    }

    public void fromXML(XMLEventReader xer, XProperties xp, SerializationState serializationState) throws XMLStreamException {
        // At this point, caller peeked and knows this is the right type of event,
        // so we know where in the stream we are starting automatically.

        XMLEvent xe = xer.nextEvent(); // start iteration it should be at the state tag
        if (xe.isStartElement() && xe.asStartElement().getName().getLocalPart().equals(STATE_TAG)) {
            internalID = xe.asStartElement().getAttributeByName(new QName(UUID_TAG)).getValue();
            readExtraXMLAttributes(xe.asStartElement());
        }
        while (xer.hasNext()) {
            xe = xer.peek(); // start iteration
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    switch (xe.asStartElement().getName().getLocalPart()) {
                        case VARIABLE_STACK:
                            if (serializationState.getVersion().equals(VERSION_2_0_TAG)) {
                                Iterator<Attribute> attr = xe.asStartElement().getAttributes();
                                while (attr.hasNext()) {
                                    Attribute attribute = attr.next();
                                    if (attribute.getName().getLocalPart().equals(VStack.VSTACK_VERSION_TAG)) {
                                        serializationState.setVariablesSerializationVersion(attribute.getValue());
                                    }
                                }
                                XMLUtilsV2.deserializeVariables(xer, this, serializationState);
                            } else {
                                // Legacy.
                                throw new NotImplementedException("Legacy viable state storage no longer supported.");
/*                                VStack vStack = new VStack();
                                SymbolStack st = new SymbolStack();
                                st.fromXML(xer);
                                // Have to transfer functions over.
                                vStack.fromJSON(st.toJSON(), null);
                                setvStack(vStack);*/
                            }
                            break;
                        case FUNCTION_TABLE_STACK_TAG:
                            if (serializationState.getVersion().equals(VERSION_2_0_TAG)) {
                                XMLUtilsV2.deserializeFunctions(xer, this, serializationState);
                            } else {
                                XMLUtils.deserializeFunctions(xer, xp, this);
                            }
                            break;
                        case INSTANCE_STACK:
                            if (serializationState.getVersion().equals(VERSION_2_0_TAG)) {
                                XMLUtilsV2.deserializeInstances(xer, this, serializationState);
                            } else {
                                XMLUtils.deserializeImports(xer, xp, this);
                            }
                            break;
                        case TEMPLATE_STACK:
                            if (serializationState.getVersion().equals(VERSION_2_0_TAG)) {
                                XMLUtilsV2.deserializeTemplates(xer, this, serializationState);
                            } else {
                                XMLUtils.deserializeTemplates(xer, xp, this);
                            }
                            break;
                        default:
                            readExtraXMLElements(xe, xer);
                            break;
                    }

                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(STATE_TAG)) {
                        return;
                    }
                    break;
            }
            xer.next(); // advance cursor
        }
        throw new XMLMissingCloseTagException(STATE_TAG);
    }

    public void fromXML(XMLEventReader xer, XProperties xp) throws XMLStreamException {
        // At this point, caller peeked and knows this is the right type of event,
        // so we know where in the stream we are starting automatically.

        XMLEvent xe = xer.nextEvent(); // start iteration it should be at the state tag
        if (xe.isStartElement() && xe.asStartElement().getName().getLocalPart().equals(STATE_TAG)) {
            internalID = xe.asStartElement().getAttributeByName(new QName(STATE_INTERNAL_ID_TAG)).getValue();
            readExtraXMLAttributes(xe.asStartElement());
        }
        while (xer.hasNext()) {
            xe = xer.peek(); // start iteration
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    switch (xe.asStartElement().getName().getLocalPart()) {
                        case STACKS_TAG:
                            throw new NotImplementedException("Legacy variable storage no longer supported");
/*                            SymbolStack st = new SymbolStack();
                            st.fromXML(xer);
                            VStack vStack = new VStack();
                            vStack.fromJSON(st.toJSON(), null);
                            setvStack(vStack);*/
                            //    break;
                        case FUNCTIONS_TAG:
                            XMLUtils.oldDeserializeFunctions(xer, xp, this);
                            break;
                        case FUNCTION_TABLE_STACK_TAG:
                            XMLUtils.deserializeFunctions(xer, xp, this);
                            break;
                        case OLD_IMPORTED_MODULES_TAG:
                            XMLUtils.deserializeImports(xer, xp, this);
                            break;
                        case OLD_MODULE_TEMPLATE_TAG:
                            XMLUtils.deserializeTemplates(xer, xp, this);
                            break;
                        default:
                            readExtraXMLElements(xe, xer);
                            break;
                    }

                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(STATE_TAG)) {
                        return;
                    }
                    break;
            }
            xer.next(); // advance cursor
        }
        throw new XMLMissingCloseTagException(STATE_TAG);
    }

    /**
     * This just drives running through a bunch of modules
     *
     * @param xer
     * @throws XMLStreamException
     */
    protected void doXMLImportedModules(XMLEventReader xer) throws XMLStreamException {
        XMLEvent xe = xer.nextEvent();
        while (xer.hasNext()) {
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    if (xe.asStartElement().getName().getLocalPart().equals(MODULE_TAG)) {
                        Module module = new Module() {
                            @Override
                            public Module newInstance(State state) {
                                return null;
                            }

                            List<String> doc = new ArrayList<>();

                            @Override
                            public List<String> getListByTag() {
                                return doc;
                            }

                            @Override
                            public void setDocumentation(List<String> documentation) {
                                doc = documentation;
                            }

                            @Override
                            public List<String> getDocumentation() {
                                return doc;
                            }

                            @Override
                            public List<String> createDefaultDocs() {
                                return doc;
                            }
                        };
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(OLD_IMPORTED_MODULES_TAG)) {
                        return;
                    }
            }
            xe = xer.nextEvent();
        }
        throw new XMLMissingCloseTagException(OLD_IMPORTED_MODULES_TAG);

    }

    /**
     * This is invoked at the end of the serialization and lets you add additional things to be serialized
     * in the State. All new elements are added right before the final closing tag for the state object.
     *
     * @param xsr
     * @throws XMLStreamException
     */
    public void writeExtraXMLElements(XMLStreamWriter xsr) throws XMLStreamException {
        xsr.writeStartElement(STATE_CONSTANTS_TAG);
        xsr.writeCData(Base64.encodeBase64URLSafeString(createConstants().toString().getBytes(StandardCharsets.UTF_8)));
        xsr.writeEndElement();
    }

    protected JSONObject createConstants() {
        JSONObject json = new JSONObject();
        json.put(STATE_ASSERTIONS_ENABLED_TAG, isAssertionsOn());
        json.put(DEBUG_LEVEL, getDebugUtil().getDebugLevel());
        json.put(STATE_ID_TAG, getStateID());
        json.put(STATE_RESTRICTED_IO_TAG, isRestrictedIO());
        json.put(STATE_SERVER_MODE_TAG, isServerMode());
        // Saving the numeric digits this way is unsatisfactory since it is also done in all
        // of the sub-states, but there is no easy way to set this once and get it right
        // The top-level state is the last read, so it wil always end up getting set
        // correctly at the end. Best we can do without a ton of machinery...
        json.put(STATE_NUMERIC_DIGITS_TAG, OpEvaluator.getNumericDigits());
        return json;
    }

    /**
     * This exists to let you add additional attributes to the state tag. It should <b><i>only</i></b>
     * contain {@link XMLStreamWriter#writeAttribute(String, String)} calls, nothing else.
     *
     * @param xsw
     * @throws XMLStreamException
     */
    public void writeExtraXMLAttributes(XMLStreamWriter xsw) throws XMLStreamException {

    }

    /**
     * This passes in the current start event so you can add your own event loop and cases.
     * Note you need have only a switch on the tag names you want.
     *
     * @param xe
     * @param xer
     * @throws XMLStreamException
     */

    public void readExtraXMLElements(XMLEvent xe, XMLEventReader xer) throws XMLStreamException {
        if (xe.asStartElement().getName().getLocalPart().equals(STATE_CONSTANTS_TAG)) {
            // only process the tag if it is the right one
            String text = XMLUtilsV2.getText(xer, STATE_CONSTANTS_TAG);
            text = new String(Base64.decodeBase64(text));
            readConstantsFromJSON(JSONObject.fromObject(text));
        }
    }

    protected void readConstantsFromJSON(JSONObject json) {
        setAssertionsOn(json.getBoolean(STATE_ASSERTIONS_ENABLED_TAG));
        if (json.containsKey(STATE_ID_TAG)) {
            setStateID(json.getInt(STATE_ID_TAG));
        } else {
            setStateID(0);
        }
        setServerMode(json.getBoolean(STATE_SERVER_MODE_TAG));
        setRestrictedIO(json.getBoolean(STATE_RESTRICTED_IO_TAG));
        OpEvaluator.setNumericDigits(json.getInt(STATE_NUMERIC_DIGITS_TAG));
        if (json.containsKey(DEBUG_LEVEL)) {
            getDebugUtil().setDebugLevel(json.getInt(DEBUG_LEVEL));
        }
    }

    /**
     * Allows you to read custom attributes from the state tag. This should <b><i>only</i></b> contain
     * calls to {@link StartElement#getAttributeByName(QName)} by name calls.
     *
     * @param xe
     * @throws XMLStreamException
     */
    public void readExtraXMLAttributes(StartElement xe) throws XMLStreamException {

    }

    SecureRandom secureRandom = new SecureRandom();
    transient Base32 base32 = new Base32('_'); // set trailing char to be an underscore

    /**
     * Returns an unused variable name.
     *
     * @return
     */
    public String getTempVariableName() {
        byte[] b = new byte[16];
        for (int i = 0; i < 10; i++) {
            String var = base32.encodeToString(b);
            if (!isDefined(var)) {
                return var;
            }
        }
        throw new NFWException("Was unable to create a random, unused variable");
    }

    public boolean isAssertionsOn() {
        return assertionsOn;
    }

    public void setAssertionsOn(boolean assertionsOn) {
        this.assertionsOn = assertionsOn;
    }

    boolean assertionsOn = true;

    /**
     * Allows back reference to workspace to run macros.
     *
     * @return
     */
    public WorkspaceCommands getWorkspaceCommands() {
        return workspaceCommands;
    }

    public void setWorkspaceCommands(WorkspaceCommands workspaceCommands) {
        this.workspaceCommands = workspaceCommands;
    }

    WorkspaceCommands workspaceCommands;

    /**
     * Recurse through the modules and collects templates and state objects from instances.
     * Now that templates and instances are handled as stacks with local state, old form of
     * serialization fails due to recursion.<br/><br/>
     * These are serialized into a flat list and references to them are used. This also
     * checks for cycles.
     *
     * @param SerializationState
     */
    public void buildSO(SerializationState SerializationState) {
        for (Object key : getMTemplates().keySet()) {
            MTKey mtKey = (MTKey) key;
            Module module = getMTemplates().getModule(mtKey);
            if (!SerializationState.processedTemplate(module)) {
                SerializationState.addTemplate(module);
            }
        }
        for (Object key : getMInstances().keySet()) {
            XKey xKey = (XKey) key;
            MIWrapper wrapper = (MIWrapper) getMInstances().get(xKey);
            Module module = wrapper.getModule();
            if (!SerializationState.processedInstance(module)) {
                SerializationState.addInstance(wrapper);
                if (!SerializationState.processedState(module.getState())) {
                    SerializationState.addState(module.getState());
                    module.getState().buildSO(SerializationState);
                }
            }

        }

    }

    public void setExtrinsicVars(VStack extrinsicVars) {
        State.extrinsicVars = extrinsicVars;
    }

    public VStack getExtrinsicVars() {
        if (extrinsicVars == null) {
            extrinsicVars = new VStack();
        }
        return extrinsicVars;
    }


    public static VStack extrinsicVars;

    public static void setExtrinsicFuncs(FStack extrinsicFuncs) {
        State.extrinsicFuncs = extrinsicFuncs;
    }

    public FStack getExtrinsicFuncs() {
        if (extrinsicFuncs == null) {
            extrinsicFuncs = new FStack();
        }
        return extrinsicFuncs;
    }

    public static FStack extrinsicFuncs;


    public boolean isAllowBaseFunctionOverrides() {
        return allowBaseFunctionOverrides;
    }

    public void setAllowBaseFunctionOverrides(boolean allowBaseFunctionOverrides) {
        this.allowBaseFunctionOverrides = allowBaseFunctionOverrides;
    }

    boolean allowBaseFunctionOverrides = false;

    State targetState = null;

    /**
     * The target state is used in cases where argument lists are processed. This allows for
     * the {@link org.qdl_lang.expressions.ANode2} assignment to a different state that
     * the calling state, in particular, this is how functions can assign variables in their
     * argument list only for the duration of the function.
     *
     * @return
     */
    public State getTargetState() {
        if (targetState == null) {
            return this;
        }
        return targetState;
    }

    public void setTargetState(State targetState) {
        this.targetState = targetState;
    }


    static State rootState = null;

    /**
     * At system startup this is set to be the top-level state object for the system. It is
     * used, e.g. in resolving default namepsace requests everywhere and should be set
     * exactly once on startup by the system.
     *
     * @return
     */
    // resolves https://github.com/ncsa/qdl/issues/24
    public static State getRootState() {
        return rootState;
    }

    public static void setRootState(State newRoot) {
        rootState = newRoot;
    }

    /**
     * Only serialize the local part of the state. This is used by e.g., modules that
     * have shared state and should never overwrite shared state on deserialization.
     *
     * @param serializationState
     * @return
     */
    public JSONObject serializeLocalStateToJSON(SerializationState serializationState) throws Throwable {
        JSONObject jsonObject = new JSONObject();
        doLocalSerialization(getMTemplates(), MODULE_TEMPLATE_TAG, jsonObject, serializationState);
        doLocalSerialization(getMInstances(), MODULE_INSTANCES_TAG, jsonObject, serializationState);
        doLocalSerialization(getFTStack(), FUNCTION_TABLE_STACK_TAG, jsonObject, serializationState);
        doLocalSerialization(getVStack(), VARIABLE_STACK, jsonObject, serializationState);
        if (!getIntrinsicVariables().isEmpty()) {
            doLocalSerialization(getIntrinsicVariables(), INTRINSIC_VARIABLES_TAG, jsonObject, serializationState);
        }
        if (!getIntrinsicFunctions().isEmpty()) {
            doLocalSerialization(getIntrinsicFunctions(), INTRINSIC_FUNCTIONS_TAG, jsonObject, serializationState);
        }

        return jsonObject;
    }

    protected void doLocalSerialization(
            XStack oldStack,
            String tag,
            JSONObject jsonObject,
            SerializationState serializationState) throws Throwable {
        XStack newStack = oldStack.newInstance();
        newStack.push(oldStack.getLocal());
        addJSONtoState(jsonObject, tag, newStack, serializationState);
    }

    public JSONObject serializeToJSON(SerializationState s) throws Throwable {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(STATE_CONSTANTS_TAG, createConstants());
        SerializationState serializationState = new SerializationState();
        serializationState.setVersion(s.getVersion());
        serializationState.setVariablesSerializationVersion(s.getVariablesSerializationVersion());
        serializationState.addTemplates(getMTemplates());
        addJSONtoState(jsonObject, MODULE_TEMPLATE_TAG, getMTemplates(), serializationState);
        addJSONtoState(jsonObject, MODULE_INSTANCES_TAG, getMInstances(), serializationState);
        addJSONtoState(jsonObject, FUNCTION_TABLE_STACK_TAG, getFTStack(), serializationState);
        addJSONtoState(jsonObject, VARIABLE_STACK, getVStack(), s);
        if (!getIntrinsicVariables().isEmpty()) {
            addJSONtoState(jsonObject, INTRINSIC_VARIABLES_TAG, getIntrinsicVariables(), s);
        }
        if (!getUsedModules().isEmpty()) {
            ModuleUtils moduleUtils = new ModuleUtils();
            JSONArray array = moduleUtils.serializeUsedModules(this, serializationState);
            if (array != null && !array.isEmpty()) {
                jsonObject.put(USED_MODULES, array);
            }
        }
        return jsonObject;
    }

    protected void addJSONtoState(JSONObject jsonObject, String tag, XStack xStack, SerializationState serializationState) throws Throwable {
        if (!(xStack == null || xStack.isEmpty())) {
            JSONObject j = xStack.serializeToJSON(serializationState);
            if (j != null) {
                jsonObject.put(tag, j);
            }
        }
    }

    public void deserializeFromJSON(JSONObject jsonObject, SerializationState s) throws Throwable {
        if (jsonObject.containsKey(STATE_CONSTANTS_TAG)) {
            readConstantsFromJSON(jsonObject.getJSONObject(STATE_CONSTANTS_TAG));
        }
        SerializationState serializationState = new SerializationState();
        serializationState.setVersion(s.getVersion());
        serializationState.setVariablesSerializationVersion(s.getVariablesSerializationVersion());
        makeStack(getMTemplates(), jsonObject, MODULE_TEMPLATE_TAG, serializationState);
        serializationState.addTemplates(getMTemplates());
        makeStack(getMInstances(), jsonObject, MODULE_INSTANCES_TAG, serializationState);
        makeStack(getFTStack(), jsonObject, FUNCTION_TABLE_STACK_TAG, serializationState);
        makeStack(getVStack(), jsonObject, VARIABLE_STACK, serializationState);
        makeStack(getIntrinsicVariables(), jsonObject, INTRINSIC_VARIABLES_TAG, serializationState);
        makeStack(getIntrinsicFunctions(), jsonObject, INTRINSIC_FUNCTIONS_TAG, serializationState);
        if (jsonObject.containsKey(USED_MODULES)) {
            ModuleUtils moduleUtils = new ModuleUtils();
            moduleUtils.deserializeUsedModules(this,
                    jsonObject.getJSONArray(USED_MODULES),
                    serializationState);
        }
    }

    XStack makeStack(XStack xStack, JSONObject jsonObject, String tag, SerializationState serializationState) {
        if (jsonObject.containsKey(tag)) {
            xStack.deserializeFromJSON(jsonObject.getJSONObject(tag), serializationState, this);
        }
        return xStack;
    }


    public static void main(String[] args) {
        State state = new State();
        MyLoggingFacade logger = new MyLoggingFacade("State");
        state.setLogger(logger);
        QDLStem stem = state.addManifestConstants("/home/ncsa/apps/qdl");
        System.out.println(stem);
    }
}
