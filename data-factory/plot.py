import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path

def gerar_grafico(nome_csv: str, operacao: str, pasta_dados: Path):
    caminho_csv = pasta_dados / nome_csv
    df = pd.read_csv(caminho_csv)
    df['Carga'] = df['Carga'].astype(str)

    sns.set(style="whitegrid")
    plt.figure(figsize=(10, 6))
    sns.lineplot(data=df, x='Carga', y='Tempo (ms)', hue='Estrutura', marker='o')
    plt.title(f'Desempenho por Estrutura de Dados - {operacao}')
    plt.xlabel('Carga de Dados')
    plt.ylabel('Tempo (ms)')
    plt.legend(title='Estrutura')
    plt.tight_layout()

    nome_imagem = pasta_dados / f"grafico_{caminho_csv.stem}.png"
    plt.savefig(nome_imagem, dpi=300)
    plt.close()
    print(f"Gráfico salvo em: {nome_imagem}")

# Configuração dos caminhos
root_path = Path(__file__).resolve().parent.parent
data_dir = root_path / "results"
data_dir.mkdir(parents=True, exist_ok=True)

# Lista de operações
operacoes = [
    ("insert.csv", "Inserção"),
    ("remove.csv", "Remoção"),
    ("search.csv", "Busca")
]

# Gerar gráficos para todas as operações
for nome_csv, operacao in operacoes:
    gerar_grafico(nome_csv, operacao, data_dir)