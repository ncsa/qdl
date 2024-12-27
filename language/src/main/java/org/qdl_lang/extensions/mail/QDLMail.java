package org.qdl_lang.extensions.mail;

import org.qdl_lang.evaluate.SystemEvaluator;
import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.QDLStem;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import edu.uiuc.ncsa.security.util.configuration.TemplateUtil;
import edu.uiuc.ncsa.security.util.mail.MailEnvironment;
import edu.uiuc.ncsa.security.util.mail.MailUtil;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import static edu.uiuc.ncsa.security.core.util.StringUtils.RJustify;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/2/23 at  11:17 AM
 */
public class QDLMail implements QDLMetaModule {
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
                        throw new BadArgException(getName() + " requires a list as its first argument.",0);
                    }
                } else {
                    throw new BadArgException(getName() + " requires a string or stem as its first argument.",0);
                }
            }
            QDLStem templates = null;
            if (objects.length == 2) {
                if (objects[1] instanceof QDLStem) {
                    templates = (QDLStem) objects[1];
                } else {
                    throw new BadArgException(getName() + " requires a stem as its second argument, if present.",1);
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
            // Now we can set up the environment for the message
            MailEnvironment mailEnvironment = MailEnvironment.create()
                    .setEnabled(true) // always for QDL Mail
                    .setDebug(getCfg().containsKey("debug")?getCfg().getBoolean("debug"):false)
                    .setBCC(getAddresses(getCfg().get(MAIL_BCC), MAIL_BCC))
                    .setCC(getAddresses(getCfg().get(MAIL_CC), MAIL_CC))
                    .setFrom(getCfg().getString(MAIL_FROM))
                    .setRecipients(getAddresses(getCfg().get(MAIL_TO), MAIL_TO))
                    .setContentType(getCfg().getString(MAIL_CONTENT_TYPE))
                    .useSSL(getCfg().containsKey(MAIL_USE_SSL)?getCfg().getBoolean(MAIL_USE_SSL):false)
                    .startTLS(getCfg().containsKey(MAIL_START_TLS)?getCfg().getBoolean(MAIL_START_TLS):false)
                    .setPort(getCfg().containsKey(MAIL_PORT)?getCfg().getLong(MAIL_PORT).intValue():-1)
                    .setServer(getCfg().getString(MAIL_SERVER))
                    .setPassword(getCfg().getString(MAIL_PASSWORD))
                    .setReplyTo(getCfg().getString(MAIL_REPLY_TO))
                    .setUsername(getCfg().getString(MAIL_USERNAME));
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

    /**
     * Convert QDL entry of addresses -- a string or a list of addresses -- to a Java
     * list so it can be passed to the {@link MailEnvironment}
     * @param rawAddresses
     * @param typeName
     * @return
     */

    private List<String> getAddresses(Object rawAddresses, String typeName) {
        List<String> addresses = new ArrayList<>();
        if(rawAddresses == null){
            return addresses;
        }
            if (rawAddresses instanceof String) {
                addresses.add( (String)rawAddresses);
            } else {
                if (rawAddresses instanceof QDLStem) {
                    QDLStem rawRR = (QDLStem) rawAddresses;
                    TreeSet<String> addressList = new TreeSet<>(); // re-order, keep unique

                    for (Object key : rawRR.keySet()) {
                        Object possibleRecipient = rawRR.get(key);
                        if(possibleRecipient instanceof String){
                            addressList.add((String)possibleRecipient);
                        }
                    }
                    addresses.addAll(addressList);
                } else {
                    throw new IllegalArgumentException(typeName + " recipients of the message '" + rawAddresses + "'cannot be parsed");
                }
            }
        return addresses;
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
            throw new BadArgException("The argument must be a stem", 0);
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
            d.add(RJustify(MAIL_CONTENT_TYPE, width) + " : content type of message. Default is plain text.");
            d.add(RJustify(MAIL_DEBUG, width) + " : low level debugging of mail. Use with discretion.");
            d.add(RJustify(MAIL_FROM, width) + " : address of sender");
            d.add(RJustify(MAIL_PASSWORD, width) + " : password for the outgoing server");
            d.add(RJustify(MAIL_PORT, width) + " : integer for the port of the mail server");
            d.add(RJustify(MAIL_REPLY_TO, width) + " : single address for the reply to field. Default is to use " + MAIL_FROM);
            d.add(RJustify(MAIL_SERVER, width) + " : address of the mail server");
            d.add(RJustify(MAIL_START_TLS, width) + " : boolean, use start TLS?");
            d.add(RJustify(MAIL_TO, width) + " : list of recipients");
            d.add(RJustify(MAIL_USE_SSL, width) + " : boolean, use SSL?");
            d.add(RJustify(MAIL_USERNAME, width) + " : the user name to use for logging in to the mail server");
            d.add(StringUtils.getBlanks(width) + "   If omitted, defaults to the " + MAIL_FROM + " value.");
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
    public static String MAIL_CONTENT_TYPE = "content_type";
    public static String MAIL_FROM = "from";
    public static String MAIL_PASSWORD = "password";
    public static String MAIL_PORT = "port";
    public static String MAIL_SERVER = "host";
    public static String MAIL_START_TLS = "start_tls";
    public static String MAIL_TO = "to";
    public static String MAIL_DEBUG = "debug";
    public static String MAIL_USE_SSL = "use_ssl";
    public static String MAIL_USERNAME = "username";
    public static String MAIL_REPLY_TO = "reply_to";

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

    protected static QDLStem addAll(List<String> x){
        if(x == null || x.isEmpty()){
            return null;
        }
        QDLStem q = new QDLStem();
        q.getQDLList().addAll(x);
        return q;
    }
    /**
     * Convert a {@link MailEnvironment} to a stem. This is not for the current configuration,
     * but is a general utility that should go in this class.
     * @param me
     * @return
     */
     public static QDLStem convertMEToStem(MailEnvironment me){
      QDLStem stem = new QDLStem();
      if(me.carbonCopy!=null){
          QDLStem q = addAll(me.carbonCopy);
          if(q!=null){
              stem.put(MAIL_CC, q);
          }
      }


      if(me.blindCarbonCopy!=null){
          QDLStem q = addAll(me.blindCarbonCopy);
          if(q!=null){
              stem.put(MAIL_BCC, q);
          }
      }
      if(me.recipients!=null){
          QDLStem q = addAll(me.recipients);
          if(q!=null){
              stem.put(MAIL_TO, q);
          }
      }

      if(me.replyTo!=null)stem.put(MAIL_REPLY_TO, me.replyTo);
      if(me.from!=null)stem.put(MAIL_FROM, me.from);
      if(me.username!=null)stem.put(MAIL_USERNAME, me.username);
      if(me.server!=null)stem.put(MAIL_SERVER, me.server);
      if(me.password!=null)stem.put(MAIL_PASSWORD, me.password);
      if(me.contentType!=null)stem.put(MAIL_CONTENT_TYPE, me.contentType);
      stem.put(MAIL_USE_SSL, me.useSSL);
      stem.put(MAIL_START_TLS, me.starttls);
      stem.put(MAIL_PORT, (long)me.port);

      return stem;
     }

    @Override
    public JSONObject serializeToJSON() {
        return null;
    }

    @Override
    public void deserializeFromJSON(JSONObject json) {

    }
}
