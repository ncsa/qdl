package org.qdl_lang.exceptions;

import org.qdl_lang.statements.Statement;
import org.qdl_lang.variables.QDLStem;

import java.util.ArrayList;
import java.util.List;

/**
 * This is used when recursing a stem with E.g. {@link org.qdl_lang.util.ProcessScalar} in
 * {@link org.qdl_lang.util.QDLAggregateUtil}. Throw this exception when there is a bad
 * argument and add the current key to the list of indices.
 *
 * <h3>Usage</h3>
 * Just throw this with no key. The {@link org.qdl_lang.util.QDLAggregateUtil} will fill in any
 * keys at runtime. If there are keys (possible that only set operations are called)
 * then stem index will be added to any message you set, along with the parsing location
 * of the error. A typical error message reads
 * <pre>
 *     integer value not allowed [stem index: b.0.3.d] at (1, 7)
 * </pre>
 */
public class BadStemValueException extends QDLExceptionWithTrace {
    public BadStemValueException(Statement statement) {
        super(statement);
    }

    public BadStemValueException(Throwable cause, Statement statement) {
        super(cause, statement);
    }

    public BadStemValueException(String message, Statement statement) {
        super(message, statement);
    }

    public BadStemValueException(String message, Object index) {
        super(message, null);
        if (index != null) {
            getIndices().add(index);
        }
    }

    public BadStemValueException(String message) {
        super(message, null);
    }

    public BadStemValueException(String message, Throwable cause, Statement statement) {
        super(message, cause, statement);
    }

    public List<Object> getIndices() {
        return indices;
    }

    public void setIndices(List<Object> indices) {
        this.indices = indices;
    }

    List<Object> indices = new ArrayList<>(10);

    /**
     * Reverses the order of the elements in {@link #getIndices()} and returns a formatted string that is the
     * index the user would add. So if the indices are [0,"foo",3,"bar"] this returns the string
     * <pre>
     *     'bar.3.foo.0'
     * </pre>
     * which is what the user would use to access the element
     *
     * @return
     */
    public String toIndexString() {
        StringBuilder builder = new StringBuilder();
        int size = getIndices().size();
        String indexMarker = "";
        for (int i = 0; i < size; i++) {
            Object index = getIndices().get(size - i - 1);
            builder.append(indexMarker + index);
            if (i == 0) {
                indexMarker = QDLStem.STEM_INDEX_MARKER;
            }
        }
        return builder.toString();
    }

    /**
     * Automatically appends the index to the message.
     *
     * @return
     */
    @Override
    public String getMessage() {
        if(getIndices().size() == 0){
            return super.getMessage();
        }
        return super.getMessage() + " [stem index: " + toIndexString() + "]";
    }
}
