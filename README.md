# Energy to EMC 🌀

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![Main Version](https://img.shields.io/badge/Minecraft-1.21.1-blue)
![Legacy Version](https://img.shields.io/badge/Minecraft-1.16.5-green)
![Loader](https://img.shields.io/badge/Loader-NeoForge%20/%20Forge-orange)

**Energy to EMC** provides a high-performance bridge between Forge Energy (FE) and ProjectE EMC. Designed for technical automation and extreme efficiency.

## 📊 Compatibility & Porting Roadmap

| Minecraft Version | Loader         | Status | Notes |
| :--- |:---------------| :--- | :--- |
| **1.21.1** | NeoForge       | ✅ **Stable** | Current Main Version |
| **1.16.5** | Forge          | ✅ **Stable** | High-Priority Legacy Port |
| **1.20.1** | Forge/NeoForge | 📅 Planned | Development starting soon |
| **1.19.2** | Forge          | 📅 Planned | - |
| **1.18.2** | Forge          | 📅 Planned | - |
| **1.12.2** | Forge          | 📅 Planned | Classic Integration Port |

## 🛠 Features

* **Advanced Syncing:** Uses a custom 64-bit split-integer system to track massive EMC values accurately across server and client.
* **Phosphor Display Smoothing:** Real-time logic that calculates average FE consumption and EMC production over 20-tick intervals for smooth GUI feedback.
* **Balanced Conversion:** Standardized at `500 FE` per `1 EMC`.
* **Intelligent Pipes:** Specialized EMC Pipes with toggleable Input/Output modes to prevent infinite extraction loops and optimize logistics.
* **Tiered Upgrades:** Support for Conversion Upgrades to scale your EMC synthesis.

## 🚀 Installation

1. **Check Version:** Ensure your Minecraft version matches the `.jar` file name.
2. **Dependency:** [ProjectE](https://www.curseforge.com/minecraft/mc-mods/projecte) must be installed.
3. **Usage:** Place an **EMC Converter** next to a power source. Use the **EMC Pipes** to extract the resulting EMC into your condensers or storage.

## 📜 License & Permissions

This project is licensed under the **MIT License**.
* **Modpacks:** You are free to include this mod in any modpack (public or private).
* **Contributions:** Found a bug? Feel free to submit an issue or a Pull Request on GitHub.

---
*Developed by **Seccad Elizade** - [View Repository](https://github.com/Seccad-Elizade/Energy-To-EMC/tree/Energy-To-EMC-1.21.1-NeoForge)*
