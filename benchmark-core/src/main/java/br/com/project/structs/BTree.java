package br.com.project.structs;

import br.com.project.entities.Pessoa;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
*  @author Oscar Rodrigues Da Silva Neto 
*  
*  Implementação de uma B-Tree (Árvore B) para armazenar objetos da classe
 * {@code Pessoa}.
 * A árvore mantém os dados ordenados pelo CPF e suporta inserção , busca e
 * deleção.
 */
public class BTree {
    private BTreeNode root;
    private int t;

    /**
     * Construtor da B-Tree.
     * 
     * @param t Grau mínimo da árvore B (quantidade mínima de filhos por nó).
     */
    public BTree(int t) {
        this.root = new BTreeNode(true);
        this.t = t;
    }

    /**
     * Insere um objeto {@code Pessoa} na árvore B.
     * 
     * @param pessoa Objeto da classe {@code Pessoa} a ser inserido.
     */
    public void add(Pessoa pessoa) {
        BTreeNode r = root;
        if (r.keys.size() == (2 * t - 1)) { // Se o nó está cheio, divide
            BTreeNode s = new BTreeNode(false);
            root = s;
            s.children.add(r);
            splitChild(s, 0, r);
            insertNonFull(s, pessoa);
        } else {
            insertNonFull(r, pessoa);
        }
    }

    /**
     * Insere uma pessoa em um nó que ainda não está cheio.
     * 
     * @param node   Nó onde a pessoa será inserida.
     * @param pessoa Objeto da classe {@code Pessoa} a ser inserido.
     */
    private void insertNonFull(BTreeNode node, Pessoa pessoa) {
        int i = node.keys.size() - 1;

        if (node.isLeaf) {
            node.keys.add(pessoa);
            Collections.sort(node.keys, (p1, p2) -> p1.getCpf().compareTo(p2.getCpf()));
        } else {
            while (i >= 0 && pessoa.getCpf().compareTo(node.keys.get(i).getCpf()) < 0) {
                i--;
            }
            i++;
            if (node.children.get(i).keys.size() == (2 * t - 1)) {
                splitChild(node, i, node.children.get(i));
                if (pessoa.getCpf().compareTo(node.keys.get(i).getCpf()) > 0) {
                    i++;
                }
            }
            insertNonFull(node.children.get(i), pessoa);
        }
    }

    /**
     * Divide um nó filho que está cheio.
     * 
     * @param parent Nó pai do nó que será dividido.
     * @param i      Índice do nó filho a ser dividido.
     * @param child  Nó filho a ser dividido.
     */
    private void splitChild(BTreeNode parent, int i, BTreeNode child) {
        BTreeNode newChild = new BTreeNode(child.isLeaf);
        int mid = t - 1;
        parent.keys.add(i, child.keys.get(mid));

        newChild.keys.addAll(child.keys.subList(mid + 1, child.keys.size()));
        child.keys.subList(mid, child.keys.size()).clear();

        if (!child.isLeaf) {
            newChild.children.addAll(child.children.subList(mid + 1, child.children.size()));
            child.children.subList(mid + 1, child.children.size()).clear();
        }

        parent.children.add(i + 1, newChild);
    }

    /**
     * Busca uma pessoa na árvore pelo CPF.
     * 
     * @param cpf CPF da pessoa a ser buscada.
     * @return Objeto {@code Pessoa} se encontrado, caso contrário {@code null}.
     */
    public Pessoa search(String cpf) {
        return search(root, cpf);
    }

    /**
     * Busca uma pessoa recursivamente na árvore pelo CPF.
     * 
     * @param node Nó atual da busca.
     * @param cpf  CPF da pessoa a ser buscada.
     * @return Objeto {@code Pessoa} se encontrado, caso contrário {@code null}.
     */
    private Pessoa search(BTreeNode node, String cpf) {
        int i = 0;
        while (i < node.keys.size() && cpf.compareTo(node.keys.get(i).getCpf()) > 0) {
            i++;
        }

        if (i < node.keys.size() && cpf.equals(node.keys.get(i).getCpf())) {
            return node.keys.get(i);
        }

        if (node.isLeaf) {
            return null;
        }

        return search(node.children.get(i), cpf);
    }

    /**
     * Remove uma pessoa da árvore pelo CPF.
     * 
     * @param cpf CPF da pessoa a ser removida.
     */
    public void delete(String cpf) {
        delete(root, cpf);

        // Se a raiz ficar vazia após a remoção, ajustamos a estrutura da árvore
        if (root.keys.isEmpty() && !root.isLeaf) {
            root = root.children.get(0);
        }
    }

    /**
     * Remove uma pessoa recursivamente pelo CPF.
     * 
     * @param node Nó atual da busca.
     * @param cpf  CPF da pessoa a ser removida.
     */
    private void delete(BTreeNode node, String cpf) {
        int idx = 0;
        while (idx < node.keys.size() && cpf.compareTo(node.keys.get(idx).getCpf()) > 0) {
            idx++;
        }

        // Caso 1: CPF encontrado no nó atual
        if (idx < node.keys.size() && cpf.equals(node.keys.get(idx).getCpf())) {
            if (node.isLeaf) {
                node.keys.remove(idx);
            } else {
                deleteInternalNode(node, idx);
            }
        } else {
            // Caso 2: CPF não está no nó atual e precisa continuar a busca
            if (node.isLeaf)
                return; // Se for folha e não achou, CPF não está na árvore

            boolean lastChild = (idx == node.keys.size());

            // Se o filho onde devemos procurar tiver menos que 't' chaves, garantimos que
            // ele terá no mínimo 't'
            if (node.children.get(idx).keys.size() < t) {
                fill(node, idx);
            }

            // Agora podemos continuar a remoção no filho corrigido
            delete(node.children.get(lastChild ? idx - 1 : idx), cpf);
        }
    }

    /**
     * Remove uma chave de um nó interno.
     * 
     * @param node Nó onde a chave será removida.
     * @param idx  Índice da chave a ser removida.
     */
    private void deleteInternalNode(BTreeNode node, int idx) {
        Pessoa key = node.keys.get(idx);

        // Caso 1: Se o filho anterior (esquerda) tem pelo menos 't' chaves,
        // substituímos pela maior chave do filho esquerdo
        if (node.children.get(idx).keys.size() >= t) {
            Pessoa predecessor = getPredecessor(node, idx);
            node.keys.set(idx, predecessor);
            delete(node.children.get(idx), predecessor.getCpf());
        }
        // Caso 2: Se o filho direito tem pelo menos 't' chaves, substituímos pela menor
        // chave do filho direito
        else if (node.children.get(idx + 1).keys.size() >= t) {
            Pessoa successor = getSuccessor(node, idx);
            node.keys.set(idx, successor);
            delete(node.children.get(idx + 1), successor.getCpf());
        }
        // Caso 3: Ambos os filhos têm menos que 't' chaves, então fundimos os dois
        // filhos e removemos a chave
        else {
            merge(node, idx);
            delete(node.children.get(idx), key.getCpf());
        }
    }

    /**
     * Obtém o predecessor de uma chave.
     * 
     * @param node Nó onde a chave está localizada.
     * @param idx  Índice da chave.
     * @return O objeto {@code Pessoa} predecessor.
     */
    private Pessoa getPredecessor(BTreeNode node, int idx) {
        BTreeNode cur = node.children.get(idx);
        while (!cur.isLeaf) {
            cur = cur.children.get(cur.keys.size());
        }
        return cur.keys.get(cur.keys.size() - 1);
    }

    /**
     * Obtém o sucessor de uma chave.
     * 
     * @param node Nó onde a chave está localizada.
     * @param idx  Índice da chave.
     * @return O objeto {@code Pessoa} sucessor.
     */
    private Pessoa getSuccessor(BTreeNode node, int idx) {
        BTreeNode cur = node.children.get(idx + 1);
        while (!cur.isLeaf) {
            cur = cur.children.get(0);
        }
        return cur.keys.get(0);
    }

    /**
     * Garante que um nó filho tenha pelo menos 't' chaves antes de remover.
     * 
     * @param parent Nó pai.
     * @param idx    Índice do filho no nó pai.
     */
    private void fill(BTreeNode parent, int idx) {
        if (idx > 0 && parent.children.get(idx - 1).keys.size() >= t) {
            borrowFromPrev(parent, idx);
        } else if (idx < parent.keys.size() && parent.children.get(idx + 1).keys.size() >= t) {
            borrowFromNext(parent, idx);
        } else {
            if (idx < parent.keys.size()) {
                merge(parent, idx);
            } else {
                merge(parent, idx - 1);
            }
        }
    }

    /**
     * Pega uma chave do filho anterior e move para o nó atual.
     * 
     * @param parent Nó pai.
     * @param idx    Índice do filho atual.
     */
    private void borrowFromPrev(BTreeNode parent, int idx) {
        BTreeNode child = parent.children.get(idx);
        BTreeNode sibling = parent.children.get(idx - 1);

        child.keys.add(0, parent.keys.get(idx - 1));
        parent.keys.set(idx - 1, sibling.keys.remove(sibling.keys.size() - 1));

        if (!sibling.isLeaf) {
            child.children.add(0, sibling.children.remove(sibling.children.size() - 1));
        }
    }

    /**
     * Pega uma chave do filho seguinte e move para o nó atual.
     * 
     * @param parent Nó pai.
     * @param idx    Índice do filho atual.
     */
    private void borrowFromNext(BTreeNode parent, int idx) {
        BTreeNode child = parent.children.get(idx);
        BTreeNode sibling = parent.children.get(idx + 1);

        child.keys.add(parent.keys.get(idx));
        parent.keys.set(idx, sibling.keys.remove(0));

        if (!sibling.isLeaf) {
            child.children.add(sibling.children.remove(0));
        }
    }

    /**
     * Mescla um nó filho com seu irmão.
     * 
     * @param parent Nó pai.
     * @param idx    Índice do filho a ser mesclado.
     */
    private void merge(BTreeNode parent, int idx) {
        BTreeNode child = parent.children.get(idx);
        BTreeNode sibling = parent.children.get(idx + 1);

        child.keys.add(parent.keys.remove(idx));
        child.keys.addAll(sibling.keys);
        child.children.addAll(sibling.children);

        parent.children.remove(idx + 1);
    }

}

/**
 * Implementação de uma B-TreeNode (Nó) que armazena as chaves do tipo pessoa e
 * os filhos do tipo BTreeNode.
 * A árvore mantém os dados ordenados pelo CPF e suporta inserção , busca e
 * deleção.
 */

class BTreeNode {
    List<Pessoa> keys;
    List<BTreeNode> children;
    boolean isLeaf;

    /**
     * Construtor do nó da árvore B.
     * 
     * @param isLeaf Indica se o nó é uma folha.
     */
    public BTreeNode(boolean isLeaf) {
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = isLeaf;
    }
}