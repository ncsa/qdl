package org.qdl_lang;

import edu.uiuc.ncsa.security.core.util.FileUtil;
import edu.uiuc.ncsa.security.util.scripting.ScriptSet;
import net.sf.json.JSONObject;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.scripting.AnaphorUtil;
import org.qdl_lang.scripting.QDLScript;
import org.qdl_lang.state.State;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class AnaphorTest extends AbstractQDLTester{
    String anaphorModule = System.getenv("NCSA_DEV_INPUT") + "/qdl/language/src/main/resources/modules/anaphors.mdl";

    public void testCreate() throws Throwable{
        File temp = File.createTempFile("anaphor-","qdl");
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script,"a := import(load('" + anaphorModule + "'));");
        addLine(script, "anaphors.0 := a#to_load('foo', 'bar');");
        addLine(script, "anaphors.1 := a#to_load('foo', ['bar0','bar1']);");
        addLine(script, "anaphors.2 := a#to_load('foo', [['bar0','bar1']]);");
        addLine(script, "anaphors.3 := a#to_load('foo', {'a':'bar0','b':'bar1'});");
        //addLine(script,"say(print(anaphors.));");
        // Each line is an anaphor.
        addLine(script,"file_write('" + temp.getAbsolutePath() +"',anaphors.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());

        // Now check against the utility that they were created right
        /*
   0 : {"qdl":{"args":"bar","load":"foo"}}
   1 : {"qdl":{"args":["bar0","bar1"],"load":"foo"}}
   2 : {"qdl":{"args":[["bar0","bar1"]],"load":"foo"}}
   3 : {"qdl":{"args":{"a":"bar0","b":"bar1"},"load":"foo"}}
         */
        List<String> anaphors = FileUtil.readFileAsLines(new FileInputStream(temp));
        for(String anaphor : anaphors){
            p(JSONObject.fromObject(anaphor));

        }
//        assert scripts.iterator().next().getLines().get(0).equals("script_load('foo', 'bar');");
    }
    protected void p(JSONObject json) {
        ScriptSet<QDLScript> scripts =  (AnaphorUtil.createScripts(json.getJSONObject(AnaphorUtil.QDL_TAG)));
        //System.out.println(scripts.iterator().next().getLines().get(0));
    }
}
