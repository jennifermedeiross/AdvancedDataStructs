package br.com.project.controllers;

import br.com.project.entities.Pessoa;
import br.com.project.service.DataReader;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StructuresController {
    private InsertController insertController;
    private SearchController searchController;
    private RemoveController removeController;
    private int[] quantidades;
    private Map<Integer, Pessoa[]> dadosMap;

    public StructuresController() {
        insertController = new InsertController();
        searchController = new SearchController();
        removeController = new RemoveController();
        quantidades = new int[]{1000, 5000, 10000, 25000, 50000, 100000, 250000, 500000, 750000, 1000000};
        dadosMap = new HashMap<>();
    }

    public void initAnalysis() throws IOException {
        loadData(); // Carrega tudo antes
        insertExecute();
        searchExecute();
        removeExecute();
    }

    private void loadData() throws IOException {
        Pessoa[] pessoas = null;
        for (int qnt : quantidades) {
            pessoas = DataReader.readJson(Pessoa[].class, "dados-" + qnt + ".json");
            dadosMap.put(qnt, pessoas);
        }

    }

    private void insertExecute() {
        for (int qnt : quantidades) {
            Pessoa[] pessoas = dadosMap.get(qnt);
            try {
                System.out.println("[INSERÇÃO]: " + qnt + " registros");
                insertController.insertLsm(pessoas);
                insertController.insertTreeMap(pessoas);
                insertController.insertBTree(pessoas);
            } catch (JsonProcessingException e) {
                System.err.println("[ERRO INSERÇÃO]: " + qnt + " registros - " + e.getMessage());
            }
        }
    }

    private void searchExecute() {
        for (int qnt : quantidades) {
            Pessoa[] pessoas = dadosMap.get(qnt);
            try {
                System.out.println("[BUSCA]: " + qnt + " registros");
                searchController.searchLsm(pessoas);
                searchController.searchTreeMap(pessoas);
                searchController.searchBTree(pessoas);
            } catch (JsonProcessingException e) {
                System.err.println("[ERRO BUSCA]: " + qnt + " registros - " + e.getMessage());
            }
        }
    }

    private void removeExecute() {
        for (int qnt : quantidades) {
            Pessoa[] pessoas = dadosMap.get(qnt);
            try {
                System.out.println("[REMOÇÃO]: " + qnt + " registros");
                removeController.removeLsm(pessoas);
                removeController.removeTreeMap(pessoas);
                removeController.removeBTree(pessoas);
            } catch (JsonProcessingException | InterruptedException e) {
                System.err.println("[ERRO REMOÇÃO]: " + qnt + " registros - " + e.getMessage());
            }
        }
    }
}
