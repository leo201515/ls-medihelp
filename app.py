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
    conversation = session.get('conversation', [])
    return render_template('index.html', language=session.get('language', 'en'), conversation=conversation)

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
    messages = [{"role": "user", "content": symptoms}]
    advice = get_medical_advice(messages, language)
    consultation_id = save_consultation(session['session_id'], symptoms, advice)
    
    return jsonify({
        'advice': advice,
        'consultation_id': consultation_id
    })

@app.route('/history')
def history():
    consultations = get_history(session.get('session_id', ''))
    return render_template('history.html', consultations=consultations)

@app.route('/chat', methods=['POST'])
def chat():
    if 'session_id' not in session:
        session['session_id'] = str(uuid.uuid4())

    data = request.get_json()
    message = data.get('message', '').strip()

    if not message:
        return jsonify({'error': 'Please enter a message'}), 400

    if 'conversation' not in session:
        session['conversation'] = []

    session['conversation'].append({"role": "user", "content": message})

    use_ai = session.get('use_ai', True)
    api_key = session.get('api_key', '')
    language = session.get('language', 'de')

    set_user_settings(use_ai, api_key)

    try:
        advice = get_medical_advice(session['conversation'], language)
    except Exception as e:
        return jsonify({'error': str(e)}), 500

    session['conversation'].append({"role": "assistant", "content": advice})
    session.modified = True

    return jsonify({
        'reply': advice
    })

@app.route('/clear-chat', methods=['POST'])
def clear_chat():
    session.pop('conversation', None)
    return jsonify({'success': True})

@app.route('/download-apk')
def download_apk():
    return send_file('MediHelp.apk', as_attachment=True, download_name='MediHelp.apk')

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8084, debug=True)
