package edu.uiuc.ncsa.qdl.extensions.xml;

import edu.uiuc.ncsa.qdl.evaluate.StemEvaluator;
import edu.uiuc.ncsa.qdl.exceptions.QDLException;
import edu.uiuc.ncsa.qdl.expressions.ConstantNode;
import edu.uiuc.ncsa.qdl.expressions.Polyad;
import edu.uiuc.ncsa.qdl.extensions.QDLFunction;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.qdl.xml.XMLUtils;
import edu.uiuc.ncsa.security.core.util.FileUtil;
import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/13/23 at  7:33 AM
 */
public class QDLXML {
    public static final String QDL_IMPORT_NAME = "from_xml";
    public static final int XML_IMPORT_LEVEL_FLATTEN = 0; // no @'s, ignore most structures, attributes flattened to properties
    public static final int XML_IMPORT_LEVEL_ATTR = 1; // @attributes, @text
    public static final int XML_IMPORT_LEVEL_COMMENTS = 2; // @attributes, @text, @comment
    public static final int XML_IMPORT_LEVEL_EXACT = 5; // All structures converted. Intended for roundtripping.

    // These are prefixed with @ signs since those are not valid XML tagnames, hence there can be
    // no collision ambiguity on import/export
    public static final String ATTRIBUTE_KEY = ">attributes";
    public static final String CDATA_KEY = ">cdata";
    public static final String TEXT_KEY = ">text";
    public static final String ATTRIBUTE_CAPUT = "@";
    public static final String DECLARATION_KEY = ">declaration";
    public static final String DTD_TYPE_KEY = ">doc_type";
    public static final String PROCESSING_INSTRUCTION_KEY = ">processing_instruction";
    public static final String COMMENT_KEY = ">comment";
    public static final String ENTITY_KEY = ">entity";
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

    public class XMLImport implements QDLFunction {
        @Override
        public String getName() {
            return QDL_IMPORT_NAME;
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
            boolean fileImport = true;
            String fileName = null;
            String raw = null;
            QDLStem inStem = null;
            Object configObject = null;
            if (objects.length == 1) {
                if(objects[0] instanceof QDLStem){
                    configObject = objects[0];
                }
                if(objects[0] instanceof String){
                    raw = (String)objects[0];
                    fileImport = false;
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
            if (inStem.containsKey(ARG_FILE)) {
                fileImport = true;
                fileName = inStem.getString(ARG_FILE);
            }
            if (inStem.containsKey(ARG_VALIDATE)) {
                validate = inStem.getBoolean(ARG_VALIDATE);
            }
            if (inStem.containsKey(ARG_IGNORE_WHITESPACE)) {
                ignoreWhitespace = inStem.getBoolean(ARG_IGNORE_WHITESPACE);
            }
            if (inStem.containsKey(ARG_IGNORE_COMMENTS)) {
                ignoreComments = inStem.getBoolean(ARG_IGNORE_COMMENTS);
            }
            if (fileImport) {
                xer = XMLUtils.getReader(new File(fileName));
            } else {
                StringReader reader = new StringReader(raw);
                xer = XMLUtils.getXMLEventReader(reader);
            }
            if (!xer.hasNext()) {
                throw new QDLException("Error! no XML found to deserialize");
            }

            XMLEvent xe = xer.nextEvent();
            if (!xe.isStartDocument()) {
                throw new QDLException("Error! no XML start of document to deserialize");
            }
            QDLStem out = new QDLStem();
            if (level == XML_IMPORT_LEVEL_EXACT) {
                out.put(DECLARATION_KEY, xe.toString()); // No good way to get the text of this...
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
                            cursorStack.peek().put(0L, event.asCharacters().toString());
                        }
                        break;
                    case XMLEvent.ATTRIBUTE: // Never seems to get called. See start element
                        break;
                    case XMLEvent.CDATA:
                        cursorStack.peek().put(CDATA_KEY, Boolean.TRUE);
                     /*   if (level <= XML_IMPORT_LEVEL_COMMENTS) {
                            cursorStack.peek().put(CDATA_KEY, event.asCharacters());
                        }*/
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
/*
                        if (previous.containsKey(currentTag)) {
                            cursorStack.push(previous.getStem(currentTag));
                        } else {
*/
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
            if(argCount == 1){
                doxx.add(getName() + "(cfg.) - parse an XML document using the parameters in cfg.");
            }else{
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

    public static void main(String[] args) {
        QDLXML qdlxml = new QDLXML();
        XMLImport xmlImport = qdlxml.new XMLImport();
        // Test XML as configuration language
        //       xmlImport.evaluate(new Object[]{"/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/xml/simple0.xml"}, null);
        xmlImport.evaluate(new Object[]{"/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/xml/planes.xml"}, null);
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
                        if (ooo instanceof String) {
                            if (((String) ooo).startsWith(ATTRIBUTE_CAPUT)) {
                                gotOne = true;
                                break;
                            }
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
}
