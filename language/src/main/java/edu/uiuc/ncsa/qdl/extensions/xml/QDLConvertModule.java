package edu.uiuc.ncsa.qdl.extensions.xml;

import edu.uiuc.ncsa.qdl.extensions.JavaModule;
import edu.uiuc.ncsa.qdl.module.Module;
import edu.uiuc.ncsa.qdl.state.State;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/13/23 at  7:29 AM
 */
public class QDLConvertModule extends JavaModule {
    public QDLConvertModule() {
    }

    public QDLConvertModule(URI namespace, String alias) {
        super(namespace, alias);
    }

    @Override
    public Module newInstance(State state) {
        QDLConvertModule qdlxml = new QDLConvertModule(URI.create("qdl:/tools/convert"), "convert");
        QDLConvert xml = new QDLConvert();
        funcs.add(xml.new XMLImport());
        funcs.add(xml.new XMLExport());
        funcs.add(xml.new GetAttributes());
        funcs.add(xml.new Snarf());
        funcs.add(xml.new YAMLExport());
        funcs.add(xml.new YAMLImport());
        funcs.add(xml.new HOCONExport());
        funcs.add(xml.new HOCONImport());
        qdlxml.addFunctions(funcs);
        if (state != null) {
            qdlxml.init(state);
        }
        setupModule(qdlxml);
        return qdlxml;
    }

    List<String> doxx = new ArrayList<>();

    @Override
    public List<String> getDescription() {
        if (doxx.isEmpty()) {
            doxx.add("QDL extended conversion module. This will allow you to convert stems to and from various formats.");
            doxx.add("Supported formats are");
            doxx.add("XML, YAML, HOCON (simplified JSON)");
            doxx.add("in addition to QDL's built in JSON support.");
            doxx.add("The basic functions are");
            doxx.add(QDLConvert.XML_IMPORT_NAME + " - import an XML file or string into a stem");
            doxx.add(QDLConvert.GET_ATTR_NAME + " - find all the indices of attributes in an XML stem");
            doxx.add(QDLConvert.XML_EXPORT_NAME + " - export a stem to XML.");
            doxx.add(QDLConvert.SNARF_NAME + " - simplify a stem that has been read in XML.");
            doxx.add(QDLConvert.HOCON_IMPORT_NAME + " - import a HOCON file or string into a stem");
            doxx.add(QDLConvert.HOCON_EXPORT_NAME + " - export a stem to HOCON");
            doxx.add(QDLConvert.YAML_IMPORT_NAME + " - import a YAML file or string into a stem");
            doxx.add(QDLConvert.YAML_EXPORT_NAME + " - export a stem to YAML");
            doxx.add("\nFor importing, all of the functions have the same basic pattern. Either the argument");
            doxx.add("is a string representation of the data or you may specify a stem with the key " + QDLConvert.ARG_FILE);
            doxx.add("which tells the system to read the given file.");
            doxx.add("\nE.g.");
            doxx.add(QDLConvert.XML_IMPORT_NAME  + "('<a><b>X</b></a>') would process the argument directly");
            doxx.add(QDLConvert.XML_IMPORT_NAME  + "({'"+ QDLConvert.ARG_FILE +"':'/path/to/file'}) would import the file");
            doxx.add("and process it. Both return a stem.");
            doxx.add("\nFor exporting, the arguments are either the stem or the stem and a file name to write to.");
            doxx.add("\nE.g.");
            doxx.add(QDLConvert.YAML_EXPORT_NAME + "(arg.) - turns arg. into yaml and returns it as a string");
            doxx.add(QDLConvert.YAML_EXPORT_NAME + "(arg.,'/path/to/file') - turns arg. into yaml and writes it to the file");
        }
        return doxx;
    }
}
