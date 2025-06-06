package org.qdl_lang.xml;

import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;
import org.qdl_lang.state.StateUtils;
import org.qdl_lang.variables.QDLStem;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import org.qdl_lang.variables.values.QDLKey;

import javax.xml.stream.*;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.math.BigDecimal;

import static org.qdl_lang.variables.values.QDLKey.from;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;
import static org.qdl_lang.xml.XMLUtils.*;

/**
 * A class for testing XML snippets. This has nothing useful in it per se, but does have
 * a lot of examples that should be kept.
 * <p>Created by Jeff Gaynor<br>
 * on 1/3/21 at  7:01 AM
 */
public class XMLTest {
    public static void main(String[] args) {
        try {
            testWriter();
//            testReader();
            testEvent();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void testWriter() throws Throwable {
        StringWriter sw = new StringWriter();
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xsw = xof.createXMLStreamWriter(sw);
        xsw.writeStartDocument("UTF-8", "1.0");
        xsw.writeStartElement(WORKSPACE_TAG);
        testConstants(xsw);
        //testSymbols(xsw);
        //testFunctionState(xsw);
        //  testModuleState(xsw);
        xsw.writeEndElement();
        xsw.writeEndDocument();
        System.out.print(prettyPrint(sw.toString()));
    }

    public static void testEvent() throws Throwable {
        File file = new File(DebugUtil.getDevPath()+"/qdl/language/src/main/resources/ws-test.xml");
        FileReader fileReader = new FileReader(file);

        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        System.out.println("FACTORY: " + xmlif);
        XMLEventReader r = xmlif.createXMLEventReader(fileReader);

        while (r.hasNext()) {
            XMLEvent xe = r.nextEvent();
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    String loc = "@[" + xe.getLocation().getLineNumber() + "," + xe.getLocation().getColumnNumber() + "]";
                    System.out.println("start element:" + xe.asStartElement().getName() + loc);
                    break;
                case XMLEvent.END_ELEMENT:
                    System.out.println("   end element:" + xe.asEndElement().getName());
                    break;
                case XMLEvent.CHARACTERS:
                case XMLEvent.CDATA:
                case XMLEvent.SPACE:
                case XMLEvent.ENTITY_REFERENCE:
                    Characters cc = xe.asCharacters();
                    if (!cc.isWhiteSpace() && !cc.isIgnorableWhiteSpace()) {
                        System.out.println("text:" + xe.asCharacters());
                    }
                    break;
                case XMLEvent.COMMENT:
                    System.out.println(" comment:" +xe);
                    break;
                case XMLEvent.ATTRIBUTE:
                    System.out.println("attributes # = " + xe);
                    break;
                default:
                    System.out.println("other:" + xe);
            }

        }
    }

    /**
     * Needed for the testing cursor to Event, the allocator will return the XMLevents
     *
     * @throws FileNotFoundException
     * @throws XMLStreamException
     */
    private static XMLEvent getXMLEvent(XMLStreamReader reader)
            throws XMLStreamException {
        return allocator.allocate(reader);
    }

    static XMLEventAllocator allocator = null;

    protected static void addLine(StringBuffer sb, String text) {
        sb.append(text + "\n");
    }


    private static void testConstants(XMLStreamWriter xsw) throws XMLStreamException {
        write(xsw, 42L);
        write(xsw, new BigDecimal("123.456"));
        write(xsw, Boolean.TRUE);
        QDLStem stem = new QDLStem();
        QDLStem stem1 = new QDLStem();
        stem.listAdd(asQDLValue("a"));
        stem.listAdd(asQDLValue("b"));
        stem1.listAdd(asQDLValue("p"));
        stem.put(from(2L), stem1);
        stem.put(from(42L), "c");
        stem.put(from("foo"), "bar");
        write(xsw, stem);
    }

    protected static void testModuleState(XMLStreamWriter xsw) throws Throwable {
        State state = StateUtils.newInstance();
        StringBuffer script = new StringBuffer();

        addLine(script, "module['a:/a','a']body[q:=1;];");
        addLine(script, "module_import('a:/a');");
        addLine(script, "module_import('a:/a','b');");
        addLine(script, "module['q:/q','w']body[module_import('a:/a');zz:=a#q+2;];");
        addLine(script, "a#q:=10;");
        addLine(script, "b#q:=11;");
        // Make sure that some of the state has changed to detect state management issues.
        addLine(script, "module_import('q:/q');");
        addLine(script, "w#a#q:=3;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        state.toXML(xsw);
    }
    public static void testReader() throws FileNotFoundException, XMLStreamException {
        File file = new File(DebugUtil.getDevPath()+"/qdl/language/src/main/resources/xml-test1.xml");
        FileReader fileReader = new FileReader(file);
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xsr = xif.createXMLStreamReader(fileReader);
        while (xsr.hasNext()) {
            xsr.next();
            switch (xsr.getEventType()) {
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.COMMENT:
                case XMLStreamReader.CDATA:
                case XMLStreamReader.SPACE:
                case XMLStreamReader.ENTITY_REFERENCE:
                    System.out.println(xsr.getText());
                    break;
                case XMLStreamReader.START_ELEMENT:
                    String name = xsr.getLocalName();
                    System.out.println("local name = " + name);

                    if (name.equals(SerializationConstants.STEM_ENTRY_TAG)) {
                        System.out.println(makeStem(xsr));
                    }
                    break;
                case XMLStreamConstants.ATTRIBUTE:
                    System.out.println("attributes # = " + xsr.getAttributeCount());
                    break;
            }
        }
        xsr.close();
    }
    protected static void testFunctionState(XMLStreamWriter xsw) throws Throwable {
        State state = StateUtils.newInstance();
        StringBuffer script = new StringBuffer();
        addLine(script, "define[\n");
        addLine(script, "f(x,y)\n");
        addLine(script, "]body[\n");
        addLine(script, ">> comment\n");
        addLine(script, "v := (x^3 + x*y^2 - 3*x*y + 1)/(x^4 + y^2 +2);\n");
        addLine(script, "return(v);\n");
        addLine(script, "];");

        addLine(script, "define[\n");
        addLine(script, "   f(x)\n");
        addLine(script, "][\n");
        addLine(script, ">> comment\n");
        addLine(script, "   v := (x^2 + x + 1)/(x^4 + x^2 +2);\n");
        addLine(script, "   return(v);\n");
        addLine(script, "];");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        interpreter.execute("k := 12.34^5;");
        interpreter.execute("s. := [1,2]~{'a':'b'};");
        state.toXML(xsw);
    }

}
