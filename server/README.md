# Windows-hosted game server — pending implementation

The Android UI is prepared for a private VPN endpoint, but no server executable is included yet. The multiplayer server must be implemented and tested before this can be called an online multiplayer APK.

Recommended first deployment:
- Windows 10 PC runs an authoritative Java/Ktor or Node WebSocket server.
- Friends connect to OpenVPN first.
- App connects to `ws://<Windows VPN IP>:8080`.
- Server manages rooms, player order, 20-second deadlines, bids, trick outcomes and scores.
- Use a room token; do not trust client-submitted scores.
