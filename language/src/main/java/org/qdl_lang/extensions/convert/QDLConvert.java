package org.qdl_lang.extensions.convert;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.qdl_lang.evaluate.IOEvaluator;
import org.qdl_lang.evaluate.StemEvaluator;
import org.qdl_lang.evaluate.SystemEvaluator;
import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.extensions.QDLVariable;
import org.qdl_lang.parsing.IniParserDriver;
import org.qdl_lang.state.State;
import org.qdl_lang.util.InputFormUtil;
import org.qdl_lang.util.QDLFileUtil;
import org.qdl_lang.variables.*;
import org.qdl_lang.variables.values.BooleanValue;
import org.qdl_lang.variables.values.QDLKey;
import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.xml.XMLUtils;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.FileUtil;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.regex.Pattern;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/13/23 at  7:33 AM
 */
public class QDLConvert implements QDLMetaModule {
    public static final String XML_IMPORT_NAME = "xml_in";
    public static final String XML_EXPORT_NAME = "xml_out";
    public static final int XML_IMPORT_LEVEL_FLATTEN = 0; // no @'s, ignore most structures, attributes flattened to properties
    public static final int XML_IMPORT_LEVEL_ATTR = 1; // @attributes, @text
    public static final int XML_IMPORT_LEVEL_COMMENTS = 2; // @attributes, @text, @comment
    public static final int XML_IMPORT_LEVEL_EXACT = 5; // All structures converted. Intended for roundtripping.

    // These are prefixed with @ signs since those are not valid XML tagnames, hence there can be
    // no collision ambiguity on import/export
    public static final String DOC_CAPUT = ">";
    public static final String ATTRIBUTE_KEY = DOC_CAPUT + "attributes";
    public static final String CDATA_KEY = DOC_CAPUT + "cdata";
    public static final String TEXT_KEY = DOC_CAPUT + "text";
    public static final String ATTRIBUTE_CAPUT = "@";
    public static final String DECLARATION_KEY = DOC_CAPUT + "declaration";
    public static final String DTD_TYPE_KEY = DOC_CAPUT + "doc_type";
    public static final String PROCESSING_IfNSTRUCTION_KEY = DOC_CAPUT + "processing_instruction";
    public static final String COMMENT_KEY = DOC_CAPUT + "comment";
    public static final String ENTITY_KEY = DOC_CAPUT + "entity";

    public static final String INI_OUT = "ini_out";
    public static final String INI_IN = "ini_in";
     /*
     Node values from the Node class:
    short ELEMENT_NODE = 1;
    short ATTRIBUTE_NODE = 2;
    short TEXT_NODE = 3;
    short CDATA_SECTION_NODE = 4;
    short ENTITY_REFERENCE_NODE = 5;
    short ENTITY_NODE = 6;
    short PROCESSING_INSTRUCTION_NODE = 7;
    short COMMENT_NODE = 8;
    short DOCUMENT_NODE = 9;
    short DOCUMENT_TYPE_NODE = 10;
    short DOCUMENT_FRAGMENT_NODE = 11;
    short NOTATION_NODE = 12;

      */

    public static final String ARG_FILE = "file";
    public static final String ARG_IGNORE_COMMENTS = "ignore_comments";
    public static final String ARG_VALIDATE = "validate";
    public static final String ARG_IGNORE_WHITESPACE = "ignore_whitespace";
    public static final String ARG_IMPORT_LEVEL = "level";

    public class XMLImport implements QDLFunction {
        @Override
        public String getName() {
            return XML_IMPORT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            return asQDLValue(newEvaluate(qdlValues, state));
            // return oldEvaluate(object, state);
        }

        /**
         * Uses XML to JSON (Jackson, actually) serialization. This does nto preserver order or structure so
         * is of decidedly limited use.
         *
         * @param objects
         * @param state
         * @return
         * @throws Throwable
         */
        protected Object newEvaluate2(Object[] objects, State state) throws Throwable {
            // To get this to work, uncomment the xom utility in the pom.
            String xml = FileUtil.readFileAsString(objects[0].toString());

            XMLSerializer xmlSerializer = new XMLSerializer();
            JSON json = xmlSerializer.read(xml);
            QDLStem stem = new QDLStem();
            stem.fromJSON(json);
            System.out.println(json.toString(2));
            System.out.println(stem.inputForm(1));
            return stem;
        }

        protected Object newEvaluate(QDLValue[] qdlValues, State state) throws XMLStreamException {
            XMLEventReader xer = null;
            boolean ignoreWhitespace = true;
            boolean validate = true;
            int level = XML_IMPORT_LEVEL_EXACT;
            boolean ignoreComments = XML_IMPORT_LEVEL_COMMENTS <= level;
            String raw = null;
            QDLStem inStem = null;
            QDLStem configStem = null;
            if (qdlValues.length == 1) {
                if (qdlValues[0].isStem()) {
                    configStem = qdlValues[0].asStem();
                }
                if (qdlValues[0].isString()) {
                    configStem = new QDLStem(); // make it empty.
                }
            } else {
                if (!(qdlValues[0].isString())) {
                    throw new BadArgException(getName() + " requires the first argument is a string", 0);
                }
                raw = qdlValues[0].asString();
                configStem = new QDLStem();
                configStem = qdlValues[1].asStem();
            }
/*
            if (!(configStem instanceof QDLStem)) {
                throw new BadArgException(getName() + " requires that that the configuration argument is  a stem", 1);
            }
*/
            inStem = (QDLStem) configStem;
            String inString = getFileArg(qdlValues[0].getValue(), state, getName());
            if (inStem.containsKey(ARG_VALIDATE)) {
                validate = inStem.getBoolean(ARG_VALIDATE);
            }
            if (inStem.containsKey(ARG_IGNORE_WHITESPACE)) {
                ignoreWhitespace = inStem.getBoolean(ARG_IGNORE_WHITESPACE);
            }
            if (inStem.containsKey(ARG_IGNORE_COMMENTS)) {
                ignoreComments = inStem.getBoolean(ARG_IGNORE_COMMENTS);
            }
            if (inStem.containsKey(ARG_IMPORT_LEVEL)) {
                level = inStem.getLong(ARG_IMPORT_LEVEL).intValue();
            }
            xer = XMLUtils.getXMLEventReader(new StringReader(inString));
            if (!xer.hasNext()) {
                throw new IllegalStateException("Error! no XML found to deserialize");
            }

            XMLEvent xe = xer.nextEvent();
            if (!xe.isStartDocument()) {
                throw new IllegalStateException("Error! no XML start of document to deserialize");
            }
            QDLStem out = new QDLStem();
            if (level == XML_IMPORT_LEVEL_EXACT) {
                StartDocument startDocument = (StartDocument) xe;
                QDLStem declaration = new QDLStem();
                if (startDocument.getVersion() == null) {
                    declaration.put("@version", "1.0");
                } else {
                    declaration.put("@version", startDocument.getVersion());
                }
                if (startDocument.getCharacterEncodingScheme() == null) {
                    declaration.put("@encoding", "UTF-8");
                } else {
                    declaration.put("@encoding", startDocument.getCharacterEncodingScheme());
                }
                declaration.put("@standalone", startDocument.isStandalone());

                out.put(DECLARATION_KEY, declaration); // No good way to get the text of this...
            }
            MyStemStack<QDLStem> cursorStack = new MyStemStack<>();
            cursorStack.push(out);
            QDLStem previous = null;
            String currentTag = null;
            while (xer.hasNext()) {
                XMLEvent event = (XMLEvent) xer.next();
                switch (event.getEventType()) {

                    case XMLEvent.CHARACTERS:
                        Characters characters = event.asCharacters();
                        if (wsPattern.matcher(characters.getData()).matches()) {
                            break;
                        }
                        if (ignoreWhitespace && characters.isIgnorableWhiteSpace()) {
                        } else {
                            cursorStack.peek().listAdd(asQDLValue(event.asCharacters().toString()));
                        }
                        break;
                    case XMLEvent.ATTRIBUTE: // Never seems to get called. See start element
                        break;
                    case XMLEvent.CDATA:
                        cursorStack.peek().put(CDATA_KEY, Boolean.TRUE);
                        break;

                    case XMLEvent.COMMENT:
                        if (XML_IMPORT_LEVEL_COMMENTS <= level) {
                            Comment comment = (Comment) event;
                            if (cursorStack.size() == 1) {
                                // if this is before any elements have been found, add to the
                                // main stem.
                                cursorStack.peek().put(COMMENT_KEY, comment.getText());
                            } else {

                                cursorStack.peek().put(COMMENT_KEY, comment.getText());
                            }
                        }
                        break;
                    case XMLEvent.START_ELEMENT:
                        StartElement startElement = (StartElement) event;

                        currentTag = startElement.getName().getLocalPart();
                        previous = cursorStack.peek();
                        QDLStem targetStem = new QDLStem();
                        if (previous.containsKey(currentTag)) {
                            previous.getStem(currentTag).listAdd(asQDLValue(targetStem));
                        } else {
                            QDLStem x = new QDLStem();
                            previous.put(currentTag, x);
                            x.listAdd(asQDLValue(targetStem));

                        }
                        cursorStack.push(targetStem);
                        //}
                        Iterator<Attribute> attributeIterator = startElement.getAttributes();
                        if (XML_IMPORT_LEVEL_ATTR <= level) {
                            while (attributeIterator.hasNext()) {
                                Attribute attribute = attributeIterator.next();
                                cursorStack.peek().put(ATTRIBUTE_CAPUT + attribute.getName().getLocalPart(), attribute.getValue());
                            }
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        previous = cursorStack.pop();
                        String endTag = event.asEndElement().getName().getLocalPart();
                        if (!currentTag.equals(endTag)) {
                            //                       throw new ParsingException("Incorrect XML document. Expected end tag '" + currentTag + "' and got '" + endTag+"'");
                        }
                        break;
                    case XMLEvent.START_DOCUMENT:
                        break;
                    case XMLEvent.END_DOCUMENT:
                        break;
                    case XMLEvent.DTD:
                        DTD dtd = (DTD) event;
                        if (level == XML_IMPORT_LEVEL_EXACT) {
                            out.put(DTD_TYPE_KEY, dtd.getDocumentTypeDeclaration());
                        }
                        break;
                    case XMLEvent.ENTITY_DECLARATION:
                        EntityDeclaration entityDeclaration = (EntityDeclaration) event;
                        break;
                    case XMLEvent.ENTITY_REFERENCE:
                        EntityReference entityReference = (EntityReference) event;
                        break;
                    default:
                        System.out.println(event.getEventType() + ": " + event);
                }
            }
            if (level == XML_IMPORT_LEVEL_FLATTEN) {
                // Cannot actually snarf this in one pass since it accumulates structure as it
                // parses. Doing it in one pass would require a very complex cursor system.
                for (Object key : out.keySet()) {
                    if (rejectKey(key)) {
                        out.remove(key.toString());
                    }
                }
                out = doSnarf(out);
            }
            return out;
        }


        /*

            module_load(info().lib.xml, 'java') =: q
     module_import(q)
  from_xml('<b><a p="r">foo</a><a p="s">bar</a></b>')

  {'>declaration':'<?xml version="null" encoding=\'null\' standalone=\'no\'?>', 'b':[{'a':[['foo']~{'@p':'r'},['bar']~{'@p':'s'}]}]}
  from_xml('<a p="s">bar</a>')

   int START_ELEMENT = 1;
   int END_ELEMENT = 2;
   int PROCESSING_INSTRUCTION = 3;
   int CHARACTERS = 4;
   int COMMENT = 5;
   int SPACE = 6;
   int START_DOCUMENT = 7;
   int END_DOCUMENT = 8;
   int ENTITY_REFERENCE = 9;
   int ATTRIBUTE = 10;
   int DTD = 11;
   int CDATA = 12;
   int NAMESPACE = 13;
   int NOTATION_DECLARATION = 14;
   int ENTITY_DECLARATION = 15;
         */

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            if (argCount == 1) {
                doxx.add(getName() + "(cfg.) - parse an XML document using the parameters in cfg.");
            } else {
                doxx.add(getName() + "(string, cfg.) - parse the string as an XML document using the parameters in cfg.");
            }
            doxx.add("This turns an XML document into a stem that has a specific contract for the elements.");
            doxx.add("cfg. has the following keys and values");
            doxx.add(ARG_FILE + " - (String) Name of the file to be read (ignored if the first argument is a string)");
            doxx.add(ARG_VALIDATE + " - (Boolean) Validate the XMl document before parsing");
            doxx.add(ARG_IGNORE_WHITESPACE + " - (Boolean) If true, will ignore as much whitespace as possible.");
            doxx.add(ARG_IGNORE_COMMENTS + " - (Boolean) Suppress adding comments to the stem");
            doxx.add("\nNote that the structure of each entry is of the form");
            doxx.add("[text]~{properties}");
            doxx.add("where properties are prefixed with a " + ATTRIBUTE_CAPUT);
            doxx.add("E.g.");
            doxx.add("   " + getName() + "(<b><a p=\"r\">foo</a></b>)");
            doxx.add("{'b':{'a':[['foo']~{'@p':r}]}");
            doxx.add("Since a has a single entry, it occurs as a list with a single element.");
            doxx.add("E.g. compare with multiple a elements in ");
            doxx.add("   " + getName() + "('<b><a p=\"r\">foo</a><a p=\"s\">bar</a></b>')");
            doxx.add("{'b':[{'a':[['foo']~{'@p':'r'},['bar']~{'@p':'s'}]}]}");
            doxx.add("See also: " + GET_ATTR_NAME);
            return doxx;
        }

        String wsRegex = "[ \\t\\r\\n\\u000C]+";
        Pattern wsPattern = Pattern.compile(wsRegex);
    }

    // NOTE this is nowhere near ready for release. It is actually really hard to get this
    // to work in all but super simple cases.
    public class XMLExport implements QDLFunction {
        @Override
        public String getName() {
            return XML_EXPORT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            if (!(qdlValues[0].isStem())) {
                throw new BadArgException(getName() + " requires a stem as its argument", 0);
            }
            QDLStem arg = qdlValues[0].asStem();

            boolean exportToFile = qdlValues.length == 2;
            String fileName = null;
            if (exportToFile) {
                if (!(qdlValues[1].isString())) {
                    throw new BadArgException(getName() + " requires a string as its second argument if present.", 1);
                }
                fileName = qdlValues[1].asString();
            }
            Writer w = new StringWriter();

            XMLOutputFactory xof = XMLOutputFactory.newInstance();

            try {
                XMLStreamWriter xsw = xof.createXMLStreamWriter(w);
                if (arg.containsKey(DECLARATION_KEY)) {
                    QDLStem declarations = arg.getStem(DECLARATION_KEY);
                    String version = declarations.containsKey("@version") ? declarations.getString("@version") : "1.0";
                    String charSet = declarations.containsKey("@encoding") ? declarations.getString("@encoding") : "UTF-8";
                    xsw.writeStartDocument(charSet, version);
                } else {
                    xsw.writeStartDocument();
                }
                String rootTag = null;
                for (Object key : arg.keySet()) {
                    if (!key.toString().startsWith(DOC_CAPUT)) {
                        rootTag = key.toString();
                    }
                }

                if (arg.containsKey(COMMENT_KEY)) {
                    xsw.writeComment(arg.getString(COMMENT_KEY));
                }
                writeElement(xsw, arg.getStem(rootTag), rootTag);
                xsw.writeEndDocument();
            } catch (XMLStreamException e) {
                if (DebugUtil.isEnabled()) {
                    e.printStackTrace();
                }
                throw new IllegalStateException(getName() + " could not serialize stem to XML:" + e.getMessage());
            }
            if (exportToFile) {
                try {
                    QDLFileUtil.writeTextFile(state, fileName, w.toString());
                } catch (Throwable e) {
                    if (DebugUtil.isEnabled()) {
                        e.printStackTrace();
                    }
                    throw new IllegalStateException(getName() + " could not write file '" + fileName + "':" + e.getMessage());
                }
                return BooleanValue.True;
            }
            return asQDLValue(w.toString());
        }


        protected void writeContent(XMLStreamWriter xsw, Object object) throws XMLStreamException {
            if (object instanceof QDLStem) {
                // Object is of form [x0,x1,...]~{@p0,@p1,...n0,n1.,,,}
                // where xi are lines of text
                // @pi are attributes
                // @ni are nodes to recurse over
                // Write the text
                QDLList qdlList = ((QDLStem) object).getQDLList();
                QDLMap qdlMap = ((QDLStem) object).getQDLMap();
                boolean isCDATA = qdlMap.containsKey(CDATA_KEY) && qdlMap.get(CDATA_KEY).asBoolean();
                // Write attributes immediately after tag or you get an exception.
                for (String key : qdlMap.keySet()) {
                    if (isProperty(key)) {
                        xsw.writeAttribute(key.substring(ATTRIBUTE_CAPUT.length()), qdlMap.get(key).toString());
                    }
                }
                for (Object key : qdlList.orderedKeys()) {
                    if (isCDATA) {
                        xsw.writeCData(qdlList.get((Long) key).toString());
                    } else {
                        xsw.writeCharacters(xmlEscape(qdlList.get((Long) key).toString()));
                    }
                }
                for (String key : qdlMap.keySet()) {
                    if (!isProperty(key)) {
                        Object value = qdlMap.get(key);
                        if (key.equals(COMMENT_KEY)) {
                            xsw.writeComment(value.toString());
                            continue;
                        }

                        if (Constant.isStem(value)) {
                            writeElement(xsw, (QDLStem) value, key);
                        } else {
                            if (Constant.isSet(value)) {
                                xsw.writeStartElement(key);
                                xsw.writeCData(((QDLList) value).toJSON().toString());
                                xsw.writeEndElement();
                            } else {

                                xsw.writeStartElement(key);
                                if (isCDATA) {
                                    xsw.writeCData(((QDLList) value).toJSON().toString());
                                } else {
                                    xsw.writeCharacters(xmlEscape(value.toString()));
                                }
                                xsw.writeEndElement();
                            }

                        }
                    }
                }

            } else {
                throw new IllegalStateException("unknown content");
            }
        }

        protected void writeElement(XMLStreamWriter xsw, QDLStem stem, String tagname) throws XMLStreamException {
            if (stem.isList()) {
                // have to process each element
                QDLList qdlList = stem.getQDLList();
                for (Object v : qdlList.values()) {
                    xsw.writeStartElement(tagname);
                    if (Constant.isStem(v)) {
                        writeContent(xsw, v);

                    } else {

                        writeContent(xsw, v);
                    }
                    xsw.writeEndElement();
                }
            }
        }

        List<String> doxx = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (doxx.isEmpty()) {
                doxx.add(getName() + "(arg.{,file_path}) - export the arg. to XML, writing to file_path if present.");
                doxx.add("If no file is given, the result is returned as a string.");
                doxx.add("Note that writing a generic stem to XML is close to a Black Art. If the argument is well-structured, you");
                doxx.add("should get very serviceable XML. A generic stem does not really fit");
                doxx.add("with how XML is intended. For instance, there is no concept of a set, but the result is valid XML. ");
                doxx.add("");
            }
            return doxx;
        }
    }

    public static void main(String[] args) throws Throwable {
        QDLConvert QDLConvert = new QDLConvert();
        XMLImport xmlImport = QDLConvert.new XMLImport();
        // Test XML as configuration language
        //       xmlImport.evaluate(new Object[]{DebugUtil.getDevPath()+"/qdl/language/src/main/resources/xml/simple0.xml"}, null);
        QDLStem cfg = new QDLStem();
        State state = new State();
        state.setServerMode(false);
        //cfg.put("file", DebugUtil.getDevPath()+"/qdl/language/src/main/resources/xml/planes.xml");
        cfg.put("file", asQDLValue(DebugUtil.getDevPath() + "/qdl/language/src/main/resources/xml/simple0.xml"));
        QDLStem stem =  xmlImport.evaluate(new QDLValue[]{asQDLValue(cfg)}, state).asStem();
        XMLExport xmlExport = QDLConvert.new XMLExport();
        Object x = xmlExport.evaluate(new QDLValue[]{asQDLValue(stem)}, state);
        System.out.println(x);
        HOCONExport hoconExport = QDLConvert.new HOCONExport();
//        System.out.println(hoconExport.evaluate(new Object[]{stem}, null));
        // Next are for testing XML as a text markup language
        //xmlImport.evaluate(new Object[]{DebugUtil.getDevPath()+"/qdl/language/src/main/resources/func_help.xml"}, null);
        //xmlImport.evaluate(new Object[]{DebugUtil.getDevPath()+"/oa4mp/oa4mp-website/src/site/xhtml/server/manuals/authorized.xhtml"}, null);

    }

    public static class MyStemStack<V extends QDLStem> extends Stack<V> {
        public QDLStem peekLast(String currentTag) {
            QDLStem qqq = peek().getQDLList().last().entry.asStem();
            return qqq;
            //return (QDLStem) peek().getStem(currentTag).getQDLList().last().entry;
        }
    }

    public static String GET_ATTR_NAME = "attr";

    public class GetAttributes implements QDLFunction {
        @Override
        public String getName() {
            return GET_ATTR_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            if (!(qdlValues[0].isStem())) {
                throw new BadArgException(getName() + " requires a stem as its first argument", 0);
            }
            QDLStem stem = qdlValues[0].asStem();
            long axis = 0L;
            boolean hasAxis = qdlValues.length == 2;
            if (hasAxis) {
                if (!(qdlValues[1].isLong())) {
                    throw new BadArgException(getName() + " require a long for its second argument", 1);
                }
                axis = qdlValues[1].asLong();
            }
            Polyad polyad = new Polyad(StemEvaluator.ALL_KEYS);
            polyad.addArgument(new ConstantNode(asQDLValue(stem)));
            if (hasAxis) {
                polyad.addArgument(new ConstantNode(asQDLValue(axis)));
            }
            polyad.evaluate(state);
            QDLStem z = polyad.getResult().asStem();
            List<QDLValue> values = z.getQDLList().values();
            List<String> out = new ArrayList<>();
            QDLStem result = new QDLStem();
            for (QDLValue qdlValue : values) {
                if (qdlValue.isStem()) {
                    QDLStem zz = qdlValue.asStem();
                    ArrayList valueList = qdlValue.asStem().getQDLList().values();
                    boolean gotOne = false;
                    for (Object ooo : valueList) {
                        if (isProperty(ooo)) {
                            gotOne = true;
                            break;
                        }
                    }
                    if (gotOne) {
                        QDLStem aaa = new QDLStem();
                        aaa.addList(valueList);
                        result.listAdd(asQDLValue(aaa));
                    }
                }
            }
            return asQDLValue(result);
        }

        /*
              module_load(info().lib.xml, 'java') =: q
     module_import(q)
         from_xml('/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/xml/planes.xml') =: p.
         */
        List<String> doxx = null;

        @Override
        public List<String> getDocumentation(int argCount) {
            if (doxx == null) {
                doxx = new ArrayList<>();
                doxx.add(getName() + "(arg.{, axis}) - return the  XML attributes for this stem as multi-indices");
                doxx.add("This has the same semantics as the " + StemEvaluator.ALL_KEYS + " function.");
            }
            return doxx;
        }
    }

    public static final String SNARF_NAME = "snarf";

    public class Snarf implements QDLFunction {
        @Override
        public String getName() {
            return SNARF_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }
        /*
        The problem with doing this during processing is that elements may be spread out all over the place and
        so only after they have been accumulated can we flatten them.
         */

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            QDLStem inStem = qdlValues[0].asStem();
            for (Object key : inStem.keySet()) {
                if (rejectKey(key)) {
                    inStem.remove(key.toString());
                }
            }
            return asQDLValue(doSnarf(inStem));
        }


        List<String> doxx = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (doxx.isEmpty()) {
                doxx.add(getName() + "(arg.{,glom_text}) - flatten all structures in the XML stem");
                doxx.add("glom_text- boolean if true, multiline text nodes are turned into a single");
                doxx.add("           string, rather than being a list of lines. Default is false.");
                doxx.add("This means to replace lists with singletons if possible, remove XML specific");
                doxx.add("structures (like the declarations, comments) and generally reduce the XML to");
                doxx.add("a minimally usable stem. This is not really intended for roundtripping XML");
                doxx.add("but to simply an XML document for processing.");
                doxx.add("E.g.");
                doxx.add("  x. := " + XML_IMPORT_NAME + "({" + ARG_FILE + ":'/path/to/config.xml'})");
                doxx.add("  cfg. := " + SNARF_NAME + "(x.'server'.0)");
                doxx.add("  cfg.'client'.'@host'");
                doxx.add("localhost:9443");
                doxx.add("would import the XML file, peel off a specfic (in this case configuration) node");
                doxx.add("and access the property @host in the client node, returning the value");
                doxx.add("This makes working with XML configuration files quite routine in QDL.");
                doxx.add("They are just mapped to stems.");
            }
            return doxx;
        }
    }

    protected boolean isProperty(Object obj) {
        if (obj instanceof String) {
            return ((String) obj).startsWith(ATTRIBUTE_CAPUT);
        }
        return false;
    }

    /**
     * Starts the recursion. Every XML document has a single root element
     *
     * @param inStem
     * @return
     */
    protected QDLStem doSnarf(QDLStem inStem) {
        QDLStem root = new QDLStem();
        StemKeys stemKeys = inStem.keySet();
        if (stemKeys.size() == 0) {
            throw new IllegalArgumentException("The XML document is missing the root element.");
        }
        if (stemKeys.size() != 1) {
            throw new IllegalArgumentException("The XML document is malformed, with more than one root element");
        }
        QDLKey rootKey = stemKeys.iterator().next();
        QDLList content = inStem.get(rootKey).asStem().getQDLList();
        root.put(rootKey, snarfList(content, true));
        return root;
    }

    protected Object snarfList(QDLList list, boolean glomStringsTogether) {
        /*
         These are entries of the form [{..},{...},...] where the stems are either
         terminal nodes or have elements. This will strip off the outer list if
         a singleton and process the node. Otherwise, it will return a list
         of processed nodes.
         */
        boolean isSingleton = list.size() == 1; // strip off list.
        QDLStem out = isSingleton ? null : (new QDLStem());
        for (Object v : list.values()) {
            QDLStem current = (QDLStem) v; // Every element is a stem
            if (isSingleton) {
                return snarfNode(current, glomStringsTogether); // return the list of strings
            }
            out.listAdd(asQDLValue(snarfNode(current, glomStringsTogether)));
        }
        return out;
    }


    /**
     * Process end stem. This is typically of the form [lines]~{properties}
     *
     * @param stem
     * @param glomText return text lines as a single string.
     * @return
     */
    protected Object snarfNode(QDLStem stem, boolean glomText) {
        if (stem.isList()) {   // no properties to process
            if (stem.getQDLList().size() == 1) {
                // Single element so this is of the form [string]. Just return string
                return stem.getQDLList().values().get(0);
            }
            if (glomText) {
                String out = "";
                boolean firstPass = true;
                for (Object s : stem.getQDLList().values()) {
                    if (firstPass) {
                        firstPass = false;
                        out = out + s.toString();
                    } else {
                        out = out + "\n" + s.toString();
                    }
                }
                return out;
            }
            return stem.getQDLList(); // Of form [s0, s1,...] just return strings.
        }
        // so we have [text]~{properties and nodes}
        QDLStem out = new QDLStem();
        out.setQDLList(stem.getQDLList());
        QDLMap map = stem.getQDLMap();
        for (String key : map.keySet()) {
            if (rejectKey(key)) {
                continue; // catch embedded >comment elements
            }
            // process the rest of the elements
            Object v = map.get(key);
            if (isProperty(key)) {
                out.put(key, v);
            } else {
                out.put(key, snarfList(((QDLStem) v).getQDLList(), glomText));
            }
        } // end for
        return out;
    }

    protected boolean rejectKey(Object key) {
        return key.toString().equals(DECLARATION_KEY) || key.toString().equals(COMMENT_KEY) || key.toString().equals(DTD_TYPE_KEY);
    }
    /*
          module_load(info().lib.xml, 'java') =: q
 module_import(q) ;
 xml := '<a p="x"><p>Y</p><q>Z</q></a>';
 import(xml) =: q.
   r. := import({'file':'/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/xml/planes.xml'})

     */

    public static String YAML_IMPORT_NAME = "yaml_in";

    public class YAMLImport implements QDLFunction {
        @Override
        public String getName() {
            return YAML_IMPORT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            String inString = getFileArg(qdlValues[0].getValue(), state, getName());
            // deprecated SafeConstructor, but prevents a very scary injection attack:
            // https://devhub.checkmarx.com/cve-details/CVE-2022-1471/?utm_source=jetbrains&utm_medium=referral&utm_campaign=idea&utm_term=maven
            Yaml yaml = new Yaml(new SafeConstructor());
            Object map = yaml.load(inString);
            QDLStem stem = null;
            if (map instanceof Map) {
                stem = StemUtility.mapToStem((Map) map);
            }
            if (map instanceof List) {
                stem = StemUtility.listToStem((List) map);
            }
            return asQDLValue(stem);
        }

        List<String> doxx = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (doxx.isEmpty()) {
                doxx.add(getName() + "(arg) - convert a string of YAML to a stem");
            }
            return doxx;
        }
    }

    public static String YAML_EXPORT_NAME = "yaml_out";

    public class YAMLExport implements QDLFunction {
        @Override
        public String getName() {
            return YAML_EXPORT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            if (!(qdlValues[0].isStem())) {
                throw new BadArgException(getName() + " requires a stem as its first argument", 0);
            }
            QDLStem stem = qdlValues[0].asStem();
            boolean exportToFile = false;
            String fileName = null;
            if (qdlValues.length == 2) {
                if (!(qdlValues[1].isString())) {
                    throw new BadArgException(getName() + " requires a string as its second argument if present", 1);
                }
                exportToFile = true;
                fileName = qdlValues[1].asString();
            }
            Yaml yaml = new Yaml();
            JSON json = stem.toJSON();
            String out = yaml.dump(json);
            if (!exportToFile) {
                return asQDLValue(out);
            }
            try {
                QDLFileUtil.writeTextFile(state, fileName, out);
            } catch (Throwable e) {
                if (DebugUtil.isEnabled()) {
                    e.printStackTrace();
                }
                throw new IllegalStateException(getName() + " could not write file '" + fileName + "':" + e.getMessage());
            }
            return BooleanValue.True;
        }

        List<String> doxx = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (doxx.isEmpty()) {
                doxx.add(getName() + "(arg.{,file_path}) - convert a stem to YAML, writing to file_path if present");
                doxx.add("If there is no file specified, return the converted YAML as a string.");
            }
            return doxx;
        }
    }

    public static String HOCON_IMPORT_NAME = "hocon_in";

    public class HOCONImport implements QDLFunction {
        @Override
        public String getName() {
            return HOCON_IMPORT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            String inString = getFileArg(qdlValues[0].getValue(), state, getName());
            StringReader stringReader = new StringReader(inString);
            Config conf = ConfigFactory.parseReader(stringReader);
            String rawJSON = conf.root().render(ConfigRenderOptions.concise());
            // String rawJSON = conf.root().render(ConfigRenderOptions.defaults().setJson(false).setComments(true).setOriginComments(true));
            QDLStem out = new QDLStem();
            out.fromJSON(JSONObject.fromObject(rawJSON));
            return asQDLValue(out);
        }

        List<String> doxx = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (doxx.isEmpty()) {
                doxx.add(getName() + "(arg) - convert a HOCON string to a stem");
            }
            return doxx;
        }
    }

    public static String HOCON_EXPORT_NAME = "hocon_out";

    public class HOCONExport implements QDLFunction {
        @Override
        public String getName() {
            return HOCON_EXPORT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public QDLValue evaluate(QDLValue[] objects, State state) {
            if (!(objects[0].isStem())) {
                throw new BadArgException(getName() + " requires a stem as its first argument", 0);
            }
            QDLStem stem = objects[0].asStem();
            boolean exportToFile = false;
            String fileName = null;
            if (objects.length == 2) {
                if (!(objects[1].isString())) {
                    throw new BadArgException(getName() + " requires a string as its second argument if present", 1);
                }
                exportToFile = true;
                fileName = objects[1].asString();
            }
            if(stem.isList()){
                QDLStem wrapper = new QDLStem();
                wrapper.put("root", stem);
                stem = wrapper;
            }
            StringReader stringReader = new StringReader(stem.toJSON().toString());
            Config conf = ConfigFactory.parseReader(stringReader);
            //String rawJSON = conf.root().render(ConfigRenderOptions.concise());
            String rawJSON = conf.root().render(ConfigRenderOptions.defaults().setJson(false).setOriginComments(false).setFormatted(true));
            if (!exportToFile) {
                return asQDLValue(rawJSON);
            }
            try {
                QDLFileUtil.writeTextFile(state, fileName, rawJSON);
            } catch (Throwable e) {
                if (DebugUtil.isEnabled()) {
                    e.printStackTrace();
                }
                throw new IllegalStateException(getName() + " could not write file '" + fileName + "':" + e.getMessage());
            }
            return BooleanValue.True;
        }

        List<String> doxx = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (doxx.isEmpty()) {
                doxx.add(getName() + "(arg.{,file_path} - convert the stem to HOCON. If file_path is present, write to the file.");
                doxx.add("Otherwise, return the result as a string.");
                doxx.add("Note that HOCON does not support serializing just an array, so that will be added to");
                doxx.add("a stem as the single entry 'root'. Sorry, best we can do");
            }
            return doxx;
        }
    }

    /**
     * This will look into the object if it is a stem or string and determine if
     * there is a file to import. This is used by all of the imports. The resulting string
     * will be the content to import.
     *
     * @param object
     * @param state
     * @param name
     * @return
     */
    protected String getFileArg(Object object, State state, String name) {
        String inString;
        if (object instanceof QDLStem) {
            QDLStem cfg = (QDLStem) object;
            if (cfg.containsKey(ARG_FILE)) {
                String fileName = cfg.getString(ARG_FILE);
                try {
                    inString = QDLFileUtil.readTextFile(state, fileName);
                } catch (Throwable e) {
                    throw new GeneralException(name + " could not read file:" + e.getMessage());
                }
            } else {
                throw new IllegalArgumentException(name + " requires the stem specify a file to import.");
            }
        } else {
            if (!(object instanceof String)) {
                throw new IllegalArgumentException(name + " requires a string or stem as its argument");
            }
            inString = (String) object;
        }
        return inString;
    }

    /**
     * Replace characters in a string by their XML analogs. result is a legal string for the XML document.
     *
     * @param s
     * @return
     */
    protected String xmlEscape(String s) {
        StringBuffer sb = new StringBuffer();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            switch (c) {
                default:
                    sb.append(c);
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
            }
        }
        return sb.toString();
    }

    public static String QDL_IMPORT_NAME = "qdl_in";

    public class QDLImport implements QDLFunction {
        @Override
        public String getName() {
            return QDL_IMPORT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            String inString = getFileArg(qdlValues[0].getValue(), state, getName());
            Polyad polyad = new Polyad(SystemEvaluator.INTERPRET);
            polyad.addArgument(new ConstantNode(asQDLValue(inString)));
            try {
                polyad.evaluate(state);
                return polyad.getResult();
            } catch (Throwable e) {
                if (DebugUtil.isEnabled()) {
                    e.printStackTrace();
                }
                throw new BadArgException(getName() + " unable to evaluate argument:" + e.getMessage(), 0);
            }
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            return null;
        }
    }

    public static String QDL_EXPORT_NAME = "qdl_out";

    public class QDLExport implements QDLFunction {
        @Override
        public String getName() {
            return QDL_EXPORT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        /*
            module_import(module_load(info().lib.convert, 'java'));
              r. := rename_keys(random_string(5,5), random_string(5,5))
               qdl_out(r., {'file':'/tmp/out.qdl'})

         */
        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            if (!(qdlValues[0].isStem())) {
                throw new BadArgException(getName() + " requires a stem as its first argument", 0);
            }
            QDLStem stem = qdlValues[0].asStem();
            boolean exportToFile = false;
            String fileName = null;
            if (qdlValues.length == 2) {
                if (!(qdlValues[1].isString())) {
                    throw new BadArgException(getName() + " requires a string as its second argument if present", 1);
                }
                exportToFile = true;
                fileName = qdlValues[1].asString();
            }
            String out = InputFormUtil.inputForm(stem);
            if (exportToFile) {
                try {
                    QDLFileUtil.writeTextFile(state, fileName, out);
                } catch (Throwable e) {
                    if (DebugUtil.isEnabled()) {
                        e.printStackTrace();
                    }
                    throw new BadArgException("unable to save file '" + fileName + "':" + e.getMessage(), 1);
                }
                return BooleanValue.True;
            }
            return asQDLValue(out);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            return null;
        }
    }

    @Override
    public JSONObject serializeToJSON() {
        return null;
    }

    @Override
    public void deserializeFromJSON(JSONObject json) {

    }

    public class IniImport implements QDLFunction {
        @Override
        public String getName() {
            return INI_IN;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1,2};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (!(qdlValues[0].isString())) {
                throw new BadArgException(INI_IN + " requires a string as its argument", 0);
            }
            boolean allowListEntries = true;

            if(qdlValues.length == 2 && qdlValues[1].isBoolean()){
                allowListEntries = qdlValues[1].asBoolean();

            }
            String content = qdlValues[0].asString();
            IniParserDriver iniParserDriver = new IniParserDriver();
            StringReader stringReader = new StringReader(content);
            QDLStem out = iniParserDriver.parse(stringReader, allowListEntries);
            return asQDLValue(out);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            if (argCount == 1) {
                dd.add(INI_IN + "(string{,allowListEntries}) - convert a string in ini file format to a stem");
                dd.add("allowListEntries = is a flag that when true will write list entries in the form");
                dd.add("    _index, allowing for putting lists in the body of a section rather than on a line.");
                dd.add("See http://qdl-lang.org/pdf/qdl_ini_file.pdf which is included in the");
                dd.add("docs directory of the standard distribution too.");

            }
            return dd;
        }
    }

    public class IniExport implements QDLFunction{
        @Override
        public String getName() {
            return INI_OUT;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1,2,3};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if(!(qdlValues[0].isStem())){
                throw new BadArgException(INI_OUT + " takes a stem as its first argument", 0);
            }
            int indentfactor = 1; // default here
            boolean allowListEntries = true;
            boolean gotOne = qdlValues.length == 1; // default case is allow for single argument
            if(qdlValues.length == 2) {
                if (qdlValues[1].isBoolean()) {
                    allowListEntries = qdlValues[1].asBoolean();
                    gotOne = true;
                }
                if (qdlValues[1].isLong()) {
                    indentfactor = qdlValues[1].asLong().intValue();
                    gotOne = true;
                }
            }
            if(qdlValues.length == 3){
                if (qdlValues[2].isBoolean()) {
                    allowListEntries = qdlValues[1].asBoolean();
                    gotOne = true;
                }
                if (qdlValues[1].isLong()) {
                    indentfactor = qdlValues[1].asLong().intValue();
                    gotOne = true;
                }

            }
            if(!gotOne){
                throw new BadArgException(INI_OUT + " requires an integer or boolean as its additional arguments if present", qdlValues.length-1);
            }

            String out =  IOEvaluator.convertToIni(qdlValues[0].asStem(), indentfactor, allowListEntries);
            return asQDLValue(out);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount){
               case 1:
                   dd.add(getName() + "(stem.) - convert a stem to ini file format");
                break;
                case 2:
                    dd.add(getName() + "(stem., indent) - convert a stem to ini file format and indent subsections by indent.");
                    break;
            }
            dd.add("E.g.");
            dd.add("If you had the following ini file\n");
            dd.add("[a]\n" +
                    "p:='q'\n" +
                    "[a.b]\n" +
                    "r:='s'\n" +
                    "t:='v'\n" +
                    "[z]\n" +
                    "m:=123\n");
            dd.add("\nin the file /tmp/eg.ini then you could issue");
            dd.add("ini.:=ini_in(file_read('/tmp/eg.ini'))");
            dd.add("  print(ini.)\n" +
                    "   a : {b:{r:s, t:v}, p:q}\n" +
                    "   z : {m: 123}");
            dd.add("and ");
            dd.add("  ini_out(ini.)\n" +
                    "[a]\n" +
                    "p := 'q'\n" +
                    " [a.b]\n" +
                    " r := 's'\n" +
                    " t := 'v'\n" +
                    "\n" +
                    "[z]\n" +
                    "m := 123");
            return dd;
        }
    }
    public class Sample implements QDLVariable{
        @Override
        public String getName() {
            return "sample.";
        }

        @Override
        public Object getValue() {
            /*
            Standard public basic from https://json.org/example.html
             */
            String raw = "{\n" +
                    "    \"glossary\": {\n" +
                    "        \"title\": \"basic glossary\",\n" +
                    "\t\t\"GlossDiv\": {\n" +
                    "            \"title\": \"S\",\n" +
                    "\t\t\t\"GlossList\": {\n" +
                    "                \"GlossEntry\": {\n" +
                    "                    \"ID\": \"SGML\",\n" +
                    "\t\t\t\t\t\"SortAs\": \"SGML\",\n" +
                    "\t\t\t\t\t\"GlossTerm\": \"Standard Generalized Markup Language\",\n" +
                    "\t\t\t\t\t\"Acronym\": \"SGML\",\n" +
                    "\t\t\t\t\t\"Abbrev\": \"ISO 8879:1986\",\n" +
                    "\t\t\t\t\t\"GlossDef\": {\n" +
                    "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n" +
                    "\t\t\t\t\t\t\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" +
                    "                    },\n" +
                    "\t\t\t\t\t\"GlossSee\": \"markup\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";
            QDLStem stem = new QDLStem();
            JSONObject json = JSONObject.fromObject(raw);
            stem.fromJSON(json);
            return stem;
        }
    }
}
/*
         module_load(info().lib.xml, 'java') =: q; module_import(q);
    d. := snarf(import({'file':'/home/ncsa/dev/csd/config/client-oa2.xml'}))
   // grab the client with a given property
      mask(d.config.client, 'sci_auth.client' == d\config\client\*\'@name')
    // list a couple of names
     d\config\client\[51,58]\'@name'
      c. := snarf(import({'file':'/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/xml/client.xml', 'level':0}))
        c\properties\entry\*\'@key'; //keys

       c\properties\entry\*\0; //values
          qq. := snarf(import({'file':'/home/ncsa/dev/csd/config/qdl-cfg.xml'}))

h. := import({'file':'/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/func_help.xml'})
  import({'file':'/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/xml/books.xml'}) =: b.

import('<a p="x"><b q="y">Z</b></a>')
import('<a p="x"><b q="y">Z</b><c>A</c><b>Y</b></a>')

                    StringReader stringReader = new StringReader(tempcfg);
                    Config conf = ConfigFactory.parseReader(stringReader);
                    String rawJSON = conf.root().render(ConfigRenderOptions.concise());
                    cfg = JSONObject.fromObject(rawJSON);
                    otherV.setRawConfig(tempcfg);

 */
