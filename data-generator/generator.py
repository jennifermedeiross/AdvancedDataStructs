import json
from datetime import date
from faker import Faker
from pathlib import Path

fake = Faker("pt_BR")

def gera_dados():
    """
    Gera um dicionário contendo informações fictícias de uma pessoa, incluindo:
    nome, CPF, idade, telefone e data de nascimento.

    Retorna:
        dict: Um dicionário contendo os seguintes dados da pessoa:
            - "Nome" (str): Nome completo fictício.
            - "Cpf" (str): CPF fictício formatado.
            - "Idade" (int): Idade calculada com base na data de nascimento.
            - "Telefone" (str): Número de telefone celular fictício.
            - "Data de nascimento" (str): Data de nascimento formatada no padrão "dd/mm/yyyy".
    """
    data_nascimento = fake.date_of_birth(minimum_age=18, maximum_age=110)
    hoje = date.today()
    
    idade = hoje.year - data_nascimento.year - ((hoje.month, hoje.day) < (data_nascimento.month, data_nascimento.day))

    pessoa = {
        "nome": fake.name(),
        "cpf": fake.cpf(),
        "idade": idade,
        "telefone": fake.cellphone_number(),
        "dataNascimento": data_nascimento.strftime("%d/%m/%Y")
    }
    return pessoa

def salvar_dados(quantidade=1000):
    """
    Gera e salva um conjunto de dados fictícios em um arquivo JSON.

    Parâmetros:
        quantidade (int, opcional): Número de registros a serem gerados. Padrão é 50.
        arquivo_nome (str, opcional): Nome do arquivo onde os dados serão salvos. Padrão é "dados.json".

    Retorna:
        None: Os dados são salvos diretamente no arquivo JSON.
    """
    current_dir = Path(__file__).resolve().parent
    arquivo_nome = current_dir.parents[0] / f"benchmark-core/src/main/java/br/com/project/data/dados-{quantidade}.json"
    dados = [gera_dados() for _ in range(quantidade)]
    with open(arquivo_nome, "w", encoding="utf-8") as arquivo:
        json.dump(dados, arquivo, indent=4, ensure_ascii=False)

# Gera e salva os dados fictícios
quantidade = [1000, 5000, 10000, 25000, 50000, 100000, 500000, 100000000]
for i in range(8):
    salvar_dados(quantidade[i])
