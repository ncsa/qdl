package org.qdl_lang.extensions;

import org.qdl_lang.state.State;
import org.qdl_lang.variables.values.QDLValue;

import java.io.Serializable;
import java.util.List;

/**
 * A wrapper for a single Java method that can be invoked from QDL. It may indeed be a single utility, or it may
 * front an entire class and expose the methods in it   call just hands this class
 * back. You could, for instance, have a single Java object with several methods, each of which holds a reference to the object
 * and invokes a method on it. There are may possibilities.
 * <p>Created by Jeff Gaynor<br>
 * on 1/27/20 at  12:02 PM
 */
public interface QDLFunction extends Serializable {
    /**
     * The name of this function as you want it invoked in QDL. This will be what the function is
     * called in the workspace.
     * @return
     */
     String getName();

    /**
     * The contract is that when QDL invokes this method, it will faithfully give all of the arguments
     * as an array of Objects. Overloading is not possible in QDL (it is weakly typed)
     * except by argument count, so if this is
     * called "foo" and you have versions with 3 and 4 arguments, then f(a,b,c) would be executed with the
     * arguments passed. To achieve overloading for Java methods, have the number of arguments this accepts
     * as the elements of the array. So if this has a value of [1,2,3] then this function will be invoked
     * if called with 1, 2 or 3 arguments and an error by the QDL runtime engine will be raised,
     * if, say, 4 are passed.
     *
     * @return
     */
     int[] getArgCount();

    /**
     * The method that is invoked by QDL that is the function. It will have the arguments
     * already evaluated and put in to the
     * array of objects. It is up to you to do any checking you see fit. State is supplied if needed.
     * @param qdlValues
     * @param state 
     * @return
     */
     QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable;


    /**
     * Return documentation to be displayed in the workspace. Short version displays the first
     * line (element of the list) so make sure that is informative. The long version should contain
     * details of use. Best practice is that have a succinct one line description for each
     * arg count and a body that explains the usage more fully.
     * @return
     */
    List<String> getDocumentation(int argCount);
}
