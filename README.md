# Wizard Private VPN — Android project starter

This repository is the first Android implementation scaffold for the approved three-screen Wizard-style game UI.

## Important build status
This environment did not have Android SDK / Gradle installed, so an APK could not be compiled or device-tested here. Open this project in Android Studio (Hedgehog or newer), allow Gradle sync, then Build > Build APK(s).

## Current scaffold
- Kotlin + Jetpack Compose UI
- Main table, bidding, and round-results screens
- 20-second local bidding countdown with auto-bid 0 on timeout
- Player-name editing and server/room settings fields\n- Admin settings control to add 0–N computer-player slots, capped at 10 total players
- 2–10 player UI configuration target
- Dark navy / purple / gold theme

## Multiplayer status
The current app is a UI/game-flow scaffold only. It does **not yet implement live networking**. The VPN server address and room-code fields are placeholders. The next implementation step is to add a WebSocket client and a Windows-hosted authoritative game server. All phones must first connect to the same OpenVPN network, then connect to the server's VPN IP and port.

Do not expose the WebSocket port to the public internet. Bind the server to the VPN interface where possible and use a room PIN/token. OpenVPN provides the private network tunnel; it does not itself provide game room logic.

## Game rules to finalize
- Standard Wizard deck: 60 cards (52 suited cards + 4 Wizards + 4 Jesters).
- Cards dealt per round equals the round number.
- Maximum rounds = floor(60 / player count).
- A complete bid set is invalid when the sum of bids equals the number of cards dealt that round.
- Each bid timer is 20 seconds; default timeout action is bid 0.
- Standard Wizard scoring should be verified and implemented before release (exact bid: 20 + 10 per trick; miss: -10 per trick difference).
- Wizard/Jester card art in this scaffold uses symbolic placeholders; replace with custom/original artwork or properly licensed official assets. Do not distribute scanned copyrighted card faces without permission.

## Suggested next steps
1. Open in Android Studio and sync.
2. Add WebSocket protocol and Windows server.
3. Configure VPN address/port once OpenVPN is installed.
4. Test 2 devices on VPN, then 2–10 devices.
5. Build signed release APK.
\n## AI difficulty selection\n- Default AI difficulty is Medium.\n- Settings UI allows Easy / Medium / Hard selection.\n- The selector is present; actual difficulty-specific AI decision logic still needs implementation.\n
## Game modes
- Offline mode is the default and is intended for one device with AI opponents; it requires no internet or VPN.
- Private VPN mode is intended for multiplayer over the user's OpenVPN network.
- Both modes retain the same three-screen visual design; mode selection is in the setup/settings screen.
- Current source includes the mode selector only. A complete offline Wizard rules engine, playable AI decision logic, and VPN networking/server are still outstanding; do not treat this scaffold as a finished playable game.
