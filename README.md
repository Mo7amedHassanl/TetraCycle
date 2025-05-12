# TetraCycle

An Android water quality monitoring application built with Jetpack Compose and Firebase Realtime Database.

## Overview

LoayApp is designed to monitor and visualize water quality metrics in real-time. The application tracks various parameters such as:

- pH level
- TDS (Total Dissolved Solids)
- Turbidity
- Flow rate
- Water volume

## Features

- **Real-time Monitoring**: View current sensor readings on the home screen
- **Historical Data**: Track sensor readings over time with trend visualization
- **Alerts**: Get notified when sensor readings go outside of normal ranges
- **Offline Support**: Firebase persistence enables the app to work without an internet connection
- **Modern UI**: Built with Jetpack Compose for a beautiful, responsive user interface

## Technical Details

- **Platform**: Android (minimum SDK 24)
- **Architecture**: MVVM with clean architecture principles
- **UI Framework**: Jetpack Compose
- **Dependency Injection**: Hilt
- **Backend**: Firebase Realtime Database
- **Charts**: Vico Compose charting library
- **Navigation**: Jetpack Navigation Compose

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11
- Firebase account

## Getting Started

1. Clone the repository
2. Open the project in Android Studio
3. Connect to your Firebase project by adding your `google-services.json` file
4. Build and run the application

## Firebase Setup

The app uses Firebase Realtime Database to store and sync sensor data. Make sure your database follows this structure:

```
{
  "sensor_readings": [
    {
      "flow": 0.0,
      "ph": 7.0,
      "tds": 120.0,
      "timestamp": "2023-10-01T12:00:00Z",
      "turbidity": 10.0,
      "volume": 500.0
    },
    ...
  ]
}
```

## License

[Your license here]

## Contact

[Your contact information] 
