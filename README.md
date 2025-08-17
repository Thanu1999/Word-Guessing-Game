Word Guessing Game for Android
https://img.shields.io/github/stars/Thanu1999/Word-Guessing-Game?style=social
https://github.com/Thanu1999/Word-Guessing-Game/workflows/Android%2520CI/badge.svg

A fun and challenging word guessing game that tests your vocabulary skills. Players guess secret words, earn points, and compete on a global leaderboard.

<p align="center"> <img src="https://via.placeholder.com/300x600?text=Game+Screenshot+1" width="200"> <img src="https://via.placeholder.com/300x600?text=Game+Screenshot+2" width="200"> <img src="https://via.placeholder.com/300x600?text=Leaderboard+Screenshot" width="200"> </p>
Features
Dynamic Word Generation: Fetches random words from API-Ninjas service

Smart Hints System:

Reveal word length

Check letter frequency

Get synonym clues

Score Tracking: Points system with penalties and bonuses

Global Leaderboard: Compete with players worldwide

User Profiles: Personalized experience with usernames

Offline Handling: Graceful network error management

Technical Architecture
text
📂 app
├── 📄 ApiService.java        - Retrofit interface for API calls
├── 📄 GameActivity.java      - Core game logic and UI
├── 📄 LeaderboardActivity.java - Displays global high scores
├── 📄 MainActivity.java      - Entry point and user setup
├── 📄 Prefs.java             - Persistent storage manager
└── 📂 models
    ├── 📄 RandomWordResponse.java - API response wrapper
    └── 📄 ThesaurusResponse.java  - Synonym data structure
Getting Started
Prerequisites
Android Studio (latest version)

Android SDK 24+

Java 11

Installation
Clone the repository:

bash
git clone https://github.com/Thanu1999/Word-Guessing-Game.git
Open the project in Android Studio

Add your API keys:

Create gradle.properties in root directory

Add:

text
API_NINJAS_KEY="your_api_ninjas_key"
DREAMLO_PUBLIC_CODE="your_dreamlo_public_code"
DREAMLO_PRIVATE_CODE="your_dreamlo_private_code"
Build and run on emulator or device

Configuration
Add these dependencies to your app/build.gradle:

gradle
dependencies {
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.9.0'
    implementation 'androidx.recyclerview:recyclerview:1.2.1'
}
Key Components
Game Logic
Word Validation: Ensures words have usable synonyms

Score Calculation:

Base score: 100 points

+50 for correct guess

-10 for incorrect guess

Hint costs: 5-10 points

Attempt Tracking: Limited guesses per word

Network Operations
Diagram
Code
sequenceDiagram
    GameActivity->>+API-Ninjas: GET /randomword
    API-Ninjas->>-GameActivity: Random word
    GameActivity->>+API-Ninjas: GET /thesaurus?word={word}
    API-Ninjas->>-GameActivity: Synonyms
    GameActivity->>+Dreamlo: POST score
    Dreamlo->>-GameActivity: Success
Data Models
RandomWordResponse

java
public String getWord() {
    if (word != null && !word.isEmpty()) {
        return word.get(0).toLowerCase(); 
    }
    return ""; // Fallback for empty responses
}
Leaderboard Integration

java
public String getFormattedTime() {
    return String.format(Locale.US, "%02d:%02d", seconds/60, seconds%60);
}
Game Flow
User Setup: Enter your name on first launch

Game Start: Begin guessing words with 10 attempts

Hints: Use points to reveal word length, letters, or synonyms

Scoring: Earn points for correct guesses, lose points for hints

Leaderboard: Post your score to global rankings

New Round: Continue with new words after each success

Contributing
Contributions are welcome! Please follow these steps:

Fork the repository

Create your feature branch (git checkout -b feature/amazing-feature)

Commit your changes (git commit -m 'Add some amazing feature')

Push to the branch (git push origin feature/amazing-feature)

Open a pull request

License
Distributed under the MIT License. See LICENSE for more information.

Acknowledgements
API-Ninjas for word generation service

dreamlo for leaderboard services

Retrofit for network operations

Gson for JSON parsing

Note: This app requires an active internet connection to fetch words and update leaderboards.
