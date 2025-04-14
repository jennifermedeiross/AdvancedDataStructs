package br.com.project.structs.lsm.utils;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectIntMutablePair;

import java.util.Iterator;

import static java.util.Comparator.comparing;

/**
 * Combina vários iteradores ordenados em um único iterador também ordenado.
 * A complexidade para acessar um único elemento é O(log n), sendo n o número de iteradores.
 * Depois que todos os elementos dos iteradores forem processados, chamadas subsequentes ao método {@code next()} retornarão {@code null}.
 *
 * @param <T> O tipo dos itens armazenados nos iteradores.
 */
public class IteratorMerger<T extends Comparable<T>> implements Iterator<T> {

    Iterator<T>[] iterators;
    ObjectHeapPriorityQueue<Pair<T, Integer>> queue;

    /**
     * Constrói um {@code IteratorMerger} a partir de uma lista de Iterators ordenados.
     * Os elementos serão extraídos de forma ordenada a partir dos Iterators fornecidos.
     *
     * @param iterators Lista de Iterators ordenados a serem mesclados.
     */
    @SafeVarargs
    public IteratorMerger(Iterator<T>... iterators) {
        this.iterators = iterators;
        queue = new ObjectHeapPriorityQueue<>(
                comparing((Pair<T, Integer> a) -> a.first())
                        .thenComparingInt(Pair::second)
        );

        for (int i = 0; i < iterators.length; i++) {
            if (iterators[i].hasNext())
                queue.enqueue(new ObjectIntMutablePair<>(iterators[i].next(), i));
        }
    }

    /**
     * Verifica se ainda há elementos disponíveis para leitura no Iterator resultante.
     *
     * @return {@code true} se houver mais elementos, {@code false} caso contrário.
     */
    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    /**
     * Retorna o próximo elemento ordenado do Iterator mesclado.
     * Se não houver mais elementos disponíveis, retorna {@code null}.
     *
     * @return O próximo elemento ordenado ou {@code null} se não houver mais elementos.
     */
    @Override
    public T next() {
        if (queue.isEmpty())
            return null;

        Pair<T, Integer> top = queue.dequeue();

        T result = top.first();

        int index = top.second();
        if (index == -1)
            return result;

        T next = iterators[index].next();
        int newIndex = iterators[index].hasNext() ? index : -1;
        queue.enqueue(top.first(next).second(newIndex));

        return result;
    }
}
