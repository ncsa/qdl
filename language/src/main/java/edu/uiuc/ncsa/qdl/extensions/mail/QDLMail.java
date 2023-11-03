package edu.uiuc.ncsa.qdl.extensions.mail;

import edu.uiuc.ncsa.qdl.evaluate.SystemEvaluator;
import edu.uiuc.ncsa.qdl.extensions.QDLFunction;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.variables.QDLNull;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.security.util.configuration.TemplateUtil;
import edu.uiuc.ncsa.security.util.mail.MailUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static edu.uiuc.ncsa.security.core.util.StringUtils.RJustify;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/2/23 at  11:17 AM
 */
public class QDLMail {
    public static String SEND_NAME = "send";

    public class Send implements QDLFunction {
        @Override
        public String getName() {
            return SEND_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            QDLStem message;
            if (objects[0] instanceof String) {
                StringTokenizer st = new StringTokenizer((String) objects[0], "\n");
                long index = 0L;
                message = new QDLStem();
                while (st.hasMoreTokens()) {
                    message.put(index++, st.nextToken());
                }
            } else {
                if (objects[0] instanceof QDLStem) {
                    message = (QDLStem) objects[0];
                    if (!message.isList()) {
                        throw new IllegalArgumentException(getName() + " requires a list as its first argument.");
                    }
                } else {
                    throw new IllegalArgumentException(getName() + " requires a string or stem as its first argument.");
                }
            }
            QDLStem templates = null;
            if (objects.length == 2) {
                if (objects[1] instanceof QDLStem) {
                    templates = (QDLStem) objects[1];
                } else {
                    throw new IllegalArgumentException(getName() + " requires a stem as its second argument, if present.");
                }
            }
            QDLStem newMessage = new QDLStem();
            if (templates != null && !templates.isEmpty()) {
                newMessage = new QDLStem();
                for (Object key : message.keySet()) {
                    Object currentLine = message.get(key);
                    if (!(currentLine instanceof QDLNull)) {
                        newMessage.putLongOrString(key, TemplateUtil.replaceAll(currentLine.toString(), templates));
                    }
                }
            }else{
                newMessage = message;
            }
            // Now we can send the message
            String recipients = "";
            if (getCfg().containsKey(MAIL_TO)) {
                Object rr = getCfg().get(MAIL_TO);
                if (rr instanceof String) {
                    recipients = getCfg().getString(MAIL_TO);
                } else {
                    if (rr instanceof QDLStem) {
                        QDLStem rawRR = (QDLStem) rr;
                        boolean firstPass = true;
                        for (Object key : rawRR.keySet()) {
                            Object possibleRecipient = rawRR.get(key);
                            if(possibleRecipient instanceof String){
                                if (firstPass) {
                                    recipients = (String)possibleRecipient;
                                    firstPass = false;
                                } else {
                                    recipients = recipients + MailUtil.ADDRESS_SEPARATOR + possibleRecipient;
                                }
                            }
                        }
                    } else {
                        throw new IllegalArgumentException(getName() + " the recipients of the message '" + rr + "'cannot be parsed");
                    }
                }
            } else {
                throw new IllegalStateException(getName() + " -- no recipients set for the message");
            }

            MailUtil.MailEnvironment mailEnvironment = new MailUtil.MailEnvironment(true,
                    getCfg().getString(MAIL_SERVER),
                    getCfg().getLong(MAIL_PORT).intValue(),
                    getCfg().containsKey(MAIL_PASSWORD) ? getCfg().getString(MAIL_PASSWORD) : "",
                    getCfg().getString(MAIL_FROM),
                    recipients,
                    null, // body sent below
                    null, // subject sent below
                    getCfg().containsKey(MAIL_USE_SSL) ? getCfg().getBoolean(MAIL_USE_SSL) : true,
                    getCfg().containsKey(MAIL_START_TLS) ? getCfg().getBoolean(MAIL_START_TLS) : true);
            MailUtil mailUtil = new MailUtil(mailEnvironment);
            mailUtil.setMyLogger(state.getLogger());

            // Mail util only understands strings, so we have to get the body.
            StringBuilder body = new StringBuilder();
            String subject = newMessage.getString(0L);
            QDLStem bodyStem = newMessage.listSubset(1L);
            for (Object key : bodyStem.keySet()) {
                if (key instanceof Long) {
                    body.append(bodyStem.getString((Long) key) + "\n");
                }
            }
            mailUtil.sendMessage(subject, body.toString(), null);
            return Boolean.TRUE;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            if (argCount == 1) {
                d.add(getName() + "(message) - send a message using the current configuration.");
                d.add("        message may be a string or list. The zero-th line is the subject.");
            }
            if (argCount == 2) {
                d.add(getName() + "(message, templates.) - send a message using the current configuration,");
                d.add("        message may be a string or list. The zero-th line is the subject.");
                d.add("        templates. is a stem of substitutions whose keys are the old value and");
                d.add("        values are the new. The contract is that every instance of the form ");
                d.add("        ${key} is replaced by value.");
            }
            d.add("Note: message. does not require each element be a string, but");
            d.add("   each line will have " + SystemEvaluator.TO_STRING + " invoked on it.");
            d.add("Note: message., if a stem,  must be a list since there is no canonical");
            d.add("   ordering of general stem elements and hence the message would be in random order.");
            d.add("Note: messages are send on a separate thread, so the workspace does not hang.");
            d.add("E.g.");
            d.add("Assuming you have a configuration object already to go, set it and send a message:");
            d.add("   " + CFG_METHOD_NAME + "(cfg.)");
            d.add("   " + getName() + "(['System maintenance outages','The system will be down on Mondays for 15 minutes','starting at 4:00 am local time.' ])");
            d.add("Sends the message whose subject is the first line and whose contents are the remaining lines");

            d.add("\nE.g. sending from file using templates");
            d.add("Assuming that you have set the configuration and your message and subject live in files,");
            d.add("you can send them as well as replace templates in them of the form ${key} as follows.");
            d.add("Let us say that the subject file reads:");
            d.add("Announcements on ${date} for ${system}.");
            d.add("Start by getting you message. Since the first line is the subject, stick that first.");
            d.add("reading in as stems:");
            d.add("  message. := file_read('path/to/subject.txt', 1)~file_read('path/to/body.txt', 1);");
            d.add("   " + getName() + "(message., {'date':head(date_iso(),'T'), 'system','OA4MP'})");
            d.add("Note this just grabs the day from the current date.");
            d.add("The subject of the message would read (date may vary):");
            d.add("Announcements on 2023-10-03 for OA4MP.");
            return d;
        }
    }

    public static String CFG_METHOD_NAME = "cfg";

    public class SetCfg implements QDLFunction {
        @Override
        public String getName() {
            return CFG_METHOD_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (objects.length == 0) {
                return getCfg();
            }
            if (objects[0] instanceof QDLStem) {
                QDLStem old = getCfg();
                setCfg((QDLStem) objects[0]);
                return old;
            }
            throw new IllegalArgumentException("The argument must be a stem");
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            if (argCount == 0) {
                d.add(getName() + "() - get the current mail configuration");
            }
            if (argCount == 1) {
                d.add(getName() + "(cfg.) - set the current mail configuration.");
                d.add("returns the previous value for this configuration");
            }

            d.add("The configuation for this utility is a stem consisting of ");
            int width = 12; // longest width of attribute
            RJustify("", 12);
            d.add(RJustify(MAIL_BCC, width) + " : list of addresses for blind carbon copy");
            d.add(RJustify(MAIL_CC, width) + " : list of addresses for carbon copy");
            d.add(RJustify(MAIL_FROM, width) + " : address of sender");
            d.add(RJustify(MAIL_PASSWORD, width) + " : password for the outgoing server");
            d.add(RJustify(MAIL_PORT, width) + " : integer for the port of the mail server");
            d.add(RJustify(MAIL_SERVER, width) + " : address of the mail server");
            d.add(RJustify(MAIL_START_TLS, width) + " : boolean, use start TLS?");
            d.add(RJustify(MAIL_TO, width) + " : list of recipients");
            d.add(RJustify(MAIL_USE_SSL, width) + " : boolean, use SSL?");
            return d;
        }
        /*
                this.mailEnabled = mailEnabled;
            this.messageTemplate = messageTemplate;
            this.password = password;
            this.port = port;
            this.recipients = parseRecipients(recipients);
            this.server = server;
            this.subjectTemplate = subjectTemplate;
            this.useSSL = useSSL;
            this.starttls = starttls;
            cc
            bcc
            address


         */
    }

    public static String MAIL_BCC = "bcc";
    public static String MAIL_CC = "cc";
    public static String MAIL_FROM = "from";
    public static String MAIL_PASSWORD = "password";
    public static String MAIL_PORT = "port";
    public static String MAIL_SERVER = "host";
    public static String MAIL_START_TLS = "start_tls";
    public static String MAIL_TO = "to";
    public static String MAIL_USE_SSL = "use_ssl";

    public QDLStem getCfg() {
        if (cfg == null) {
            cfg = new QDLStem();
        }
        return cfg;
    }

    public void setCfg(QDLStem cfg) {
        this.cfg = cfg;
    }

    public boolean hasCfg() {
        return cfg != null;
    }

    QDLStem cfg = null;

}
