package org.qdl_lang.state;

import org.fife.ui.autocomplete.BasicCompletion;
import org.qdl_lang.evaluate.MetaEvaluator;
import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.exceptions.IndexError;
import org.qdl_lang.exceptions.NamespaceException;
import org.qdl_lang.exceptions.QDLException;
import org.qdl_lang.exceptions.UnknownSymbolException;
import org.qdl_lang.expressions.module.MIStack;
import org.qdl_lang.expressions.module.MTStack;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.variables.*;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.variables.values.StemValue;

import java.util.*;
import java.util.regex.Pattern;

import static org.qdl_lang.variables.QDLStem.STEM_INDEX_MARKER;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/2/20 at  6:42 AM
 */
public abstract class VariableState extends NamespaceAwareState {
    public static String var_regex = "^[a-zA-Z0-9_$]+[a-zA-Z0-9_$\\.]*";
    public static String int_regex = "[1-9][0-9]*";

    public VariableState(VStack vStack,
                         OpEvaluator opEvaluator,
                         MetaEvaluator metaEvaluator,
                         MTStack mtStack,
                         MIStack miStack,
                         MyLoggingFacade myLoggingFacade) {
        super(vStack,
                opEvaluator,
                metaEvaluator,
                mtStack,
                miStack,
                myLoggingFacade);
    }

    private static final long serialVersionUID = 0xcafed00d7L;


    /**
     * Checks if a symbol is defined. Note that this does do stem tail resolution and namespace resolution.
     *
     * @param symbol
     * @return
     */
    public boolean isDefined(String symbol) {
        try {
            return getValue(symbol) != null;
        } catch (IndexError | UnknownSymbolException u) {
            // Can happen if the request is for a stem that eventually does not resolve.
            return false;
        }
    }


    /**
     * GetValue may return null if the variable has not been set.
     *
     * @param variableName
     * @return
     */
    public QDLValue getValue(String variableName) {
        if (variableName == null) {
            throw new NFWException("null variable name encountered.");
        }
        return getValue(variableName, null); // kick off the search

    }


    public QDLValue getValue(String variableName, Set<XKey> checkedAliases) {
        if (checkedAliases == null) {
            checkedAliases = new HashSet<>();
        }
        if (isStem(variableName)) {
            StemMultiIndex w = new StemMultiIndex(variableName);
            return gsrNSStemOp(w, OP_GET, null, checkedAliases);
        }
         QDLVariable variable = gsrNSScalarOp(variableName, OP_GET, null, checkedAliases);
        return variable != null ? variable.getQDLValue() : null;
    }


    public void setValue(String variableName, QDLValue value) {
        setValue(variableName, value, null);
        if (hasCompletionProvider()) {
            getCompletionProvider().addCompletion(new BasicCompletion(getCompletionProvider(), variableName));
        }
    }
    public void setValue(String variableName, QDLValue value, Set<XKey> checkedAliases) {
        if (checkedAliases == null) {
            checkedAliases = new HashSet<>();
        }
        if (isStem(variableName)) {
            StemMultiIndex w = new StemMultiIndex(variableName);
            // Don't allow assignments of wrong type, but do let them set a stem to null.
            if (w.isStem()) {
                if (value.isSet()) {
                    if (value.asSet().isEmpty()) {
                        // Fix for https://github.com/ncsa/qdl/issues/5
                        // Issue is that input_form would wrongly serialize an empty stem as {},
                        // then deserialization would bomb since that is the empty set.
                        // If the value is the empty set, allow trivial case of setting empty set to empty stem
                        // or saved workspaces will not deserialize.
                        value = new StemValue(); // empty stem
                    }
                }
                if (!(value.isStem()) && !(value.isNull())) {
                    throw new IndexError("Error: You cannot set the " + ((value.isSet()) ? "set '" : "scalar value '") + value + "' to the stem variable '" + variableName + "'", null);
                }
            } else {
                if (value.isStem()) {
                    throw new IndexError("Error: You cannot set the scalar variable '" + variableName + "' to the stem value '" + value + "'", null);
                }
            }
            gsrNSStemOp(w, OP_SET, value, new HashSet<>());
            return;
        }
        if (value.isStem()) {
            throw new IndexError("Error: You cannot set the scalar variable '" + variableName + "' to the stem value '" + value + "'", null);
        }

        gsrNSScalarOp(variableName, OP_SET, value, checkedAliases);
        return;
    }

/*
    public void OLDsetValue(String variableName, Object value, Set<XKey> checkedAliases) {
        if (checkedAliases == null) {
            checkedAliases = new HashSet<>();
        }
        if (isStem(variableName)) {
            StemMultiIndex w = new StemMultiIndex(variableName);
            // Don't allow assignments of wrong type, but do let them set a stem to null.
            if (w.isStem()) {
                if (value instanceof QDLSet) {
                    if (((QDLSet) value).isEmpty()) {
                        // Fix for https://github.com/ncsa/qdl/issues/5
                        // Issue is that input_form would wrongly serialize an empty stem as {},
                        // then deserialization would bomb since that is the empty set.
                        // If the value is the empty set, allow trivial case of setting empty set to empty stem
                        // or saved workspaces will not deserialize.
                        value = new QDLStem();
                    }
                }
                if (!(value instanceof QDLStem) && !(value instanceof QDLNull)) {
                    throw new IndexError("Error: You cannot set the " + ((value instanceof QDLSet) ? "set '" : "scalar value '") + value + "' to the stem variable '" + variableName + "'", null);
                }
            } else {
                if (value instanceof QDLStem) {
                    throw new IndexError("Error: You cannot set the scalar variable '" + variableName + "' to the stem value '" + value + "'", null);
                }
            }
            gsrNSStemOp(w, OP_SET, value, new HashSet<>());
            return;
        }
        if (value instanceof QDLStem) {
            throw new IndexError("Error: You cannot set the scalar variable '" + variableName + "' to the stem value '" + value + "'", null);
        }

        gsrNSScalarOp(variableName, OP_SET, value, checkedAliases);
        return;
    }
*/

    public void remove(String variableName) {
        if (hasCompletionProvider()) {
            getCompletionProvider().removeCompletion(new BasicCompletion(getCompletionProvider(), variableName));
        }
        if (isStem(variableName)) {
            StemMultiIndex w = new StemMultiIndex(variableName);
            gsrNSStemOp(w, OP_REMOVE, null, new HashSet<>());
            return;
        }
        gsrNSScalarOp(variableName, OP_REMOVE, null, new HashSet<>());
    }


    /**
     * This loops from right to left through the indices of the wrapper. The result is the fully resolved
     * indices against whatever the current state of the symbol stacks are (including modules).
     * The resulting wrapper (which may be substantially smaller than the original) may then be used
     * against the symbol table to actually do the GSR operation.
     *
     * @param w
     * @return
     */
    private StemMultiIndex resolveStemIndices(StemMultiIndex w) {
        int startinglength = w.getRealLength() - 1; // first pass require some special handling
        for (int i = w.getRealLength() - 1; -1 < i; i--) {
            String x = w.getComponents().get(i);
            if (x == null || x.isEmpty()) {
                // skip missing stem elements. so a...b == a.b;
                continue;
            }
            String newIndex = resolveStemIndex(x);

            if (i == startinglength) {
                // No stem resolution should be attempted on the first pass.r
                w.getComponents().set(i, newIndex);
                continue;
            }
            // Check if current index is a stem and feed what's to the right to it as a multi-index
            if (isDefined(newIndex + STEM_INDEX_MARKER)) {
                StemMultiIndex ww = new StemMultiIndex(w, i);
                Object v = gsrNSStemOp(ww, OP_GET, null, new HashSet<>());
                if (v == null) {
                    throw new IndexError("Error: The stem in the index \"" + ww.getName() + "\" did not resolve to a value", null);
                }
                StringTokenizer st = new StringTokenizer(v.toString(), ".");
                ArrayList<String> newIndices = new ArrayList<>();
                while (st.hasMoreTokens()) {
                    String nt = st.nextToken();
                    newIndices.add(nt);
                }
                w.getComponents().remove(i); // replace the element at i with whatever was found.
                w.getComponents().addAll(i, newIndices);

                w.setRealLength(i + newIndices.size());
            } else {
                w.getComponents().set(i, newIndex);
            }
        }
        return w.truncate();
    }


    /**
     * gsr = get, set or remove. This resolves the name of the stem
     *
     * @param w
     * @param op
     * @param value
     * @return
     */
    protected QDLValue gsrNSStemOp(StemMultiIndex w, int op,
                                   QDLValue value, Set<XKey> checkInstances) {
        w = resolveStemIndices(w);
        String variableName;
        QDLStem stem = null;
        boolean isQDLNull = false;
        VStack vStack = null;
        variableName = w.name;
        XKey vKey = new XKey(w.name);
        Object object;
        boolean didIt = false;
        QDLVariable curretVariable = null;
        if (isExtrinsic(variableName)) {
            VThing vThing = (VThing) getExtrinsicVars().get(vKey);
            if (vThing == null) {
                stem = null;
            } else {
                curretVariable = vThing.getVariable();
                isQDLNull = vThing.isNull(); // Fix https://github.com/ncsa/qdl/issues/42
                if (vThing.isStem()) {
                    stem = vThing.getStemValue();
                }
            }
            didIt = true;
        }
        if (isIntrinsic(variableName)) {
            vStack = getIntrinsicVariables();
            VThing vThing = (VThing) vStack.get(vKey);
            if (vThing == null) {
                stem = null;
            } else {
                curretVariable = vThing.getVariable();
                isQDLNull = vThing.isNull(); // Fix https://github.com/ncsa/qdl/issues/42
                if (vThing.isStem()) {
                    stem = vThing.getStemValue();
                }
            }
            didIt = true;
        }
        if (!didIt) {
            VThing vThing;
            // look in specific places
            if (isImportMode()) {
                vThing = (VThing) getVStack().localGet(vKey);
            } else {
                vThing = (VThing) getVStack().get(vKey);
            }
            if (vThing != null && vThing.isNull()) {
                isQDLNull = true;
            } else {
                VThing oooo = (VThing) getVStack().get(vKey);
                if (oooo != null && !(oooo.isStem())) {
                    throw new IllegalArgumentException("error: a stem was expected");
                }
                if (oooo == null) {
                    stem = null;
                } else {
                    stem = oooo.getStemValue();
                }
            }
            boolean gotOne = false;
            VThing v;
            // most likely place for it was in the main symbol table. But since there is
            // no name clash, look for it in the OLD modules. As long as there is nothing
            // here this (very expensive) search is skipped
            if (stem == null && !isQDLNull) {
                if (!getMInstances().isEmpty()) {
                    for (Object key : getMInstances().keySet()) {
                        XKey xKey = (XKey) key;
                        if (checkInstances.contains(xKey)) {
                            return null;
                        }
                        Module m = getMInstances().getModule((XKey) key);
                        if (m != null) {
                            for (Object kk : m.getState().getVStack().keySet()) {
                                XKey xx = (XKey) kk;
                                if (variableName.equals(xx.getKey())) {
                                    if (gotOne) {
                                        throw new NamespaceException("multiple modules found for '" + variableName + "', Please qualify the name.");
                                    } else {
                                        gotOne = true;
                                        stem = ((VThing) m.getState().getVStack().get(xx)).getStemValue();

                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        switch (op) {
            case OP_GET:
                if (stem == null && !isQDLNull) {
                    //    throw new UnknownSymbolException("error: The stem variable \"" + variableName + "\" does not exist, so cannot get its value.");
                    return null;
                }
                if (isQDLNull) {
                    return QDLNull.getInstance().getResult();
                }
                if (w.isEmpty()) {
                    return new QDLValue(stem);
                }
                Object oo = stem.get(w);
                if(oo == null) {return null;}
                return new QDLValue(oo);
            case OP_SET:
                //VThing vValue = new VThing(new XKey(variableName), value);
                if (w.isEmpty()) {
                    setValueImportAware(variableName, value);
                } else {
                    StemValue stemValue;
                    if (stem == null || isQDLNull) {
                        stem = new QDLStem();
                        stemValue =  new StemValue(stem);
                        setValueImportAware(variableName, stemValue);
                    }else{
                        stemValue =  new StemValue(stem);
                    }
                    stem.set(w, value);
                    setValueImportAware(variableName, stemValue);
                }
                return null;
            case OP_REMOVE:
                if (stem == null && !isQDLNull) {
                    throw new UnknownSymbolException("error: The stem variable \"" + variableName + "\" does not exist, so cannot remove a value from it.", null);
                }
                if (w.isEmpty()) {
                    if (isExtrinsic(variableName)) {
                        getExtrinsicVars().remove(vKey);
                    } else {
                        getVStack().remove(vKey);
                    }
                } else {
                    stem.remove(w);
                }
                return null;
        }
        throw new NFWException("Internal error; unknown operation type on stem variables.");
    }

    /**
     * the contract here is that if the {@link VThing} is null, then it can be created. If
     * it does exist, then cvarious contracts for type safety are followed. These are in the
     * {@link QDLVariable#setQDLValue(QDLValue)} method.
     *
     * <p>This returns the vThing to save or throws an exception for a type violation.</p>
     * @param vThing
     * @param key
     * @param newValue
     * @return
     */
    protected VThing handleVariableType(VThing vThing, XKey key, QDLValue newValue) {
        if(vThing == null){
            return new VThing(key, new QDLVariable(newValue));
        }
        vThing.getVariable().setQDLValue(newValue);
        return vThing;
    }

    public int getTypeSafetyMode() {
        return typeSafetyMode;
    }

    public void setTypeSafetyMode(int typeSafetyMode) {
        this.typeSafetyMode = typeSafetyMode;
    }

    protected int typeSafetyMode = QDLVariable.TYPE_DYNAMIC;
    /**
     * This sets variables allowing for {@link #isImportMode()} to be true, so the module
     * statements are evaluated, but discarded
     * @param variableName
     * @param value
     */
    private void setValueImportAware(String variableName, QDLValue value) {
        XKey xKey = new XKey(variableName);

        VThing vThing;
        if (isExtrinsic(variableName)) {
            vThing = handleVariableType((VThing)getExtrinsicVars().get(xKey), xKey, value);
            getExtrinsicVars().put(vThing);
            return;
        }
        if (isIntrinsic(variableName)) {
            vThing = handleVariableType((VThing)getIntrinsicVariables().get(xKey), xKey, value);
            getIntrinsicVariables().put(vThing);
            return;
        }
        if (isImportMode()) {
            vThing = handleVariableType((VThing)getVStack().localGet(xKey), xKey, value);
            getVStack().localPut(vThing);
        } else {
            vThing = handleVariableType((VThing)getVStack().get(xKey), xKey, value);
            getVStack().put(vThing);
        }
    }

    Pattern intPattern = Pattern.compile(int_regex);

    /*
      module['a:/b','X'][__a:=1;get_a()->__a;]
 module_import('a:/b','X')
   X#__a
      X#get_a()
     */
    protected QDLVariable gsrNSScalarOp(String variableName,
                                        int op,
                                        QDLValue value,
                                        Set<XKey> checkedAliases) {
        // if(!pattern.matcher(v.getName()).matches()){
        if (variableName.equals("0") || intPattern.matcher(variableName).matches()) {
            // so its an actual index, like 0, 1, ...
            // Short circuit all the machinery because it will never resolve
            // and there is no need to jump through all of this.
            return null;
        }

        VThing v = null;
        XKey xKey = new XKey(variableName);
        boolean didIt = false;
        if (isExtrinsic(variableName)) {
            v = (VThing) getExtrinsicVars().get(xKey);
            didIt = true;
        }
        if (isIntrinsic(variableName)) {
            v = (VThing) getIntrinsicVariables().get(xKey);
            didIt = true;
        }
        if (!didIt) {
            v = (VThing) getVStack().get(xKey);
            if (v == null && value== null) { // only check old imports for clashes. Re-assingin
                boolean gotOne = false;
                if (!(isImportMode() || getMInstances().isEmpty())) {
                    for (Object key : getMInstances().keySet()) {
                        XKey xkey = (XKey) key;
                        if (checkedAliases.contains(xkey)) {
                            return null;
                        }

                        Module m = getMInstances().getModule((XKey) key);
                        if (m != null) {
                            // New module stuff. The next block will look for name collisions
                            // in old style modules.  The aim is
                            // to grab an unqualified existing entry if it exists or blow up.
                            // The previous code for old modules
                            // could not cope with improvements to the module system.
                            // It is deprecated and should be considered fragile.
                            // Old code was also pretty slow. New code does not look deeply, but
                            // for old modules, nesting didn't really work right anyway, so was not used.
                            for (Object kk : m.getState().getVStack().keySet()) {
                                XKey xx = (XKey) kk;
                                if (variableName.equals(xx.getKey())) {
                                    if (gotOne) {
                                        throw new NamespaceException("multiple modules found for '" + variableName + "', Please qualify the name.");
                                    } else {
                                        gotOne = true;
                                        v = (VThing) m.getState().getVStack().get(xx);

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        switch (op) {
            case OP_GET:
                // For resolving intrinsic variables.
                if (v == null) {
                    return null;
                }
                return v.getVariable();
            case OP_SET:
                setValueImportAware(variableName, value);
                return null;
            case OP_REMOVE:
                if (isExtrinsic(variableName)) {
                    getExtrinsicVars().remove(xKey);
                } else {
                    if(isIntrinsic(variableName)) {
                        getIntrinsicVariables().remove(xKey);
                    }else {
                        getVStack().remove(xKey);
                    }
                }
        }

        return null;
    }

    static final int OP_SET = 0;
    static final int OP_GET = 1;
    static final int OP_REMOVE = 2;


    public static class ResolveState {
        ArrayList<String> foundVars = new ArrayList<>();

        public void addFound(String index) {
            if (foundVars.contains(index)) {
                throw new CyclicalError("Cyclical index error for " + foundVars);
            }
            foundVars.add(index);
        }
    }

    /**
     * Thrown by {@link ResolveState} if a cycle is found.
     */
    public static class CyclicalError extends QDLException {
        public CyclicalError(String message) {
            super(message);
        }
    }

    protected String resolveStemIndex(String index, ResolveState resolveState) {

        Object obj = getValue(index);
        if (obj == null) {
            return index; // null value means does not resolve to anything
        }
        String newIndex = obj.toString();
        resolveState.addFound(newIndex);
        return resolveStemIndex(newIndex, resolveState);
    }

    protected String resolveStemIndex(String index) {
        ResolveState resolveState = new ResolveState();
        // start the cyclical monitoring
        return resolveStemIndex(index, resolveState);
    }

    public TreeSet<String> listVariables(boolean useCompactNotation,
                                         boolean includeModules,
                                         boolean showIntrinsic,
                                         boolean showExtrinsic) {
        TreeSet<String> out = getVStack().listVariables();
        if (showExtrinsic) {
            out.addAll(getExtrinsicVars().listVariables());
        }
        if (showIntrinsic) {
            out.addAll(getIntrinsicVariables().listVariables());
        }
        if (!includeModules) {
            return out;
        }
        for (Object key : getMInstances().keySet()) {
            XKey xKey = (XKey) key;
            Module m = getMInstances().getModule(xKey);
            if (m == null) {
                continue; // the user specified a non-existent module.
            }

            TreeSet<String> uqVars = m.getState().getVStack().listVariables();
            for (String x : uqVars) {
                if (isIntrinsic(x) && !showIntrinsic) {
                    continue;
                }
                if (useCompactNotation) {
                    out.add(getMInstances().getAliasesAsString(m.getMTKey()) + NS_DELIMITER + x);
                } else {
                    for (Object alias : getMInstances().getAliasesAsString(m.getMTKey())) {
                        out.add(alias + NS_DELIMITER + x);
                    }
                }
            }
        }

        return out;
    }

    public boolean isStem(String var) {
        return var.contains(STEM_INDEX_MARKER); // if there is an embedded period, needs to be resolved
    }

    //public static final String EXTRINSIC_MARKER = "&";
    public static final String EXTRINSIC_MARKER = "$$";

    public static boolean isExtrinsic(String x) {
        return x.startsWith(EXTRINSIC_MARKER);
    }

    // This is actually static but due to some visibility issues with static elements, it
    // has to be in the State object proper. This accessor is overridden there.,
    public VStack getExtrinsicVars() {
        return null;
    }

    public void setIntrinsicVariables(VStack instrinsicVariables) {
        this.instrinsicVariables = instrinsicVariables;
    }

    VStack instrinsicVariables;

    public VStack getIntrinsicVariables() {
        if (instrinsicVariables == null) {
            instrinsicVariables = new VStack();
        }
        return instrinsicVariables;
    }
}
