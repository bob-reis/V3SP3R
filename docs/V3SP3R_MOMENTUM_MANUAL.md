# V3SP3R — Manual Completo para Momentum Firmware

> Versão documentada: Momentum Firmware · V3SP3R (branch Momentum)
> Conexão: Bluetooth Low Energy (BLE) — protocolo RPC protobuf

---

## O que é V3SP3R?

V3SP3R é um agente de IA para Android que controla o Flipper Zero remotamente via BLE. Você conversa em linguagem natural e o agente executa ações reais no dispositivo: abre apps, transmite sinais de rádio, lê arquivos, controla GPIO, emula cartões NFC, e muito mais.

No Momentum Firmware, todas as capacidades exclusivas do firmware (RGB backlight, JavaScript engine, Sub-GHz estendido, keybinds, asset packs, etc.) estão disponíveis como comandos nativos.

---

## Arquitetura de Conexão

```
[Android App] ──BLE──► [Flipper Zero (Momentum)]
       │                        │
   V3SP3R Agent           RPC Session
   (Claude/GPT)          auto-start on BLE
       │                        │
  CommandExecutor ──RPC──► AppStart / FileOp / InputEvent
```

O Momentum inicia automaticamente uma sessão RPC ao conectar via BLE — não é necessário nenhum passo manual de "iniciar sessão". Todos os comandos trafegam via protobuf RPC, não via terminal CLI.

### Transporte RPC vs CLI

| Operação | Via RPC (BLE) | Via CLI (USB) |
|----------|--------------|---------------|
| Abrir app (loader open) | ✅ | ✅ |
| Transmitir Sub-GHz | ✅ | ✅ |
| Enviar botão (input_send) | ✅ RPC GUI | ✅ CLI |
| Buzzer (nota musical) | ✅ CLI*  | ✅ CLI |
| GPIO / I2C | ⚠️ só USB** | ✅ |
| loader list / close | ⚠️ só USB** | ✅ |

> *BLE: tenta CLI; se indisponível, retorna erro descritivo.
> **No BLE puro (RPC-only), comandos CLI sem mapeamento RPC requerem conexão USB ou CLI disponível.

---

## Ações Disponíveis

### Operações de Arquivo e Sistema

| Ação | Parâmetros | Risco | Descrição |
|------|-----------|-------|-----------|
| `list_directory` | `path` | LOW | Lista arquivos em um diretório |
| `read_file` | `path` | LOW | Lê conteúdo de um arquivo |
| `write_file` | `path`, `content` | MEDIUM/HIGH | Escreve conteúdo em um arquivo |
| `create_directory` | `path` | MEDIUM | Cria um diretório |
| `delete` | `path`, `recursive` | HIGH | Apaga arquivo ou diretório |
| `move` | `path`, `destination_path` | HIGH | Move arquivo |
| `rename` | `path`, `new_name` | HIGH | Renomeia arquivo |
| `copy` | `path`, `destination_path` | MEDIUM | Copia arquivo |
| `get_device_info` | — | LOW | Info do dispositivo (firmware, bateria) |
| `get_storage_info` | — | LOW | Espaço livre interno/SD |
| `execute_cli` | `command` | varies | Executa comando CLI direto |

### Controle de Hardware

| Ação | Parâmetros | Risco | Descrição |
|------|-----------|-------|-----------|
| `launch_app` | `app_name`, `app_args` | MEDIUM | Abre qualquer app por nome |
| `subghz_transmit` | `path` | MEDIUM | Transmite sinal Sub-GHz de arquivo .sub |
| `ir_transmit` | `path`, `signal_name` | MEDIUM | Transmite sinal IR de arquivo .ir |
| `nfc_emulate` | `path` | MEDIUM | Emula cartão NFC de arquivo .nfc |
| `rfid_emulate` | `path` | MEDIUM | Emula tag RFID de arquivo .rfid |
| `ibutton_emulate` | `path` | MEDIUM | Emula chave iButton de arquivo .ibtn |
| `badusb_execute` | `path` | HIGH | Executa script DuckyScript BadUSB |
| `ble_spam` | `app_args` | MEDIUM | Inicia/para spam BLE |
| `led_control` | `red`, `green`, `blue` | LOW | Controla LED RGB (0-255) |
| `vibro_control` | `enabled` | LOW | Liga/desliga vibração |

### Momentum Firmware — Ações Exclusivas

#### Controle de Interface

| Ação | Parâmetros | Risco | Descrição |
|------|-----------|-------|-----------|
| `input_send` | `key`, `press_type` | LOW | Simula pressão de botão no Flipper |
| `loader_list` | — | LOW | Lista todos os apps disponíveis |
| `loader_close` | — | MEDIUM | Fecha o app em execução |
| `loader_signal` | `signal_id`, `content` | MEDIUM | Envia sinal ao app em execução |
| `buzzer` | `note`, `duration_ms` | LOW | Toca nota no buzzer interno |

**Teclas para `input_send`:** `up`, `down`, `left`, `right`, `ok`, `back`
**Tipos para `press_type`:** `short` (padrão), `long`, `press`, `release`

#### Sub-GHz Estendido

| Ação | Parâmetros | Risco | Descrição |
|------|-----------|-------|-----------|
| `subghz_receive` | `frequency` | MEDIUM | Recebe/decodifica sinal em tempo real |
| `subghz_decode` | `path` | LOW | Decodifica arquivo .sub RAW salvo |
| `subghz_chat` | `frequency` | MEDIUM | Chat P2P Sub-GHz entre Flippers |

#### Infrared Estendido

| Ação | Parâmetros | Risco | Descrição |
|------|-----------|-------|-----------|
| `ir_receive` | — | LOW | Recebe e decodifica sinal IR passivamente |
| `ir_universal` | `category`, `signal_name` | MEDIUM | Bruteforce universal remote por categoria |

**Categorias para `ir_universal`:** `TVs`, `ACs`, `Projectors`, `Audio`, `Fans`, `BoxesAndDongles`

#### NFC Suite Completa

| Ação | Parâmetros | Risco | Descrição |
|------|-----------|-------|-----------|
| `nfc_scanner` | — | LOW | Detecta tags passivamente (UID/tipo) |
| `nfc_field` | `enabled` | LOW | Liga/desliga campo NFC |
| `nfc_dump` | `path` | MEDIUM | Dump completo da tag para arquivo .nfc |
| `nfc_apdu` | `data_hex` | MEDIUM | Envia comando APDU bruto para tag |

#### GPIO e I2C

| Ação | Parâmetros | Risco | Descrição |
|------|-----------|-------|-----------|
| `gpio_control` | `operation`, `pin`, `content`, `gpio_mode` | LOW/MEDIUM | Lê/escreve/configura pino GPIO |
| `i2c_control` | `operation`, `address`, `register`, `data_hex` | LOW/MEDIUM | Scan/leitura/escrita no barramento I2C |
| `power_rail` | `operation` | HIGH | Controla saídas de energia 5V/3.3V externas |

**Operações `gpio_control`:** `get`, `set`, `mode`
**Pinos disponíveis:** `PA4`, `PA6`, `PA7`, `PB2`, `PB3`, `PC0`, `PC1`, `PC3`
**Operações `i2c_control`:** `scan`, `read`, `write`

#### JavaScript Engine

| Ação | Parâmetros | Risco | Descrição |
|------|-----------|-------|-----------|
| `js_run` | `path` | MEDIUM | Executa arquivo .js via engine Momentum |

#### Configurações Momentum

| Ação | Parâmetros | Risco | Descrição |
|------|-----------|-------|-----------|
| `rgb_backlight` | `preset` (0-20) ou `red/green/blue` | MEDIUM | Define preset de backlight RGB |
| `momentum_setting` | `setting_key`, `setting_value` | LOW/MEDIUM | Lê ou escreve setting do Momentum |
| `device_spoof` | `content` | MEDIUM | Muda o nome do Flipper na tela |
| `asset_pack_list` | — | LOW | Lista asset packs disponíveis |
| `asset_pack_set` | `pack_name` | MEDIUM | Ativa um asset pack |
| `power_control` | `operation` | HIGH | Desliga, reinicia ou entra em DFU |

**Presets `rgb_backlight`:** 0=desligado, 1-19=cores, 20=rainbow
**Operações `power_control`:** `off`, `reboot`, `reboot_dfu`

---

## Exemplos de Uso Conversacional

### Navegação básica
```
Você:  "Pressiona OK no Flipper"
Vesper: → input_send, key: "ok", press_type: "short"

Você:  "Segura o botão direito"
Vesper: → input_send, key: "right", press_type: "long"

Você:  "Volta ao menu"
Vesper: → input_send, key: "back"

Você:  "Navega até Sub-GHz e abre"
Vesper: → input_send: down (várias vezes) → input_send: ok
```

### Sub-GHz
```
Você:  "Escuta sinais na frequência 433MHz"
Vesper: → subghz_receive, frequency: 433920000

Você:  "Decodifica o arquivo capture.sub"
Vesper: → subghz_decode, path: "/ext/subghz/capture.sub"

Você:  "Transmite o sinal da garagem"
Vesper: → subghz_transmit, path: "/ext/subghz/garagem.sub"

Você:  "Abre chat Sub-GHz com meu amigo em 868MHz"
Vesper: → subghz_chat, frequency: 868000000
```

### Infrared
```
Você:  "Aprende o sinal do meu controle remoto"
Vesper: → ir_receive
         (Aponte o controle para o Flipper e pressione o botão)

Você:  "Tenta desligar a TV"
Vesper: → ir_universal, category: "TVs", signal_name: "Power"

Você:  "Transmite o sinal Power da TV Samsung"
Vesper: → ir_transmit, path: "/ext/infrared/Samsung_TV.ir", signal_name: "Power"
```

### NFC
```
Você:  "Detecta qualquer tag NFC por perto"
Vesper: → nfc_scanner
         (Aproxime o cartão do Flipper)

Você:  "Lê o meu cartão de acesso"
Vesper: → nfc_dump, path: "/ext/nfc/meu_cartao.nfc"

Você:  "Emula meu cartão de acesso"
Vesper: → nfc_emulate, path: "/ext/nfc/meu_cartao.nfc"

Você:  "Liga o campo NFC"
Vesper: → nfc_field, enabled: true
```

### JavaScript
```
Você:  "Escreve um script que pisca o LED 3 vezes"
Vesper: → write_file, path: "/ext/scripts/blink.js", content: """
           let notify = require("js_notification");
           for(let i = 0; i < 3; i++) {
               notify.success();
               delay(500);
           }
         """
       → js_run, path: "/ext/scripts/blink.js"

Você:  "Executa meu script de Sub-GHz"
Vesper: → js_run, path: "/ext/scripts/subghz_scan.js"
```

### Configurações Momentum
```
Você:  "Muda o menu para estilo Wii"
Vesper: → momentum_setting, setting_key: "MenuStyle", setting_value: "Wii"
       → power_control, operation: "reboot"  (para aplicar)

Você:  "Coloca o backlight em rainbow"
Vesper: → rgb_backlight, preset: 20
       → power_control, operation: "reboot"

Você:  "Desativa a tela de bloqueio"
Vesper: → momentum_setting, setting_key: "LockOnBoot", setting_value: "false"

Você:  "Muda o nome do Flipper para 'BlackNet'"
Vesper: → device_spoof, content: "BlackNet"

Você:  "Qual tema de asset pack eu tenho?"
Vesper: → asset_pack_list

Você:  "Ativa o tema EvilEye"
Vesper: → asset_pack_set, pack_name: "EvilEye"
       → power_control, operation: "reboot"
```

### GPIO / Hardware
```
Você:  "Liga o LED externo no pino PA7"
Vesper: → gpio_control, operation: "set", pin: "PA7", content: "1"

Você:  "Lê o estado do pino PB3"
Vesper: → gpio_control, operation: "get", pin: "PB3"

Você:  "Escaneia dispositivos I2C"
Vesper: → i2c_control, operation: "scan"

Você:  "Habilita saída 5V"
Vesper: → power_rail, operation: "5v_on"
```

---

## Estrutura de Arquivos no SD Card

```
/ext/                          ← Raiz do SD card
├── subghz/                    ← Capturas Sub-GHz (.sub)
├── infrared/                  ← Controles IR (.ir)
├── nfc/                       ← Dumps e emulações NFC (.nfc)
├── lfrfid/                    ← Tags RFID 125kHz (.rfid)
├── ibutton/                   ← Chaves iButton (.ibtn)
├── badusb/                    ← Scripts DuckyScript (.txt)
├── apps/                      ← Apps instalados (.fap)
├── music_player/              ← Músicas (formato .fmf/.rtttl)
├── scripts/                   ← Scripts JavaScript (.js)
├── asset_packs/               ← Temas e animações Momentum
│   ├── EvilEye/
│   ├── MuayThai/
│   └── ...
├── momentum/
│   └── settings               ← Configurações do Momentum
└── dolphin/
    └── name.settings          ← Nome do dispositivo
```

---

## Referência de Settings Momentum

Arquivo: `/ext/momentum/settings` (formato `Chave=Valor`)

| Chave | Valores | Descrição |
|-------|---------|-----------|
| `MenuStyle` | List/Wii/DSi/PS4/Vertical/C64/Compact/MNTM/CoverFlow | Estilo do menu principal |
| `LockOnBoot` | true/false | Pular tela de bloqueio na inicialização |
| `RgbBacklight` | 0-20 | Preset de backlight (0=off, 20=rainbow) |
| `RgbBacklightBrightness` | 0-255 | Intensidade do backlight |
| `AssetPack` | (nome da pasta) | Pack de tema/animação ativo |
| `BatteryIcon` | 0-5 | Estilo do ícone de bateria |
| `FavoriteTimeout` | 0-300 | Timer de fechamento automático (segundos) |
| `ShowHiddenFiles` | true/false | Mostrar arquivos ocultos no browser |
| `SortDirsFirst` | true/false | Ordenar pastas primeiro |
| `SpiCc1101Handle` | Internal/External | Selecionar antena CC1101 |
| `SpiNrf24Handle` | Internal/External | Selecionar módulo NRF24 |
| `UartEspChannel` | 0-2 | Canal UART para módulo ESP Wi-Fi |
| `UartNmeaChannel` | 0-2 | Canal UART para NMEA/GPS |

**Exemplo: ler e alterar MenuStyle:**
```
Você: "Qual o estilo de menu atual?"
→ momentum_setting, setting_key: "MenuStyle"

Você: "Muda para CoverFlow"
→ momentum_setting, setting_key: "MenuStyle", setting_value: "CoverFlow"
→ power_control, operation: "reboot"
```

---

## Módulos JavaScript Disponíveis

| Módulo | Capacidade |
|--------|-----------|
| `js_subghz` | Transmissão/recepção Sub-GHz |
| `js_infrared` | Transmissão IR |
| `js_gpio` | Controle de pinos GPIO |
| `js_i2c` | Comunicação I2C |
| `js_spi` | Comunicação SPI |
| `js_serial` | UART/Serial |
| `js_badusb` | Injeção HID (teclado/mouse) |
| `js_storage` | Leitura/escrita de arquivos |
| `js_gui` | Interface: diálogos, menus, input de texto |
| `js_blebeacon` | Beacon BLE personalizado |
| `js_notification` | Feedback háptico, LED, som |
| `js_flipper` | APIs do dispositivo (nome, modelo, firmware) |
| `js_math` | Funções matemáticas |
| `js_vgm` | Reprodução de música chiptune VGM |
| `js_usbdisk` | Emulação de disco USB |
| `js_event_loop` | Loop de eventos para scripting assíncrono |

**Template de script JS:**
```javascript
// Script Momentum JS
let notify = require("js_notification");
let storage = require("js_storage");

// Lê arquivo
let content = storage.read("/ext/subghz/meu_sinal.sub");

// Feedback de sucesso
notify.success();
```

---

## Níveis de Risco e Aprovação

| Nível | Comportamento | Exemplos |
|-------|---------------|---------|
| **LOW** | Executa automaticamente | list, read, led, input_send, ir_receive, nfc_scanner |
| **MEDIUM** | Pede confirmação do usuário | write, launch_app, subghz_tx, ir_tx, nfc_emulate, momentum_setting |
| **HIGH** | Confirmação longa (segurar botão) | delete, badusb_execute, power_control, power_rail |
| **BLOCKED** | Requer desbloqueio nas configurações | /int/ (armazenamento interno), .key, .priv |

---

## Solução de Problemas

### "CLI commands are unavailable on this connection"
O Flipper está em modo RPC-only (BLE puro). Use conexão USB ou um comando que tenha mapeamento RPC. Comandos afetados: gpio_control, i2c_control, loader_list, loader_close.

### "No RPC action mapping for command"
O comando não tem equivalente RPC. Tente via `execute_cli` explicitamente, ou reconecte via USB.

### App não abre no Flipper
1. Verifique se o app está instalado: `loader_list`
2. Tente pelo nome exato do app
3. Para apps .fap externos: certifique-se que o arquivo está em `/ext/apps/`

### Configuração não aplicou após momentum_setting
Faça o reboot: `power_control, operation: "reboot"`

### Backlight não mudou
Altere o preset via `rgb_backlight` e faça reboot. O Momentum aplica configurações de backlight após reinicialização.
