package org.qdl_lang.variables;

import org.qdl_lang.exceptions.IndexError;
import org.qdl_lang.exceptions.QDLException;
import org.qdl_lang.state.QDLConstants;
import org.qdl_lang.util.InputFormUtil;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * This is used internally by a stem to store its entries that have integer indices.
 * <p>Created by Jeff Gaynor<br>
 * on 2/20/20 at  8:39 AM
 */
public class QDLList implements List, Serializable {
    @Override
    public boolean isEmpty() {
        return getArrayList().isEmpty() && getSparseEntries().isEmpty();
    }

    public ArrayList getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList arrayList) {
        this.arrayList = arrayList;
    }

    ArrayList arrayList = new ArrayList();

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
            arrayList.add(i);
        }
    }

    /**
     * Fill this list with size elements from the fill array. If size<length(fill)
     * then only size elements are taken. If size > length(fill), the elements of fill
     * are cyclically resused.
     * @param size
     * @param fill
     */
    public QDLList(long size, Object[] fill) {
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
                arrayList.add(i);
            } else {
                arrayList.add(fill[(int) i % fillSize]);
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
        HashSet set = new HashSet();
        for (Object obj : this) {
            if (obj instanceof QDLStem) {
                QDLStem ss = ((QDLStem) obj).almostUnique();
                set.addAll(ss.getQDLList().unique());
            } else {
                set.add(obj);
            }

        }
        QDLList qdlList1 = new QDLList();
        HashSet hashSet1 = new HashSet();
        for (Object obj : set) {
            hashSet1.add(obj);
        }

        for (Object object : set) {
            qdlList1.append(object);
        }
        return qdlList1;
    }


    public Object get(long index) {
        // Fixes https://github.com/ncsa/qdl/issues/47
        if (index < 0) {
            return getRelativeAddress(index);
        }
        return getAbsoluteAddress(index);
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
    protected Object getRelativeAddress(long originalIndex) {
        int s = size();
        long index = originalIndex + s;
        if (index < 0L) {
            // we' tried to wrap around once, but more than that should fail
            throw new IndexError("index " + originalIndex + " out of bounds for list of length " + s, null);
        }
        if (index < arrayList.size()) { // so it's in the array list unless the next condition fails
            return arrayList.get((int) index);
        }
        // It's a sparse entry. A tree set may have entries like {100:3, 200:4} and
        // get originalIndex = -1 would mean returning the value associated with 200.
        if (originalIndex == -1) {
            return getSparseEntries().last().entry;
        }
        index = index - arrayList.size(); // restricts to indices in Sparse entries.
        if (index == 0) {
            return getSparseEntries().first().entry;
        }
        // neither first nor last, now we have to iterate. This is s-l-o-o-o-w.
        Iterator<SparseEntry> it = getSparseEntries().iterator();
        int i = 0;
        SparseEntry current = null;
        while (it.hasNext() && i <= index) {  // want to jump out at i == index.
            current = it.next();
            i++;
        }
        return current.entry;
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
    protected Object getAbsoluteAddress(long index) {
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
     * Find the largest element of this list and append the given object to the end of it.
     *
     * @param obj
     */
    public void append(Object obj) {
        if (!hasSparseEntries()) {
            getArrayList().add(obj);
            return;
        }

        SparseEntry newEntry;
        if (obj instanceof SparseEntry) {
            // in this case, stem entries are being added directly, so don't wrap them in a stem entry.

            newEntry = new SparseEntry(size(), ((SparseEntry) obj).entry); // argh Java requires a cast. If StemEntry is ever extended, this will break.

        } else {
            if (isEmpty()) {
                newEntry = new SparseEntry(0L, obj);
            } else {
                SparseEntry stemEntry = getSparseEntries().last();
                newEntry = new SparseEntry(stemEntry.index + 1, obj);
            }
        }
        getSparseEntries().add(newEntry);
    }

    /**
     * Appends all elements in a list. Terrificallty inefficient. Optimize. Someday. Maybe.
     *
     * @param objects
     */
    public void appendAll(List<Object> objects) {
        for (Object obj : objects) {
            add(obj);
        }
    }

    public void append(QDLSet set) {
        long index = 0L;
        if (!hasSparseEntries()) {
            getArrayList().addAll(set);
            return;
        }
        index = getSparseEntries().last().index;
        for (Object k : set) {
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

            Object obj = get(i);
            if (obj == null) {
                throw new seGapException();
            }
            if (obj instanceof QDLStem) {
                if (isFirst) {
                    isFirst = false;
                    output = output + "\n";
                } else {
                    output = output + ",\n";
                }
                output = output + newIndent + ((QDLStem) obj).toString(indentFactor, newIndent);
            } else {
                needsCRWithClosingBrace = false;
                if (isFirst) {
                    isFirst = false;
                    output = output + StemConverter.convert(obj);
                } else {

                    output = output + "," + StemConverter.convert(obj);
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
            Object obj = get(i);
            if (obj == null) {
                throw new seGapException();
            }
            String vv;
            if (obj instanceof BigDecimal) {
                vv = InputFormUtil.inputForm((BigDecimal) obj);
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

    public JSONArray toJSON(boolean escapeNames, int type) {
        JSONArray array = new JSONArray();
        for (Object element : getArrayList()) {
            if (element instanceof QDLStem) {
                array.add(((QDLStem) element).toJSON(escapeNames, type));
            } else {
                if (element instanceof QDLNull) {
                    array.add(QDLConstants.JSON_QDL_NULL);
                } else {
                    array.add(element);
                }
            }
        }
        for (SparseEntry s : getSparseEntries()) {
            Object v = s.entry;
            if (v instanceof QDLStem) {
                array.add(((QDLStem) v).toJSON(escapeNames, type));
            } else {
                array.add(v);
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
            Object obj = get(i);
            if (obj == null) {
                throw new seGapException();
            }
            output = output + InputFormUtil.inputForm(obj);
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
            Object obj = get(i);
            if (obj == null) {
                throw new seGapException();
            }
            output = output + newIndent + InputFormUtil.inputForm(obj);
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
    public Object[] toArray(boolean noGaps, boolean allowStems) {
        if (!hasSparseEntries()) {
            return getArrayList().toArray();
        }
        if (noGaps) {
            Object[] r = new Object[getArrayList().size()];
            int ii = 0;
            for (Object ooo : getArrayList()) {
                if (!allowStems && (ooo instanceof QDLStem)) {
                    throw new IllegalArgumentException("error: a stem is not allowed in this list");
                }
                r[ii++] = ooo;
            }
            return r;
        }

        Object[] r = new Object[size()];
        int ii = 0;
        for (Object ooo : getArrayList()) {
            r[ii++] = ooo;
        }
        // now handle sparse entries. All that is left is to fill in
        for (SparseEntry sparseEntry : getSparseEntries()) {
            if (!allowStems && (sparseEntry.entry instanceof QDLStem)) {
                throw new IllegalArgumentException("error: a stem is not allowed in this list");
            }
            r[ii++] = sparseEntry.entry;
        }
        return r;
    }

    /**
     * Get the dimension list for this object. dim(n(3,4,5)) == [3,4,5]<br/>
     * This id very simple minded and assumes rectangular arrays.
     *
     * @return
     */
    public QDLList dim() {
        QDLList s = new QDLList();
        if (isEmpty()) {
            return s;
        }
        long index = 0L;
        Object obj = get(0);
        s.add((long) size());
        Object currentEntry = obj;
        while (currentEntry != null) {
            if (currentEntry instanceof QDLStem) {
                QDLStem s1 = (QDLStem) currentEntry;
                if (s1.getQDLList().size() == 0) {
                    break;
                }
                s.add((long) s1.getQDLList().size());
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
    public ArrayList values() {
        ArrayList list = new ArrayList();
        Iterator iterator = iterator(true);
        while (iterator.hasNext()) {
            list.add(iterator.next());
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
        TreeSet<Long> treeSet = new TreeSet<>();
        for (long i = 0; i < getArrayList().size(); i++) {
            treeSet.add(i);
        }
        for (SparseEntry sparseEntry : getSparseEntries()) {
            treeSet.add(sparseEntry.index);
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
                    for (SparseEntry sparseEntry : arg.getSparseEntries()) {
                        arrayList.add(sparseEntry.entry);
                    }
                    return true;
                case 11: // TTFT array entries, sparse entries, arg is sparse
                    throw new NotImplementedException("Surgery needed");
                case 12: // FFTT empty, arg has both
                case 13: // TFTT array not empty. both
                case 14: // FTTT sparse only, both
                    arrayList.addAll(arg.getArrayList());
                    for (SparseEntry sparseEntry : arg.getSparseEntries()) {
                        arrayList.add(sparseEntry.entry);
                    }
                    return true;
                case 15: //TTTT both, both
                    throw new NotImplementedException("Surgery needed");
            }

        }
        getArrayList().addAll(c);
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

    public void set(long index, Object element) {
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
      /*
             int s = size();
        long index = originalIndex + s;
        if (index < 0L) {
            // we' tried to wrap around once, but more than that should fail
            throw new IndexError("index " + originalIndex + " out of bounds for list of length " + s, null);
        }
        if (index < arrayList.size()) { // so it's in the array list unless the next condition fails
            return arrayList.get((int) index);
        }
        // It's a sparse entry. A tree set may have entries like {100:3, 200:4} and
        // get originalIndex = -1 would mean returning the value associated with 200.
        if (originalIndex == -1) {
            return getSparseEntries().last().entry;
        }
        index = index - arrayList.size(); // restricts to indices in Sparse entries.
        if (index == 0) {
            return getSparseEntries().first().entry;
        }
        // neither first nor last, now we have to iterate. This is s-l-o-o-o-w.
        Iterator<SparseEntry> it = getSparseEntries().iterator();
        int i = 0;
        SparseEntry current = null;
        while (it.hasNext() && i <= index) {  // want to jump out at i == index.
            current = it.next();
            i++;
        }
        return current.entry;
       */

    /**
     * Set a relative value. Note that unlike absolute addresses, relative ones must exist prior to being
     * set. So a.42 can always be set (may result in a sparse entry) but a.(-42) requires there be at least 42 elements
     *
     * @param originalIndex
     * @param element
     */
    protected void setRelativeIndex(long originalIndex, Object element) {
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

    protected void setAbsoluteIndex(long index, Object element) {

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

        List sourceList = null;
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
            for (Object obj : sourceList) {
                SparseEntry sparseEntry = new SparseEntry(index++, obj);
                getSparseEntries().remove(sparseEntry);
                getSparseEntries().add(sparseEntry);
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

        List sourceList = null;
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
        for (Object obj : sourceList) {
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
        Iterator arrayIterator;
        Iterator sparseEntryIterator;
        boolean objectsOnly = false;

        public MyIterator(Iterator arrayIterator, Iterator sparseEntryIterator, boolean objectsOnly) {
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
                Object obj = arrayIterator.next();
                if (obj instanceof SparseEntry) {
                    return ((SparseEntry) obj).entry;
                }
                return obj;
            }
            if (objectsOnly) {
                return ((SparseEntry) sparseEntryIterator.next()).entry;

            }
            return sparseEntryIterator.next();
        }
    }

    /**
     * An iterator
     *
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
            return getArrayList().contains(((SparseEntry) o).entry);
        }
        if (getArrayList().contains(o)) {
            return true;
        }
        // Now grunt work. Does a random object exist in the sparse entries
        if (hasSparseEntries()) {
            for (SparseEntry sparseEntry : getSparseEntries()) {
                if (sparseEntry.entry.equals(o)) return true;
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

    @Override
    public Object[] toArray(Object[] a) {
        throw new NotImplementedException("toArray(Object[])");
    }

    @Override
    public boolean add(Object o) {
        if(o instanceof Integer) {
            // assume they are adding just an number to the list
            o = ((Integer) o).longValue();
        }
        if (hasSparseEntries()) {
            SparseEntry lastEntry = getSparseEntries().last();
            SparseEntry newEntry = new SparseEntry(lastEntry.index + 1, o);
            getSparseEntries().add(newEntry);
            return true;
        }
        if (o instanceof SparseEntry) {
            return getArrayList().add(((SparseEntry) o).entry);
        }
        return getArrayList().add(o);
    }


    /**
     * Remove by value  from <b>top level</b> only. This fulfills Java's contract for
     * lists/collections, which removes <i>the first instance of this object <b>only</b></i>.
     *
     * @param o
     * @return
     */
    @Override
    public boolean remove(Object o) {
        boolean rc = true;
        rc = rc && getArrayList().remove(o);
        if (o instanceof SparseEntry) {
            SparseEntry sparseEntry = (SparseEntry) o;
            rc = rc && getArrayList().remove(sparseEntry.entry);
            SparseEntry removeIt = null;
            for (SparseEntry se : getSparseEntries()) {
                if (se.entry.equals(sparseEntry.entry)) {
                    removeIt = se;
                    break;
                }
            }
            if (removeIt == null) {
                return false;
            }
            rc = rc && getSparseEntries().remove(removeIt);
        }
        return rc;
    }

    public boolean remove(Collection c) {
        boolean rc = true;
        for (Object obj : c) {
            rc = rc && remove(obj);
        }
        return rc;
    }


    public boolean removeAllByValue(Collection c, boolean reorderLists) {
        ArrayList newList = new ArrayList();
        if (reorderLists) {
            for (Object v : getArrayList()) {
                if (v instanceof QDLStem) {
                    QDLStem vv = (QDLStem) v;
                    vv.removeAllByValues(c, reorderLists);
                    if (!vv.isEmpty()) {
                        newList.add(vv);
                    }
                } else {
                    if (!c.contains(v)) {
                        newList.add(v);
                    }
                }
            }
            for (SparseEntry sparseEntry : getSparseEntries()) {
                if (sparseEntry.entry instanceof QDLStem) {
                    QDLStem vv = (QDLStem) sparseEntry.entry;
                    vv.removeAllByValues(c, reorderLists);
                    if (!vv.isEmpty()) {
                        newList.add(vv);
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
            rc = rc && removeAllByValue(obj, reorderLists);
        }
        return rc;
    }

    /**
     * removes a single object from everywhere in this stem.
     *
     * @param c
     * @return
     */
    protected boolean removeAllByValue(Object c, boolean reorderLists) {
        List<Integer> removeList = new ArrayList<>();
        boolean rc = true;
        for (int i = 0; i < getArrayList().size(); i++) {
            Object value = getArrayList().get(i);
            if (value instanceof QDLStem) {
                rc = rc && ((QDLStem) value).removeAllByValue(c, reorderLists);
            } else {
                if (value.equals(c)) {
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
                Object value = sparseEntry.entry;
                if (value instanceof QDLStem) {
                    rc = rc && ((QDLStem) value).removeAllByValue(c, reorderLists);
                } else {
                    if (value.equals(c)) {
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
     * removes every element by value.
     *
     * @param c
     * @return
     */
    @Override
    public boolean removeAll(Collection c) {
        List<Integer> rr = new ArrayList<>();
        for (int i = 0; i < getArrayList().size(); i++) {
            Object obj = getArrayList().get(i);
            if (obj instanceof QDLStem) {
                ((QDLStem) obj).removeAllByValues(c, false);
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
    public Object get(int index) {
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
    public Object set(int index, Object element) {
        return getArrayList().set(index, element);
    }

    @Override
    public void add(int index, Object element) {
        throw new NotImplementedException("add(int, Object) -- need logic for sparse entries");
    }

    @Override
    public Object remove(int index) {
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
        return new SparseEntry(new Long(index), getArrayList().get(index));
    }

    public SparseEntry first() {
        if (hasArrayList()) {
            return new SparseEntry(new Long(0L), getArrayList().get(0));
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

    public static void main(String[] args) {
        QDLList list = new QDLList(10L, new String[]{"a"});
        System.out.println(list);
        list.appendAll(new QDLList(10L, new String[]{"b"}));
        System.out.println(list);
        System.out.println(list.size());
    }

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
    public QDLStem permuteEntries(List<Long> permutation) {
        /*
        This uses the internal structure of the stem and lists, so this is seriously hot-rodding it.
         */
        QDLList out = new QDLList();
        int size = permutation.size();
        for (Object ooo : getArrayList()) {
            QDLList qdlList = ((QDLStem) ooo).getQDLList(); // each stem has a list of n entries.
            QDLList outList = new QDLList(size);

            for (int index = 0; index < size; index++) {
                //outList.set(permutation.get(index), qdlList.get(index));
                outList.set(index, qdlList.get(permutation.get(index)));
            }
            QDLStem outStem = new QDLStem(outList);
            out.append(outStem);
        }
        QDLStem r = new QDLStem(out);
        return r;
    }
}
