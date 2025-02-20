package br.com.project.entities;

import java.time.LocalDate;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonFormat;


/**
 * A classe {@code Pessoa} representa uma pessoa com atributos básicos como nome, idade, CPF e telefone e data de nascimento.
 * Ela inclui métodos para acessar e modificar esses atributos, além de implementar igualdade baseada no CPF.
 */
public class Pessoa{
    private String nome;
    private int idade;
    private String cpf;
    private String telefone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate dataNascimento;


    /**
     *
     * @param nome Nome da pessoa;
     * @param idade idade da pessoa;
     * @param cpf CPF da pessoa(Identificador único);
     * @param telefone telefone da pessoa;
     * @param dataNascimento Data de nascimento da pessoa.
     */

    public Pessoa(String nome, int idade , String cpf , String telefone, LocalDate dataNascimento){

        this.idade = idade;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
    }

    public Pessoa(){}

    public String getNome(){
        return this.nome;
    }

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

    public LocalDate getDataNascimento() { return dataNascimento; }

    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    @Override
    public String toString(){
        return "Nome : " + this.nome +"\nData de nascimento : " + this.dataNascimento + "\nIdade : " + this.idade + "\nCPF : " + this.cpf + "\nTelefone : " + this.telefone;
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
}