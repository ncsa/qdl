package org.qdl_lang.gui;

import org.qdl_lang.state.State;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/10/22 at  1:27 PM
 */
public class QDLSwingUtil {

    /**
     * Most barebones completion -- just create it with the basic state.
     * @return
     */
    public static DefaultCompletionProvider createCompletionProvider() {
                            return createCompletionProvider(new State());
    }

    /**
     * Create the auto completion provider with a given state object. This will
     * get the functions from any modules loaded in the state.
     * @return
     */
    public static DefaultCompletionProvider createCompletionProvider(State state) {
        ArrayList<String> functions = new ArrayList<>();
        functions.addAll(state.getMetaEvaluator().listFunctions(false));
        functions.addAll(state.listFunctions(true,
                null, true, false, false));
        return createCompletionProvider(functions);
    }

    /**
     * For a given set of functions, return the auto completion provider.
     * @param functions
     * @return
     */
    public static DefaultCompletionProvider createCompletionProvider(List<String> functions) {

        // A DefaultCompletionProvider is the simplest concrete implementation
        // of CompletionProvider. This provider has no understanding of
        // language semantics. It simply checks the text entered up to the
        // caret position for a match against known completions. This is all
        // that is needed in the majority of cases.
        DefaultCompletionProvider provider = new DefaultCompletionProvider();

        // Add completions for all Java keywords. A BasicCompletion is just
        // a straightforward word completion.
        for (String function : functions) {
            // function names come back from the workspace e.g. as say([1,2]) with the
            // number of arguments. Do some surgery.
            function = function.substring(0, function.indexOf("("));
            provider.addCompletion(new BasicCompletion(provider, function + "("));
        }
        // statements with open [
        provider.addCompletion(new BasicCompletion(provider, "assert["));
        provider.addCompletion(new BasicCompletion(provider, "block["));
        provider.addCompletion(new BasicCompletion(provider, "body["));
        provider.addCompletion(new BasicCompletion(provider, "catch["));
        provider.addCompletion(new BasicCompletion(provider, "define["));
        provider.addCompletion(new BasicCompletion(provider, "do["));
        provider.addCompletion(new BasicCompletion(provider, "else["));
        provider.addCompletion(new BasicCompletion(provider, "if["));
        provider.addCompletion(new BasicCompletion(provider, "local["));
        provider.addCompletion(new BasicCompletion(provider, "module["));
        provider.addCompletion(new BasicCompletion(provider, "then["));
        provider.addCompletion(new BasicCompletion(provider, "try["));
        provider.addCompletion(new BasicCompletion(provider, "switch["));
        provider.addCompletion(new BasicCompletion(provider, "while["));

        // Keywords
        provider.addCompletion(new BasicCompletion(provider, "true"));
        provider.addCompletion(new BasicCompletion(provider, "false"));
        provider.addCompletion(new BasicCompletion(provider, "null"));

        // Types
        provider.addCompletion(new BasicCompletion(provider, "Decimal"));
        provider.addCompletion(new BasicCompletion(provider, "Integer"));
        provider.addCompletion(new BasicCompletion(provider, "List"));
        provider.addCompletion(new BasicCompletion(provider, "Null"));
        provider.addCompletion(new BasicCompletion(provider, "Number"));
        provider.addCompletion(new BasicCompletion(provider, "Set"));
        provider.addCompletion(new BasicCompletion(provider, "Stem"));
        provider.addCompletion(new BasicCompletion(provider, "String"));

        // workspace commands -- can't start with a ) since auto complete for () completely screws up
        provider.addCompletion(new BasicCompletion(provider, "buffer"));
        provider.addCompletion(new BasicCompletion(provider, "clear"));
        provider.addCompletion(new BasicCompletion(provider, "edit"));
        provider.addCompletion(new BasicCompletion(provider, "env"));
        provider.addCompletion(new BasicCompletion(provider, "funcs"));
        provider.addCompletion(new BasicCompletion(provider, "help"));
        provider.addCompletion(new BasicCompletion(provider, "load"));
        provider.addCompletion(new BasicCompletion(provider, "modules"));
        provider.addCompletion(new BasicCompletion(provider, "off"));
        provider.addCompletion(new BasicCompletion(provider, "save"));
        provider.addCompletion(new BasicCompletion(provider, "si"));
        provider.addCompletion(new BasicCompletion(provider, "vars"));
        provider.addCompletion(new BasicCompletion(provider, "ws"));


        provider.addCompletion(new BasicCompletion(provider, "list"));
        provider.addCompletion(new BasicCompletion(provider, "link"));
        provider.addCompletion(new BasicCompletion(provider, "create"));
        provider.addCompletion(new BasicCompletion(provider, "read"));
        provider.addCompletion(new BasicCompletion(provider, "write"));
        provider.addCompletion(new BasicCompletion(provider, "drop"));


        // Add a couple of "shorthand" completions. These completions don't
        // require the input text to be the same thing as the replacement text.
   /*
           provider.addCompletion(new ShorthandCompletion(provider, "sysout",
                   "System.out.println(", "System.out.println("));
           provider.addCompletion(new ShorthandCompletion(provider, "syserr",
                   "System.err.println(", "System.err.println("));
   */

        return provider;

    }
}
