# LightingMod-Paper

If you are looking for the Fabric version of this mod, you can find it [here](https://github.com/plngvln/lightingmod).

LightingMod-Paper is a custom Minecraft plugin for Paper servers that adds realistic lightning behavior, including the ability for lightning to be attracted to lightning rods. The plugin allows for dynamic lightning strikes based on player activity and thunderstorm conditions in the game world.

## Features

- **Random Lightning Strikes**: During thunderstorms, random players in the world can be targeted for lightning strikes.
- **Lightning Rod Attraction**: Lightning can be attracted to nearby lightning rods within a specified radius.
- **Configurable Chance and Radius**: The chance of lightning striking and the radius for lightning rod attraction can be configured.

## Installation

1. Download the latest version of the plugin `.jar` file.
2. Place the `.jar` file into your server's `plugins` folder.
3. Start or restart your Paper server.
4. The plugin will automatically generate a configuration file in the `plugins/LightingMod-Paper` directory.

## Configuration

The configuration file `config.yml` contains the following settings:

- `modEnabled` (boolean): Whether the lightning mod is enabled (`true` or `false`).
- `checkInterval` (integer): The interval in server ticks for checking if lightning should strike (default is 100 ticks, or 5 seconds).
- `lightningChance` (double): The chance of lightning striking each interval (0.0 - 1.0).
- `lightningRadius` (integer): The radius within which a random lightning strike can occur around a player.
- `lightningRodEnabled` (boolean): Whether lightning rods can attract lightning.
- `debugMode` (boolean): Outputs additional debug information if enabled.

## Permissions

Currently, the plugin does not require any permissions to function, and all configuration is done through the `config.yml` file.

## Commands

There are no commands associated with this plugin. All settings are managed through the configuration file.

## Troubleshooting

- **Lightning Doesn't Strike**: Ensure `modEnabled` is set to `true` and the chance of lightning (`lightningChance`) is greater than `0.0`.
- **Performance Issues**: Lower the `checkInterval` or reduce the `lightningRadius` if you experience performance problems.

## License

This plugin is distributed under the MIT License. See the `LICENSE` file for more information.

## Contributing

Feel free to open an issue or submit a pull request if you would like to contribute to the project. Your feedback and contributions are always welcome!

