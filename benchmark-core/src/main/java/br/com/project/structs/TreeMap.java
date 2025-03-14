package br.com.project.structs;
import java.util.*;
import br.com.project.entities.Pessoa;

/**
 * Esta classe implementa uma estrutura de dados de árvore balanceada que garante
 * operações de inserção, remoção e busca em tempo logarítmico (O(log n)). A árvore
 * rubro-negra é uma árvore binária de busca balanceada.
 *
 * Essa estrutura foi implementada para ser utilizada para a implementação de métodos específicos para manipulação
 * de objetos da classe
 *
 * {@code Pessoa}.
 */
public class TreeMap<K extends Comparable<K>, V> {

    // constantes para representar as cores do nós.
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    /**
     * Classe interna que representa um no da árvore.
     * Cada nó contém uma chave , um valor , referência para os filhos esquerdo e direito , e uma cor(vermelho ou preto).
     */
    private class Node {
        K key;
        V value;
        Node left, right;
        boolean color;

        Node(K key, V value, boolean color) {
            this.key = key;
            this.value = value;
            this.color = color;
        }
    }

    private Node root;// raiz da árvore
    private int size = 0; // Contador de elementos

    /**
     *  Verifica se o nó é vermelho
     *  Um nó nulo é considerado preto.
     */
    private boolean isRed(Node node) {
        return node != null && node.color == RED;
    }

    /**
     * Realiza a rotação para a esquerda , para balancear a árvore quando há desequilíbrio à direita.
     * @param h Nó que vai sofrer a rotação.
     * @return O nó após sofrer a rotação.
     */
    private Node rotateLeft(Node h) {
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    /**
     * Realiza a rotação para a direita , para balancear a árvore quando há desequilíbrio à esquerda.
     * @param h Nó que vai sofrer a rotação.
     * @return O nó após sofrer a rotação.
     */
    private Node rotateRight(Node h) {
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }


    /**
     * Troca as cores de um nó e de seus filhos para lidar com a violação da propriedade de cores.
     * @param h Nó que vai ter a cor alterada.
     */
    private void flipColors(Node h) {
        h.color = RED;
        if (h.left != null) h.left.color = BLACK;
        if (h.right != null) h.right.color = BLACK;
    }

    /**
     * Insere a partir do método insert um objeto na árvore que vai ser acessado a partir da sua chave.
     * @param key Chave que referência o objeto
     * @param value Objeto.
     */
    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Chave não pode ser nula.");
        root = insert(root, key, value);
        root.color = BLACK; // A raiz deve sempre ser preta
        size++;
    }

    /**
     * Método que insere um novo nó na árvore Red-Black ou atualiza o valor de um nó existente.
     * A inserção segue as regras da árvore Red-Black, garantindo o balanceamento adequado
     * por meio de rotações e inversões de cores.
     *
     * @param h O nó atual que está sendo analisado durante a inserção (subárvore).
     * @param key A chave associada ao nó que será inserido ou atualizado.
     * @param value O valor associado à chave que será armazenado no nó.
     * @return O nó atualizado após a inserção e possível balanceamento.
     */
    private Node insert(Node h, K key, V value) {
        if (h == null) return new Node(key, value, RED);
        // Compara o valor da chave atual com a que acabou de ser criada
        int cmp = key.compareTo(h.key);
        if (cmp < 0) h.left = insert(h.left, key, value);
        else if (cmp > 0) h.right = insert(h.right, key, value);
        else h.value = value; // Atualiza se a chave já existe

        // Balanceamento de acordo com a situação que ocorrer
        if (isRed(h.right) && !isRed(h.left)) h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right)) flipColors(h);

        return h;
    }

    /** Busca por uma chave
     *  Fazendo comparações com o valor do nó atual ara decidir se é ele que é o que a gente procura ou se vai continuar procurando para a esquerda ou direita */
    public V get(K key) {
        Node node = root;
        while (node != null) {
            int cmp = key.compareTo(node.key);
            if (cmp < 0) node = node.left;
            else if (cmp > 0) node = node.right;
            else return node.value;
        }
        return null;
    }


    public boolean containsKey(K key) {
        return get(key) != null;
    }


    public int size() {
        return size;
    }

    /** Limpa a árvore */
    public void clear() {
        root = null;
        size = 0;
    }

    /** Retorna a menor chave */
    public K firstKey() {
        Node node = root;
        if (node == null) return null;
        while (node.left != null) node = node.left;
        return node.key;
    }

    /** Retorna a maior chave */
    public K lastKey() {
        Node node = root;
        if (node == null) return null;
        while (node.right != null) node = node.right;
        return node.key;
    }
    /*
     * Este método remove o nó com a menor chave presente na árvore. Ele garante
     * que a árvore mantenha as propriedades de balanceamento da árvore Red-Black
     * após a remoção.
     *
     */
    public void deleteMin() {
        if (root == null) return;
        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;
        root = deleteMin(root);
        if (root != null) root.color = BLACK;
        size--;
    }

    private Node deleteMin(Node h) {
        if (h.left == null) return null;
        if (!isRed(h.left) && !isRed(h.left.left))
            h = moveRedLeft(h);
        h.left = deleteMin(h.left);
        return balance(h);
    }

    /** Remove um nó com a chave especificada */
    public void delete(K key) {
        if (key == null) throw new IllegalArgumentException("Chave não pode ser nula.");
        if (!containsKey(key)) return; // Chave não existe

        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;
        root = delete(root, key);
        if (root != null) root.color = BLACK;
        size--;
    }

    /**
     * Remove um nó com a chave especificada na árvore rubro-negra, preservando suas propriedades.
     *
     * O método realiza a busca pelo nó a ser removido, garantindo que a árvore permaneça balanceada
     * e que as regras de cores sejam mantidas. Ele lida com diferentes casos, como remoção de folhas,
     * rotação e substituição pelo menor nó da subárvore direita.
     *
     * @param h Nó atual da árvore.
     * @param key Chave do nó a ser removido.
     * @return O nó balanceado após a remoção.
     */
    private Node delete(Node h, K key) {
        if (key.compareTo(h.key) < 0) {
            if (!isRed(h.left) && !isRed(h.left.left))
                h = moveRedLeft(h);
            h.left = delete(h.left, key);
        } else {
            if (isRed(h.left))
                h = rotateRight(h);
            if (key.compareTo(h.key) == 0 && h.right == null)
                return null;
            if (!isRed(h.right) && !isRed(h.right.left))
                h = moveRedRight(h);
            if (key.compareTo(h.key) == 0) {
                Node minNode = min(h.right);
                h.key = minNode.key;
                h.value = minNode.value;
                h.right = deleteMin(h.right);
            } else {
                h.right = delete(h.right, key);
            }
        }
        return balance(h);
    }

    /**
     * Move o vermelho para a esquerda, garantindo que o nó à esquerda de um nó preto seja vermelho.
     * Isso é usado durante a remoção de um nó para facilitar a remoção na subárvore esquerda.
     *
     * @param h O nó atual a ser processado.
     * @return O nó atualizado após a operação.
     */
    private Node moveRedLeft(Node h) {
        flipColors(h);
        if (isRed(h.right.left)) {
            h.right = rotateRight(h.right);
            h = rotateLeft(h);
            flipColors(h);
        }
        return h;
    }

    /**
     * Move o vermelho para a direita, garantindo que o nó à direita de um nó preto seja vermelho.
     * Isso é usado durante a remoção de um nó para facilitar a remoção na subárvore direita.
     *
     * @param h O nó atual a ser processado.
     * @return O nó atualizado após a operação.
     */
    private Node moveRedRight(Node h) {
        flipColors(h);
        if (isRed(h.left.left)) {
            h = rotateRight(h);
            flipColors(h);
        }
        return h;
    }


    private Node min(Node h) {
        while (h.left != null)
            h = h.left;
        return h;
    }

    /** Balanceia a árvore */
    private Node balance(Node h) {
        if (isRed(h.right)) h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right)) flipColors(h);
        return h;
    }

    /**
     * Verifica se a árvore rubro-negra está balanceada, ou seja, se todos os caminhos
     * da raiz até as folhas contêm o mesmo número de nós pretos.
     *
     * @return true se a árvore estiver balanceada, false caso contrário.
     */
    public boolean isBalanced() {
        int blackNodes = 0; // Número de nós pretos no caminho da raiz até o nó mais à esquerda
        Node node = root;
        while (node != null) {
            if (!isRed(node)) blackNodes++;
            node = node.left;
        }
        return isBalanced(root, blackNodes);
    }

    /**
     * Método recursivo para verificar se todos os caminhos da árvore a partir do nó h
     * têm o mesmo número de nós pretos.
     *
     * @param h O nó atual sendo verificado.
     * @param blackNodes O número de nós pretos restantes para o caminho atual.
     * @return true se o caminho atual estiver balanceado, false caso contrário.
     */
    private boolean isBalanced(Node h, int blackNodes) {
        if (h == null) return blackNodes == 0;
        if (!isRed(h)) blackNodes--;
        return isBalanced(h.left, blackNodes) && isBalanced(h.right, blackNodes);
    }

    /** Retorna uma lista com as chaves em ordem */
    public List<K> keys() {
        List<K> keys = new ArrayList<>();
        inOrderKeys(root, keys);
        return keys;
    }

    private void inOrderKeys(Node node, List<K> keys) {
        if (node != null) {
            inOrderKeys(node.left, keys);
            keys.add(node.key);
            inOrderKeys(node.right, keys);
        }
    }

    /** Retorna uma lista com os valores em ordem */
    public List<V> values() {
        List<V> values = new ArrayList<>();
        inOrderValues(root, values);
        return values;
    }

    private void inOrderValues(Node node, List<V> values) {
        if (node != null) {
            inOrderValues(node.left, values);
            values.add(node.value);
            inOrderValues(node.right, values);
        }
    }

    /**
     * Retorna um iterador para percorrer as chaves da árvore em ordem crescente.
     *
     * @return Um iterador para as chaves da árvore.
     */
    public Iterator<K> iterator() {
        return new KeyIterator();
    }

    /**
     * Classe interna que implementa um iterador para percorrer as chaves da árvore em ordem crescente.
     */
    private class KeyIterator implements Iterator<K> {
        private Stack<Node> stack = new Stack<>();

        public KeyIterator() {
            pushLeft(root);
        }

        /**
         * Empilha todos os nós à esquerda de um nó dado.
         *
         * @param node O nó a partir do qual os nós à esquerda serão empilhados.
         */
        private void pushLeft(Node node) {
            while (node != null) {
                stack.push(node);
                node = node.left;
            }
        }


        /**
         * Verifica se ainda há elementos a serem percorridos pelo iterador.
         *
         * @return true se houver mais elementos, false caso contrário.
         */

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        /**
         * Retorna a próxima chave na ordem crescente e avança o iterador.
         *
         * @return A próxima chave na ordem crescente.
         * @throws NoSuchElementException Se não houver mais elementos a serem percorridos.
         */
        @Override
        public K next() {
            if (!hasNext()) throw new NoSuchElementException();
            Node node = stack.pop();
            pushLeft(node.right);
            return node.key;
        }
    }
}
