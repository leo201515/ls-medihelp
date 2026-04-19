"""
MediHelp - Database Module
Copyright (c) 2025 Leo Somaratne. All rights reserved.
"""

"""
MediHelp - Database Module
Copyright (c) 2026 Leo Somaratne. All rights reserved.
"""

import sqlite3
from datetime import datetime
import os

DATABASE = 'medihelp.db'

def get_db():
    conn = sqlite3.connect(DATABASE, timeout=30.0)
    conn.row_factory = sqlite3.Row
    # Enable WAL mode for better concurrency
    conn.execute('PRAGMA journal_mode=WAL')
    return conn

def init_db():
    if not os.path.exists(DATABASE):
        conn = get_db()
        conn.execute('''
            CREATE TABLE IF NOT EXISTS consultations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id TEXT NOT NULL,
                symptoms TEXT NOT NULL,
                ai_response TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        conn.commit()
        conn.close()

def save_consultation(session_id, symptoms, ai_response):
    conn = get_db()
    try:
        cursor = conn.execute(
            'INSERT INTO consultations (session_id, symptoms, ai_response) VALUES (?, ?, ?)',
            (session_id, symptoms, ai_response)
        )
        conn.commit()
        consultation_id = cursor.lastrowid
    finally:
        conn.close()
    return consultation_id

def get_history(session_id):
    conn = get_db()
    try:
        rows = conn.execute(
            'SELECT * FROM consultations WHERE session_id = ? ORDER BY created_at DESC',
            (session_id,)
        ).fetchall()
        return [dict(row) for row in rows]
    finally:
        conn.close()