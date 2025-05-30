package org.qdl_lang.expressions;

import org.qdl_lang.evaluate.StemEvaluator;
import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.*;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/1/22 at  4:50 PM
 */
public class IndexArgs extends ArrayList<IndexArg> {
    /**
     * Does one of the index args have a wildcard?
     *
     * @return
     */
    public boolean hasWildcard() {
        for (IndexArg indexArg : this) {
            if (indexArg.isWildcard()) return true;
        }
        return false;
    }

    /**
     * If one (except the zero-th element, which is the actual stem) is all wildcards.
     *
     * @return
     */
    public boolean isAllWildcards() {
        for (int i = 1; i < size(); i++) {
            if (!get(i).isWildcard()) return false;
        }
        return true;

    }

    /**
     * Only works if there are no wildcards! This counts  the number of indices that will be
     * computed.
     *
     * @return
     */
    public int count() {
        int s = 1;
        for (int i = 1; i < size(); i++) { // zero-th element is left most
            s = s * argSize(get(i));
        }
        return s;
    }

    public int argSize(IndexArg indexArg) {
        QDLValue obj = indexArg.swri.getResult();
        if (Constant.isScalar(obj)) {
            return 1;
        }
        if (obj.isAllIndices()) {
            throw new IllegalStateException("cannot compute size for wildcards");
        }
        if (obj.isStem()) {
            return obj.asStem().size();
        }
        if (obj.isSet()) {
            return obj.asSet().size();
        }
        throw new IllegalStateException("cannot compute size for unknown type");

    }


      /*
   In an expression like a\x1.\x2.\...\xk. where the xk are lists or scalars
   list sizes are n1,n2,...nk.
   ν = total size = n1*n2*...*nk
   An basic shows this.
   a\[;2]\[;3]\[;4]    generates indices

 - [0,    - 0,    k- 0]       And the relationship is (general = here)
|  [0,   g| 0,    e| 1]       n1, n2, n3, n4, ... = 2, 3, 4 given
|  [0,   r| 0,    y| 2]       p1, p2, p3, p4, ... = 12 4  1
p  |  [0,   o| 0,    s- 3]       g1, g2, g3, g4, ... = 1  2  3
e  |  [0,   u| 1,       0]
r  |  [0,   p| 1,       1]      ν = 24
i  |  [0,    | 1,       2]      p1 = ν/n1, p_k = p_k-1/nk, 1<k
o  |  [0,    - 1,       3]      gk = ν/(nk*pk)
d  |  [0,      2,       0]
|  [0,      2,       1]
|  [0,      2,       2]
-  [0,      2,       3]
   [1,      0,       0]
   [1,      0,       1]
   [1,      0,       2]
   [1,      0,       3]
   [1,      1,       0]
   [1,      1,       1]
   [1,      1,       2]
   [1,      1,       3]
   [1,      2,       0]
   [1,      2,       1]
   [1,      2,       2]
   [1,      2,       3]

       */

    public boolean add(IndexArg indexArg, State state) {
        return super.addAll(checkIfList(indexArg, state));
    }

    protected IndexArgs checkIfList(IndexArg indexArg, State state) {
        IndexArgs indexArgs = new IndexArgs();
        if (indexArg.interpretListArg) {
            /*
              Meaning of this is that the argument is to be interpreted as a stem index,
              so
              a\[2,3]
              means to take a.2 and a.3 and return them
              a\>[2,3]
              means the specific index a.2.3
             */
            indexArg.swri.evaluate(state);
            if (!(indexArg.swri.getResult().isStem())) {
                throw new BadArgException("stem index for extraction " + StemExtractionNode.EXTRACT_LIST + " must be a list", indexArg.swri);
            }
            QDLStem args = indexArg.swri.getResult().asStem();
            if (!args.isList()) {
                throw new BadArgException("stem index for extraction " + StemExtractionNode.EXTRACT_LIST + " must be a list", indexArg.swri);
            }
            for (Object key : args.keySet()) {
                IndexArg indexArg1 = new IndexArg();
                indexArg1.strictOrder = indexArg.strictOrder;
                if(args.get(key).getValue() instanceof AllIndices){
                    indexArg1.swri = (AllIndices) args.get(key).getValue();

                }else{
                    ConstantNode constantNode = new ConstantNode(new QDLValue(args.get(key)));
                    constantNode.setEvaluated(true);
                    indexArg1.swri = constantNode;
                }
                indexArgs.add(indexArg1);

            }
        } else {
            if((indexArg.swri instanceof Polyad)){
                if(((Polyad)indexArg.swri).getName().equals(StemEvaluator.STAR)){
                    indexArg.swri = new AllIndices();
                }
            }
            indexArgs.add(indexArg);
        }
        return indexArgs;
    }
}
