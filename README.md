# Pedal Gaiden 🚴‍♂️🥷
<img width="1280" height="716" alt="Screenshot 2026-11-11 at 2 17 47 AM" src="https://github.com/user-attachments/assets/3d38e68a-c4e5-4719-9c99-bd0a9943e3de" />


**Pedal Gaiden** é um protótipo jogável de jogo sério 2D, desenvolvido em **Java com libGDX**, que combina ciclismo, coleta seletiva e uma temática inspirada no Japão. O personagem é um ninja ciclista cuja velocidade acompanha a cadência de pedalada (RPM). O projeto explora o uso de sensores IoT em experiências interativas voltadas à atividade física.

> **Estado do projeto:** protótipo funcional de interface e jogabilidade. Não é um dispositivo médico nem foi clinicamente validado. Metas de cadência, pontuação e parâmetros de sessão não devem ser interpretados como recomendações de reabilitação.

## Funcionalidades

- Menu principal com **Jogar**, **Instruções**, **Configurações** e **Créditos**.
- Fluxo de perfil do jogador: dados pessoais, objetivos, experiência, confirmação e seleção de fases. O nome inicial é **Jogador 1**; os dados não são persistidos entre execuções.
- Interface em **português brasileiro**, com pontuação, tempo restante, cadência, ritmo, itens reciclados e Energia Eco.
- Cenários em pixel art com rolagem paralaxe; quatro faixas de trânsito utilizáveis.
- Coleta de resíduos, reciclagem automática em lixeiras correspondentes e efeitos visuais e sonoros.
- Carros animados, alertas de tráfego, colisões com redução temporária de velocidade e imunidade breve.
- Pausa, reinício, contagem regressiva e tela de resultados.
- Integração experimental com sensor de cadência **XOSS via Bluetooth Low Energy (BLE)**, por uma ponte Python local.

## Requisitos

- **JDK 17 ou superior**.
- **Gradle** instalado e disponível no terminal.
- macOS para o fluxo BLE descrito abaixo (a compatibilidade com outros sistemas não foi documentada).
- Para integração com sensor: **Python 3**, Bluetooth habilitado e sensor XOSS compatível com o serviço BLE de velocidade/cadência (CSC).

## Executar o jogo

Na raiz do repositório:

```bash
gradle :lwjgl3:run
```

No macOS, o LWJGL3 exige a opção JVM `-XstartOnFirstThread`. Caso apareça um erro de GLFW relacionado à *main thread*, configure a tarefa `run` em `lwjgl3/build.gradle`:

```gradle
tasks.named('run') {
    jvmArgs '-XstartOnFirstThread'
}
```

Se já existir uma configuração para essa tarefa, adicione apenas o argumento JVM ao bloco existente.

### Controles

| Ação | Controle |
| --- | --- |
| Simular pedalada (desenvolvimento) | Segurar `SPACE` ou `↑` |
| Simular aceleração extra (desenvolvimento) | Segurar `SHIFT` enquanto pedala |
| Mudar de faixa | `W` / `S`, `←` / `→` ou botões **FAIXA − / FAIXA +** |
| Pausar | `P` ou `ESC` |
| Navegar nos menus | Mouse |

**Atenção:** os comandos de teclado são recursos de desenvolvimento ou de controle auxiliar. A velocidade de deslocamento pode ser alimentada pelo sensor real no modo `--iot`; a troca de faixas permanece manual na implementação descrita aqui. Caso a avaliação exija controle **exclusivamente por IoT**, essa parte ainda precisa ser adaptada.

## Integração com sensor XOSS (macOS)

O fluxo implementado é:

```text
Sensor XOSS (modo Cadence)
       ↓ Bluetooth Low Energy (CSC)
Ponte Python: tools/ble_cadence.py
       ↓ UDP local — 127.0.0.1:5055
Jogo Java / libGDX (modo --iot)
       ↓
Cadência (RPM) → velocidade do personagem
```

**Não é necessário ESP32 para este fluxo específico.** Se a documentação acadêmica do projeto descrever `XOSS → ESP32 → USB → jSerialComm`, ela precisa ser atualizada ou essa arquitetura alternativa precisa ser efetivamente implementada.

### 1. Preparar o sensor

1. No aplicativo XOSS, selecione o modo **Cadence**, e não **Speed**.
2. Prenda o sensor ao pedivela conforme as orientações do fabricante e gire os pedais para ativá-lo.
3. Confirme a leitura de RPM no aplicativo e depois desconecte o aplicativo do sensor, para liberar a conexão BLE.
4. Permita acesso ao Bluetooth para o terminal/Python em **Ajustes do Sistema → Privacidade e Segurança → Bluetooth**, se solicitado.

### 2. Instalar a ponte Python

Na raiz do projeto:

```bash
python3 -m venv .venv-ble
.venv-ble/bin/python -m pip install -r tools/requirements.txt
```

### 3. Localizar e inspecionar o dispositivo

```bash
.venv-ble/bin/python tools/ble_cadence.py scan
```

O comando busca candidatos pelo nome XOSS ou pelo serviço CSC (`1816`). No macOS, o identificador exibido é um **UUID**, não um endereço MAC. Confirme o dispositivo e os serviços disponíveis:

```bash
.venv-ble/bin/python tools/ble_cadence.py inspect --device "UUID-RETORNADO-PELO-SCAN"
```

A ponte procura a característica **CSC Measurement** (`2A5B`) com notificações e, quando disponível, **CSC Feature** (`2A5C`). A detecção do dispositivo, por si só, não comprova que ele fornece dados de cadência: isso deve ser confirmado ao pedalar.

### 4. Iniciar jogo e ponte

**Terminal 1 — jogo:**

```bash
gradle :lwjgl3:run --args="--iot"
```

**Terminal 2 — sensor:**

```bash
.venv-ble/bin/python tools/ble_cadence.py bridge --device "UUID-RETORNADO-PELO-SCAN"
```

A ponte transmite mensagens `RPM:<valor>` apenas para `127.0.0.1:5055`, sem envio de dados à internet. Para encerrá-la, use `Ctrl+C`.

**Comportamento esperado:** a ponte calcula RPM a partir da diferença de rotações e dos tempos dos eventos BLE (resolução de 1/1024 s), envia atualizações quatro vezes por segundo e zera a cadência após três segundos sem novas rotações. O receptor do jogo também zera a leitura após dois segundos sem pacotes. Valores fora de 0–160 RPM são rejeitados. A primeira leitura estabelece a referência; é necessária outra rotação para calcular a cadência.

**Solução de problemas:** se o sensor não aparecer, gire os pedais, aproxime-o do Mac, feche conexões com outros aplicativos e repita o `scan`. Se a ponte desconectar, execute `bridge` novamente. A compatibilidade BLE precisa ser verificada com o dispositivo físico.

## Mecânicas de jogo

### Sessões, coleta e reciclagem

- Cada fase dura o tempo configurado para a sessão (**5 minutos por padrão**), independentemente da distância ou da quantidade de itens.
- A contagem inicial **3–2–1–START!** dura aproximadamente **3,7 segundos**. Movimento, pontuação, leitura de jogo e tempo de sessão começam depois dela.
- Os itens surgem aproximadamente a cada **1.600 pixels de deslocamento**; não aparecem enquanto o jogador está parado.
- A coleta ocorre automaticamente ao alcançar um item na mesma faixa. Uma lixeira compatível aparece **360 pixels de rolagem** depois do item e realiza a reciclagem automaticamente.
- Materiais e cores: **papel (azul), metal (amarelo), vidro (verde), plástico (vermelho) e pneus (cinza)**.
- Cada reciclagem correta concede **20 pontos** e **12 unidades de Energia Eco**.

### Energia Eco e Eco Turbo

Ao atingir 100 unidades de Energia Eco, a barra é consumida automaticamente, concede **100 pontos extras** e ativa o **Eco Turbo** por **5 segundos**. Enquanto o jogador pedala, a velocidade-alvo recebe multiplicador de **1,5×**. Colisões ainda podem desacelerar o personagem. O Turbo utiliza uma animação própria de quatro quadros; a pausa congela a contagem e a animação.

### Trânsito e dificuldade

- Quatro faixas de trânsito ficam disponíveis em todas as fases.
- Sedãs, SUVs e táxis animados circulam em sentidos distintos conforme a faixa.
- Os intervalos de surgimento de veículos variam de **8 segundos na fase 1** a **5 segundos na fase 4**.
- Um aviso **ATENÇÃO / VEÍCULO À FRENTE** aparece **1,5 segundo** antes da entrada do carro.
- Colisões reduzem temporariamente a velocidade e concedem **3 segundos de imunidade**; não retiram pontos nem encerram a partida.
- Buracos na pista ainda não foram implementados, pois dependem dos recursos gráficos correspondentes.

> O protótipo descrito contém **quatro fases/cenários**. Caso o artigo acadêmico descreva apenas três, alinhe a quantidade e os nomes das fases entre o texto e a versão entregue.

### Interface, animação e áudio

- O HUD **Fuji Blossom** apresenta nível, pontos, resíduos reciclados, RPM, ritmo e uma barra de Energia Eco com dez segmentos.
- O personagem usa animação de pedalada de seis quadros, comemoração de oito quadros após reciclagem e Turbo de quatro quadros.
- Há efeitos de mudança de faixa e colisão, além de brilho nas lixeiras e indicadores de pontuação após reciclagem.
- Três ninjas ciclistas decorativos aparecem ao fundo; não interagem com obstáculos, itens ou pontuação.
- A fase **Fuji Day** utiliza seis camadas com velocidades diferentes de paralaxe.
- Trilha do menu: `assets/audio/mainsoundtrack.mp3` (50%); trilha da corrida: `menuSound.mp3` (80%). Cliques, coleta e reciclagem usam efeitos específicos. Os volumes são relativos, não valores em dB.
- Música, animações e cronômetros são pausados com a partida; reiniciar restaura o estado inicial.

## Testes

Testes de regressão da jogabilidade:

```bash
gradle :core:gameplayRegressionTest
```

Também são incluídos em:

```bash
gradle :core:check
```

Os testes cobrem duração de fases, coleta, espaçamento de itens, ordem de reciclagem automática, recursos de efeitos e outros comportamentos de jogabilidade.

Testes da ponte BLE **sem hardware**:

```bash
PYTHONDONTWRITEBYTECODE=1 python3 -m unittest discover -s tools -p 'test_*.py'
```

Esses testes verificam cálculos de RPM, parada/retomada, retorno dos contadores a zero e tratamento de dados inválidos. **Eles não substituem testes com o sensor físico.**

## Limitações e próximos passos

- Confirmar a comunicação BLE real com o modelo de sensor utilizado.
- Definir e implementar a estratégia de troca de faixa compatível com os requisitos de controle por IoT, se necessário.
- Alinhar fases, métricas e arquitetura de integração com a documentação acadêmica.
- Persistir perfil e histórico de sessões, caso isso faça parte do escopo final.
- Refinar sprites e camadas de cenário: alguns recursos ainda possuem elementos incorporados às imagens.
- Validar qualquer uso em reabilitação com profissionais qualificados antes de considerar aplicação clínica.

## Equipe

- Erik Yutaka Takara
- Fernando Seong Jae Yoo
- Guilherme Silva Dias

## Repositório

Este README acompanha o código-fonte. Para entregas acadêmicas, inclua a URL do repositório e as instruções de acesso no artigo ou na documentação de submissão.
