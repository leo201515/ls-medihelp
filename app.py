"""
MediHelp - Medical Consultation Application
Copyright (c) 2025 Leo Somaratne. All rights reserved.
"""

"""
MediHelp - Medical Consultation Application
Copyright (c) 2026 Leo Somaratne. All rights reserved.
"""

import os
from flask import Flask, render_template, request, jsonify, session, redirect, url_for, send_file
from werkzeug.utils import secure_filename
from dotenv import load_dotenv
import uuid

from database import init_db, save_consultation, get_history
from ai_service import get_medical_advice, set_user_settings

load_dotenv()

app = Flask(__name__)
app.secret_key = os.getenv('SECRET_KEY', os.urandom(24))
app.config['UPLOAD_FOLDER'] = 'uploads'
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'webp'}

os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)

init_db()

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@app.route('/')
def index():
    if 'session_id' not in session:
        session['session_id'] = str(uuid.uuid4())
    session.setdefault('use_ai', True)
    return render_template('index.html', language=session.get('language', 'en'))

@app.route('/settings')
def settings():
    if 'session_id' not in session:
        session['session_id'] = str(uuid.uuid4())
    return render_template('settings.html',
        use_ai=session.get('use_ai', True),
        api_key='',
        language=session.get('language', 'en'))

@app.route('/save-settings', methods=['POST'])
def save_settings():
    data = request.get_json()
    session['use_ai'] = data.get('use_ai', False)
    session['api_key'] = data.get('api_key', '')
    session['language'] = data.get('language', 'de')
    session.permanent = True
    return jsonify({'success': True})

@app.route('/consult', methods=['POST'])
def consult():
    if 'session_id' not in session:
        session['session_id'] = str(uuid.uuid4())
    symptoms = request.form.get('symptoms', '').strip()
    photo_path = None
    
    if not symptoms:
        return jsonify({'error': 'Bitte beschreiben Sie Ihre Symptome'}), 400
    
    if 'photo' in request.files:
        file = request.files['photo']
        if file and file.filename and allowed_file(file.filename):
            filename = secure_filename(f"{uuid.uuid4()}_{file.filename}")
            filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
            file.save(filepath)
            photo_path = filepath
    
    use_ai = session.get('use_ai', True)
    api_key = session.get('api_key', '')
    language = session.get('language', 'de')
    
    set_user_settings(use_ai, api_key)
    advice = get_medical_advice(symptoms, language)
    consultation_id = save_consultation(session['session_id'], symptoms, advice)
    
    return jsonify({
        'advice': advice,
        'consultation_id': consultation_id
    })

@app.route('/history')
def history():
    consultations = get_history(session.get('session_id', ''))
    return render_template('history.html', consultations=consultations)

@app.route('/download-apk')
def download_apk():
    return send_file('MediHelp.apk', as_attachment=True, download_name='MediHelp.apk')

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8084, debug=True)
