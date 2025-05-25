package org.qdl_lang.variables;

import org.qdl_lang.exceptions.IndexError;
import org.qdl_lang.exceptions.NoDefaultValue;
import org.qdl_lang.expressions.AllIndices;
import org.qdl_lang.expressions.IndexList;
import org.qdl_lang.state.QDLConstants;
import org.qdl_lang.state.StemMultiIndex;
import org.qdl_lang.state.VariableState;
import org.qdl_lang.util.InputFormUtil;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.qdl_lang.statements.WhileLoop;
import org.qdl_lang.util.aggregate.AxisRestrictionIdentity;
import org.qdl_lang.util.aggregate.QDLAggregateUtil;
import software.amazon.awssdk.services.kendra.model.QuerySuggestionsBlockListStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.qdl_lang.state.VariableState.var_regex;
import static org.qdl_lang.variables.StemConverter.convert;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/27/22 at  6:33 AM
 */
public class QDLStem implements Map<String, Object>, Serializable {
    public static final String STEM_INDEX_MARKER = ".";
    public static String STEM_ENTRY_CONNECTOR = ":";

    public QDLStem() {
    }

    /**
     * Initialize a QDL stem with a list
     *
     * @param list
     */
    public QDLStem(QDLList list) {
        setQDLList(list);
    }

    /**
     * Initialize a QDL stem with count elements form the fillList. This
     * returns a simple list. See {@link QDLList#QDLList(long, Object[])}
     * for the contract.
     *
     * @param count
     * @param fillList
     */
    public QDLStem(Long count, Object[] fillList) {
        QDLList s = new QDLList(count, fillList);
        setQDLList(s);
    }

    public QDLStem newInstance() {
        return new QDLStem();
    }

    public QDLStem newInstance(Long count, Object[] fillList) {
        return new QDLStem(count, fillList);
    }

    public QDLMap getQDLMap() {
        if (qdlMap == null) {
            qdlMap = new QDLMap();
        }
        return qdlMap;
    }

    public void setQDLMap(QDLMap qdlMap) {
        this.qdlMap = qdlMap;
    }


    /* *******
      QDLMap specific
      ****** */
    QDLMap qdlMap;

    // Convenience methods.
    public Long getLong(String key) {
        if (isLongIndex(key)) {
            return getLong(Long.parseLong(key));
        }
        return (Long) getQDLMap().get(key);
    }

    public QDLStem getStem(String key) {
        if (isLongIndex(key)) {
            return getStem(Long.parseLong(key));
        }
        return (QDLStem) getQDLMap().get(key);

    }

    public QDLStem getStem(Long key) {
        return (QDLStem) getQDLList().get(key);
    }

    public Long getLong(Long key) {
        return (Long) getQDLList().get(key);
    }

    public Object remove(String key) {
        if (isLongIndex(key)) {
            return remove(Long.parseLong(key.toString()));
        }
        return getQDLMap().remove(key);
    }

    public boolean containsKey(String key) {
        if (isLongIndex(key)) {
            boolean rc = getQDLList().containsKey(key);
            return rc;
        }
        return getQDLMap().containsKey(key);
    }

    /*
     get call with a conversion to a string or null if the object is null. This invokes to string on the object.
     */
    public String getString(String key) {
        Object obj = get(key);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public Boolean getBoolean(String key) {
        return (Boolean) get(key);
    }

    public Object get(String key) {
        // TODO -- Horribly inefficient. This should be improved but that may take some serious work, so deferring
        if (key.endsWith(STEM_INDEX_MARKER)) {
            key = key.substring(0, key.length() - 1);
        }
        try {
            if (isLongIndex(key)) {
                return get(Long.parseLong(key));
            }
            if (key.endsWith(STEM_INDEX_MARKER)) {
                try {
                    Long kk = Long.parseLong(key.substring(0, key.length() - 1));
                    return get(kk);

                } catch (Throwable t) {
                    // not a number
                }
            }
            if (!containsKey(key) && defaultValue != null) {
                return defaultValue;
            }
            if (StemPath.isPath(key)) {
                StemPath stemPath = new StemPath();
                stemPath.parsePath(key);
                return get(stemPath);
            }
            return getQDLMap().get(key);
        } catch (StackOverflowError | PatternSyntaxException sto) {
            //In this case someplace there is a reference to the stem itself, e.g.
            // a. := indices(5);
            // a.b. := a.
            // This can work if the indices are accessed directly but attempting to access the whole
            // things (such as printing it out with "say" is going to fail).
            throw new VariableState.CyclicalError("recursive overflow at index '" + key + "'");
        }
    }

    /* *******
        QDLList specific
       ****** */
    public QDLList getQDLList() {
        if (qdlList == null) {
            qdlList = new QDLList();
        }
        return qdlList;
    }

    public void setQDLList(QDLList qdlList) {
        this.qdlList = qdlList;
    }

    QDLList qdlList;

    public Boolean getBoolean(Long key) {
        return (Boolean) get(key);
    }

    public Object get(Long key) {
        Object rc = getQDLList().get(key);
        if (rc == null && defaultValue != null) {
            return defaultValue;
        }
        return rc;
    }

    public BigDecimal getDecimal(Long key) {
        Object obj = getQDLList().get(key);
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        // try to convert it.
        return new BigDecimal(obj.toString());
    }

    /*
     get call with a conversion to a string or null if the object is null. This invokes to string on the object.
     */
    public String getString(Long key) {
        Object obj = getQDLList().get(key);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public Object put(Long index, Object value) {
        getQDLList().set(index, value);
        return null;
    }

    public boolean containsKey(Long key) {
        SparseEntry s = new SparseEntry(key);
        return getQDLList().containsKey(s);
    }

    public Object remove(Long key) {
        getQDLList().removeByIndex(key);
        return null;
    }


    /**
     * Adds a list of objects to this stem, giving them indices appropriate indices
     * This is mostly a convenience for people writing in java to create lists
     * programatically. Note there is no parameter for this list since that will blow up
     * if there are mixed entries.
     *
     * @param list
     */
    public void addList(List list) {
        getQDLList().addAll(list);
    }

    /**
     * Append the list elements of the stem to this list. This integrates the lists but does not affect
     * the maps.
     *
     * @param stem
     */
    public void listAppend(QDLStem stem) {
        getQDLList().appendAll(stem.getQDLList().values());
    }

    /**
     * Add the single object to the list in this stem. This allows you to add a stem as a list
     * value, unlike {@link #listAppend(QDLStem)} which appends the elements of the argument's
     * list to the current object's list.
     *
     * @param value
     */
    public void listAdd(Object value) {
        getQDLList().append(value);
    }

    /**
     * Copies the elements from this list to the target list. Note that this will over-write any elements
     * already in the target. If you need to insert elements, use the {@link #listInsertAt(long, long, QDLStem, long)}
     * method.
     *
     * @param startIndex  first index in the source
     * @param length      how many elements to take from the source
     * @param target      that target to get the copy
     * @param insertIndex where in the target to start copying.
     */
    public void listCopy(long startIndex, long length, QDLStem target, long insertIndex) {
        // Caveat: in QDL List you copy/insert from a source into the current list
        target.getQDLList().listCopyFrom(startIndex, length, getQDLList(), insertIndex);
    }

    /**
     * Insert the current
     *
     * @param startIndex
     * @param length
     * @param target
     * @param insertIndex
     */
    public void listInsertAt(long startIndex, long length, QDLStem target, long insertIndex) {
        target.getQDLList().listInsertFrom(startIndex, length, getQDLList(), insertIndex);
    }

    /**
     * Insert the whole argument in to the current stem, re-adjusting indices.
     *
     * @param startIndex
     */

    public QDLStem listSubset(long startIndex) {
        return listSubset(startIndex, getQDLList().size() - startIndex);
    }

    public QDLStem listSubset(long startIndex, long length) {
        QDLStem stem = newInstance();
        stem.setQDLList(getQDLList().subList(startIndex, true, length, false));
        return stem;
    }

    public boolean isList() {
        return getQDLMap().isEmpty();
    }

    public Object put(int index, Object value) {
        return put(Long.valueOf(index), value);
    }

    /* *******
        Convenience methods
       ****** */


    /* *******
        Stem specific
       ****** */

    /**
     * If this is set, then any get with no key will return this value. Since
     * the basic unit of QDL is the stem, this gives us a way of basically turning
     * a scalar in to a stem without having to do complicated size and key matching.
     * <br/><br/>
     * <b>Note</b> that {@link #containsKey(Object)} still works as usual, so you can ask
     * if a key exists.
     *
     * @return
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    Object defaultValue;

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }


    /**
     * As per usual Java {@link Map#size()} contract, return axis 0 count
     *
     * @return
     */
    @Override
    public int size() {
        return getQDLMap().size() + getQDLList().size();
    }

    /**
     * Return count down to a given axis. This accepts {@link org.qdl_lang.util.aggregate.ProcessStemAxisRestriction#ALL_AXES}
     * as well, which gives the count of every entry, i.e., the cardinality of the stem.
     *
     * @param axis
     * @return
     */
    public long size(int axis) {
        SizeOf s = new SizeOf(axis);
        Object x = QDLAggregateUtil.process(this, s);
        return s.size;
    }

    /*
       b. :=[0,[0,1,2,3,4,5,6],{1,{10,11,12},2,3,4,5,6},[[0,1,2],[0,1,2]],4, true, 2/17]~{'a':['a','b','c']};
       indices(b.)
    [[0],[2],[4],[5],[6]
     [1,0],[1,1],[1,2],[1,3],[1,4],[1,5],[1,6],[a,0],[a,1],[a,2],
     [3,0,0],[3,0,1],[3,0,2],[3,1,0],[3,1,1],[3,1,2]]

     Idiom is
        while[ j∈[;size(b.) ]do[...
     so changing contract for axis 0 breaks things badly.
     */
    public static class SizeOf extends AxisRestrictionIdentity {
        Long size = 0L;

        public SizeOf(int axis) {
            this.axis = axis;
        }

        @Override
        public Object getDefaultValue(List<Object> index, Object key, Object value) {
            size++;
            return value;
        }
        public Long getSize(){
            return size;
        }
    }

    @Override
    public boolean isEmpty() {
        return getQDLMap().isEmpty() && getQDLList().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof Long) {
            return containsKey((Long) key);
        }
        if (key instanceof String) {
            return containsKey((String) key);
        }
        if (key instanceof IndexList) {
            return get((IndexList) key, false) != null;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if (getQDLMap().containsValue(value)) return true;
        if (getQDLList().isEmpty()) return false;
        // *sigh* have to look for it
        return getQDLList().contains(value);
    }

    /**
     * Make a shallow copy of this stem variable.
     *
     * @return
     */
    @Override
    public Object clone() {
        QDLStem output = newInstance();
        if (hasDefaultValue()) {
            output.setDefaultValue(getDefaultValue());
        }
        for (Object key : keySet()) {
            Object obj = get(key);
            if (obj instanceof QDLStem) {
                output.putLongOrString(key, ((QDLStem) obj).clone());
            } else {
                output.putLongOrString(key, obj);
            }
        }

        return output;
    }

    public void putLongOrString(Object key, Object value) {
        if (key instanceof Long) {
            put((Long) key, value);
        } else {
            put((String) key, value);
        }
    }

    @Override
    public Object get(Object key) {
        if (key instanceof QDLStem) {
            // try to make a stem list
            IndexList r = get(new IndexList((QDLStem) key), true);
            return r.get(0);
        }
        if (key instanceof Integer) {
            key = Long.valueOf((Integer) key);
        }
        Object value = null;
        if (key instanceof Long) {
            value = get((Long) key);
        }
        if (key instanceof String) {
            String sKey = (String) key;
            if (StemPath.isPath(sKey)) {
                StemPath stemPath = new StemPath();
                stemPath.parsePath(sKey);
                value =  get(stemPath);
            }else {
                value = get((String) key);
            }
        }
        if(value == null) {
            value = getQDLMap().get(key);
        }
        // Fixes https://github.com/ncsa/qdl/issues/122
        if(hasDefaultValue() && (value  instanceof QDLStem)) {
            ((QDLStem) value).setDefaultValue(getDefaultValue());
        }
        return value;
    }

    @Override
    public Object put(String key, Object value) {

        if (key.endsWith(STEM_INDEX_MARKER)) {
            key = key.substring(0, key.length() - 1);
        }
        if (StringUtils.isTrivial(key)) {
            throw new IllegalArgumentException("cannot have a trivial stem key");
        }

        if (isIntVar(key)) {
            return put(Long.parseLong(key), value);
        }

        return getQDLMap().put(key, value);
    }

    @Override
    public Object remove(Object key) {
        if (key instanceof Long) {
            return remove((Long) key);
        }
        return getQDLMap().remove(key);
    }

    public void addAll(QDLStem qdlStem) {
        getQDLMap().putAll(qdlStem.getQDLMap());
        getQDLList().addAll(qdlStem.getQDLList());
    }

    /**
     * This does <b>not</b> add the list elements because that causes issues
     * with the contract for maps. If you are adding all the values of a stem
     * to this one, use {@link #addAll(QDLStem)}.
     *
     * @param m
     */
    @Override
    public void putAll(Map<? extends String, ?> m) {
        if (m instanceof QDLStem) {
            QDLStem qdlStem = (QDLStem) m;
            getQDLMap().putAll(qdlStem.getQDLMap());
            return;
        }
        throw new IllegalArgumentException("Unknown map type");
    }

    @Override
    public void clear() {
        qdlMap = null;
        qdlList = null;
    }

    /**
     * return an enumeration (set) of <b>ALL</b> keys in order, starting with the list indices
     * then the keys.
     *
     * @return
     */
    @Override
    public StemKeys keySet() {
        return orderedkeySet();
    }


    /**
     * Be aware that this creates an actual set so it reads every item. If you need to iterate
     * over the elements (so single pass, not potentially multiple passes) consider using
     * {@link #valuesIterator()} while gets the iterators and manages them.
     *
     * @return
     */
    @Override
    public Collection<Object> values() {
        return valueSet();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return null;
    }

    /* *******
            Utilities
        ****** */

    /**
     * Does a regex on the index to see if it is really a long. Note that this
     * still is needed since a user can set a.'2' := 3 and this should turn
     * it into a list entry otherwise we get both string and list entries with
     * "the same" key.
     *
     * @param key
     * @return
     */
    public boolean isLongIndex(String key) {
        // special case of index being zero!! Otherwise, no such index can start with zero,
        // so a key of "01" is a string, not the number 1. Sorry, best we can do.
        //     try {
        return key.equals("0") || key.matches(int_regex);
    }

    String int_regex = "[+-]?[1-9][0-9]*";
    Pattern var_pattern = Pattern.compile(var_regex);
    Pattern int_pattern = Pattern.compile(int_regex);

    protected boolean isVar(String var) {
        return var_pattern.matcher(var).matches();
    }

    protected boolean isIntVar(String var) {
        return var.equals("0") || int_pattern.matcher(var).matches();
    }

    /**
     * A map that is keyed by an integer and has lists of indices as values. It is used to collect
     * indices of a given rank or along a given axis.
     */
    public static class KeyRankMap extends TreeMap<Integer, List<List>> {
        public void put(List list) {
            if (!containsKey(list.size())) {
                List enclosingList = new ArrayList();
                put(list.size(), enclosingList);
            }
            get(list.size()).add(list);
        }
    }

    protected static class OrderedIndexEntry implements Comparable, Serializable {
        public OrderedIndexEntry(long index, boolean hasStem) {
            this.index = index;
            this.hasStem = hasStem;
        }

        long index;
        boolean hasScalar;
        boolean hasStem;

        @Override
        public int compareTo(Object o) {
            if (o instanceof OrderedIndexEntry) {
                OrderedIndexEntry s = (OrderedIndexEntry) o;
                if (index < s.index) return -1;
                if (index == s.index) return 0;
                if (index > s.index) return 1;
            }
            throw new ClassCastException("the object '" + o.getClass().getSimpleName() + "' is not comparable.");
        }

        @Override
        public boolean equals(Object obj) {
            return compareTo(obj) == 0;
        }
    }

    public QDLSet valueSet() {
        QDLSet qdlSet = new QDLSet();
        for (Object key : keySet()) {
            Object value = get(key);
            switch (Constant.getType(value)) {
                case Constant.STEM_TYPE:
                    qdlSet.addAll(((QDLStem) value).values());
                    break;
                case Constant.SET_TYPE:
                    qdlSet.addAll(((QDLSet) value));
                    break;
                default:
                    qdlSet.add(value);
            }
        }
        return qdlSet;
    }

    StemKeys orderedkeySet() {
        if (getQDLMap().keySet().isEmpty() && getQDLList().isEmpty()) {
            return new StemKeys();
        }
        StemKeys stemKeys = getQDLList().orderedKeys();
        TreeSet<String> stringTreeSet = new TreeSet<>();
        stringTreeSet.addAll(getQDLMap().keySet());
        stemKeys.setStemKeys(stringTreeSet);
        return stemKeys;
    }

    /**
     * This almost returns all the unique elements. The issue is that if there are deeply
     * nested stems, then do not entirely get made unique before getting added to the result,
     * hence the simplest fix is that in that case is to call this twice. Someday this can
     * be fixed with a careful rewrite of the recursion in stem lists.
     *
     * @return
     */
    public QDLStem almostUnique() {
        QDLStem output = newInstance();
        if (isList()) {
            output.setQDLList(getQDLList().unique());
            return output;
        }
        HashSet hashSet = new HashSet();
        for (Object key : keySet()) {
            Object value = get(key);
            if (value instanceof QDLStem) {
                QDLStem ss = ((QDLStem) value).almostUnique();
                hashSet.addAll(ss.getQDLList().values());
            } else {
                hashSet.add(value);
            }
        }
        QDLList qdlList1 = new QDLList();
        HashSet hashSet1 = new HashSet();
        for (Object obj : hashSet) {
            hashSet1.add(obj);
        }

        for (Object obj : hashSet) {
            qdlList1.append(obj);
        }
        output.setQDLList(qdlList1);
        return output;
    }

    public QDLStem dim() {
        QDLStem dim = newInstance();
        dim.setQDLList(getQDLList().dim());
        return dim;
    }

    public Long getRank() {
        return Long.valueOf(dim().size());
    }

    public Object get(StemPath<StemPathEntry> stemPath) {
        if (stemPath.isEmpty()) {
            return null;
        }
        MetaCodec codec = new MetaCodec();
        Object currentObj = QDLNull.getInstance();
        QDLStem lastStem = this;
        for (StemPathEntry spe : stemPath) {
            currentObj = lastStem.get(spe.isString() ? codec.decode(spe.getKey()) : spe.getIndex());
            if (currentObj == null) {
                return QDLNull.getInstance();
            }
            if (currentObj instanceof QDLStem) {
                lastStem = (QDLStem) currentObj;
            } else {
                lastStem = null;
            }
        }
        return currentObj;
    }

    /**
     * This will return a new stem consisting of this stem and the union of all
     * the stem arguments. The effect is to overwrite the current stem values with
     * the argument values. so e.g.
     * <pre>
     *       {'a':1}~{'a':2,'b':3}~{'b':4,'c':5}
     * {a:2, b:4, c:5}
     * </pre>
     *
     * @param stemVariables
     * @return
     */
    public QDLStem union(QDLStem... stemVariables) {
        QDLStem newStem = (QDLStem) clone();
        for (QDLStem stemVariable : stemVariables) {
            newStem.putAll(stemVariable); // non-list
            newStem.listAppend(stemVariable); // list elements
            if (stemVariable.getDefaultValue() != null) {
                newStem.setDefaultValue(stemVariable.getDefaultValue());
            }
        }
        return newStem;
    }


    public class ValueIterator implements Iterator {
        Iterator listIterator = getQDLList().iterator();
        Iterator stemIterator = getQDLMap().values().iterator();

        @Override
        public boolean hasNext() {
            return listIterator.hasNext() || stemIterator.hasNext();
        }

        @Override
        public Object next() {
            if (listIterator.hasNext()) {
                Object obj = listIterator.next();
                if (obj instanceof SparseEntry) {
                    return ((SparseEntry) obj).entry;
                }

                return obj;
            }
            return stemIterator.next();
        }
    }

    /**
     * A specific iterator for the values of this stem. This should be used when
     * traversing all values, such as in {@link WhileLoop}s.
     *
     * @return
     */
    public Iterator valuesIterator() {
        return new ValueIterator();
    }

    public boolean hasValue(Object x) {
        for (Object k : keySet()) {
            Object v = get(k);
            if (v instanceof QDLStem) {
                if (((QDLStem) v).hasValue(x)) {
                    return true;
                }
            } else {
                if (v.equals(x)) {
                    return true;
                }
            }
        }
        return false;
    }

    public QDLStem mask(QDLStem stem2) {
        QDLStem result = newInstance();

        for (Object key : stem2.keySet()) {
            if (!containsKey(key)) {
                throw new IllegalArgumentException("'" + key + "' is not a key in the first stem. " +
                        "Every key in the second argument must be a key in the first.");
            }
            Object rawBit = stem2.get(key);
            if (!(rawBit instanceof Boolean)) {
                throw new IllegalArgumentException("every value of the second argument must be boolean");
            }
            Boolean b = (Boolean) rawBit;
            if (b) {
                result.putLongOrString(key, get(key));
            }
        }
        return result;
    }

    public QDLStem commonKeys(QDLStem arg2) {
        QDLStem result = newInstance();
        int index = 0;
        for (Object key : arg2.keySet()) {
            if (containsKey(key)) {
                String currentIndex = Integer.toString(index++);
                result.put(currentIndex, key);
            }
        }
        return result;
    }

    /*
     In point of fact, renameKeys does not handle list values, only string keys
      */
    public void renameKeys(QDLStem newKeys, boolean overWriteKeys) {
        for (Object oldKey : newKeys.keySet()) {
            Object newKey = newKeys.get(oldKey);
            if (newKey.equals(oldKey)) continue;
            if (containsKey(oldKey)) {
                if (containsKey(newKey)) {
                    if (overWriteKeys) {
                        Object oldValue = get(oldKey);
                        remove(oldKey);
                        putLongOrString(newKey, oldValue);
                    } else {
                        throw new IllegalArgumentException("'" + oldKey + "' is already a key. You  must explicitly overwrite it with the flag");
                    }
                } else {
                    Object oldValue = get(oldKey);
                    remove(oldKey);
                    putLongOrString(newKey, oldValue);
                }
            }
        }
    }

    /*  Quick basic of rename.
              b.OA2_foo := 'a';
              b.OA2_woof := 'b';
              b.OA2_arf := 'c';
              b.fnord := 'd';
              rename_keys(b., keys(b.)-'OA2_')
         */
    public QDLStem excludeKeys(QDLStem keyList) {

        QDLStem result = newInstance();
        for (Object key : keySet()) {
            if (key instanceof Long) {
                if (!keyList.containsValue((Long) key)) {
                    result.put((Long) key, get(key));
                }
            } else {
                if (!keyList.containsValue(key)) {
                    result.put((String) key, get(key));
                }
            }
        }
        return result;
    }

    public QDLStem includeKeys(QDLStem keyList) {
        QDLStem result = newInstance();
        for (int i = 0; i < keyList.size(); i++) {
            // for loop to be sure that everything is done in order.
            String index = Integer.toString(i);
            if (!keyList.containsKey(index)) {
                throw new IllegalArgumentException("the set of supplied keys is not a list");
            }
            String currentKey = keyList.getString(index);
            if (containsKey(currentKey)) {
                result.put(currentKey, get(currentKey));
            }
        }
        return result;
    }

    /**
     * Takes a stem  and returns a boolean list conformable to the argument.
     *
     * @param keyList
     * @return
     */
    public QDLStem hasKeys(QDLStem keyList) {
        QDLStem result = newInstance();
        if (!keyList.isList()) {
            throw new IllegalArgumentException("has keys requires a list");
        }
        for (Object ndx : keyList.getQDLList().orderedKeys()) {
            result.put((Long) ndx, containsKey(keyList.get((Long) ndx))); // since we know it's a list
        }
        return result;
    }

    /**
     * Modern successor to the deprecated {@link #hasKeys(QDLStem)}. This returns a left conformable
     * stem as it should.
     *
     * @param keyList
     * @return
     */
    public QDLStem hasKey(QDLStem keyList) {
        QDLStem result = newInstance();

        for (Object k : keySet()) {
            result.putLongOrString(k, keyList.containsKey(k));
        }
        return result;
    }

    /* ********
         IndexEntry operations
       ********* */
    /*
    Contract: E.g. a. has rank 4, b. has rank 2 then
    b.a.1.2.3.4.0 should resolve to b.(a.1.2.3.4).0 naturally.
    This would get the indices [1,2,3,4,0] and return
    [a.1.2.3.4, 0], i.e. zeroth element is the actual value (can even be a stem)
    This is used to overlay the stem in the calling function
    so arguments don't get lost
     */
    public IndexList get(IndexList indexList, boolean strictMatching) {
        return newGet(indexList, strictMatching);
    }

    /**
     * Strict matching is used at the last resolution of the stem. It means that left over scalars
     * are flagged as errors since there is no stem waiting to resolve them.
     *
     * @param indexList
     * @param strictMatching
     * @return
     */
    public IndexList newGet(IndexList indexList, boolean strictMatching) {
        if (indexList.get(indexList.size() - 1) instanceof QDLStem) {
            QDLStem ndx = (QDLStem) indexList.get(indexList.size() - 1);
            if (!ndx.isList()) {
                throw new IndexError("stem index list must be a list", null);
            }
            Object obj = null;
            QDLList qdlList = ndx.getQDLList();
            QDLStem lastStem = this;
            for (int i = 0; i < qdlList.size(); i++) {
                obj = lastStem.get(ndx.get(i));
                if (obj == null) {
                    throw new IndexError("the index of \"" + indexList.get(i) + "\" was not found in this stem", null);
                }
                if (obj instanceof QDLStem) {
                    lastStem = (QDLStem) obj;
                } else {
                    if (i != qdlList.size() - 1) {
                        throw new IndexError(" index depth error '" + obj + "' is not a stem.", null);

                    }
                }
            }
            IndexList rc = new IndexList();
            rc.add(obj);
            return rc;
        }
        IndexList rc = new IndexList();
        QDLStem currentStem = this;
        boolean gotOne = false;
        Object obj = null;
        for (int i = 0; i < indexList.size(); i++) {
            if (gotOne) {
                rc.add(indexList.get(i));
                continue;
            }
            if(indexList.get(i) instanceof AllIndices) {
                if(currentStem.hasDefaultValue()){
                    obj = currentStem.getDefaultValue();

                }else{
                    throw new NoDefaultValue("No default value for this stem", null);
                }

            }else{

                obj = currentStem.get(indexList.get(i));
            }
            if (obj == null) {
                if (hasDefaultValue()) {
                    obj = getDefaultValue();
                } else {
                    throw new IndexError("the index " + i + " in " + indexList + "\" was not found in this stem", null);
                }
            }

            if (obj instanceof QDLStem) {
                currentStem = (QDLStem) obj;
            } else {
                if (strictMatching && i != indexList.size() - 1) {
                    throw new IndexError("no such stem at  multi-index " + indexList, null);
                }
                rc.add(obj); // 0th entry is returned value
                gotOne = true;
            }
            if ((i == indexList.size() - 1) && !gotOne) {
                rc.add(currentStem); // result is a stem
            }
        }
        return rc;

    }

    public void set(IndexList indexList, Object value) {
        QDLStem currentStem = this;
        // Contract inside stems is that a variable is either a scalar
        // or a stem (since it would be very hard to differentiate these
        // in tail resolution.
        Object currentIndex;
        for (int i = 0; i < indexList.size() - 1; i++) {
            currentIndex = indexList.get(i);
            Object obj = currentStem.get(currentIndex);
            if (obj == null) {
                QDLStem newStem = newInstance();
                currentStem.myPut(currentIndex, newStem);
                currentStem = newStem;

            } else {
                if (obj instanceof QDLStem) {
                    currentStem = (QDLStem) currentStem.get(currentIndex);
                } else {
                    QDLStem newStem = newInstance();
                    currentStem.myPut(indexList.get(i), newStem);
                    currentStem = newStem;
                }

            }
        }
        currentStem.myPut(indexList.get(indexList.size() - 1), value);

    }

    public boolean remove(IndexList indexList) {
        QDLStem currentStem = this;
        for (int i = 0; i < indexList.size() - 1; i++) {
            String name = indexList.get(i) + STEM_INDEX_MARKER;
            QDLStem nextStem = (QDLStem) currentStem.get(name);
            if (nextStem == null) {
                throw new IndexError("could not find the given index '" + name + "' in this stem", null);
            }
            currentStem = nextStem;
        }
        // for last one. May be a variable or a stem
        Object lastIndex = indexList.get(indexList.size() - 1);
        switch (Constant.getType(lastIndex)) {
            case Constant.LONG_TYPE:
                currentStem.remove((Long) lastIndex);
                return true;
            case Constant.STRING_TYPE:
                currentStem.remove((String) lastIndex);
                return true;

        }
        return false;
    }


    protected void myPut(Object index, Object value) {
        if (index instanceof Long) {
            put((Long) index, value);
            return;
        }
        if (index instanceof String) {
            put((String) index, value);
            return;
        }
        if (index instanceof QDLStem) {
            QDLStem s = (QDLStem) index;
            IndexList indexList = new IndexList(s);
            set(indexList, value);
            return;
        }
        if (index instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) index;
            try {
                put(bd.longValueExact(), value);

            } catch (ArithmeticException arithmeticException) {
                // over flow, so it's too big to have as an index.
                BigInteger bi = bd.toBigIntegerExact();
                SparseEntry sparseEntry = new SparseEntry(bi, value);
                getQDLList().getSparseEntries().add(sparseEntry);

            }
            return;
        }
        throw new IndexError("Unknown index type for \"" + index + "\"", null);
    }

    /* ********
         MultiIndex operations
       ********* */

    /**
     * Note that the
     *
     * @param w
     * @return
     */
    public Object get(StemMultiIndex w) {
        QDLStem currentStem = this;
        /**
         * Drill down, checking everything exists.
         */
        for (int i = 0; i < w.getComponents().size() - 1; i++) {
            String name = w.getComponents().get(i) + STEM_INDEX_MARKER;
            QDLStem nextStem = (QDLStem) currentStem.get(name);
            if (nextStem == null) {
                throw new IndexError("could not find the given index \"" + name + "\" in this stem \"" + w.getName() + STEM_INDEX_MARKER, null);
            }
            currentStem = nextStem;
        }
        // for last one. May be a variable or a stem
        if (w.isStem()) {
            return currentStem.get(w.getLastComponent() + STEM_INDEX_MARKER);
        } else {
            return currentStem.get(w.getLastComponent());
        }
    }

    public void set(StemMultiIndex w, Object value) {
        QDLStem currentStem = this;
        /**
         * Drill down to next. If this is a completely new variable, may have to make all
         * the ones in between.
         */
        for (int i = 0; i < w.getComponents().size() - 1; i++) {
            //   String name = w.getComponents()[i] + STEM_INDEX_MARKER;
            String name = w.getComponents().get(i);
            Object object = currentStem.get(name);
            QDLStem nextStem = null;
            if (object instanceof QDLStem) {
                nextStem = (QDLStem) object;
            }
            if (nextStem == null) {
                nextStem = newInstance();
                currentStem.put(name, nextStem);
            }
            currentStem = nextStem;
        }
        // for last one
        if (w.isStem()) {
            currentStem.put(w.getLastComponent() + STEM_INDEX_MARKER, value);
        } else {
            currentStem.put(w.getLastComponent(), value);
        }
    }

    public void remove(StemMultiIndex w) {
        QDLStem currentStem = this;
        /**
         * Drill down, checking everything exists.
         */
        for (int i = 0; i < w.getComponents().size() - 1; i++) {
            String name = w.getComponents().get(i) + STEM_INDEX_MARKER;
            QDLStem nextStem = (QDLStem) currentStem.get(name);
            if (nextStem == null) {
                throw new IndexError("could not find the given index \"" + name + "\" in this stem \"" + w.getName() + STEM_INDEX_MARKER, null);
            }
            currentStem = nextStem;
        }
        // for last one. May be a variable or a stem
        if (w.isStem()) {
            currentStem.remove(w.getLastComponent() + STEM_INDEX_MARKER);
        } else {
            currentStem.remove(w.getLastComponent());
        }
    }

    protected KeyRankMap allKeys2() {
        KeyRankMap keyRankMap = new KeyRankMap();
        for (Object key : keySet()) {
            List list = new ArrayList();
            list.add(key);
            Object v = get(key);
            if (v instanceof QDLStem) {
                indicesByRank((QDLStem) v, list, keyRankMap);
            } else {
                keyRankMap.put(list);
            }
        }
        return keyRankMap;
    }

    /**
     * Get by axis. So if axis is 0, top level keys only, axis == 1 yields rank 2 keys
     *
     * @param axis
     * @return
     */
    public QDLStem keysByAxis(Long axis) {
        ARGetkeys arGetkeys = new ARGetkeys(axis.intValue());
        //return (QDLStem) QDLAggregateUtil.process(this, arGetkeys);
        QDLAggregateUtil.process(this, arGetkeys);
        QDLStem qdlStem = new QDLStem();
        qdlStem.setQDLList(arGetkeys.getAccumulator());
        return qdlStem;
    }

    public static class ARGetkeys extends AxisRestrictionIdentity {
        public ARGetkeys(int axis) {
            this.axis = axis;
        }

        /**
         * Flat list of all keys. Otherwise this returns a structured list of each
         * key in its proper location.
         *
         * @return
         */
        public QDLList getAccumulator() {
            return accumulator;
        }

        QDLList accumulator = new QDLList();

        @Override
        public Object getDefaultValue(List<Object> index, Object key, Object value) {
            QDLStem stem = new QDLStem();
            stem.getQDLList().appendAll(index);
            stem.getQDLList().append(key);
            accumulator.append(stem);
            return stem;
        }
    }

    /**
     * Return the indices of a given rank for this stem.
     *
     * @param rank
     * @return
     */
    public QDLStem indicesByRank(Long rank) {
        KeyRankMap keysByRank = allKeys2();
        //keysByRank.;
        if (rank == 0L) {
            QDLStem stemVariable = newInstance();
            if (!keysByRank.containsKey(1)) {
                return newInstance();
            }
            List<List> list = keysByRank.get(1);
            for (List list1 : list) {
                stemVariable.addList(list1);
            }
            return stemVariable;
        }
        int targetRank = rank.intValue();
        if (targetRank < 0) {
            long x = rank % keysByRank.lastKey(); // get it in the right range
            if (x == 0) {
                targetRank = 1; // They asked for a non-zero rank, so assume that is what they want.
            } else {
                targetRank = x < 0L ? (int) (keysByRank.lastKey() + x + 1) : rank.intValue();
            }
        }

        if (!keysByRank.containsKey(targetRank)) {
            return newInstance();
        }
        List list = keysByRank.get(targetRank);
        return convertKeyByRank(list);
    }

    /**
     * Gets a list of lists, e.g.
     * <pre>
     *  [[foo, 0], [foo, 1], [foo, 2], [foo, tyu]]
     *  </pre>
     * and returns a stem of these.
     *
     * @param list
     * @return
     */
    protected QDLStem convertKeyByRank(List<List> list) {
        QDLStem stemVariable = newInstance();
        long i = 0;
        for (List obj : list) {
            QDLStem index = newInstance();
            index.addList(obj);
            stemVariable.put(i++, index);
        }
        return stemVariable;
    }

    protected void indicesByRank(QDLStem v, List list, KeyRankMap keyRankMap) {
        for (Object key : v.keySet()) {
            List list2 = new ArrayList();
            list2.addAll(list);
            list2.add(key);
            if (v.get(key) instanceof QDLStem) {
                indicesByRank((QDLStem) v.get(key), list2, keyRankMap);
            } else {
                keyRankMap.put(list2);
            }
        }

    }

    /**
     * Returns a <b>flat list</b> of all indices at a given rank. Each element is a stem.
     * Special case is rank of 0 returns the same as rank == 1, except the elements are scalars,
     * not stems with a single element.
     *
     * @return
     */
    public QDLStem indicesByRank() {
        KeyRankMap keysByRank = allKeys2();
        // really simple case of a basic list with no structure. Just return the elements in a stem
        if (keysByRank.size() == 1 && keysByRank.keySet().iterator().next() == 1) {
            //List indices = keysByRank.get(1);
            return convertKeyByRank(keysByRank.get(1));
            //rc.addList(indices);
            //return rc;
        }
        QDLStem rc = new QDLStem();
        for (Integer i : keysByRank.keySet()) {
            QDLStem stemVariable = convertKeyByRank(keysByRank.get(i));
            for (Object key : stemVariable.keySet()) {
                rc.getQDLList().append(stemVariable.get(key));
            }
        }
        return rc;
    }

    /* ***************
       JSON conversions
       *************** */


    /**
     * Converts this to a JSON object. Names of stem components are decoded. So if you have a stem, a.,
     * with component $23foo, then a.$23foo yields
     * <pre>
     *     {"#foo":...}
     * </pre> I.e. the $23 is treated as an escaped name
     * and converted back. If you do not want stem names escaped when converting to JSON, then use
     * {@link #toJSON(boolean, int)} with the argument being <b>false</b>. In that case the outputted JSON would be
     * <pre>
     *     {"$23foo":...}
     * </pre>
     *
     * @return
     */
    public JSON toJSON() {
        return toJSON(false, -1); //
    }


    public JSON toJSON(boolean escapeNames, int type) {
        MetaCodec codec = escapeNames ? (new MetaCodec(type)) : null;

        if (getQDLMap().size() == 0 && getQDLList().size() == 0) {
            // Empty stem corresponds to an empty JSON Object
            return new JSONObject();
        }
        if (getQDLList().size() == size()) {
            return getQDLList().toJSON(escapeNames, type); // handles case of simple list of simple elements
        }
        JSONObject json = new JSONObject();
        //edge case. empty stem list should return a JSON object
        if (size() == 0) {
            return json;
        }
        if (getQDLMap().size() == 0) {
            // super.size counts the number of non-stem entries. This means there are
            // list elements and nothing else.
            // if it is just a list, return it asap.
            return getQDLList().toJSON(escapeNames, type);
        }
        QDLList localSL = new QDLList();
        localSL.addAll(getQDLList());

        // Special case of a JSON array of objects that has been turned in to a stem list.
        // We want to recover this since it is a very common construct.
        for (String key : getQDLMap().keySet()) {
            Object object = get(key);

            if (object instanceof QDLStem) {
                QDLStem x = (QDLStem) object;

                // compound object

                String newKey = key;
                if (newKey.endsWith(STEM_INDEX_MARKER)) {
                    newKey = key.substring(0, key.length() - 1);
                }
                if (isLongIndex(newKey)) {
                    SparseEntry sparseEntry = new SparseEntry(Long.parseLong(newKey));
                    if (!localSL.contains(sparseEntry)) {
                        sparseEntry.entry = x;
                        localSL.add(sparseEntry);
                    } else {
                        throw new IndexError("The stem contains a list element '" + newKey + "' " +
                                "and a stem entry '" + key + "'. This is not convertible to a JSON Object", null);
                    }
                } else {
                    json.put(escapeNames ? codec.decode(newKey) : newKey, x.toJSON(escapeNames, type));
                }

            } else {
                if (!(object instanceof QDLNull)) {
                    // don't add it if it is null.
                    json.put(escapeNames ? codec.decode(key) : key, object);
                }
            }
        }
        // This is here because what it does is it checks that stem lists have all been added.
        // remove this and stem lists don't get processed right.
        if (localSL.size() == size()) {
            return localSL.toJSON(); // Covers 99.9% of all cases for lists of compound objects.
        }


        // now for the messy bit -- lists
        // At this point there were no collisions in the indices.
        JSONArray array = getQDLList().toJSON();
        if (!array.isEmpty()) {
            for (int i = 0; i < array.size(); i++) {
                json.put(i, array.get(i));
            }
        }

        return json;
    }


    public QDLStem fromJSON(JSON json) {
        if (json instanceof JSONObject) {
            return fromJSON((JSONObject) json, false, -1);
        }
        if (json instanceof JSONArray) {
            return fromJSON((JSONArray) json, false, -1);
        }
        throw new IllegalArgumentException("argument is neither a JSON object nor JSON array");
    }

    public QDLStem fromJSON(JSONObject jsonObject) {
        return fromJSON(jsonObject, false, -1);
    }

    /**
     * Populate this from a JSON object. Note that JSON arrays are turned in to stem lists.
     *
     * @param jsonObject return this object, populated
     */
    public QDLStem fromJSON(JSONObject jsonObject, boolean convertVars, int type) {
        MetaCodec codec = convertVars ? (new MetaCodec(type)) : null;
        for (Object k : jsonObject.keySet()) {
            String key = k.toString();

            Object v = jsonObject.get(k);
            if (v instanceof JSONObject) {
                QDLStem x = newInstance();
                if (convertVars) {
                    put(codec.encode(key) + STEM_INDEX_MARKER, x.fromJSON((JSONObject) v, convertVars, type));
                } else {
                    put(key + STEM_INDEX_MARKER, x.fromJSON((JSONObject) v, convertVars, type));
                }
            } else {
                if (v instanceof JSONArray) {
                    QDLStem x = newInstance();
                    if (convertVars) {
                        put(codec.encode(key) + STEM_INDEX_MARKER, x.fromJSON((JSONArray) v, convertVars, type));
                    } else {
                        put(key + STEM_INDEX_MARKER, x.fromJSON((JSONArray) v, convertVars, type));
                    }
                } else {
                    if (convertVars) {
                        put(codec.encode(key), v);
                    } else {
                        if (v instanceof Integer) {
                            put(key, ((Integer) v).longValue());
                        } else if (v instanceof Float) {
                            put(key, new BigDecimal(Float.toString((Float) v)));
                        } else if (v instanceof Double) {
                            put(key, new BigDecimal(Double.toString((Double) v)));
                        } else {
                            if (v instanceof String && v.equals(QDLConstants.JSON_QDL_NULL)) {
                                put(key, QDLNull.getInstance());
                            } else {
                                put(key, v);
                            }
                        }
                    }
                }
            }
        }

        return this;
    }

    public QDLStem fromJSON(JSONArray array, boolean convert, int type) {
        for (int i = 0; i < array.size(); i++) {
            Object v = array.get(i);
            if (v instanceof JSONObject) {
                QDLStem x = newInstance();
                put((long) i, x.fromJSON((JSONObject) v, convert, type));
            } else {
                if (v instanceof JSONArray) {
                    QDLStem x = newInstance();
                    put((long) i, x.fromJSON((JSONArray) v, convert, type));
                } else {
                    //   sl.add(new StemEntry(i, v));
                    if (v instanceof Integer) {
                        put((long) i, ((Integer) v).longValue());
                    } else if (v instanceof Float) {
                        put((long) i, new BigDecimal(Float.toString((Float) v)));
                    } else if (v instanceof Double) {
                        put((long) i, new BigDecimal(Double.toString((Double) v)));
                    } else {
                        if ((v instanceof String) && QDLConstants.JSON_QDL_NULL.equals(v)) {
                            put((long) i, QDLNull.getInstance());
                        } else {
                            put((long) i, v);
                        }
                    }
                }
            }
        }
        return this;
    }

    /* ***************
       toString methods
       *************** */

    /**
     * Used as a utility, this prints with an indent relative to a current indent.
     * @param indentFactor
     * @param currentIndent
     * @return
     */
    public String toString(int indentFactor, String currentIndent) {
        if (isEmpty()) {
            if (hasDefaultValue()) {
                return "[]~{*:" + getDefaultValue() + "}";
            }
            return "[]";
        }
        String list = null;
        try {
            if (!getQDLList().isEmpty()) {
                list = getQDLList().toString(indentFactor, currentIndent);
                if (isList()) {
                    if (getDefaultValue() != null) {
                        list = "{*:" + getDefaultValue() + "}~" + list;
                    }
                    return list;
                }
            }
        } catch (QDLList.seGapException x) {
            //rock on
        }
        String output = currentIndent + "{\n";
        boolean isFirst = true;
        if (getDefaultValue() != null) {
            isFirst = false;
            output = output + "*:" + getDefaultValue();
        }
        String newIndent = currentIndent + StringUtils.getBlanks(indentFactor);
        for (String key : getQDLMap().keySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                output = output + ",\n";
            }
            Object o = get(key);
            if (o instanceof QDLStem) {
                output = output + newIndent + key + STEM_ENTRY_CONNECTOR + ((QDLStem) o).toString(indentFactor, newIndent);
            } else {
                output = output + newIndent + key + STEM_ENTRY_CONNECTOR + convert(o);
            }

        }
        if (list == null) {
            // now for any list
            long i = 0;

            for (Object obj : getQDLList()) {
                String temp = null;
                if (obj instanceof SparseEntry) {
                    SparseEntry entry = (SparseEntry) obj;
                    temp = newIndent + entry.index + STEM_ENTRY_CONNECTOR + convert(entry.entry);
                } else {
                    temp = newIndent + (i++) + STEM_ENTRY_CONNECTOR + convert(obj);
                }
                if (isFirst) {
                    isFirst = false;
                } else {
                    output = output + ",\n";
                }
                output = output + temp;
            }
        } else {
            output = list + "~" + output;

        }
        return output + "\n" + currentIndent + "}";

    }

    public String toString(int indentFactor) {
        return toString(indentFactor, "");
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Whether or not to show default values. This shows the top level, but not any
     * embedded stems, or the output becomes huge and unreadable fast.
     * @param showDefaultValues
     * @return
     */
    protected String toString(boolean showDefaultValues) {
        String list = null;
        try {
            if (!getQDLList().isEmpty()) {
                list = getQDLList().toString();
                if (isList()) {
                    if (showDefaultValues && hasDefaultValue()) {
                        list = "{*:" + getDefaultValue() + "}~" + list;
                    }
                    return list;
                }
            }
        } catch (QDLList.seGapException x) {
            //rock on. Just means the list is sparse so use full notation.
        }
        if (isEmpty()) {
            String out = "";
            if(showDefaultValues && hasDefaultValue()){
                out = out + "{*:" + getDefaultValue() + "}~";
            }
            return out + "[]";
        }
        String output = "{";
        boolean isFirst = true;

        if (showDefaultValues && hasDefaultValue()) {
            if (getDefaultValue() instanceof BigDecimal) {
                output = output + "*:" + InputFormUtil.inputForm((BigDecimal) getDefaultValue());
            } else {
                output = output + "*:" + getDefaultValue();
            }
            isFirst = false;
        }
        StemKeys keys = keySet();
        for (Object key : keys) {
            if (isFirst) {
                isFirst = false;
            } else {
                output = output + ", ";
            }
            Object vv = get(key);

            String ss;
            if (vv instanceof BigDecimal) {
                ss = InputFormUtil.inputForm((BigDecimal) vv);
            } else {
                if(vv instanceof QDLStem){
                    ss = ((QDLStem)vv).toString(false);

                }else{
                    ss = vv.toString();
                }
            }
            output = output + key + STEM_ENTRY_CONNECTOR + ss;
        }

        return output + "}";

    }

    /* ***************
       input form
       *************** */
    public String inputForm() {
        String list = null;
        try {
            if (!getQDLList().isEmpty()) {
                list = getQDLList().inputForm();
                if (isList()) {
                    if (getDefaultValue() != null) {
                        list = "{*:" + InputFormUtil.inputForm(getDefaultValue()) + "}~" + list;
                    }
                    return list;
                }
            }
        } catch (QDLList.seGapException x) {
            //rock on. Just means the list is sparse so use full notation.
        }
        if (isEmpty()) {
            if (hasDefaultValue()) {
                return "{*:" + InputFormUtil.inputForm(getDefaultValue()) + "}";
            }
            // Make SURE nothing else evaluates since this would return {}
            return "[]";
        }
        String output = "{";
        boolean isFirst = true;

        if (getDefaultValue() != null) {
            output = output + "*:" + InputFormUtil.inputForm(getDefaultValue());
            isFirst = false;
        }
        Set<String> keys;
        if (list == null) {
            keys = keySet(); // process everything here.
        } else {
            keys = getQDLMap().keySet(); // only process proper stem entries.
            output = list + "~" + output;
        }
        for (Object key : keys) {
            if (isFirst) {
                isFirst = false;
            } else {
                output = output + ", ";
            }
            output = output + InputFormUtil.inputForm(key) + STEM_ENTRY_CONNECTOR + InputFormUtil.inputForm(get(key));
        }

        return output + "}";

    }

    public String inputForm(int indentFactor) {
        return inputForm(indentFactor, "");
    }

    public String inputForm(int indentFactor, String currentIndent) {
        String list = null;
        try {
            if (!getQDLList().isEmpty()) {
                list = getQDLList().inputForm(indentFactor, currentIndent);
                if (isList()) {
                    if (getDefaultValue() != null) {
                        list = "{*:" + InputFormUtil.inputForm(getDefaultValue()) + "}~" + list;
                    }
                    return list;
                }
            }
        } catch (QDLList.seGapException x) {
            //rock on
        }
        String output = currentIndent + "{\n";
        boolean isFirst = true;
        if (getDefaultValue() != null) {
            isFirst = false;
            output = output + "*:" + InputFormUtil.inputForm(getDefaultValue());
        }
        String newIndent = currentIndent + StringUtils.getBlanks(indentFactor);
        for (String key : getQDLMap().keySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                output = output + ",\n";
            }
            Object o = get(key);
            output = output + newIndent + InputFormUtil.inputForm(key) + STEM_ENTRY_CONNECTOR + InputFormUtil.inputForm(get(key));
        }
        if (list == null) {
            // now for any list
            long i = 0;
            isFirst = true;
            for (Object obj : getQDLList()) {
                String temp = null;
                if (obj instanceof SparseEntry) {
                    SparseEntry entry = (SparseEntry) obj;
                    temp = newIndent + InputFormUtil.inputForm(entry.index) + STEM_ENTRY_CONNECTOR + InputFormUtil.inputForm(entry.entry);
                } else {
                    temp = newIndent + InputFormUtil.inputForm(i++) + STEM_ENTRY_CONNECTOR + InputFormUtil.inputForm(obj);
                }
                if (isFirst) {
                    isFirst = false;
                } else {
                    output = output + ",\n";
                }
                output = output + temp;
            }
        } else {
            output = list + "~" + output;

        }
        return output + "\n" + currentIndent + "}";

    }

    /**
     * Removes every value in the collection from everywhere in the stem.
     * Optionally,
     * lists will be reordered too. Since this a bit more
     *
     * @param c
     * @param reorderLists
     * @return
     */
    public boolean removeAllByValues(Collection c, boolean reorderLists) {
        // list remove is a bit optimized, so just do that if possible.
        boolean rc = getQDLList().removeAllByValue(c, reorderLists);
        for (Object obj : c) {
            rc = rc && removeAllByValue(obj, reorderLists, true);
        }
        return rc;
    }

    public boolean removeAllByValue(Object c, boolean reorderLists) {
        return removeAllByValue(c, reorderLists, false);
    }

    public boolean removeAllByValue(Object c, boolean reorderLists, boolean listProcessed) {
        boolean rc = true;
        if (!listProcessed) {
            // remove from list
            rc = rc && getQDLList().removeAllByValue(c, reorderLists);
        }
        // remove from map.
        Collection keysToRemove = new ArrayList();
        for (Object key : getQDLMap().keySet()) {
            Object value = get(key);
            if (value instanceof QDLStem) {
                rc = rc && ((QDLStem) value).removeAllByValue(c, reorderLists);
            } else {
                if (value.equals(c)) {
                    keysToRemove.add(key);
                }
            }
        }
        for (Object key : keysToRemove) {
            getQDLMap().remove(key); // have to be carefule to remove this so no concurrent modification exception.
        }
        return rc;
    }

    /**
     * This will take a stem with embedded . and return the object at that index.<br/><br/>
     * E.g.<br/> <br/>
     * myStem.getByMultiIndex("a.b.c");<br/> <br/>
     * would return myStem.a.b.c;<br/>
     * <p>This just calls {@link #get(StemMultiIndex)}.</p>
     * <h3>Note</h3>
     * <p>In this call, you pass in the exact index you want, unlike {@link #get(StemMultiIndex)}
     * which accepts the name of the stem as the first argument</p>
     *
     * @param index
     * @return
     */
    public Object getByMultiIndex(String index) {
        // This adds a dummy first argument that is omitted in the get call. It is set to something that cannot
        // ever resolve to a valid variable name, hence avoids variable resolution.
        StemMultiIndex stemMultiIndex = new StemMultiIndex("^^^" + QDLStem.STEM_INDEX_MARKER + index);
        return get(stemMultiIndex);
    }


} // end class
