package br.com.project.structs;
import java.util.*;
import br.com.project.entities.Pessoa;

public class RedBlackTreeMap<K extends Comparable<K>, V> {

    // consntes para representar as cores do nós.
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
     * Rotação à esquerda
     * Usada para balancear a árvore quando há desequilíbrio à direita.
     */
    private Node rotateLeft(Node h) {
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    /*
    * Rotação à direita
    * Usada para balancear a árvore quando há desequilíbrio à esquerda.
    */
    private Node rotateRight(Node h) {
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    /** Troca as cores de um nó e seus filhos */
    private void flipColors(Node h) {
        h.color = RED;
        if (h.left != null) h.left.color = BLACK;
        if (h.right != null) h.right.color = BLACK;
    }

    /*insere um par chave-valor*/
    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Chave não pode ser nula.");
        root = insert(root, key, value);
        root.color = BLACK; // A raiz deve sempre ser preta
        size++;
    }

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

    /** Verifica se uma chave existe */
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /** Retorna o número de elementos na árvore */
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

    /** Remove o menor nó */
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

    /** Move o vermelho para a esquerda */
    private Node moveRedLeft(Node h) {
        flipColors(h);
        if (isRed(h.right.left)) {
            h.right = rotateRight(h.right);
            h = rotateLeft(h);
            flipColors(h);
        }
        return h;
    }

    /** Move o vermelho para a direita */
    private Node moveRedRight(Node h) {
        flipColors(h);
        if (isRed(h.left.left)) {
            h = rotateRight(h);
            flipColors(h);
        }
        return h;
    }

    /** Encontra o menor nó */
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

    /** Verifica se a árvore está balanceada */
    public boolean isBalanced() {
        int blackNodes = 0; // Número de nós pretos no caminho da raiz até o nó mais à esquerda
        Node node = root;
        while (node != null) {
            if (!isRed(node)) blackNodes++;
            node = node.left;
        }
        return isBalanced(root, blackNodes);
    }

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

    /** Iterador para percorrer as chaves em ordem */
    public Iterator<K> iterator() {
        return new KeyIterator();
    }

    private class KeyIterator implements Iterator<K> {
        private Stack<Node> stack = new Stack<>();

        public KeyIterator() {
            pushLeft(root);
        }

        private void pushLeft(Node node) {
            while (node != null) {
                stack.push(node);
                node = node.left;
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public K next() {
            if (!hasNext()) throw new NoSuchElementException();
            Node node = stack.pop();
            pushLeft(node.right);
            return node.key;
        }
    }


    /**
     *  MÉTODOS ESPECÍFICOS PARA A CLASSE PESSOA
     */


    // Método para adicionar uma pessoa
    public void adicionarPessoa(Pessoa pessoa) {
        if (pessoa == null) throw new IllegalArgumentException("Pessoa não pode ser nula.");
        root = put(root, pessoa.getCpf(), (V) pessoa);  // A chave é o CPF
        root.color = BLACK;
        size++;
    }

    // Método para buscar uma pessoa
    public Pessoa buscarPessoa(String cpf) {
        if (cpf == null) throw new IllegalArgumentException("CPF não pode ser nulo.");
        return (Pessoa) get(root, cpf);
    }

    // Método para remover uma pessoa
    public void removerPessoa(String cpf) {
        if (cpf == null) throw new IllegalArgumentException("CPF não pode ser nulo.");
        if (!containsKey(cpf)) return;

        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;
        root = delete(root, cpf);
        if (root != null) root.color = BLACK;
        size--;
    }
}