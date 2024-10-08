package org.qdl_lang.vfs;

import org.qdl_lang.variables.QDLStem;
import edu.uiuc.ncsa.security.core.configuration.XProperties;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/19/20 at  5:58 AM
 */
public interface VFSEntry extends VFSMetaEntry {
    String SHEBANG_REGEX = "^#!.*";
    public String getText(String regexFilter);
    public String getText();
    public List<String> getLines();
    public XProperties getProperties();
    public void setProperties(XProperties xp);
    public void setProperty(String key, Object value);
    public Object getProperty(String key);
    byte[] getContents();
    String getType();
    boolean isBinaryType();
    byte[] getBytes();
    QDLStem convertToStem();
    String getPath();
    void setPath(String newPath);

    /**
     * Special case for internal use. The file is processed and a reader is created
     * for the content. This is used internally, e.g. the for_lines iterator.
     * @return
     */
    InputStream getInputStream();
}
