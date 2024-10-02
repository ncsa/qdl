package org.qdl_lang.extensions.mail;

import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.module.Module;
import org.qdl_lang.state.State;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/2/23 at  11:17 AM
 */
public class QDLMailModule extends JavaModule {
    public QDLMailModule() {
    }

    public QDLMailModule(URI namespace, String alias) {
        super(namespace, alias);
    }

    @Override
    public Module newInstance(State state) {
        QDLMailModule mailModule = new QDLMailModule(URI.create("qdl:/tools/email"), "mail");
        QDLMail qdlMail = new QDLMail();
        mailModule.setMetaClass(qdlMail);
        ArrayList<QDLFunction> funcs = new ArrayList<>();

        funcs.add(qdlMail.new Send());
        funcs.add(qdlMail.new SetCfg());
        mailModule.addFunctions(funcs);
        if (state != null) {
            mailModule.init(state);
        }
        setupModule(mailModule);
        return mailModule;
    }

    @Override
    public List<String> getDocumentation() {
        List<String> d = new ArrayList<>();
        d.add("QDL Mail");
        d.add("--------");
        d.add("A very simple email program that allows you to send simple emails");
        d.add("directly from QDL with a minimum of fuss.");
        d.add("This is not intended to be a toolkit to let you write a full");
        d.add("fledged mail program, it is mostly intended for sending quick messages");
        d.add("say, from within a server. It supports emails (various content types");
        d.add("are allowed, such as html, default is plain text, of course)");
        d.add("but does not directly support attachments");
        d.add("or multipart mime messages.");
        d.add("");
        d.add("Basic operation is to set a configuration (a stem) and use the " + QDLMail.SEND_NAME + " function.");
        d.add("Example.");
        d.add("   mail:=jload('mail');");
        d.add("   mail#cfg(cfg.); // assuming cfg. has your mail account information.");
        d.add("   mail#send(['This is the subject','This is the body'];");
        return d;
    }
}
