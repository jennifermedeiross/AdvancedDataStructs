public class Pessoa{
    private String nome;
    private String idade;
    private String cpf;
    private String telefone;

    public class pessoa(String nome ,int idade , String cpf , String telefone){
        this.idade = idade;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
    }

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

    @Override
    public String toString(){
        return "Nome : " + this.nome + "\nIdade : " + this.idade + "\nCPF : " + this.cpf;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pessoa pessoa = (Pessoa) obj;
        return this.cpf.equals(outraPessoa.cpf);
    }

    @Override
    public int hashCode(){
        return Objects.hash(cpf);
    }

}