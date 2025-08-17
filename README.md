# Word Guessing Game for Android

[![GitHub stars](https://img.shields.io/github/stars/Thanu1999/Word-Guessing-Game?style=social)](https://github.com/Thanu1999/Word-Guessing-Game/stargazers)
[![GitHub issues](https://img.shields.io/github/issues/Thanu1999/Word-Guessing-Game)](https://github.com/Thanu1999/Word-Guessing-Game/issues)
[![GitHub license](https://img.shields.io/github/license/Thanu1999/Word-Guessing-Game)](https://github.com/Thanu1999/Word-Guessing-Game/blob/master/LICENSE)

A fun and challenging word guessing game that tests your vocabulary skills. Players guess secret words, earn points, and compete on a global leaderboard.

<p align="center">
  <img src="https://raw.githubusercontent.com/Thanu1999/Word-Guessing-Game/main/screenshots/main.png" width="200" alt="Main Screen">
  <img src="https://raw.githubusercontent.com/Thanu1999/Word-Guessing-Game/main/screenshots/game.png" width="200" alt="Gameplay">
  <img src="https://raw.githubusercontent.com/Thanu1999/Word-Guessing-Game/main/screenshots/leaderboard.png" width="200" alt="Leaderboard">
</p>

## Features

- **Dynamic Word Generation**: Fetches random words from API-Ninjas service
- **Smart Hints System**: 
  - Reveal word length
  - Check letter frequency
  - Get synonym clues
- **Score Tracking**: Points system with penalties and bonuses
- **Global Leaderboard**: Compete with players worldwide
- **User Profiles**: Personalized experience with usernames
- **Offline Handling**: Graceful network error management

## Technical Architecture
📂 app

├── 📄 ApiService.java - Retrofit interface for API calls

├── 📄 GameActivity.java - Core game logic and UI

├── 📄 LeaderboardActivity.java - Displays global high scores

├── 📄 MainActivity.java - Entry point and user setup

├── 📄 Prefs.java - Persistent storage manager

└── 📂 models

├── 📄 RandomWordResponse.java - API response wrapper

└── 📄 ThesaurusResponse.java - Synonym data structure

## Getting Started

### Prerequisites
- Android Studio (latest version)
- Android SDK 24+
- Java 11

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Thanu1999/Word-Guessing-Game.git
2. Open the project in Android Studio

3. Add your API keys:

- Create gradle.properties in root directory

- Add:
   ```bash
   API_NINJAS_KEY="your_api_ninjas_key"
   DREAMLO_PUBLIC_CODE="your_dreamlo_public_code"
   DREAMLO_PRIVATE_CODE="your_dreamlo_private_code"
   Build and run on emulator or device

### Configuration

Add these dependencies to your app/build.gradle:

    dependencies {
        implementation 'com.squareup.retrofit2:retrofit:2.9.0'
        implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
        implementation 'com.squareup.okhttp3:okhttp:4.9.0'
        implementation 'androidx.recyclerview:recyclerview:1.2.1'
    }

## Key Components
### Game Logic
- **Word Validation**: Ensures words have usable synonyms

- **Score Calculation**:
  - Base score: 100 points
  - +50 for correct guess
  - -10 for incorrect guess
  - Hint costs: 5-10 points
- **Attempt Tracking**: Limited guesses per word

### Network Operations
<p align="center">
  <img src="https://raw.githubusercontent.com/Thanu1999/Word-Guessing-Game/main/screenshots/Network_Operations.svg" width="500" alt="Network_Operations">
</p>

### Data Models
**RandomWordResponse**

    public String getWord() {
        if (word != null && !word.isEmpty()) {
            return word.get(0).toLowerCase(); 
        }
        return ""; // Fallback for empty responses
    }
**Leaderboard Integration**

    public String getFormattedTime() {
        return String.format(Locale.US, "%02d:%02d", seconds/60, seconds%60);
    }
## Game Flow
1. **User Setup**: Enter your name on first launch
2. **Game Start**: Begin guessing words with 10 attempts
3. **Hints**: Use points to reveal word length, letters, or synonyms
4. **Scoring**: Earn points for correct guesses, lose points for hints
5. **Leaderboard**: Post your score to global rankings
6. **New Round**: Continue with new words after each success


## Acknowledgements
- API-Ninjas for word generation service
- dreamlo for leaderboard services
- Retrofit for network operations
- Gson for JSON parsing

**Note**: This app requires an active internet connection to fetch words and update leaderboards.

[![GitHub stars](https://img.shields.io/github/forks/Thanu1999/Word-Guessing-Game?style=social)](https://github.com/Thanu1999/Word-Guessing-Game/forks)
