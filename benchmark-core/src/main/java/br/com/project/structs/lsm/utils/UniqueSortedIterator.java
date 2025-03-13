package br.com.project.structs.lsm.utils;

import java.util.Iterator;

/**
 * Ignora duplicatas em um Iterator ordenado, mantendo apenas o primeiro elemento de cada valor repetido.
 * <p>
 * Chamadas ao método {@code next()} após o último elemento do último Iterator retornarão {@code null}.
 *
 * @param <T> O tipo dos elementos contidos nos Iterators.
 */
public class UniqueSortedIterator<T extends Comparable<T>> implements Iterator<T> {

    Iterator<T> iterator;
    private T last;

    public UniqueSortedIterator(Iterator<T> iterator) {
        this.iterator = iterator;
        last = iterator.next();
    }

    @Override
    public boolean hasNext() {
        return last != null;
    }

    @Override
    public T next() {
        T next = iterator.next();
        while (next != null && last.compareTo(next) == 0)
            next = iterator.next();

        T toReturn = last;
        last = next;

        return toReturn;
    }

}
