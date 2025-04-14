package br.com.project.entities;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;
import lombok.Setter;

/**
 * A classe {@code Pessoa} representa uma pessoa com atributos básicos como nome, CPF, idade, telefone e data de nascimento.
 * Ela inclui métodos para acessar e modificar esses atributos, além de implementar igualdade baseada no CPF.
 */
@Getter
@Setter
public class Pessoa implements Comparable<Pessoa>, Serializable {
    private String nome;
    private int idade;
    private String cpf;
    private String telefone;
    private String dataNascimento;
<<<<<<< HEAD

=======
>>>>>>> 593a20f3a504b5d96e1a8cb12cd0fddcb73a326c

    /**
     * Construtor que inicializa um objeto {@code Pessoa} com os dados fornecidos.
     *
     * @param nome     Nome da pessoa;
     * @param cpf      CPF da pessoa(Identificador único);
     * @param idade    idade da pessoa;
     * @param telefone telefone da pessoa;
     * @param dataNascimento data de nascinemto da pessoa;
     */
<<<<<<< HEAD

    public Pessoa(String nome, int idade , String cpf , String telefone, String dataNascimento){

        this.idade = idade;
=======
    public Pessoa(String nome, String cpf, int idade, String telefone, String dataNascimento) {
>>>>>>> 593a20f3a504b5d96e1a8cb12cd0fddcb73a326c
        this.nome = nome;
        this.cpf = cpf;
        this.idade = idade;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
    }

    public Pessoa() {
    }

<<<<<<< HEAD
    public void setNome(String nome){
        this.nome = nome;
    }

    public int getIdade(){
        return this.idade;
    }

    public void setIdade(int idade){
        this.idade = idade;
    }

    public String getCpf(){
        return this.cpf;
    }

    public void setCpf(String cpf){
        this.cpf = cpf;
    }

    public String getTelefone(){
        return this.telefone;
    }
    public void setTelefone(String telefone){
        this.telefone = telefone;
    }

    public String getDataNascimento() { return dataNascimento; }

    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }

=======
>>>>>>> 593a20f3a504b5d96e1a8cb12cd0fddcb73a326c
    @Override
    public String toString() {
        return "Nome: " + nome + "\n" +
                "CPF: " + cpf + "\n" +
                "Idade: " + idade + "\n" +
                "Telefone: " + telefone + "\n" +
                "Data de Nasc.: " + dataNascimento;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pessoa pessoa = (Pessoa) obj;
        return this.cpf.equals(pessoa.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(cpf);
    }

    /**
     * Converte o objeto {@code Pessoa} atual em uma representação JSON.
     *
     * @return uma {@code String} contendo a representação JSON do objeto {@code Pessoa}.
     * @throws RuntimeException se ocorrer algum erro durante a conversão.
     */
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter Pessoa para JSON", e);
        }
    }

    /**
     * Cria uma instância de {@code Pessoa} a partir de uma {@code String} JSON.
     *
     * @param json a {@code String} contendo os dados da pessoa no formato JSON.
     * @return um objeto {@code Pessoa} construído a partir da string JSON.
     * @throws RuntimeException se ocorrer erro de mapeamento ou processamento do JSON.
     */
    public static Pessoa fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try {
            return mapper.readValue(json, Pessoa.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException("Erro ao mapear JSON para Pessoa", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao processar JSON para Pessoa", e);
        }
    }

    @Override
    public int compareTo(Pessoa other) {
        return this.cpf.compareTo(other.cpf);
    }
}