package org.qdl_lang.state;

import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import org.qdl_lang.evaluate.MetaEvaluator;
import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.exceptions.QDLException;
import org.qdl_lang.functions.FStack;
import org.qdl_lang.expressions.module.MIStack;
import org.qdl_lang.expressions.module.MTStack;
import org.qdl_lang.variables.VStack;
import org.qdl_lang.xml.SerializationConstants;
import org.qdl_lang.xml.SerializationState;
import org.qdl_lang.xml.XMLUtils;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/6/20 at  1:33 PM
 */
public abstract class StateUtils {
    /**
     * Take the current state and make a complete copy of it. Note this serialized then deserializes it.
     *
     * @param state
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static State clone(State state)  {
        SerializationState serializationState = new SerializationState();
        serializationState.setVersion(SerializationConstants.VERSION_2_1_TAG);
        JSONObject json = null;
        try {
            State newState;
            if(state == null){
                // in the case of, e.g., a new module that is being created, there is no state,
                // in that case, create one from the current factory.
                newState = State.getFactory().newInstance();
            }else{
                newState = state.newInstance();
                json = state.serializeToJSON(serializationState);
                newState.deserializeFromJSON(json, serializationState);
            }
            newState.setWorkspaceCommands(state.getWorkspaceCommands());
            return newState;
        } catch(net.sf.json.JSONException | StackOverflowError sox){
            // In this case, there the system will get overwhelmed with JSON messages
            // which are not searchable since we don't generate them, and it is hard
            // to see where this happened. It indicates
            // an actual internal issue with the implementation. Catch it here
            // so this can get tracked down.
            sox.printStackTrace();
            if(json != null){System.err.println(json.toString(2));}
           /* try {
                return javaClone(state);
            } catch (IOException  | ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }*/
              throw new NFWException("internal error serializing state.", sox);
        }catch (Throwable e) {
            if(e instanceof RuntimeException){
                throw (RuntimeException)e;
            }
            throw new QDLException("error cloning state", e);
        }
    }
    public static State javaClone(State state) throws IOException, ClassNotFoundException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        save(state, baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        State newState = load(bais);
        // Now set all the transient fields that were not serialized.
        newState.setOpEvaluator(state.getOpEvaluator());
        newState.setMetaEvaluator(state.getMetaEvaluator());
        newState.setLogger(state.getLogger());
        newState.setScriptPaths(state.getScriptPaths());
        newState.setModulePaths(state.getModulePaths());
        newState.setVfsFileProviders(state.getVfsFileProviders());
        newState.setServerMode(state.isServerMode());
        newState.setRestrictedIO(state.isRestrictedIO());
        newState.setWorkspaceCommands(state.getWorkspaceCommands());
        return newState;

    }

    public static int size(State state) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            save(state, baos);
            return baos.toByteArray().length;
        } catch (IOException iox) {
            if(state.isDebugOn()) {
                iox.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * Serialize the Sate object to the given output stream.
     *
     * @param state
     * @param outputStream
     * @throws IOException
     */
    public static void save(State state, OutputStream outputStream) throws IOException {
        saveObject(state, outputStream);
    }

    public static void saveObject(Object object, OutputStream outputStream) throws IOException {
        GZIPOutputStream gos = new GZIPOutputStream(outputStream,65536);
        ObjectOutputStream out = new ObjectOutputStream(gos);

        // Method for serialization of object
        out.writeObject(object);
        out.flush();
        out.close();
    }

    /**
     * Serialize the state to a base 64 encoded string.
     *
     * @param state
     * @return
     * @throws IOException
     */
    public static String saveb64(State state) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        save(state, baos);
        return Base64.encodeBase64URLSafeString(baos.toByteArray());
    }

    /**
     * Deserialize the state from a base 64 encoded string.
     *
     * @param encodedState
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static State loadb64(String encodedState) throws IOException, ClassNotFoundException {
        byte[] bytes = Base64.decodeBase64(encodedState);
        ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
        return load(baos);
    }

    public static Object loadObject(InputStream inputStream) throws IOException, ClassNotFoundException {
        GZIPInputStream gis = new GZIPInputStream(inputStream,65536); // *trick* set buffer size large really ups speed
        ObjectInputStream in = new ObjectInputStream(gis);
        // Method for deserialization of object
        Object object = in.readObject();
        in.close();
        return object;

    }

    /**
     * See note on {@link #load(State, XMLEventReader)}
     * @param inputStream
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static State load(InputStream inputStream) throws IOException, ClassNotFoundException {
        return (State) loadObject(inputStream);
    }

    /**
     * Read in the state from an even reader. Notes that this is used in cases where there is
     * <b><i>no</i></b> workspace, such as in scripts, but there is a (possibly custom) state object
     * that needs to be populated.
     * @param state
     * @param xer
     * @return
     * @throws XMLStreamException
     */
    public static State load(State state, XMLEventReader xer) throws XMLStreamException {
        state.fromXML(xer,null);
        return state;
    }

    public static State load(State state, SerializationState SerializationState, XMLEventReader xer) throws XMLStreamException {
        state.fromXML(xer,null, SerializationState);
        return state;
    }

    public static State load(XMLEventReader xer) throws XMLStreamException {
        State state = newInstance();
        return load(state, xer);
    }

    public static State newInstance() {
        if (factory == null) {
            // default is plain vanilla State object.
            factory = new StateUtils() {
                @Override
                public State create() {
                    return new State(
                            new VStack(),
                            new OpEvaluator(),
                            MetaEvaluator.getInstance(),
                            new FStack(),
                            new MTStack(),
                            new MIStack(), 
                            new MyLoggingFacade("foo"),
                            false,
                            false,
                            true);
                }
            };
        }
        return getFactory().create();
    }

    public static void main(String[] args) {
        try {
            // Just a quick test for this
            State state = StateUtils.newInstance();
            state.setValue("foo", asQDLValue(42L));
            String b = saveb64(state);
            System.out.println("b = " + b);
            System.out.println("size = " + b.length());
            state = loadb64(b);
            System.out.println("state ok? " + state.getValue("foo").equals(42L));

            StringWriter sw = new StringWriter();
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            XMLStreamWriter xsw = xof.createXMLStreamWriter(sw);
            state.toXML(xsw);
            System.out.println(XMLUtils.prettyPrint(sw.toString()));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public abstract State create();

    protected static StateUtils factory;

    public static boolean isFactorySet() {
        return factory != null;
    }

    public static StateUtils getFactory() {
        return factory;
    }

    public static void setFactory(StateUtils f) {
        factory = f;
    }
}
