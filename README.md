# Pedal Gaiden — libGDX playable UI prototype

Requires JDK 17+ and Gradle. In this folder:

```bash
gradle :lwjgl3:run
```

The project includes a clickable main menu, profile survey (age, duration, ninja), goals, experience, confirmation, level selector, four stage backgrounds, gameplay HUD, pause and results. **Player name is currently a fixed placeholder**; survey fields are not saved between launches. The game runs with existing assets from the EcoNinja starter package and does not depend on the composite UI reference image.

Development controls: hold **SPACE** or **UP** to pedal; hold **SHIFT** while pedalling to boost; click buttons for navigation; **P/ESC** pause. In demo mode collectibles are automatically recycled when reached. The scoring and pace are game-only values, not clinical rehabilitation metrics.

IoT mode: `gradle :lwjgl3:run --args="--iot"`. Send `RPM:65` ASCII UDP packets to `127.0.0.1:5055` at least every 2 seconds. Without packets the rider stops. A real device and clinical validation are not included. Menu clicks are UI controls; gameplay movement uses cadence.

The provided artwork contains some baked-in text/character artifacts; production sprites and seamless background layers should be prepared separately. The app is a functional visual prototype, not a pixel-perfect recreation of the reference mockup. Progression and survey settings need clinician-approved calibration before any rehabilitation use.
