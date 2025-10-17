package org.qdl_lang.evaluate;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.Option;
import org.qdl_lang.exceptions.*;
import org.qdl_lang.expressions.*;
import org.qdl_lang.functions.*;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.Statement;
import org.qdl_lang.util.aggregate.AxisRestrictionIdentity;
import org.qdl_lang.variables.*;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.qdl_lang.variables.values.*;

import java.util.*;
import java.util.regex.Pattern;

import static org.qdl_lang.state.VariableState.var_regex;
import static org.qdl_lang.util.aggregate.ProcessStemAxisRestriction.ALL_AXES;
import static org.qdl_lang.variables.Constant.*;
import static org.qdl_lang.variables.QDLStem.STEM_INDEX_MARKER;
import static org.qdl_lang.variables.StemUtility.LAST_AXIS_ARGUMENT_VALUE;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  9:19 AM
 */
public class StemEvaluator extends AbstractEvaluator {
    public static final String STEM_NAMESPACE = "stem";

    @Override
    public String getNamespace() {
        return STEM_NAMESPACE;
    }

    public static final int STEM_FUNCTION_BASE_VALUE = 2000;

    public static final String SIZE = "size";
    public static final int SIZE_TYPE = 1 + STEM_FUNCTION_BASE_VALUE;

    public static final String SHORT_MAKE_INDICES = "n";
    public static final int MAKE_INDICES_TYPE = 4 + STEM_FUNCTION_BASE_VALUE;


    public static final String REMOVE = "remove";
    public static final int REMOVE_TYPE = 5 + STEM_FUNCTION_BASE_VALUE;

    public static final String SET_DEFAULT = "set_default";
    public static final int SET_DEFAULT_TYPE = 7 + STEM_FUNCTION_BASE_VALUE;

    public static final String BOX = "box";
    public static final int BOX_TYPE = 8 + STEM_FUNCTION_BASE_VALUE;

    public static final String UNBOX = "unbox";
    public static final int UNBOX_TYPE = 9 + STEM_FUNCTION_BASE_VALUE;

    public static final String UNION = "union";
    public static final int UNION_TYPE = 10 + STEM_FUNCTION_BASE_VALUE;


    public static final String HAS_VALUE = "has_value";
    public static final int HAS_VALUE_TYPE = 12 + STEM_FUNCTION_BASE_VALUE;

    public static final String DIMENSION = "dim";
    public static final int DIMENSION_TYPE = 14 + STEM_FUNCTION_BASE_VALUE;

    public static final String RANK = "rank";
    public static final int RANK_TYPE = 15 + STEM_FUNCTION_BASE_VALUE;

    public static final String FOR_EACH = "for_each";
    public static final int FOR_EACH_TYPE = 16 + STEM_FUNCTION_BASE_VALUE;

    // Key functions
    public static final String COMMON_KEYS = "common_keys";
    public static final int COMMON_KEYS_TYPE = 100 + STEM_FUNCTION_BASE_VALUE;

    public static final String EXCLUDE_KEYS = "exclude_keys";
    public static final int EXCLUDE_KEYS_TYPE = 101 + STEM_FUNCTION_BASE_VALUE;

    public static final String LIST_KEYS = "list_keys";
    public static final int LIST_KEYS_TYPE = 102 + STEM_FUNCTION_BASE_VALUE;

    public static final String HAS_KEY = "has_key";
    public static final int HAS_KEY_TYPE = 103 + STEM_FUNCTION_BASE_VALUE;


    public static final String INCLUDE_KEYS = "include_keys";
    public static final int INCLUDE_KEYS_TYPE = 104 + STEM_FUNCTION_BASE_VALUE;


    public static final String RENAME_KEYS = "rename_keys";
    public static final int RENAME_KEYS_TYPE = 105 + STEM_FUNCTION_BASE_VALUE;

    public static final String MASK = "mask";
    public static final int MASK_TYPE = 106 + STEM_FUNCTION_BASE_VALUE;

    public static final String KEYS = "keys";
    public static final int KEYS_TYPE = 107 + STEM_FUNCTION_BASE_VALUE;

    public static final String SHUFFLE = "shuffle";
    public static final int SHUFFLE_TYPE = 108 + STEM_FUNCTION_BASE_VALUE;

    public static final String UNIQUE_VALUES = "unique";
    public static final int UNIQUE_VALUES_TYPE = 109 + STEM_FUNCTION_BASE_VALUE;


    public static final String JOIN = "join";
    public static final int JOIN_TYPE = 110 + STEM_FUNCTION_BASE_VALUE;

    public static final String ALL_KEYS = "indices";
    public static final int ALL_KEYS_TYPE = 111 + STEM_FUNCTION_BASE_VALUE;

    public static final String TRANSPOSE = "transpose";
    public static final int TRANSPOSE_TYPE = 112 + STEM_FUNCTION_BASE_VALUE;

    public static final String REMAP = "remap";
    public static final int REMAP_TYPE = 114 + STEM_FUNCTION_BASE_VALUE;

    // Note that there are deprecated in favor of has_key.
    public static final String HAS_KEYS = "has_keys";
    public static final int HAS_KEYS_TYPE = 115 + STEM_FUNCTION_BASE_VALUE;

    public static final String EXCISE = "excise";
    public static final int EXCISE_TYPE = 116 + STEM_FUNCTION_BASE_VALUE;

/*    public static final String MAP = "map";
    public static final int MAP_TYPE = 117 + STEM_FUNCTION_BASE_VALUE;*/


    public static final String IS_LIST = "is_list";
    public static final int IS_LIST_TYPE = 204 + STEM_FUNCTION_BASE_VALUE;

    public static final String VALUES = "values";
    public static final int VALUES_TYPE = 208 + STEM_FUNCTION_BASE_VALUE;


    public static final String STAR = "star";
    public static final int STAR_TYPE = 209 + STEM_FUNCTION_BASE_VALUE;

    public static final String DIFF = "diff";
    public static final int DIFF_TYPE = 210 + STEM_FUNCTION_BASE_VALUE;

    // Conversions to/from JSON.
    public static final String TO_JSON = "to_json";
    public static final int TO_JSON_TYPE = 300 + STEM_FUNCTION_BASE_VALUE;

    public static final String FROM_JSON = "from_json";
    public static final int FROM_JSON_TYPE = 301 + STEM_FUNCTION_BASE_VALUE;

    public static final String JSON_PATH_QUERY = "query";
    public static final int JSON_PATH_QUERY_TYPE = 302 + STEM_FUNCTION_BASE_VALUE;

    public static final String DISPLAY = "print";
    public static final int DISPLAY_TYPE = 303 + STEM_FUNCTION_BASE_VALUE;

    /**
     * A list of the names that this Evaluator knows about. NOTE that this must be kept in sync
     * by the developer since it is used to determine if a function is built in or a user-defined function.
     */

    @Override
    public String[] getFunctionNames() {
        if (fNames == null) {
            fNames = new String[]{
                    //    MAP,
                    EXCISE,
                    HAS_KEYS,
                    DISPLAY,
                    DIFF,
                    STAR,
                    DIMENSION, RANK,
                    TRANSPOSE,
                    REMAP,
                    SIZE,
                    JOIN,
                    SHORT_MAKE_INDICES,
                    HAS_VALUE,
                    REMOVE,
                    SET_DEFAULT,
                    BOX,
                    UNBOX,
                    UNION,
                    FOR_EACH,
                    COMMON_KEYS,
                    EXCLUDE_KEYS,
                    LIST_KEYS,
                    ALL_KEYS,
                    HAS_KEY,
                    INCLUDE_KEYS,
                    RENAME_KEYS,
                    SHUFFLE,
                    MASK,
                    KEYS, VALUES,
                    IS_LIST,
                    UNIQUE_VALUES,
                    TO_JSON,
                    FROM_JSON, JSON_PATH_QUERY};
        }
        return fNames;
    }


    @Override
    public int getType(String name) {
        switch (name) {
          /*  case MAP:
                return MAP_TYPE;*/
            case EXCISE:
                return EXCISE_TYPE;
            case HAS_KEYS:
                return HAS_KEYS_TYPE;
            case DISPLAY:
                return DISPLAY_TYPE;
            case DIFF:
                return DIFF_TYPE;
            case STAR:
                return STAR_TYPE;
            case DIMENSION:
                return DIMENSION_TYPE;
            case RANK:
                return RANK_TYPE;
            case JOIN:
                return JOIN_TYPE;
            case SIZE:
                return SIZE_TYPE;
            case SET_DEFAULT:
                return SET_DEFAULT_TYPE;
            case HAS_VALUE:
                return HAS_VALUE_TYPE;
            case REMAP:
                return REMAP_TYPE;
            case MASK:
                return MASK_TYPE;
            case COMMON_KEYS:
                return COMMON_KEYS_TYPE;
            case KEYS:
                return KEYS_TYPE;
            case VALUES:
                return VALUES_TYPE;
            case LIST_KEYS:
                return LIST_KEYS_TYPE;
            case ALL_KEYS:
                return ALL_KEYS_TYPE;
            case HAS_KEY:
                return HAS_KEY_TYPE;
            case INCLUDE_KEYS:
                return INCLUDE_KEYS_TYPE;
            case EXCLUDE_KEYS:
                return EXCLUDE_KEYS_TYPE;
            case RENAME_KEYS:
                return RENAME_KEYS_TYPE;
            case SHUFFLE:
                return SHUFFLE_TYPE;
            case UNIQUE_VALUES:
                return UNIQUE_VALUES_TYPE;
            case IS_LIST:
                return IS_LIST_TYPE;
            case TRANSPOSE:
                return TRANSPOSE_TYPE;
            case SHORT_MAKE_INDICES:
                return MAKE_INDICES_TYPE;
            case REMOVE:
                return REMOVE_TYPE;
            case BOX:
                return BOX_TYPE;
            case UNBOX:
                return UNBOX_TYPE;

            case UNION:
                return UNION_TYPE;
            case TO_JSON:
                return TO_JSON_TYPE;
            case FROM_JSON:
                return FROM_JSON_TYPE;
            case JSON_PATH_QUERY:
                return JSON_PATH_QUERY_TYPE;
            case FOR_EACH:
                return FOR_EACH_TYPE;
        }
        return EvaluatorInterface.UNKNOWN_VALUE;
    }


    @Override
    public boolean dispatch(Polyad polyad, State state) {
        switch (polyad.getName()) {
        /*    case MAP:
                doMap(polyad, state);
                return true;*/
            case EXCISE:
                doExcise(polyad, state);
                return true;
            case HAS_KEYS:
                doHasKeys(polyad, state);
                return true;
            case DISPLAY:
                doDisplay(polyad, state);
                return true;
            case DIFF:
                doDiff(polyad, state);
                return true;
            case STAR:
                doStar(polyad, state);
                return true;
            case DIMENSION:
                doDimension(polyad, state);
                return true;
            case RANK:
                doRank(polyad, state);
                return true;
            case JOIN:
                doJoin(polyad, state);
                return true;
            case SIZE:
                doSize(polyad, state);
                return true;
            case SET_DEFAULT:
                doSetDefault(polyad, state);
                return true;
            case TRANSPOSE:
                doTransform(polyad, state);
                return true;
            case MASK:
                doMask(polyad, state);
                return true;
            case COMMON_KEYS:
                doCommonKeys(polyad, state);
                return true;
            case KEYS:
                doKeys(polyad, state);
                return true;
            case VALUES:
                doValues(polyad, state);
                return true;
            case LIST_KEYS:
                doListKeys(polyad, state);
                return true;
            case ALL_KEYS:
                doIndices(polyad, state);
                return true;
            case HAS_KEY:
                doHasKey(polyad, state);
                return true;
            case INCLUDE_KEYS:
                doIncludeKeys(polyad, state);
                return true;
            case HAS_VALUE:
                doIsMemberOf(polyad, state);
                return true;
            case EXCLUDE_KEYS:
                doExcludeKeys(polyad, state);
                return true;
            case RENAME_KEYS:
                doRenameKeys(polyad, state);
                return true;
            case SHUFFLE:
                doShuffle(polyad, state);
                return true;
            case UNIQUE_VALUES:
                doUniqueValues(polyad, state);
                return true;
            case IS_LIST:
                doIsList(polyad, state);
                return true;

            case REMAP:
                doRemap(polyad, state);
                return true;

            case SHORT_MAKE_INDICES:
                doMakeIndex(polyad, state);
                return true;
            case REMOVE:
                doRemove(polyad, state);
                return true;
            case BOX:
                doBox(polyad, state);
                return true;
            case UNBOX:
                doUnBox(polyad, state);
                return true;

            case UNION:
                doUnion(polyad, state);
                return true;
            case TO_JSON:
                doToJSON(polyad, state);
                return true;
            case FOR_EACH:
                doForEach(polyad, state);
                return true;
            case FROM_JSON:
                doFromJSON(polyad, state);
                return true;
            case JSON_PATH_QUERY:
                doJPathQuery(polyad, state);
                return true;
        }
        return false;
    }


    /**
     * Removes elements from a stem by value. In general stems this is not as useful as in lists.
     *
     * @param polyad
     * @param state
     */
    private void doExcise(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(EXCISE + " takes at least two arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }
        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(EXCISE + " takes at most three arguments", polyad.getArgAt(2));
        }
        QDLValue arg0 = polyad.evalArg(0, state);
        QDLValue arg1 = polyad.evalArg(1, state);
        boolean reorderLists = true; // default
        if (polyad.getArgCount() == 3) {
            QDLValue arg2 = polyad.evalArg(2, state);
            if (arg2.isBoolean()) {
                reorderLists =  arg2.asBoolean();
            } else {
                throw new BadArgException(EXCISE + " takes a boolean as its third argument", polyad.getArgAt(2));
            }

        }
        Collection<QDLValue> values;
        if (arg1.isStem()) {
            values = arg1.asStem().values();
        } else {
            values = new QDLSet();
            values.add(arg1);
        }
        if (arg0.isStem()) {
            QDLStem inStem = arg0.asStem();
            inStem.removeAllByValues(values, reorderLists);
            polyad.setResult(inStem);
            polyad.setEvaluated(true);
            return;
        } // end stem processing
        if (arg0.isSet()) {
            QDLSet set =  arg0.asSet();
            set.removeAll(values);
            polyad.setResult(set);
            polyad.setEvaluated(true);
            return;
        }
        throw new BadArgException("unknown type for " + EXCISE, polyad.getArgAt(0));
    }

    public static final String BOX_AROUND_RESULT = "box";
    public static final String BOX_UNICODE = "box_unicode";
    public static final String DISPLAY_INDENT = "indent";
    public static final String DISPLAY_ATTRIBUTES = "attr";
    public static final String DISPLAY_SHORT_FORM = "short";
    public static final String DISPLAY_SORT = "sort";
    public static final String DISPLAY_STRING_OUTPUT = "string_output";
    public static final String DISPLAY_WIDTH = "width";
    public static final String SHOW_KEYS = "show_keys";
    public static boolean DISPLAY_SHORT_FORM_DEFAULT = false; // display first line of value one
    public static boolean DISPLAY_SORT_DEFAULT = true;
    public static boolean DISPLAY_STRING_OUTPUT_DEFAULT = true;
    public static int DISPLAY_INDENT_DEFAULT = 0;
    public static int DISPLAY_WIDTH_DEFAULT = -1;

     /*
     q. :=     {'at_lifetime':900000, 'callback_uri':'["http://localhost/callback","http://localhost/callback2","http://localhost/callback3"]', 'client_id':'cilogon:/client_id/26e0aef76c6685473909b08c179cbc85', 'creation_ts':'2021-10-13T17:47:46.000Z', 'debug_on':false, 'df_interval':-1, 'df_lifetime':-1, 'email':'your.email@here.org', 'extended_attributes':'{"xoauth_attributes":{"grant_type":["refresh_token","urn:ietf:params:oauth:grant-type:device_code"]},"oidc-cm_attributes":{"comment":["This is a basic basic from the specification to test if a public client has been created correctly","Note that this also includes a refresh token lifetime parameter (rt_lifetime). Omitting this disables refresh tokens.","The lifetime is in seconds.","To create a public client, the scope must be \'openid\' and the auth method must be \'none\'."]}}', 'home_url':'', 'last_modified_ts':'2021-10-13T17:47:46.000Z', 'name':'Another test client', 'proxy_limited':false, 'public_client':true, 'public_key':'', 'rt_lifetime':2592000000, 'scopes':'["openid"]', 'sign_tokens':true, 'strict_scopes':true}
       format. := {'width':100, 'sort':true, 'short':false, 'string_output':true}
       display(q., format.)
      */

    /**
     * Format a stem or list
     * <br/>If a stem only, then format it with simple format.
     *
     * @param polyad
     * @param state
     */
    private void doDisplay(Polyad polyad, State state) {

        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            throw new MissingArgException(DISPLAY + " takes at least one arguments", polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(DISPLAY + " takes at most two arguments", polyad.getArgAt(2));
        }
        QDLValue arg0 = polyad.evalArg(0, state);
        if (arg0 == null) {
            if (polyad.getArgAt(0) instanceof VariableNode) {
                throw new QDLExceptionWithTrace("the variable named '" +
                        ((VariableNode) polyad.getArgAt(0)).getVariableReference() + "' was not found", polyad.getArgAt(0));
            }
            // It is possible to try and print a variable that has not been set.
            throw new BadArgException("value not found", polyad.getArgAt(0));
        }
        // defaults
        List<String> keySubset = null;
        boolean sortKeys = DISPLAY_SORT_DEFAULT;
        boolean multilineMode = !DISPLAY_SHORT_FORM_DEFAULT;
        int indent = DISPLAY_INDENT_DEFAULT;
        int width = DISPLAY_WIDTH_DEFAULT;
        boolean returnAsString = DISPLAY_STRING_OUTPUT_DEFAULT;
        boolean boxResult = false;
        boolean showKeys = true;
        boolean unicodeBoxChars = true;
        if (polyad.getArgCount() == 2) {
            QDLValue arg1 = polyad.evalArg(1, state);
            if (arg1.isLong()) {
                width = arg1.asLong().intValue();
            } else {
                if (arg1.isStem()) {
                    QDLStem c = arg1.asStem();
                    if(c.isList()){
                        // can be a list for the form [flag, flag, ... width] in any order
                        QDLList<? extends QDLValue> list = c.getQDLList();
                        for(QDLValue o : list){
                            if(o.isLong()){
                                width = o.asLong().intValue();
                                continue;
                            }
                            if(!(o.isString())){
                                throw new QDLExceptionWithTrace("illegal argument, must be a string or integer", polyad.getArgAt(1));
                            }
                            String key = o.asString();
                            boolean value = true;
                            if(key.startsWith("!") || key.startsWith("¬")){
                                value = false;
                                key = key.substring(1);
                            }
                            switch (key){
                                case BOX_AROUND_RESULT:
                                    boxResult = value;
                                    break;
                                case BOX_UNICODE:
                                    unicodeBoxChars = value;
                                    break;
                                case DISPLAY_SHORT_FORM:
                                    multilineMode = !value; // since option is actually backwards
                                    break;
                                case DISPLAY_SORT:
                                    sortKeys = value;
                                    break;
                                case DISPLAY_STRING_OUTPUT:
                                    returnAsString = value;
                                    break;
                                case SHOW_KEYS:
                                    showKeys = value;
                                    break;
                                case DISPLAY_INDENT:
                                case DISPLAY_ATTRIBUTES:
                                    // just ignore them
                                    break;
                                default:
                                    throw new BadArgException("unknown option ;" + o + "'", polyad.getArgAt(1));
                            }
                        }
                    }else {

                        for (QDLKey k : c.keySet()) {
                            switch (k.asString()) {
                                case BOX_AROUND_RESULT:
                                    boxResult = c.getBoolean(BOX_AROUND_RESULT);
                                    break;
                                case BOX_UNICODE:
                                    unicodeBoxChars = c.getBoolean(BOX_UNICODE);
                                    break;
                                case DISPLAY_INDENT:
                                    indent = c.getLong(DISPLAY_INDENT).intValue();
                                    break;
                                case DISPLAY_ATTRIBUTES:
                                    if (!c.get(DISPLAY_ATTRIBUTES).isStem()) {
                                        throw new BadArgException(DISPLAY_ATTRIBUTES + " in second argument to " + DISPLAY + " must be a list", polyad.getArgAt(1));
                                    }
                                    QDLStem zzz = c.get(DISPLAY_ATTRIBUTES).asStem();
                                    if (!zzz.isList()) {
                                        throw new BadArgException(DISPLAY_ATTRIBUTES + " in second argument to " + DISPLAY + " must be a list", polyad.getArgAt(1));
                                    }
                                    for(QDLValue vvv : zzz.getQDLList().values()){
                                        keySubset.add(vvv.asString()); // format utility eats strings
                                    }
                                    break;
                                case DISPLAY_SHORT_FORM:
                                    multilineMode = !c.getBoolean(DISPLAY_SHORT_FORM);
                                    break;
                                case DISPLAY_SORT:
                                    sortKeys = c.getBoolean(DISPLAY_SORT);
                                    break;
                                case DISPLAY_STRING_OUTPUT:
                                    returnAsString = c.getBoolean(DISPLAY_STRING_OUTPUT);
                                    break;
                                case DISPLAY_WIDTH:
                                    width = c.getLong(DISPLAY_WIDTH).intValue();
                                    break;
                                case SHOW_KEYS:
                                    showKeys = c.getBoolean(SHOW_KEYS);
                                    break;
                                default:
                                    throw new BadArgException("unknown option ;" + k + "'", polyad.getArgAt(1));
                            }
                        } //end for
                    }
                }
            }
        }
        List<String> list = null;
        if (arg0.isStem()) {
            QDLStem stem = arg0.asStem();

            Map map = new HashMap<>(); // Java values for formatting util
            for (QDLKey key : stem.keySet()) {
                map.put(key.getValue(), stem.get(key).getValue());
            }

            list = StringUtils.formatMap(map,
                    keySubset,
                    sortKeys,
                    multilineMode,
                    indent,
                    width,
                    false,
                    !showKeys); // don't let it try to turn random stems into JSON.
        } else {
            if (arg0.isString()) {
                String inString =  arg0.asString();
                int lineNumber = 0;
                if (-1 < inString.indexOf("\n")) {
                    StringTokenizer st = new StringTokenizer(inString, "\n");
                    int count = st.countTokens();
                    Double dColWidth = Math.log10(count * 1.0d) + 1;
                    int colWidth = dColWidth.intValue(); // if line numbering, how wide to make that column
                    list = new ArrayList<>(count);
                    while (st.hasMoreTokens()) {
                        if (showKeys) {
                            list.add(StringUtils.RJustify(Integer.toString(lineNumber++), colWidth) + " : " + st.nextToken());
                        } else {
                            list.add(st.nextToken());
                        }
                    }
                }else{
                    list = new ArrayList<>(1);
                    if (showKeys) {
                        list.add("0 : " + inString); // only a single number
                    } else {
                        list.add(inString);
                    }

                }
            } else {
                list = new ArrayList<>(1);
                if (showKeys) {
                    list.add("0 : " + arg0.toString()); // only a single number
                } else {
                    list.add(arg0.toString());
                }
            }
        }
        if (boxResult) {
            char leftTopCorner = '+';
            char rightTopCorner = '+';
            char leftBottomCorner = '+';
            char rightBottomCorner = '+';
            char rightSide = '|';
            char leftSide = '|';
            String horizontal = "-";
            if(unicodeBoxChars){
                leftTopCorner = '╔'; //u 2554
                rightTopCorner = '╗'; //u 2557
                leftBottomCorner = '╚'; // u 255A
                rightBottomCorner = '╝'; // u 255D
                rightSide = '║'; // u 2551
                leftSide = '║'; // u 2551
                horizontal = "═"; // u2550
            }
            int maxWidth = 0;
            List<String> boxedList = new LinkedList<>();
            for (String s : list) {
                List<String> tempList = StringUtils.stringToList(s);
                for (String t : tempList) {
                    maxWidth = Math.max(maxWidth, t.length());
                    boxedList.add(t);
                }
            }
            for (int i = 0; i < boxedList.size(); i++) {
                boxedList.set(i, leftSide + StringUtils.pad2(boxedList.get(i), maxWidth) + rightSide);
            }
            boxedList.add(0, leftTopCorner + StringUtils.hLine(horizontal, maxWidth) + rightTopCorner);
            boxedList.add(leftBottomCorner + StringUtils.hLine(horizontal, maxWidth) + rightBottomCorner);
            list = boxedList;
        }
        if (returnAsString) {
            String x = "";
            boolean firstPass = true;
            for (String y : list) {
                if (firstPass) {
                    x = y;
                    firstPass = false;
                } else {
                    x = x + "\n" + y;
                }
            }
            polyad.setEvaluated(true);
            polyad.setResult(x);
            return;
        }


        QDLStem out = new QDLStem();
        out.addList(list);
        polyad.setEvaluated(true);
        polyad.setResult(out);
    }

    /*
    Make a recursive version of this to format stems?  Problem is that truncate in StringUtils is designed to strip
    out embedded linefeeds, hence the formatting gets munged. Probably need another case for this.
     */
    protected String rFormatStem(QDLStem stem,
                                 List<String> keySubset,
                                 boolean sortKeys,
                                 boolean multilineMode,
                                 int indent,
                                 int width) {
        Map map = new HashMap<>();
        for (QDLKey key : stem.keySet()) {
            QDLValue vvv = stem.get(key);
            if (vvv.isStem()) {
                map.put(key.getValue(), rFormatStem(vvv.asStem(), keySubset, sortKeys, multilineMode, indent, width));
            } else {
                map.put(key.getValue(), vvv.getValue());
            }
        }

        List<String> list = StringUtils.formatMap(map,
                keySubset,
                sortKeys,
                multilineMode,
                indent,
                width);
        String x = "";
        boolean firstPass = true;
        for (String y : list) {
            if (firstPass) {
                x = y;
                firstPass = false;
            } else {
                x = x + "\n" + y;
            }
        }
        return x;

    }

    /*
     This should have a signature of
     diff(x.,y.{,true|false})
     returns a stem that compares each element of x. with y. (no depth!). Id
     last arg is true (default) then missing elements are rendered as null
     if false, then missing elements are ignored.
     */
    private void doDiff(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(DIFF + " takes at least two arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }
        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(DIFF + " takes at most three arguments", polyad.getArgAt(3));
        }
        boolean subsettingOn = true;
        QDLValue  arg0 = polyad.evalArg(0, state);
        QDLStem stem0;
        boolean arg0Scalar = false;
        if (arg0.isStem()) {
            stem0 =  arg0.asStem();
        } else {
            stem0 = new QDLStem();
            stem0.setDefaultValue(arg0);
            arg0Scalar = true;
        }

        QDLValue arg1 = polyad.evalArg(1, state);
        QDLStem stem1;
        boolean arg1Scalar = false;
        if (arg1.isStem()) {
            stem1 = arg1.asStem();
        } else {
            stem1 = new QDLStem();
            stem1.setDefaultValue(arg1);
            arg1Scalar = true;
        }
        if (arg0Scalar && arg1Scalar) {
            QDLStem out = new QDLStem();
            if (!arg0.equals(arg1)) {
                QDLStem r = new QDLStem();
                r.put(LongValue.Zero, asQDLValue(arg0));
                r.put(LongValue.One, asQDLValue(arg1));
                out.put(LongValue.Zero, asQDLValue(r)); // give it the right shape
            }
            polyad.setResult(out);
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 3) {
            QDLValue arg2 = polyad.evalArg(2, state);
            if (arg2.isBoolean()) {
                subsettingOn =  arg2.asBoolean();
            } else {
                throw new BadArgException("last argument to " + DIFF + " must be a boolean", polyad.getArgAt(2));
            }
        }

        StemKeys allkeys = stem1.keySet();
        allkeys.addAll(stem0.keySet());
        QDLStem out = new QDLStem();
        for (QDLKey key : allkeys) {
            QDLValue lArg = stem0.get(key);
            QDLValue rArg = stem1.get(key);
            if (subsettingOn) {
                // Java null means no such element.
                if (lArg == null || rArg == null) {
                    continue;
                }
            }
            // at most one of these is null we pick our way through this and only
            // create the list if needed so we don't end up with a bunch of unused
            // objects for very large stems.
            if (lArg == null) {
                if (!subsettingOn) {
                    QDLStem r = new QDLStem();
                    r.put(LongValue.Zero, QDLValue.getNullValue());
                    r.put(LongValue.One, rArg);
                    out.put(key, r);
                }
            } else {
                if (rArg == null) {
                    if (!subsettingOn) {
                        QDLStem r = new QDLStem();
                        r.put(LongValue.Zero, lArg);
                        r.put(LongValue.One, QDLValue.getNullValue());
                        out.put(key, r);
                    }

                } else {
                    // neither is a Java null, so NPE possible here
                    if (!lArg.equals(rArg)) {
                        QDLStem r = new QDLStem();
                        r.put(LongValue.Zero, lArg);
                        r.put(LongValue.One, rArg);
                        out.put(key, r);
                    }
                }
            }
        }

        polyad.setResult(out);
        polyad.setEvaluated(true);
    }

    private void doStar(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{0});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() != 0) {
            throw new MissingArgException(STAR + " takes no arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }
        polyad.setResult(AllIndicesValue.getAllIndicesValue());
        polyad.setEvaluated(true);
    }

    private void doRemap(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2, 3});
            polyad.setEvaluated(true);
            return;
        }
        QDLValue arg1 = polyad.evalArg(0, state);
        checkNull(arg1, polyad.getArgAt(0));
        if (!arg1.isStem()) {
            throw new BadArgException(REMAP + " requires stem as its first argument", polyad.getArgAt(0));
        }
        QDLStem stem = arg1.asStem();
        if (polyad.getArgCount() == 1) {
            // reverse keys and values
            QDLStem out = reverseKeysAndValues(stem);
            polyad.setResult(out);
            polyad.setEvaluated(true);
            return;
        }

        QDLValue arg2 = polyad.evalArg(1, state);
        checkNull(arg2, polyad.getArgAt(1));

        if (!arg2.isStem()) {
            throw new BadArgException(REMAP + " requires an stem or integer as its second argument", polyad.getArgAt(1));
        }
        QDLStem newIndices = null;
        if (polyad.getArgCount() == 3) {
            QDLValue arg3 = polyad.evalArg(2, state);
            checkNull(arg3, polyad.getArgAt(2), state);
            if (!arg3.isStem()) {
                throw new BadArgException(REMAP + " requires an stem or integer as its third argument", polyad.getArgAt(2));
            }

            newIndices = arg3.asStem();
            threeArgRemap(polyad, stem, arg2.asStem(), newIndices);
            return;
        }
        try {
            twoArgRemap(polyad, stem, arg2.asStem());
        } catch (IndexError indexError) {
            indexError.setStatement(polyad.getArgAt(1));
            throw indexError;
        }


    }

    protected QDLStem reverseKeysAndValues(QDLStem inStem) {
        QDLStem out = new QDLStem();
        for (QDLKey kk : inStem.keySet()) {
            QDLValue v = inStem.get(kk);
            if (v.isLong() || v.isString()) {
                out.put(QDLKey.from(v), kk);
            }
            if (isStem(v)) {
                out.put(kk, asQDLValue(reverseKeysAndValues(v.asStem())));
            }
        }
        return out;
    }

    protected void doIndices(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            throw new MissingArgException(ALL_KEYS + " requires at least one argument", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(ALL_KEYS + " requires at most two arguments", polyad.getArgAt(2));
        }

        QDLValue arg0 = polyad.evalArg(0, state);
        checkNull(arg0, polyad.getArgAt(0), state);
        if (!arg0.isStem()) {
            throw new BadArgException(ALL_KEYS + " requires a stem as its first argument", polyad.getArgAt(0));
        }
        boolean hasAxisExpression = arg0.isAxisRestriction();
        QDLStem stem;
        long axis = 0L;
        if (hasAxisExpression) {
            AxisExpression ae = arg0.asAxisExpression();
            stem = ae.getStem();
            if (ae.isStar()) {
                // In this case, the effect is to nullify the argument.
                // the user is asking for all axes.
                hasAxisExpression = false;
            } else {
                axis = ae.getAxis();
            }
        } else {
            stem = arg0.asStem();
        }
        boolean returnAll = true;
        if (!hasAxisExpression && polyad.getArgCount() == 2) {
            returnAll = false;
            QDLValue arg1 = polyad.evalArg(1, state);
            checkNull(arg1, polyad.getArgAt(1), state);
            if (!arg1.isLong()) {
                throw new BadArgException(ALL_KEYS + " requires the second argument be an integer if present.", polyad.getArgAt(1));
            }
            axis = arg1.asLong();
        }
        QDLStem rc = returnAll ? stem.indicesByRank() : stem.indicesByRank(axis);
        polyad.setResult(rc);
        polyad.setEvaluated(Boolean.TRUE);
    }

    protected void doValues(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() != 1) {
            throw new MissingArgException(VALUES + " requires an argument", polyad);
        }
        // create a list of values for a stem.
        QDLStem out = new QDLStem();
        QDLValue object0 = polyad.evalArg(0, state);
        checkNull(object0, polyad.getArgAt(0));
        QDLSet outSet;
        if (object0.isStem()) {
            QDLStem inStem =  object0.asStem();
            outSet = inStem.valueSet();
        } else {
            outSet = new QDLSet();
            outSet.add(asQDLValue(object0));
        }

        polyad.setResult(outSet);
        polyad.setEvaluated(true);
    }

    /**
     * Apply n-ary function to outer product of stems. There n stems passed in. The function is applied to each of them
     * as an outer product. So every element of every argument is evaluated.
     *
     * @param polyad
     * @param state
     */
    protected void doForEach(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(getBigArgList());
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            throw new MissingArgException(FOR_EACH + " requires at least 2 arguments", polyad);
        }
        // Fix for https://github.com/ncsa/qdl/issues/110 use local state here.
        State localState = state.newLocalState();
        //FunctionReferenceNodeInterface frn = getFunctionReferenceNode(state, polyad.getArgAt(0), true);
        FunctionReferenceNodeInterface frn = getFunctionReferenceNode(localState, polyad.getArgAt(0), true);
        if (polyad.getArgCount() == 1) {
            throw new MissingArgException(FOR_EACH + " requires at least 2 arguments", polyad.getArgAt(0));
        }
        QDLValue[] stems = new QDLValue[polyad.getArgCount() - 1];
        boolean allScalars = true;
        for (int i = 1; i < polyad.getArgCount(); i++) {
            //Object arg = polyad.evalArg(i, state);
            QDLValue arg = polyad.evalArg(i, localState);
            checkNull(arg, polyad.getArgAt(i));
            stems[i - 1] = arg;
            allScalars = allScalars && (!isStem(arg));
        }
        ExpressionImpl f;
        try {
            // One special case is they have defined an anonymous lambda with precisely 2 arguments
            // and want o apply this as a dyadic function to all arguments. Allow that explicitly.
            if (frn.isAnonymous() && frn.hasFunctionRecord(2)) {
                f = getOperator(localState, frn, 2);
            } else {
                // usual case: Just get the function based on the number of arguments.
                f = getOperator(localState, frn, stems.length);
            }
        } catch (UndefinedFunctionException ufx) {
            ufx.setStatement(polyad.getArgAt(0));
            // Other option -- might be trying to do a dyadic function
            throw ufx;
        }
        if (allScalars) {
            // Just skip the machinery below and evaluate it.
            //Object y = forEachEval(f, state, Arrays.asList(stems));
            Object y = forEachEval(f, localState, Arrays.asList(stems));
            polyad.setResult(y);
            polyad.setEvaluated(true);
            return;
        }
        QDLStem output = new QDLStem();

        // Fixes https://github.com/ncsa/qdl/issues/17
        //forEachRecursion2(output, f, state, stems, new IndexList(), new ArrayList(), 0);
        forEachRecursion2(output, f, localState, stems, new IndexList(), new ArrayList(), 0);
        polyad.setResult(output);
        polyad.setEvaluated(true);
    }


    /*
    for_each(@*, n(2,3, [;6]), n(3,4,[;12]+100))
    for_each(@*, 1+n(5), n(2,3, [;6]))
      for_each(@*, 1+n(5), 1+n(6))
                     for_each(@*, [1;5], 3)
                     @size∀['asd']
        ss(x,y,z)->x*y-z;
        for_each(@ss, [1;6], 4, [;5])
           g(x,y,n)->x^n+y^n
           @g∀[4,[;5],1]
           *Really good test function -- spits out the formatted indices:
        f(x,y,z)->x+'_' + y + '_' + z
        @f∀[[1,2],[3,4],[5,6]]

     */
    protected void forEachRecursion(QDLStem output,
                                    ExpressionImpl f,
                                    State state,
                                    QDLValue[] args,
                                    IndexList indexList,
                                    ArrayList<QDLValue> values,
                                    int currentIndex) {

        while (!isStem(args[currentIndex])) {
            values.add(args[currentIndex]); // we can add scalars to the end of this, but it will recurse on the next stem
            currentIndex++;
            if (currentIndex == args.length) {
                // end of the line for recursion. Evaluate
                output.set(indexList, asQDLValue(forEachEval(f, state, values)));
                return;
            }
        }
        QDLStem currentStem = args[currentIndex++].asStem();
        // Next, get *every* index
        ArrayList<QDLValue> allIndices = currentStem.indicesByRank().getQDLList().getArrayList();
        for (QDLValue index : allIndices) {
            IndexList currentIndexList = new IndexList(index.asStem()); // index looks like [0,0,1]
            IndexList nextIndexList = new IndexList(); // index looks like [0,0,1]
            nextIndexList.addAll(indexList);
            nextIndexList.addAll(currentIndexList);
            ArrayList<QDLValue> valuesList1 = new ArrayList();
            valuesList1.addAll(values);
            //     valuesList1.add(currentStem.get(currentIndexList, true).get(0));
            valuesList1.add(currentStem.get(index));
            if (currentIndex == args.length) {
                // end of the line for recursion. Evaluate
                output.set(nextIndexList, asQDLValue(forEachEval(f, state, valuesList1)));
            } else {
                forEachRecursion(output, f, state, args, nextIndexList, valuesList1, currentIndex);
            }
        }
    }

    protected void forEachRecursion2(QDLStem output,
                                     ExpressionImpl f,
                                     State state,
                                     QDLValue[] args,
                                     IndexList indexList,
                                     ArrayList<QDLValue> values,
                                     int currentIndex) {

        while (!isStem(args[currentIndex])) {
            values.add(args[currentIndex]); // we can add scalars to the end of this, but it will recurse on the next stem
            currentIndex++;
            if (currentIndex == args.length) {
                // end of the line for recursion. Evaluate
                output.set(indexList, asQDLValue(forEachEval(f, state, values)));
                return;
            }
        }
        QDLStem currentStem;
        Long axis = 0L;
        ArrayList<QDLValue> allIndices;
        boolean isAxisExpression = args[currentIndex].isAxisRestriction();
        AxisExpression ae = null;
        if (isAxisExpression) {
            ae = args[currentIndex].asAxisExpression();
        }

        if (isAxisExpression && !ae.isStar()) {
            // star for axis is same as ignoring it.
            currentStem = ae.getStem();
            axis = ae.getAxis();
            //allIndices = currentStem.indicesByRank(axis+1).getQDLList().getArrayList();
            allIndices = currentStem.keysByAxis(axis).getQDLList().getArrayList();
        } else {
            currentStem = args[currentIndex].asStem();
            allIndices = currentStem.indicesByRank().getQDLList().getArrayList();
        }
        currentIndex++;


        for (QDLValue index : allIndices) {
            IndexList currentIndexList = new IndexList(index.asStem()); // index looks like [0,0,1]
            IndexList nextIndexList = new IndexList(); // index looks like [0,0,1]
            nextIndexList.addAll(indexList);
            nextIndexList.addAll(currentIndexList);
            ArrayList<QDLValue> valuesList1 = new ArrayList();
            valuesList1.addAll(values);
            //     valuesList1.add(currentStem.get(currentIndexList, true).get(0));
            valuesList1.add(currentStem.get(index));
            if (currentIndex == args.length || (isAxisExpression && args.length == axis)) {
                // end of the line for recursion. Evaluate
                output.set(nextIndexList, asQDLValue(forEachEval(f, state, valuesList1)));
            } else {
                forEachRecursion2(output, f, state, args, nextIndexList, valuesList1, currentIndex);
            }
        }
    }

    // Processor that replaces each stem at a given level with the constant "foo".
    public static class ARForEachImpl extends AxisRestrictionIdentity {
        public ARForEachImpl(ExpressionImpl f, State state, int axis) {
            this.f = f;
            this.state = state;
            this.axis = axis;
        }

        ExpressionImpl f;
        State state;

        @Override
        public QDLValue getDefaultValue(List<Object> index, Object key, Object value) {
            ArgList argList1 = new ArgList();
            argList1.add(new ConstantNode(asQDLValue(value)));
            f.setArguments(argList1);
            return f.evaluate(state);
        }
    }
/*
f(x.)→x.0+x.1;
@f∀[n(3,4,4,[;3*4*4])|0]
 */

    protected QDLValue forEachEval(ExpressionImpl f, State state, List<QDLValue> args) {
        if (f instanceof Monad) {
            if (args.size() != 1) {
                throw new BadArgException("cannot apply monad to multiple arguments", f);
            }
        }
        // Fix https://github.com/ncsa/qdl/issues/38

        ExpressionImpl dd = null;
        boolean isDyad = false;
        if (f instanceof Dyad) {
            //       f(x,y)→x*y;
            //2@f∀[[1;5],[2;6],[-1;2]]
            // have to apply pairwise
            dd = (Dyad) f;
            isDyad = true;

        }
        if (f instanceof UserFunction) {
            UserFunction userFunction = (UserFunction) f;
            if (userFunction.getFunctionRecord().getArgCount() == 2) {
                dd = userFunction;
                isDyad = true;
            }
        }
        if (isDyad) {
            dd.addArgument(new ConstantNode(asQDLValue(args.get(0))));
            dd.addArgument(new ConstantNode(asQDLValue(args.get(1))));
            QDLValue out = dd.evaluate(state);
            for (int i = 2; i < args.size(); i++) {
                dd.getArguments().set(0, new ConstantNode(dd.getResult()));
                dd.getArguments().set(1, new ConstantNode(asQDLValue(args.get(i))));
                out = dd.evaluate(state);
            }
            dd.getArguments().clear(); // so the next iteration does not have cruft.
            return out;

        }
        ArgList argList1 = new ArgList();
        for (QDLValue arg : args) {
            argList1.add(new ConstantNode(asQDLValue(arg)));
        }
        f.setArguments(argList1);
        return f.evaluate(state);

    }

    protected void doJPathQuery(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(JSON_PATH_QUERY + " requries at least 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }
        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(JSON_PATH_QUERY + " accepts at most 3 arguments", polyad.getArgAt(3));
        }
        QDLValue arg0 = polyad.evalArg(0, state);
        checkNull(arg0, polyad.getArgAt(0));

        if (!arg0.isStem()) {
            throw new BadArgException(JSON_PATH_QUERY + " requires a stem as its first argument", polyad.getArgAt(0));
        }
        QDLStem stemVariable = arg0.asStem();
        QDLValue arg1 = polyad.evalArg(1, state);
        checkNull(arg1, polyad.getArgAt(1));

        if (!arg1.isString()) {
            throw new BadArgException(JSON_PATH_QUERY + " requires a string as its second argument", polyad.getArgAt(1));
        }

        String query = arg1.asString();
        Configuration conf = null;
        boolean returnAsPaths = false;
        if (polyad.getArgCount() == 3) {
            QDLValue arg2 = polyad.evalArg(2, state);
            checkNull(arg2, polyad.getArgAt(2));

            if (!arg2.isBoolean()) {
                throw new BadArgException(JSON_PATH_QUERY + " requires a boolean as its third argument", polyad.getArgAt(2));
            }
            returnAsPaths = arg2.asBoolean();
            conf = Configuration.builder()
                    .options(Option.AS_PATH_LIST).build();
        }
        String output;
        QDLStem outStem;

        if (returnAsPaths) {
            try {
                output = JsonPath.using(conf).parse(stemVariable.toJSON().toString()).read(query).toString();
                outStem = stemPathConverter(output);
            } catch (JsonPathException jpe) {
                if (jpe.getMessage().contains("No results")) {
                    // A "feature" of this API is to return an empty list if there are no values
                    // but throw a path exception if you want the results as paths.
                    // Only way to check is to look at the message. 
                    outStem = new QDLStem();
                } else {
                    throw new BadArgException("error processing query:" + jpe.getMessage(), polyad);
                }
            }
        } else {
            try {
                conf = Configuration.builder()
                        .options(Option.ALWAYS_RETURN_LIST).build();
                // This type of query returns the values of the query, not the indices, so
                // we just have to convert it to a stem and return that. Handles the couple cases
                // of a JSON array vs object. The JsonPath generally tends to return arrays so we
                // test for that first.
                output = JsonPath.using(conf).parse(stemVariable.toJSON().toString()).read(query).toString();
                outStem = new QDLStem();
                try {
                    JSONArray array = JSONArray.fromObject(output);
                    outStem.fromJSON(array);
                } catch (JSONException x) {
                    JSONObject jo = JSONObject.fromObject(output);
                    outStem.fromJSON(jo);
                }

            } catch (JsonPathException jpe) {
                throw new BadArgException("error processing query:" + jpe.getMessage(), polyad);
            }
        }
        polyad.setResult(outStem);
        polyad.setEvaluated(true);
    }


    /**
     * Convert output of a JSON query to a stem of lists. Each list is an
     * index entry.
     *
     * @param indexList
     * @return
     */
    protected QDLStem stemPathConverter(String indexList) {
        JSONArray arrayIn = JSONArray.fromObject(indexList);
        QDLStem arrayOut = new QDLStem();
        for (int i = 0; i < arrayIn.size(); i++) {
            String x = arrayIn.getString(i);
            x = x.substring(2); // All JSON paths start with a $.
            StringTokenizer tokenizer = new StringTokenizer(x, "[");
            QDLStem r = new QDLStem();
            while (tokenizer.hasMoreTokens()) {
                String nextOne = tokenizer.nextToken();
                if (nextOne.startsWith("'")) {
                    nextOne = nextOne.substring(1);
                }
                nextOne = nextOne.substring(0, nextOne.length() - 1);
                if (nextOne.endsWith("'")) {
                    nextOne = nextOne.substring(0, nextOne.length() - 1);
                }
                r.listAdd(asQDLValue(nextOne));
            }
            arrayOut.put(i, asQDLValue(r));

        }
        return arrayOut;
    }


    private void doRank(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            throw new MissingArgException(RANK + " requires an argument", polyad);
        }
        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(RANK + " requires a single argument", polyad.getArgAt(1));
        }

        if (!isStem(polyad.evalArg(0, state))) {
            polyad.setEvaluated(true);
            polyad.setResult(0L);
            return;
        }
        polyad.setEvaluated(true);
        QDLStem s = polyad.getArgAt(0).getResult().asStem();
        polyad.setResult(s.getRank());
    }

    private void doDimension(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            throw new MissingArgException(DIMENSION + " requires an argument", polyad);
        }
        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(DIMENSION + " requires a single argument", polyad.getArgAt(1));
        }
        if (!isStem(polyad.evalArg(0, state))) {
            polyad.setEvaluated(true);
            polyad.setResult(0L);
            return;
        }
        // so its a stem
        polyad.setEvaluated(true);
        QDLStem s = polyad.getArgAt(0).getResult().asStem();
        polyad.setResult(s.dim());

    }

    private void doUniqueValues(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException("the " + UNIQUE_VALUES + " function requires 1 argument", polyad);
        }
        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException("the " + UNIQUE_VALUES + " function requires at most 1 argument", polyad.getArgAt(1));
        }


        QDLValue arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0));

        if (!isStem(arg)) {
            polyad.setResult(new QDLStem()); // just an empty stem
            polyad.setEvaluated(true);
            return;
        }
        QDLStem stemVariable = arg.asStem();
        QDLStem out = stemVariable.almostUnique().almostUnique();

        polyad.setResult(out);
        polyad.setEvaluated(true);
    }


    /**
     * Compute if the left argument is a member of the right argument. result is always conformable to the left argument.
     *
     * @param polyad
     * @param state
     */
    protected void doIsMemberOf(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() != 2) {
            throw new WrongArgCountException(HAS_VALUE + " requires 2 arguments.", polyad);
        }
        QDLValue  leftArg = polyad.evalArg(0, state);
        checkNull(leftArg, polyad.getArgAt(0));

        QDLValue rightArg = polyad.evalArg(1, state);
        checkNull(rightArg, polyad.getArgAt(1));
        // breaks  down tidily in to 4 cases.
        if (leftArg.isStem()) {
            QDLStem lStem = leftArg.asStem();
            QDLStem result = new QDLStem(); // result is always conformable to left arg

            if (rightArg.isStem()) {
                QDLStem rStem = rightArg.asStem();
                for (QDLKey lkey : lStem.keySet()) {
                    result.put(lkey, asQDLValue(rStem.hasValue(lStem.get(lkey))));
                }
            } else {
                if (rightArg.isSet()) {
                    QDLSet<? extends QDLValue> rSet =  rightArg.asSet();
                    for (QDLKey key : lStem.keySet()) {
                        QDLValue ooo = lStem.get(key);
                        if (ooo.isDecimal()) {
                            result.put(key, asQDLValue(Boolean.FALSE));
/*
                            if (keyIsLong) {
                                result.put((Long) key, asQDLValue(Boolean.FALSE));
                            } else {
                                result.put((String) key, asQDLValue(Boolean.FALSE));
                            }
*/
                            for (QDLValue element : rSet) {
                                if (element.isDecimal()) {
                                    boolean tempB = bdEquals(ooo.asDecimal(), element.asDecimal());
                                    if (tempB) {
                                        result.put(key, BooleanValue.True);
                                        /*if (keyIsLong) {
                                            result.put((Long) key, BooleanValue.True);
                                        } else {
                                            result.put((String) key, BooleanValue.True);
                                        }*/
                                        break;
                                    }
                                }
                            }

                        } else {
                            result.put(key, new BooleanValue(rSet.contains(ooo)));
/*
                            if (keyIsLong) {
                                result.put((Long) key, new BooleanValue(rSet.contains(ooo)));
                            } else {
                                result.put((String) key, new BooleanValue(rSet.contains(ooo)));
                            }
*/
                        }
                    }
                } else {
                    // check if each element in the left stem matches the value of the right arg.
                    for (QDLKey lKey : lStem.keySet()) {
                        result.put(lKey, asQDLValue(lStem.get(lKey).equals(rightArg) ? Boolean.TRUE : Boolean.FALSE)); // got to finagle these are right Java objects
                    }
                }
            }
            polyad.setResult(result);
        } else {
            // left arg is not a stem.
            Boolean result = Boolean.FALSE;
            if (rightArg.isStem()) {
                QDLStem rStem = rightArg.asStem();
                result = rStem.hasValue(asQDLValue(leftArg));
            } else {
                if (rightArg.isSet()) {
                    if (leftArg.isDecimal()) {
                        // much slower, but there is no other way to compare big decimals.
                        QDLSet<? extends QDLValue> qdlSet = rightArg.asSet();
                        result = false;
                        for (QDLValue element : qdlSet) {
                            if (element.isDecimal()) {
                                boolean tempB = bdEquals(leftArg.asDecimal(), element.asDecimal());
                                if (tempB) {
                                    result = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        result = rightArg.asSet().contains(leftArg);
                    }
                } else {
                    result = leftArg.equals(rightArg);
                }
            }
            polyad.setResult(result);
        }
        polyad.setEvaluated(true);
    }


    protected void doFromJSON(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            throw new MissingArgException(FROM_JSON + " requires an argument", polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(FROM_JSON + " takes at most two arguments", polyad.getArgAt(2));
        }
        QDLValue arg0 = polyad.evalArg(0, state);
        checkNull(arg0, polyad.getArgAt(0));
        boolean convertKeys = false;
        int converterType = -1;
        if (polyad.getArgCount() == 2) {
            QDLValue arg1 = polyad.evalArg(1, state);
            checkNull(arg1, polyad.getArgAt(2));
            if (!arg1.isLong()) {
                throw new BadArgException(FROM_JSON + " requires an integer boolean as its second argument, if present.", polyad.getArgAt(1));
            }
            convertKeys = true;
            converterType = arg1.asLong().intValue();
        }
        JSONObject jsonObject = null;
        QDLStem output = new QDLStem();
        boolean gotOne = false;
        if (arg0.isStem()) {
            gotOne = true;
            QDLStem stem =  arg0.asStem();
            for (QDLKey key : stem.keySet()) {
                QDLValue value = stem.get(key);
                if (!value.isString()) {
                    continue;
                }
                System.out.println("[StemEvaluator.dofromJSON] " + key + " -> " + value);
                try {
                    QDLStem nextStem = new QDLStem();
                    jsonObject = JSONObject.fromObject(value);
                    nextStem.fromJSON(jsonObject, convertKeys, converterType);
                    output.put(key, nextStem);
/*
                    if (key instanceof Long) {
                        output.put((Long) key, asQDLValue(nextStem));
                    } else {
                        output.put((String) key, asQDLValue(nextStem));
                    }
*/
                } catch (Throwable tt) {
                    // ok, so this is not valid JSON, rock on
                    //throw new BadArgException(FROM_JSON + " could not parse the argument as valid JSON", polyad.getArgAt(0));
                }

            }
        }

        if (arg0.isString()) {
            gotOne = true;
            try {
                jsonObject = JSONObject.fromObject(arg0.asString());
                output.fromJSON(jsonObject, convertKeys, converterType);
            } catch (Throwable t) {
                try {
                    JSONArray array = JSONArray.fromObject(arg0.asString());
                    output.fromJSON(array, convertKeys, converterType);
                } catch (Throwable tt) {
                    // ok, so this is not valid JSON. Constrcut error message with first exception since that
                    // is more apt to be correct.
                    throw new BadArgException(FROM_JSON + " could not parse the argument as valid JSON: " +
                            tt.getMessage().substring(0, Math.min(100, tt.getMessage().length())), polyad.getArgAt(0));
                }
            }
        }
        if (!gotOne) {
            throw new BadArgException(FROM_JSON + " requires a string or stem of strings as its first argument", polyad.getArgAt(0));
        }

        /*
        {'$2E':'a','$2E$2E':'b','$2E$2E$2Ec$2E$2E':'c'}
         */
        polyad.setResult(output);
        polyad.setEvaluated(true);

    }

    protected void doToJSON(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (0 == polyad.getArgCount()) {
            throw new MissingArgException(TO_JSON + " requires an argument", polyad);
        }
        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(TO_JSON + " takes at most 3 arguments", polyad.getArgAt(3));
        }
        QDLValue  arg0 = polyad.evalArg(0, state);
        checkNull(arg0, polyad.getArgAt(0));

        if (!arg0.isStem()) {
            throw new BadArgException(TO_JSON + " requires a stem as its first argument", polyad.getArgAt(0));
        }
        int indent = -1;
        boolean convertNames = false;
        int conversionAlgorithm = 0; // default v-encode
        /*
        Two args means the second is either a boolean for conversion or it an  int as the indent factor.
         */
        if (polyad.getArgCount() == 2) {
            QDLValue arg1 = polyad.evalArg(1, state);
            checkNull(arg1, polyad.getArgAt(1));

            if (arg1.isLong()) {
                Long argL =  arg1.asLong();
                indent = argL.intValue(); // best we can do
            } else {
                throw new BadArgException(TO_JSON + " requires an integer  as its second argument", polyad.getArgAt(1));
            }
        }
        /*
        3 arguments:  2nd = indent, 3rd = type of conversion
         */
        if (polyad.getArgCount() == 3) {
            convertNames = true;
            QDLValue arg1 = polyad.evalArg(1, state);
            checkNull(arg1, polyad.getArgAt(1));
            if (arg1.isLong()) { // contract true = v-encode, false means no encode
                indent = arg1.asLong().intValue(); // best we can do
            } else {
                throw new BadArgException(TO_JSON + " with 3 arguments requires an integer as its second argument", polyad.getArgAt(1));
            }

            QDLValue  arg2 = polyad.evalArg(2, state);
            checkNull(arg2, polyad.getArgAt(2));
            if (!arg2.isLong()) {
                throw new BadArgException(TO_JSON + " requires an integer as its third argument", polyad.getArgAt(2));
            }
            Long argL = arg2.asLong();
            conversionAlgorithm = argL.intValue(); // best we can do
        }

        JSON j = arg0.asStem().toJSON(convertNames, conversionAlgorithm);
        if (0 < indent) {
            polyad.setResult(j.toString(indent));
        } else {
            polyad.setResult(j.toString());
        }
        polyad.setEvaluated(true);
    }


    /**
     * Do a union of stems.
     *
     * @param polyad
     * @param state
     */
    private void doUnion(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(getBigArgList());
            polyad.setEvaluated(true);
            return;
        }
        if (0 == polyad.getArgCount()) {
            throw new MissingArgException("the " + UNION + " function requires at least 1 argument", polyad);
        }
        QDLStem outStem = new QDLStem();

        for (int i = 0; i < polyad.getArgCount(); i++) {
            QDLValue arg = polyad.evalArg(i, state);
            checkNull(arg, polyad.getArgAt(i));
            if (!isStem(arg)) {
                throw new BadArgException(UNION + " only works on stems.", polyad.getArgAt(i));
            }
            outStem = outStem.union(arg.asStem());
        }
        polyad.setResult(outStem);
        polyad.setEvaluated(true);
    }

    private void doUnBox(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException("the " + UNBOX + " function requires  at least 1 argument", polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(UNBOX + " takes at most two arguments", polyad.getArgAt(2));
        }
        QDLValue  arg0 = polyad.evalArg(0, state);
        checkNull(arg0, polyad.getArgAt(0));

        // should take either a stem or a variable reference to it.
        QDLStem stem = null;
        Boolean safeMode = Boolean.TRUE;
        if (polyad.getArgCount() == 2) {
            QDLValue o = polyad.evalArg(1, state);
            checkNull(o, polyad.getArgAt(1));

            if (!o.isBoolean()) {
                throw new BadArgException("The second argument of " + UNBOX + " must be a boolean.", polyad.getArgAt(1));
            }
            safeMode =  o.asBoolean();
        }
        String varName = null;
        if (polyad.getArgAt(0) instanceof VariableNode) {
            VariableNode vn = (VariableNode) polyad.getArgAt(0);
            varName = vn.getVariableReference();
            if (!(vn.getResult().isStem())) {
                throw new BadArgException("You can only apply " + UNBOX + " to a stem.", polyad.getArgAt(0));
            }
            stem = vn.getResult().asStem();
        }
        if (polyad.getArgAt(0) instanceof StemVariableNode) {
            stem = polyad.evalArg(0, state).asStem();
        }

        if ((polyad.getArgAt(0) instanceof QDLStem)) {
            stem = (QDLStem) polyad.getArgAt(0);
        }
        if (stem == null) {
            throw new BadArgException("You can only apply " + UNBOX + " to a stem.", polyad.getArgAt(0));
        }
        if (stem.getQDLList().size() != 0) {
            throw new BadArgException("You can only apply " + UNBOX + " to a stem without a list.", polyad.getArgAt(0));
        }
        // Make a safe copy of the state to unpack this in case something bombs
        List<String> keys = new ArrayList<>();
        State localState = state.newCleanState();
        MetaCodec codec = new MetaCodec();

        for (QDLKey k : stem.keySet()) {
            // implicit in contract that all the keys are string, not integers
            //String key = (String) k;
            QDLValue  ob = stem.get(k);
            String key = k.asString() + (ob.isStem() ? STEM_INDEX_MARKER : "");
            if (safeMode) {
                if (!pattern.matcher(key).matches()) {
                    key = codec.encode(key);
                }
                if (state.isDefined(key)) {
                    throw new IllegalArgumentException("name clash in safe mode for '" + key + "'");
                }
            } else {
                if (!pattern.matcher(key).matches()) {
                    throw new IllegalArgumentException("the variable name '" + key + "' is not a legal variable name.");
                }
            }
            keys.add(key);
            localState.setValue(key, ob);
        }
        // once all is said and done and none of this bombed copy it. That way we don't leave the actual state in disarray
        for (String key : keys) {
            state.setValue(key, localState.getValue(key));
        }
        if (varName != null) {
            state.remove(varName);
        }
        polyad.setResult(Boolean.TRUE);
        polyad.setEvaluated(true);
    }

    Pattern pattern = Pattern.compile(var_regex);

    /**
     * Take a collection of variables and stem them up, removing them from the symbol table.
     *
     * @param polyad
     * @param state
     */

    private void doBox(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(getBigArgList());
            polyad.setEvaluated(true);
            return;
        }
        if (0 == polyad.getArgCount()) {
            throw new MissingArgException("the " + BOX + " function requires at least 1 argument", polyad);
        }
        ArrayList<String> varNames = new ArrayList<>();
        QDLStem stem = new QDLStem();

        for (int i = 0; i < polyad.getArgCount(); i++) {
            polyad.evalArg(i, state);
            if (!(polyad.getArgAt(i) instanceof VariableNode)) {
                throw new BadArgException(BOX + " requires a list of variables.", polyad.getArgAt(i));
            }
            VariableNode vn = (VariableNode) polyad.getArgAt(i);
            varNames.add(vn.getVariableReference());
            stem.put(vn.getVariableReference(), vn.getResult());
        }
        for (String varName : varNames) {
            state.remove(varName);
        }
        polyad.setResult(stem);
        polyad.setEvaluated(true);
    }

    protected void doSize(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException("the " + SIZE + " function requires 1 argument", polyad);
        }
        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException("the " + SIZE + " function requires 1 argument", polyad.getArgAt(1));
        }
        QDLValue arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0));

        long size = 0;
        if (arg.isSet()) {
            size = arg.asSet().size();
        }
        // Do https://github.com/ncsa/qdl/issues/108
        if (arg.isAxisRestriction()) {
            // get size along an axis
            AxisExpression ae = arg.asAxisExpression();
            Long count = ae.getStem().size(ae.isStar() ? ALL_AXES : ae.getAxis().intValue());
            polyad.setResult(count);
            polyad.setEvaluated(true);
            return;
        }
        if (arg.isStem()) {
            size = arg.asStem().size();
        }
        if (arg.isString()) {
            size = arg.asString().length();
        }
        polyad.setResult(size);
        polyad.setEvaluated(true);
    }

    public static class SizeOf extends AxisRestrictionIdentity {
        Long out = 0L;

        public SizeOf(int axis) {
            this.axis = axis;
        }

        @Override
        public Object getDefaultValue(List<Object> index, Object key, Object value) {
            return super.getDefaultValue(index, key, value);
        }

        @Override
        public Object process(List index, Object key, QDLStem value) {
            out++;
            return value.size();
        }

        @Override
        public Object process(List index, Object key, Long value) {
            out++;
            return value;
        }
    }

    static final int all_keys = 0;
    static final int only_stems = 1;
    static final int only_scalars = 2;

    /**
     * Returns the keys in a stem as a list, filtering if wanted.
     *
     * @param polyad
     * @param state
     */
    protected void doListKeys(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            throw new MissingArgException(LIST_KEYS + " requires at least one argument", polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(LIST_KEYS + " takes at most 2 arguments", polyad.getArgAt(2));
        }
        QDLValue arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0));

        int returnScope = all_keys;
        int returnType = UNKNOWN_TYPE;
        boolean returnByType = false;
        if (polyad.getArgCount() == 2) {
            QDLValue arg2 = polyad.evalArg(1, state);
            checkNull(arg2, polyad.getArgAt(1));

            if (arg2.isBoolean()) {
                returnByType = false;
                if (arg2.asBoolean()) {
                    returnScope = only_scalars;
                } else {
                    returnScope = only_stems;
                }

            } else if (arg2.isLong()) {
                returnByType = true;
                returnType = arg2.asLong().intValue();
            } else {
                throw new BadArgException(LIST_KEYS + " second argument must be a boolean or integer if present.", polyad.getArgAt(1));
            }
        }
        long size = 0;
        if (!arg.isStem()   ) {
            polyad.setResult(new StemValue()); // just an empty stem
            polyad.setEvaluated(true);
            return;
        }
        QDLStem stemVariable =  arg.asStem();
        QDLStem out = new QDLStem();
        if (returnByType) {
            long i = 0L;
            for (QDLKey key : stemVariable.keySet()) {
                if (returnType == Constant.getType(stemVariable.get(key))) {
                    out.put(QDLKey.from(i++), asQDLValue(key));
                }
            }
        } else {
            long i = 0L;

            for (QDLKey key : stemVariable.keySet()) {
                QDLValue qdlValue= null;
                switch (returnScope) {
                    case all_keys:
                        //out.put(QDLKey.from(i++), asQDLValue(key));
                        qdlValue = asQDLValue(key);
                        break;
                    case only_scalars:
                        if (Constant.getType(stemVariable.get(key)) != STEM_TYPE) {
                            //out.put(QDLKey.from(i++), asQDLValue(key));
                            qdlValue =  asQDLValue(key);
                        }
                        break;
                    case only_stems:
                        if (Constant.getType(stemVariable.get(key)) == STEM_TYPE) {
                            //out.put(i++, asQDLValue(key));
                            qdlValue  = asQDLValue(key);
                        }
                        break;
                }
                if(qdlValue != null) {
                    out.put(QDLKey.from(i++), qdlValue);
                }
            }

        }
        polyad.setResult(out);
        polyad.setEvaluated(true);
    }

    /**
     * Return a stem of nothing key the keys, possibly filtering, so the final stem is of the form
     * <pre>
     *     {key0=key0,key1=key1,...}
     * </pre>
     * This is useful in conjunction with the rename keys call, so you can get the keys and do some
     * operations on them then rename the keys in the original stem.
     *
     * @param polyad
     * @param state
     */
    protected void doKeys(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(KEYS + " requires at least 1 argument", polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(KEYS + " takes at most two arguments", polyad.getArgAt(2));
        }
        QDLValue arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0));
        if (!isStem(arg)) {
            polyad.setResult(new QDLStem()); // just an empty stem
            polyad.setEvaluated(true);
            return;
        }
        int returnScope = all_keys;
        int returnType = UNKNOWN_TYPE;
        boolean returnByType = false;
        if (polyad.getArgCount() == 2) {
            QDLValue arg2 = polyad.evalArg(1, state);
            checkNull(arg2, polyad.getArgAt(1));

            if (arg2.isBoolean()) {
                returnByType = false;
                if (arg2.asBoolean()) {
                    returnScope = only_scalars;
                } else {
                    returnScope = only_stems;
                }

            } else if (arg2.isLong()) {
                returnByType = true;
                Long arg2Long = arg2.asLong();
                returnType = arg2Long.intValue();
            } else {
                throw new BadArgException(LIST_KEYS + " second argument must be a boolean or integer if present.", polyad.getArgAt(1));
            }
        }
        QDLStem stemVariable = arg.asStem();

        QDLStem out = new QDLStem();

        if (returnByType) {
            for (QDLKey key : stemVariable.keySet()) {
                if (returnType == Constant.getType(stemVariable.get(key))) {
                //    putLongOrStringKey(out, key);
                    out.put(key, key);
                }
            }
        } else {
            for (QDLKey key : stemVariable.keySet()) {
                switch (returnScope) {
                    case all_keys:
//                        putLongOrStringKey(out, key);
                        out.put(key, key);
                        break;
                    case only_scalars:
                        if (stemVariable.get(key).getType() != STEM_TYPE) {
                            out.put(key, key);
                       //     putLongOrStringKey(out, key);
                        }
                        break;
                    case only_stems:
                        if (stemVariable.get(key).getType() == STEM_TYPE) {
                            out.put(key, key);
                            //putLongOrStringKey(out, key);
                        }
                        break;
                }
            }
        }
        polyad.setResult(out);
        polyad.setEvaluated(true);
    }

    /**
     * Needed by list_keys and keys so that returned indices that are longs are indeed longs.
     * Otherwise everything returned would be a string (e.g., '0' not 0) which makes subsequent
     * algebraic operations  on them fail.
     *
     * @param out
     * @param key
     */
/*    protected void putLongOrStringKey(QDLStem out, Object key) {
        if (key instanceof Long) {
            Long k = (Long) key;
            out.put(k, asQDLValue(k));
        } else {
            if(key instanceof QDLValue) {
                putLongOrStringKey(out, ((QDLValue) key).getValue());
                return;
            }
            String k = (String) key;
            out.put(k, asQDLValue(k));
        }
    }*/

    /**
     * This is not left conformable and any uses should be removed in favor of {@link #doHasKey(Polyad, State)}
     *
     * @param polyad
     * @param state
     * @Deprecated
     */
    protected void doHasKeys(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(HAS_KEYS + " requires 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(HAS_KEYS + " requires 2 arguments", polyad.getArgAt(2));
        }
        QDLValue arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0));
        if (!arg.isStem()) {
            throw new BadArgException(HAS_KEYS + " command requires a stem as its first argument.", polyad.getArgAt(0));
        }
        QDLStem target = arg.asStem();
        polyad.evalArg(1, state);
        QDLValue arg2 = polyad.getArgAt(1).getResult();
        checkNull(arg2, polyad.getArgAt(1));

        if (!arg2.isStem()) {
            polyad.setResult(target.containsKey(arg2.toString()));
            polyad.setEvaluated(true);
            return;
        }
        QDLStem result = target.hasKeys(arg2.asStem());
        polyad.setResult(result);
        polyad.setEvaluated(true);
    }

    /**
     * has_keys(key | keysList., arg.) returns left conformable result if the key or keylist. are keys in the arg.
     *
     * @param polyad
     * @param state
     */
    protected void doHasKey(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(HAS_KEY + " requires 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(HAS_KEY + " requires 2 arguments", polyad.getArgAt(2));
        }
        QDLValue arg0 = polyad.evalArg(0, state);
        checkNull(arg0, polyad.getArgAt(0));
        boolean isLeftScalar = false;
        QDLStem leftArg = null;
        if (arg0.isStem()) {
            leftArg = arg0.asStem();
        } else {
            isLeftScalar = true;
        }
        QDLStem rightArg = null;
        QDLValue arg1 = polyad.evalArg(1, state);
        checkNull(arg1, polyad.getArgAt(1));
        boolean isRightScalar = false;
        if (arg1.isStem()) {
            rightArg = arg1.asStem();
        } else {
            isRightScalar = true;
            //throw new QDLExceptionWithTrace(HAS_KEY + " requires a stem as its second argument", polyad.getArgAt(1));
        }

        if (isLeftScalar) {
            if (isRightScalar) {
                polyad.setResult(Boolean.TRUE);
            } else {
                if (isString(arg0) || isLong(arg0)) {
                    polyad.setEvaluated(true);
                    polyad.setResult(rightArg.containsKey(arg0));
                    return;
                }
                throw new BadArgException(HAS_KEY + " ", polyad.getArgAt(1));
            }
            polyad.setEvaluated(true);
            return;
        } else {
            if (isRightScalar) {
                throw new BadArgException("second argument must be a stem ", polyad.getArgAt(1));
            }
            // if neither is a scalar, do the next bit,
        }
        QDLStem result = leftArg.hasKey(rightArg);
        polyad.setEvaluated(true);
        polyad.setResult(result);
    }


    protected void doMakeIndex(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(getBigArgList());
            polyad.setEvaluated(true);
            return;
        }
        if (0 == polyad.getArgCount()) {
            throw new MissingArgException(SHORT_MAKE_INDICES + " requires at least 1 argument", polyad);
        }
        QDLValue arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0));
        boolean gotOne = false;
        // First argument always has to be an integer.
        boolean isLongArg = arg.isLong()  ;
        boolean isStemArg = arg.isStem();

        if (!(isLongArg || isStemArg)) {
            throw new BadArgException(SHORT_MAKE_INDICES + " requires a non-negative integer argument or a stem as its first argument", polyad.getArgAt(0));
        }

        int[] lengths = null;

        if (isStemArg) {
            QDLStem argStem = arg.asStem();
            JSON json = argStem.toJSON();
            if (!json.isArray()) {
                throw new BadArgException("first argument must be a stem list in " + SHORT_MAKE_INDICES, polyad.getArgAt(0));

            }
            JSONArray array = (JSONArray) json;
            lengths = new int[array.size()];
            for (int i = 0; i < array.size(); i++) {
                lengths[i] = array.getInt(i);
            }

        }
        QDLValue[] fill = null;
        CyclicArgList cyclicArgList = null;
        boolean hasFill = false;
        if (polyad.getArgCount() != 1) {
            QDLValue lastArg = polyad.evalArg(polyad.getArgCount() - 1, state);
            checkNull(lastArg, polyad.getArgAt(polyad.getArgCount() - 1));

            if (!lastArg.isStem()) {
                // fine, no fill.
                hasFill = false;
            } else {
                QDLStem fillStem = lastArg.asStem();
                if (!fillStem.isList()) {
                    throw new BadArgException("fill argument must be a list of scalars", polyad.getArgAt(polyad.getArgCount() - 1)); // last arg is fill list
                }
                QDLList qdlList = fillStem.getQDLList();
                fill = qdlList.toArray(true, false);
                cyclicArgList = new CyclicArgList(fill);

                hasFill = true;
            }
        }

        // Special case is a simple list. n(3) should yield [0,1,2] rather than a 1x3
        // array (recursion automatically boxes it into at least a 2 rank array).
        if (isLongArg && (polyad.getArgCount() == 1 || (polyad.getArgCount() == 2 && hasFill))) {
            long size = arg.asLong();
            QDLStem out = createSimpleStemVariable(polyad, cyclicArgList, hasFill, size);
            if (out == null) return; // special case where zero length requested
            polyad.setResult(out);
            polyad.setEvaluated(true);
            return;
        }
        // so the left arg is a stem Check simple case
        if (isStemArg) {
            QDLStem argStem = arg.asStem();
            if (argStem.size() == 1) {
                long size = argStem.get(0L).asLong();
                QDLStem out = createSimpleStemVariable(polyad, cyclicArgList, hasFill, size);
                if (out == null) return; // special case where zero length requested
                polyad.setResult(out);
                polyad.setEvaluated(true);
                return;
            }
        }

        int lastArgIndex = polyad.getArgCount() - 1;
        if (fill != null && fill.length != 0) {
            lastArgIndex--; // last arg is the fill pattern
        }

        if (lengths == null) {
            lengths = new int[lastArgIndex + 1];
            for (int i = 0; i < lastArgIndex + 1; i++) {
                QDLValue obj = polyad.evalArg(i, state);
                if (!obj.isLong()) {
                    throw new BadArgException("argument " + i + " is not an integer. All dimensions must be positive integers.", polyad.getArgAt(i));
                }
                lengths[i] = obj.asLong().intValue();
                // Any dimension of 0 returns an empty list
                if (lengths[i] == 0) {
                    polyad.setResult(new QDLStem());
                    polyad.setEvaluated(true);
                    return;
                }
                if (lengths[i] < 0L) {
                    throw new BadArgException("argument " + i + " is negative. All dimensions must be positive integers.", polyad.getArgAt(i));
                }
            }

        }
        QDLStem out = new QDLStem();
        indexRecursion(out, lengths, 0, cyclicArgList);
        polyad.setResult(out);
        polyad.setEvaluated(true);
        return;
    }

    private QDLStem createSimpleStemVariable(Polyad polyad, CyclicArgList cyclicArgList, boolean hasFill, long size) {
        if (size == 0) {
            polyad.setResult(new StemValue()); // set to empty stem
            polyad.setEvaluated(true);
            return null;
        }
        if (size < 0L) {
            throw new IndexError("negative index encountered", polyad);
        }
        QDLList qdlList;
        if (hasFill) {
            qdlList = new QDLList(size, cyclicArgList.next((int) size));
        } else {
            qdlList = new QDLList(size);
        }
        QDLStem out = new QDLStem();
        out.setQDLList(qdlList);
        return out;
    }

    /**
     * This makes an infinite arg list for creating new stems.
     * <pre>
     *       Object[] myArgs = new Object[]{"a","b",0L};
     *       CyclicArgClist cal = new CyclicArgList(myArgs);
     *       cal.next(17);
     * </pre>
     * would return the array
     * <pre>
     *     ["a","b",),"a","b",...]
     * </pre>
     * with 17 elements
     */
    public static class CyclicArgList {
        public CyclicArgList(QDLValue[] args) {
            this.args = args;
        }

        QDLValue[] args;

        /**
         * Cyclically get the next n elements
         *
         * @param n
         * @return
         */
        public QDLValue[] next(int n) {
            QDLValue[] out = new QDLValue[n];
            for (int i = 0; i < n; i++) {
                out[i] = args[((currentIndex++) % args.length)];
            }
            return out;
        }

        int currentIndex = 0;
    } // end CyclicArgList class

    /**
     * Fills in the elements for the n(x,y,z,...) function.
     *
     * @param out
     * @param lengths
     * @param index
     * @param cyclicArgList
     */
    protected void indexRecursion(QDLStem out, int[] lengths, int index, CyclicArgList cyclicArgList) {
        for (int i = 0; i < lengths[index]; i++) {
            if (lengths.length == index + 2) {
                // end of recursion
                QDLStem out1;
                if (cyclicArgList == null) {
                    out1 = new QDLStem((long) lengths[lengths.length - 1], null);
                } else {

                    out1 = new QDLStem((long) lengths[lengths.length - 1], cyclicArgList.next(lengths[lengths.length - 1]));
                }
                out.put(new LongValue(i), asQDLValue(out1));

            } else {
                QDLStem out1 = new QDLStem();
                indexRecursion(out1, lengths, index + 1, cyclicArgList);
                out.put(new LongValue( i), asQDLValue(out1));
            }
        }

    }

    /**
     * Processs case subset(arg., list.) so that
     * <pre>
     * out.i := arg.list.i
     * </pre>
     *
     * @param polyad
     * @param arg0
     * @param arg1
     */
    private void twoArgRemap(Polyad polyad, QDLStem arg0, QDLStem arg1) {
        QDLStem indices1 = arg1;
        QDLStem output = new QDLStem();
        for (QDLKey key : indices1.keySet()) {
            QDLValue v = indices1.get(key);
            Object gotValue = null;
            if (v.isStem()) {
                QDLStem ii = v.asStem();
                if (!ii.isList()) {
                    throw new BadArgException("stem index " + ii + " must be a list.", polyad.getArgAt(1));
                }
                IndexList indexList = new IndexList(ii);
                IndexList returnedIL = arg0.get(indexList, true);
                if (returnedIL.size() == 1) {
                    gotValue = returnedIL.get(0);
                } else {
                    throw new BadArgException("index does not resolve to a value.", polyad.getArgAt(1));
                }
            } else {
                gotValue = arg0.get(v);
            }
            if (gotValue != null) {
                output.put(key, asQDLValue(gotValue));
            }
        }

        polyad.setResult(output);
        polyad.setEvaluated(true);
    }

    /*
      a. := n(3,5,n(15))
  old. := all_keys(a.-1)
  new. := for_each(@reverse,  all_keys(a.-1))
  subset(a., new., old.)
     */

    /**
     * Subset with remapping such that for subset(arg., new_indices., old_indices.) out. satisfies
     * <pre>
     *     out.new_indices.i := arg.old_indices.i;
     * </pre>
     *
     * @param polyad
     * @param stem
     * @param newIndices
     * @param oldIndices
     */
    private void threeArgRemap(Polyad polyad, QDLStem stem,
                               QDLStem oldIndices,
                               QDLStem newIndices
    ) {
        QDLStem output = new QDLStem();
        for (long i = 0L; i < newIndices.size(); i++) {

            QDLValue newI = newIndices.get(i);
            QDLValue oldI = oldIndices.get(i);
            IndexList newIndex;
            if (newI.isStem()) {
                newIndex = new IndexList(newI.asStem());
            } else {
                newIndex = new IndexList();
                newIndex.add(newI);
            }
            IndexList oldIndex;
            if (oldI.isStem()) {
                oldIndex = new IndexList(oldI.asStem());
            } else {
                oldIndex = new IndexList();
                oldIndex.add(oldI);
            }
            // Note that if there is strict matching on, and it works, there is a single
            // value at index 0 in the result.
            try {
                output.set(newIndex, asQDLValue( stem.get(oldIndex, true).get(0)));
            } catch (IndexError indexError) {
                indexError.setStatement(polyad);// not great but it works.
                throw indexError;

            }
        }

        polyad.setResult(output);
        polyad.setEvaluated(true);
    }

    protected void doIsList(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (0 == polyad.getArgCount()) {
            throw new MissingArgException(IS_LIST + " requires an argument", polyad);
        }
        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(IS_LIST + " requires at most 1 argument", polyad.getArgAt(1));
        }
        QDLValue arg1 = polyad.evalArg(0, state);
        checkNull(arg1, polyad.getArgAt(0));
        Boolean isList;
        if (!arg1.isStem()) {
            //throw new BadArgException(IS_LIST + " requires stem as its first argument", polyad.getArgAt(0));
            isList = Boolean.FALSE;
        } else {
            isList = arg1.asStem().isList();
        }
        polyad.setResult(isList);
        polyad.setEvaluated(true);
    }

    /**
     * Remove the entire variable from the symbol table.
     *
     * @param polyad
     * @param state
     */
    protected void doRemove(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }

        if (0 == polyad.getArgCount()) {
            throw new MissingArgException(REMOVE + " requires at least one argument", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(REMOVE + " requires at most 2 arguments", polyad.getArgAt(1));
        }
        boolean isFunction = false;
        Long argCount = -2L; // -1 is reserved for all functions
        if (polyad.getArgCount() == 2) {
            isFunction = true;
            // user is trying to remove a function
            QDLValue arg1 = polyad.evalArg(1, state);
            // This should be an arg count
            if (!arg1.isLong()) {
                throw new BadArgException(REMOVE + " argument count must be an integer", polyad.getArgAt(1));
            }
            argCount = arg1.asLong();
        }
        try {
            polyad.evalArg(0, state);
        } catch (IndexError indexError) {
            // it is possible that the user is trying to grab something impossible
            polyad.setEvaluated(true);
            polyad.setResult(Boolean.TRUE);
            return;
        }
        String var = null;
        switch (polyad.getArgAt(0).getNodeType()) {
            case ExpressionInterface.VARIABLE_NODE:
                VariableNode variableNode = (VariableNode) polyad.getArgAt(0);
                // Don't evaluate this because it might not exist (that's what we are testing for). Just check
                // if the name is defined.
                var = variableNode.getVariableReference();
                if (var == null) {
                    polyad.setResult(Boolean.FALSE);
                } else {
                    if (isFunction) { // parser cannot always tell if the argument is a function.
                        state.getFTStack().remove(new FKey(var, argCount.intValue()));
                    } else {
                        state.remove(var);
                    }
                    polyad.setResult(Boolean.TRUE);
                }
                break;
            case ExpressionInterface.CONSTANT_NODE:
                throw new BadArgException(" cannot remove a constant", polyad.getArgAt(0));

            case ExpressionInterface.EXPRESSION_STEM2_NODE:
                ESN2 esn2 = (ESN2) polyad.getArgAt(0);
                polyad.setResult(esn2.remove(state));
                break;
            case ExpressionInterface.FUNCTION_REFERENCE_NODE:
                if (!isFunction) {
                    throw new BadArgException(REMOVE + " requires a n argument count", polyad);
                }
                FunctionReferenceNode functionReferenceNode = (FunctionReferenceNode) polyad.getArgAt(0);
                if (argCount == -1) {
                    for (FunctionRecordInterface fr : functionReferenceNode.getFunctionRecords()) {
                        state.getFTStack().remove(new FKey(functionReferenceNode.getFunctionName(), fr.getArgCount()));
                    }
                } else {
                    state.getFTStack().remove(new FKey(functionReferenceNode.getFunctionName(), argCount.intValue()));
                }
                polyad.setResult(Boolean.TRUE);
                break;
        }
        polyad.setEvaluated(true);
    }


    /**
     * <code>include_keys(stem., var | list.);</code><br/><br/> include keys on the right in the resulting stem
     *
     * @param polyad
     * @param state
     */
    protected void doIncludeKeys(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(INCLUDE_KEYS + " requires 2 argument", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(INCLUDE_KEYS + " requires 2 arguments", polyad.getArgAt(2));
        }
        polyad.evalArg(0, state);
        QDLValue arg = polyad.getArgAt(0).getResult();
        checkNull(arg, polyad.getArgAt(0));

        if (!arg.isStem()) {
            throw new BadArgException("The " + INCLUDE_KEYS + " command requires a stem as its first argument.", polyad.getArgAt(0));
        }
        QDLStem target = arg.asStem();
        polyad.evalArg(1, state);
        QDLValue arg2 = polyad.getArgAt(1).getResult();
        checkNull(arg2, polyad.getArgAt(1));
        if (!arg2.isStem()) {
            QDLStem result = new QDLStem();
            if (target.containsKey(arg2.asString())) {
                result.put(arg2.toString(), target.get(arg2.toString()));
            }
            polyad.setResult(result);
            polyad.setEvaluated(true);
            return;
        }
        QDLStem result = target.includeKeys(arg2.asStem());
        polyad.setResult(result);
        polyad.setEvaluated(true);
    }

    /**
     * <code>exclude_keys(stem., var | list.)</code><br/><br/> remove all the keys in list. from the stem.
     *
     * @param polyad
     * @param state
     */
    protected void doExcludeKeys(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        if (2 < polyad.getArgCount()) {
            throw new MissingArgException(EXCLUDE_KEYS + " requires 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (polyad.getArgCount() < 2) {
            throw new ExtraArgException(EXCLUDE_KEYS + " requires 2 arguments", polyad.getArgAt(2));
        }
        polyad.evalArg(0, state);
        QDLValue arg = polyad.getArgAt(0).getResult();
        checkNull(arg, polyad.getArgAt(0));
        if (!arg.isStem()) {
            throw new BadArgException("The " + EXCLUDE_KEYS + " command requires a stem as its first argument.", polyad.getArgAt(0));
        }
        QDLStem target = arg.asStem();
        polyad.evalArg(1, state);
        QDLValue arg2 = polyad.getArgAt(1).getResult();
        checkNull(arg2, polyad.getArgAt(1));

        if (!arg2.isStem()) {
            QDLStem result = new QDLStem();
            String excluded = arg2.toString();
            for (QDLKey ndx : target.keySet()) {
                result.put(ndx, target.get(ndx));
/*
                if (ndx instanceof Long) {
                    result.put((Long) ndx, target.get(ndx));
                } else {
                    result.put((String) ndx, target.get(ndx));
                }
*/
            }
            result.remove(excluded);
            polyad.setResult(result);
            polyad.setEvaluated(true);
            return;
        }
        QDLStem result = target.excludeKeys(arg2.asStem());
        polyad.setResult(result);
        polyad.setEvaluated(true);
    }


    /**
     * Permute the elements in a stem. The right argument must contain every key in the left argument or
     * an exception is raised. This is really just using cycle notation from abstract algebra...
     * E.g.,
     * <pre>
     * 10+3*indices(5)
     * [10,13,16,19,22]
     * rename_keys(10+3*indices(5), [4,2,3,1,0])
     * [22,19,13,16,10]
     * </pre>
     * read that in [4,2,3,1,0]: old → new, so 0 → 4, 1 → 2, 2→3, 3→1, 4→0
     */
    /*
    Test commands
    a.p:='foo';a.q:='bar';a.r:='baz';a.0:=10;a.1:=15;
    b.q :='r';b.0:='q';b.1:=0;b.p:=1;b.r:='p';
     shuffle(a., b.);
     */
    protected void doShuffle(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(SHUFFLE + " requires 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(SHUFFLE + " requires 2 arguments", polyad.getArgAt(2));
        }
        polyad.evalArg(0, state);
        QDLValue  arg = polyad.getArgAt(0).getResult();
        checkNull(arg, polyad.getArgAt(0));

        if (arg.isLong()) {
            // Fix https://github.com/ncsa/qdl/issues/128
            Long argL = arg.asLong();
            int argInt = argL.intValue();
            if (argL < 0L) {
                throw new BadArgException("the argument to" + SHUFFLE + " must be > 0", polyad.getArgAt(0));
            }
            Long[] array = new Long[argInt];
            long j = 0L;
            for (int i = 0; i < argInt; i++) {
                array[i] = j++; // fill it with longs
            }
            List<Long> longList = Arrays.asList(array);
            Collections.shuffle(longList);
            QDLStem stem = new QDLStem();
            stem.addList(longList);
            polyad.setResult(stem);
            polyad.setEvaluated(true);
            return;
        }

        if (!isStem(arg)) {
            throw new BadArgException(SHUFFLE + " requires a stem as its first argument.", polyad.getArgAt(0));
        }

        QDLValue arg2 = polyad.evalArg(1, state);
        checkNull(arg2, polyad.getArgAt(1));
        QDLStem target = arg.asStem();

        QDLStem newKeyStem;
        if(arg2.isLong()){
            // contract is to return a new stem.
            QDLList targetList = target.getQDLList();
           int targetSize =  targetList.size();
           // Case 1, nix to do
           if(targetSize == 0){
               polyad.setResult(target);
               polyad.setEvaluated(true);
               return;
           }

            QDLList<QDLValue> outList = new QDLList<>();
            long shift = arg2.asLong();
            shift = shift % targetSize;
            if(shift < 0L){
                shift = shift + targetSize; // make positive if needed
            }
           // Case 2, no sparse values
            if(!targetList.hasSparseEntries()){
                ArrayList<QDLValue> arrayList = new ArrayList<>();
                for(long j = 0; j < targetSize; j++){
                    arrayList.add(new LongValue((j+shift)%targetSize));
                }
                outList.setArrayList(arrayList);

            }else{
                // case 3: Sparse entries, so rotate, keeping same indices.
                TreeSet<LongValue> orderedKeys = targetList.orderedKeys().getListkeys();
                long[] indices  = new long[orderedKeys.size()];
                int j = 0;
                for(LongValue key : orderedKeys){
                    indices[j++] = key.asLong();
                }
                // nopw we can run through the indices directly.
                for(int i = 0 ; i < indices.length ; i++){
                    outList.set(indices[i] , targetList.get(indices[(int) ((i+shift)%targetSize)]));
                }
            }
            polyad.setResult(new QDLStem(outList));
            polyad.setEvaluated(true);
            return;



        }else {
            if (!arg2.isStem()) {
                throw new BadArgException(SHUFFLE + " requires a stem as its second argument.", polyad.getArgAt(1));
            }
             newKeyStem = arg2.asStem();
        }

        StemKeys newKeys = newKeyStem.keySet();
        StemKeys usedKeys = target.keySet();

        QDLStem output = new QDLStem();

        StemKeys keys = target.keySet();
        // easy check is to count. If this fails, then we throw and exception.
        if (keys.size() != newKeys.size()) {
            throw new BadArgException(" the supplied set of keys must match every key in the source stem.", polyad.getArgAt(0));
        }

        for (QDLKey key : keys) {
            if (newKeys.contains(key)) {
                QDLValue kk = newKeyStem.get(key);
                usedKeys.remove(kk);
                QDLValue vv = target.get(kk);
                output.put( key, vv);
            } else {
                throw new BadArgException("'" + key.getValue() + "' is not a key in the second argument.", polyad.getArgAt(1));
            }
        }
        if (!usedKeys.isEmpty()) {
            throw new BadArgException(" each key in the left argument must be used as a value in the second argument. This assures that all elements are shuffled.", polyad.getArgAt(1));
        }

        polyad.setResult(output);
        polyad.setEvaluated(true);
    }

    /*
    This only renames keys in situ, i.e. it changes the stem given.
    THE critical difference between rename_keys and using remap is that
    rename_keys alters its argument and as such, only the changes you want
    to make happen. Using remap, omitting unchanged elements removes them
    from the result.
     */

    /**
     * <code>rename_keys(arg., indices.);</code><br/><br/> list. contains entries of the form
     * <pre>
     *    indices.old = new
     * </pre>
     * Note that this is different from {@link #doRemap(Polyad, State)}.
     * The result is that each key in arg. is renamed, but the values are not changed.
     * If a key is in indices. and does not correspond to one on the left, it is skipped,
     * by subsetting rule.
     * If  a key is indices and the old and new value are the same, it is skipped.
     * <br/><br/> Limitations are that it applies to the zeroth axis, modifies arg. and
     * the indices. are different than remap.
     *
     * @param polyad
     * @param state
     */

    protected void doRenameKeys(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(RENAME_KEYS + " requires at least 2 arguments.", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }
        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(RENAME_KEYS + " takes at most 3 arguments", polyad.getArgAt(3));
        }
        polyad.evalArg(0, state);
        QDLValue arg = polyad.getArgAt(0).getResult();
        checkNull(arg, polyad.getArgAt(0));

        if (!arg.isStem()) {
            throw new BadArgException(RENAME_KEYS + " requires a stem as its first argument.", polyad.getArgAt(0));
        }
        polyad.evalArg(1, state);

        QDLValue arg2 = polyad.getArgAt(1).getResult();
        polyad.evalArg(1, state);
        checkNull(arg2, polyad.getArgAt(1));

        if (!arg2.isStem()) {
            throw new BadArgException("The " + RENAME_KEYS + " command requires a stem as its second argument.", polyad.getArgAt(1));
        }

        boolean overwriteKeys = false; //default
        if (polyad.getArgCount() == 3) {
            polyad.evalArg(2, state);

            QDLValue arg3 = polyad.getArgAt(2).getResult();
            polyad.evalArg(2, state);
            checkNull(arg2, polyad.getArgAt(2));
            if (arg3.isBoolean()) {
                overwriteKeys = arg3.asBoolean();
            } else {
                throw new BadArgException(RENAME_KEYS + " third argument, if present, must be a boolean", polyad.getArgAt(2));
            }

        }
        QDLStem target = arg.asStem();
        target.renameKeys(arg2.asStem(), overwriteKeys);

        polyad.setResult(target);
        polyad.setEvaluated(true);
    }

    /**
     * <code>common_keys(stem1., stem2.)</code><br/><br/> Return a list of keys common to both stems.
     *
     * @param polyad
     * @param state
     */
    protected void doCommonKeys(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(COMMON_KEYS + " requires 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(COMMON_KEYS + " function requires 2 arguments", polyad.getArgAt(2));
        }
        polyad.evalArg(0, state);
        QDLValue arg = polyad.getArgAt(0).getResult();
        checkNull(arg, polyad.getArgAt(0));

        if (!arg.isStem()) {
            throw new BadArgException(COMMON_KEYS + " requires a stem as its first argument.", polyad.getArgAt(0));
        }
        QDLValue arg2 = polyad.evalArg(1, state);

        //QDLValue arg2 = polyad.getArgAt(1).getResult();
        checkNull(arg2, polyad.getArgAt(1));

        if (!arg2.isStem()) {
            throw new BadArgException(COMMON_KEYS + " requires a stem as its second argument.", polyad.getArgAt(1));
        }

        QDLStem target = arg.asStem();
        QDLStem result = target.commonKeys(arg2.asStem());

        polyad.setResult(result);
        polyad.setEvaluated(true);
    }

    /**
     * Sets the default value for a stem. returns the default value set.
     *
     * @param polyad
     * @param state
     */
    protected void doSetDefault(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        if (2 < polyad.getArgCount()) {
            throw new MissingArgException(SET_DEFAULT + " requires 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(SET_DEFAULT + " requires 2 arguments", polyad.getArgAt(2));
        }
        QDLStem stemVariable = getOrCreateStem(polyad.getArgAt(0),
                state,
                "the " + SET_DEFAULT + " command accepts   only a stem variable as its first argument.");

        polyad.evalArg(1, state);
        QDLValue oldDefault = stemVariable.getDefaultValue();

        QDLValue defaultValue = polyad.getArgAt(1).getResult();
        stemVariable.setDefaultValue(defaultValue);
        // now return the previous result or null if there was none
        if (oldDefault == null) {
            polyad.setResult(QDLNull.getInstance());
        } else {
            polyad.setResult(oldDefault);
        }
        polyad.setEvaluated(true);
    }

    protected void doMask(Polyad polyad, State state) {

        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(MASK + " requires 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(MASK + " requires 2 arguments", polyad.getArgAt(2));
        }
        QDLValue obj1 = polyad.evalArg(0, state);
        // QDLValue obj1 = polyad.getArgAt(0).getResult();
        checkNull(obj1, polyad.getArgAt(0));

        QDLValue obj2 = polyad.evalArg(1, state);
        //Object obj2 = polyad.getArgAt(1).getResult();
        checkNull(obj2, polyad.getArgAt(1));
        QDLStem stem1 = null;
        QDLStem stem2 = null;
        if (obj1.isStem()) {
            stem1 = obj1.asStem();
            // short circuit for https://github.com/ncsa/qdl/issues/25
            if (stem1.isEmpty() && (obj2.isNull())) {
                polyad.setResult(new StemValue()); // return empty stemp
                 polyad.setEvaluated(true);
                return;
            }
        }
        if (obj2.isStem()) {
            stem2 = obj2.asStem();
            if (stem1 != null && stem1.isEmpty() && stem2.isEmpty()) {
                polyad.setResult(new StemValue()); // return empty stem
                polyad.setEvaluated(true);
                return;
            }
        }

        if (!areAllStems(obj1, obj2)) {
            Statement s = obj1.isStem() ? polyad.getArgAt(0) : polyad.getArgAt(1);
            throw new BadArgException("the " + MASK + " requires both arguments be stem variables", s);
        }
        QDLStem result = stem1.mask(stem2);
        polyad.setResult(result);
        polyad.setEvaluated(true);
    }

    /*
    Long block of QDL here to show how the Java should work. It is easy to do cases of this in QDL, but this
    ought to be a built in function.
    q. has different length of things
    q. := [[n(4), 4+n(4)],[8+n(4),12+n(4)], [16+n(5),21+n(5)]]
    w. := 100 + q.
     join(q., w., 1)
     join(q., w., 2)
     join(q., w., 3)
    q.
[
 [[0,1,2,3],[4,5,6,7]],
 [[8,9,10,11],[12,13,14,15]],
 [[16,17,18,19,20],[21,22,23,24,25]]
]
    w.
[
 [[100,101,102,103],[104,105,106,107]],
 [[108,109,110,111],[112,113,114,115]],
 [[116,117,118,119,120],[121,122,123,124,125]]
]
  // QDL to do the first few cases of this directly
  join0(x., y.)->[z.:=null;z.:=x.~y.;return(z.);]
  join1(x., y.)->[z.:=null;while[for_keys(i0,x.)][z.i0. := x.i0~y.i0;];return(z.);]
  join2(x., y.)->[z.:=null;while[for_keys(i0,x.)][while[for_keys(i1, x.i0)][z.i0.i1.:=x.i0.i1~y.i0.i1;];];return(z.);]
  join3(x., y.)->[z.:=null;while[for_keys(i0,x.)][while[for_keys(i1, x.i0)][while[for_keys(i2, x.i0.i1)][z.i0.i1.i2.:=x.i0.i1.i2~y.i0.i1.i2;];];];return(z.);]
  join4(x., y.)->[z.:=null;while[for_keys(i0,x.)][while[for_keys(i1, x.i0)][while[for_keys(i2, x.i0.i1)][while[for_keys(i3, x.i0.i1)][z.i0.i1.i2.i3.:=x.i0.i1.i2.i3~y.i0.i1.i2.i3;];];];];return(z.);]

  // also q.~w.
  z. := join0(q.,w.)
  //  *   <--- You are here
  //  z.i.j.k
          join0(q.,w.)
[
 [[0,1,2,3],[4,5,6,7]],
 [[8,9,10,11],[12,13,14,15]],
 [[16,17,18,19,20],[21,22,23,24,25]],
 [[100,101,102,103],[104,105,106,107]],
 [[108,109,110,111],[112,113,114,115]],
 [[116,117,118,119,120],[121,122,123,124,125]]
]
// result is list of combined lengths size(z.) == size(q.) + size(w.)

 z. :=  join1(q., w.)
  //   *  <--- You are here
  // z.i.j.k
[
 [[0,1,2,3],[4,5,6,7],[100,101,102,103],[104,105,106,107]],
 [[8,9,10,11],[12,13,14,15],[108,109,110,111],[112,113,114,115]],
 [[16,17,18,19,20],[21,22,23,24,25],[116,117,118,119,120],[121,122,123,124,125]]
]
// z. has same shape, but z.k == q.k ~ w.k


       z. := join2(q.,w.)
  //     *  <--- You are here
  // z.i.j.k
[
 [[0,1,2,3,100,101,102,103],[4,5,6,7,104,105,106,107]],
 [[8,9,10,11,108,109,110,111],[12,13,14,15,112,113,114,115]],
 [[16,17,18,19,20,116,117,118,119,120],[21,22,23,24,25,121,122,123,124,125]]
]
// z. now has size(z.k) == size(q.k) + size(w.k)

z. :=  join3(q.,w.)
  //       * <--- You are here
  // z.i.j.k
[
 [[[0,100],[1,101],[2,102],[3,103]],[[4,104],[5,105],[6,106],[7,107]]],
 [[[8,108],[9,109],[10,110],[11,111]],[[12,112],[13,113],[14,114],[15,115]]],
 [[[16,116],[17,117],[18,118],[19,119],[20,120]],[[21,121],[22,122],[23,123],[24,124],[25,125]]]
]

  // Since this is the last index this joins each element into new elements, increasing the
  // rank of the stem by 1
     */

    protected void doJoin(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2, 3});
            polyad.setEvaluated(true);
            return;
        }

        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(JOIN + " requires at least 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }
        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(JOIN + " takes at most 3 arguments", polyad.getArgAt(3));
        }

        QDLValue[] args = new QDLValue[polyad.getArgCount()];
        int argCount = polyad.getArgCount();
        for (int i = 0; i < argCount; i++) {
            args[i] = polyad.evalArg(i, state);
            checkNull(args[i], polyad.getArgAt(i));
        }
        int axis = 0;
        if (args.length == 3) {
            axis = args[2].asLong().intValue();
        }
        QDLStem leftStem;
        if (args[0].isStem()) {
            leftStem = args[0].asStem();
        } else {
            leftStem = new QDLStem();
            leftStem.put(LongValue.Zero, args[0]);
        }
        QDLStem rightStem;
        if (args[1].isStem()) {
            rightStem = args[1].asStem();
        } else {
            rightStem = new QDLStem();
            rightStem.put(LongValue.Zero, args[1]);
        }
        if (leftStem.isEmpty() && rightStem.isEmpty()) {
            // edge case -- they sent  empty arguments, so don't blow up, just return nothing
            polyad.setResult(new QDLStem());
            polyad.setEvaluated(true);
            return;
        }

        boolean doJoinOnLastAxis = false;
        if (axis == LAST_AXIS_ARGUMENT_VALUE) {
            doJoinOnLastAxis = true;
        }
        if (axis == 0 || (leftStem.isEmpty() && rightStem.dim().size() == 1)) {
            // Cases are axis 0 join or monadic -1 axis join
            QDLStem outStem = leftStem.union(rightStem);
            polyad.setEvaluated(true);
            polyad.setResult(outStem);
            return;
        }
        if (leftStem.dim().size() == 1) {
            if (axis == -1) {
                QDLStem outStem = leftStem.union(rightStem);
                polyad.setEvaluated(true);
                polyad.setResult(outStem);
                return;
            }
            throw new RankException("axis of " + axis + " exceeds rank");
        }
        QDLStem outStem = new QDLStem();

        StemUtility.DyadAxisAction joinAction = new StemUtility.DyadAxisAction() {
            @Override
            public void action(QDLStem out, QDLKey key, QDLStem leftStem, QDLStem rightStem) {

                out.put(key, leftStem.union(rightStem));
/*
                if (key instanceof Long) {
                    out.put((Long) key, leftStem.union(rightStem));
                } else {
                    out.put((String) key, asQDLValue(leftStem.union(rightStem)));
                }
*/
            }
        };
        if (Math.max(leftStem.getRank(), rightStem.getRank()) <= axis) {
            throw new RankException("axis of " + axis + " exceeds rank");
        }
        StemUtility.axisDayadRecursion(outStem, leftStem, rightStem, doJoinOnLastAxis ? 1000000 : (axis - 1), doJoinOnLastAxis, joinAction);
        polyad.setResult(outStem);
        polyad.setEvaluated(true);
    }

    public interface AxisAction {
        void action(QDLStem out, String key, QDLStem leftStem, QDLStem rightStem);
    }


    protected void doTransform(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        /*  Waaaay easier to do this in QDL, but this should be in base system
           not a module.
           axis :=1
          a. := n(3,4,5,n(60))
           
         old. := indices(a., axis);
         rank := rank(old.);
         axis := (axis<0)?axis+rank:axis; // if negative, fix it
         permute. := axis~[;axis]~[axis+1;rank];
         new. := for_each(@shuffle, old., [permute.]);
         return(remap(x., old., new.));

                           old. := indices(x., -1);
                   rank := size(old.0);
                   perm. := p~~exclude_keys([;rank], p);
                   new. := for_each(@shuffle, old., [perm.]);
                   return(subset(x., new., old.));
         */
        if (polyad.getArgCount() == 0) {
            throw new MissingArgException(TRANSPOSE + " requires at least one argument.", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(TRANSPOSE + " takes at most two arguments.", polyad.getArgAt(2));
        }

        QDLValue arg0 = polyad.evalArg(0, state);
        checkNull(arg0, polyad.getArgAt(0), state);
        AxisExpression ae = null;
        boolean hasAxisExpression = false;
        QDLStem stem;
        Long axis = null;
        if (arg0.isAxisRestriction()) {
            ae = arg0.asAxisExpression();
            stem = ae.getStem();
            if (ae.isStar()) {
                hasAxisExpression = false;
            } else {
                axis = ae.getAxis();
                hasAxisExpression = true;
            }
        } else {
            if (!arg0.isStem()) {
                throw new BadArgException(TRANSPOSE + " requires a stem as its first argument", polyad.getArgAt(0));
            }
            stem = arg0.asStem();
        }

        QDLStem oldIndices = stem.indicesByRank(-1L);
        // kludge, assume that the rank of all at the last axis is the same.
        int rank = oldIndices.get(0L).asStem().size();

        if (rank == 1) {
            // nothing to do. This is just a list
            polyad.setResult(stem);
            polyad.setEvaluated(Boolean.TRUE);
            return;
        }

        QDLStem pStem0;
        // Start QDL. sliceNode is [;rank]
        OpenSliceNode sliceNode = new OpenSliceNode(polyad.getTokenPosition());
        sliceNode.getArguments().add(new ConstantNode(LongValue.Zero));
        sliceNode.getArguments().add(new ConstantNode(new LongValue(Integer.toUnsignedLong(rank))));
        QDLValue arg2 = null;
        boolean arg2ok = false;
        QDLStem stem1 = null;
        boolean hasArg2 = polyad.getArgCount() == 2;
        if (!hasAxisExpression && hasArg2) {
            arg2 = polyad.evalArg(1, state);
            checkNull(arg2, polyad.getArgAt(1), state);
            if (arg2.isLong()) {
                axis = arg2.asLong();
                hasAxisExpression = true; //either it is set here or comes with axis operator
                arg2ok = true;
            }
        }
        // handle case that there is an axis
        if (hasAxisExpression) {
            if (axis == 0L) {
                // They are requesting essentially the identity permutation, so don't jump through hoops.
                polyad.setResult(stem);
                polyad.setEvaluated(Boolean.TRUE);
                return;
            }
            stem1 = new QDLStem();
            if (axis < 0) {
                long newArg = rank + axis;
                if (newArg < 0) {
                    throw new IndexError("the requested axis of " + axis + " is not valid for a stem of rank " + rank, polyad.getArgAt(1));
                }
                stem1.listAdd(asQDLValue(newArg));
            } else {
                if (rank <= axis) {
                    throw new IndexError("the requested axis of " + axis + " is not valid for a stem of rank " + rank, polyad.getArgAt(1));
                }
                stem1.listAdd(asQDLValue(axis));
            }
            arg2ok = true;
            hasArg2 = true;
        } else {

        }
        if (hasArg2) {
            if (arg2 != null && arg2.isStem()) {
                stem1 = arg2.asStem();
                arg2ok = true;
            }
            // Can finally decide whether or not second argument is a dud.
            if (!arg2ok) {
                throw new BadArgException(TRANSPOSE + " requires an axis or stem of them as its second argument", polyad.getArgAt(1));
            }
            // If the second argument is p., then the new reshuffing is
            // p. ~ ([]~ exclude_keys([;rank(p)], p.))
            Polyad excludeKeys = new Polyad(EXCLUDE_KEYS);
            excludeKeys.addArgument(sliceNode);
            excludeKeys.addArgument(new ConstantNode(new StemValue(stem1)));
            Dyad monadicTilde = new Dyad(OpEvaluator.TILDE_VALUE); // mondic tilde does not exist. It is done as []~arg.
            monadicTilde.setLeftArgument(new ConstantNode(new StemValue())); // empty stem
            monadicTilde.setRightArgument(excludeKeys);
            Dyad dyadicTilde = new Dyad(OpEvaluator.TILDE_VALUE);
            dyadicTilde.setLeftArgument(new ConstantNode(new StemValue(stem1)));
            dyadicTilde.setRightArgument(monadicTilde);
            dyadicTilde.evaluate(state);
            pStem0 = dyadicTilde.getResult().asStem();
        } else {
            // default is to use reverse([;rank]) as the second argument
            Polyad reverse = new Polyad(ListEvaluator.LIST_REVERSE);
            reverse.addArgument(sliceNode);
            reverse.evaluate(state);
            pStem0 = reverse.getResult().asStem();
        }

        // Now we need to create QDL for
        // newIndices. := shuffle(oldIndices., pStem0.)
        //  QDLStem pStem = new QDLStem();
        // pStem.put(0L, pStem0); // makes [pstem.]

        QDLStem newIndices = oldIndices.getQDLList().permuteEntries(pStem0.getQDLList().getArrayList());

        // QDL to remap everything.
        Polyad subset = new Polyad(REMAP);
        subset.addArgument(new ConstantNode(asQDLValue(stem)));
        subset.addArgument(new ConstantNode(asQDLValue(oldIndices)));
        subset.addArgument(new ConstantNode(asQDLValue(newIndices)));
        polyad.setResult(subset.evaluate(state));
        polyad.setEvaluated(Boolean.TRUE); // set evaluated true or next line bombs.
    }
}
