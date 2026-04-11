# Medical Help Web App - Specification

## 1. Project Overview

**Project Name:** MediHelp  
**Type:** Web Application (Flask + SQLite + HTML)  
**Core Functionality:** User describes medical symptoms/problems, receives AI-generated advice including German-available medicines and general recommendations.  
**Target Users:** German-speaking users seeking general medical information

## 2. Technical Stack

- **Backend:** Python 3 with Flask
- **Database:** SQLite (stores consultation history)
- **Frontend:** HTML templates with minimal CSS (Bootstrap 5)
- **AI Integration:** OpenAI API (configurable)
- **Port:** 8083

## 3. Database Schema

### Table: consultations
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER PRIMARY KEY | Auto-increment ID |
| symptoms | TEXT | User's described symptoms |
| ai_response | TEXT | AI's response |
| created_at | TIMESTAMP | Creation time |

## 4. Features

### 4.1 Home Page
- Clean, medical-themed interface
- Form to enter symptoms/medical problems
- Text area for detailed description
- Submit button

### 4.2 AI Consultation
- User enters symptoms in German or English
- Backend sends to OpenAI API with prompt engineering for:
  - German-approved medicine names (exact brand names)
  - General recommendations (rest, hydration, when to see doctor)
  - Clear disclaimer that this is not medical advice
- Response stored in SQLite
- Response displayed to user

### 4.3 Disclaimer
- Prominent disclaimer that app provides general information, not medical advice
- Recommendation to consult a real doctor for proper diagnosis

### 4.4 History Page
- View past consultations from same session/browser
- Stored in SQLite, linked by session ID

## 5. API Design

### POST /consult
- Request: `{ "symptoms": "string" }`
- Response: `{ "advice": "string", "consultation_id": int }`

### GET /history
- Response: List of past consultations

## 6. Configuration

Environment variables:
- `OPENAI_API_KEY` - OpenAI API key (required)
- `SECRET_KEY` - Flask secret key

## 7. File Structure

```
/home/leo/opencode/medicel_help/
├── app.py              # Flask application
├── database.py         # SQLite operations
├── ai_service.py       # OpenAI integration
├── templates/
│   ├── base.html       # Base template
│   ├── index.html      # Home/consultation page
│   └── history.html    # History page
├── static/
│   └── style.css       # Custom styles
├── requirements.txt    # Dependencies
└── SPEC.md            # This file
```

## 8. AI Prompt Strategy

System prompt instructs AI to:
1. Respond in the same language as the query
2. List only medicines available in Germany with exact names
3. Include general wellness recommendations
4. Always include medical disclaimer
5. Recommend seeing a doctor if symptoms are severe
