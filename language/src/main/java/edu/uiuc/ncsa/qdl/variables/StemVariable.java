package edu.uiuc.ncsa.qdl.variables;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  12:09 PM
 * @deprecated use {@link QDLStem}
 */
public class StemVariable extends QDLStem {

    public StemVariable() {
        super();
    }

    public StemVariable(Long count, Object[] fillList) {
        super(count, fillList);
    }

    @Override
    public QDLStem newInstance() {
        return new StemVariable();
    }

    @Override
    public QDLStem newInstance(Long count, Object[] fillList) {
        return new StemVariable(count, fillList);
    }

}
