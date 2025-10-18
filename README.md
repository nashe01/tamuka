# Glide - Ride Sharing App

A comprehensive Android ride-sharing application built with modern Android development practices, featuring real-time location tracking, Firebase integration, and a dual-role system for both commuters and drivers.

## Tech Stack

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=for-the-badge&logo=Firebase&logoColor=white)
![Google Maps](https://img.shields.io/badge/Google_Maps-4285F4?style=for-the-badge&logo=google-maps&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=material-design&logoColor=white)

## Overview

Glide is a full-featured ride-sharing platform that connects commuters with drivers in real-time. The app provides seamless user experiences for both user types with role-based navigation, real-time location tracking, and comprehensive ride management.

## Key Features

### Authentication System
- **Firebase Authentication** with email/password login
- **Role-based registration** (Commuter/Driver)
- **Secure user management** with Firebase Realtime Database
- **Profile setup** with role-specific information
- **Password reset** functionality

### Driver Features
- **Real-time location tracking** with GPS integration
- **Availability toggle** (online/offline status)
- **Incoming ride request notifications** with real-time updates
- **Driver dashboard** with earnings and statistics
- **Ride acceptance/decline** with instant status updates
- **Profile management** with vehicle information

### Commuter Features
- **Interactive map interface** with Google Maps integration
- **Destination selection** via map interaction or search
- **Nearby drivers display** with real-time markers
- **Ride request system** with automatic driver matching
- **Real-time ride tracking** and status updates
- **Ride history** and trip management

### Location Services
- **Google Maps integration** with custom markers
- **Real-time GPS tracking** for both drivers and commuters
- **Location permissions** handling
- **Distance calculations** using Haversine formula
- **Automatic location updates** with Firebase synchronization

### Real-time Features
- **Live ride request updates** across all connected clients
- **Driver availability monitoring** with real-time UI updates
- **Instant notifications** for ride status changes
- **Real-time database synchronization** with Firebase
- **Automatic UI updates** based on database changes

## Technical Architecture

### Android Components
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 14)
- **Language**: ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white)
- **Architecture**: MVC with Firebase integration
- **UI Framework**: ![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=flat&logo=material-design&logoColor=white)

### Firebase Integration
- ![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=flat&logo=Firebase&logoColor=white) **Authentication** for user management
- ![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=flat&logo=Firebase&logoColor=white) **Realtime Database** for live data
- ![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=flat&logo=Firebase&logoColor=white) **Analytics** for app insights
- ![Google Play](https://img.shields.io/badge/Google_Play-414141?style=flat&logo=google-play&logoColor=white) **Services** for Maps and Location

### Database Structure
```
Firebase Realtime Database:
├── commuters/{userId} - Commuter profiles and location
├── drivers/{userId} - Driver profiles and availability
├── rideRequests/{rideId} - Active ride requests
└── drivers_live/{driverId} - Real-time driver locations
```

### Technology Stack
- ![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=flat&logo=Firebase&logoColor=white) **Firebase BoM** - Firebase services management
- ![Google Maps](https://img.shields.io/badge/Google_Maps-4285F4?style=flat&logo=google-maps&logoColor=white) **Maps SDK** - Map functionality
- ![Google Play](https://img.shields.io/badge/Google_Play-414141?style=flat&logo=google-play&logoColor=white) **Play Services** - Location services
- ![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=flat&logo=material-design&logoColor=white) **Material Design** - UI components
- ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) **AndroidX** - Modern Android libraries

## Getting Started

### Prerequisites
- ![Android Studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=flat&logo=android-studio&logoColor=white) **Android Studio** Arctic Fox or later
- ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) **Android SDK** 24+
- ![Google Maps](https://img.shields.io/badge/Google_Maps-4285F4?style=flat&logo=google-maps&logoColor=white) **Google Maps API** key
- ![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=flat&logo=Firebase&logoColor=white) **Firebase** project setup

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/glide.git
   cd glide
   ```

2. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Download `google-services.json` and place it in `app/` directory
   - Enable Authentication and Realtime Database in Firebase Console

3. **Configure Google Maps**
   - Get a Google Maps API key from [Google Cloud Console](https://console.cloud.google.com)
   - Update the API key in `AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_API_KEY_HERE"/>
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

## App Flow

### User Journey

#### New User Registration
1. **Splash Screen** → **Onboarding** (3 screens)
2. **Role Selection** → Choose Commuter or Driver
3. **Registration** → Fill profile information
4. **Profile Setup** → Complete role-specific details
5. **Dashboard** → Access main app features

#### Existing User Login
1. **Login Screen** → Enter credentials
2. **Role Detection** → Automatic navigation based on user type
3. **Dashboard** → Access role-specific features

### Driver Workflow
1. **Go Online** → Toggle availability status
2. **Receive Requests** → Real-time ride request notifications
3. **Accept/Decline** → Respond to ride requests
4. **Navigate to Pickup** → GPS navigation to commuter
5. **Complete Ride** → End trip and receive payment

### Commuter Workflow
1. **Set Destination** → Long-press on map or search
2. **View Drivers** → See nearby available drivers
3. **Request Ride** → Send request to selected driver
4. **Track Driver** → Real-time driver location updates
5. **Complete Trip** → Rate driver and make payment

## Development Phases

### Phase 1: Authentication & Onboarding ✅
- ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) **Splash screen** with auto-navigation
- ![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=flat&logo=material-design&logoColor=white) **3-screen onboarding** flow
- ![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=flat&logo=Firebase&logoColor=white) **Authentication** system
- ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white) **Role-based registration** (Commuter/Driver)
- ![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=flat&logo=material-design&logoColor=white) **Profile setup** screens
- ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) **Navigation flow** implementation

### Phase 2: Core Features ✅
- ![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=flat&logo=material-design&logoColor=white) **Main application** interfaces
- ![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=flat&logo=Firebase&logoColor=white) **Ride request** system
- ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white) **Driver matching** and acceptance
- ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) **Trip management**
- ![Payment](https://img.shields.io/badge/Payment-4285F4?style=flat&logo=google-pay&logoColor=white) **Payment system** integration
- ![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=flat&logo=material-design&logoColor=white) **User profile** and settings

### Phase 3: Real-time Features ✅
- ![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=flat&logo=Firebase&logoColor=white) **Realtime Database** integration
- ![Real-time](https://img.shields.io/badge/Real--time-FF6B6B?style=flat&logo=clock&logoColor=white) **Live ride request** updates
- ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) **Driver availability** monitoring
- ![Google Maps](https://img.shields.io/badge/Google_Maps-4285F4?style=flat&logo=google-maps&logoColor=white) **Real-time location** tracking
- ![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=flat&logo=material-design&logoColor=white) **Automatic UI** updates
- ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white) **Distance calculations**

## Project Structure

```
app/
├── src/main/
│   ├── java/com/kodelink/glide/
│   │   ├── Activities/
│   │   │   ├── MainActivity.java
│   │   │   ├── OnboardingActivity.java
│   │   │   ├── LoginActivity.java
│   │   │   ├── RegisterCommuterActivity.java
│   │   │   ├── RegisterDriverActivity.java
│   │   │   ├── HomeCommuterActivity.java
│   │   │   ├── DashboardDriverActivity.java
│   │   │   └── ...
│   │   ├── Models/
│   │   │   ├── User.java
│   │   │   ├── Driver.java
│   │   │   ├── Commuter.java
│   │   │   ├── RideRequest.java
│   │   │   └── Vehicle.java
│   │   ├── Services/
│   │   │   ├── FirebaseService.java
│   │   │   └── DatabaseCleanup.java
│   │   └── Utils/
│   │       ├── BaseActivity.java
│   │       └── DataInitializer.java
│   ├── res/
│   │   ├── layout/ - XML layout files
│   │   ├── drawable/ - Icons and graphics
│   │   ├── values/ - Colors, strings, styles
│   │   └── mipmap/ - App icons
│   └── AndroidManifest.xml
├── build.gradle.kts
└── google-services.json
```

## Configuration

### Firebase Setup
1. Create Firebase project
2. Enable Authentication (Email/Password)
3. Enable Realtime Database
4. Download `google-services.json`
5. Configure database rules

### Google Maps Setup
1. Enable Maps SDK for Android
2. Enable Places API (for future search features)
3. Generate API key with proper restrictions
4. Update AndroidManifest.xml

### Permissions
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.INTERNET"/>
```

## Testing

### Test Data
The app includes sample data for testing:
- **Sample Drivers**: John Doe, Jane Smith, Mike Johnson
- **Sample Commuters**: Blessing, Sarah
- **Test Locations**: Harare CBD, Eastlea, Avondale

### Testing Features
- User registration and login
- Role-based navigation
- Real-time location updates
- Ride request workflow
- Driver availability toggle
- Firebase database operations

## Deployment

### Build Configuration
```kotlin
android {
    compileSdk = 36
    defaultConfig {
        applicationId = "com.kodelink.glide"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
```

### Release Build
```bash
./gradlew assembleRelease
```

## Performance Features

- ![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=flat&logo=Firebase&logoColor=white) **Real-time updates** with Firebase listeners
- ![Google Play](https://img.shields.io/badge/Google_Play-414141?style=flat&logo=google-play&logoColor=white) **Efficient location tracking** with FusedLocationProviderClient
- ![Database](https://img.shields.io/badge/Database-336791?style=flat&logo=firebase&logoColor=white) **Optimized database queries** with proper indexing
- ![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=flat&logo=material-design&logoColor=white) **Material Design** for smooth UI interactions
- ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) **Edge-to-edge** display for modern Android experience

## Security Features

- ![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=flat&logo=Firebase&logoColor=white) **Firebase Authentication** for secure user management
- ![Security](https://img.shields.io/badge/Security-FF6B6B?style=flat&logo=security&logoColor=white) **Role-based access control** for different user types
- ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white) **Input validation** and sanitization
- ![Database](https://img.shields.io/badge/Database-336791?style=flat&logo=firebase&logoColor=white) **Secure database rules** for data protection
- ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) **Location permission handling** with user consent

## Known Issues & Solutions

### Resolved Issues
- ✅ Authentication flow timing issues
- ✅ App logo integration crashes
- ✅ Build resource merge errors
- ✅ Missing import compilation errors
- ✅ UI layout optimization
- ✅ Navigation drawer logout functionality

### Current Limitations
- Address search requires Google Places API integration
- Push notifications need FCM setup
- Payment processing requires gateway integration
- Offline functionality needs implementation

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Team

- **Development**: Kodelink Team
- **Design**: Material Design 3
- **Backend**: Firebase Services
- **Maps**: Google Maps Platform

## Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation in the `/docs` folder

## Future Enhancements

### Phase 4+ Features
- ![Google Maps](https://img.shields.io/badge/Google_Maps-4285F4?style=flat&logo=google-maps&logoColor=white) **Address search** with Google Places API
- ![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=flat&logo=Firebase&logoColor=white) **Push notifications** for ride updates
- ![Payment](https://img.shields.io/badge/Payment-4285F4?style=flat&logo=google-pay&logoColor=white) **Payment integration** with multiple methods
- ![Database](https://img.shields.io/badge/Database-336791?style=flat&logo=firebase&logoColor=white) **Ride history** and rating system
- ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white) **Advanced driver filtering** (distance, rating, vehicle type)
- ![Real-time](https://img.shields.io/badge/Real--time-FF6B6B?style=flat&logo=clock&logoColor=white) **Real-time ETA** calculations
- ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) **In-app messaging** between users
- ![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=flat&logo=material-design&logoColor=white) **Ride sharing** and carpooling features

### Technical Improvements
- ![Database](https://img.shields.io/badge/Database-336791?style=flat&logo=firebase&logoColor=white) **Offline data caching** for better performance
- ![Google Play](https://img.shields.io/badge/Google_Play-414141?style=flat&logo=google-play&logoColor=white) **Background location updates** for continuous tracking
- ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) **Battery optimization** for location services
- ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white) **Enhanced error handling** and retry mechanisms
- ![Performance](https://img.shields.io/badge/Performance-00C851?style=flat&logo=speedometer&logoColor=white) **Performance optimization** for large datasets
- ![Security](https://img.shields.io/badge/Security-FF6B6B?style=flat&logo=security&logoColor=white) **Security enhancements** for user data protection

---

**Glide** - Connecting people, one ride at a time!
