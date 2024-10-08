package org.qdl_lang.variables;

import org.qdl_lang.state.State;
import org.qdl_lang.util.InputFormUtil;
import net.sf.json.JSONArray;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/6/22 at  4:02 PM
 */
public class QDLSet extends HashSet {
    public QDLSet() {
    }

    /**
     * Constructor to initialize a set from another non-QDL set. This adds every
     * element of the argument, not just the argument.
     * @param set
     */
    public QDLSet(Set set) {
        addAll(set);
    }

    /**
     * JSON does not have sets, so this is a bit klugy: It will take an array and
     * stick the values into a set. This is lossy.
     *
     * @param array
     */
    public void fromJSON(JSONArray array) {
        QDLStem stemVariable = new QDLStem();
        stemVariable.fromJSON(array);
        clear();
        addAll(stemVariable.getQDLList().values());
    }

    public JSONArray toJSON() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(this);
        return jsonArray;
    }

    public String inputForm() {
        String out = "{";
        boolean isFirst = true;
        for (Object obj : this) {
            out = out + (isFirst ? "" : ",") + InputFormUtil.inputForm(obj);
            isFirst = false;
        }
        return out + "}";
    }

    @Override
    public String toString() {
        if(isEmpty()){
            if(State.isPrintUnicode()){
                return "∅";
            }
            return "{}";
        }
        String out = "{";
        boolean isFirst = true;
        for (Object obj : this) {
            String value;
            if (obj instanceof BigDecimal) {
                value = InputFormUtil.inputForm((BigDecimal) obj);
            } else {
                value = obj.toString();
            }
            out = out + (isFirst ? "" : ",") + value;
            isFirst = false;
        }
        return out + "}";
    }


    public QDLSet intersection(QDLSet arg) {
        QDLSet outSet = new QDLSet();
        for (Object key : this) {
            if (arg.contains(key)) {
                outSet.add(key);
            }
        }
        return outSet;
    }

    public QDLSet difference(QDLSet arg) {
        QDLSet outSet = new QDLSet();
        for (Object key : this) {
            if (!arg.contains(key)) {
                outSet.add(key);
            }
        }
        return outSet;
    }

    public QDLSet union(QDLSet arg) {
        QDLSet outSet = new QDLSet();
        outSet.addAll(this);
        outSet.addAll(arg);
        return outSet;
    }

    public boolean isSubsetOf(QDLSet arg) {
        for (Object key : this) {
            if (!arg.contains(key)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEqualTo(QDLSet arg) {
        return isSubsetOf(arg) && (size() == arg.size());
    }

    public QDLSet symmetricDifference(QDLSet arg) {
        return difference(arg).union(arg.difference(this));
    }


    public static void main(String[] args) {
        QDLSet set1 = new QDLSet();
        set1.add(2);
        set1.add(3);
        set1.add(5);
        set1.add("p");
        set1.add("a");

        QDLSet set2 = new QDLSet();
        set2.add(2);
        set2.add(4);
        set2.add(5);
        set2.add("a");
        set2.add("b");
        set2.add("q");
        QDLSet nested = new QDLSet();
        nested.addAll(set1);
        nested.add(set2); // nested

        System.out.println("to stem = " + nested.toStem());
        System.out.println("s1= " + set1);
        System.out.println("s2= " + set2);
        System.out.println("s1 + s2 = " + set1.union(set2));
        System.out.println("s1 - s2= " + set1.difference(set2));
        System.out.println("s2 - s1= " + set2.difference(set1));
        System.out.println("s1 subset s2= " + set1.isSubsetOf(set2));
        System.out.println("s1 sDiff s2= " + set1.symmetricDifference(set2));
        System.out.println("s1 == s1? " + set1.isEqualTo(set1));

    }
   protected List convertToList(){
       ArrayList list = new ArrayList();
       for(Object element: this) {
           if (element instanceof QDLSet) {
               list.add(((QDLSet) element).convertToList());
           } else {
               list.add(element);
           }
       }
        return list;
   }
    public QDLStem toStem(){
        QDLStem outStem = new QDLStem();
        outStem.getQDLList().addAll(this);
//        outStem.addList(convertToList());
        return outStem;
    }
}
