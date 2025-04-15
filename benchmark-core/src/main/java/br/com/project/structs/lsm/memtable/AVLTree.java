package br.com.project.structs.lsm.memtable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Árvore AVL genérica que armazena pares chave-valor.
 *
 * @param <K> tipo da chave (deve ser comparável)
 * @param <V> tipo do valor (deve ser comparável)
 *            Usa um {@code keyExtractor} para extrair a chave a partir do valor armazenado.
 *            Garante operações de busca, inserção e remoção em O(log n) mantendo a árvore balanceada.
 */
public class AVLTree<K extends Comparable<K>, V extends Comparable<V>> implements Iterable<V> {
    private Node<V> root;
    private int size;

    /**
     * Função utilizada para extrair a chave (do tipo {@code K}) a partir de um valor (do tipo {@code V}).
     */
    private final Function<V, K> keyExtractor;

    /**
     * Constrói uma nova árvore AVLTree que utiliza a função fornecida para extrair a chave dos valores armazenados.
     * A chave é usada para realizar comparações entre os nós da árvore.
     *
     * @param keyExtractor função que extrai a chave {@code K} a partir de um valor {@code V}.
     *                     Essa função é obrigatória para que a árvore saiba como ordenar os elementos.
     */
    public AVLTree(Function<V, K> keyExtractor) {
        this.keyExtractor = keyExtractor;
    }

    /**
     * Verifica se a árvore está vazia.
     *
     * @return true se a árvore estiver vazia, false caso contrário.
     */
    public boolean isEmpty() {
        return this.root == null;
    }

    /**
     * Adiciona um novo elemento na árvore AVL. Se já existir um com a mesma chave, os seus dados são atualizados.
     *
     * @param element elemento a ser inserido ou atualizado.
     */
    public void add(V element) {
        root = addRec(root, element, null);
    }

    private Node<V> addRec(Node<V> current, V value, Node<V> parent) {
        if (current == null) {
            size++;
            Node<V> newNode = new Node<V>(value);
            newNode.parent = parent;
            return newNode;
        }

        int cmp = value.compareTo(current.value);

        if (cmp < 0) {
            current.left = addRec(current.left, value, current);
        } else if (cmp > 0) {
            current.right = addRec(current.right, value, current);
        } else {
            current.value = value;
        }

        updateHeight(current);
        return balance(current);
    }

    /**
     * Busca por um nó com chave igual ao fornecido.
     *
     * @param key chave a ser buscado.
     * @return o nó correspondente, ou null se não encontrado.
     */
    public Node<V> search(K key) {
        return searchRec(root, key);
    }

    private Node<V> searchRec(Node<V> node, K key) {
        if (node == null) return null;

        K nodeKey = keyExtractor.apply(node.value);  // pega a chave do valor
        int cmp = key.compareTo(nodeKey);

        if (cmp == 0) return node;
        if (cmp < 0) return searchRec(node.left, key);
        return searchRec(node.right, key);
    }

    /**
     * Retorna a altura da árvore.
     *
     * @return altura da árvore, ou -1 se vazia.
     */
    public int height() {
        return heightRec(root);
    }

    private int heightRec(Node<V> current) {
        if (current == null) return -1;
        return 1 + Math.max(heightRec(current.left), heightRec(current.right));
    }

    /**
     * Remove um elemento da árvore com base na chave.
     *
     * @param key chave do elemento a ser removida.
     * @return o nó removido, ou null se não encontrado.
     */
    public Node<V> remove(K key) {
        Node<V> toRemove = search(key);
        if (toRemove == null) return null;

        remove(toRemove);
        size--;
        return toRemove;
    }

    private void remove(Node<V> target) {
        if (isLeaf(target)) {
            if (target == root) {
                root = null;
            } else if (isRightChild(target)) {
                target.parent.right = null;
            } else {
                target.parent.left = null;
            }
            return;
        }

        if (grau(target) == 1) {
            Node<V> child = getFilhoGrau1(target);

            if (target == root) {
                root = child;
                child.parent = null;
            } else if (isRightChild(target)) {
                target.parent.right = child;
                child.parent = target.parent;
            } else {
                target.parent.left = child;
                child.parent = target.parent;
            }
            return;
        }

        if (grau(target) == 2) {
            Node<V> successor = sucessor(target);
            target.value = successor.value;
            remove(successor);
        }
    }

    private boolean isRightChild(Node<V> node) {
        return node.parent != null && node.parent.right == node;
    }

    private boolean isLeaf(Node<V> node) {
        return node.left == null && node.right == null;
    }

    private Node<V> getFilhoGrau1(Node<V> node) {
        return (node.left != null) ? node.left : node.right;
    }

    private int grau(Node<V> node) {
        if (node == null || isLeaf(node)) return 0;
        if (node.left != null && node.right != null) return 2;
        return 1;
    }

    /**
     * Verifica se duas árvores AVL são iguais com base na travessia pré-ordem.
     *
     * @param outra outra árvore a ser comparada.
     * @return true se forem iguais, false caso contrário.
     */
    public boolean equals(AVLTree<K, V> outra) {
        return preOrder().equals(outra.preOrder());
    }

    /**
     * Retorna o número de elementos armazenados na árvore.
     *
     * @return quantidade de elementos.
     */
    public int size() {
        return this.size;
    }

    /**
     * Retorna o valor do predecessor do nó com chave fornecido.
     *
     * @param key chave de referência.
     * @return representação textual do predecessor, ou string vazia se não existir.
     */
    public String predecessor(K key) {
        Node<V> target = search(key);
        if (target == null) return "";

        if (target.left != null) {
            return predecessor(target.left);
        }

        Node<V> aux = target.parent;
        K nodeKey = keyExtractor.apply(aux.value);
        while (aux != null && nodeKey.compareTo(key) >= 0)
            aux = aux.parent;

        return aux != null ? aux.value.toString() : "";
    }

    private String predecessor(Node<V> current) {
        if (current == null) return "";
        if (isLeaf(current) || current.right == null) return current.value + "";
        return predecessor(current.right);
    }

    /**
     * Retorna o sucessor de um determinado nó.
     *
     * @param node nó de referência.
     * @return nó sucessor, ou null se não existir.
     */
    public Node<V> sucessor(Node<V> node) {
        if (node == null) return null;

        if (node.right != null) return min(node.right);

        Node<V> aux = node.parent;
        while (aux != null && aux.value.compareTo(node.value) <= 0) {
            aux = aux.parent;
        }

        return aux;
    }

    public Node<V> sucessor(K key) {
        return sucessor(search(key));
    }

    /**
     * Retorna uma string com a travessia em pré-ordem da árvore.
     *
     * @return string com os elementos em pré-ordem.
     */
    public String preOrder() {
        return preOrderRec(root).trim();
    }

    private String preOrderRec(Node<V> current) {
        if (current == null) return "";
        K currentKey = keyExtractor.apply(current.value);
        return currentKey + " " + preOrderRec(current.left) + preOrderRec(current.right);
    }

    /**
     * Retorna o nó com menor valor da árvore.
     *
     * @return nó mínimo, ou null se a árvore estiver vazia.
     */
    public Node<V> min() {
        return min(root);
    }

    private Node<V> min(Node<V> current) {
        if (current == null) return null;
        return (current.left == null) ? current : min(current.left);
    }

    /**
     * Retorna o nó com maior valor da árvore.
     *
     * @return nó máximo, ou null se a árvore estiver vazia.
     */
    public Node<V> max() {
        return max(root);
    }

    private Node<V> max(Node<V> current) {
        if (current == null) return null;
        return (current.right == null) ? current : max(current.right);
    }

    /**
     * Atualiza a altura de um nó com base na altura de seus filhos.
     * A altura de um nó é 1 + a maior altura entre seus filhos.
     *
     * @param node o nó cuja altura será atualizada
     */
    private void updateHeight(Node<V> node) {
        int left = (node.left != null) ? node.left.height : 0;
        int right = (node.right != null) ? node.right.height : 0;
        node.height = 1 + Math.max(left, right);
    }

    /**
     * Calcula o fator de balanceamento de um nó.
     * O fator é a diferença entre a altura da subárvore esquerda e direita.
     *
     * @param node o nó cujo fator de balanceamento será calculado
     * @return um inteiro representando o balanceamento (positivo = desbalanceado para esquerda,
     * negativo = desbalanceado para direita)
     */
    private int balanceFactor(Node<V> node) {
        int left = (node.left != null) ? node.left.height : 0;
        int right = (node.right != null) ? node.right.height : 0;
        return left - right;
    }

    /**
     * Garante o balanceamento da árvore AVL a partir de um nó.
     * Aplica rotações simples ou duplas conforme o fator de balanceamento.
     *
     * @param node o nó a ser balanceado
     * @return o novo nó raiz após o balanceamento (pode ser o mesmo ou alterado por rotação)
     */
    private Node<V> balance(Node<V> node) {
        int bf = balanceFactor(node);

        if (bf > 1) {
            if (balanceFactor(node.left) < 0)
                node.left = rotateLeft(node.left);
            return rotateRight(node);
        } else if (bf < -1) {
            if (balanceFactor(node.right) > 0)
                node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    /**
     * Executa uma rotação simples para a esquerda em um nó.
     * Exemplo visual:
     * x               y
     * \             / \
     * y    =>     x   T3
     * / \           \
     * T2  T3           T2
     *
     * @param node o nó desbalanceado à direita
     * @return nova raiz após a rotação
     */
    private Node<V> rotateLeft(Node<V> node) {
        Node<V> newRoot = node.right;
        node.right = newRoot.left;

        if (newRoot.left != null) newRoot.left.parent = node;

        newRoot.left = node;
        newRoot.parent = node.parent;
        node.parent = newRoot;

        updateHeight(node);
        updateHeight(newRoot);

        return newRoot;
    }

    /**
     * Executa uma rotação simples para a direita em um nó.
     * <p>
     * Exemplo visual:
     * y           x
     * /           / \
     * x     =>    T1  y
     * / \             /
     * T1  T2           T2
     *
     * @param node o nó desbalanceado à esquerda
     * @return nova raiz após a rotação
     */
    private Node<V> rotateRight(Node<V> node) {
        Node<V> newRoot = node.left;
        node.left = newRoot.right;

        if (newRoot.right != null) newRoot.right.parent = node;

        newRoot.right = node;
        newRoot.parent = node.parent;
        node.parent = newRoot;

        updateHeight(node);
        updateHeight(newRoot);

        return newRoot;
    }

    /**
     * Recupera o valor associado à chave fornecida.
     *
     * @param key a chave do elemento a ser buscado.
     * @return o valor associado à chave ou {@code null} se a chave não existir na árvore.
     */
    public V get(K key) {
        Node<V> node = search(key);
        return (node != null) ? node.value : null;
    }

    @Override
    public Iterator<V> iterator() {
        List<V> elements = new ArrayList<>();
        inOrderTraversal(root, elements);
        return elements.iterator();
    }

    private void inOrderTraversal(Node<V> node, List<V> result) {
        if (node == null) return;
        inOrderTraversal(node.left, result);
        result.add(node.value);
        inOrderTraversal(node.right, result);
    }

    /**
     * Limpa todos os elementos da árvore AVL.
     */
    public void clear() {
        root = null;
        size = 0;
    }

    public static class Node<V> {
        V value;
        Node<V> left, right, parent;
        int height = 1;

        Node(V value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString();
        }

        public V getValue() {
            return value;
        }
    }
}

