/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enumj;

import java.util.Iterator;

/**
 *
 * @author Marius Filip
 */
class BasicEnumerator<E> extends AbstractEnumerator<E> {

    protected final Iterator<E> source;

    public BasicEnumerator(Iterator<E> source) {
        Utils.ensureNotNull(source, Messages.NullEnumeratorSource);
        this.source = source;
    }

    @Override
    public boolean hasNext() {
        return source.hasNext();
    }

    @Override
    protected E nextValue() {
        return source.next();
    }
}
