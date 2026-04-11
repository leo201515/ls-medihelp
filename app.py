import os
from flask import Flask, render_template, request, jsonify, session
from dotenv import load_dotenv
import uuid

from database import init_db, save_consultation, get_history
from ai_service import get_medical_advice

load_dotenv()

app = Flask(__name__)
app.secret_key = os.getenv('SECRET_KEY', os.urandom(24))

init_db()

@app.route('/')
def index():
    if 'session_id' not in session:
        session['session_id'] = str(uuid.uuid4())
    return render_template('index.html')

@app.route('/consult', methods=['POST'])
def consult():
    data = request.get_json()
    symptoms = data.get('symptoms', '').strip()
    
    if not symptoms:
        return jsonify({'error': 'Bitte beschreiben Sie Ihre Symptome'}), 400
    
    advice = get_medical_advice(symptoms)
    consultation_id = save_consultation(session['session_id'], symptoms, advice)
    
    return jsonify({
        'advice': advice,
        'consultation_id': consultation_id
    })

@app.route('/history')
def history():
    consultations = get_history(session.get('session_id', ''))
    return render_template('history.html', consultations=consultations)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8083, debug=True)
