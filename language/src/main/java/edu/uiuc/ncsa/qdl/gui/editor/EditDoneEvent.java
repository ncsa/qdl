package edu.uiuc.ncsa.qdl.gui.editor;

import java.util.UUID;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/10/22 at  2:42 PM
 */
public class EditDoneEvent {
    public static final int TYPE_VARIABLE = 0;
    public static final int TYPE_FUNCTION = 1;
    public static final int TYPE_FILE = 2;
    public static final int TYPE_BUFFER = 3;
    String localName;
    int argCount = 0;

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public int getArgCount() {
        return argCount;
    }

    public void setArgCount(int argCount) {
        this.argCount = argCount;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    int type;

    public EditDoneEvent(UUID id, String content) {
        this.id = id;
        this.content = content;
    }

  public  UUID id;
  public  String content;

}
