package org.qdl_lang.workspace;

import org.qdl_lang.state.State;
import org.qdl_lang.util.QDLFileUtil;
import org.qdl_lang.vfs.VFSPaths;
import org.qdl_lang.xml.XMLUtilsV2;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.qdl_lang.xml.SerializationConstants.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This manages buffers, i.e., things that may be edited and run.
 * <p>Created by Jeff Gaynor<br>
 * on 4/27/20 at  6:47 AM
 */
public class BufferManager implements Serializable {


    public static class BufferRecord implements Serializable {
        public String alias;
        public String src;
        public String link;
        public boolean edited = false;
        public boolean deleted = false;
        public List<String> content = null;
        public boolean memoryOnly = false;
        public String srcSavePath = null;
        public String linkSavePath = null;


        public List<String> getContent() {
            return content;
        }

        public void setContent(List<String> content) {
            this.content = content;
        }


        public boolean hasContent() {
            if (content == null) {
                return false;
            }
            return !content.isEmpty();
        }

        public boolean isLink() {
            return link != null;
        }

        public String toString() {
            String x = alias + ": ";
            if (memoryOnly) {
                x = x + src;
            } else {
                x = x + srcSavePath;
                if (isLink()) {
                    x = x + " --> " + linkSavePath;
                }
            }
            return x;
        }

        public JSONObject toJSON() {
            JSONObject bufferRecord = new JSONObject();
            if (!StringUtils.isTrivial(alias)) bufferRecord.put(BR_ALIAS, alias);
            if (!StringUtils.isTrivial(src)) bufferRecord.put(BR_SOURCE, src);
            if (!StringUtils.isTrivial(link)) bufferRecord.put(BR_LINK, link);
            bufferRecord.put(BR_EDITED, edited);
            bufferRecord.put(BR_DELETED, deleted);
            bufferRecord.put(BR_MEMORY_ONLY, memoryOnly);
            if (srcSavePath != null) bufferRecord.put(BR_SOURCE_SAVE_PATH, srcSavePath);
            if (linkSavePath != null) bufferRecord.put(BR_LINK_SAVE_PATH, linkSavePath);
            if (content != null && !content.isEmpty()) {
                JSONArray jsonArray = new JSONArray();
                jsonArray.addAll(content);
                bufferRecord.put(BR_CONTENT, Base64.encodeBase64URLSafeString(jsonArray.toString().getBytes(UTF_8)));
            }
            return bufferRecord;
        }

        public void fromJSON(JSONObject json) {
            if (json.containsKey(BR_ALIAS)) alias = json.getString(BR_ALIAS);
            if (json.containsKey(BR_SOURCE)) src = json.getString(BR_SOURCE);
            if (json.containsKey(BR_LINK)) link = json.getString(BR_LINK);
            if(json.containsKey(BR_EDITED)) edited = json.getBoolean(BR_EDITED);
            if(json.containsKey(BR_DELETED))  deleted = json.getBoolean(BR_DELETED);
            if(json.containsKey(BR_MEMORY_ONLY))  memoryOnly = json.getBoolean(BR_MEMORY_ONLY);
            if (json.containsKey(BR_SOURCE_SAVE_PATH)) srcSavePath = json.getString(BR_SOURCE_SAVE_PATH);
            if (json.containsKey(BR_LINK_SAVE_PATH)) linkSavePath = json.getString(BR_LINK_SAVE_PATH);
            if (json.containsKey(BR_CONTENT)) {
                content = JSONArray.fromObject(new String(Base64.decodeBase64(json.getString(BR_CONTENT)), UTF_8));
            }
        }

        public void toXML(XMLStreamWriter xsw) throws XMLStreamException {

            xsw.writeStartElement(BUFFER_RECORD);
            if (!StringUtils.isTrivial(alias)) {
                xsw.writeAttribute(BR_ALIAS, alias);
            }

            if (!StringUtils.isTrivial(src)) {
                xsw.writeAttribute(BR_SOURCE, src);
            }
            if (!StringUtils.isTrivial(link)) {
                xsw.writeAttribute(BR_LINK, link);
            }
            xsw.writeAttribute(BR_EDITED, Boolean.toString(edited));
            xsw.writeAttribute(BR_DELETED, Boolean.toString(deleted));
            xsw.writeAttribute(BR_MEMORY_ONLY, Boolean.toString(memoryOnly));
            if (srcSavePath != null) {
                xsw.writeAttribute(BR_SOURCE_SAVE_PATH, srcSavePath);
            }
            if (linkSavePath != null) {
                xsw.writeAttribute(BR_LINK_SAVE_PATH, linkSavePath);
            }
            if (content != null && !content.isEmpty()) {
                xsw.writeStartElement(BR_CONTENT);
                JSONArray jsonArray = new JSONArray();
                jsonArray.addAll(content);
                xsw.writeCData(Base64.encodeBase64URLSafeString(jsonArray.toString().getBytes(UTF_8)));
                xsw.writeEndElement(); //end content tag
            }
            xsw.writeEndElement(); //end BR tag
        }

        public void fromXML(XMLEventReader xer) throws XMLStreamException {
            XMLEvent xe = xer.nextEvent(); // position at start
            // get the attributes
            Iterator iterator = xe.asStartElement().getAttributes(); // Use iterator since it tracks state
            while (iterator.hasNext()) {
                Attribute a = (Attribute) iterator.next();
                String v = a.getValue();
                switch (a.getName().getLocalPart()) {
                    case BR_ALIAS:
                        alias = v;
                        break;
                    case BR_SOURCE:
                        src = v;
                        break;
                    case BR_SOURCE_SAVE_PATH:
                        srcSavePath = v;
                        break;
                    case BR_LINK:
                        link = v;
                        break;
                    case BR_LINK_SAVE_PATH:
                        linkSavePath = v;
                        break;
                    case BR_DELETED:
                        deleted = Boolean.parseBoolean(v);
                        break;
                    case BR_EDITED:
                        edited = Boolean.parseBoolean(v);
                        break;
                    case BR_MEMORY_ONLY:
                        memoryOnly = Boolean.parseBoolean(v);
                        break;
                }

            }
            while (xer.hasNext()) {
                xe = xer.peek();
                switch (xe.getEventType()) {
                    case XMLEvent.START_ELEMENT:
                        if (xe.asStartElement().getName().getLocalPart().equals(BR_CONTENT)) {
                            String raw = new String(Base64.decodeBase64(XMLUtilsV2.getText(xer, BR_CONTENT)));
                            content = JSONArray.fromObject(raw);
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (xe.asEndElement().getName().getLocalPart().equals(BUFFER_RECORD)) {
                            return;
                        }
                        break;
                }

                xer.next();
            }
            throw new IllegalStateException("Error: XML file corrupt. No end tag for " + BUFFER_RECORD);

        }
    }

    public ArrayList<BufferRecord> getBufferRecords() {
        return bufferRecords;
    }

    /*
    These have the same buffer records. The list lets us display them with indices
    the map allows for lookup by key.
     */
    ArrayList<BufferRecord> bufferRecords = new ArrayList<>();
    HashMap<String, BufferRecord> brMap = new HashMap<>();

    public State getState() {
        return state;
    }

    State state;

    /**
     * Get the record by index
     *
     * @param index
     * @return
     */
    public BufferRecord getBufferRecord(int index) {
        if (!hasBR(index)) {
            return null;
        }
        return bufferRecords.get(index);
    }

    public boolean anyEdited() {
        for (String key : brMap.keySet()) {
            if (getBufferRecord(key).edited) return true;
        }
        return false;
    }

    public BufferRecord getBufferRecord(String name) {
        if (!brMap.containsKey(name)) {
            return null;
        }
        return brMap.get(name);
    }

    public boolean hasBR(int index) {
        if (bufferRecords.isEmpty()) return false;
        if (index < 0 || bufferRecords.size() < index) return false;
        return true;
    }

    public boolean hasBR(String name) {
        if (bufferRecords.isEmpty()) return false;
        return brMap.containsKey(name);
    }

    public int create(String alias) {
        BufferRecord br = new BufferRecord();
        br.alias = alias;
        bufferRecords.add(br);
        brMap.put(alias, br);
        return bufferRecords.size() - 1;
    }

    public int link(String alias, String source, String target) {
        BufferRecord br = new BufferRecord();
        br.alias = alias;
        br.src = source;
        br.link = target;
        bufferRecords.add(br);
        brMap.put(alias, br);
        return bufferRecords.size() - 1;
    }

    protected int getIndex(BufferRecord br) {
        for (int i = 0; i < bufferRecords.size(); i++) {
            if (bufferRecords.get(i) == br) {
                return i;
            }
        }
        return -1;
    }

    public boolean remove(String name) {
        if (!hasBR(name)) {
            return false;
        }
        BufferRecord br = brMap.get(name);
        // We don't actually remove the record since we do not want indices to change.
        // If they recreate it later, they will get a different index. Best we can do...

        br.deleted = true;
        brMap.remove(br.src);
        br.src = null;
        br.link = null;
        return true;
    }


    protected List<String> readFile(String fName) throws Throwable {
        return QDLFileUtil.readTextFileAsLines(getState(), fName);
    }

    public List<String> read(int index, boolean useSource) throws Throwable {
        BufferRecord currentBR = getBufferRecord(index);
        if (currentBR == null) {
            return null;
        }

        String f = useSource ? currentBR.srcSavePath : currentBR.linkSavePath;
        return readFile(f);
    }

    /**
     * @param parent
     * @param name
     * @return
     */
    protected String figureOutSavePath(String parent, String name) {
        // Figure it out.
        if (name.contains(VFSPaths.SCHEME_DELIMITER)) {
            // so its in a VFS and its absolute
            return name;
        }
        if (parent.contains(VFSPaths.SCHEME_DELIMITER)) {
            return parent + name;
        }

        // it's a regular file
        File targetForSave = new File(name);
        if (!targetForSave.isAbsolute()) {
            // resolve it against the parent
            return (new File(parent, name)).getAbsolutePath();
        } else {
            return targetForSave.getAbsolutePath();
        }
    }

    public boolean write(BufferRecord currentBR) throws Throwable {
        //BufferRecord currentBR = getBufferRecord(index);
        if (currentBR == null) {
            return false;
        }


        if (currentBR.isLink()) {
            // save br.link to br.src
            try {
                QDLFileUtil.writeTextFile(getState(), QDLFileUtil.readTextFile(getState(), currentBR.linkSavePath), currentBR.srcSavePath);
            } catch (Throwable throwable) {
                getState().error("could not write file", throwable);
                return false;
            }
            return true;
        }

        if (currentBR.edited) {
            // Some save logic first

            QDLFileUtil.writeTextFile(getState(), currentBR.srcSavePath, currentBR.getContent());
            return true;
        }
        return false;
    }


    BufferRecord defaultBR = null;

    public BufferRecord defaultBR() {
        return defaultBR;
    }

    public BufferRecord defaultBR(BufferRecord br) {
        return defaultBR = br;
    }

    public void toXML(XMLStreamWriter xsw) throws XMLStreamException {
        xsw.writeStartElement(BUFFER_MANAGER);
        xsw.writeStartElement(BUFFER_RECORDS);
        // improvement might be to pass along the index and use that as part of the BR attributes. Someday?
        // This relies on the implicit ordering in and out to keep the indices right.
        for (BufferRecord br : bufferRecords) {
            br.toXML(xsw);
        }
        xsw.writeEndElement(); // records
        xsw.writeEndElement(); // buffer manager
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();
        for (BufferRecord br : bufferRecords) {
            array.add(br.toJSON());
        }
        jsonObject.put(BUFFER_RECORDS, array);
        return jsonObject;
    }

    public void fromJSON(JSONObject json) {
        if (json.containsKey(BUFFER_RECORDS)) {
            JSONArray array = json.getJSONArray(BUFFER_RECORDS);
            for (int i = 0; i < array.size(); i++) {
                BufferRecord br = new BufferRecord();
                br.fromJSON(array.getJSONObject(i));
                bufferRecords.add(br);
                brMap.put(br.src, br);
            }
        }
    }

    public void fromXML(XMLEventReader xer) throws XMLStreamException {
        XMLEvent xe = xer.nextEvent();
        while (xer.hasNext()) {
            xe = xer.peek();
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    if (xe.asStartElement().getName().getLocalPart().equals(BUFFER_RECORDS)) {
                        doBRecs(xer);
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(BUFFER_MANAGER)) {
                        return;
                    }
                    break;
            }
            xer.next();
        }
        throw new IllegalStateException("Error: XML file corrupt. No end tag for " + BUFFER_MANAGER);

    }

    protected void doBRecs(XMLEventReader xer) throws XMLStreamException {
        XMLEvent xe = xer.nextEvent();
        while (xer.hasNext()) {
            xe = xer.peek();
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    if (xe.asStartElement().getName().getLocalPart().equals(BUFFER_RECORD)) {
                        BufferRecord br = new BufferRecord();
                        br.fromXML(xer);
                        bufferRecords.add(br);
                        brMap.put(br.src, br);
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(BUFFER_RECORDS)) {
                        return;
                    }
                    break;
            }
            xer.next();
        }
        throw new IllegalStateException("Error: XML file corrupt. No end tag for " + BUFFER_RECORDS);

    }

    public boolean isEmpty() {
        return bufferRecords.isEmpty();
    }
}
