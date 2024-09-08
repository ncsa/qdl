package org.qdl_lang.extensions.examples.stateful;

import net.sf.json.JSONObject;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.extensions.QDLVariable;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.QDLNull;

import java.util.ArrayList;
import java.util.List;

public class StatefulExample implements QDLMetaModule {
    protected String s;

    public class SetS implements QDLFunction {
        @Override
        public String getName() {
            return "set_string";
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (!(objects[0] instanceof String)) {
                throw new IllegalArgumentException(getName() + " requires a string");
            }
            String oldValue = s;
            s = (String) objects[0];
            return oldValue;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doc = new ArrayList<>();
            switch (argCount) {
                case 1:
                    doc.add(getName() + "(s) - set a string value.");
                    break;
            }
            doc.add("This function lets you set a string value. This is part of the sample");
            doc.add("in the toolkit that shows how to save Java state of modules.");
            return List.of();
        }
    }

    public class GetS implements QDLFunction {
        @Override
        public String getName() {
            return "get_string";
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (s == null) {
                return QDLNull.getInstance(); // QDL's equivalent of not being set.
            }
            return s;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doc = new ArrayList<>();
            doc.add(getName() + "() - get the current string value.");
            return doc;
        }
    }
    public class LoadTimestamp  implements QDLVariable {
        @Override
        public String getName() {
            return "load_timestamp";
        }

        @Override
        public Object getValue() {
            return System.currentTimeMillis();
        }
    }
    /**
      * Thios serializes the state of the Java module only. There is a QDl variable that
      * is created, but that is stored in QDL's {@link State} object, so you don't need to do
     * anything with it. There is no requirement on the structure of the JSON object.
     * You are free to structure it however you want.
     */
    @Override
    public JSONObject serializeToJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("s", s);
        return jsonObject;
    }

    /**
     * This is invoked on deserialization for you with the JSON object you set. You may also
     * do any other re-initialization tasks that are required to make your class functional.
     * The contract states that when this method exits, the class's state should be fully
     * restored.
     * @param json
     */
    @Override
    public void deserializeFromJSON(JSONObject json) {
        if (json.containsKey("s")) {
            s = json.getString("s");
        }
    }
}
