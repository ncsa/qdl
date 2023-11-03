package edu.uiuc.ncsa.qdl.extensions.mail;

import edu.uiuc.ncsa.qdl.extensions.JavaModule;
import edu.uiuc.ncsa.qdl.module.Module;
import edu.uiuc.ncsa.qdl.state.State;

import java.net.URI;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/2/23 at  11:17 AM
 */
public class QDLMailModule  extends JavaModule {
    public QDLMailModule() {
    }

    public QDLMailModule(URI namespace, String alias) {
        super(namespace, alias);
    }

    @Override
    public Module newInstance(State state) {
        QDLMailModule mailModule = new QDLMailModule(URI.create("qdl:/tools/email"), "mail");
        QDLMail qdlMail = new QDLMail();
        funcs.add(qdlMail.new Send());
        funcs.add(qdlMail.new SetCfg());
        mailModule.addFunctions(funcs);
        if(state != null){
            mailModule.init(state);
        }
        setupModule(mailModule);
        return mailModule;
    }
}
