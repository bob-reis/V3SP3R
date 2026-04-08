package com.vesper.flipper.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The single command interface for AI agent interaction.
 * All Flipper operations go through this unified structure.
 */
@Serializable
data class ExecuteCommand(
    val action: CommandAction,
    val args: CommandArgs,
    val justification: String,
    @SerialName("expected_effect")
    val expectedEffect: String
)

@Serializable
enum class CommandAction {
    @SerialName("list_directory")
    LIST_DIRECTORY,

    @SerialName("read_file")
    READ_FILE,

    @SerialName("write_file")
    WRITE_FILE,

    @SerialName("create_directory")
    CREATE_DIRECTORY,

    @SerialName("delete")
    DELETE,

    @SerialName("move")
    MOVE,

    @SerialName("rename")
    RENAME,

    @SerialName("copy")
    COPY,

    @SerialName("get_device_info")
    GET_DEVICE_INFO,

    @SerialName("get_storage_info")
    GET_STORAGE_INFO,

    @SerialName("search_faphub")
    SEARCH_FAPHUB,

    @SerialName("install_faphub_app")
    INSTALL_FAPHUB_APP,

    @SerialName("push_artifact")
    PUSH_ARTIFACT,

    @SerialName("execute_cli")
    EXECUTE_CLI,

    @SerialName("forge_payload")
    FORGE_PAYLOAD,

    @SerialName("search_resources")
    SEARCH_RESOURCES,

    @SerialName("list_vault")
    LIST_VAULT,

    @SerialName("run_runbook")
    RUN_RUNBOOK,

    // ── Hardware control actions ──────────────────────────────

    @SerialName("launch_app")
    LAUNCH_APP,

    @SerialName("subghz_transmit")
    SUBGHZ_TRANSMIT,

    @SerialName("ir_transmit")
    IR_TRANSMIT,

    @SerialName("nfc_emulate")
    NFC_EMULATE,

    @SerialName("rfid_emulate")
    RFID_EMULATE,

    @SerialName("ibutton_emulate")
    IBUTTON_EMULATE,

    @SerialName("badusb_execute")
    BADUSB_EXECUTE,

    @SerialName("ble_spam")
    BLE_SPAM,

    @SerialName("led_control")
    LED_CONTROL,

    @SerialName("vibro_control")
    VIBRO_CONTROL,

    @SerialName("browse_repo")
    BROWSE_REPO,

    @SerialName("download_resource")
    DOWNLOAD_RESOURCE,

    @SerialName("github_search")
    GITHUB_SEARCH,

    // ── Smartglasses camera ───────────────────────────────────

    @SerialName("request_photo")
    REQUEST_PHOTO,

    // ── Momentum Firmware exclusive actions ───────────────────

    /** Send a hardware button event to the Flipper UI (Momentum: `input send <key> <type>`). */
    @SerialName("input_send")
    INPUT_SEND,

    /** Power off, reboot, or enter DFU/recovery mode (`power off|reboot|reboot_dfu`). */
    @SerialName("power_control")
    POWER_CONTROL,

    /** List all installed/available apps on the Flipper (`loader list`). */
    @SerialName("loader_list")
    LOADER_LIST,

    /** Close the currently running foreground app (`loader close`). */
    @SerialName("loader_close")
    LOADER_CLOSE,

    /** Play a tone on the Flipper buzzer (`music_player` / `buzzer`). */
    @SerialName("buzzer")
    BUZZER,

    /** List available asset packs from `/ext/asset_packs/`. */
    @SerialName("asset_pack_list")
    ASSET_PACK_LIST,

    /** Switch to a different asset pack (updates Momentum settings file). */
    @SerialName("asset_pack_set")
    ASSET_PACK_SET,

    // ── Batch A: Sub-GHz extended ─────────────────────────────

    /** Receive and decode a Sub-GHz signal at a given frequency (`subghz rx <freq>`). */
    @SerialName("subghz_receive")
    SUBGHZ_RECEIVE,

    /** Decode a previously captured RAW .sub file (`subghz decode_raw <path>`). */
    @SerialName("subghz_decode")
    SUBGHZ_DECODE,

    /** Open Sub-GHz text chat at a frequency (`subghz chat <freq>`). */
    @SerialName("subghz_chat")
    SUBGHZ_CHAT,

    // ── Batch B: Infrared extended ────────────────────────────

    /** Receive and decode an incoming IR signal (`ir rx`). */
    @SerialName("ir_receive")
    IR_RECEIVE,

    /** Brute-force transmit all signals from a universal remote category (`ir universal <remote> <signal>`). */
    @SerialName("ir_universal")
    IR_UNIVERSAL,

    // ── Batch C: NFC suite ────────────────────────────────────

    /** Toggle NFC field on/off (`nfc field`). */
    @SerialName("nfc_field")
    NFC_FIELD,

    /** Send raw APDU command to an NFC tag (`nfc apdu <hex>`). */
    @SerialName("nfc_apdu")
    NFC_APDU,

    /** Dump full NFC tag contents to a file (`nfc dump <path>`). */
    @SerialName("nfc_dump")
    NFC_DUMP,

    /** Passive NFC scanner — detect tags and report UID/type (`nfc scanner`). */
    @SerialName("nfc_scanner")
    NFC_SCANNER,

    // ── Batch D: GPIO / I2C / Power extended ─────────────────

    /** Read or write a GPIO pin (`gpio set|get|mode <pin> [value]`). */
    @SerialName("gpio_control")
    GPIO_CONTROL,

    /** Scan or read/write the I2C bus (`i2c scan|read|write ...`). */
    @SerialName("i2c_control")
    I2C_CONTROL,

    /** Control external power rails (`power 5v|3v3 <0|1>`). */
    @SerialName("power_rail")
    POWER_RAIL,

    // ── Batch E: JavaScript engine ────────────────────────────

    /** Execute a JavaScript file via Momentum JS engine (`js <path>`). */
    @SerialName("js_run")
    JS_RUN,

    // ── Batch F: Momentum settings / display ─────────────────

    /** Set RGB backlight color preset (Momentum exclusive). */
    @SerialName("rgb_backlight")
    RGB_BACKLIGHT,

    /** Read or write any Momentum settings key in `/ext/momentum/settings`. */
    @SerialName("momentum_setting")
    MOMENTUM_SETTING,

    /** Spoof the Flipper device name shown on screen. */
    @SerialName("device_spoof")
    DEVICE_SPOOF,

    /** Send a signal to the currently running app (`loader signal <id> [arg]`). */
    @SerialName("loader_signal")
    LOADER_SIGNAL
}

@Serializable
data class CommandArgs(
    val command: String? = null,
    val path: String? = null,
    @SerialName("destination_path")
    val destinationPath: String? = null,
    val content: String? = null,
    @SerialName("new_name")
    val newName: String? = null,
    val recursive: Boolean = false,
    @SerialName("artifact_type")
    val artifactType: String? = null,
    @SerialName("artifact_data")
    val artifactData: String? = null,
    val prompt: String? = null,
    @SerialName("resource_type")
    val resourceType: String? = null,
    @SerialName("runbook_id")
    val runbookId: String? = null,
    @SerialName("payload_type")
    val payloadType: String? = null,
    val filter: String? = null,

    // Hardware control fields
    @SerialName("app_name")
    val appName: String? = null,
    @SerialName("app_args")
    val appArgs: String? = null,
    val frequency: Long? = null,
    val protocol: String? = null,
    val address: String? = null,
    @SerialName("signal_name")
    val signalName: String? = null,
    val enabled: Boolean? = null,
    val red: Int? = null,
    val green: Int? = null,
    val blue: Int? = null,
    @SerialName("repo_id")
    val repoId: String? = null,
    @SerialName("sub_path")
    val subPath: String? = null,
    @SerialName("download_url")
    val downloadUrl: String? = null,
    @SerialName("search_scope")
    val searchScope: String? = null,
    @SerialName("photo_prompt")
    val photoPrompt: String? = null,

    // Momentum-specific args
    /** Hardware button key for input_send: up / down / left / right / ok / back / unlock */
    val key: String? = null,
    /** Press type for input_send: press / release / short / long */
    @SerialName("press_type")
    val pressType: String? = null,
    /** Power operation: off / reboot / reboot_dfu / 5v_on / 5v_off / 3v3_on / 3v3_off */
    val operation: String? = null,
    /** Asset pack name for asset_pack_set */
    @SerialName("pack_name")
    val packName: String? = null,
    /** Musical note or frequency string for buzzer (e.g. "A4", "440") */
    val note: String? = null,
    /** Duration in milliseconds for buzzer */
    @SerialName("duration_ms")
    val durationMs: Int? = null,
    /** GPIO pin name for gpio_control (e.g. "PA7", "PB3", "PC3") */
    val pin: String? = null,
    /** GPIO mode for gpio_control: input / output / analog / opendrain */
    @SerialName("gpio_mode")
    val gpioMode: String? = null,
    /** I2C register address (0-255) for i2c_control read/write */
    val register: Int? = null,
    /** Hex data string for i2c_control write or nfc_apdu command */
    @SerialName("data_hex")
    val dataHex: String? = null,
    /** RGB backlight preset: 0=off, 1-19=color preset, 20=rainbow */
    val preset: Int? = null,
    /** Momentum setting key for momentum_setting (e.g. "MenuStyle", "LockOnBoot") */
    @SerialName("setting_key")
    val settingKey: String? = null,
    /** Momentum setting value for momentum_setting (e.g. "Wii", "false") */
    @SerialName("setting_value")
    val settingValue: String? = null,
    /** Signal ID for loader_signal */
    @SerialName("signal_id")
    val signalId: Int? = null,
    /** IR universal remote category (e.g. "TVs", "ACs") for ir_universal */
    val category: String? = null
)

/**
 * Result returned after command execution
 */
@Serializable
data class CommandResult(
    val success: Boolean,
    val action: CommandAction,
    val data: CommandResultData? = null,
    val error: String? = null,
    @SerialName("execution_time_ms")
    val executionTimeMs: Long = 0,
    @SerialName("requires_confirmation")
    val requiresConfirmation: Boolean = false,
    @SerialName("pending_approval_id")
    val pendingApprovalId: String? = null
)

@Serializable
data class CommandResultData(
    val entries: List<FileEntry>? = null,
    val content: String? = null,
    @SerialName("bytes_written")
    val bytesWritten: Long? = null,
    @SerialName("device_info")
    val deviceInfo: DeviceInfo? = null,
    @SerialName("storage_info")
    val storageInfo: StorageInfo? = null,
    val diff: FileDiff? = null,
    val message: String? = null
)

@Serializable
data class FileEntry(
    val name: String,
    val path: String,
    @SerialName("is_directory")
    val isDirectory: Boolean,
    val size: Long = 0,
    @SerialName("modified_timestamp")
    val modifiedTimestamp: Long? = null
)

@Serializable
data class DeviceInfo(
    val name: String,
    @SerialName("firmware_version")
    val firmwareVersion: String,
    @SerialName("hardware_version")
    val hardwareVersion: String,
    @SerialName("battery_level")
    val batteryLevel: Int,
    @SerialName("is_charging")
    val isCharging: Boolean
)

@Serializable
data class StorageInfo(
    @SerialName("internal_total")
    val internalTotal: Long,
    @SerialName("internal_free")
    val internalFree: Long,
    @SerialName("external_total")
    val externalTotal: Long? = null,
    @SerialName("external_free")
    val externalFree: Long? = null,
    @SerialName("has_sd_card")
    val hasSdCard: Boolean
)

@Serializable
data class FileDiff(
    @SerialName("original_content")
    val originalContent: String?,
    @SerialName("new_content")
    val newContent: String,
    @SerialName("lines_added")
    val linesAdded: Int,
    @SerialName("lines_removed")
    val linesRemoved: Int,
    @SerialName("unified_diff")
    val unifiedDiff: String
)
