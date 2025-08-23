package org.qdl_lang.variables;

import org.qdl_lang.exceptions.IndexError;
import org.qdl_lang.exceptions.QDLException;
import org.qdl_lang.state.QDLConstants;
import org.qdl_lang.util.InputFormUtil;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.qdl_lang.variables.values.LongValue;
import org.qdl_lang.variables.values.QDLValue;

import java.io.Serializable;
import java.util.*;

/**
 * This is used internally by a stem to store its entries that have integer indices.
 * <p>Created by Jeff Gaynor<br>
 * on 2/20/20 at  8:39 AM
 */
public class QDLList<K extends QDLValue> implements List<K>, Serializable {


    @Override
    public boolean isEmpty() {
        return getArrayList().isEmpty() && getSparseEntries().isEmpty();
    }

    public ArrayList<QDLValue> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<QDLValue> arrayList) {
        this.arrayList = arrayList;
    }

    ArrayList<QDLValue> arrayList = new ArrayList();

    public boolean hasArrayList() {
        return !arrayList.isEmpty();
    }

    public TreeSet<SparseEntry> getSparseEntries() {
        return sparseEntries;
    }

    public void setSparseEntries(TreeSet<SparseEntry> sparseEntries) {
        this.sparseEntries = sparseEntries;
    }

    //public class QDLList<V extends SparseEntry> extends TreeSet<V> {
    TreeSet<SparseEntry> sparseEntries = new TreeSet<>();

    public boolean hasSparseEntries() {
        return !sparseEntries.isEmpty();
    }

    public QDLList subList(long startIndex, boolean includeStartIndex, long endIndex, boolean includeEndIndex) {
        return subListNEW(startIndex, includeStartIndex, endIndex, includeEndIndex);
    }

    /*
b. := [;15]
remove(b.4)
remove(b.7)
remove(b.10)
remove(b.11)
  subset(b., -4, 2)

  subset(b., -3)


subset(b., 10)
subset(b., 10, 10)
subset(b., 1000, 5)
subset(b., 3, 6)

     */

    /**
     * @param startIndex        - negative means start from end
     * @param includeStartIndex
     * @param count             - negative means rest of list from start index
     * @param includeEndIndex
     * @return
     */
    public QDLList subListNEW(long startIndex, boolean includeStartIndex, long count, boolean includeEndIndex) {
        if (isEmpty() || count == 0L) {
            return new QDLList();
        }
        if (0 <= startIndex) {
            if (0 < count) {
                // start of list, finite count
                return subsetBasicCase(startIndex, includeStartIndex, count, includeEndIndex);
            } else {
                //rest of list
                return subsetEndOfList(startIndex, includeStartIndex, count, includeEndIndex);
            }
        } else {
            if (0 < count) {
                return subsetEndofListFinite(startIndex, includeStartIndex, count, includeEndIndex);
                // end of list, finite count
            } else {
                return subsetEndOfListFromEnd(startIndex, includeStartIndex, count, includeEndIndex);
                // end of list, rest of list
            }
        }
    }

    private QDLList subsetEndofListFinite(long startIndex, boolean includeStartIndex, long count, boolean includeEndIndex) {
        if (!hasSparseEntries()) {
            startIndex = startIndex + getArrayList().size(); // fixes this for very simple case
            return subsetBasicCase(startIndex, includeStartIndex, count, includeEndIndex);
        }
        SparseEntry last = last();
        startIndex = startIndex + last.index;
        return subsetBasicCase(startIndex, includeStartIndex, count, includeEndIndex);
    }

    public QDLList subsetEndOfListFromEnd(long startIndex, boolean includeStartIndex, long count, boolean includeEndIndex) {
        // Now the hard case....
        long ss = -startIndex;
        if (getSparseEntries().size() <= ss) {
            // the requested start index spans the gap.
            ArrayList sparse = new ArrayList(getSparseEntries().size());
            // add them all
            for (SparseEntry se : getSparseEntries()) {
                sparse.add(se.entry);
            }
            // now get the stuff from the array list
            ArrayList otherAL = new ArrayList();
            otherAL.addAll(getArrayList().subList((int) (startIndex + getArrayList().size()), getArrayList().size()));
            otherAL.addAll(sparse);
            QDLList out = new QDLList();
            out.setArrayList(otherAL);
            return out;

        } else {
            long i = 0;
            ArrayList arrayList = new ArrayList();
            Iterator<SparseEntry> iterator = getSparseEntries().descendingIterator();
            while (iterator.hasNext()) {
                if (ss == i++) {
                    break;
                }
                arrayList.add(iterator.next().entry);
            }
            Collections.reverse(arrayList);
            QDLList out = new QDLList();
            out.setArrayList(arrayList);
            return out;
        }

    }

    public QDLList subsetBasicCase(long startIndex, boolean includeStartIndex, long count, boolean includeEndIndex) {
        // start is positive, count is positive
        // return count elements of list.
        ArrayList arrayList = new ArrayList((int) count);
        SparseEntry currentSE = null;
        for (long i = 0; i < count; i++) {
            if (startIndex + i < getArrayList().size()) {
                arrayList.add(getArrayList().get((int) (i + startIndex)));
            } else {
                if (!hasSparseEntries()) {
                    break;
                }
                if (currentSE == null) {
                    currentSE = new SparseEntry(startIndex);
                    currentSE = getSparseEntries().ceiling(currentSE);
                    if (currentSE == null) {
                        // no such element -- they overshot the list
                        QDLList out = new QDLList();
                        return out;
                    }

                    arrayList.add(currentSE.entry);
                    continue;
                }
                currentSE = new SparseEntry(currentSE.index + 1);
                currentSE = getSparseEntries().ceiling(currentSE);

                if (currentSE == null) {
                    break; // ran out of elements
                }
                arrayList.add(currentSE.entry);
            }

        }
        QDLList out = new QDLList();
        out.setArrayList(arrayList);
        return out;

    }

    public QDLList subsetEndOfList(long startIndex, boolean includeStartIndex, long count, boolean includeEndIndex) {
        if (startIndex < getArrayList().size()) {
            int ss = (int) startIndex;
            ArrayList arrayList = new ArrayList(size() - ss); // crude at best
            arrayList.addAll(getArrayList().subList(ss, getArrayList().size())); // rest of the list
            if (hasSparseEntries()) {
                for (SparseEntry se : getSparseEntries()) {
                    arrayList.add(se.entry);
                }
            }
            QDLList out = new QDLList();
            out.setArrayList(arrayList);
            return out;
        }
        ArrayList arrayList = new ArrayList();

        SparseEntry seNext = new SparseEntry(startIndex);
        SparseEntry se = getSparseEntries().ceiling(seNext);
        while (se != null) {
            arrayList.add(se.entry);
            seNext = new SparseEntry(se.index + 1);
            se = getSparseEntries().ceiling(seNext);
        }
        QDLList out = new QDLList();
        out.setArrayList(arrayList);
        return out;

    }


    public QDLList() {
        super();
    }

    public QDLList(long size) {
        if (Integer.MAX_VALUE < size) {
            throw new NotImplementedException("need to implement long lists");
        }
        getArrayList().ensureCapacity((int) size);
        for (long i = 0L; i < size; i++) {
            arrayList.add(new QDLValue(i));
        }
    }

    /**
     * Fill this list with size elements from the fill array. If size<length(fill)
     * then only size elements are taken. If size > length(fill), the elements of fill
     * are cyclically resused.
     *
     * @param size
     * @param fill
     */
    public QDLList(long size, QDLValue[] fill) {
        if (Integer.MAX_VALUE < size) {
            throw new NotImplementedException("need to implement long lists");
        }
        getArrayList().ensureCapacity((int) size);
        int fillSize = -1;
        if (fill != null && fill.length != 0) {
            fillSize = fill.length;
        }

        for (long i = 0L; i < size; i++) {
            if (fill == null) {
                arrayList.add(new LongValue(i));
            } else {
                QDLValue ooo = fill[(int) i % fillSize];
                arrayList.add(ooo);
            }
        }
    }

    /**
     * Runs over <i>every</i> entry in the stem list (including danglers).
     * result is a standard list (starts at 0, no gaps) of unique elements.
     *
     * @return
     */
    public QDLList unique() {
        HashSet<QDLValue> set = new HashSet();
        for (QDLValue obj : this) {
            if (obj.isStem()) {
                QDLStem ss = obj.asStem().almostUnique();
                set.addAll(ss.getQDLList().unique());
            } else {
                set.add(obj);
            }

        }
        QDLList qdlList1 = new QDLList();
        HashSet<QDLValue> hashSet1 = new HashSet();
        for (QDLValue obj : set) {
            hashSet1.add(obj);
        }

        for (QDLValue object : set) {
            qdlList1.append(object);
        }
        return qdlList1;
    }


    public K get(long index) {
        // Fixes https://github.com/ncsa/qdl/issues/47
        if (index < 0) {
            return getRelativeAddress(index);
        }
        return (K) getAbsoluteAddress(index);
    }

    /**
     * Used in cases where the index < 0 and we have to compute it relative to the other indices.
     * Note that for sparse entries, this can be expensive, so a few special cases are handled directly.
     * Searching a sparse list for a relative address will be at worst linear because of the way
     * {@link TreeSet} is implemented.
     *
     * @param originalIndex
     * @return
     */
    protected K getRelativeAddress(long originalIndex) {
        int s = size();
        long index = originalIndex + s;
        if (index < 0L) {
            // we' tried to wrap around once, but more than that should fail
            throw new IndexError("index " + originalIndex + " out of bounds for list of length " + s, null);
        }
        if (index < arrayList.size()) { // so it's in the array list unless the next condition fails
            return (K) arrayList.get((int) index);
        }
        // It's a sparse entry. A tree set may have entries like {100:3, 200:4} and
        // get originalIndex = -1 would mean returning the value associated with 200.
        if (originalIndex == -1) {
            return (K) getSparseEntries().last().entry;
        }
        index = index - arrayList.size(); // restricts to indices in Sparse entries.
        if (index == 0) {
            return (K) getSparseEntries().first().entry;
        }
        // neither first nor last, now we have to iterate. This is s-l-o-o-o-w.
        Iterator<SparseEntry> it = getSparseEntries().iterator();
        int i = 0;
        SparseEntry current = null;
        while (it.hasNext() && i <= index) {  // want to jump out at i == index.
            current = it.next();
            i++;
        }
        return (K) current.entry;
    }

    /*
      a.:= [;5]
      a.100 := 11
      a.200 := 12
      a.300 := 14
      a.400 := 15
      a.(-2)
      a.
     */
    protected QDLValue getAbsoluteAddress(long index) {
        if (index < arrayList.size()) { // so it's in the array list
            return arrayList.get((int) index);
        }
        // It's a sparse entry
        SparseEntry sparseEntry = new SparseEntry(index);
        if (!contains(sparseEntry)) return null;
        return getSparseEntries().floor(sparseEntry).entry;
    }

    /**
     * Remove is a bit different than a java list remove. We allow for gaps and
     * sparse arrays, so in Java [0,1,2,3,4] remove index 2 yields [0,1,3,4] -- still
     * has index 2. QDL would have a result of [0,1]~{3:3,4;4}
     *
     * @param index
     * @return
     */
    public boolean removeByIndex(long index) {
        if (index < 0L) {
            index = size() + index;
        }
        if (index < arrayList.size()) {
            for (long i = index + 1; i < arrayList.size(); i++) {
                SparseEntry sparseEntry = new SparseEntry(i, arrayList.get((int) i));
                getSparseEntries().add(sparseEntry);
            }
            ArrayList aa = new ArrayList();
            // Since subList returns a view of the list, clearing the list in the
            // next line throws a concurrent modification exception.
            // Have to add the elements to another list first.
            aa.addAll(arrayList.subList(0, (int) index));
            arrayList.clear();
            arrayList.addAll(aa);
            return true;
        }

        SparseEntry sparseEntry = new SparseEntry(index);
        return getSparseEntries().remove(sparseEntry);
    }

    /**
     * Find the largest element of this list and append the given object to the end of it. This
     * is also used internally to append sparse entries, so it accepts an Object
     *
     * @param obj
     */
    public void append(Object obj) {
        if (!hasSparseEntries()) {
            if (obj instanceof QDLValue) {
                getArrayList().add((QDLValue) obj);
            } else {
                getArrayList().add(new QDLValue(obj));
            }
            return;
        }

        SparseEntry newEntry;
        if (obj instanceof SparseEntry) {
            // in this case, stem entries are being added directly, so don't wrap them in a stem entry.
            newEntry = new SparseEntry(size(), ((SparseEntry) obj).entry); // argh Java requires a cast. If StemEntry is ever extended, this will break.
        } else {
            QDLValue vvv = QDLValue.asQDLValue(obj);

            if (isEmpty()) {
                newEntry = new SparseEntry(0L, vvv);
            } else {
                SparseEntry stemEntry = getSparseEntries().last();
                newEntry = new SparseEntry(stemEntry.index + 1, vvv);
            }
        }
        getSparseEntries().add(newEntry);
    }

    /**
     * Appends all elements in a list. Converts elements as needed
     *
     * @param objects
     */
    public void appendAll(List objects) {
        for (Object obj : objects) {
            add(QDLValue.asQDLValue(obj));
        }
    }

    public void append(QDLSet<QDLValue> set) {
        long index = 0L;
        if (!hasSparseEntries()) {
            getArrayList().addAll(set);
            return;
        }
        index = getSparseEntries().last().index;
        for (QDLValue k : set) {
            add(new SparseEntry(index++, k));
        }
    }


    public static class seGapException extends QDLException {
        // If there is a gap in the entries, fall back on stem notation.
        // All this exception needs is to exist.
    }

    public String toString(int indentFactor, String currentIndent) {
        String output = currentIndent + "[";
        String newIndent = currentIndent + StringUtils.getBlanks(indentFactor);
        boolean needsCRWithClosingBrace = true;
        boolean isFirst = true;
        for (long i = 0; i < size(); i++) {

            QDLValue obj = get(i);
            if (obj == null) {
                throw new seGapException();
            }
            if (obj.isStem()) {
                if (isFirst) {
                    isFirst = false;
                    output = output + "\n";
                } else {
                    output = output + ",\n";
                }
                output = output + newIndent + obj.asStem().toString(indentFactor, newIndent);
            } else {
                needsCRWithClosingBrace = false;
                if (isFirst) {
                    isFirst = false;
                    output = output + JSONAndStemUtility.convert(obj.getValue());
                } else {
                    output = output + "," + JSONAndStemUtility.convert(obj.getValue());
                }
            }
        }

        return output + (needsCRWithClosingBrace ? "\n" + newIndent + "]" : "]");
    }

    public String toString(int indentFactor) {
        return toString(indentFactor, "");
    }

    @Override
    public String toString() {
        String output = "[";
        boolean isFirst = true;
        for (long i = 0; i < size(); i++) {
            if (isFirst) {
                isFirst = false;
            } else {
                output = output + ",";
            }
            QDLValue obj = get(i);
            if (obj == null) {
                throw new seGapException();
            }
            String vv;
            if (obj.isDecimal()) {
                vv = InputFormUtil.inputForm(obj.asDecimal());
            } else {
                vv = obj.toString();
            }
            output = output + vv;
        }

        return output + "]";
    }

    /**
     * This exports the current list as a {@link JSONArray}. Note that there is no
     * analog for importing one -- use the {@link QDLStem#fromJSON(JSONObject)}
     * to do that, since the result will in general be a stem (if one element of the
     * array is a JSONObject, then the index has to make it a stem -- this is just how the
     * bookkeeping is done).
     *
     * @return
     */
    public JSONArray toJSON() {
        return toJSON(false, -1);
    }

    /**
     * Converts to JSON elements.
     * @param escapeNames
     * @param conversionAlgorithm
     * @return
     */
    public JSONArray toJSON(boolean escapeNames, int conversionAlgorithm) {
        JSONArray array = new JSONArray();
        for (QDLValue element : getArrayList()) {
            if (element.isStem()) {
                array.add(element.asStem().toJSON(escapeNames, conversionAlgorithm));
            } else {
                if (element.isNull()) {
                    array.add(QDLConstants.JSON_QDL_NULL);
                } else {
                    array.add(element.getValue());
                }
            }
        }
        for (SparseEntry s : getSparseEntries()) {
            QDLValue v = s.entry;
            if (v.isStem()) {
                array.add(v.asStem().toJSON(escapeNames, conversionAlgorithm));
            } else {
                array.add(v.getValue());
            }
        }
        return array;
    }


    public String inputForm(int indent) {
        return inputForm(indent, "");
    }


    public String inputForm() {
        String output = "[";
        boolean isFirst = true;

        for (long i = 0; i < size(); i++) {
            if (isFirst) {
                isFirst = false;
            } else {
                output = output + ",";
            }
            QDLValue qdlValue = get(i);
            if (qdlValue == null) {
                throw new seGapException();
            }
            output = output + qdlValue.getInputForm();
        }
        return output + "]";
    }

    public String inputForm(int indentFactor, String currentIndent) {
        String output = currentIndent + "[\n";
        String newIndent = currentIndent + StringUtils.getBlanks(indentFactor);

        boolean isFirst = true;
        for (long i = 0; i < size(); i++) {
            if (isFirst) {
                isFirst = false;
            } else {
                output = output + ",\n";
            }
            QDLValue obj = get(i);
            if (obj == null) {
                throw new seGapException();
            }
            output = output + newIndent + obj.getInputForm();
        }

        return output + "\n]";
    }


    /**
     * Convert this to an array of objects. Note that there may be gaps
     * filled in with null values if this is sparse.
     *
     * @param noGaps     - if true, truncates array at first encountered gap
     * @param allowStems - if true, hitting a stem throws an exception.
     * @return
     */
    public QDLValue[] toArray(boolean noGaps, boolean allowStems) {
        if (!hasSparseEntries()) {
            QDLValue[] array = new QDLValue[getArrayList().size()];
            return getArrayList().toArray(array);
        }
        if (noGaps) {
            QDLValue[] r = new QDLValue[getArrayList().size()];
            int ii = 0;
            for (QDLValue ooo : getArrayList()) {
                if (!allowStems && (ooo.isStem())) {
                    throw new IllegalArgumentException("error: a stem is not allowed in this list");
                }
                r[ii++] = ooo;
            }
            return r;
        }

        QDLValue[] r = new QDLValue[size()];
        int ii = 0;
        for (QDLValue ooo : getArrayList()) {
            r[ii++] = ooo;
        }
        // now handle sparse entries. All that is left is to fill in
        for (SparseEntry sparseEntry : getSparseEntries()) {
            if (!allowStems && (sparseEntry.entry.isStem())) {
                throw new IllegalArgumentException("error: a stem is not allowed in this list");
            }
            r[ii++] = sparseEntry.entry;
        }
        return r;
    }

    /**
     * Get the dimension list for this object. dim(n(3,4,5)) == [3,4,5]<br/>
     * This is very simple minded and assumes rectangular arrays.
     *
     * @return
     */
    public QDLList dim() {
        QDLList s = new QDLList();
        if (isEmpty()) {
            return s;
        }
        long index = 0L;
        QDLValue qdlValue = get(0);
        s.add(new LongValue(size()));
        QDLValue currentEntry = qdlValue;
        while (currentEntry != null) {
            if (currentEntry.isStem()) {
                QDLStem s1 = currentEntry.asStem();
                if (s1.getQDLList().size() == 0) {
                    break;
                }
                s.add(new LongValue(s1.getQDLList().size()));
                currentEntry = s1.getQDLList().get(0L);
            } else {
                break;
            }
        }
        return s;
    }

    public Long getRank() {
        return (long) dim().size();
    }

    /**
     * Return all values for this list, including sparse values
     *
     * @return
     */
    public List<? extends QDLValue> values() {
        ArrayList<QDLValue> list = new ArrayList();
        Iterator iterator = iterator(true);
        while (iterator.hasNext()) {
            list.add((QDLValue) iterator.next());
        }
        return list;
    }

    @Override
    public int size() {
        return getArrayList().size() + getSparseEntries().size();
    }

    public int arraySize() {
        return getArrayList().size();
    }


    /**
     * Get the keys in a linked hash set. This is specifically for cases where stems have to
     * get them for loops.
     *
     * @return
     */
    public StemKeys orderedKeys() {
        StemKeys stemKeys = new StemKeys();
        TreeSet<LongValue> treeSet = new TreeSet<>();
        for (long i = 0; i < getArrayList().size(); i++) {
            treeSet.add(new LongValue(i));
        }
        for (SparseEntry sparseEntry : getSparseEntries()) {
            treeSet.add(new LongValue(sparseEntry.index));
        }
        stemKeys.setListkeys(treeSet);
        return stemKeys;
    }

    /**
     * Add every element in a collection to this list. It will <b>NOT</b> attempt to convert values, it
     * just appends them.
     *
     * @param c
     * @return
     */
    @Override
    public boolean addAll(Collection c) {

        // 16 cases
        if (c instanceof QDLList) {
            QDLList arg = (QDLList) c;
            Iterator<SparseEntry> sparseEntryIterator;
            int whichCase = (0 < arraySize() ? 1 : 0) + (0 < getSparseEntries().size() ? 2 : 0) + (0 < arg.arraySize() ? 4 : 0) + (0 < arg.getSparseEntries().size() ? 8 : 0);
            // Comment is
            // array, sparse, arg array, arg sparse =TFTF
            switch (whichCase) {
                case 0: // FFFF arg is empty
                case 1: // FTFF
                case 2: // TFFF
                case 3: // FTFF
                    return true;
                case 4: // FFTF arg has only an array
                case 5: // TFTF only array list all the way around
                    getArrayList().addAll(arg.getArrayList());
                    return true;
                case 6: // FTTF this is sparse, arg is not
                    long lowestIndex = getSparseEntries().first().index;
                    if (arraySize() + arg.arraySize() < lowestIndex) {
                        getArrayList().addAll(arg.getArrayList());
                        return true;
                    }
                case 7: // TTTF this has array list and sparse entries, arg has no sparse entries.
                    lowestIndex = getSparseEntries().first().index;
                    if (arraySize() + arg.arraySize() < lowestIndex) {
                        getArrayList().addAll(arg.getArrayList());
                        return true;
                    }
                    throw new NotImplementedException("Surgery needed");
                case 8: // FFFT empty, sparse
                case 9: // TFFT this has an array, arg has sparse entries only
                case 10: // FTFT both are only sparse
                    sparseEntryIterator = getSparseEntries().iterator();
                    while (sparseEntryIterator.hasNext()) {
                        arrayList.add(sparseEntryIterator.next().entry);
                    }
                    return true;
                case 11: // TTFT array entries, sparse entries, arg is sparse
                    throw new NotImplementedException("Surgery needed");
                case 12: // FFTT empty, arg has both
                case 13: // TFTT array not empty. both
                case 14: // FTTT sparse only, both
                    arrayList.addAll(arg.getArrayList());
                    sparseEntryIterator = getSparseEntries().iterator();
                    while (sparseEntryIterator.hasNext()) {
                        arrayList.add(sparseEntryIterator.next().entry);
                    }
                    return true;
                case 15: //TTTT both, both
                    throw new NotImplementedException("Surgery needed");
            }

        }
        for (Object o : c) {
            if (o instanceof QDLValue) {
                getArrayList().add((QDLValue) o);
            } else {
                getArrayList().add(new QDLValue(o));
            }
        }
        return true;
    }

    boolean isInt(long x) {
        return Integer.MIN_VALUE < x && x < Integer.MAX_VALUE;
    }

    public boolean hasIndex(long index) {
        if (index < getArrayList().size()) {
            return true;
        }
        SparseEntry entry = new SparseEntry(index);
        return getSparseEntries().contains(entry);
    }

    /**
     * Add an element in a sparse entry. This puts it in the right place and might adjust indices
     * accordingly.
     *
     * @param sparseEntry
     */
    public void set(SparseEntry sparseEntry) {
        set(sparseEntry.index, sparseEntry.entry);
    }

    public void set(long index, QDLValue element) {
        if (index == 0 && getArrayList().size() == 0) {
            // edge case
            getArrayList().add(element);
            return;
        }
        if (0 <= index) {
            setAbsoluteIndex(index, element);
            return;
        }
        // Fixes https://github.com/ncsa/qdl/issues/48
        setRelativeIndex(index, element);
    }

    /**
     * Set a relative value. Note that unlike absolute addresses, relative ones must exist prior to being
     * set. So a.42 can always be set (may result in a sparse entry) but a.(-42) requires there be at least 42 elements
     *
     * @param originalIndex
     * @param element
     */
    protected void setRelativeIndex(long originalIndex, QDLValue element) {
        int s = size();
        long index = originalIndex + s;
        if (index < 0L) {
            // we' tried to wrap around once, but more than that should fail
            throw new IndexError("index " + originalIndex + " out of bounds for list of length " + s, null);
        }
        if (index < getArrayList().size()) {
            getArrayList().set((int) index, element);
            return;
        }
        if (originalIndex == -1) {
            getSparseEntries().last().entry = element;
            return;
        }
        index = index - arrayList.size(); // restricts to indices in Sparse entries.
        if (index == 0) {
            getSparseEntries().first().entry = element;
            return;
        }

        Iterator<SparseEntry> it = getSparseEntries().iterator();
        int i = 0;
        SparseEntry current = null;
        while (it.hasNext() && i <= index) {  // want to jump out at i == index.
            current = it.next();
            i++;
        }
        current.entry = element;
    }

    protected void setAbsoluteIndex(long index, QDLValue element) {

        if (index < getArrayList().size()) {
            if (index < 0) {
                getArrayList().set((int) (getArrayList().size() + (index % getArrayList().size())), element);
            } else {
                getArrayList().set((int) index, element);
            }
            return;
        }
        if (getArrayList().size() == index) {
            getArrayList().add(element); // tack it on the end
            return;
        }
        SparseEntry sparseEntry = new SparseEntry(index, element);
        getSparseEntries().remove(sparseEntry);
        getSparseEntries().add(sparseEntry); // TreeSet only adds if it does not exist. Have to remove first
    }

    public void listInsertFrom(long startIndex, long length, QDLList source, long insertIndex) {
// set up
        if (length == 0L) return; // do nothing.

        if (!source.hasIndex(startIndex)) {
            throw new IllegalArgumentException("the start index in the source for this operation does not exist.");
        }

        if (length + startIndex > source.size()) {
            throw new IllegalArgumentException("the source does not have enough elements for this operation.");
        }

        List<QDLValue> sourceList = null;
        if (startIndex < source.getArrayList().size()) {
            sourceList = source.getArrayList().subList((int) startIndex, (int) (startIndex + length));
        }
        if (source.hasSparseEntries()) {
            if (sourceList == null) {
                SparseEntry fromIndex = new SparseEntry(startIndex);
                SparseEntry toIndex = new SparseEntry(startIndex + length);

                SortedSet<SparseEntry> sparseEntries = source.getSparseEntries().subSet(fromIndex, toIndex);
                sourceList = new ArrayList();
                for (SparseEntry sparseEntry : sparseEntries) {
                    sourceList.add(sparseEntry.entry);
                }
            }
        }
        if (insertIndex <= getArrayList().size()) {
            if (insertIndex < getArrayList().size()) {
                getArrayList().addAll((int) insertIndex, sourceList);
            }
            if (insertIndex == getArrayList().size()) {
                getArrayList().addAll(sourceList);
            }
            long offset = startIndex + length;
            if (hasSparseEntries()) {
                for (SparseEntry sparseEntry : getSparseEntries().descendingSet()) {
                    sparseEntry.index = sparseEntry.index + offset;
                }
            }
            normalizeIndices();
            return;
        }
        long offset = startIndex + length;
        if (hasSparseEntries()) {
            for (SparseEntry sparseEntry : getSparseEntries().descendingSet()) {
                if (sparseEntry.index < insertIndex) {
                    break;
                }
                sparseEntry.index = sparseEntry.index + offset - 1;
            }
            long index = insertIndex;
            if (sourceList != null) {
                for (QDLValue obj : sourceList) {
                    SparseEntry sparseEntry = new SparseEntry(index++, obj);
                    getSparseEntries().remove(sparseEntry);
                    getSparseEntries().add(sparseEntry);
                }
            }
            normalizeIndices();
        }
    }

    protected void normalizeIndices() {
        if (!hasSparseEntries()) return;

        long first = getSparseEntries().first().index;
        List<SparseEntry> removeList = new ArrayList<>();
        if (getArrayList().size() == first) {
            long lastIndex = first;
            for (SparseEntry sparseEntry : getSparseEntries()) {
                if (lastIndex + 1 < sparseEntry.index) {
                    break;
                }
                lastIndex++;
                getArrayList().add(sparseEntry.entry);
                removeList.add(sparseEntry);
            }
        }
        for (SparseEntry sparseEntry : removeList) {
            getSparseEntries().remove(sparseEntry);
        }
    }

    /*
  a. := [;10] ~{15:15, 16:16}
       a. := [;10]
       a.15 := 15;a.16 :=16;
  b. := [-10;0]
  insert_at(b., 1, 3, a., 5)
     */
    //  public void listCopyOrInsertFrom(long startIndex, long length, QDLList source, long insertIndex, boolean doCopy) {
    public void listCopyFrom(long startIndex, long length, QDLList source, long insertIndex) {

        if (length == 0L) return; // do nothing.

        if (!source.hasIndex(startIndex)) {
            throw new IllegalArgumentException("the start index in the source for this operation does not exist.");
        }

        if (length + startIndex > source.size()) {
            throw new IllegalArgumentException("the source does not have enough elements for this operation.");
        }

        List<QDLValue> sourceList = null;
        if (startIndex < source.getArrayList().size()) {
            sourceList = source.getArrayList().subList((int) startIndex, (int) (startIndex + length));
        }
        if (source.hasSparseEntries()) {
            if (sourceList == null) {
                SparseEntry fromIndex = new SparseEntry(startIndex);
                SparseEntry toIndex = new SparseEntry(startIndex + length);

                SortedSet<SparseEntry> sparseEntries = source.getSparseEntries().subSet(fromIndex, toIndex);
                sourceList = new ArrayList();
                for (SparseEntry sparseEntry : sparseEntries) {
                    sourceList.add(sparseEntry.entry);
                }
            }
        }

        // now for surgery...

        int index = 0;
        // No easy way to replace, just have to do it.
        for (QDLValue obj : sourceList) {
            int nextIndex = (int) insertIndex + index++;
            if (hasSparseEntries()) {
                SparseEntry nextSE = new SparseEntry(nextIndex, obj);
                if (hasArrayList()) {
                    // hard bit -- if this is overshooting the end of the array list
                    if (getSparseEntries().contains(nextSE)) {
                        getSparseEntries().remove(nextSE);
                        getSparseEntries().add(nextSE);
                    } else {
                        if (nextIndex < getArrayList().size()) {
                            getArrayList().set(nextIndex, obj);
                        } else {
                            getArrayList().add(obj);
                        }
                    }
                } else {
                    nextSE.entry = obj;
                    getSparseEntries().remove(nextSE);
                    getSparseEntries().add(nextSE);
                }
            } else {
                if (nextIndex < getArrayList().size()) {
                    getArrayList().set(nextIndex, obj);
                } else {
                    getArrayList().add(obj);
                }
            }
        }
        normalizeIndices();
    }
   /*
        a.:=[;10];
    remove(a.3)
    b.:=[-10;0]
      copy(b., 2, 5, a., 1)

    */

    /**
     * This iterates over the elements of this QDL list. It will do elements in the
     * array list -- so next returns the actual object -- and the index may be inferred.
     * Then it will iterate
     * over the elements of the sparse entries, which are {@link SparseEntry}
     * (if objectsOnly is false) and have the index too.
     */
    public static class MyIterator implements Iterator {
        Iterator<QDLValue> arrayIterator;
        Iterator<SparseEntry> sparseEntryIterator;
        boolean objectsOnly = false;

        public MyIterator(Iterator<QDLValue> arrayIterator, Iterator<SparseEntry> sparseEntryIterator, boolean objectsOnly) {
            this.arrayIterator = arrayIterator;
            this.sparseEntryIterator = sparseEntryIterator;
            this.objectsOnly = objectsOnly;
        }

        boolean doneWithArray = false;

        @Override
        public boolean hasNext() {
            return sparseEntryIterator.hasNext() || arrayIterator.hasNext();
        }

        @Override
        public Object next() {
            if (arrayIterator.hasNext()) {
                return arrayIterator.next();
/*
                if (obj instanceof SparseEntry) {
                    return ((SparseEntry) obj).entry;
                }

                return obj;
                */

            }
            if (objectsOnly) {
                return (sparseEntryIterator.next()).entry;
            }
            return sparseEntryIterator.next().entry;
        }
    }

    /**
     * Iterator over values. This is overloaded, so that if objectsOnly is true, just
     * {@link QDLValue}s are returned, otherwise, this will return a mix of values and sparse
     * entries.
     *
     * @param objectsOnly
     * @return
     */
    public Iterator iterator(boolean objectsOnly) {
        return new MyIterator(getArrayList().iterator(), getSparseEntries().iterator(), objectsOnly);
    }

    /**
     * Now we can do for-each loop constructs. See {@link MyIterator}.
     *
     * @return
     */
    @Override
    public Iterator iterator() {
        return iterator(false);
    }

    /**
     * Checks if this key (as a string or long) is an index in this list.
     *
     * @param o
     * @return
     */
    public boolean containsKey(Object o) {
        if (o instanceof SparseEntry) {
            if (getSparseEntries().contains(o)) {
                return true;
            }
            return ((SparseEntry) o).index < getArrayList().size();
        }
        Long index = null;
        if (o instanceof String) {
            index = Long.parseLong((String) o);
        }
        if (o instanceof Long) {
            index = (Long) o;
        }
        if (index < getArrayList().size()) {
            return true;
        }
        SparseEntry sparseEntry = new SparseEntry(index);
        return getSparseEntries().contains(sparseEntry);
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof SparseEntry) {
            if (getSparseEntries().contains(o)) {
                return true;
            }
            // It is possible this is trying to get the sparse entry by index.
            if(((SparseEntry) o).entry == null) return false;
            return getArrayList().contains(((SparseEntry) o).entry);
        }
        QDLValue qdlValue = QDLValue.asQDLValue(o);
        if (getArrayList().contains(qdlValue)) {
            return true;
        }
        // Now grunt work. Does a random object exist in the sparse entries
        if (hasSparseEntries()) {
            for (SparseEntry sparseEntry : getSparseEntries()) {
                if (sparseEntry.entry.equals(qdlValue)) return true;
            }
        }
        return false;
    }

    @Override
    public Object[] toArray() {
        ArrayList out = new ArrayList();
        out.addAll(arrayList);
        if (!getSparseEntries().isEmpty()) {
            int i = arrayList.size();
            for (SparseEntry sparseEntry : getSparseEntries()) {
                out.add(sparseEntry.entry);
            }
        }
        return out.toArray();
    }

    public List<String> toStringList() {
        return Arrays.asList(toStringArray());
    }

    /**
     * Convenience method. This takes the elements of this {@link QDLList}
     * and either returns their value as a string or invokes the standard {@link Object#toString()}
     * method. It is intended to allow for passing values to non_-QDL code. Since Java has no
     * concept of sparse entries, these are just appended in order to the end of the array.
     * @return
     */
    public String[] toStringArray() {
        String[] out = new String[size()];
        for (int i = 0; i < getArrayList().size(); i++) {
            if(getArrayList().get(i).isString()){
                out[i] = getArrayList().get(i).asString();
            }else{
                out[i] = getArrayList().get(i).toString();
            }
        }
        if(hasSparseEntries()){
            int i = getArrayList().size();
            for (SparseEntry sparseEntry : getSparseEntries()) {
                if(sparseEntry.entry.isString()){
                    out[i] = sparseEntry.entry.asString();
                }else{
                    out[i] = sparseEntry.entry.toString();
                }
            }
        }
     return out;
    }


    @Override
    public Object[] toArray(Object[] a) {
        throw new NotImplementedException();
/*
        if (a.length < this.size()) {

            Object[] outA = Arrays.copyOf(getArrayList().toArray(), getArrayList().size(), a.getClass());
            if(hasSparseEntries()) {
                int i = arrayList.size();
                for(SparseEntry sparseEntry : getSparseEntries()) {
                    outA[i] = QDLValue.asJavaValue(sparseEntry.entry);
                }
                return outA;
            }
        } else {
            // rest of contract is if a has too many elements, null out the rest.
            System.arraycopy(this.elementData, 0, a, 0, this.size);
            if (a.length > this.size) {
                a[this.size] = null;
            }

            return a;
        }
*/
    }

    public boolean add(Integer o) {
        return add(new LongValue(o));
    }

    public boolean add(SparseEntry sparseEntry) {
        if (hasSparseEntries()) {
            SparseEntry lastEntry = getSparseEntries().last();
            SparseEntry newEntry = new SparseEntry(lastEntry.index + 1, sparseEntry.entry);
            getSparseEntries().add(newEntry);
            return true;
        }else{
            getSparseEntries().add(sparseEntry);
            return true;
        }
    }

    public boolean add(QDLValue qdlValue) {

        if (hasSparseEntries()) {
            SparseEntry lastEntry = getSparseEntries().last();
            SparseEntry newEntry = new SparseEntry(lastEntry.index + 1, qdlValue);
            getSparseEntries().add(newEntry);
            return true;
        }
        return getArrayList().add(qdlValue);
    }

    public boolean remove(QDLValue qdlValue) {
        boolean rc = getArrayList().remove(qdlValue);
        if (rc) {
            return rc; // got first element
        }
        if (hasSparseEntries()) {
            SparseEntry removeIt = null;
            for (SparseEntry se : getSparseEntries()) {
                if (se.entry.equals(qdlValue.getValue())) {
                    removeIt = se;
                    break; // Contract for List is to remove *first* element by value
                }
            }
            if (removeIt == null) {
                return false;
            }
            return getSparseEntries().remove(removeIt);
        }
        return false;
    }

    public boolean remove(SparseEntry sparseEntry) {
        boolean rc = getArrayList().remove(sparseEntry.entry);
        if (rc) {
            return rc; // removed first element of the list
        }
        SparseEntry removeIt = null;

        for (SparseEntry se : getSparseEntries()) {
            if (se.entry.equals(sparseEntry.entry)) {
                removeIt = se;
                break; // Contract for List is to remove *first* element by value
            }
        }
        if (removeIt == null) {
            return false;
        }
        return getSparseEntries().remove(removeIt);
    }


    /**
     * Remove by value  from <b>top level</b> only. This fulfills Java's contract for
     * lists/collections, which removes <i>the first instance of this object <b>only</b></i>.
     * To fulfill QDL's contract, use {@link #removeAllByValue(QDLValue, boolean)}
     * <br/><br/>
     * This might pass in sparse entries, so those have to be taken into account.
     *
     * @param o
     * @return
     */
    @Override
    public boolean remove(Object o) {
        if (o instanceof SparseEntry) {
            return remove((SparseEntry) o);
        }
        if (o instanceof QDLValue) {
            return remove((QDLValue) o);
        }
        return false;
    }

    public boolean remove(Collection c) {
        boolean rc = true;
        for (Object obj : c) {
            rc = rc && remove(obj);
        }
        return rc;
    }


    /**
     * Fulfills QDL's contract to remove all elements by value.
     *
     * @param c
     * @param reorderLists
     * @return
     */
    public boolean removeAllByValue(Collection<QDLValue> c, boolean reorderLists) {
        ArrayList<QDLValue> newList = new ArrayList<>();
        if (reorderLists) {
            for (QDLValue v : getArrayList()) {
                if (v.isStem()) {
                    QDLStem vv = v.asStem();
                    vv.removeAllByValues(c, reorderLists);
                    if (!vv.isEmpty()) {
                        newList.add(QDLValue.asQDLValue(vv));
                    }
                } else {
                    if (!c.contains(v)) {
                        newList.add(v);
                    }
                }
            }
            for (SparseEntry sparseEntry : getSparseEntries()) {
                if (sparseEntry.entry.isStem()) {
                    QDLStem vv = sparseEntry.entry.asStem();
                    vv.removeAllByValues(c, reorderLists);
                    if (!vv.isEmpty()) {
                        newList.add(QDLValue.asQDLValue(vv));
                    }
                } else {
                    if (!c.contains(sparseEntry.entry)) {
                        newList.add(sparseEntry.entry);
                    }
                }
            }
            setSparseEntries(new TreeSet<>()); // never null.
            setArrayList(newList);
            return true;
        }
        boolean rc = true;
        for (Object obj : c) {
            QDLValue v;
            if (obj instanceof QDLValue) {
                v = (QDLValue) obj;
            } else {
                v = new QDLValue(obj);
            }
            rc = rc && removeAllByValue(v, reorderLists);
        }
        return rc;
    }

    /**
     * removes a single object from everywhere in this List.
     *
     * @param qdlValue
     * @return
     */
    protected boolean removeAllByValue(QDLValue qdlValue, boolean reorderLists) {
        List<Integer> removeList = new ArrayList<>(getArrayList().size());
        boolean rc = true;
        for (int i = 0; i < getArrayList().size(); i++) {
            QDLValue value = getArrayList().get(i);
            if (value.isStem()) {
                rc = rc && value.asStem().removeAllByValue(qdlValue, reorderLists);
            } else {
                if (value.equals(qdlValue)) {
                    removeList.add(i);
                }
            }
        }
        Collections.reverse(removeList);
        for (Integer ndx : removeList) {
            // Can't remove Integer, since that attempts to remove an Integer with
            // the given value. It must be an int to remove by index.
            getArrayList().remove(ndx.intValue());
        }
        if (!getSparseEntries().isEmpty()) {
            List<SparseEntry> removeSE = new ArrayList<>();
            for (SparseEntry sparseEntry : getSparseEntries()) {
                QDLValue value = sparseEntry.entry;
                if (value.isStem()) {
                    rc = rc && value.asStem().removeAllByValue(qdlValue, reorderLists);
                } else {
                    if (value.equals(qdlValue)) {
                        removeSE.add(sparseEntry);
                    }
                }
            }
            rc = rc && getSparseEntries().removeAll(removeSE);
        }
        return rc;
    }

    @Override
    public boolean containsAll(Collection c) {
        throw new NotImplementedException("containsAll(Collection)");
    }

    @Override
    public boolean addAll(int index, Collection c) {
        throw new NotImplementedException("addAll(int, Collection)");
    }

    /**
     * removes every element by value. The collection should be a collection of
     * {@link QDLValue}s, but the signature does not allow for that.
     *
     * @param c
     * @return
     */
    @Override
    public boolean removeAll(Collection c) {
        List<Integer> rr = new ArrayList<>();
        for (int i = 0; i < getArrayList().size(); i++) {
            QDLValue obj = getArrayList().get(i);
            if (obj.isStem()) {
                obj.asStem().removeAllByValues(c, false);
            } else {
                if (c.contains(obj)) {
                    rr.add(i);
                }
            }
        }
        Collections.reverse(rr);
        for (int i : rr) {
            getArrayList().remove(i);
        }
        getArrayList().removeAll(c);
        if (!getSparseEntries().isEmpty()) {
            // Yuck. No choice but to look everything up and remove them
            List<SparseEntry> removeList = new ArrayList<>();
            for (SparseEntry sparseEntry : getSparseEntries()) {
                for (Object object : c) {
                    if (sparseEntry.entry.equals(object)) {
                        removeList.add(sparseEntry);
                    }
                }
            }
            getSparseEntries().removeAll(removeList);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection c) {
        throw new NotImplementedException("retainAll(Collection)");
    }

    @Override
    public void clear() {
        setArrayList(new ArrayList());
        setSparseEntries(new TreeSet<>());
    }

    @Override
    public K get(int index) {
        return get((long) index);
    }

    /**
     * This is mostly used when hot-rodding performance. It <i>only</i> updates
     * the array list backing this object, not any sparse entries. Generally
     * only call it if you have a well-articulated need to do so.
     *
     * @param index
     * @param element
     * @return
     */
    @Override
    public QDLValue set(int index, QDLValue element) {
        return getArrayList().set(index, element);
    }

    @Override
    public void add(int index, QDLValue element) {
        throw new NotImplementedException("add(int, Object) -- need logic for sparse entries");
    }

    @Override
    public K remove(int index) {
        throw new NotImplementedException("remove(int)");
    }

    @Override
    public int indexOf(Object o) {
        throw new NotImplementedException("indexOf(Object)");
    }

    @Override

    public int lastIndexOf(Object o) {
        throw new NotImplementedException("lastIndexOf(Object)");
    }

    @Override
    public ListIterator listIterator() {
        throw new NotImplementedException("listIterator");
    }

    @Override
    public ListIterator listIterator(int index) {
        throw new NotImplementedException("listIterator(index)");
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return subList((long) fromIndex, true, (long) toIndex, false);
    }


    public SparseEntry last() {
        if (hasSparseEntries()) {
            return getSparseEntries().last();
        }
        if (getArrayList().isEmpty()) {
            throw new NoSuchElementException();
        }
        int index = getArrayList().size() - 1;
        return new SparseEntry(index, getArrayList().get(index));
    }

    public SparseEntry first() {
        if (hasArrayList()) {
            return new SparseEntry(0L, getArrayList().get(0));
        }
        if (hasSparseEntries()) {
            return getSparseEntries().first();
        }
        throw new NoSuchElementException();

    }

    public Iterator descendingIterator(boolean objectsOnly) {
        return new MyDescendingIterator(getSparseEntries().descendingIterator(), objectsOnly);
    }

    public class MyDescendingIterator implements Iterator {
        Iterator sparseEntryIterator;
        boolean objectsOnly = false;

        public MyDescendingIterator(Iterator sparseEntryIterator, boolean objectsOnly) {
            this.sparseEntryIterator = sparseEntryIterator;
            this.objectsOnly = objectsOnly;
        }

        boolean doneWithArray = false;
        int currentIndex = getArrayList().size() - 1;

        @Override
        public boolean hasNext() {
            if (sparseEntryIterator.hasNext()) return true;
            return -1 < currentIndex;
        }

        @Override
        public Object next() {
            if (sparseEntryIterator.hasNext()) {
                if (objectsOnly) {
                    return ((SparseEntry) sparseEntryIterator.next()).entry;
                }
                return sparseEntryIterator.next();
            }
            if (-1 < currentIndex) {
                return getArrayList().get(currentIndex--);
            }
            throw new NoSuchElementException();
        }

    }

 /*   public static void main(String[] args) {
        QDLList list = new QDLList(10L, new QDLValue[]{new StringValue("a")});
        System.out.println(list);
        list.appendAll(new QDLList(10L, new QDLValue[]{new StringValue("b")}));
        System.out.println(list);
        System.out.println(list.size());
    }*/

    /**
     * Keep this! It is not used by QDL though and won't show up in any searches of methods used.
     * This is an internal method used by the IDE for debugging. Supremely useful in that context.
     */
    protected String otherToString() {
        String x = getClass().getSimpleName() +
                "{ array[" + (hasArrayList() ? 0 : arrayList.size()) + "]=" + (hasArrayList() ? arrayList.toString() : "[], ");
        String se;
        if (hasSparseEntries()) {
            se = "sparseEntries[" + sparseEntries.size() + "]=";
            String ll = "{";
            boolean isFirst = true;
            for (SparseEntry sparseEntry : sparseEntries) {
                ll = ll + (isFirst ? "" : ",") + sparseEntry.index + ":" + sparseEntry.entry;
                if (isFirst) {
                    isFirst = false;
                }
            }
            se = se + ll + "}";
        } else {
            se = "sparseEntries[0]=[]";
        }
        x = x + se;
        x = x + "}";

        return x;
    }

    /**
     * A <b><i>very</i></b> specific utility, used in the transpose function. The assumptions are
     * <ul>
     *     <li>This list consists entirely indices to a stem, so all entries are longs</li>
     *     <li>All the indices are the same length</li>
     *     <li>The permutation will be applied to every entry</li>
     *     <li>No sparse entries</li>
     * </ul>
     * This is the case where a {@link QDLStem} has the indices in it and we need
     * to permute all of them for a transpose or other operation. This can be very slow
     * and clunky using QDL standard calls, so this is a backdoor for speed to grab the entries
     * directly and remap them. It is not a generally applicable function.
     *
     * @param permutation
     * @return
     */
    public QDLStem permuteEntries(List<QDLValue> permutation) {
        /*
        This uses the internal structure of the stem and lists, so this is seriously hot-rodding it.
         */
        QDLList out = new QDLList();
        int size = permutation.size();
        for (QDLValue ooo : getArrayList()) {
            QDLList qdlList = ooo.asStem().getQDLList(); // each stem has a list of n entries.
            QDLList outList = new QDLList(size);

            for (int index = 0; index < size; index++) {
                outList.set(index, qdlList.get(permutation.get(index).asLong()));
            }
            QDLStem outStem = new QDLStem(outList);
            out.append(outStem);
        }
        QDLStem r = new QDLStem(out);
        return r;
    }
}
