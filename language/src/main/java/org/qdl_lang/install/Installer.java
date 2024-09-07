package org.qdl_lang.install;

import org.qdl_lang.util.QDLVersion;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Really simple installer. This basically just copies stuff that has been set up in the
 * create_installer.sh script, so chances are excellent if you need to change the installer,
 * you should be looking there.<br/><br/>
 * This gets copied to your jar and will just copy everything in the jar to
 * a given directory (including sub directories). So make the tree you want, jar it up with this class
 * and run it.
 * <h2>Caveat for changing this class</h2>
 * This is a completely standalone class -- no dependencies but plain Old Java -- because
 * otherwise you have to manage dependencies (might involve writing your own class loader!)
 * for this installer program which can get very hard. The idea is that this is a lean,
 * single class. Even inheritance doesn't work.
 * <p>Created by Jeff Gaynor<br>
 * on 3/30/20 at  7:23 AM
 */
public class Installer {
    protected void trace(String message) {
        if (isDebugOn()) {
            say(message);
        }
    }

    public boolean isDebugOn() {
        return debugOn;
    }

    public void setDebugOn(boolean debugOn) {
        this.debugOn = debugOn;
    }

    boolean debugOn = false;


    public static void main(String[] args) {
        Installer installer = new Installer();
        try {
            installer.runnit2(args);
        } catch (Throwable t) {
            installer.say(t.getMessage());
            if (installer.isDebugOn()) {
                t.printStackTrace();
            }
        }
    }


    protected void runnit2(String[] args) throws Throwable {
        say("QDL installer version " + QDLVersion.VERSION);
        argMap = new HashMap<>();
        setupMap(args);
        if (isShowHelp()) {
            showHelp();
            return;
        }
        if (getOperation().startsWith("-")) {
            say("unknown operation \"" + getOperation() + "\"");
            return;
        }
        setDebugOn(is(DEBUG_FLAG));

        if (isList()) {
            doListFiles();
            return;
        }
        if (isInstall() && isUpgrade()) {
            say("sorry, you cannot specify both an upgrade and an install");
            return;
        }
        if ((isInstall() || isUpgrade()) && isRemove()) {
            say("sorry, you cannot specify both an removing QDL and an install/upgrade");
            return;
        }
        if (isRemove()) {
            doRemove();
            return;
        }
        File rootDir = checkRootDir(getRootDir(), isUpgrade());
        if (rootDir == null) {
            return;
        }
        if (isInstall()) {
            doInstall();
        }
        if (isUpgrade()) {
            doUpgrade();
        }
    }

    protected void doUpgrade() throws Exception {
        File rootDir = getRootDir();
        if (isQDL()) {
            upgradeQDL(rootDir);
        }
        if (isNano()) {
            upgradeNano();
        }
        if (isVim()) {
            upgradeVim();
        }
    }

    protected void doInstall() throws Exception {
        File rootDir = getRootDir();
        if (isQDL()) {
            installQDL(rootDir);
            say("Done! You should add");
            say("   export QDL_HOME=\"" + rootDir.getAbsolutePath() + "\"");
            say("to your environment and");
            say("   $QDL_HOME" + File.separator + "bin\"");
            say("to your PATH");
        }
        if (isNano()) {
            installNano(rootDir);
        }
        if (isVim()) {
            installVim(rootDir);
        }
    }

    protected void doRemove() throws IOException {
        if (isQDL()) {
            uninstallQDL(getRootDir());
        }
        if (isVim()) {
            uninstallVim();
        }
        if (isNano()) {
            uninstallNano();
        }
    }

    protected void uninstallQDL(File rootDir) {
        if (rootDir == null) {
            say("you must explicitly specify the directory to be removed. exiting...");
        } else {
            nukeDir(rootDir);
            rootDir.delete(); //adios muchacho
            say(rootDir + " and all of its subdirectories have been removed.");
        }
    }


    static protected final String UPGRADE_FLAG = "-u";
    static protected final String UPGRADE_OPTION = "upgrade";
    static protected final String UPDATE_OPTION = "update";
    static protected final String HELP_FLAG = "--help";
    static protected final String HELP_OPTION = "help";
    static protected final String DIR_ARG = "-dir";
    static protected final String DEBUG_FLAG = "-debug";
    static protected final String INSTALL_OPTION = "install";
    static protected final String LIST_OPTION = "list";
    static protected final String REMOVE_OPTION = "remove";
    static protected final String VIM_FLAG = "-vim";
    static protected final String NANO_FLAG = "-nano";
    static protected final String QDL_FLAG = "-qdl";
    static protected final String ALL_FLAG = "-all";

    static List<String> allOps = Arrays.asList(UPGRADE_OPTION, UPDATE_OPTION, REMOVE_OPTION, HELP_OPTION, INSTALL_OPTION, LIST_OPTION);

    private void showHelp() {
        say("===============================================================");
        say("java -jar qdl-installer.jar install operation arguments* flags*");
        say("===============================================================");
        say("This will install QDL to your system. Options are:");
        say("(none) = same as help");
        say(INSTALL_OPTION + " = install");
        say(UPGRADE_OPTION + " = upgrade");
        say(UPGRADE_OPTION + " = same as " + UPGRADE_OPTION);
        say(REMOVE_OPTION + " = remove");
        say(HELP_OPTION + " = show help and exit. Note you can also use the flag " + HELP_FLAG);
        say(LIST_OPTION + " = list all the files in the distribution. Nothing is done.");
        say("--------------");
        say("arguments are:");
        say(DIR_ARG + " root = install to the given directory. If omitted, you will be prompted.");
        say("--------------");
        say("Flags are:");
        say(DEBUG_FLAG + " = debug mode -- print all messages from the installer as it runs. This is quite verbose.");
        say(HELP_FLAG + " = this help message");
        say(NANO_FLAG + " = install support for nano 4 or higher");
        say(VIM_FLAG + " = install support for vim ");
        say(QDL_FLAG + " = install support for QDL");
        say(ALL_FLAG + " = do all components = QDL, nano and vim");
        say("");
        say("E.g.");
        say(getClass().getSimpleName() + " " + UPGRADE_OPTION + " " + ALL_FLAG + " " + DIR_ARG + " $QDL_HOME");
        say("upgrades all components.\n");
        say(getClass().getSimpleName() + " " + INSTALL_OPTION + " " + QDL_FLAG + " " + NANO_FLAG);
        say("installs QDL and nano support. Since no directory is specified, you will be prompted.\n");
        say(getClass().getSimpleName() + " " + INSTALL_OPTION + " " + VIM_FLAG + " " + DIR_ARG + " ${QDL_HOME}");
        say("just installs support for vim. This requires the installed directory to get the right versions.");
    }

    protected void upgradeNano() throws IOException {
        File nanorc = new File(getUserHome(), ".nanorc");
        if (!nanorc.exists()) {
            say("nano support is not installed");
        }
    }

    /**
     * This will install nano support in the user's home directory
     */
    protected void installNano(File rootDir) throws IOException {
        File nanorc = new File(getUserHome(), ".nanorc");
        List<String> content = null;
        if (nanorc.exists()) {
            content = Files.readAllLines(nanorc.toPath());
            for (String line : content) {
                if (line.endsWith("/qdl.nanorc")) {
                    // this has been done
                    trace("nano support already installed.");
                    return;
                }
            }

        } else {
            content = new ArrayList<>();
        }
        content.add("include " + rootDir.getAbsolutePath() + "/etc/qdl.nanorc");
        Files.write(nanorc.toPath(), content);
        say("nano support installed.");
    }

    protected void uninstallNano() throws IOException {
        File nanorc = new File(getUserHome(), ".nanorc");
        List<String> content = null;
        if (nanorc.exists()) {
            content = Files.readAllLines(nanorc.toPath());
            for (int i = 0; i < content.size(); i++) {
                String line = content.get(i);
                if (line.endsWith("/qdl.nanorc")) {
                    content.remove(i);
                }
            }

        } else {
            return; //nothing to uninstall.
        }
        if (content.size() == 0) {
            nanorc.delete();
        } else {
            Files.write(nanorc.toPath(), content);
        }
        say("nano support uninstalled.");
    }

    protected void upgradeVim() throws IOException {
        File vimDir = new File(getUserHome(), ".vim");
        File ftDetect = new File(vimDir, "ftdetect");
        File syntax = new File(vimDir, "syntax");
        if (!vimDir.exists() || !ftDetect.exists() || !syntax.exists()) {
            say("vim support not installed");
            return;
        }
        File vimFT = new File(ftDetect, "qdl.vim");

        File vimSyn = new File(syntax, "qdl.vim");
        if (!vimFT.exists() || !vimSyn.exists()) {
            say("vim support not installed");
        }
    }

    protected void uninstallVim() throws IOException {
        File vimDir = new File(getUserHome(), ".vim");
        File ftDetect = new File(vimDir, "ftdetect");
        File syntax = new File(vimDir, "syntax");
        if (!vimDir.exists()) {
            return; //nothing to do
        }
        if (ftDetect.exists()) {
            File qdlVim = new File(ftDetect, "qdl.vim");
            qdlVim.delete(); // removes if a file or link.
        }
        if (syntax.exists()) {
            File qdlVim = new File(syntax, "qdl.vim");
            qdlVim.delete();
        }
        say("vim support has been removed");
    }

    /**
     * install vim support
     *
     * @throws IOException
     */
    protected void installVim(File rootDir) throws IOException {
        File vimDir = new File(getUserHome(), ".vim");
        File ftDetect = new File(vimDir, "ftdetect");
        File syntax = new File(vimDir, "syntax");
        if (!vimDir.exists()) {
            // completely new install
            vimDir.mkdir();
            ftDetect.mkdir();
            syntax.mkdir();
        }
        boolean ftDetectDone = false;
        if (!ftDetect.exists()) {
            ftDetect.mkdir();
        }
        for (String f : ftDetect.list()) {
            if (f.equals("qdl.vim")) {
                ftDetectDone = true;
                break;
            }
        }
        // at this point, we just bail if the file system does not support links
        // Might want to just copy files?    The issue then is that we have to manage
        // upgrades directly.
        if (!ftDetectDone) {
            File ftTarget = new File(rootDir.getAbsolutePath() + "/etc/vim/ftdetect/qdl.vim");
            File ftLink = new File(ftDetect, "qdl.vim");
            try {
                Files.createSymbolicLink(ftLink.toPath(), ftTarget.toPath());
            } catch (UnsupportedOperationException x) {
                // Some file systems do not support symbolic links.
                say(x.toString());
                return;
            }
        }
        boolean syntaxDone = false;
        if (!syntax.exists()) {
            syntax.mkdir();
        }
        for (String f : syntax.list()) {
            if (f.equals("qdl.vim")) {
                syntaxDone = true;
                break;
            }
        }

        if (!syntaxDone) {
            File sTarget = new File(rootDir.getAbsolutePath() + "/etc/vim/syntax/qdl.vim");
            File sLink = new File(syntax, "qdl.vim");
            try {
                Files.createSymbolicLink(sLink.toPath(), sTarget.toPath());
            } catch (UnsupportedOperationException x) {
                // Some file systems do not support symbolic links.
                say(x.toString());
            }
        }
        say("vim support installed.");
    }

    File userHome = null;

    protected File getUserHome() {
        if (userHome == null) {
            userHome = new File(System.getProperty("user.home"));
        }
        return userHome;
    }


    /**
     * For upgrades. If the diretory does not exist, create it.
     * Return false if the directory does not exist.
     *
     * @param dir
     * @return
     */
    protected boolean checkUpgradeDir(File dir) {
        trace("checking if dir exists for " + dir);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                trace("  nope");
                return false;
            }

        }
        trace("  yup");

        return true;
    }

    /**
     * Remove the contents of the  directory. At the end of this,
     * the directory is empty. It does not delete the directory,
     * however
     *
     * @param dir
     */
    protected void nukeDir(File dir) {
        if (!dir.isDirectory()) return; //
        File[] contents = dir.listFiles();
        for (File f : contents) {
            trace("found " + f);

            if (f.isFile()) {
                trace("   deleting file:" + f);
                f.delete();
            }
            if (f.isDirectory()) {
                trace("   deleting dir:" + f);
                nukeDir(f);
                f.delete();
            }
        }
    }


    protected void doListFiles() throws Exception {
        say("files in this distribution");
        say("--------------------------");
        InputStream is = getClass().getResourceAsStream("/" + ListDistroFiles.FILE_LIST); // start with something we know is there
        List<String> fileList = isToList(is);
        for (String file : fileList) {
            say(file);
        }
    }

    protected List<String> isToList(InputStream inputStream) throws IOException {
        // This has been ingested as a collection of lines. Convert to list
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        List<String> myList = new ArrayList<String>(Arrays.asList(text.split("\\r?\\n")));
        return myList;
    }

    /**
     * Overwrites (but does not delete) files and will make more complex paths.
     * This allows for upgrading much older QDL installs that might have
     * different or missing (e.g. vim support) directories.
     *
     * @throws Exception
     */
    protected void upgradeQDL(File rootDir) throws Exception {
        InputStream is = getClass().getResourceAsStream("/" + ListDistroFiles.FILE_LIST); // start with something we know is there
        List<String> fileList = isToList(is);
        setupDirs(rootDir);

        for (String file : fileList) {
            if (file.startsWith("/bin") || (file.startsWith("/etc") && file.endsWith(".xml"))) {
                // On upgrades, do NOT touch the bin or etc config files  since the files are
                // edited in the installation and overwriting them breaks their QDL install.
                continue;
            }
            File f = new File(rootDir.getAbsolutePath() + file);
            if (f.exists()) {
                f.delete();
            }
            trace("  " + file + " --> " + f.getCanonicalPath());
            cp(file, f);
            if (file.endsWith(".qdl")) {
                doSetupScript(f, rootDir);
            }
        }
    }

    private void setupDirs(File rootDir) throws IOException {
        InputStream is;
        is = getClass().getResourceAsStream("/" + ListDistroFiles.DIR_LIST); // start with something we know is there
        List<String> dirList = isToList(is);
        for (String dir : dirList) {
            File f = new File(rootDir.getAbsolutePath() + dir);
            trace("checking dir " + dir + " --> " + f.getCanonicalPath());
            if (!f.exists()) {
                f.mkdirs();
            }
        }
    }

    /**
     * gets the resourceName as a stream and copies it to the physical target
     * file.
     *
     * @param resourceName
     * @param target
     * @throws IOException
     */
    protected void cp(String resourceName, File target) throws IOException {
        if (target.isDirectory()) {
            trace("Skipping directory " + target);
            return;
        }
        InputStream is = getClass().getResourceAsStream(resourceName); // start with something we know is there
        Files.copy(is, target.toPath()); // binary copy.
    }


    protected void installQDL(File rootDir) throws Exception {
        InputStream is = getClass().getResourceAsStream("/" + ListDistroFiles.FILE_LIST); // start with something we know is there
        List<String> fileList = isToList(is);
        trace("starting install...");
        setupDirs(rootDir);

        for (String file : fileList) {
            File f = new File(rootDir.getAbsolutePath() + file);
            if (f.exists()) {
                f.delete();
            }
            trace("  " + file + " --> " + f.getCanonicalPath());
            cp(file, f);
            if (file.equals("/bin/qdl") || file.equals("/bin/qdl-run")) {
                trace("   setting up qdl script to be executable");
                doSetupExec(f, rootDir);
            }
            if (file.endsWith(".qdl")) {
                doSetupScript(f, rootDir);
            }
            if (file.startsWith("/etc/") && file.endsWith(".xml")) {
                // process xml config files in /etc only.
                trace("  setting up basic configuration");
                doSetupConfig(f, rootDir);
            }
        }
    }


    /**
     * Read the executable file (the one they invoke to run QDL) and set the root directory in it,
     * then set it to be executable.
     *
     * @param f
     */
    private void doSetupExec(File f, File rootDir) throws IOException {
        List<String> lines = Files.readAllLines(f.toPath());
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("QDL_HOME")) {
                lines.set(i, "QDL_HOME=\"" + rootDir.getCanonicalPath() + "\"");
                break;
            }
        }
        Files.write(f.toPath(), lines, Charset.defaultCharset());
        f.setExecutable(true);
    }

    public static String SHEBANG = "#!";

    /**
     * QDL files that start with a shebang (#!) should be set executable.
     *
     * @param f
     * @param rootDir
     * @throws IOException
     */
    private void doSetupScript(File f, File rootDir) throws IOException {
        trace("setting up script: " + f.getAbsolutePath());
        List<String> lines = Files.readAllLines(f.toPath());
        for (String line : lines) {
            if (!line.isBlank()) {
                if (line.trim().startsWith(SHEBANG)) {
                    f.setExecutable(true);
                    trace("   >> was set executable!");
                }
                // only sniff first non-blank line. Don't care about anything else,
                // so don't process the rest of the file.
                return;
            }
        }

    }

    private void doSetupConfig(File f, File rootDir) throws IOException {
        List<String> lines = Files.readAllLines(f.toPath());
        List<String> config = new ArrayList<>(lines.size() + 20);
        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i);
            if (currentLine.contains("${QDL_HOME}")) {
                currentLine = currentLine.replace("${QDL_HOME}", rootDir.getCanonicalPath() + File.separator);
            }
            if (currentLine.contains("${EDITOR}")) { // sets the external editor in the workspace
                String editor = "line"; // default
                if (isNano()) {
                    editor = "nano";
                }
                if (isVim()) {
                    editor = "vim";
                }
                currentLine = currentLine.replace("${EDITOR}", editor);
            }
            if (currentLine.contains("${EDITORS}")) {
                // writes a block of code for the editor
                if (isVim() || isNano()) {
                    String indent = currentLine.substring(0, currentLine.indexOf("$"));
                    config.add(indent + "<editors>");
                    if (isVim()) {
                        config.add(indent + "   " + "<editor name=\"vim\" exec=\"vim\"/>");
                    }
                    if (isNano()) {
                        config.add(indent + "   " + "<editor name=\"nano\" exec=\"nano\"/>");
                    }
                    config.add(indent + "</editors>");
                }
                // note that if there are no editors, this line is skipped.
            } else {

                config.add(currentLine);
            }
        }
        Files.write(f.toPath(), config, Charset.defaultCharset());
    }

    /**
     * Prompts for the right directory, if missing, and then it will check if various
     * directories exist. If this returns false, then the install cannot
     * proceed, because, e.g., they request an upgrade but no base install
     * exists.
     *
     * @return
     * @throws Exception
     */
    protected File checkRootDir(File rootDir, boolean upgrade) throws Exception {
        if (rootDir == null) {
            String lineIn = readline("Enter the target directory for the QDL installer:");
            rootDir = new File(lineIn);
        }
        if (upgrade) {
            if (!rootDir.exists()) {
                say("Sorry, but that directory does not exist so no upgrade can be done. Exiting...");
                return null;
            }
        } else {
            if (rootDir.exists()) {
                if (rootDir.list().length != 0) {
                    say("This exists and is not empty. This will only install to an empty/non-existent directory.\nDid you mean " + UPGRADE_OPTION + "?");
                }
                return null;
            }
            trace("creating directories for root path");
            Files.createDirectories(rootDir.toPath());
        }
        return rootDir;
    }


    protected BufferedReader getBufferedReader() {
        if (bufferedReader == null) {
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        }
        return bufferedReader;
    }


    BufferedReader bufferedReader;

    public String readline(String prompt) throws Exception {
        System.out.print(prompt);
        return getBufferedReader().readLine();
    }

    protected void say(String x) {
        System.out.println(x);
    }


    protected HashMap<String, String> getFileList() {
        HashMap<String, String> fileList = new HashMap<>();
        return fileList;
    }

    HashMap<String, Object> argMap;
    public String operationKey = "operation";

    protected void setupMap(String[] args) {
        argMap = new HashMap<>();

        if (args.length == 0
                || args[0].equals(HELP_OPTION)
                || args[0].equals(HELP_FLAG)) {
            // if there are no options or the only one is help, just print help
            argMap.put(Installer.HELP_OPTION, true);
            return;
        }
        argMap.put(operationKey, args[0]);
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case Installer.HELP_FLAG:
                    argMap.put(Installer.HELP_OPTION, true);
                    return;
                case Installer.DEBUG_FLAG:
                    argMap.put(Installer.DEBUG_FLAG, true);
                    break;
                case Installer.DIR_ARG:
                    if ((i + 1) < args.length) {
                        // if this is the very last argument on the line, skip it
                        if (args[i + 1].startsWith("-")) {
                            throw new IllegalArgumentException("missing directory");
                        }
                        argMap.put(Installer.DIR_ARG, new File(args[++i]));
                    }
                    break;
                case Installer.VIM_FLAG:
                    argMap.put(Installer.VIM_FLAG, true);
                    break;
                case Installer.NANO_FLAG:
                    argMap.put(Installer.NANO_FLAG, true);
                    break;
                case ALL_FLAG:
                    argMap.put(ALL_FLAG, true);
                    break;
                case QDL_FLAG:
                    argMap.put(QDL_FLAG, true);
                    break;
            }

        }
        if (!isShowHelp()) {
            if (!Installer.allOps.contains(getOperation())) {
                throw new IllegalArgumentException("unknown operation \"" + getOperation() + "\"");
            }
        }
    }

    // Help functions. These SHOULD be in another class, but that would mean writing
    // a separate classloader for the executable jar -- way too much work
    public Boolean is(String key) {
        if (!argMap.containsKey(key)) return false;
        return (Boolean) argMap.get(key);
    }

    public File getRootDir() {
        if (!argMap.containsKey(DIR_ARG)) return null;
        return (File) argMap.get(DIR_ARG);
    }

    public boolean isInstall() {
        return getOperation().equals(INSTALL_OPTION);
    }

    public boolean isRemove() {
        return getOperation().equals(REMOVE_OPTION);
    }

    public boolean isUpgrade() {
        return getOperation().equals(UPGRADE_OPTION) || getOperation().equals(UPDATE_OPTION);
    }

    public boolean isShowHelp() {
        return is(HELP_OPTION) || getOperation().equals(HELP_OPTION);
    }

    public boolean isList() {
        return getOperation().equals(LIST_OPTION);
    }

    public boolean hasRootDir() {
        return getRootDir() != null;
    }

    public String getOperation() {
        return (String) argMap.get(operationKey);
    }

    public boolean isAll() {
        return is(ALL_FLAG);
    }

    public boolean isNano() {
        return is(NANO_FLAG) || is(ALL_FLAG);
    }

    public boolean isVim() {
        return is(VIM_FLAG) || is(ALL_FLAG);
    }

    public boolean isQDL() {
        return is(QDL_FLAG) || is(ALL_FLAG);
    }

}
