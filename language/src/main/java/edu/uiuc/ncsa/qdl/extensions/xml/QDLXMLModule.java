package edu.uiuc.ncsa.qdl.extensions.xml;

import edu.uiuc.ncsa.qdl.extensions.JavaModule;
import edu.uiuc.ncsa.qdl.module.Module;
import edu.uiuc.ncsa.qdl.state.State;

import java.net.URI;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/13/23 at  7:29 AM
 */
public class QDLXMLModule extends JavaModule {
    public QDLXMLModule() {
    }

    public QDLXMLModule(URI namespace, String alias) {
        super(namespace, alias);
    }

    @Override
    public Module newInstance(State state) {
        QDLXMLModule qdlxml = new QDLXMLModule(URI.create("qdl:/tools/xml"), "xml");
        QDLXML xml = new QDLXML();
        funcs.add(xml.new XMLImport());
        funcs.add(xml.new GetAttributes());
        funcs.add(xml.new Snarf());
        funcs.add(xml.new YAMLExport());
        funcs.add(xml.new YAMLImport());
        funcs.add(xml.new HOCONExport());
        funcs.add(xml.new HOCONImport());
        qdlxml.addFunctions(funcs);
        if(state != null){
            qdlxml.init(state);
        }
        setupModule(qdlxml);
        return qdlxml;
    }
}
