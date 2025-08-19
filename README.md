# Plugin Abilities Configuration

**Note:** This is a plugin and should be placed in the **plugins** folder of your server.

---

## AirKick
The AirKick ability allows players to jump and perform a powerful aerial attack.

| Parameter   | Default Value | Description                                     |
|------------|---------------|-------------------------------------------------|
| JumpHeight | 4             | How high the player jumps before hovering.     |
| Range      | 20            | How far the kick can travel.                   |
| Cooldown   | 5000          | Time (ms) before the ability can be used again. |
| Damage     | 4.0           | Amount of damage dealt to hit entities.       |

---

## LavaCrash


| Parameter     | Default Value | Description                                                   |
|---------------|--------------|---------------------------------------------------------------|
| Cooldown      | 8000         | Time (ms) before LavaCrash can be used again.                |
| MagmaDelay    | 1500         | Delay (ms) before the magma phase begins after crashing blocks. |
| LavaDelay     | 4000         | Delay (ms) before lava particles appear and effects progress. |
| AirDelay      | 4000         | Delay (ms) before molten blocks launch into the air.         |
| RestoreDelay  | 20000        | Delay (ms) before all temporary blocks revert to original state. |
| MaxBlocks     | 400          | Maximum number of earth blocks affected by the ability.      |

---

## BlazingUppercut
The BlazingUppercut ability allows the player to dash forward and launch a target into the air with a spiral of fire.

| Parameter          | Default Value | Description                                                                 |
|-------------------|---------------|-----------------------------------------------------------------------------|
| DashDistance       | 6.0           | How far the player dashes forward.                                         |
| Damage             | 5.0           | Amount of damage dealt to hit entities.                                    |
| Cooldown           | 7000          | Time (ms) before the ability can be used again.                             |
| LaunchHeight       | 5             | How high the target is launched into the air.                               |
| BurnDuration       | 3000          | How long the target remains on fire (in ms).                                |
| FireTrailDuration  | 3000          | How long the fire trail lasts behind the player (in ms).                   |
