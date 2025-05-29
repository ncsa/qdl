package org.qdl_lang.extensions.examples.stateful;

import net.sf.json.JSONObject;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.extensions.QDLVariable;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.values.QDLNullValue;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;
import java.util.List;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (!(qdlValues[0].isString())) {
                throw new IllegalArgumentException(getName() + " requires a string");
            }
            String oldValue = s;
            s = qdlValues[0].asString();
            if(oldValue == null){
                return QDLValue.getNullValue();
            }
            return asQDLValue(oldValue);
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (s == null) {
                return QDLNullValue.getNullValue(); // QDL's equivalent of not being set.
            }
            return asQDLValue(s);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doc = new ArrayList<>();
            doc.add(getName() + "() - get the current string value.");
            return doc;
        }
    }
    public class ImportTimestamp implements QDLVariable {
        @Override
        public String getName() {
            return "import_ts";
        }

        @Override
        public Object getValue() {
            return System.currentTimeMillis();
        }
    }
    /**
      * This serializes the state of the Java module only. There is a QDl variable that
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
