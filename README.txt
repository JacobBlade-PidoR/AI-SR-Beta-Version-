AI-SR Lite — normal full prototype

Includes:
- Java Android app
- MediaProjection screen capture
- TinySR learned convolution model (CPU prototype)
- 160x90 working image -> 320x180 SR preview
- optional Android overlay permission shortcut
- no external AI runtime dependency

Important:
This prototype captures and processes frames; it does not yet replace another game's renderer.
The overlay permission is optional and is not a transparent GPU compositor.

Build:
Open this folder itself as the project root in Android IDE - PHONE AS.
Do not create/open a nested project inside it.
