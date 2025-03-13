# Se o pacote não tiver instalado na máquina.
#install.packages('ggplot2', repos = "http://cran.us.r-project.org")

library('ggplot2')

args <- commandArgs()

data = read.table(args[length(args)], header = T)
ggplot(data, aes(x = amostra, y = time, colour = estrutura)) + geom_line()# Carregar a biblioteca ggplot2
library(ggplot2)

data <- read.table("../AdvancedDataStructs/benchmark-core/src/main/java/br/com/project/data/analiseDados.txt", 
                   header = TRUE, sep = " ", stringsAsFactors = FALSE)

print(head(data))


grafico <- ggplot(data, aes(x = amostra, y = time, colour = estrutura)) + 
  geom_line() + 
  geom_point() + 
  labs(title = "Procura", x = "Amostra", y = "Tempo", color = "Algoritmo") +
  theme_minimal()

caminho_saida <- "../AdvancedDataStructs/benchmark-core/src/main/java/br/com/project/data/.grafico.png"

ggsave(filename = caminho_saida, plot = grafico, width = 8, height = 6, dpi = 300)

cat("Gráfico salvo em:", caminho_saida, "\n")
