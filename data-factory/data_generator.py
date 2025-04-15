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

    Os dados gerados são armazenados no diretório:
    'benchmark-core/src/main/java/br/com/project/data/', com o nome
    'dados-{quantidade}.json', onde {quantidade} representa a quantidade de registros gerados.

    Parâmetros:
        quantidade (int, opcional): Número de registros a serem gerados. Padrão é 1000.

    Retorna:
        None
    """
    root_path = Path(__file__).resolve().parent.parent
    data_dir = root_path / "benchmark-core/src/main/java/br/com/project/data"
    data_dir.mkdir(parents=True, exist_ok=True)

    arquivo = data_dir / f"dados-{quantidade}.json"
    dados = [gera_dados() for _ in range(quantidade)]
    with arquivo.open("w", encoding="utf-8") as f:
        json.dump(dados, f, indent=4, ensure_ascii=False)

quantidade = [1000, 5000, 10000, 25000, 50000, 100000, 250000, 500000, 750000,1000000,1500000]
for i in range(len(quantidade)):
   salvar_dados(quantidade[i])
