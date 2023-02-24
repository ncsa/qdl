package edu.uiuc.ncsa.qdl.extensions.xml;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import edu.uiuc.ncsa.qdl.evaluate.StemEvaluator;
import edu.uiuc.ncsa.qdl.exceptions.QDLException;
import edu.uiuc.ncsa.qdl.expressions.ConstantNode;
import edu.uiuc.ncsa.qdl.expressions.Polyad;
import edu.uiuc.ncsa.qdl.extensions.QDLFunction;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.util.QDLFileUtil;
import edu.uiuc.ncsa.qdl.variables.*;
import edu.uiuc.ncsa.qdl.xml.XMLUtils;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.FileUtil;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.yaml.snakeyaml.Yaml;

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

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/13/23 at  7:33 AM
 */
public class QDLConvert {
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
    public static final String PROCESSING_INSTRUCTION_KEY = DOC_CAPUT + "processing_instruction";
    public static final String COMMENT_KEY = DOC_CAPUT + "comment";
    public static final String ENTITY_KEY = DOC_CAPUT + "entity";
     /*
     Node types from the Node class:
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
        public Object evaluate(Object[] objects, State state) {
            try {
                return newEvaluate(objects, state);
            } catch (Throwable xmlStreamException) {
                xmlStreamException.printStackTrace();
            }
            // return oldEvaluate(object, state);
            return null;
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

        protected Object newEvaluate(Object[] objects, State state) throws XMLStreamException {
            XMLEventReader xer = null;
            boolean ignoreWhitespace = true;
            boolean validate = true;
            int level = XML_IMPORT_LEVEL_EXACT;
            boolean ignoreComments = XML_IMPORT_LEVEL_COMMENTS <= level;
            String raw = null;
            QDLStem inStem = null;
            Object configObject = null;
            if (objects.length == 1) {
                if (objects[0] instanceof QDLStem) {
                    configObject = objects[0];
                }
                if (objects[0] instanceof String) {
                    configObject = new QDLStem(); // make it empty.
                }
            } else {
                if (!(objects[0] instanceof String)) {
                    throw new IllegalArgumentException(getName() + " requires the first argument is a string");
                }
                raw = objects[0].toString();
                configObject = objects[1];
            }
            if (!(configObject instanceof QDLStem)) {
                throw new IllegalArgumentException(getName() + " requires that that the configuration argument is  a stem");
            }
            inStem = (QDLStem) configObject;
            String inString = getFileArg(objects[0], state, getName());
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
                throw new QDLException("Error! no XML found to deserialize");
            }

            XMLEvent xe = xer.nextEvent();
            if (!xe.isStartDocument()) {
                throw new QDLException("Error! no XML start of document to deserialize");
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
                            cursorStack.peek().listAdd(event.asCharacters().toString());
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
                            previous.getStem(currentTag).listAdd(targetStem);
                        } else {
                            QDLStem x = new QDLStem();
                            previous.put(currentTag, x);
                            x.listAdd(targetStem);

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
        public Object evaluate(Object[] objects, State state) {
            if (!(objects[0] instanceof QDLStem)) {
                throw new IllegalArgumentException(getName() + " requires a stem as its argument");
            }
            QDLStem arg = (QDLStem) objects[0];
            /*if (arg.size() != 1) {
                throw new QDLException(getName() + " the stem is an invalid XML document. It must have a single root element.");
            }*/
            boolean exportToFile = objects.length == 2;
            String fileName = null;
            if (exportToFile) {
                if (!(objects[1] instanceof String)) {
                    throw new QDLException(getName() + " requires a string as its second argument if present.");
                }
                fileName = (String) objects[1];
            }
            Writer w = new StringWriter();

            XMLOutputFactory xof = XMLOutputFactory.newInstance();

            try {
                XMLStreamWriter xsw = xof.createXMLStreamWriter(w);
                if(arg.containsKey(DECLARATION_KEY)){
                    QDLStem declarations = arg.getStem(DECLARATION_KEY);
                    String version = declarations.containsKey("@version")?declarations.getString("@version"):"1.0";
                    String charSet = declarations.containsKey("@encoding")?declarations.getString("@encoding"):"UTF-8";
                    xsw.writeStartDocument(charSet,version);
                }else {
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
                throw new QDLException(getName() + " could not serialize stem to XML:" + e.getMessage());
            }
            if (exportToFile) {
                try {
                    QDLFileUtil.writeTextFile(state, fileName, w.toString());
                } catch (Throwable e) {
                    if (DebugUtil.isEnabled()) {
                        e.printStackTrace();
                    }
                    throw new QDLException(getName() + " could not write file '" + fileName + "':" + e.getMessage());
                }
                return Boolean.TRUE;
            }
            return w.toString();
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
                boolean isCDATA = qdlMap.containsKey(CDATA_KEY) && (Boolean) qdlMap.get(CDATA_KEY);
                // Write attributes immediately after tag or you get an exception.
                for (String key : qdlMap.keySet()) {
                    if (isProperty(key)) {
                        xsw.writeAttribute(key.substring(ATTRIBUTE_CAPUT.length()), qdlMap.get(key).toString());
                    }
                }
                for (Object key : qdlList.orderedKeys()) {
                    if (isCDATA) {
                        xsw.writeCData(qdlList.get((Long)key).toString());
                    } else {
                        xsw.writeCharacters(xmlEscape(qdlList.get((Long)key).toString()));
                    }
                }
                for (String key : qdlMap.keySet()) {
                    if (!isProperty(key)) {
                        Object value = qdlMap.get(key);
                        if(key.equals(COMMENT_KEY)){
                              xsw.writeComment(value.toString());
                              continue;
                        }

                        if(Constant.isStem(value)){
                            writeElement(xsw, (QDLStem) value, key);
                        }else{
                            if(Constant.isSet(value)){
                                xsw.writeStartElement(key);
                                xsw.writeCData(((QDLList)value).toJSON().toString());
                                xsw.writeEndElement();
                            }else{

                                xsw.writeStartElement(key);
                                if(isCDATA){
                                    xsw.writeCData(((QDLList)value).toJSON().toString());
                                } else{
                                    xsw.writeCharacters(xmlEscape(value.toString()));
                                }
                                xsw.writeEndElement();
                            }

                        }
                    }
                }

            } else {
                throw new QDLException("unknown content");
            }
        }

        protected void writeElement(XMLStreamWriter xsw, QDLStem stem, String tagname) throws XMLStreamException {
            if (stem.isList()) {
                // have to process each element
                QDLList qdlList = stem.getQDLList();
                for (Object v : qdlList.values()) {
                    xsw.writeStartElement(tagname);
                    if(Constant.isStem(v)){
                        writeContent(xsw, v);

                    }else{

                        writeContent(xsw, v);
                    }
                    xsw.writeEndElement();
                }
            }
/*
            for (Object key : stem.keySet()) {
                if (isProperty(key)) {
                    String k = (String) key;
                    xsw.writeAttribute(k.substring(ATTRIBUTE_CAPUT.length()), stem.getString(k));
                    continue;
                }


                if (value instanceof QDLStem) {
                    // This is just a stem
                    QDLStem qdlStem = (QDLStem) value;
                    xsw.writeStartElement(key.toString());
                    boolean writeCDATA = false;
                    if (qdlStem.containsKey(CDATA_KEY)) {
                        writeCDATA = qdlStem.getBoolean(CDATA_KEY);
                    }
                    for (Object text : qdlStem.getQDLList().values()) {

                    }
                    xsw.writeEndElement();
                }

            }
*/
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

    public static void main(String[] args) {
        QDLConvert QDLConvert = new QDLConvert();
        XMLImport xmlImport = QDLConvert.new XMLImport();
        // Test XML as configuration language
        //       xmlImport.evaluate(new Object[]{"/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/xml/simple0.xml"}, null);
        QDLStem cfg = new QDLStem();
        State state = new State();
        state.setServerMode(false);
        //cfg.put("file", "/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/xml/planes.xml");
        cfg.put("file", "/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/xml/simple0.xml");
        QDLStem stem = (QDLStem) xmlImport.evaluate(new Object[]{cfg}, state);
        XMLExport xmlExport = QDLConvert.new XMLExport();
        Object x = xmlExport.evaluate(new Object[]{stem}, state);
        System.out.println(x);
        HOCONExport hoconExport = QDLConvert.new HOCONExport();
//        System.out.println(hoconExport.evaluate(new Object[]{stem}, null));
        // Next are for testing XML as a text markup language
        //xmlImport.evaluate(new Object[]{"/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/func_help.xml"}, null);
        //xmlImport.evaluate(new Object[]{"/home/ncsa/dev/ncsa-git/oa4mp/oa4mp-website/src/site/xhtml/server/manuals/authorized.xhtml"}, null);

    }

    public static class MyStemStack<V extends QDLStem> extends Stack<V> {
        public QDLStem peekLast(String currentTag) {
            QDLStem qqq = (QDLStem) peek().getQDLList().last().entry;
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
        public Object evaluate(Object[] objects, State state) {
            if (!(objects[0] instanceof QDLStem)) {
                throw new IllegalArgumentException(getName() + " requires a stem as its first argument");
            }
            QDLStem stem = (QDLStem) objects[0];
            long axis = 0L;
            boolean hasAxis = objects.length == 2;
            if (hasAxis) {
                if (!(objects[1] instanceof Long)) {
                    throw new IllegalArgumentException(getName() + " require a long for its second argument");
                }
                axis = (Long) objects[1];
            }
            Polyad polyad = new Polyad(StemEvaluator.ALL_KEYS);
            polyad.addArgument(new ConstantNode(stem));
            if (hasAxis) {
                polyad.addArgument(new ConstantNode(axis));
            }
            polyad.evaluate(state);
            QDLStem z = (QDLStem) polyad.getResult();
            List values = z.getQDLList().values();
            List<String> out = new ArrayList<>();
            QDLStem result = new QDLStem();
            for (Object obj : values) {
                if (obj instanceof QDLStem) {
                    QDLStem zz = (QDLStem) obj;
                    ArrayList valueList = ((QDLStem) obj).getQDLList().values();
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
                        result.listAdd(aaa);
                    }
                }
            }
            return result;
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
        public Object evaluate(Object[] objects, State state) {
            QDLStem inStem = (QDLStem) objects[0];
            for (Object key : inStem.keySet()) {
                if (rejectKey(key)) {
                    inStem.remove(key.toString());
                }
            }
            return doSnarf(inStem);
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
        Object rootKey = stemKeys.iterator().next();
        QDLList content = inStem.getStem(rootKey.toString()).getQDLList();
        root.putLongOrString(rootKey, snarfList(content, true));
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
            out.listAdd(snarfNode(current, glomStringsTogether));
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
        public Object evaluate(Object[] objects, State state) {
            String inString = getFileArg(objects[0], state, getName());
            Yaml yaml = new Yaml();
            Object map = yaml.load(inString);
            QDLStem stem = null;
            if (map instanceof Map) {
                stem = StemUtility.mapToStem((Map) map);
            }
            if (map instanceof List) {
                stem = StemUtility.listToStem((List) map);
            }
            return stem;
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
        public Object evaluate(Object[] objects, State state) {
            if (!(objects[0] instanceof QDLStem)) {
                throw new IllegalArgumentException(getName() + " requires a stem as its first argument");
            }
            QDLStem stem = (QDLStem) objects[0];
            boolean exportToFile = false;
            String fileName = null;
            if (objects.length == 2) {
                if (!(objects[1] instanceof String)) {
                    throw new IllegalArgumentException(getName() + " requires a string as its second argument if present");
                }
                exportToFile = true;
                fileName = (String) objects[1];
            }
            Yaml yaml = new Yaml();
            JSON json = stem.toJSON();
            String out = yaml.dump(json);
            if (!exportToFile) {
                return out;
            }
            try {
                QDLFileUtil.writeTextFile(state, fileName, out);
            } catch (Throwable e) {
                if (DebugUtil.isEnabled()) {
                    e.printStackTrace();
                }
                throw new QDLException(getName() + " could not write file '" + fileName + "':" + e.getMessage());
            }
            return Boolean.TRUE;
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
        public Object evaluate(Object[] objects, State state) {
            String inString = getFileArg(objects[0], state, getName());
            StringReader stringReader = new StringReader(inString);
            Config conf = ConfigFactory.parseReader(stringReader);
            String rawJSON = conf.root().render(ConfigRenderOptions.concise());
            // String rawJSON = conf.root().render(ConfigRenderOptions.defaults().setJson(false).setComments(true).setOriginComments(true));
            QDLStem out = new QDLStem();
            out.fromJSON(JSONObject.fromObject(rawJSON));
            return out;
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
        public Object evaluate(Object[] objects, State state) {
            if (!(objects[0] instanceof QDLStem)) {
                throw new IllegalArgumentException(getName() + " requires a stem as its first argument");
            }
            QDLStem stem = (QDLStem) objects[0];
            boolean exportToFile = false;
            String fileName = null;
            if (objects.length == 2) {
                if (!(objects[1] instanceof String)) {
                    throw new IllegalArgumentException(getName() + " requires a string as its second argument if present");
                }
                exportToFile = true;
                fileName = (String) objects[1];
            }
            StringReader stringReader = new StringReader(stem.toJSON().toString());
            Config conf = ConfigFactory.parseReader(stringReader);
            //String rawJSON = conf.root().render(ConfigRenderOptions.concise());
            String rawJSON = conf.root().render(ConfigRenderOptions.defaults().setJson(false).setOriginComments(false).setFormatted(true));
            if (!exportToFile) {
                return rawJSON;
            }
            try {
                QDLFileUtil.writeTextFile(state, fileName, rawJSON);
            } catch (Throwable e) {
                if (DebugUtil.isEnabled()) {
                    e.printStackTrace();
                }
                throw new QDLException(getName() + " could not write file '" + fileName + "':" + e.getMessage());
            }
            return Boolean.TRUE;
        }

        List<String> doxx = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (doxx.isEmpty()) {
                doxx.add(getName() + "(arg.{,file_path} - convert the stem to HOCON. If file_path is present, write to the file.");
                doxx.add("Otherwise, return the result as a string.");
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
                    throw new QDLException(name + " could not read file:" + e.getMessage());
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
