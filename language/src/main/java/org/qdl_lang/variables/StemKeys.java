package org.qdl_lang.variables;

import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import org.qdl_lang.variables.values.LongValue;
import org.qdl_lang.variables.values.QDLKey;
import org.qdl_lang.variables.values.StringValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A set for looping through keys of a stem. This manages long indices and string keys.
 * This will keep the keys in order and will process long keys first, then string keys.
 * <br/>N.B. it produces keys that are Objects.
 * <p>Created by Jeff Gaynor<br>
 * on 5/12/22 at  6:44 AM
 */
public class StemKeys implements Set<QDLKey> {
    @Override
    public boolean add(QDLKey qdlKey) {
        switch (qdlKey.getType()) {
            case Constant.LONG_TYPE:
            case Constant.STRING_TYPE:

        }
        return false;
    }

    /*
        public boolean add(Object o) {
        if (o instanceof Long) {
            return listkeys.add((Long) o);
        }
        if(o instanceof String) {
            return stemKeys.add((String) o);
        }
        throw new IllegalArgumentException("Cannot add key '" + o + "'. Must be a Long or String object");
    }
     */
    public TreeSet<LongValue> getListkeys() {
        return listkeys;
    }

    public void setListkeys(TreeSet<LongValue> listkeys) {
        this.listkeys = listkeys;
    }

    public TreeSet<StringValue> getStemKeys() {
        return stemKeys;
    }

    public void setStemKeys(TreeSet<StringValue> stemKeys) {
        this.stemKeys = stemKeys;
    }

    TreeSet<LongValue> listkeys = new TreeSet<>();
    TreeSet<StringValue> stemKeys = new TreeSet<>();

    @Override
    public int size() {
        return listkeys.size() + stemKeys.size();
    }

    @Override
    public boolean isEmpty() {
        return listkeys.isEmpty() && stemKeys.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof QDLKey) {
            switch (((QDLKey) o).getType()) {
                case Constant.LONG_TYPE:
                    return listkeys.contains(o);
                case Constant.STRING_TYPE:
                    return stemKeys.contains(o);
            }
        }
        return false;
    }

    public class MyStemKeyIterator implements Iterator<QDLKey> {
        Iterator<LongValue> listKeysIterator;
        Iterator<StringValue> stemKeysIterator;

        public MyStemKeyIterator(Iterator<LongValue> listKeysIterator, Iterator<StringValue> stemKeysIterator) {
            this.listKeysIterator = listKeysIterator;
            this.stemKeysIterator = stemKeysIterator;
        }

        @Override
        public boolean hasNext() {
            return listKeysIterator.hasNext() || stemKeysIterator.hasNext();
        }

        @Override
        public QDLKey next() {
            if (listKeysIterator.hasNext()) {
                return listKeysIterator.next();
            }
            return stemKeysIterator.next();
        }
    }

    @Override
    public Iterator<QDLKey> iterator() {
        return new MyStemKeyIterator(listkeys.iterator(), stemKeys.iterator());
    }

    @Override
    public Object[] toArray() {
        return toArray(new Object[listkeys.size()]);
    }

    @Override
    public Object[] toArray(Object[] a) {
        Object[] ooo;

        if (a.length == size()) {
            ooo = a;
        } else {
            ooo = new Object[size()];
        }
        int i = 0;
        for (Object kkk : listkeys) {
            ooo[i++] = kkk;
        }
        for (Object kkk : stemKeys) {
            ooo[i++] = kkk;
        }
        return ooo;
    }
/*
status.:=[{'client_id':42, 'status':'active'},{'client_id':11, 'status':'inactive'},{'client_id':99, 'status':'suspended'},{'client_id':17, 'status':'active'}];
status\*\((k,v.)→v.'status'≡'active')
 */
/*    @Override
    public boolean add(Object o) {
        if (o instanceof Long) {
            return listkeys.add((Long) o);
        }
        if(o instanceof String) {
            return stemKeys.add((String) o);
        }
        throw new IllegalArgumentException("Cannot add key '" + o + "'. Must be a Long or String object");
    }*/

    @Override
    public boolean remove(Object o) {
        if (o instanceof QDLKey) {
            switch (((QDLKey) o).getType()) {
                case Constant.LONG_TYPE:
                    return listkeys.remove(o);
                case Constant.STRING_TYPE:
                    return stemKeys.remove(o);

            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection c) {
        return false;
    }

    /**
     * This will let you add collection so POJOs though it is more expensive
     *
     * @param c
     * @return
     */
    @Override
    public boolean addAll(Collection c) {

        if (c instanceof StemKeys) {
            StemKeys arg = (StemKeys) c;
            return listkeys.addAll(arg.getListkeys()) || stemKeys.addAll(arg.getStemKeys());
        }
        for (Object obj : c) {
            QDLKey key = QDLKey.from(obj);
            switch (key.getType()) {
                case Constant.LONG_TYPE:
                    listkeys.add((LongValue) key);
                    break;
                case Constant.STRING_TYPE:
                    stemKeys.add((StringValue) key);
                    break;
                default:
                    throw new NotImplementedException("addAll(Collection) unknown element type " + obj.getClass().getSimpleName() + " for general collection");
            }

/*
            boolean gotOne = false;
            if(key instanceof LongValue){
                listkeys.add((LongValue) key);
                gotOne = true;
            }
            if(key instanceof StringValue){
                stemKeys.add((StringValue) key);
                gotOne = true;
            }
            if(!gotOne) {
                throw new NotImplementedException("addAll(Collection) unknown element type " + getClass().getSimpleName() + " for general collection");
            }
*/
        }

        return true;
    }

    @Override
    public boolean retainAll(Collection c) {
        if (c instanceof StemKeys) {
            StemKeys arg = (StemKeys) c;
            return listkeys.retainAll(arg.getListkeys()) || stemKeys.retainAll(arg.getStemKeys());
        }
        throw new NotImplementedException("retainAll(Collection) not implemented in " + getClass().getSimpleName() + " for general collection");
    }

    @Override
    public boolean removeAll(Collection c) {
        if (c instanceof StemKeys) {
            StemKeys arg = (StemKeys) c;
            return listkeys.removeAll(arg.getListkeys()) || stemKeys.removeAll(arg.getStemKeys());
        }
        throw new NotImplementedException("removeAll(Collection) not implemented in " + getClass().getSimpleName() + " for general collection");
    }

    @Override
    public void clear() {
        listkeys.clear();
        stemKeys.clear();
    }

/*
    public Iterator<String> getStringKeyIterator() {
        return stemKeys.iterator();
    }

    public Iterator<Long> getListKeyIterator() {
        return listkeys.iterator();
    }
*/

    /**
     * Note that this takes the intersection of this set with the given set and returns a new
     * set that is the intersection.
     *
     * @param stemKeys
     */
/*    public StemKeys intersection(StemKeys stemKeys) {
        StemKeys outKeys = new StemKeys();

        return outKeys;
    }*/
}
