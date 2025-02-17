import json
import random
from faker import Faker
fake = Faker("pt_BR")

#Função que gera os dados de cada pessoa.
def gera_dados():
    pessoa= {"Nome":  fake.name(),
             "Cpf":  fake.cpf(),
             "Idade":  random.randint(18,80),
             "Telefone":  fake.cellphone_number()
             }
    return pessoa

#Dados vai recebor os dados criados para cada pessoa.
#Atenção: se necessario for mude a numeração do range para mudar a quantidade de dados gerados.

dados = [gera_dados() for i in range(50)]

#Nas linhas de código abaixo nos abrimos (ou criamos se não ouver) 0 arquivo dados.json no modo de escrita (w) e chamamos a função jason.dump que serve para escrever os dados dentro da biblioteca dados no aquivo.

with open("dados.json", "w", encoding="utf-8") as arquivo:
    json.dump(dados, arquivo, indent= 4, ensure_ascii = False)