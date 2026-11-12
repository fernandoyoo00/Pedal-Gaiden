# Pedal Gaiden — libGDX playable UI prototype

## Trilha sonora

O menu e o fluxo de perfil tocam `assets/audio/mainsoundtrack.mp3` em loop a 50%. A música da corrida, `menuSound.mp3`, começa já na contagem regressiva a 80% e continua sem reiniciar na largada. O som ambiente de trânsito foi removido da reprodução e não é carregado; o arquivo original é preservado. São volumes relativos, não níveis físicos em dB. A música pausa junto com a partida e retoma sem reiniciar. Reiniciar a corrida reinicia sua faixa; sair ou concluir volta à música de menu.

Cliques usam `clickMenu.mp3` a 60%; coletar um item usa `collectCollectible.mp3` a 70%; uma reciclagem correta usa `binCollected.mp3` a 80%.

Ao selecionar **Pedalar**, a pista e o personagem aparecem congelados durante a contagem 3–2–1 e a imagem **START!**, de `assets/gameplay/countdown/countdown_start_green.png` (3,7 segundos no total). A imagem substitui o texto “PEDALE!” e mantém suas proporções, centralizada na tela. O escurecimento é transparente: o cenário permanece visível. Movimento, sensor, pontuação e tempo da sessão só são atualizados depois da contagem. Pausar também congela a contagem; reiniciar começa novamente em 3.

O perfil, objetivos, experiência e confirmação usam o fundo da seleção de fases e o painel pixel art. Valores, rótulos e setas dos campos são centralizados verticalmente. O menu contém **Jogar**, **Instruções**, **Configurações** e **Créditos**; o perfil continua acessível por **Jogar**, sem opção duplicada. O rodapé mostra Fernando Yoo e Guilherme Dias na primeira linha, com Erik Yutaka centralizado abaixo.

## Idioma do jogo

A interface do jogo está em **português brasileiro (pt-BR)**: menu principal, perfil, objetivos, experiência, confirmação, seleção de fases, instruções, painel de status, controles, avisos, pausa e resultados. Os nomes de fases e personagens exibidos também estão traduzidos. Números decimais usam vírgula e a fonte padrão suporta os acentos do português. Os botões e rótulos longos ajustam o tamanho do texto ao espaço disponível.

As imagens com títulos em inglês na seleção de fases e no aviso de trânsito foram substituídas por texto em português renderizado pelo jogo, sem alterar os arquivos de arte. Nomes de arquivos, identificadores internos e o comando de integração `RPM:65` permanecem inalterados.

Requires JDK 17+ and Gradle. In this folder:

```bash
gradle :lwjgl3:run
```

The project includes a clickable illustrated main menu; selecting **Jogar** opens the player profile survey, followed by goals, experience, confirmation, and level selection. The game also includes four stage backgrounds, gameplay HUD, pause, and results. The default name is **Jogador 1** and can be edited; survey fields are not saved between launches. The game runs with existing assets from the EcoNinja starter package and does not depend on the composite UI reference image.

Development controls: hold **SPACE** or **UP** to pedal; hold **SHIFT** while pedalling to boost; click buttons for navigation; **P/ESC** pause. Collectibles are picked up automatically when reached. Each item has a matching bin 360 scrolling pixels behind it on the path; riding past that bin automatically recycles the item. Bin colors are blue for paper, yellow for metal cans, green for glass, red for plastic, and gray for tires. A deposit earns 20 points and fills Eco Energy. Full Eco Energy awards a 100-point bonus. Bin sparkles and point popups mark successful deposits; no bin clicking is needed. Collectibles spawn every 1,600 scrolling pixels (about every 6 seconds at normal speed), rather than every 1.4 seconds, and do not spawn while stopped. The scoring and pace are game-only values, not clinical rehabilitation metrics.

Each stage lasts the full configured session duration (5 minutes by default), regardless of distance or item count. The HUD shows the remaining time. Pausing freezes the session timer, motion, and effects; restarting clears the queue and score.

Each successful recycle plays the new green recycling effect at its bin, alongside the existing cheer, sparkles, and points. Every roadside bin has a Japanese Recycling Pavilion behind it; pavilion and bin scroll together. The Fuji Blossom status frame replaces the blue rectangular HUD, with live level, score, recycled count, cadence, rhythm, and a ten-segment Eco meter. The compact frame is centered at 1,000 pixels wide (20% smaller), preserves the artwork's aspect ratio, and uses fully backed panel windows with aligned text. The outlined energy segments show a numeric charge percentage or a gold turbo countdown.

Recycling adds 12 Eco Energy. Reaching 100 automatically consumes the meter, preserves the +100 bonus, and activates Eco Turbo for 5 seconds at 1.5 times the cadence-driven target speed, only while pedalling. Collisions still slow the rider. During turbo, the selected character loops its four `characters/boostAnimation/<ninja>/<ninja>_boost_00.png` through `_03.png` frames at 10 fps and the HUD shows a gold countdown meter; boost takes priority over cheering. Frames retain a shared canvas to avoid jitter. Turbo and its animation freeze while paused; turbo expires after five unpaused seconds even if pedalling stops. Restarting clears charge, turbo, and animation progress.

Path bins always sit along the far road edge and use a 64-by-92 layout. Collectibles fit proportionally inside 48-by-48 pixels, without stretching, upscaling beyond native resolution, or added glow. Nearest-neighbor filtering preserves the supplied pixel art; source PNGs (including any baked-in backgrounds) are left untouched. All four lanes are visible and usable in every stage, without a dark inactive-lane overlay. After a successful recycle, the selected ninja plays its eight-frame cheering animation for 1.2 seconds before returning to pedalling. Cheering preserves the sprite's proportions, freezes while paused, and resets on restart.

The main menu displays the scenery and logo without the decorative cyclist. Collectible pickups no longer trigger a burst or glow. New `powerEffects` artwork provides brief, fading feedback on the rider: `01_lane_change_blue` for a successful lane change and `04_car_collision` for traffic contact. These effects preserve their proportions, freeze while paused, and clear on restart; they do not alter scoring or physics. Recycling cheers, bin sparkles, and score popups remain unchanged. The removed `burst_1` through `burst_8` assets are no longer required.

Run gameplay regression checks with `gradle :core:gameplayRegressionTest` (also included in `:core:check`). These cover stage-duration thresholds, pickup crossings, collectible spacing and frequency, automatic path-bin recycling order, and bin/effect asset loading.

During a ride, the scenery scrolls with speed and the road moves with distance. Fuji Day uses the six `stage1fuji_layers` assets with separate parallax speeds from sky to road. Transparent vertical padding is cropped at load time; the artwork retains its aspect ratio. Sky fills the scene behind overlapping, ground-aligned mountains, forest, trees, and guardrail. The road fills the four-lane area using its own painted markings. Other stages retain their scrolling backgrounds. Layers alternate with mirrored copies for continuous scrolling. Riding uses the new six-frame `characters/ninja_sprites` cycle driven by cadence, with a subtle bounce; motion slows to a stop when pedalling stops and freezes while paused.

The elevated road shows four usable lanes in every stage, with dashed white separators and a double yellow center line. **W/S** or **RIGHT/LEFT** change lanes. On-screen **FAIXA -** and **FAIXA +** buttons also work while bike cadence is supplied through UDP; no new bike commands are required. Align with a collectible's lane to pick it up. Its following bin stays at the far road edge and automatically recycles from any lane. A bin recycles the first carried item of its matching material, so missing an earlier bin does not block other recycling.

Animated sedans, SUVs, and taxis use `obstacles/cars` artwork. Near-side traffic travels right and far-side traffic travels left, with varied speeds relative to the rider. Traffic spawns in usable lanes every 8 seconds in Level 1, dropping to 5 seconds in Level 4. Change lanes to avoid cars. Contact briefly reduces riding speed, highlights the ninja, and grants 3 seconds of immunity, with no point loss or game over. Traffic and collision effects freeze while paused and clear on restart. Potholes remain deferred until their artwork is supplied.

The Portuguese **ATENÇÃO / VEÍCULO À FRENTE** warning announces traffic 1.5 seconds before it spawns and remains visible while cars enter from the right. Each warning is 144 by 44 pixels, centered within the affected lane at the right road edge. Rendered text replaces the English warning artwork. The upcoming lane is selected before the advance warning and retained for the car's spawn. Warnings freeze with the traffic state while paused.

Three friendly ninja cyclists form part of the background scenery, using the three characters not selected by the player and the existing six-frame cycling sprites. They ride along the far road edge at 40–48 pixels tall (less than or equal to half the player's height), with a muted tint and subtle bounce. Their sprites face right, matching their rightward travel across the scene at varied speeds; they leave the right edge and reappear later from the left. Their base speed is reduced to 25–49 pixels per second plus 40% of scene movement, and they wait 8–17 seconds off-screen before returning, reducing how frequently they appear. They continue cycling when the player stops, like moving traffic. They render behind traffic, collectibles, bins, and the player rather than occupying gameplay lanes. They never collide, take collectibles, use bins, or change scoring. Their motion and animation freeze while paused, and restarting restores all three to their starting positions. Gameplay regression checks cover their count, character selection, background placement and size, screen exits and reentry, animation, and reset state.

## Sensor XOSS via BLE (macOS)

O modo `--iot` agora e preservado ao entrar pelo menu, consultar instrucoes e retornar ao menu. A ponte Python roda separadamente e envia a cadencia somente para `127.0.0.1:5055`; nenhum dado e enviado para a internet.

1. Configure **Cadence**, nao **Speed**, no aplicativo XOSS e confirme a leitura em RPM. Desconecte o aplicativo antes de usar a ponte. Pedale para acordar o sensor.
2. No terminal, na raiz do projeto, prepare um ambiente isolado:

   ```bash
   python3 -m venv .venv-ble
   .venv-ble/bin/python -m pip install -r tools/requirements.txt
   .venv-ble/bin/python tools/ble_cadence.py scan
   ```

3. O comando lista candidatos pelo nome XOSS ou pelo servico BLE CSC (`1816`). No macOS o ID e um **UUID**, nao um endereco MAC. Nome/servico anunciado identifica apenas um candidato; confirme os servicos conectando ao ID exato:

   ```bash
   .venv-ble/bin/python tools/ble_cadence.py inspect --device "UUID-RETORNADO-PELO-SCAN"
   ```

   A ponte verifica `CSC Measurement` (`2A5B`, notificacoes) e, quando disponivel, `CSC Feature` (`2A5C`) para confirmar suporte a pedivela. Um sensor configurado somente para velocidade nao fornece cadencia. A leitura real de dados de pedivela durante `bridge` confirma o funcionamento.

4. Inicie o jogo em modo sensor e, em outro terminal, a ponte:

   ```bash
   gradle :lwjgl3:run --args="--iot"
   ```

   ```bash
   .venv-ble/bin/python tools/ble_cadence.py bridge --device "UUID-RETORNADO-PELO-SCAN"
   ```

   A cadencia aparece no terminal e no jogo. Trocas de faixa continuam nos botoes da tela ou teclado. Para encerrar a ponte, pressione **Ctrl+C**.

Permita Bluetooth ao terminal/aplicativo que executa Python em **Ajustes do Sistema > Privacidade e Seguranca > Bluetooth**, quando solicitado. Se nada aparecer, acorde o sensor, aproxime-o do Mac, desconecte o XOSS de outros aplicativos e repita o scan. A ponte nao seleciona nem conecta automaticamente a dispositivos desconhecidos.

O calculo usa a diferenca de rotacoes e de tempo BLE (1/1024 s), incluindo o retorno dos contadores de 16 bits a zero. Valores fora de 0–160 RPM sao rejeitados com erro explicito. A primeira amostra estabelece a referencia; outra rotacao e necessaria para calcular RPM. A ponte envia `RPM:<valor>` quatro vezes por segundo, envia zero apos tres segundos sem novas rotacoes e envia zero ao encerrar/desconectar. A perda da ponte tambem faz o receptor do jogo zerar apos dois segundos sem pacotes; a desaceleracao visual continua suavizada. Desconexoes sao exibidas como erro: execute `bridge` novamente para reconectar.

Testes sem hardware: `PYTHONDONTWRITEBYTECODE=1 python3 -m unittest discover -s tools -p 'test_*.py'`. Eles cobrem RPM, parada, retomada, contadores, mensagens combinadas de roda/pedivela e dados invalidos. A compatibilidade BLE real depende dos servicos oferecidos pelo sensor e precisa ser verificada no dispositivo; nao ha validacao clinica.

The provided artwork contains some baked-in text/character artifacts; production sprites and seamless background layers should be prepared separately. The app is a functional visual prototype, not a pixel-perfect recreation of the reference mockup. Progression and survey settings need clinician-approved calibration before any rehabilitation use.
# Pedal-Gaiden
