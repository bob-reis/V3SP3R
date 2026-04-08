# Momentum Firmware — Manual Completo de Uso e Recursos

**Versão de Referência:** Momentum Firmware (baseado no Unleashed, fork do OFW)  
**Dispositivo:** Flipper Zero  
**Data:** Abril 2026  

---

## Sumário

1. [O que é o Momentum Firmware](#1-o-que-é-o-momentum-firmware)
2. [Instalação e Atualização](#2-instalação-e-atualização)
3. [Sistema de Configurações](#3-sistema-de-configurações)
4. [Asset Packs — Temas Visuais](#4-asset-packs--temas-visuais)
5. [RGB Backlight](#5-rgb-backlight)
6. [Menu e Navegação](#6-menu-e-navegação)
7. [Sub-GHz — Recursos Avançados](#7-sub-ghz--recursos-avançados)
8. [Infravermelho (IR)](#8-infravermelho-ir)
9. [NFC](#9-nfc)
10. [RFID / iButton](#10-rfid--ibutton)
11. [Bluetooth](#11-bluetooth)
12. [GPIO e I2C](#12-gpio-e-i2c)
13. [JavaScript Engine](#13-javascript-engine)
14. [Sistema Dolphin](#14-sistema-dolphin)
15. [CLI — Interface de Linha de Comando](#15-cli--interface-de-linha-de-comando)
16. [Buzzer e Sons](#16-buzzer-e-sons)
17. [Loader e Aplicativos](#17-loader-e-aplicativos)
18. [Keybinds (Atalhos de Teclado)](#18-keybinds-atalhos-de-teclado)
19. [Spoofing de Identidade do Dispositivo](#19-spoofing-de-identidade-do-dispositivo)
20. [Diferenças em Relação ao Firmware Original](#20-diferenças-em-relação-ao-firmware-original)
21. [Referência de Caminhos de Arquivos](#21-referência-de-caminhos-de-arquivos)
22. [Solução de Problemas](#22-solução-de-problemas)

---

## 1. O que é o Momentum Firmware

O Momentum é um firmware alternativo para o Flipper Zero, derivado do **Unleashed Firmware**, que por sua vez é um fork do firmware oficial (OFW). O Momentum foca em:

- **Customização visual profunda** via Asset Packs e RGB Backlight
- **Expansão de protocolos** Sub-GHz, IR e NFC além dos limites do OFW
- **JavaScript Engine** integrada para scripts e automações
- **Sistema de configurações unificado** em arquivo texto
- **Remoção de restrições regionais** de frequências de rádio
- **Keybinds configuráveis** para acesso rápido

### Diferença entre Momentum, Unleashed e OFW

| Recurso | OFW | Unleashed | Momentum |
|---|---|---|---|
| Protocolos Sub-GHz extras | Não | Sim | Sim + mais |
| JavaScript Engine | Não | Não | Sim |
| Asset Packs | Não | Parcial | Sim (completo) |
| RGB Backlight | Não | Não | Sim |
| Configurações em arquivo | Não | Parcial | Sim (unificado) |
| Menu estilo Dolhin | Sim | Sim | Alternativo |
| Frequências sem restrição | Não | Sim | Sim |
| Keybinds customizáveis | Não | Não | Sim |

---

## 2. Instalação e Atualização

### Método 1 — qFlipper (Recomendado)

1. Baixe o `.tgz` do Momentum em: `https://github.com/Next-Flip/Momentum-Firmware/releases`
2. Abra o qFlipper no computador
3. Vá em **Install from file** e selecione o `.tgz`
4. Aguarde a instalação completa (~2-3 minutos)
5. O Flipper reiniciará automaticamente

### Método 2 — Cartão SD Manual

1. Baixe o `.zip` do release do Momentum
2. Extraia na raiz do cartão SD (substitua todos os arquivos)
3. Copie o arquivo `.dfu` para a pasta raiz do SD
4. No Flipper, vá em **Settings > Update** e instale o firmware local

### Método 3 — Web Updater

Acesse `momentum-firmware.github.io` pelo browser no computador com o Flipper conectado via USB.

### Verificando a Versão

No Flipper: **Settings > About > Firmware Version**

Ou via CLI:
```
version
```

---

## 3. Sistema de Configurações

O Momentum armazena suas configurações em `/ext/momentum/settings` no cartão SD.

### Formato do Arquivo

```
Key=Value
Key2=Value2
```

Linhas começando com `#` são comentários. As mudanças exigem **reinicialização** do Flipper para entrar em vigor.

### Configurações Disponíveis

#### Interface Visual

| Chave | Valores | Descrição |
|---|---|---|
| `AssetPack` | Nome da pasta | Pack de assets ativo |
| `WelcomeAnim` | `true` / `false` | Animação de boas-vindas na inicialização |
| `BatteryDisplay` | `Percentage` / `Icon` / `Off` | Exibição da bateria |
| `StatusBarFullscreen` | `true` / `false` | Status bar em tela cheia |

#### Comportamento

| Chave | Valores | Descrição |
|---|---|---|
| `LockOnBoot` | `true` / `false` | Tela de bloqueio ao iniciar |
| `MenuStyle` | `List` / `Dolphin` / `Compact` | Estilo do menu principal |
| `FavoritesTimeout` | `0` a `30` (segundos) | Timeout para favoritos |
| `XpLevel` | Número | Nível de XP do Dolphin |

#### Sub-GHz

| Chave | Valores | Descrição |
|---|---|---|
| `SubGhzFreqMax` | Frequência em Hz | Frequência máxima permitida |
| `SubGhzFreqMin` | Frequência em Hz | Frequência mínima permitida |
| `SubGhzExtendBands` | `true` / `false` | Habilitar bandas estendidas |

#### RGB Backlight

| Chave | Valores | Descrição |
|---|---|---|
| `RGBBacklight` | `true` / `false` | Habilitar RGB |
| `RGBPreset` | Nome do preset | Preset de cor ativo |
| `RGBBrightness` | `0` a `255` | Brilho do RGB |

### Editando Configurações

**Via V3SP3R:**
```
Configure: salve o parâmetro [nome_da_chave] com o valor [valor]
```

**Via CLI:**
```bash
# Ler configuração atual
storage read /ext/momentum/settings

# Escrever nova configuração
storage write /ext/momentum/settings
```

---

## 4. Asset Packs — Temas Visuais

Asset Packs são temas completos que alteram animações, ícones e a aparência visual do Flipper Zero.

### Estrutura de um Asset Pack

```
/ext/asset_packs/
└── MeuTheme/
    ├── Anims/           # Animações do Dolphin
    │   ├── manifest.txt
    │   └── *.fam        # Arquivos de animação
    ├── Icons/           # Ícones do sistema
    │   ├── MainMenu/
    │   ├── Passport/
    │   └── ...
    └── Fonts/           # Fontes (opcional)
```

### Instalando um Asset Pack

1. Copie a pasta do pack para `/ext/asset_packs/` no SD
2. A pasta deve conter pelo menos `Anims/` ou `Icons/`
3. No Flipper: **Settings > Momentum > Asset Pack** → selecione o pack
4. Reinicie o dispositivo

**Via V3SP3R:**
```
Liste os asset packs disponíveis
Mude o asset pack para [nome do pack]
```

### Asset Packs Populares

| Pack | Estilo | Link |
|---|---|---|
| Momentum Default | Moderno, minimal | Incluso no firmware |
| Xtreme | Cyberpunk | Repositório Xtreme |
| DeepSky | Espaço | Comunidade |
| Retro | 8-bit clássico | Comunidade |

### Criando um Asset Pack Personalizado

1. Use o Flipper Toolbox ou Animation Manager
2. Crie animações no formato `.fam` (Frame Animation Metadata)
3. Exporte ícones em BMP 1-bit (preto e branco)
4. Organize na estrutura de pastas acima
5. Adicione o `manifest.txt` em `Anims/`

**Formato do manifest.txt:**
```
Filetype: Flipper Animation Manifest
Version: 1

Name: AnimName
Min butthurt: 0
Max butthurt: 14
Min level: 1
Max level: 30
Weight: 3
```

---

## 5. RGB Backlight

O Momentum suporta retroiluminação RGB no hardware com o módulo de backlight RGB (disponível como mod de hardware ou em versões especiais do Flipper).

### Habilitando o RGB

**No Flipper:** Settings > Momentum > RGB Backlight > Enable

**No arquivo de configurações:**
```
RGBBacklight=true
RGBPreset=Rainbow
```

### Presets Disponíveis

| Preset | Descrição |
|---|---|
| `Rainbow` | Ciclo completo de cores |
| `RedBlue` | Alternância vermelho/azul |
| `CyberPunk` | Roxo e cyan |
| `SolarFlare` | Laranja e amarelo |
| `Arctic` | Azul gelo |
| `Stealth` | Branco frio muito baixo |
| `Off` | Desligado |

### Via V3SP3R

```
Ative o backlight RGB com o preset Cyberpunk
Configure o preset de RGB para Rainbow
```

---

## 6. Menu e Navegação

### Estilos de Menu

O Momentum oferece três estilos diferentes de menu principal:

#### List (Padrão)
Menu em lista vertical, similar ao OFW. Navegação com cima/baixo, OK para entrar.

#### Dolphin
Menu animado com o golfinho se movendo. Visual mais dinâmico.

#### Compact
Menu compacto com mais itens visíveis na tela simultaneamente.

**Alterando via CLI:**
```
momentum_setting MenuStyle List
momentum_setting MenuStyle Dolphin  
momentum_setting MenuStyle Compact
```

### Navegação Rápida (Keybinds)

O Momentum permite configurar ações para botões pressionados na tela de desktop:

- **Botão Esquerdo**: abre aplicativo configurado
- **Botão Direito**: abre aplicativo configurado  
- **Botão Cima**: abre aplicativo configurado
- **OK Longo**: abre Passport ou ação customizada

Ver seção [18. Keybinds](#18-keybinds-atalhos-de-teclado) para detalhes.

---

## 7. Sub-GHz — Recursos Avançados

O Momentum expande significativamente as capacidades Sub-GHz do Flipper.

### Frequências Suportadas

Sem as restrições regionais do OFW, o Momentum suporta:

| Banda | Frequência | Uso Típico |
|---|---|---|
| ISM 300 MHz | 300–348 MHz | Controles de garagem EUA |
| ISM 315 MHz | 315 MHz | Controles remotos americanos |
| ISM 433 MHz | 433.05–434.79 MHz | Padrão europeu/global |
| ISM 868 MHz | 868 MHz | LoRa, Europa |
| ISM 915 MHz | 902–928 MHz | LoRa, EUA |

### Protocolos Extras (vs OFW)

O Momentum inclui protocolos adicionais que não estão no firmware original:

#### Protocolos de Portão/Garagem
- **Nice FLO** — portões italianos Nice
- **Came** — portões Came
- **Faac** — portões Faac
- **BFT** — portões BFT
- **Doorhan** — portões Doorhan (Rússia)
- **Alecto** — estações meteorológicas

#### Protocolos de Automação
- **Somfy** — automação residencial
- **Hormann** — portões industriais Hormann
- **Linear** — controles Linear/Multicode

#### Protocolos de Segurança (Educacional)
- **Keeloq** — rolling code (análise)
- **Microchip HCS** — análise de sequência

### Recepção Sub-GHz

**Via CLI:**
```bash
# Modo de recepção em frequência específica
subghz rx 433920000

# Decodificar arquivo raw capturado
subghz decode_raw /ext/subghz/MinhaCaptura.sub

# Chat Sub-GHz (comunicação entre Flippers)
subghz chat 433920000
```

**Via V3SP3R:**
```
Inicie a recepção Sub-GHz em 433.92 MHz
Decodifique o arquivo /ext/subghz/captura.sub
```

### Transmissão

**Via CLI:**
```bash
# Transmitir arquivo .sub gravado
subghz tx_from_file /ext/subghz/MinhaCaptura.sub
```

### Formato de Arquivo .sub

```
Filetype: Flipper SubGhz Key File
Version: 1
Frequency: 433920000
Preset: FuriHalSubGhzPresetOok650Async
Protocol: NiceFlo
Bit: 24
Key: 0x00AABBCC
```

Para raw captures:
```
Filetype: Flipper SubGhz RAW File
Version: 1
Frequency: 433920000
Preset: FuriHalSubGhzPresetOok650Async
Protocol: RAW
RAW_Data: 100 -200 300 -100 ...
```

---

## 8. Infravermelho (IR)

### Recursos Extras no Momentum

O Momentum expande a base de dados de controles IR e adiciona funcionalidades:

- **Biblioteca Universal** ampliada com mais marcas e modelos
- **Recepção de sinais** IR desconhecidos
- **Controles Universais** para TV, AC, Projetores etc.

### Recepção IR

**Via CLI:**
```bash
# Iniciar recepção de sinal IR
ir rx
```

O Flipper ficará aguardando um sinal IR e exibirá os dados decodificados.

**Via V3SP3R:**
```
Inicie a recepção de sinal infravermelho
Receba um sinal IR
```

### Controle Universal IR

O Momentum inclui uma grande biblioteca de controles universais:

**Via CLI:**
```bash
# Abrir controle universal por categoria
ir universal tv
ir universal ac
ir universal projector
ir universal audio
```

**Via V3SP3R:**
```
Abra o controle universal de IR para TV
Use o IR universal para ar-condicionado
```

### Formato de Arquivo .ir

```
Filetype: IR signals file
Version: 1
# 
name: Power
type: parsed
protocol: NEC
address: 04 00 00 00
command: 08 00 00 00
# 
name: VolumeUp
type: parsed
protocol: NEC
address: 04 00 00 00
command: 02 00 00 00
```

---

## 9. NFC

### Recursos Expandidos no Momentum

- **NFC Scanner** avançado detectando mais tipos de cartão
- **NFC Field Detector** — detecta campos NFC ativos próximos
- **NFC APDU** — envio manual de comandos APDU
- **Emulação** de mais tipos de cartões

### Tipos de Cartão Suportados

| Tipo | Padrão | Capacidade |
|---|---|---|
| Mifare Classic 1K/4K | ISO 14443-A | Leitura + emulação |
| Mifare Ultralight | ISO 14443-A | Leitura + escrita |
| Mifare DESFire | ISO 14443-A | Leitura parcial |
| NTAG213/215/216 | ISO 14443-A | Leitura + escrita + emulação |
| EMV (cartões de crédito) | ISO 14443-A | Leitura de dados públicos |
| FeliCa | ISO 18092 | Leitura básica |
| ISO 15693 | ISO 15693 | Leitura básica |

### Comandos NFC

**Via CLI:**
```bash
# Scanner NFC — detectar e ler cartão próximo
nfc scanner

# Detector de campo NFC
nfc field

# Dump de cartão para arquivo
nfc dump /ext/nfc/meu_cartao.nfc

# Enviar APDU raw
nfc apdu 00A4040007A0000000041010
```

**Via V3SP3R:**
```
Escaneie um cartão NFC
Detecte campos NFC próximos
Faça dump do cartão NFC para arquivo
Envie o APDU 00A4040007A0000000041010
```

### Formato de Arquivo .nfc

```
Filetype: Flipper NFC device
Version: 4
Device type: Mifare Classic
# Device UID
UID: AA BB CC DD
ATQA: 00 04
SAK: 08
```

---

## 10. RFID / iButton

### RFID (125 kHz)

O Momentum mantém suporte completo a RFID de baixa frequência:

**Protocolos suportados:**
- EM4100 (mais comum)
- HID Prox
- Indala
- Paradox
- Viking
- Keri
- Gallagher

**Via CLI:**
```bash
# Ler cartão RFID
rfid read

# Emular cartão salvo
rfid emulate /ext/lfrfid/meu_cartao.rfid

# Escrever em cartão virgem
rfid write /ext/lfrfid/meu_cartao.rfid
```

### iButton (Dallas/Maxim)

**Chaves suportadas:**
- DS1990A (mais comum)
- DS1992, DS1993, DS1994, DS1995, DS1996
- Cyfral (formato russo)
- Metakom (formato russo)

**Via CLI:**
```bash
# Ler chave iButton
ikey read

# Emular chave
ikey emulate /ext/ibutton/minha_chave.ibtn
```

---

## 11. Bluetooth

### Recursos BLE no Momentum

O Momentum mantém e expande os recursos BLE do Flipper:

#### Modo Periférico (HID)
O Flipper se comporta como dispositivo HID Bluetooth:
- Teclado wireless
- Mouse wireless
- Gamepad

#### Modo Servidor BLE (RPC)
Comunicação com apps como V3SP3R via protocolo RPC/Protobuf.

#### Recursos Extras

**Via CLI:**
```bash
# Informações do adaptador BLE
bt hci_info

# Transmitir carrier BLE (teste de RF)
bt carrier

# Modo de pacote BLE (teste)
bt packet
```

### Emparelhamento com V3SP3R

1. No Flipper: Settings > Bluetooth > Enable
2. No V3SP3R: toque em "Conectar Flipper"
3. Selecione seu Flipper na lista de dispositivos
4. Aceite o PIN no Flipper (se solicitado)
5. Conexão estabelecida — todas as funções ficam disponíveis

---

## 12. GPIO e I2C

### GPIO (General Purpose Input/Output)

O Flipper Zero possui pinos GPIO expostos no conector de 18 pinos.

#### Pinout do Conector GPIO

```
     [   Flipper Zero — Conector GPIO   ]
     
PA7  [ 1]  [ 2] GND
PA6  [ 3]  [ 4] GND  
PA4  [ 5]  [ 6] 3.3V
PB3  [ 7]  [ 8] PB2 (BOOT1)
PB2  [ 9]  [10] PC3
PA14 [11]  [12] PC1
PA13 [13]  [14] PC0
GND  [15]  [16] 1V8 (saída)
GND  [17]  [18] 5V USB
```

#### Comandos GPIO

**Via CLI:**
```bash
# Ler estado de um pino
gpio get PC3

# Definir estado de um pino (modo output)
gpio set PC3 1    # HIGH
gpio set PC3 0    # LOW

# Configurar modo do pino
gpio mode PC3 output
gpio mode PC3 input
gpio mode PC3 input_pull_up
gpio mode PC3 input_pull_down
```

**Via V3SP3R:**
```
Leia o estado do pino PC3
Defina o pino PC3 como HIGH
Configure o pino PA7 como saída
```

### I2C

Interface de comunicação serial de dois fios (SDA + SCL).

**Pinos I2C no Flipper:**
- SDA: PA14 (pino 11)
- SCL: PA13 (pino 13)

**Comandos I2C:**

**Via CLI:**
```bash
# Escanear dispositivos I2C conectados
i2c scan

# Ler registrador de dispositivo
i2c read 0x48 0x00    # addr=0x48, reg=0x00

# Escrever em registrador
i2c write 0x48 0x01 0xFF   # addr, reg, data
```

**Via V3SP3R:**
```
Escaneie dispositivos I2C
Leia o registrador 0x00 do dispositivo I2C 0x48
Escreva 0xFF no registrador 0x01 do dispositivo 0x48
```

---

## 13. JavaScript Engine

Uma das funcionalidades mais poderosas e exclusivas do Momentum é a engine JavaScript integrada, baseada no mJS.

### Executando Scripts

**Via CLI:**
```bash
js /ext/scripts/meu_script.js
```

**Via V3SP3R:**
```
Execute o script /ext/scripts/meu_script.js
Rode o JavaScript /ext/scripts/automacao.js
```

### Módulos JavaScript Disponíveis

#### `flipper` — Controle do Sistema
```javascript
let flipper = require("flipper");
flipper.setLed("blue", true);      // Acende LED azul
flipper.vibro(true);               // Liga vibração
let name = flipper.getName();      // Nome do dispositivo
let model = flipper.getModel();    // Modelo ("Flipper Zero")
let firmware = flipper.getFirmwareVersion(); // Versão firmware
let battery = flipper.getBatteryLevel();     // Nível bateria %
```

#### `subghz` — Sub-GHz
```javascript
let subghz = require("subghz");
subghz.setFreq(433920000);         // Define frequência
subghz.setup_presets(["AM650"]);   // Configura preset
subghz.rx(function(data) {        // Callback de recepção
    print(data);
});
```

#### `infrared` — Infravermelho
```javascript
let ir = require("infrared");
let signal = ir.rx();              // Recebe sinal IR
ir.tx("NEC", 0x04, 0x08);        // Transmite sinal NEC
```

#### `nfc` — NFC
```javascript
let nfc = require("nfc");
let card = nfc.read();             // Lê cartão NFC
print(card.uid);                   // Exibe UID
```

#### `ble` — Bluetooth LE
```javascript
let ble = require("ble");
ble.beacon({                       // Emite beacon BLE
    uuid: "1234",
    major: 1,
    minor: 1
});
```

#### `gpio` — GPIO
```javascript
let gpio = require("gpio");
gpio.init("PC3", "output", "none");
gpio.write("PC3", true);           // HIGH
gpio.write("PC3", false);          // LOW
let val = gpio.read("PA7");       // Lê pino
```

#### `serial` — Porta Serial
```javascript
let serial = require("serial");
serial.setup("usart1", 115200);
serial.write("Hello World\n");
let data = serial.readln(1000);   // Lê linha, timeout 1s
```

#### `storage` — Sistema de Arquivos
```javascript
let storage = require("storage");
storage.write("/ext/test.txt", "Conteúdo");
let content = storage.read("/ext/test.txt");
storage.delete("/ext/temp.txt");
let files = storage.readdir("/ext/scripts");
```

#### `dialog` — Diálogos de UI
```javascript
let dialog = require("dialog");
dialog.message("Título", "Mensagem");
let result = dialog.pickFile("/ext", ".js");
let choice = dialog.inputString("Digite:", "default");
```

#### `notification` — Notificações
```javascript
let notify = require("notification");
notify.success();    // LED verde + vibração curta
notify.error();      // LED vermelho + vibração
notify.blink("blue", 3);  // Pisca LED azul 3x
```

#### `keyboard` — Teclado Virtual
```javascript
let keyboard = require("keyboard");
let input = keyboard.text(30, "Digite aqui:", true);
let number = keyboard.number(0);
```

#### `math` — Funções Matemáticas
```javascript
let m = require("math");
let pi = m.PI;
let root = m.sqrt(16);    // 4
let angle = m.sin(m.PI / 2);  // 1
let rand = m.random(1, 100);  // 1-100
```

#### `crypto` — Criptografia
```javascript
let crypto = require("crypto");
let hash = crypto.md5("texto");
let hmac = crypto.hmac("sha256", "chave", "dados");
```

#### `usbdisk` — Armazenamento USB
```javascript
let usb = require("usbdisk");
usb.start("/ext/disk.img");  // Monta imagem como pen drive
usb.stop();
```

#### `badusb` — HID BadUSB
```javascript
let badusb = require("badusb");
badusb.setup({ vid: 0x046D, pid: 0xC31C, mfr: "Keyboard", product: "USB Keyboard" });
badusb.press("GUI", "r");       // Win+R
badusb.type("notepad\n");
badusb.release();
```

### Script de Exemplo — Automação Sub-GHz

```javascript
// Script: scan_and_log.js
// Escaneia Sub-GHz e registra sinais detectados

let subghz = require("subghz");
let storage = require("storage");
let notification = require("notification");
let dialog = require("dialog");

let freq = 433920000;
let logFile = "/ext/scripts/scan_log.txt";
let count = 0;

dialog.message("SubGHz Scanner", "Iniciando scan em 433.92 MHz");

subghz.setFreq(freq);
storage.write(logFile, "=== Scan Log ===\n");

// Escaneia por 10 segundos
let start = Date.now();
while (Date.now() - start < 10000) {
    let data = subghz.rx_raw(100);
    if (data) {
        count++;
        storage.append(logFile, "Sinal " + count + ": " + JSON.stringify(data) + "\n");
        notification.blink("blue", 1);
    }
}

notification.success();
dialog.message("Scan Concluído", "Detectados: " + count + " sinais\nLog: " + logFile);
```

### Localização dos Scripts

Coloque seus scripts JS em:
```
/ext/scripts/          # Scripts personalizados
/ext/apps/Scripts/     # Via App Manager
```

---

## 14. Sistema Dolphin

O Dolphin é o sistema de progresso e gamificação do Flipper Zero. O Momentum expande suas possibilidades.

### Níveis e XP

O Dolphin sobe de nível conforme você usa o Flipper:

| Ação | XP Ganho |
|---|---|
| Ler cartão RFID | +2 XP |
| Transmitir sinal Sub-GHz | +3 XP |
| Ler NFC | +2 XP |
| Usar BadUSB | +5 XP |
| Usar iButton | +2 XP |

### Comandos Dolphin

**Via CLI:**
```bash
# Ver estado atual do Dolphin
dolphin stats

# Adicionar XP manualmente
dolphin xp 100

# Resetar Dolphin (volta ao nível 1)
dolphin reset

# Sincronizar com servidor
dolphin flush
```

### Nome do Dispositivo

O nome exibido no Flipper é configurado em `/ext/dolphin/name.settings`:

```
Name: MeuFlipper
```

**Via V3SP3R:**
```
Mude o nome do dispositivo para "MeuFlipper"
Adicione 500 XP ao Dolphin
```

### Animações do Dolphin

O Dolphin exibe animações diferentes conforme:
- **Nível atual** (mais animações em níveis mais altos)
- **Mood** (humo baseado em quanto tempo sem uso)
- **Asset Pack ativo** (animações customizadas)

---

## 15. CLI — Interface de Linha de Comando

O Momentum expande o CLI do Flipper com novos comandos.

### Acessando o CLI

**Opção 1 — qFlipper:** Aba "Console" no qFlipper
**Opção 2 — USB Serial:** `screen /dev/ttyACM0 115200` (Linux/Mac)
**Opção 3 — V3SP3R:** Comandos via app (BLE/USB)

### Comandos do Sistema

```bash
help              # Lista todos os comandos
version           # Versão do firmware
date              # Data/hora atual
uptime            # Tempo ligado
free              # Memória disponível
top               # Processos ativos
ps                # Lista de threads
log               # Visualizar logs
neofetch          # Info do sistema estilo neofetch
```

### Comandos de Storage

```bash
storage list /ext                    # Listar arquivos
storage read /ext/arquivo.txt        # Ler arquivo
storage write /ext/arquivo.txt       # Escrever arquivo (stdin)
storage stat /ext/arquivo.txt        # Informações do arquivo
storage remove /ext/arquivo.txt      # Remover arquivo
storage mkdir /ext/nova_pasta        # Criar diretório
storage md5 /ext/arquivo.txt         # Hash MD5
storage copy /ext/orig.txt /ext/dest.txt  # Copiar
storage rename /ext/orig.txt /ext/novo.txt # Renomear
```

### Comandos Sub-GHz

```bash
subghz rx 433920000           # Receber em frequência
subghz rx_raw 433920000       # Receber sinal raw
subghz tx_from_file /ext/subghz/file.sub  # Transmitir arquivo
subghz decode_raw /ext/subghz/file.sub    # Decodificar raw
subghz chat 433920000         # Chat Sub-GHz
```

### Comandos IR

```bash
ir rx                          # Receber sinal IR
ir tx /ext/infrared/arquivo.ir PowerOn  # Transmitir sinal específico
ir universal tv                # Abrir controle universal TV
```

### Comandos NFC

```bash
nfc scanner               # Scanner NFC
nfc field                 # Detector de campo NFC
nfc dump /ext/nfc/cartao.nfc  # Dump de cartão
nfc apdu <hex>            # Enviar APDU
```

### Comandos RFID

```bash
rfid read                           # Ler cartão RFID
rfid write /ext/lfrfid/cartao.rfid  # Escrever cartão
rfid emulate /ext/lfrfid/cartao.rfid # Emular cartão
```

### Comandos iButton

```bash
ikey read                        # Ler chave iButton
ikey write /ext/ibutton/chave.ibtn  # Escrever chave
ikey emulate /ext/ibutton/chave.ibtn # Emular chave
```

### Comandos GPIO

```bash
gpio get <pin>                    # Ler pino
gpio set <pin> <0|1>             # Definir pino
gpio mode <pin> <mode>           # Configurar modo
```

Modos disponíveis: `output`, `input`, `input_pull_up`, `input_pull_down`

### Comandos I2C

```bash
i2c scan                         # Escanear dispositivos
i2c read <addr> <reg>           # Ler registrador
i2c write <addr> <reg> <data>   # Escrever registrador
```

### Comandos JavaScript

```bash
js /ext/scripts/meu_script.js   # Executar script JS
```

### Comandos do Loader

```bash
loader list                      # Listar aplicativos
loader open "Sub-GHz"           # Abrir aplicativo por nome
loader open "Sub-GHz" /ext/subghz/arquivo.sub  # Abrir com arquivo
loader close                     # Fechar aplicativo atual
loader info                      # Info do app atual
loader signal <id>               # Enviar sinal para app
```

### Comandos Bluetooth

```bash
bt hci_info        # Info do adaptador BLE
bt carrier         # Transmitir carrier BLE
bt packet          # Modo de pacote BLE
```

### Comandos de Input

```bash
input send <key> <type>    # Enviar evento de botão
input dump                 # Capturar próximo evento de botão
```

Keys: `up`, `down`, `right`, `left`, `ok`, `back`  
Types: `press`, `release`, `short`, `long`, `repeat`

### Comandos do Buzzer (Momentum Exclusivo)

```bash
buzzer note <nome_nota> <duração>ms    # Tocar nota por nome
buzzer freq <hz> <duração>ms           # Tocar frequência específica
```

Notas disponíveis: `A4`, `B4`, `C5`, `D5`, `E5`, `F5`, `G5`, etc.

```bash
# Exemplos:
buzzer note A4 500ms     # Lá por 500ms
buzzer note C5 250ms     # Dó por 250ms
buzzer freq 440 1000ms   # 440 Hz por 1 segundo
buzzer freq 1000 100ms   # 1kHz por 100ms
```

### Comandos Dolphin

```bash
dolphin stats      # Estatísticas do Dolphin
dolphin xp <n>     # Adicionar XP
dolphin reset      # Resetar progresso
dolphin flush      # Sincronizar
```

### Comandos do Momentum

```bash
# Configurações
momentum_setting <key> <value>    # Definir configuração

# Asset Packs
asset_pack list                   # Listar packs disponíveis
asset_pack set <nome>             # Definir pack ativo

# RGB
rgb_backlight <preset>            # Definir preset RGB
```

### Comandos de Energia/Power

```bash
power poweroff        # Desligar o Flipper
power reboot          # Reiniciar o Flipper
power reboot_dfu      # Reiniciar em modo DFU (atualização)
```

---

## 16. Buzzer e Sons

O Momentum adiciona controle direto do buzzer (speaker piezoelétrico) via CLI.

### Usando o Buzzer

```bash
# Por nome de nota
buzzer note C4 500ms    # Dó central, meio segundo
buzzer note A4 1000ms   # Lá 440Hz, um segundo
buzzer note G5 200ms    # Sol oitava, 200ms

# Por frequência
buzzer freq 440 500ms   # 440 Hz por 500ms
buzzer freq 1760 100ms  # 1760 Hz por 100ms
buzzer freq 262 1000ms  # C4 (261.63 Hz) por 1s
```

### Referência de Notas

| Nota | Frequência | Oitava 4 | Oitava 5 |
|---|---|---|---|
| C (Dó) | 261.63 Hz | C4 | C5 |
| D (Ré) | 293.66 Hz | D4 | D5 |
| E (Mi) | 329.63 Hz | E4 | E5 |
| F (Fá) | 349.23 Hz | F4 | F5 |
| G (Sol) | 392.00 Hz | G4 | G5 |
| A (Lá) | 440.00 Hz | A4 | A5 |
| B (Si) | 493.88 Hz | B4 | B5 |

**Via V3SP3R:**
```
Toque a nota A4 por 500 milissegundos
Toque a frequência 440 Hz por 1 segundo
```

---

## 17. Loader e Aplicativos

### Gerenciador de Aplicativos

O Momentum usa o Loader para gerenciar aplicativos instalados.

```bash
loader list          # Lista todos os apps instalados
loader open "Nome"   # Abre app por nome exato
loader close         # Fecha o app atual
loader info          # Mostra info do app rodando
```

### Aplicativos Exclusivos do Momentum

| App | Descrição | Localização no Menu |
|---|---|---|
| JavaScript Runner | Executa scripts .js | Apps > Scripts |
| Sub-GHz Chat | Chat peer-to-peer Sub-GHz | Sub-GHz > Chat |
| NFC Scanner | Scanner NFC avançado | NFC > Scanner |
| GPIO Control | Interface visual GPIO | GPIO |
| Momentum Settings | Configurações do firmware | Settings > Momentum |
| Asset Pack Manager | Gerenciar temas | Settings > Asset Pack |

### Aplicativos da Comunidade

O Flipper suporta apps `.fap` (Flipper Application Package) instalados em `/ext/apps/`.

**Categorias disponíveis:**
```
/ext/apps/Sub-GHz/      # Apps Sub-GHz
/ext/apps/NFC/          # Apps NFC
/ext/apps/Infrared/     # Apps IR
/ext/apps/GPIO/         # Apps GPIO
/ext/apps/Games/        # Jogos
/ext/apps/Tools/        # Utilitários
/ext/apps/Scripts/      # Scripts JS
/ext/apps/USB/          # Apps USB/BadUSB
/ext/apps/Bluetooth/    # Apps BLE
```

---

## 18. Keybinds (Atalhos de Teclado)

O Momentum permite configurar ações para botões pressionados no desktop (tela inicial).

### Configurando Keybinds

**Via Settings > Momentum > Keybinds:**

| Botão | Ação padrão | Configurável |
|---|---|---|
| Esquerdo | Menu | Sim |
| Direito | Passaporte | Sim |
| Cima | Menu Favoritos | Sim |
| OK (longo) | Passport | Sim |
| Back (longo) | Desligar/Bloquear | Sim |

### Exemplos de Configurações

```
# Botão esquerdo abre Sub-GHz
LeftKey=SubGHz

# Botão direito abre BadUSB
RightKey=BadUSB

# Botão cima abre scripts JS  
UpKey=JavaScript

# OK longo abre NFC
OkLongKey=NFC
```

---

## 19. Spoofing de Identidade do Dispositivo

O Momentum permite alterar o nome e aparência do Flipper no BLE e no sistema.

### Nome do Dispositivo BLE

O nome exibido quando o Flipper é listado por outros dispositivos via Bluetooth:

**Via arquivo:**
Edite `/ext/dolphin/name.settings`:
```
Name: MeuFlipper
```

**Via V3SP3R:**
```
Mude o nome do Flipper para "MeuFlipper"
```

Após salvar, reinicie o Flipper para aplicar.

### Efeito do Nome

- Aparece no scan BLE de outros dispositivos
- Exibido no Passaporte do Flipper
- Usado pelo Dolphin como identidade

---

## 20. Diferenças em Relação ao Firmware Original

### Recursos REMOVIDOS/LIMITADOS no OFW que existem no Momentum

| Recurso | OFW | Momentum |
|---|---|---|
| Frequências Sub-GHz | Limitadas por região | Todas desbloqueadas |
| Protocolo Keeloq (análise) | Não | Sim |
| JavaScript Engine | Não | Sim |
| Asset Packs | Não | Sim |
| RGB Backlight | Não | Sim |
| GPIO avançado (CLI) | Limitado | Completo |
| I2C via CLI | Não | Sim |
| Buzzer CLI | Não | Sim |
| Loader CLI completo | Não | Sim |
| Sub-GHz Chat | Não | Sim |
| NFC Field Detector | Não | Sim |
| Keybinds configuráveis | Não | Sim |
| Configurações em arquivo | Não | Sim |
| Dolphin XP manual | Não | Sim |

### Recursos do OFW mantidos no Momentum

- Todas as funcionalidades NFC básicas
- RFID completo
- iButton completo
- BadUSB
- Sub-GHz básico (ler/gravar/emular)
- IR básico (ler/gravar/transmitir)
- Sistema Dolphin
- Update OTA
- Conexão qFlipper
- BLE HID

### Considerações de Segurança

O Momentum desbloqueia capacidades que o firmware oficial limita por conformidade regulatória. Use com responsabilidade:

- **Sub-GHz**: Transmitir em frequências fora das bandas ISM pode violar regulamentações locais
- **Rolling codes**: A análise de sistemas de segurança deve ser feita apenas em sistemas próprios ou com autorização explícita
- **BadUSB**: Use apenas em dispositivos próprios ou com permissão do proprietário
- **NFC/RFID**: Leitura de cartões de terceiros sem autorização pode ser ilegal

---

## 21. Referência de Caminhos de Arquivos

```
/ext/                              # Raiz do cartão SD
├── subghz/                        # Capturas Sub-GHz (.sub)
├── infrared/                      # Sinais IR (.ir)
├── nfc/                           # Cartões NFC (.nfc)
├── lfrfid/                        # Cartões RFID (.rfid)
├── ibutton/                       # Chaves iButton (.ibtn)
├── badusb/                        # Scripts BadUSB (.txt)
├── scripts/                       # Scripts JavaScript (.js)
├── apps/                          # Aplicativos (.fap)
│   ├── Sub-GHz/
│   ├── NFC/
│   ├── Infrared/
│   ├── GPIO/
│   ├── Games/
│   ├── Tools/
│   ├── Scripts/
│   ├── USB/
│   └── Bluetooth/
├── asset_packs/                   # Temas visuais
│   └── NomeDoPack/
│       ├── Anims/
│       ├── Icons/
│       └── Fonts/
├── momentum/                      # Configurações do Momentum
│   └── settings                   # Arquivo de configuração
├── dolphin/                       # Dados do Dolphin
│   └── name.settings             # Nome do dispositivo
├── music_player/                  # Músicas (.fmf)
├── u2f/                           # Certificados U2F
└── update/                        # Updates de firmware
```

---

## 22. Solução de Problemas

### Flipper não conecta via BLE ao V3SP3R

1. Certifique-se de que o BLE está habilitado: Settings > Bluetooth > Enabled
2. Verifique se o Flipper não está emparelhado com outro dispositivo
3. No V3SP3R, vá em Configurações > Desconectar e tente novamente
4. Reinicie o Bluetooth no Flipper: Settings > Bluetooth > Off → On
5. Se persistir, faça "Unpair All Devices" no Flipper e reconecte

### Comandos Sub-GHz não funcionam

1. Verifique se a frequência é suportada pelo hardware (CC1101)
2. Para bandas acima de 868 MHz, certifique-se que `SubGhzExtendBands=true` nas configurações
3. Reinicie o Flipper após alterar configurações de Sub-GHz

### Asset Pack não carrega

1. Verifique a estrutura de pastas: deve ter `Anims/` ou `Icons/`
2. Confira o `manifest.txt` em `Anims/` — campos obrigatórios precisam estar corretos
3. Verifique se os arquivos `.fam` não estão corrompidos
4. Tente com um pack diferente para isolar o problema

### JavaScript não executa

1. Verifique se o caminho do script está correto
2. Confirme que o arquivo `.js` existe no SD
3. Verifique a sintaxe do script — erros de JS aparecem no CLI
4. Tente via: `loader open "JavaScript Runner" /ext/scripts/script.js`

### GPIO não responde

1. Verifique o pinout — pinos de sistema (power, ground) não são controláveis
2. Certifique-se de usar o modo correto (`output` para escrever, `input` para ler)
3. Verifique a tensão — o Flipper opera em 3.3V, não 5V nos pinos GPIO

### Configurações não aplicam após edição

1. As configurações em `/ext/momentum/settings` requerem reinicialização
2. Verifique o formato: deve ser `Key=Value` sem espaços em branco extras
3. Linhas de comentário devem começar com `#`
4. O cartão SD deve ter permissão de escrita (não estar no modo read-only)

### Buzzer não toca

1. Verifique a sintaxe: `buzzer note A4 500ms` ou `buzzer freq 440 500ms`
2. O Flipper pode estar com volume baixo ou mudo nas configurações
3. Certifique-se de que nenhum app está monopolizando o speaker

### Atualização falhou / Flipper em DFU

1. Use o qFlipper para detectar o Flipper em modo DFU
2. Clique em "Repair" no qFlipper para reinstalar o firmware
3. Se o qFlipper não detectar, segure BACK + LEFT ao ligar para forçar DFU
4. Em último caso, use o ST-Link para reflash manual via porta debug

---

## Apêndice: Links e Recursos

- **Repositório oficial:** `https://github.com/Next-Flip/Momentum-Firmware`
- **Releases:** `https://github.com/Next-Flip/Momentum-Firmware/releases`
- **Asset Packs da comunidade:** `https://github.com/Next-Flip/Momentum-Firmware/wiki/Asset-Packs`
- **qFlipper:** `https://flipperzero.one/update`
- **Protobuf RPC:** `https://github.com/flipperdevices/flipperzero-protobuf`
- **Documentação de Scripts JS:** `https://github.com/flipperdevices/flipperzero-firmware/blob/dev/applications/main/js/README.md`

---

*Manual elaborado com base no Momentum Firmware, analisando o código-fonte do repositório Next-Flip/Momentum-Firmware. Recursos e comandos podem variar conforme a versão instalada.*
