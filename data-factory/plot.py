import pandas as pd
import matplotlib.pyplot as plt
from pathlib import Path
from matplotlib.ticker import ScalarFormatter

def ler_csv_e_gerar_grafico(caminho_arquivo):
    df = pd.read_csv(caminho_arquivo)

    colunas_necessarias = ["Estrutura", "Carga", "Operação", "Tempo (ms)", "Memória"]
    if not all(col in df.columns for col in colunas_necessarias):
        print("O CSV não contém todas as colunas necessárias:", colunas_necessarias)
        return

    estruturas = df["Estrutura"].unique()
    operacoes = df["Operação"].unique()

    # Agrupa por Estrutura, Carga, Operação e tira a média
    df_media = df.groupby(["Estrutura", "Carga", "Operação"]).mean(numeric_only=True).reset_index()

    pasta_resultados = Path("/home/oscar-rodrigues/Faculdade/AdvancedDataStructs/results")
    pasta_resultados.mkdir(parents=True, exist_ok=True)

    for operacao in operacoes:
        # Gráfico de Tempo
        plt.figure(figsize=(10, 6))
        for estrutura in estruturas:
            subset = df_media[(df_media["Estrutura"] == estrutura) & (df_media["Operação"] == operacao)]
            subset = subset.sort_values(by="Carga")

            plt.plot(
                subset["Carga"],
                subset["Tempo (ms)"],
                marker='o',
                label=estrutura
            )

        plt.title(f"Tempo Médio de Execução - Operação: {operacao}")
        plt.xlabel("Carga (Quantidade de registros)")
        plt.ylabel("Tempo Médio (ms)")
        plt.xticks(subset["Carga"], rotation=45)
        plt.legend(title="Estrutura")
        plt.grid(True)
        plt.tight_layout()
        plt.savefig(pasta_resultados / f"grafico_tempo_medio_{operacao}.png")
        plt.close()

        # Gráfico de Memória
        plt.figure(figsize=(10, 6))
        for estrutura in estruturas:
            subset = df_media[(df_media["Estrutura"] == estrutura) & (df_media["Operação"] == operacao)]
            subset = subset.sort_values(by="Carga")

            plt.plot(
                subset["Carga"],
                subset["Memória"],
                marker='o',
                label=estrutura
            )

        plt.title(f"Uso Médio de Memória - Operação: {operacao}")
        plt.xlabel("Carga (Quantidade de registros)")
        plt.ylabel("Memória Média (KB)")
        plt.xticks(subset["Carga"], rotation=0)
        plt.legend(title="Estrutura")
        plt.grid(True)
        plt.tight_layout()
        plt.savefig(pasta_resultados / f"grafico_memoria_medio_{operacao}.png")
        plt.close()

# Caminho do CSV
caminho_arquivo = "/home/oscar-rodrigues/Faculdade/AdvancedDataStructs/results/search.csv"
ler_csv_e_gerar_grafico(caminho_arquivo)
