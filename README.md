# MediHelp - Free Medical Consultation App

A medical consultation app with AI-powered symptom analysis - **100% FREE, no paid APIs required!**

## 🆓 Free AI Options

This app works completely **FREE** with two options:

### Option 1: No API Key (Rule-Based System)
- **Works immediately** - no configuration needed
- Built-in medical knowledge base
- Covers common symptoms in German and English
- No registration or API required

### Option 2: Hugging Face Free API (Optional)
- Get **FREE** API key: https://huggingface.co/settings/tokens
- Uses AI model for more dynamic responses
- Free tier: Limited requests per month
- Add to `.env` file: `HF_API_KEY=your_key_here`

## Features

✨ **Web App** (Flask)
- Beautiful modern medical-themed UI
- Photo upload functionality
- Multi-language support (German/English)
- Consultation history
- Responsive design

📱 **Android App**
- Material Design 3
- Photo capture from camera/gallery
- Local database storage
- Age-based advice
- Multiple languages

🏥 **Medical AI**
- Symptom analysis
- Medication recommendations
- When-to-see-doctor guidance
- German pharmacy products
- Completely free - works without API keys!

## Quick Start

### Web App

```bash
# Install dependencies (if not already installed)
pip3 install --break-system-packages flask python-dotenv requests

# Run the app
python3 app.py

# Open in browser
http://localhost:8083
```

### Android App
- Install APK: `MediHelp.apk`
- No configuration needed
- Works offline!

## How It Works

1. **Enter symptoms** in German or English
2. **Get instant advice** from AI or knowledge base
3. **See recommendations**:
   - Symptom analysis
   - OTC medications
   - Home remedies
   - When to see doctor

## Supported Symptoms

- 😣 Headaches/Migraines
- 🤒 Fever
- 🤧 Cold/Flu
- 🤢 Stomach issues
- 😴 Sleep problems
- And more!

## Supported Languages

- 🇩🇪 Deutsch (German)
- 🇬🇧 English
- 🇳🇱 Nederlands (Dutch)
- 🇫🇷 Français (French)
- 🇪🇸 Español (Spanish)
- 🇱🇰 සිංහල (Sinhala)

## Tech Stack

- **Backend**: Python Flask
- **Frontend**: HTML/CSS/JavaScript, Bootstrap 5
- **Mobile**: Java, Material Design, Room Database
- **AI**: Hugging Face API (optional) + Rule-based fallback

## Configuration (Optional)

Create `.env` file to use Hugging Face AI:

```bash
# Optional: Get free key at https://huggingface.co/settings/tokens
HF_API_KEY=hf_xxxxxxxxxxxxxxxxxxxxx

# Required: Flask session secret
SECRET_KEY=development-key-change-in-production
```

## Important Medical Disclaimer

⚠️ **This app provides general health information only and is NOT a substitute for professional medical advice.**

Always consult a doctor for:
- Severe symptoms
- Persistent conditions
- Emergency situations
- Serious health concerns

## License

MIT License - Free to use and modify

## Support

For issues or questions, please check the documentation or consult a healthcare professional for medical advice.