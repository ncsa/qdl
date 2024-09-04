package org.qdl_lang.variables;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Comparable entries for sparse entries. This contains the index and the actual entry itself.
 * You can get this using {@link QDLList#first()} and {@link QDLList#last()}.
 * <p>Created by Jeff Gaynor<br>
 * on 2/20/20 at  8:35 AM
 */
public class SparseEntry implements Comparable, Serializable {
    /**
     * Useful for making index entries, i.e., checking is an element is at a certain index
     * but not caring about the entry (which we never do, since we are modelling lists with these).
     * @param index
     */
    public SparseEntry(long index) {
        this(index, null);
    }

    public SparseEntry(long index, Object entry) {
        this.index = index;
        this.entry = entry;
    }

    public SparseEntry(BigInteger index, Object entry) {
        this.bigIndex = index;
        this.entry = entry;
    }

    public long index;
    public Object entry;
    public BigInteger bigIndex = null;
   /*
     There was an attempt to allow for BigInteger indices, but that ended up being far too
     messy. We will leave this for now since it was a fair bit of work to do and may want
     to finish it, but until done, QDL is limited to longs as indices in lists.
    */


    @Override
    public int compareTo(Object o) {
        if(o instanceof BigDecimal){
            BigDecimal bd = (BigDecimal) o;
            BigInteger bi ;
            if(bigIndex!=null){
                 bi = bd.toBigIntegerExact();
            }else{
                 bi = new BigInteger(Long.toString(index)); // only way to keep exact
            }
            return bigIndex.compareTo(bi);
        }

        if(o instanceof Long){
            Long ll = (Long)o;
            if(bigIndex !=null){
                BigInteger bi = new BigInteger(Long.toString(index));
                return bigIndex.compareTo(bi);
            }
            if (index < ll) return -1;
            if (index == ll) return 0;
            if (index > ll) return 1;

        }
        if (o instanceof SparseEntry) {
            SparseEntry s = (SparseEntry) o;
            if(s.bigIndex!=null || bigIndex!=null){
                BigInteger thisBI;
                BigInteger thatBI;
                if(bigIndex==null){
                    thisBI = new BigInteger(Long.toString(index));
                }else{
                    thisBI = bigIndex;
                }
                if(s.bigIndex==null){
                    thatBI = new BigInteger(Long.toString(s.index));
                }else{
                    thatBI = s.bigIndex;
                }
                return thisBI.compareTo(thatBI);
            }
            if (index < s.index) return -1;
            if (index == s.index) return 0;
            if (index > s.index) return 1;
        }
        throw new ClassCastException("Error: the object \"" + o.getClass().getSimpleName() + "\" is not comparable.");
    }

    @Override
    public boolean equals(Object obj) {
        return compareTo(obj) == 0;
    }

    @Override
    public String toString() {
        return "SparseEntry{" +
                "index=" + index +
                ", bigIndex =" + bigIndex +
                ", entry=" + entry +
                '}';
    }
}
