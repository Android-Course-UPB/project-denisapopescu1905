# 🏙️  CityAirQ: Urban Air Quality Monitoring Android Application

CityAirQ is a reliable air quality monitoring solution part of an IoT network for tracking urban pollution. Designed to collect air pollutant parameters through a device attached to bycicles, the application displays live and historical pollution data, together with dynamic charts, pollution prediction heat maps, personalized routing and warning alarms. The Android Jetpack Compose application, deployed and tested on various mobile Android releases, provides a robust data flow communication between components using BLE, support for data visualization and accessibility, easily turning any bicycle into a powerful environmental tool.

---

## Context

Author: Ioana-Denisa Popescu SRIC1A

Air quality monitoring is potentially the most important short-term solutions when it comes to reducing health risks and preserving ecosystems in the future, especially in heavily populated cities. By anticipating and visualizing this unseen phenomenon on the mobile application, metropolitan inhabitants can stay in touch with the air pollution levels in the area at all times and be alerted when air quality indicator values become remarkably high.

---

## 📲 Features

### 📡 Real-Time Monitoring
- Bluetooth scanning, state handling and connection based on service UUIDs
- BLE-based communication with ESP32 air quality devices equipped with sensors
- Live sensor data:
  - CO₂, PM1.0, PM2.5, PM10
  - Temperature, Humidity, Pressure, Altitude

<div align="center">
  <img src="https://github.com/user-attachments/assets/6a770315-3222-4d67-81be-b73592cd87b0" alt="Application BLE Screens" width="600"/>
  <img src="https://github.com/user-attachments/assets/8f83c150-cede-47b3-aa5b-dd662f258e0b" alt="Data Flow Screens" width="600"/>
</div>


### 📉 Data Visualization
- Animated circular charts and historical pollutant trend graphs
- Informational overlays for pollutant units and health tips
- Composable UI with Material Design 3
- Text-to-Speech support for accessibility
<div align="center">
  <img src="https://github.com/user-attachments/assets/9d9ce9cc-53db-41bd-ac31-2b2b6bd3c71c" alt="Data Visualization"  width="600"/>
</div>

### 🔔 Alerts & Insights
- Push notifications via Firebase Cloud Messaging
- In-app classification of alerts based on severity
- Prediction-based warnings from cloud-generated data
  
### 🗺️ Smart Mapping System
- Real-time routing using MapBox Compose
- Dynamic heatmap overlays (HeatmapLayer and CircleLayer)
- GeoJSON-based rendering of pollutants using processed data
- Distance tracking, live route drawing and customizable pollutant layers

<div align="center">
  <img src="https://github.com/user-attachments/assets/6352e037-02ee-42d6-ae87-4cdb7619453b" alt="HeatMap"  width="600"/>
</div>
<div align="center">
  <img src="https://github.com/user-attachments/assets/518a7f3b-3398-4b78-91a2-4b00268e8267" alt="Routing"  width="600"/>
</div>

---

## Architecture
### System Architecture
1. **ESP32 Device**: Collects air quality data, connects via BLE
2. **Mobile Application**: Displays real-time data, handles BLE and network communication
3. **Cloud System**: Ingests data, stores in QuestDB, runs AI for predictions

The app enables communication between modules of the architecture, following the data life cycle, from initial collection (through pollution tracking devices), to sending collected values to the cloud-based pipeline and, after thorough analysis and processing, receiving and displaying results, in forms of dynamic maps, predictions and alerts.

<div align="center">
  <img src="https://github.com/user-attachments/assets/413a0546-5cd6-4bc5-aaeb-a7f1f501dd7a" alt="Architecture Diagram" width="600" />
</div>

---

## Design Pattern - MVVMC (Model-View-ViewModel-Coordinator)
- Separation of UI, data flow, and back-end logic
- Low memory usage and lifecycle-friendly architecture
- Enhanced navigation without loss of state
<div align="center">
  <img src="https://github.com/user-attachments/assets/8c76f09a-6c86-4ff3-b24a-e0c35f736995" alt="Architecture Diagram" width="300" />
</div>

---

## 🛠 Technologies

| Component         | Technology                          |
|------------------|--------------------------------------|
| Language          | Kotlin                              |
| UI Toolkit        | Jetpack Compose                     |
| Design            | Material Design 3                   |
| Navigation        | Android Navigation Library          |
| Bluetooth         | Android BLE Library                 |
| Networking        | Retrofit + Moshi                    |
| Mapping           | MapBox Compose Extension            |
| Charts            | MPAndroidChart, AnyChart, Canvas    |
| Voice Output      | Android TextToSpeech API            |
| Data Storage      | Room Database                       |

---

## 📡 Data Flow

### 1. BLE GATT Communication
- ESP32 devices act as **GATT servers**
- BLE clients on Android scan and connect using UUIDs
- Sensor values transferred using `Notify` characteristics
<div align="center">
  <img src="https://github.com/user-attachments/assets/d689a7fe-626f-421e-985c-fb4002f5c513" alt="BLE Diagram" width="600" />
</div>

### 2. Cloud Communication
- JSON payload via HTTP POST every 30s:
```json
{
  "clientId": "clientId-ec85695bd6a1fdaa",
  "timestamp": 1718699160,
  "location": {
    "latitude": 44.384256,
    "longitude": 26.0866048
  },
  "data": [
    {
      "dimension": "Temperature",
      "value": 35
    },
    {
      "dimension": "Humidity",
      "value": 67
    }
  ]
}
```
- A similar POST request is transmitted every 30s to get map results.
## 📍 Mapping & Routing

- **MapBox Features:**
  - Real-time tracking with smooth zoom and styling
  - 2D puck animation for user location visualization
- **User Routes:**
  - Rendered using `LineString` GeoJSON objects
- **Visual Layers:**
  - HeatMap and CircleLayer styles reflecting pollution intensity
- **Distance Tracking:**
  - Calculates distance between location points with experimental threshold cutoffs

---

## 🧭 Navigation & Design

- Smooth, intuitive navigation with persistent state across screens  
- Clean, user-friendly interface focused on real-time tracking and pollution data
- Modern UI made using Material Design 3
- Figma Design
- Audio feedback for hands-free use and accessibility  
- Optimized for performance on a range of devices

<div align="center">
  <img src="https://github.com/user-attachments/assets/c3e77f58-4406-4d39-b12c-178e8323d7bf" alt="Navigation" width="300" />
</div>
<div align="center">
  <img src="https://github.com/user-attachments/assets/bbb9a3bc-c8f8-46b3-89dd-cddb821eb6e8" alt="Design" width="500" />
</div>
<div align="center">
  <img src="https://github.com/user-attachments/assets/87760fe1-5451-46e8-a058-8a2bd916929c" alt="UI" width="500" />
</div>

---  

## 🗃 Permissions & Requirements

### Permissions
- `BLUETOOTH`, `BLUETOOTH_ADMIN`
- `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`

### Requirements
- Android 8.0+ (min SDK 26)
- BLE (Bluetooth Low Energy) support on device
- Internet and location services enabled (state handling mechanisms exist)

---

## 🧪 Testing & Evaluation

### Tested Devices
- Xiaomi Redmi Note 8 Pro
- HONOR Magic6 Lite, Samsung A13, Samsung Tab S8
- Huawei P20, Xiaomi 12X

### Performance Insights

| Action                   | CPU Usage | RAM Usage | Energy Use |
|--------------------------|-----------|-----------|------------|
| BLE Connection           | 18%       | 272MB     | Light      |
| Measure Screen + TTS     | 29%       | 290MB     | Light      |
| Map Routing + HeatMap    | 43%       | 470MB     | Medium     |
| Background Sync (screen off) | 17%   | 400MB     | Light      |

---

## 🔐 Privacy

CityAirQ respects user privacy. Only anonymized location data is transmitted. Permissions are requested transparently with clear user consent.

---

## 📥 Getting Started

### Clone the Repository
```bash
git clone  https://github.com/Android-Course-UPB/project-denisapopescu1905.git
