package org.qdl_lang.config;

import org.qdl_lang.evaluate.ModuleEvaluator;
import org.qdl_lang.evaluate.SystemEvaluator;
import org.qdl_lang.exceptions.ReturnException;
import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.expressions.module.MIWrapper;
import org.qdl_lang.expressions.module.MTKey;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.scripting.QDLScript;
import org.qdl_lang.state.State;
import org.qdl_lang.util.QDLFileUtil;
import edu.uiuc.ncsa.security.core.configuration.StorageConfigurationTags;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.storage.sql.mysql.MySQLConnectionParameters;
import edu.uiuc.ncsa.security.storage.sql.mysql.MySQLConnectionPool;
import org.qdl_lang.vfs.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;

import static org.qdl_lang.config.QDLConfigurationConstants.*;
import static org.qdl_lang.evaluate.SystemEvaluator.resolveScript;
import static edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider.*;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * Takes  node from a QDL configuration and sets a bunch of information for a configuration.
 * <p>Created by Jeff Gaynor<br>
 * on 2/27/20 at  3:52 PM
 */
public class QDLConfigurationLoaderUtils {
    public static void setupVFS(QDLEnvironment config, State state) {
        if (!config.getVFSConfigurations().isEmpty()) {
            VFSFileProvider provider;
            for (VFSConfig vfsConfig : config.getVFSConfigurations()) {
                switch (vfsConfig.getType()) {
                    case VFS_TYPE_PASS_THROUGH:
                        VFSPassThroughConfig vpt = (VFSPassThroughConfig) vfsConfig;
                        provider = new VFSPassThruFileProvider(
                                vpt.getRootDir(), vpt.getScheme(), vpt.getMountPoint(),
                                vpt.canRead(), vpt.canWrite());
                        state.addVFSProvider(provider);
                        config.getMyLogger().info("VFS pass-through mount: " + vfsConfig.getScheme() +
                                VFSPaths.SCHEME_DELIMITER + vfsConfig.getMountPoint());
                        break;
                    case VFS_TYPE_MEMORY:
                        provider = new VFSMemoryFileProvider(
                                vfsConfig.getScheme(), vfsConfig.getMountPoint(), vfsConfig.canRead(), vfsConfig.canWrite());
                        state.addVFSProvider(provider);
                        config.getMyLogger().info("VFS in-memory mount: " + vfsConfig.getScheme() +
                                VFSPaths.SCHEME_DELIMITER + vfsConfig.getMountPoint());
                        break;
                    case VFS_TYPE_MYSQL:
                        provider = setupMySQLVFS(config, (VFSSQLConfig) vfsConfig);
                        state.addVFSProvider(provider);
                        config.getMyLogger().info("VFS MySQL mount: " + vfsConfig.getScheme() +
                                VFSPaths.SCHEME_DELIMITER + vfsConfig.getMountPoint());
                        break;
                    case VFS_TYPE_ZIP:
                        VFSZipFileConfig zc = (VFSZipFileConfig) vfsConfig;
                        provider = new VFSZipFileProvider(zc.getZipFilePath(), zc.getScheme(), zc.getMountPoint(),
                                zc.canRead(), zc.canWrite());
                        state.addVFSProvider(provider);
                        config.getMyLogger().info("VFS zip file mount: " + vfsConfig.getScheme() +
                                VFSPaths.SCHEME_DELIMITER + vfsConfig.getMountPoint());

                        break;
                    default:
                        config.getMyLogger().warn("VFS mount: Unknown VFS type " + vfsConfig.getType() + ", scheme= " + vfsConfig.getScheme() +
                                VFSPaths.SCHEME_DELIMITER + vfsConfig.getMountPoint());

                }

            }
        }
    }

    public static VFSDatabase setupMySQLDatabase(MyLoggingFacade logger, Map<String, String> map) {
        int port = 3306; // means use default
        boolean useSSL = false; // also default
        if (!map.containsKey(DRIVER)) {
            map.put(DRIVER, "com.mysql.cj.jdbc.Driver");
        }
        if (!map.containsKey(HOST)) {
            map.put(HOST, "localhost");
        }
        if (!map.containsKey(DATABASE)) {
            map.put(DATABASE, map.get(SCHEMA));
        }
        try {
            if (map.containsKey(PORT)) {
                port = Integer.parseInt(map.get(PORT));
            }
        } catch (Throwable t) {
            if (logger != null)
                logger.warn("VFS mount: Could not determine port from value " + map.get(PORT));
        }
        try {
            if (map.containsKey(USE_SSL)) {
                useSSL = Boolean.parseBoolean(map.get(USE_SSL));
            }
        } catch (Throwable t) {
            if (logger != null)
                logger.warn("VFS mount: Could not determine whether or not to use SSL from value " + map.get(USE_SSL));
        }
        MySQLConnectionParameters parameters = new MySQLConnectionParameters(
                map.get(USERNAME),
                map.get(PASSWORD),
                map.get(DATABASE),
                map.get(SCHEMA),
                map.get(HOST),
                port,
                map.get(DRIVER),
                useSSL,
                map.get(PARAMETERS)
        );
        MySQLConnectionPool connectionPool = new MySQLConnectionPool(parameters);

        return new VFSDatabase(connectionPool, map.get(StorageConfigurationTags.SQL_TABLENAME));

    }

    protected static VFSFileProvider setupMySQLVFS(QDLEnvironment config, VFSSQLConfig myCfg) {
        VFSFileProvider provider;
        Map<String, String> map = myCfg.getConnectionParameters();

        VFSDatabase db = setupMySQLDatabase(config.getMyLogger(), map);
        provider = new VFSMySQLProvider(db,
                myCfg.getScheme(), myCfg.mountPoint, myCfg.canRead(), myCfg.canWrite());
        return provider;
    }

    public static int JAVA_MODULE_INDEX = 0;
    public static int QDL_MODULE_INDEX = 1;
    public static int MODULE_FAILURES_INDEX = 2;
    public static int MODULE_LOAD_MESSAGES_SIZE = 3;


    public static String[] setupModules(QDLEnvironment config, State state) throws Throwable {
        String[] x = new String[MODULE_LOAD_MESSAGES_SIZE];
        // At some point, start putting in ResourceModules that are automatically loaded.
        // This will slightly change the logic here, since there will always be something to load.
        // the big question though is this: Do we let users load modules that are in the distro
        // this way, or is that a 'system thing' which we control?

        if (!config.getModuleConfigs().isEmpty()) {
            String foundClasses = "";
            String foundModules = "";
            String failedModules = "";
            boolean isFirstJavaModules = true;
            boolean isFirstQDLModules = true;
            QDLInterpreter interpreter = new QDLInterpreter(state);

            for (ModuleConfig moduleConfig : config.getModuleConfigs()) {
                if (moduleConfig.getType().equals(MODULE_TYPE_JAVA)) {
                    JavaModuleConfig jmc = (JavaModuleConfig) moduleConfig;
                    String className = jmc.getClassName();
                    try {
                   

                        Class klasse = state.getClass().forName(className);
                        // Next bit involves some trickery: we might want to import the
                        // module on start, but cannot get the actual namespace or alias
                        // until after the modules is loaded. So this makes the loader
                        // and pulls it off that
                        QDLLoader qdlLoader = (QDLLoader) klasse.newInstance();
                        setupJavaModule(state, qdlLoader, jmc);


                    /*
                    Next stuff just makes the entry for the environment
                     */
                        config.getMyLogger().info("loaded module:" + className);
                        if (isFirstJavaModules) {
                            isFirstJavaModules = false;
                            foundClasses = className;
                        } else {
                            foundClasses = foundClasses + "," + className;
                        }
                    } catch (Throwable t) {
                        if(moduleConfig.isFailOnError()){
                            throw t;
                        }
                        DebugUtil.printStackTrace(t);
                        config.getMyLogger().error(
                                "WARNING: module \"" + className + "\" could not be loaded:" + t.getMessage(),
                                t);
                    }// end try
                }// end if for java modules
                if (moduleConfig.getType().equals(MODULE_TYPE_QDL)) {
                    QDLModuleConfig qmc = null;
                    String module = null; // actual code in the module
                    if (moduleConfig instanceof QDLModuleConfig) {
                        qmc = (QDLModuleConfig) moduleConfig;
                        if(QDLFileUtil.isAbsolute(qmc.getPath())){
                            module = QDLFileUtil.readFileAsString(qmc.getPath());

                        }else{
                            // try to resolve it against the module path
                            //  Fixes https://github.com/ncsa/qdl/issues/51
                            QDLScript script = resolveScript(qmc.getPath(), state.getModulePaths(), state);
                            module = script == null ? null : script.getText();
                        }
                    }
                    if (moduleConfig instanceof ResourceModule) {
                        // read it from a resource in the distro, not from a file.
                        qmc = (ResourceModule) moduleConfig;
                        InputStream textStream = QDLModuleConfig.class.getResourceAsStream(qmc.getPath());
                        InputStreamReader reader = new InputStreamReader(textStream);
                        BufferedReader br = new BufferedReader(reader);
                        StringBuilder stringBuilder = new StringBuilder();
                        String inputLine = br.readLine();
                        while (inputLine != null) {
                            stringBuilder.append(inputLine + "\n");
                            inputLine = br.readLine();
                        }
                        br.close();
                        module = stringBuilder.toString();
                    }
                    // only (easy) way to tell to compare before and after
                    Set<URI> oldImports = new HashSet<>();
                    oldImports.addAll(interpreter.getState().getMTemplates().keySet());
                    try {
                        interpreter.execute(module);
                    } catch (Throwable t) {
                        DebugUtil.printStackTrace(t);
                        // a missing module should not crash the load
                        config.getMyLogger().error(
                                "WARNING: QDL module \"" + module.substring(0, Math.min(50, module.length())) + "\" could not be loaded:" + t.getMessage(),
                                t);
                        failedModules = failedModules + (failedModules.length() == 0 ? "" : ",") + qmc.getPath();
                        if (moduleConfig.isFailOnError()) {
                            throw t; // end of story
                        }
                        continue;
                    }
                    Set<MTKey> newImports = interpreter.getState().getMTemplates().keySet();
                    if (newImports.size() != oldImports.size() + 1) {
                        throw new NFWException("Error: added multiple modules unexpectedly");
                    }
                    for (MTKey uri : newImports) {
                        if (!oldImports.contains(uri)) {
                            String y = null;
                            if (qmc.isImportOnStart()) {
                                if (moduleConfig.getVersion().equals(MODULE_ATTR_VERSION_1_0)) {
                                    y = SystemEvaluator.MODULE_IMPORT + "('" + uri.getKey() + "');";
                                }
                                if (moduleConfig.getVersion().equals(MODULE_ATTR_VERSION_2_0)) {
                                    if (moduleConfig.isUse()) {
                                        y = ModuleEvaluator.USE + "('" + uri.getKey() + "');";
                                    } else {
                                        Module m = interpreter.getState().getMTemplates().getModule(uri);
                                        String varName = m.getAlias();
                                        String configVarName = moduleConfig.getVarName();

                                        if (varName == null && configVarName == null) {
                                            if (((ModuleConfigImpl) moduleConfig).failOnError) {
                                                throw new IllegalArgumentException("no name for the imported variable was given and there is no alias for module '" + uri + "'");
                                            } else {
                                                interpreter.getState().getLogger().warn("no name for the imported variable was given and there is no alias for module '" + uri + "'");
                                                continue;
                                            }
                                        }
                                        varName = configVarName == null ? varName : configVarName;
                                        y = varName + ":=" + ModuleEvaluator.IMPORT + "('" + uri.getKey() + "');";

                                    }
                                }
                            }
                            interpreter.execute(y);

                            break;
                        }
                    }
                    config.getMyLogger().info("loaded qdl module " + qmc.getPath());
                    if (isFirstQDLModules) {
                        isFirstQDLModules = false;
                        foundModules = qmc.getPath();
                    } else {
                        foundModules = foundModules + "," + qmc.getPath();
                    }
                }
            }// end for loop
            if (!foundClasses.isEmpty()) {
                x[JAVA_MODULE_INDEX] = foundClasses;
            }
            if (!foundModules.isEmpty()) {
                x[QDL_MODULE_INDEX] = foundModules;
            }
            if (!failedModules.isEmpty()) {
                x[MODULE_FAILURES_INDEX] = failedModules;
            }
        } // end if loop
        return x;
    }

    public static List<String> setupJavaModule(State state, QDLLoader loader, JavaModuleConfig jmc) {
        List<String> importedFQNames = new ArrayList<>();
        // Fix for https://github.com/ncsa/qdl/issues/79 monitor the version and set up state inheritance based on that.
        boolean oldImports = jmc.getVersion().equals(MODULE_ATTR_VERSION_1_0);
        for (Module template : loader.load()) {
            template.setTemplate(true);
            state.addModule(template); // done!
            importedFQNames.add(template.getNamespace().toString());
            if (jmc.isImportOnStart()) {
                // state.getMInstances().put(m);
                State state1;
                if(oldImports){
                     state1 = state.newLocalState();
                }else{
                    state1 = state.newCleanState();
                }
                Module instance = template.newInstance(state1);
                ((JavaModule) instance).init(state1);
                if (oldImports) {
                    instance.setInheritanceMode(ModuleEvaluator.IMPORT_STATE_SHARE_VALUE);
                    state.getMInstances().put(new MIWrapper(template.getKey(), instance));// puts it in the table with default alias.
                }else{
                    if(jmc.isUse()){
                        state.getVStack().appendTables(instance.getState().getVStack());
                        state.getFTStack().appendTables(instance.getState().getFTStack());
                        state.getUsedModules().put(instance.getNamespace(), instance);
                    }else{
                        // Actually fox for https://github.com/ncsa/qdl/issues/79
                        instance.setInheritanceMode(ModuleEvaluator.IMPORT_STATE_NONE_VALUE);
                        String varName = template.getAlias();
                        if(jmc.getVarName()!=null){
                            varName = jmc.getVarName();
                        }
                        if(varName == null){
                            throw new IllegalArgumentException("no alias or variable name for import was set for the module " + template.getNamespace());
                        }
                        state.setValue(varName, asQDLValue(instance));

                    }
                }
            }
        }
        return importedFQNames;
    }

    public static String runBootScript(QDLEnvironment config, State state) {
        if (config.hasBootScript()) {
            QDLInterpreter interpreter = new QDLInterpreter(state);
            String bootFile = config.getBootScript();
            try {
                String bootScript = QDLFileUtil.readFileAsString(bootFile);
                interpreter.execute(bootScript);

                config.getMyLogger().info("loaded boot script " + bootFile);
                return bootFile;
            } catch (Throwable t) {
                if(!(t instanceof ReturnException)) {
                    // Don't blow up if the script returns.
                    config.getMyLogger().error("warning: Could not load boot script\"" + bootFile + "\": " + t.getMessage(), t);
                }
            }
        }
        return null;
    }
}
