import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path

root_path = Path(__file__).resolve().parent.parent
data_dir = root_path / "results"

data_dir.mkdir(parents=True, exist_ok=True)

csv_name = "insert.csv"
csv_path = data_dir / csv_name

df = pd.read_csv(csv_path)
df['Carga'] = df['Carga'].astype(str)

operation = "Inserção"

sns.set(style="whitegrid")
plt.figure(figsize=(10, 6))
sns.lineplot(data=df, x='Carga', y='Tempo (ms)', hue='Estrutura', marker='o')
plt.title(f'Desempenho por Estrutura de Dados - {operation}')
plt.xlabel('Carga de Dados')
plt.ylabel('Tempo (ms)')
plt.legend(title='Estrutura')
plt.tight_layout()

output_image = data_dir / f"grafico_{csv_path.stem}.png"

plt.savefig(output_image, dpi=300)
plt.close()

print(f"Gráfico salvo em: {output_image}")
