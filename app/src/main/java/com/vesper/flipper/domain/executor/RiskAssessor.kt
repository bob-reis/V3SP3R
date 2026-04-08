package com.vesper.flipper.domain.executor

import com.vesper.flipper.domain.model.*
import com.vesper.flipper.domain.service.PermissionService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Assesses the risk level of commands.
 * Android always computes the real risk, ignoring any AI assessment.
 *
 * Risk classes:
 * - LOW: list, read → auto-execute
 * - MEDIUM: write inside project scope → diff + apply
 * - HIGH: delete, move, overwrite, mass ops → confirmation popup
 * - BLOCKED: protected paths → settings unlock required
 */
@Singleton
class RiskAssessor @Inject constructor(
    private val permissionService: PermissionService
) {

    /**
     * Assess the risk of a command.
     * This is the authoritative risk calculation.
     */
    fun assess(command: ExecuteCommand): RiskAssessment {
        val paths = extractPaths(command)

        // Check for blocked paths first
        val blockedPath = paths.find { ProtectedPaths.isProtected(it) }
        if (blockedPath != null && !isUnlockedInSettings(blockedPath)) {
            return RiskAssessment(
                level = RiskLevel.BLOCKED,
                reason = "Protected path",
                affectedPaths = paths,
                requiresDiff = false,
                requiresConfirmation = false,
                blockedReason = getBlockedReason(blockedPath)
            )
        }

        // Assess based on action type
        return when (command.action) {
            // LOW risk: read-only operations
            CommandAction.LIST_DIRECTORY,
            CommandAction.READ_FILE,
            CommandAction.GET_DEVICE_INFO,
            CommandAction.GET_STORAGE_INFO,
            CommandAction.SEARCH_FAPHUB -> {
                RiskAssessment(
                    level = RiskLevel.LOW,
                    reason = "Read-only operation",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = false
                )
            }

            // MEDIUM risk: write operations in scope
            CommandAction.WRITE_FILE -> {
                val path = command.args.path ?: ""
                if (permissionService.hasPermission(path, CommandAction.WRITE_FILE)) {
                    RiskAssessment(
                        level = RiskLevel.MEDIUM,
                        reason = "File modification",
                        affectedPaths = paths,
                        requiresDiff = true,
                        requiresConfirmation = false
                    )
                } else {
                    RiskAssessment(
                        level = RiskLevel.HIGH,
                        reason = "Write outside permitted scope",
                        affectedPaths = paths,
                        requiresDiff = true,
                        requiresConfirmation = true
                    )
                }
            }

            CommandAction.CREATE_DIRECTORY -> {
                val path = command.args.path ?: ""
                if (permissionService.hasPermission(path, CommandAction.CREATE_DIRECTORY)) {
                    RiskAssessment(
                        level = RiskLevel.LOW,
                        reason = "Directory creation in scope",
                        affectedPaths = paths,
                        requiresDiff = false,
                        requiresConfirmation = false
                    )
                } else {
                    RiskAssessment(
                        level = RiskLevel.MEDIUM,
                        reason = "Directory creation outside scope",
                        affectedPaths = paths,
                        requiresDiff = false,
                        requiresConfirmation = true
                    )
                }
            }

            // HIGH risk: destructive operations
            CommandAction.DELETE -> {
                val recursive = command.args.recursive
                RiskAssessment(
                    level = RiskLevel.HIGH,
                    reason = if (recursive) "Recursive deletion" else "File deletion",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            CommandAction.MOVE,
            CommandAction.RENAME -> {
                RiskAssessment(
                    level = RiskLevel.HIGH,
                    reason = "Move/rename operation",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            CommandAction.COPY -> {
                val destPath = command.args.destinationPath ?: ""
                if (permissionService.hasPermission(destPath, CommandAction.WRITE_FILE)) {
                    RiskAssessment(
                        level = RiskLevel.MEDIUM,
                        reason = "Copy operation",
                        affectedPaths = paths,
                        requiresDiff = false,
                        requiresConfirmation = false
                    )
                } else {
                    RiskAssessment(
                        level = RiskLevel.HIGH,
                        reason = "Copy to unscoped destination",
                        affectedPaths = paths,
                        requiresDiff = false,
                        requiresConfirmation = true
                    )
                }
            }

            CommandAction.PUSH_ARTIFACT -> {
                val path = command.args.path ?: ""
                val artifactType = command.args.artifactType ?: "unknown"

                // Executables and apps are HIGH risk
                if (artifactType in listOf("fap", "app", "executable")) {
                    RiskAssessment(
                        level = RiskLevel.HIGH,
                        reason = "Pushing executable artifact",
                        affectedPaths = paths,
                        requiresDiff = false,
                        requiresConfirmation = true
                    )
                } else {
                    RiskAssessment(
                        level = RiskLevel.MEDIUM,
                        reason = "Pushing artifact",
                        affectedPaths = paths,
                        requiresDiff = false,
                        requiresConfirmation = true
                    )
                }
            }

            CommandAction.INSTALL_FAPHUB_APP -> {
                RiskAssessment(
                    level = RiskLevel.HIGH,
                    reason = "Download and install executable app artifact",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            // LOW risk: read-only new actions
            CommandAction.SEARCH_RESOURCES,
            CommandAction.LIST_VAULT,
            CommandAction.BROWSE_REPO,
            CommandAction.GITHUB_SEARCH -> {
                RiskAssessment(
                    level = RiskLevel.LOW,
                    reason = "Read-only catalog/inventory query",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = false
                )
            }

            // MEDIUM risk: downloads file from internet to Flipper
            CommandAction.DOWNLOAD_RESOURCE -> {
                RiskAssessment(
                    level = RiskLevel.MEDIUM,
                    reason = "Download remote file to Flipper storage",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            // MEDIUM risk: AI forge generates content (doesn't write to Flipper yet)
            CommandAction.FORGE_PAYLOAD -> {
                RiskAssessment(
                    level = RiskLevel.MEDIUM,
                    reason = "AI payload generation",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            // MEDIUM risk: runbooks execute read-only diagnostic sequences
            CommandAction.RUN_RUNBOOK -> {
                RiskAssessment(
                    level = RiskLevel.MEDIUM,
                    reason = "Diagnostic runbook execution",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            // ── Hardware control actions ─────────────────────────

            // MEDIUM risk: app launching — non-destructive but affects device state
            CommandAction.LAUNCH_APP -> {
                RiskAssessment(
                    level = RiskLevel.MEDIUM,
                    reason = "Launch app on Flipper",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            // MEDIUM risk: signal transmission — sends RF/IR but not destructive
            CommandAction.SUBGHZ_TRANSMIT -> {
                RiskAssessment(
                    level = RiskLevel.MEDIUM,
                    reason = "Sub-GHz signal transmission",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            CommandAction.IR_TRANSMIT -> {
                RiskAssessment(
                    level = RiskLevel.MEDIUM,
                    reason = "Infrared signal transmission",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            CommandAction.NFC_EMULATE -> {
                RiskAssessment(
                    level = RiskLevel.MEDIUM,
                    reason = "NFC emulation",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            CommandAction.RFID_EMULATE -> {
                RiskAssessment(
                    level = RiskLevel.MEDIUM,
                    reason = "RFID emulation",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            CommandAction.IBUTTON_EMULATE -> {
                RiskAssessment(
                    level = RiskLevel.MEDIUM,
                    reason = "iButton emulation",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            // HIGH risk: BadUSB executes keystrokes on a connected computer
            CommandAction.BADUSB_EXECUTE -> {
                RiskAssessment(
                    level = RiskLevel.HIGH,
                    reason = "BadUSB script execution (injects keystrokes)",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            // MEDIUM risk: BLE spam is non-destructive broadcast
            CommandAction.BLE_SPAM -> {
                RiskAssessment(
                    level = RiskLevel.MEDIUM,
                    reason = "BLE advertisement spam",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            // LOW risk: photo request reads from glasses camera — no Flipper side-effects
            CommandAction.REQUEST_PHOTO -> {
                RiskAssessment(
                    level = RiskLevel.LOW,
                    reason = "Glasses camera capture (read-only)",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = false
                )
            }

            // LOW risk: LED and vibro are harmless hardware feedback
            CommandAction.LED_CONTROL,
            CommandAction.VIBRO_CONTROL -> {
                RiskAssessment(
                    level = RiskLevel.LOW,
                    reason = "Hardware feedback control",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = false
                )
            }

            // ── Momentum Firmware exclusive actions ──────────────

            // LOW risk: send button press — non-destructive UI navigation
            CommandAction.INPUT_SEND -> {
                RiskAssessment(
                    level = RiskLevel.LOW,
                    reason = "Hardware button input simulation",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = false
                )
            }

            // HIGH risk: power off / reboot — interrupts device operation
            CommandAction.POWER_CONTROL -> {
                val op = (command.args.operation ?: command.args.command ?: "").lowercase()
                val isDfu = op.contains("dfu") || op.contains("recovery")
                RiskAssessment(
                    level = RiskLevel.HIGH,
                    reason = if (isDfu) "Reboot into DFU/recovery mode" else "Power off or reboot device",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            // LOW risk: loader list is read-only
            CommandAction.LOADER_LIST -> {
                RiskAssessment(
                    level = RiskLevel.LOW,
                    reason = "Read-only app list query",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = false
                )
            }

            // MEDIUM risk: loader close interrupts running app
            CommandAction.LOADER_CLOSE -> {
                RiskAssessment(
                    level = RiskLevel.MEDIUM,
                    reason = "Close foreground app on Flipper",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            // LOW risk: buzzer is harmless audio output
            CommandAction.BUZZER -> {
                RiskAssessment(
                    level = RiskLevel.LOW,
                    reason = "Buzzer audio output",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = false
                )
            }

            // LOW risk: listing asset packs is read-only
            CommandAction.ASSET_PACK_LIST -> {
                RiskAssessment(
                    level = RiskLevel.LOW,
                    reason = "Read-only asset pack query",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = false
                )
            }

            // MEDIUM risk: changing asset pack modifies a settings file
            CommandAction.ASSET_PACK_SET -> {
                RiskAssessment(
                    level = RiskLevel.MEDIUM,
                    reason = "Change active Momentum asset pack",
                    affectedPaths = paths,
                    requiresDiff = false,
                    requiresConfirmation = true
                )
            }

            // ── Batch A: Sub-GHz extended ────────────────────────

            // MEDIUM: receive opens radio — non-destructive
            CommandAction.SUBGHZ_RECEIVE -> RiskAssessment(
                level = RiskLevel.MEDIUM,
                reason = "Sub-GHz signal reception",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
            )
            // LOW: decoding a file is read-only
            CommandAction.SUBGHZ_DECODE -> RiskAssessment(
                level = RiskLevel.LOW,
                reason = "Decode Sub-GHz .sub file (read-only)",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = false
            )
            // MEDIUM: opens radio channel for chat
            CommandAction.SUBGHZ_CHAT -> RiskAssessment(
                level = RiskLevel.MEDIUM,
                reason = "Sub-GHz P2P chat (RF transmission)",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
            )

            // ── Batch B: Infrared extended ───────────────────────

            // LOW: receive is passive
            CommandAction.IR_RECEIVE -> RiskAssessment(
                level = RiskLevel.LOW,
                reason = "Passive IR signal reception",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = false
            )
            // MEDIUM: transmits IR signals (brute-force)
            CommandAction.IR_UNIVERSAL -> RiskAssessment(
                level = RiskLevel.MEDIUM,
                reason = "IR universal remote brute-force transmission",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
            )

            // ── Batch C: NFC suite ───────────────────────────────

            // LOW: toggling field is harmless
            CommandAction.NFC_FIELD -> RiskAssessment(
                level = RiskLevel.LOW,
                reason = "NFC field toggle (hardware state only)",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = false
            )
            // MEDIUM: sends data to a card
            CommandAction.NFC_APDU -> RiskAssessment(
                level = RiskLevel.MEDIUM,
                reason = "Send APDU command to NFC tag",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
            )
            // MEDIUM: writes a dump file
            CommandAction.NFC_DUMP -> RiskAssessment(
                level = RiskLevel.MEDIUM,
                reason = "Dump NFC tag to file",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
            )
            // LOW: passive scanner
            CommandAction.NFC_SCANNER -> RiskAssessment(
                level = RiskLevel.LOW,
                reason = "Passive NFC tag scanner",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = false
            )

            // ── Batch D: GPIO / I2C / Power extended ─────────────

            // MEDIUM: GPIO changes hardware pins — can affect connected circuits
            CommandAction.GPIO_CONTROL -> {
                val op = (command.args.operation ?: command.args.command ?: "get").lowercase()
                if (op == "get") {
                    RiskAssessment(
                        level = RiskLevel.LOW,
                        reason = "Read GPIO pin state",
                        affectedPaths = paths, requiresDiff = false, requiresConfirmation = false
                    )
                } else {
                    RiskAssessment(
                        level = RiskLevel.MEDIUM,
                        reason = "GPIO pin control (may affect connected hardware)",
                        affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
                    )
                }
            }
            // MEDIUM: I2C can affect connected sensors/devices
            CommandAction.I2C_CONTROL -> {
                val op = (command.args.operation ?: command.args.command ?: "scan").lowercase()
                if (op == "scan") {
                    RiskAssessment(
                        level = RiskLevel.LOW,
                        reason = "I2C bus scan (read-only)",
                        affectedPaths = paths, requiresDiff = false, requiresConfirmation = false
                    )
                } else {
                    RiskAssessment(
                        level = RiskLevel.MEDIUM,
                        reason = "I2C read/write (affects connected devices)",
                        affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
                    )
                }
            }
            // HIGH: toggling power rails can damage connected hardware
            CommandAction.POWER_RAIL -> RiskAssessment(
                level = RiskLevel.HIGH,
                reason = "External power rail control (5V/3.3V) — can damage hardware",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
            )

            // ── Batch E: JavaScript engine ────────────────────────

            // MEDIUM: JS can access GPIO, Sub-GHz, storage, etc.
            CommandAction.JS_RUN -> RiskAssessment(
                level = RiskLevel.MEDIUM,
                reason = "Execute JavaScript (can access hardware and storage)",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
            )

            // ── Batch F: Momentum settings / display ─────────────

            // MEDIUM: modifies backlight settings file
            CommandAction.RGB_BACKLIGHT -> RiskAssessment(
                level = RiskLevel.MEDIUM,
                reason = "Change RGB backlight setting",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
            )
            // MEDIUM: modifies Momentum settings file
            CommandAction.MOMENTUM_SETTING -> {
                if (command.args.settingValue == null) {
                    RiskAssessment(
                        level = RiskLevel.LOW,
                        reason = "Read Momentum setting (read-only)",
                        affectedPaths = paths, requiresDiff = false, requiresConfirmation = false
                    )
                } else {
                    RiskAssessment(
                        level = RiskLevel.MEDIUM,
                        reason = "Modify Momentum firmware setting",
                        affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
                    )
                }
            }
            // MEDIUM: writes device name file
            CommandAction.DEVICE_SPOOF -> RiskAssessment(
                level = RiskLevel.MEDIUM,
                reason = "Change Flipper display name",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
            )
            // MEDIUM: sends signal to running app
            CommandAction.LOADER_SIGNAL -> RiskAssessment(
                level = RiskLevel.MEDIUM,
                reason = "Send signal to foreground app",
                affectedPaths = paths, requiresDiff = false, requiresConfirmation = true
            )

            CommandAction.EXECUTE_CLI -> {
                val cliCommand = (command.args.command ?: command.args.content).orEmpty()
                when {
                    isLowRiskCli(cliCommand) -> RiskAssessment(
                        level = RiskLevel.LOW,
                        reason = "Read-only CLI command",
                        affectedPaths = paths,
                        requiresDiff = false,
                        requiresConfirmation = false
                    )
                    isMediumRiskCli(cliCommand) -> RiskAssessment(
                        level = RiskLevel.MEDIUM,
                        reason = "Hardware control CLI command",
                        affectedPaths = paths,
                        requiresDiff = false,
                        requiresConfirmation = true
                    )
                    else -> RiskAssessment(
                        level = RiskLevel.HIGH,
                        reason = "Potentially destructive CLI command",
                        affectedPaths = paths,
                        requiresDiff = false,
                        requiresConfirmation = true
                    )
                }
            }
        }
    }

    /**
     * Check if an operation is considered a mass operation
     */
    fun isMassOperation(command: ExecuteCommand): Boolean {
        return when (command.action) {
            CommandAction.DELETE -> command.args.recursive
            CommandAction.EXECUTE_CLI -> isMassCliOperation(command.args.command ?: command.args.content.orEmpty())
            else -> false
        }
    }

    /**
     * Extract all paths affected by a command
     */
    private fun extractPaths(command: ExecuteCommand): List<String> {
        val paths = mutableListOf<String>()
        command.args.path?.let { paths.add(it) }
        command.args.destinationPath?.let { paths.add(it) }
        if (command.action == CommandAction.EXECUTE_CLI) {
            val cliCommand = command.args.command ?: command.args.content.orEmpty()
            cliCommand.split(Regex("\\s+"))
                .filter { it.startsWith("/") }
                .forEach { paths.add(it) }
        }
        return paths
    }

    private fun isUnlockedInSettings(path: String): Boolean {
        // Check if user has explicitly unlocked this protected path
        return permissionService.isProtectedPathUnlocked(path)
    }

    private fun getBlockedReason(path: String): String {
        return when {
            ProtectedPaths.isSystemPath(path) -> "System path requires settings unlock"
            ProtectedPaths.isFirmwarePath(path) -> "Firmware path requires settings unlock"
            ProtectedPaths.SENSITIVE_EXTENSIONS.any { path.endsWith(it) } ->
                "Sensitive file type requires settings unlock"
            else -> "Protected path requires settings unlock"
        }
    }

    private fun isLowRiskCli(command: String): Boolean {
        val normalized = command.trim().lowercase()
        if (normalized.isBlank()) return false
        return SAFE_CLI_PREFIXES.any { normalized.startsWith(it) }
    }

    private fun isMediumRiskCli(command: String): Boolean {
        val normalized = command.trim().lowercase()
        if (normalized.isBlank()) return false
        return MEDIUM_CLI_PREFIXES.any { normalized.startsWith(it) }
    }

    private fun isMassCliOperation(command: String): Boolean {
        val normalized = command.trim().lowercase()
        return normalized.contains("remove_recursive") ||
                normalized.startsWith("storage format") ||
                normalized.contains(" rm ") ||
                normalized.startsWith("rm ")
    }

    companion object {
        private val SAFE_CLI_PREFIXES = listOf(
            // Read-only diagnostics
            "help",
            "version",
            "device_info",
            "device info",
            "info",
            "storage list",
            "storage ls",
            "storage read",
            "storage cat",
            "storage info",
            "storage stat",
            // Harmless hardware feedback
            "led ",
            "vibro ",
            // Momentum: read-only loader query
            "loader list",
            "loader info",
            // Momentum: input simulation (non-destructive UI navigation)
            "input send",
            "input dump",
            // Momentum: buzzer / music output
            "music_player play_note",
            "music_player stop",
            // Momentum: read-only dolphin/Momentum status
            "momentum info",
            "momentum status",
            "dolphin stats",
            "dolphin level",
            // Momentum: JS read/list
            "js list",
            "js --list",
            // Sub-GHz read-only
            "subghz decode_raw",
            // Infrared read-only
            "ir rx",
            // NFC passive operations
            "nfc scanner",
            "nfc field",
            // GPIO read
            "gpio get",
            // I2C scan
            "i2c scan",
            // System info
            "top",
            "free",
            "uptime",
            "date",
            "info",
            "neofetch",
            "src",
            // Bluetooth info
            "bt hci_info",
            // OneWire scan
            "onewire scan"
        )

        /**
         * CLI commands that should be MEDIUM risk (user confirms once)
         * instead of HIGH risk. Non-destructive hardware operations.
         */
        private val MEDIUM_CLI_PREFIXES = listOf(
            "loader open",
            "loader close",
            "loader signal",
            "subghz tx",
            "subghz tx_from_file",
            "subghz rx",
            "subghz chat",
            "subghz read_raw",
            "ir tx",
            "infrared tx",
            "nfc emulate",
            "nfc emu",
            "nfc apdu",
            "nfc raw",
            "nfc field",
            "nfc scanner",
            "rfid emulate",
            "rfid emu",
            "lfrfid emulate",
            "lfrfid emu",
            "ibutton emulate",
            "ibutton emu",
            "ble_spam",
            "blespam",
            "ble spam",
            "ble_scan",
            "blescan",
            "ble scan",
            // Momentum: music playback
            "music_player play",
            // Momentum: JavaScript execution (sandboxed, medium risk)
            "js ",
            // Momentum: dolphin XP manipulation (non-destructive)
            "dolphin xp",
            "dolphin flush",
            // Momentum: momentum settings changes (medium risk — affects device UI)
            "momentum settings",
            "momentum set",
            // Sub-GHz medium
            "subghz rx",
            "subghz chat",
            "subghz tx_from_file",
            "subghz tx ",
            // Infrared medium
            "ir tx",
            "ir universal",
            // NFC medium
            "nfc apdu",
            "nfc raw",
            "nfc dump",
            "nfc emulate",
            "nfc mfu",
            // RFID
            "rfid read",
            "rfid emulate",
            "rfid write",
            // iButton
            "ikey read",
            "ikey emulate",
            "ikey write",
            // OneWire
            "onewire read",
            // GPIO write/mode
            "gpio set",
            "gpio mode",
            // I2C
            "i2c read",
            "i2c write",
            // BT testing
            "bt carrier_tx",
            "bt carrier_rx",
            "bt packet_tx",
            "bt packet_rx",
            // Loader
            "loader open",
            "loader close",
            "loader signal",
            // Note: power off/reboot/5v/3v3 are HIGH risk via else branch.
            // Note: badusb is HIGH risk via dedicated BADUSB_EXECUTE action.
        )
    }
}
