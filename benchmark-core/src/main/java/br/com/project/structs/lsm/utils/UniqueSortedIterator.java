package br.com.project.structs.lsm.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Ignora duplicatas em um Iterator ordenado, mantendo apenas o primeiro elemento de cada valor repetido.
 * Chamadas ao método {@code next()} após o último elemento do último Iterator retornarão {@code null}.
 *
 * @param <T> O tipo dos elementos contidos nos Iterators.
 */
public class UniqueSortedIterator<T extends Comparable<T>> implements Iterator<T> {

    Iterator<T> iterator;
    private T next;
    private T lastReturned;

    public UniqueSortedIterator(Iterator<T> iterator) {
        this.iterator = iterator;
        advance();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public T next() {
        if (next == null) {
            throw new NoSuchElementException("Nenhum elemento restante na iteração.");
        }

        lastReturned = next;
        advance();
        return lastReturned;
    }

    private void advance() {
        next = null;
        while (iterator.hasNext()) {
            T candidate = iterator.next();
            if (lastReturned == null || lastReturned.compareTo(candidate) != 0) {
                next = candidate;
                break;
            }
        }
    }

}
