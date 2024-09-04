package org.qdl_lang.config;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.exceptions.QDLException;
import org.qdl_lang.gui.FontUtil;
import org.qdl_lang.state.LibLoader;
import org.qdl_lang.util.QDLVersion;
import org.qdl_lang.workspace.WorkspaceCommands;
import edu.uiuc.ncsa.security.core.configuration.StorageConfigurationTags;
import edu.uiuc.ncsa.security.core.util.*;
import edu.uiuc.ncsa.security.util.cli.editing.EditorEntry;
import edu.uiuc.ncsa.security.util.cli.editing.EditorUtils;
import edu.uiuc.ncsa.security.util.cli.editing.Editors;
import edu.uiuc.ncsa.security.util.configuration.XMLConfigUtil;
import org.apache.commons.configuration.tree.ConfigurationNode;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.uiuc.ncsa.security.core.configuration.Configurations.*;
import static edu.uiuc.ncsa.security.core.util.StringUtils.isTrivial;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/27/20 at  7:37 AM
 */
public class QDLConfigurationLoader<T extends QDLEnvironment> extends LoggingConfigLoader<T> implements QDLConfigurationConstants, ConfigurationLoader<T> {
    public QDLConfigurationLoader(String cfgFile, ConfigurationNode node) {
        super("qdl.log", "qdl", node, null); // This makes it read the logging configuration and create the logger. Logging should just work
        configFile = cfgFile;
    }

    /**
     * Use only in those cases where the logger is constructed elsewhere. This will override the logging
     * configuration and use this logger instead.
     *
     * @param node
     * @param logger
     */
    public QDLConfigurationLoader(String cfgFile, ConfigurationNode node, MyLoggingFacade logger) {
        // set defaults for the logger if none configured or you get references to NCSA Delegation
        super("qdl.log", "qdl", node, logger);
        configFile = cfgFile;
    }

    @Override
    public T load() {
        T env = createInstance();
        return env;
    }

    /**
     * This is set to point to the configuration file (that information is not contained inside the file). It is optional.
     *
     * @param configFile
     */
    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    protected String getConfigFile() {
        return configFile;
    }

    String configFile = null;

    protected String getBootScript() {
        String x = getFirstAttribute(cn, BOOT_SCRIPT_TAG);
        return x == null ? "" : x;
    }

    protected String getWSEnvFile() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getNodeValue(node, WS_ENV, "");
    }

    protected String getWSHomeDir() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getNodeValue(node, WS_HOME_DIR_TAG, "");
    }

    protected Font getFont() {
        if(GraphicsEnvironment.isHeadless()){
            return null;
        }
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        ConfigurationNode fontNode = getFirstNode(node, WS_FONT_TAG);
        if (fontNode == null) return null;
        String name = getFirstAttribute(fontNode, WS_ATTR_FONT_NAME);
        String rawStyle = getFirstAttribute(fontNode, WS_ATTR_FONT_TYPE);
        String rawSize = getFirstAttribute(fontNode, WS_ATTR_FONT_SIZE);
        name = (name == null) ? DEFAULT_FONT_NAME : name;
        rawStyle = (rawStyle == null) ? "bold" : rawStyle;
        rawSize = (rawSize == null) ? "14" : rawSize;
        int type = FontUtil.getStyle(rawStyle);
        int size = DEFAULT_FONT_SIZE;
        try {
            size = Integer.parseInt(rawSize);
        } catch (NumberFormatException nfx) {

        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        for(String f : fonts){
            if(f.equals(name)){
                return new Font(name, type, size);
            }
        }
        if(isWSVerboseOn()){
            System.out.println("warn-- could not find font named '" + name + "', loading default font");
        }
        return new Font(DEFAULT_FONT_NAME, DEFAULT_FONT_STYLE, DEFAULT_FONT_SIZE);
    }

    /*
      Note on loading a custom font. This would do it if we want to, but it gets messy.

      try {
          // As a resource in the fonts folder:
          InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("/fonts/custom_font.ttf")
          Font customFont = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(48f);
          // As a file
          Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("/path/to/custom_font.ttf")).deriveFont(12f);
          // NOTE THAT THE deriveFont FONT SIZE IS A FLOAT!!!
          // Since the derive method (would create a possibly smoother font if the point size has to be interpolated)
          // expects the style to be an integer, it does a fake overload
          // of the size and passes it as a float. If you pass it, say 12, as the size vs. 12F,
          // it would throw an exception because the styles are in the range 0 (PLAIN) to 3 (BOLD+ITALIC).
      } catch (IOException e) {
          e.printStackTrace();
      } catch(FontFormatException e) {
          e.printStackTrace();
      }

          // Much later, however, this font can only be used when there is a graphical environment, so
          // in the SwingTerminal during setup, it would need to be communicated this has to be registered
          // and then
          GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
          //register the font
          ge.registerFont(customFont);
          // Now you can just set the font for a Swing component using setFont(customFont).

     */
    public static String DEFAULT_FONT_NAME = "Monospaced";
    public static int DEFAULT_FONT_STYLE = Font.BOLD;
    public static int DEFAULT_FONT_SIZE = 14;

    protected boolean getCompressionOn() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getFirstBooleanValue(node, WS_COMPRESS_SERIALIZATION_TAG, true);
    }

    protected String getScriptPath() {
        String x = getFirstAttribute(cn, SCRIPT_PATH_TAG);
        return x == null ? "" : x;
    }

    protected String getLibPath() {
        String x = getFirstAttribute(cn, LIB_PATH_TAG);
        return x == null ? "" : x;
    }

    protected String getModulePath() {
        String x = getFirstAttribute(cn, MODULE_PATH_TAG);
        return x == null ? "" : x;
    }

    protected boolean getFirstBooleanValue(ConfigurationNode node, String attrib, boolean defaultValue) {
        if (node == null) return defaultValue;
        try {
            String x = getFirstAttribute(node, attrib);
            if (isTrivial(x)) {
                return defaultValue;
            } //  Null argument returns false.
            return Boolean.parseBoolean(x);
        } catch (Throwable t) {

        }
        return defaultValue;
    }

    protected boolean useWSExternalEditor() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getFirstBooleanValue(node, WS_EDITOR_ENABLE, false);
    }

    Boolean ansiModeOn = null;

    protected Boolean isAnsiModeOn() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getFirstBooleanValue(node, WS_ATTR_ANSI_MODE_ON, false);
    }

    protected String getExternalEditorPath() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        String x = getFirstAttribute(node, WS_EDITOR_NAME);
        if (isTrivial(x)) {
            return "";
        }
        return x;
    }

    protected String getSaveDir() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        String x = getFirstAttribute(node, WS_SAVE_DIR);
        if (isTrivial(x)) {
            return null; // means none set.
        }
        return x;
    }

    protected Editors getEditors() {
        Editors editors = EditorUtils.getEditors(cn); // never null
        if (!editors.hasEntry(WorkspaceCommands.LINE_EDITOR_NAME)) {
            /*
               Create the line editor so there is always something available.
             */
            EditorEntry qdlEditor = new EditorEntry();
            qdlEditor.name = WorkspaceCommands.LINE_EDITOR_NAME;
            //qdlEditor.exec = null;
            //qdlEditor.clearScreen = false;
            editors.put(qdlEditor);
        }
        return editors;
    }


    protected boolean isWSVerboseOn() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getFirstBooleanValue(node, WS_ATTR_VERBOSE, false);
    }

    protected boolean showBanner() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getFirstBooleanValue(node, WS_ATTR_SHOW_BANNER, true);
    }

    protected String useLogo() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        String logo = getFirstAttribute(node, WS_ATTR_logo);
        if (StringUtils.isTrivial(logo)) return "default";
        return logo;
    }

    protected String getTerminalType() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        String terminalType = getFirstAttribute(node, WS_ATTR_TERMINAL_TYPE);
        if (StringUtils.isTrivial(terminalType)) {
            terminalType = getFirstAttribute(node, WS_ATTR_TERMINAL_TYPE2); // in case they used the alternate
        }
        if (StringUtils.isTrivial(terminalType)) return WS_TERMINAL_TYPE_TEXT; // still nothing. Use default
        return terminalType;
    }

    protected boolean isEnabled() {
        return getFirstBooleanValue(cn, CONFG_ATTR_ENABLED, true);
    }


    protected String getName() {
        String name = getFirstAttribute(cn, CONFG_ATTR_NAME);
        if (isTrivial(name)) {
            return "(none)";
        }
        return name;
    }

    protected boolean isOverwriteBaseFunctionsOn() {
        return getFirstBooleanValue(cn, CONFG_ATTR_OVERWRITE_BASE_FUNCTIONS_ENABLED, false);
    }

    protected boolean isServerModeOn() {
        return getFirstBooleanValue(cn, CONFG_ATTR_SERVER_MODE_ENABLED, false);
    }

    protected boolean isRestrictedIO() {
        return getFirstBooleanValue(cn, CONFG_ATTR_RESTRICTED_IO_RESTRICTED, false);
    }


    protected String getDebugLevel() {
        String level = getFirstAttribute(cn, CONFG_ATTR_DEBUG);
        if (level == null) {
            level = DebugUtil.DEBUG_LEVEL_OFF_LABEL;
        }
        return level;
    }

    protected boolean isEchoModeOn() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getFirstBooleanValue(node, WS_ATTR_ECHO_MODE_ON, true);
    }

    protected boolean areAssertionsEnabled() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getFirstBooleanValue(node, WS_ATTR_ASSERTIONS_ON, true);
    }

    protected boolean isPrettyPrint() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getFirstBooleanValue(node, WS_ATTR_PRETTY_PRINT, false);
    }

    protected boolean isAutosaveOn() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getFirstBooleanValue(node, WS_ATTR_AUTOSAVE_ON, false);
    }

    protected boolean isAutosaveMessagesOn() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getFirstBooleanValue(node, WS_ATTR_AUTOSAVE_MESSAGES_ON, false);
    }

    protected long getAutosaveInterval() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        String rawValue = getFirstAttribute(node, WS_ATTR_AUTOSAVE_INTERVAL);
        long autosaveInterval = 10 * 60 * 1000L; // 10 minutes in milliseconds.
        if (rawValue != null) {
            autosaveInterval = XMLConfigUtil.getValueSecsOrMillis(rawValue);
        }
        return autosaveInterval;
    }

    protected boolean isEnableLibrarySupport() {
        return getFirstBooleanValue(cn, ENABLE_LIBRARY_SUPPORT, true);
    }

    protected boolean isRunInitOnLoad() {
        ConfigurationNode node = getFirstNode(cn, WS_TAG);
        return getFirstBooleanValue(cn, RUN_INIT_ON_LOAD, true);

    }

    protected int getNumericDigits() {
        String raw = getFirstAttribute(cn, CONFG_ATTR_NUMERIC_DIGITS);
        if (isTrivial(raw)) {
            return OpEvaluator.numericDigits;
        }
        try {
            return Integer.parseInt(raw);
        } catch (Throwable t) {
            return OpEvaluator.numericDigits;
        }
    }

    protected List<VFSConfig> getVFSConfigs() {
        ArrayList<VFSConfig> configs = new ArrayList<>();
        ConfigurationNode vNode = getFirstNode(cn, VIRTUAL_FILE_SYSTEMS_TAG_NAME);
        if (vNode == null) {
            return new ArrayList<>();
        }
        // need to snoop through children and create VFSEntries.
        for (ConfigurationNode kid : vNode.getChildren()) {
            String access = getFirstAttribute(kid, VFS_ATTR_ACCESS);
            VFSAbstractConfig v = null;
            switch (getFirstAttribute(kid, VFS_ATTR_TYPE)) {
                case VFS_TYPE_PASS_THROUGH:
                    v = new VFSPassThroughConfig(
                            getNodeValue(kid, VFS_ROOT_DIR_TAG),
                            getNodeValue(kid, VFS_SCHEME_TAG),
                            getNodeValue(kid, VFS_MOUNT_POINT_TAG),
                            access.contains("r"),
                            access.contains("w")
                    );
                    break;
                case VFS_TYPE_ZIP:
                    v = new VFSZipFileConfig(getNodeValue(kid, VFS_ZIP_FILE_PATH),
                            getNodeValue(kid, VFS_SCHEME_TAG),
                            getNodeValue(kid, VFS_MOUNT_POINT_TAG),
                            access.contains("r"),
                            false);
                    break;
                case VFS_TYPE_MYSQL:
                    VFSSQLConfig vv = new VFSSQLConfig(
                            getNodeValue(kid, VFS_SCHEME_TAG),
                            getNodeValue(kid, VFS_MOUNT_POINT_TAG),
                            access.contains("r"),
                            access.contains("w")
                    );
                    ConfigurationNode myNode = getFirstNode(kid, StorageConfigurationTags.MYSQL_STORE);
                    // stash everything into the map.
                    Map<String, String> map = vv.getConnectionParameters();
                    for (ConfigurationNode attr : myNode.getAttributes()) {
                        map.put(attr.getName(), attr.getValue().toString());
                    }
                    // Add the defaults here if they are missing
                    v = vv;
                    break;
                case VFS_TYPE_MEMORY:
                    v = new VFSMemoryConfig(
                            getNodeValue(kid, VFS_SCHEME_TAG),
                            getNodeValue(kid, VFS_MOUNT_POINT_TAG),
                            access.contains("r"),
                            access.contains("w")
                    );
                    break;
                default:
                    throw new QDLException("Error: unsupported VFS type of " + getFirstAttribute(kid, VFS_ATTR_TYPE));

            }
            configs.add(v);

        }
        return configs;
    }

    public LibLoader getLibLoader() {
        return libLoader;
    }

    LibLoader libLoader = null;

    protected List<ModuleConfig> getModuleConfigs() {
        ArrayList<ModuleConfig> configs = new ArrayList<>();
        ConfigurationNode vNode = getFirstNode(cn, MODULES_TAG_NAME);
        if (vNode == null) {
            return new ArrayList<>();
        }
        String defaultVersion = MODULE_ATTR_VERSION_1_0;
        boolean defaultFailOnError = false;
        boolean defaultImportOnStart = true;
        if (getFirstAttribute(vNode, MODULE_ATTR_VERSION) != null) {
            defaultVersion = getFirstAttribute(vNode, MODULE_ATTR_VERSION);
        }
        if (getFirstAttribute(vNode, MODULE_FAIL_ON_ERRORS) != null) {
            defaultFailOnError = getFirstBooleanValue(vNode, MODULE_FAIL_ON_ERRORS, defaultFailOnError);
        }
        if (getFirstAttribute(vNode, MODULE_ATTR_IMPORT_ON_START) != null) {
            defaultImportOnStart = getFirstBooleanValue(vNode, MODULE_ATTR_IMPORT_ON_START, defaultImportOnStart);
        }
        String libLoaderName = getFirstAttribute(vNode, MODULE_ATTR_LIB_LOADER);
        if (!StringUtils.isTrivial(libLoaderName)) {
            try {
                Class<?> clazz = Class.forName(libLoaderName);
                libLoader = (LibLoader) clazz.getConstructor().newInstance();
            } catch (Throwable t) {
                if (myLogger != null) {
                    myLogger.warn("could not find library loader '" + libLoaderName + "'" + t.getMessage());
                } else {
                    DebugUtil.trace(this, "could not find library loader'" + libLoaderName + "':" + t.getMessage());
                }
            }
        }
        // need to snoop through children and create VFSEntries.
        for (ConfigurationNode kid : vNode.getChildren()) {
            boolean gotOne = false;
            ModuleConfigImpl v = null;
            if (getFirstAttribute(kid, MODULE_ATTR_TYPE).equals(MODULE_TYPE_JAVA)) {
                gotOne = true;
                JavaModuleConfig vv = new JavaModuleConfig();
                vv.setClassName(getNodeValue(kid, MODULE_CLASS_NAME_TAG));
                v = vv;
            }
            if (getFirstAttribute(kid, MODULE_ATTR_TYPE).equals(MODULE_TYPE_QDL)) {
                gotOne = true;
                QDLModuleConfig vv = new QDLModuleConfig();
                vv.setPath(getNodeValue(kid, QDL_MODULE_PATH_TAG));
                v = vv;
            }
            if (gotOne) {
                if (getFirstAttribute(kid, MODULE_ATTR_IMPORT_ON_START) == null) {
                    v.setImportOnStart(defaultImportOnStart);
                } else {
                    v.setImportOnStart(getFirstBooleanValue(kid, MODULE_ATTR_IMPORT_ON_START, defaultImportOnStart));
                }
                v.setUse(getFirstBooleanValue(kid, MODULE_ATTR_USE_MODULE, false));
                if (!v.isUse()) {
                    v.setVarName(getFirstAttribute(kid, MODULE_ATTR_ASSIGN_VARIABLE));
                }
                if (null == getFirstAttribute(kid, MODULE_ATTR_VERSION)) {
                    v.setVersion(defaultVersion);
                } else {
                    v.setVersion(getFirstAttribute(kid, MODULE_ATTR_VERSION));
                }
                if (getFirstAttribute(kid, MODULE_FAIL_ON_ERRORS) == null) {
                    v.setFailOnError(defaultFailOnError);
                } else {
                    v.setFailOnError(getFirstBooleanValue(kid, MODULE_FAIL_ON_ERRORS, defaultFailOnError));
                }
                configs.add(v);
            }
        }
        return configs;

    }

    @Override
    public T createInstance() {
        return (T) new QDLEnvironment(
                myLogger,
                getConfigFile(),
                getName(),
                isEnabled(),
                isServerModeOn(),
                isRestrictedIO(),
                getNumericDigits(),
                getBootScript(),
                getWSHomeDir(),
                getWSEnvFile(),
                isEchoModeOn(),
                isPrettyPrint(),
                isWSVerboseOn(),
                getCompressionOn(),
                showBanner(),
                getVFSConfigs(),
                getModuleConfigs(),
                getScriptPath(),
                getModulePath(),
                getLibPath(),
                getDebugLevel(),
                isAutosaveOn(),
                getAutosaveInterval(),
                isAutosaveMessagesOn(),
                useWSExternalEditor(),
                getExternalEditorPath(),
                getEditors(),
                isEnableLibrarySupport(),
                areAssertionsEnabled(),
                getSaveDir(),
                isOverwriteBaseFunctionsOn(),
                getLibLoader(),
                isAnsiModeOn(),
                useLogo(),
                getTerminalType(),
                getFont());
    }

    @Override
    public HashMap<String, String> getConstants() {
        return null;
    }


    public static void main(String[] args) {
        String path = DebugUtil.getDevPath() + "/qdl/language/src/main/resources/qdl-cfg.xml";
        ConfigurationNode node = XMLConfigUtil.findConfiguration(path, "test", QDLConfigurationConstants.CONFIG_TAG_NAME);

        QDLConfigurationLoader loader = new QDLConfigurationLoader(path, node);

        QDLEnvironment config = loader.load();
        System.out.println("Root node = " + node.getName());
        System.out.println("is enabled = " + config.isEnabled());
        System.out.println("server mode on = " + config.isServerModeOn());
        System.out.println("boot script = " + config.getBootScript());
        System.out.println("ws env = " + config.getWSEnv());
        System.out.println("ws home dir = " + config.getWSHomeDir());
        System.out.println("ws verbose = " + config.isWSVerboseOn());
        System.out.println("ws echo mode on = " + config.isEchoModeOn());
        System.out.println("ws pretty print = " + config.isPrettyPrint());
        System.out.println("vfs config = " + config.getVFSConfigurations());
        System.out.println("module config = " + config.getModuleConfigs());
    }

    @Override
    public String getVersionString() {
        return QDLVersion.VERSION;
    }
}
