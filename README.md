# BudgetBuddy

BudgetBuddy is a comprehensive personal finance and budget tracking Android application designed to help users manage their expenses, set financial goals, and gain insights into their spending habits through visual analytics and AI-powered features.

## 🚀 Features

- **Expense Tracking**: Easily log and categorize your daily expenses.
- **Budget Management**: Set monthly limits for different categories and track your progress in real-time.
- **Visual Analytics**: Interactive pie charts and progress bars to visualize spending patterns using MPAndroidChart.
- **AI Integration**: Leverage advanced AI capabilities (Google Gemini / Anthropic) for financial insights and smart suggestions.
- **Personalized Onboarding**: Tailored setup process to configure categories and user preferences.
- **Streak System**: Stay motivated with usage streaks for consistent tracking.
- **Offline First**: Built with Room database for seamless offline usage and data persistence.
- **Notifications**: Reminders and alerts to keep you on track with your budget.

## 🛠 Tech Stack

- **Language**: Java (Gradle Kotlin DSL for build scripts)
- **UI Framework**: Android AppCompat, Material Design Components
- **Architecture**: MVVM (ViewModel, LiveData)
- **Database**: Room Persistence Library
- **Background Tasks**: WorkManager
- **Networking**: OkHttp
- **Charts**: MPAndroidChart
- **AI SDKs**: Google Gemini AI
- **Dependency Injection/Management**: Version Catalogs (libs.versions.toml)

## 📦 Getting Started

### Prerequisites

- Android Studio Flamingo or newer
- Android SDK 24 (Nougat) or higher
- [Optional] API keys for AI features (Gemini/Anthropic)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/henrystitch-debug/BudgetBuddy.git
   ```
2. Open the project in Android Studio.
3. (Optional) Configure API keys for experimental AI features:
   - Add `ANTHROPIC_API_KEY` (and optionally `GOOGLE_API_KEY`) to `local.properties`.
   - Note: additional code configuration may be required to pass the key into the AI client.
4. Sync the project with Gradle files.
5. Run the app on an emulator or a physical device.

## 📂 Project Structure

- `ui/`: Contains UI components organized by feature (overview, budget, settings, onboarding).
- `database/`: Room database entities, DAOs, and configuration.
- `models/`: ViewModels and data models.
- `api/`: AI and networking service implementations.
- `utils/`: Helper classes for colors, time, and common utilities.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📜 License

License information is not yet included in this repository (add a `LICENSE` file to specify terms).
