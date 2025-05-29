package org.qdl_lang.state;

import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.parsing.QDLRunner;
import edu.uiuc.ncsa.security.core.util.Iso8601;
import org.qdl_lang.statements.Statement;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.workspace.SIInterrupts;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Serializable;
import java.util.Date;

/**
 * An entry in the SI (state indicator) table.
 * <p>Created by Jeff Gaynor<br>
 * on 10/25/20 at  2:35 PM
 */
public class SIEntry implements Serializable {
    public boolean initialized = false;
    public int pid = -1;
    public Date timestamp = new Date();
    public State state = null;
    public String message = null;
    public Statement statement;
    public QDLRunner qdlRunner;  // needed to restart where the interpreter left off
    public int statementNumber = -1;
    public QDLInterpreter interpreter = null; // Can't be set at the point of the interrupt.

    /**
     * For the cases where this is the existing SIEntry and new one has been created
     * by the interrupt handler. Update with that state
     *
     * @param entry
     */

    public void update(SIEntry entry) {
        state = entry.state;
        message = entry.message;
        statementNumber = entry.statementNumber;
        if (entry.label != null) {
            label = entry.label;
        }
    }

    public void toXML(XMLStreamWriter xsw) throws XMLStreamException {
        xsw.writeStartElement("si_entry");
        xsw.writeAttribute("pid", Integer.toString(pid));
        xsw.writeAttribute("timestamp", Iso8601.date2String(timestamp));
        xsw.writeAttribute("statement_number", Integer.toString(statementNumber));
        xsw.writeAttribute("initialized", Boolean.toString(initialized));
        xsw.writeStartElement("message");
        xsw.writeCData(message);
        xsw.writeEndElement();// end message tag
        xsw.writeEndElement(); // end si entry tag
    }

    public QDLValue getLabel() {
        return label;
    }

    public void setLabel(QDLValue label) {
        this.label = label;
    }

    public QDLValue label;

    public boolean hasLabel() {
        return label != null;
    }

    public Long getIntLabel() {
        return label.asLong();
    }

    public String getStringLabel() {
        return label.asString();
    }

    public int getLabelType() {
        return label.getType();
    }

    public SIInterrupts getInterrupts() {
        if (interrupts == null) {
            interrupts = new SIInterrupts();
        }
        return interrupts;
    }

    public void setInterrupts(SIInterrupts interrupts) {
        this.interrupts = interrupts;
    }

    SIInterrupts interrupts;
}
