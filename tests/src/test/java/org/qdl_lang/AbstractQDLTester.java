package org.qdl_lang;

import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XKey;
import org.qdl_lang.variables.MetaCodec;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.VThing;
import org.qdl_lang.workspace.WorkspaceCommands;
import org.qdl_lang.xml.XMLUtils;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import javax.xml.stream.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  1:15 PM
 */
public class AbstractQDLTester extends TestBase {

    public static boolean isSerializationTestsOff() {
        return serializationTestsOff;
    }

    public static void setSerializationTestsOff(boolean serializationTestsOff) {
        AbstractQDLTester.serializationTestsOff = serializationTestsOff;
    }

    public static boolean serializationTestsOff = false;

    protected TestUtils testUtils = TestUtils.newInstance();

    /*
    Convenience getters for testing
     */
    protected BigDecimal getBDValue(String variable, State state) {
        return state.getValue(variable).asDecimal();
    }

    protected Long getLongValue(String variable, State state) {
        return state.getValue(variable).asLong();
    }

    protected String getStringValue(String variable, State state) {
        return state.getValue(variable).asString();
    }

    protected Boolean getBooleanValue(String variable, State state) {
        return state.getValue(variable).asBoolean();
    }

    protected QDLStem getStemValue(String variable, State state) {
        return  state.getValue(variable).asStem();
    }

    protected BigDecimal comparisonTolerance = new BigDecimal(".000000000001");

    /**
     * Compares two {@link BigDecimal}s. Tells if their difference is less than the
     * comparison tolerance. This effectively means they are equal.
     *
     * @param x
     * @param y
     * @param comparisonTolerance
     * @return
     */
    protected boolean areEqual(BigDecimal x, BigDecimal y, BigDecimal comparisonTolerance) {
        return x.subtract(y).abs().compareTo(comparisonTolerance) < 0;
    }

    /**
     * Tests that two {@link BigDecimal} numbers are with {@link #comparisonTolerance}
     * of each other.
     *
     * @param x
     * @param y
     * @return
     */
    protected boolean areEqual(BigDecimal x, BigDecimal y) {
        return areEqual(x, y, comparisonTolerance);
    }

    /**
     * Shallow check that two stems are equal. This is messy to write since Java and QDL
     * have very different typing systems.
     *
     * @param stem1
     * @param stem2
     * @return
     */
    protected boolean areEqual(QDLStem stem1, QDLStem stem2) {
        if (stem1.size() != stem2.size()) return false;
        for (Object key1 : stem1.keySet()) {
            Object v1 = null;
            Object v2 = null;
            if (key1 instanceof Long) {
                Long k1 = (Long) key1;
                if (!stem2.containsKey(k1)) return false;
                v1 = stem1.get(k1);
                v2 = stem2.get(k1);
            } else {
                if (!stem2.containsKey(key1)) return false;
                v1 = stem1.get(key1);
                v2 = stem2.get(key1);
            }
            if (v1 == null) {
                if (v2 != null) return false;
            } else {
                if (v2 == null) {
                    return false;
                } else {
                    if (!v1.equals(v2)) return false;
                }
            }
        }
        return true;
    }

    /**
     * For use in conjunction with {@link #areEqual(QDLStem, QDLStem)}.
     * <br/>
     * Note: These will be exact decimals and later no comparison tolerance is used.
     *
     * @param array
     * @return
     */
    protected QDLStem arrayToStem(double[] array) {
        ArrayList<Object> arrayList = new ArrayList<>();
        for (double dd : array) {
            // Have to convert to a string then back since otherwise the BigD adds rounding cruft
            // and these cannot be check for equality
            arrayList.add(new BigDecimal(Double.toString(dd)));
        }
        QDLStem stemVariable = new QDLStem();
        stemVariable.addList(arrayList);
        return stemVariable;
    }

    protected QDLStem arrayToStem(long[] array) {
        ArrayList<Object> arrayList = new ArrayList<>();
        for (long dd : array) {
            arrayList.add(dd);
        }
        QDLStem stemVariable = new QDLStem();
        stemVariable.addList(arrayList);
        return stemVariable;

    }

    protected QDLStem arrayToStem(int[] array) {
        ArrayList<Object> arrayList = new ArrayList<>();
        for (int dd : array) {
            arrayList.add(new Long(dd));
        }
        QDLStem stemVariable = new QDLStem();
        stemVariable.addList(arrayList);
        return stemVariable;

    }

    // get an encoded random string
    protected String geter() {
        return enc(getRandomString());
    }

    MetaCodec codec = new MetaCodec();

    protected String enc(String x) {
        return codec.encode(x);
    }

    protected String dec(String x) {
        return codec.decode(x);
    }

    /**
     * Simple utility that appends the line and a return. Use this to make scripts for testing the parser itself.
     *
     * @param stringBuffer
     * @param x
     * @return
     */
    protected StringBuffer addLine(StringBuffer stringBuffer, String x) {
        return stringBuffer.append(x + "\n");
    }

    /*
    This is actually just a copy of the test base class in security-util, but it cuts out
    a dependency.
     */
    public static int randomStringLength = 8; // default length for random strings
    public int count = 5; // on tests with loops, this sets max reps.

    public static Random getRandom() {
        if (random == null) {
            random = new Random();
        }
        return random;
    }

    static Random random;

    public static String getRandomString(int length) {
        // so approximate how long the result will be and add in (at most) 2 characters.
        byte[] bytes = new byte[(int) (Math.round(Math.ceil(length * .75)) + 1)];
        getRandom().nextBytes(bytes);
        // Have to be careful to use only URL safe encoding or random errors can start occurring,
        // especially if using these to make other urls!
        return Base64.encodeBase64URLSafeString(bytes).substring(0, length);
    }

    public static String getRandomString() {
        return getRandomString(randomStringLength);
    }


    protected boolean testNumberEquals(Object arg1, Object arg2) {
        if ((arg1 instanceof Long) && (arg2 instanceof Long)) {
            return arg1.equals(arg2);
        }
        BigDecimal left;
        BigDecimal right;
        if (arg1 instanceof Long) {
            left = new BigDecimal((Long) arg1);
        } else {
            left = (BigDecimal) arg1;
        }
        if (arg2 instanceof Long) {
            right = new BigDecimal((Long) arg2);
        } else {
            right = (BigDecimal) arg2;
        }

        BigDecimal result = left.subtract(right);
        return result.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Tests that the variable has the given value in the {@link org.qdl_lang.variables.VStack}
     *
     * @param variableName
     * @param newValue
     * @param state
     * @return
     */
    protected boolean checkVThing(String variableName, Object newValue, State state) {
        return ((VThing) state.getVStack().get(new XKey(variableName))).getVariable().getQDLValue().equals(newValue);
    }

    /**
     * Takes the current state, serializes, deserializes it then returns a new workspace
     *
     * @param state
     * @return
     * @throws Throwable
     */
    protected State pickleXMLState(State state) throws Throwable {
        // Serialize the workspace
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter xsw = createXSW(stringWriter);

        WorkspaceCommands workspaceCommands = WorkspaceCommands.getInstance().newInstance();
        workspaceCommands.setState(state);
        workspaceCommands.toXML(xsw);

        // Deserialize the workspace
        // Need pretty print. This takes the place or writing it to a file, then reading it.
        String pp = XMLUtils.prettyPrint(stringWriter.toString());
        //  System.out.println("XML:\n" + pp);
        StringReader reader = new StringReader(pp);
        XMLEventReader xer = createXER(reader);
        workspaceCommands.fromXML(xer, false);
        return workspaceCommands.getInterpreter().getState();
    }

    protected State pickleJSONState(State state) throws Throwable {
        // Serialize the workspace
        WorkspaceCommands workspaceCommands = WorkspaceCommands.getInstance().newInstance();
        workspaceCommands.setState(state);
        JSONObject json = workspaceCommands.toJSON();
        // Deserialize the workspace
        State newState = workspaceCommands.fromJSON(json).getState();
        return newState;
    }

    protected State pickleJavaState(State state) throws Throwable {
        // Serialize the workspace
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WorkspaceCommands workspaceCommands = WorkspaceCommands.getInstance().newInstance();
        workspaceCommands.setState(state);
        workspaceCommands._xmlWSJavaSave(baos);

        // Deserialize the workspace
        // Need pretty print. This takes the place or writing it to a file, then reading it.
        //  System.out.println("XML:\n" + pp);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        workspaceCommands._xmlWSJavaLoad(bais);
        return workspaceCommands.getInterpreter().getState();
    }

    /**
     * Sort of a test for QDL dump. Note that a dump is <b>not</b> a full save
     * of a workspace. This test is for simple cases mostly for regression
     * in common use cases (simple workspace, pickle it, reload later).
     *
     * @param state
     * @return
     * @throws Throwable
     */
    protected State pickleQDLState(State state) throws Throwable {
        // Serialize the workspace
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(baos);
        WorkspaceCommands workspaceCommands = WorkspaceCommands.getInstance().newInstance();
        workspaceCommands.setState(state);
        workspaceCommands._xmlWSQDLSave(osw);
       //System.out.println(new String(baos.toByteArray())); // Debugging aid. Dumps the whole thing to the console.

        // Deserialize the workspace
        // This takes the place or writing it to a file, then reading it.
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        InputStreamReader inputStreamReader = new InputStreamReader(bais);
        QDLInterpreter qdlInterpreter = new QDLInterpreter(null, state.newCleanState());
        workspaceCommands._xmlWSQDLLoad(qdlInterpreter, inputStreamReader);
        return qdlInterpreter.getState();
    }

    /**
     * One stop shopping for roundtripping serializing the state.
     * The script is interpreted and serialized then deserialized and the new state is
     * returned.
     * <h3>Usage</h3>
     * A typical use is to fork a test into a serialization test and nonserialization test
     * by passing in the doRoundtrip. Then before the tests you want to run inser
     * <pre>
     *     State = ...
     *     StringBuffer script; // <i>setup whatever state you need for your test</i>
     *     if(doRoundtrip){
     *         state = roundTripStateSerialization(state, script, true);
     *         script = new StringBuffer();
     *     }
     *     // ... <i>Put in any checks you would do on the previously created state</i>
     * </pre>
     * This replaces the state with its roundtripped version and restarts the script
     *
     * @param oldState
     * @param script
     * @return
     * @throws Throwable
     */
    protected State roundTripXMLSerialization(State oldState, StringBuffer script) throws Throwable {
        QDLInterpreter interpreter = new QDLInterpreter(null, oldState);
        interpreter.execute(script.toString());
        return pickleXMLState(oldState);
    }

    protected State roundTripQDLSerialization(State oldState, StringBuffer script) throws Throwable {
        QDLInterpreter interpreter = new QDLInterpreter(null, oldState);
        interpreter.execute(script.toString());
        return pickleQDLState(oldState);
    }

    protected State roundTripJavaSerialization(State oldState, StringBuffer script) throws Throwable {
        QDLInterpreter interpreter = new QDLInterpreter(null, oldState);
        interpreter.execute(script.toString());
        return pickleJavaState(oldState);
    }

    protected State roundTripJSONSerialization(State oldState, StringBuffer script) throws Throwable {
        QDLInterpreter interpreter = new QDLInterpreter(null, oldState);
        interpreter.execute(script.toString());
        return pickleJSONState(oldState);
    }

    public static final int ROUNDTRIP_NONE = 0;
    public static final int ROUNDTRIP_XML = 1;
    public static final int ROUNDTRIP_QDL = 2;
    public static final int ROUNDTRIP_JAVA = 3;
    public static final int ROUNDTRIP_JSON = 4;

    /**
     * delegates rountripping to various serializers. This will also zero out the
     * script so that the pending calls are not repeated (which would just stick
     * them back in the state if they were missing, giving a false positive.)
     * @param state
     * @param script
     * @param testCase
     * @return
     * @throws Throwable
     */
    public State rountripState(State state, StringBuffer script, int testCase) throws Throwable {
        switch (testCase) {
            case ROUNDTRIP_XML:
                // XML
                state = roundTripXMLSerialization(state, script);
                script.delete(0,script.length());
                break;
            case ROUNDTRIP_QDL:
                //QDL
                state = roundTripQDLSerialization(state, script);
                script.delete(0,script.length());
                break;
            case ROUNDTRIP_JAVA:
                //java
                state = roundTripJavaSerialization(state, script);
                script.delete(0,script.length());
                break;
            case ROUNDTRIP_JSON:
                state = roundTripJSONSerialization(state, script);
                script.delete(0,script.length());
               break;
            default:
            case ROUNDTRIP_NONE:
                // Do no serialization.
                // Do not reset the script since nothing has executed.
        }
        return state;
    }

    /**
     * Create the {@link XMLStreamWriter}
     *
     * @param w
     * @return
     * @throws XMLStreamException
     */
    protected XMLStreamWriter createXSW(Writer w) throws XMLStreamException {
        return XMLOutputFactory.newInstance().createXMLStreamWriter(w);
    }

    protected XMLEventReader createXER(Reader reader) throws XMLStreamException {
        return XMLInputFactory.newInstance().createXMLEventReader(reader);
    }
    /**
      * Gets a file from the distro source for testing. The environment variable NCSA_DEV_INPUT
      * <b>must</b> be set for this to work.
      * <h3>E.g.</h3>
      * getSourcePath("qdl/language/src/main/resources/modules/math-x.mdl") <br/><br/>
      * returns <br/><br/>
      * /home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/modules/math-x.mdl
      * <br/><br/>
      * on my system.
      *
      * @param path
      * @return
      */
     protected String getSourcePath(String path) {
         String devRoot = System.getenv("NCSA_DEV_INPUT");
         if (devRoot == null) {
             throw new IllegalStateException("NCSA_DEV_INPUT variable not set, cannot run test");
         }
         devRoot = devRoot.endsWith("/") ? devRoot.substring(0, devRoot.length() - 1) : devRoot;
         path = path.startsWith("/") ? path.substring(1) : path;
         return devRoot + "/" + path;
     }
}
