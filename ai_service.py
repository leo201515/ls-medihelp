import os
import requests
import json
import re
from dotenv import load_dotenv

load_dotenv()

HF_API_KEY = os.getenv('HF_API_KEY', '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')
HF_API_URL = "https://router.huggingface.co/hf-inference/models/microsoft/DialoGPT-medium"
BACKUP_HF_API_URL = "https://router.huggingface.co/hf-inference/models/facebook/blenderbot-400M-distill"

_user_settings = {'use_ai': True, 'api_key': ''}

def set_user_settings(use_ai, api_key):
    global _user_settings
    _user_settings['use_ai'] = use_ai if use_ai is not None else True
    _user_settings['api_key'] = api_key

def get_medical_advice(symptoms, language='de'):
    german_words = ['ich', 'habe', 'schmerzen', 'kopfschmerzen', 'fieber', 'husten', 'erkältung', 
                   'bauch', 'hals', 'schmerz', 'weh', 'kalt', 'heiß', 'müde', 'übel', 'nicht',
                   'art', 'und', 'der', 'die', 'ist', 'bin', 'sind']
    symptoms_lower = symptoms.lower()
    is_german = any(word in symptoms_lower for word in german_words) or language == 'de'
    
    api_key = _user_settings.get('api_key') or HF_API_KEY
    use_ai = _user_settings.get('use_ai', True)
    
    if use_ai and api_key:
        try:
            return get_hf_advice(symptoms, is_german, api_key)
        except Exception as e:
            print(f"[INFO] AI API failed, using rule-based: {e}")
    
    return get_rule_based_advice(symptoms, is_german)

def get_hf_advice(symptoms, is_german, api_key):
    """Use Hugging Face free inference API"""
    
    if is_german:
        prompt = f"""Patient: {symptoms}

Arzt: Als medizinischer Assistent empfehle ich folgende allgemeine Maßnahmen:

1. Symptom-Analyse:
"""
    else:
        prompt = f"""Patient: {symptoms}

Doctor: As a medical assistant, I recommend these general measures:

1. Symptom Analysis:
"""
    
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }
    
    payload = {
        "inputs": prompt,
        "parameters": {
            "max_length": 300,
            "temperature": 0.7,
            "top_p": 0.95,
            "do_sample": True,
            "return_full_text": False
        },
        "options": {
            "wait_for_model": True
        }
    }
    
    try:
        # Try primary model
        response = requests.post(HF_API_URL, headers=headers, json=payload, timeout=30)
        
        if response.status_code == 503:
            # Model is loading, try backup
            print(f"[INFO] Primary model loading, trying backup...")
            response = requests.post(BACKUP_HF_API_URL, headers=headers, json=payload, timeout=30)
        
        if response.status_code == 200:
            result = response.json()
            if isinstance(result, list) and len(result) > 0:
                generated_text = result[0].get('generated_text', '')
                if generated_text:
                    # Combine with prompt and format
                    full_advice = prompt + generated_text
                    return format_ai_response(full_advice, is_german)
        
        print(f"[WARN] API returned status {response.status_code}: {response.text[:200]}")
        
    except requests.exceptions.Timeout:
        print(f"[WARN] API request timed out")
    except requests.exceptions.RequestException as e:
        print(f"[WARN] API request failed: {e}")
    except Exception as e:
        print(f"[WARN] Unexpected error: {e}")
    
    # If we get here, API failed
    raise Exception("API call failed")

def format_ai_response(text, is_german):
    """Format the AI response with proper structure"""
    
    # Add disclaimer
    disclaimer_german = "\n\n⚠️ **Wichtiger Hinweis:** Dies ist keine ärztliche Beratung. Bei schweren oder anhaltenden Symptomen konsultieren Sie bitte einen Arzt."
    disclaimer_english = "\n\n⚠️ **Important Notice:** This is not medical advice. For serious or persistent symptoms, please consult a doctor."
    
    # Clean up the text
    formatted = text.strip()
    
    # Add disclaimer
    if is_german:
        if "wichtiger hinweis" not in formatted.lower():
            formatted += disclaimer_german
    else:
        if "important notice" not in formatted.lower():
            formatted += disclaimer_english
    
    return formatted

def get_rule_based_advice(symptoms, is_german):
    """Rule-based medical advice system (completely free, works offline)"""
    
    if is_german:
        return get_german_advice(symptoms.lower())
    else:
        return get_english_advice(symptoms.lower())

def get_german_advice(symptoms):
    """German language medical advice"""
    
    if any(word in symptoms for word in ['kopfschmerz', 'kopf', 'migräne', 'kopfweh', 'kopfschmerzen']):
        advice = """**Symptom-Analyse: Kopfschmerzen**

**Mögliche Ursachen:**
- Stress oder Anspannung
- Schlafmangel
- Dehydrierung
- Augenanstrengung
- Wetteränderungen
- Koffeinentzug

**Empfohlene Medikamente (in Deutschland erhältlich):**
- **Ibuprofen** (z.B. Advil, Dolormin) - 400mg bei Bedarf
- **Paracetamol** (z.B. Ben-u-ron) - 500mg bei Bedarf
- **Aspirin** (Bayer) - 500mg bei Bedarf

**Allgemeine Maßnahmen:**
- Ruhe in einem dunklen, ruhigen Raum
- Ausreichend Wasser trinken (2-3 Liter täglich)
- Entspannungsübungen oder Meditation
- Kalte Kompressen auf die Stirn
- Regelmäßige Pausen bei Bildschirmarbeit

**Hausmittel:**
- Pfefferminztee
- Lavendelöl auf Schläfen massieren
- Frische Luft

**Wann zum Arzt:**
- Starke, plötzliche Kopfschmerzen
- Kopfschmerzen mit Fieber
- Nach Kopfverletzung
- Bei neurologischen Symptomen (Sehstörungen, Sprachstörungen)
- Kopfschmerzen die über Tage schlimmer werden

**⚠️ Wichtiger Hinweis:** Dies ist keine medizinische Beratung. Bei anhaltenden Beschwerden konsultieren Sie bitte einen Arzt.
"""
    
    elif any(word in symptoms for word in ['fieber', 'erhöht', 'temperatur', 'warm', 'heiß', 'fieberhaft']):
        advice = """**Symptom-Analyse: Fieber**

**Mögliche Ursachen:**
- Virusinfektion
- Bakterielle Infektion
- Entzündung
- Körperliche Anstrengung

**Empfohlene Medikamente:**
- **Paracetamol** (Ben-u-ron) - 500mg alle 6 Stunden
- **Ibuprofen** (Advil, Dolormin) - 400mg alle 6-8 Stunden
- Wadenwickel mit lauwarmem Wasser

**Allgemeine Maßnahmen:**
- Viel trinken (Wasser, Tee, Saft) - mindestens 2-3 Liter
- Bettruhe und Ruhe
- Leichte, vitaminreiche Kost
- Regelmäßige Temperaturkontrolle
- Luftige Kleidung

**Wann zum Arzt:**
- Fieber über 39.5°C
- Fieber länger als 3 Tage
- Bei Kindern unter 3 Jahren mit Fieber
- Zusätzliche Symptome wie Atemnot, Hautausschlag

**⚠️ Wichtiger Hinweis:** Dies ist keine medizinische Beratung. Bei anhaltenden Beschwerden konsultieren Sie bitte einen Arzt.
"""
    
    elif any(word in symptoms for word in ['husten', 'hals', 'erkältung', 'schnupfen', 'nase', 'erkältet', 'halsweh']):
        advice = """**Symptom-Analyse: Erkältung/Husten**

**Mögliche Ursachen:**
- Virusinfektion (Erkältung)
- Allergische Reaktion
- Trockene Luft
- Reizstoffe

**Empfohlene Medikamente:**
- **Hustensaft**: Mucosolvan, Bronchicum, Wick
- **Nasenspray**: Otriven, Nasivin, Sinex
- **Halspastillen**: Dolo-Dobendan, Tyrothricin, Neo-Angin
- **Schmerzmittel**: Paracetamol oder Ibuprofen bei Halsschmerzen

**Allgemeine Maßnahmen:**
- Viel Flüssigkeit trinken (Kräutertee, Wasser)
- Dampfinhalation mit Kamille oder Salz
- Hals gurgeln mit Salzwasser
- Ruhe und ausreichend Schlaf
- Raumluft befeuchten
- Vitamin C und Zink

**Hausmittel:**
- Honig-Zwiebel-Saft
- warmer Tee mit Honig und Zitrone
- Hühnersuppe
- Ingwer

**Wann zum Arzt:**
- Husten länger als 2 Wochen
- Atemnot
- Hohes Fieber
- Eitriger Auswurf
- Starke Halsschmerzen

**⚠️ Wichtiger Hinweis:** Dies ist keine medizinische Beratung. Bei anhaltenden Beschwerden konsultatieren Sie bitte einen Arzt.
"""
    
    elif any(word in symptoms for word in ['bauch', 'magen', 'bauchschmerz', 'bauchweh', 'verdau', 'durchfall', 'übel', 'magenschmerz']):
        advice = """**Symptom-Analyse: Magen/Darm-Beschwerden**

**Mögliche Ursachen:**
- Lebensmittelunverträglichkeit
- Magen-Darm-Infektion
- Stress
- Unregelmäßiges Essen
- Zu viel Alkohol oder Kaffee

**Empfohlene Medikamente:**
- **Bei Übelkeit**: Vomex, Cerucal
- **Bei Bauchschmerzen**: Iberogast, Gaviscon
- **Bei Durchfall**: Imodium, Hidrasec
- **Elektrolyte**: Elotrans, Oralpas
- **Probiotika**: Perenterol, Omniflora

**Allgemeine Maßnahmen:**
- Leichte Kost (Zwieback, Reis, Banane, geriebener Apfel)
- Viel trinken (Tee, Wasser, klare Brühe)
- Vermeiden von Kaffee, Alkohol, scharfem Essen
- Ruhe und Entspannung
- Wärme auf den Bauch

**Hausmittel:**
- Pfefferminz- oder Kamillentee
- Fenchel-Anis-Kümmel-Tee
- Geriebener Apfel (lässt braun werden)
- Zwieback oder Toast

**Wann zum Arzt:**
- Starke Schmerzen
- Blut im Stuhl oder Erbrochenem
- Anhaltende Symptome über 3 Tage
- Austrocknungszeichen (zu wenig Urin)

**⚠️ Wichtiger Hinweis:** Dies ist keine medizinische Beratung. Bei anhaltenden Beschwerden konsultieren Sie bitte einen Arzt.
"""
    
    else:
        advice = f"""**Allgemeine Gesundheitsberatung**

**Ihre beschriebenen Symptome:**
{symptoms}

**Allgemeine Empfehlungen:**
1. **Ruhe und Erholung** - Körper braucht oft einfach Ruhe
2. **Ausreichend Flüssigkeit** - 2-3 Liter Wasser täglich
3. **Gesunde Ernährung** - Frisches Obst und Gemüse
4. **Genug Schlaf** - 7-8 Stunden pro Nacht
5. **Stressreduktion** - Entspannung und Bewegung

**Häufig verwendete Medikamente (ohne Rezept):**
- Schmerzen/Fieber: Paracetamol, Ibuprofen
- Erkältung: Nasenspray (Otriven), Hustensaft (Mucosolvan)
- Magenprobleme: Iberogast

**Wann zum Arzt:**
- Symptome länger als 1 Woche
- Starke Schmerzen
- Hohes Fieber
- Plötzliche Verschlechterung

**⚠️ Wichtiger Hinweis:** Dies ist keine medizinische Beratung. Bei unklaren oder ernsthaften Beschwerden konsultatieren Sie bitte einen Arzt.
"""
    
    return advice

def get_english_advice(symptoms):
    """English language medical advice"""
    
    if any(word in symptoms for word in ['headache', 'head', 'migraine', 'head pain']):
        advice = """**Symptom Analysis: Headache**

**Possible Causes:**
- Stress or tension
- Lack of sleep
- Dehydration
- Eye strain
- Weather changes
- Caffeine withdrawal

**Recommended Medications (OTC):**
- **Ibuprofen** (Advil, Motrin) - 400mg as needed
- **Paracetamol** (Tylenol) - 500-1000mg as needed
- **Aspirin** - 325-500mg as needed

**General Measures:**
- Rest in a dark, quiet room
- Drink plenty of water (2-3 liters daily)
- Relaxation exercises or meditation
- Cold compress on forehead
- Regular breaks during screen work

**Home Remedies:**
- Peppermint tea
- Lavender oil on temples
- Fresh air
- Gentle neck massage

**When to See a Doctor:**
- Sudden, severe headache ("thunderclap")
- Headache with fever
- After head injury
- Neurological symptoms (vision problems, speech issues)
- Headaches that worsen over days

**⚠️ Important Notice:** This is not medical advice. For persistent symptoms, please consult a doctor.
"""
    
    elif any(word in symptoms for word in ['fever', 'temperature', 'hot', 'feverish']):
        advice = """**Symptom Analysis: Fever**

**Possible Causes:**
- Viral infection
- Bacterial infection
- Inflammation
- Physical exertion
- Heat exhaustion

**Recommended Medications:**
- **Paracetamol** (Tylenol) - 500-1000mg every 6 hours
- **Ibuprofen** (Advil) - 400mg every 6-8 hours
- Cold compresses on forehead, wrists
- Lukewarm bath (not cold)

**General Measures:**
- Drink plenty of fluids (water, tea, juice) - 2-3 liters
- Bed rest
- Light, vitamin-rich meals (soups, fruits)
- Regular temperature monitoring
- Light clothing

**Warning Signs:**
- Fever over 39.5°C (103°F)
- Fever lasting more than 3 days
- Children under 3 with fever
- Additional symptoms: rash, stiff neck, difficulty breathing

**⚠️ Important Notice:** This is not medical advice. For persistent symptoms, please consult a doctor.
"""
    
    elif any(word in symptoms for word in ['cough', 'cold', 'throat', 'sore throat', 'flu', 'congestion', 'runny nose']):
        advice = """**Symptom Analysis: Cold/Cough**

**Possible Causes:**
- Viral infection (common cold)
- Allergic reaction
- Dry air
- Irritants (smoke, pollution)

**Recommended Medications:**
- **Cough syrup**: Delsym, Robitussin, Mucosolvan
- **Throat lozenges**: Cepacol, Halls, Strepsils
- **Decongestants**: Sudafed, Afrin (nasal spray)
- **Pain relievers**: Paracetamol or Ibuprofen for sore throat

**General Measures:**
- Drink plenty of fluids (warm tea, water, soup)
- Honey with warm water/tea
- Steam inhalation
- Gargling with warm salt water
- Rest and sleep
- Humidify room air

**Natural Remedies:**
- Ginger tea
- Garlic
- Vitamin C and Zinc supplements
- Chicken soup
- Eucalyptus oil

**When to See a Doctor:**
- Cough lasting more than 2 weeks
- Difficulty breathing
- High fever (>38.5°C/101°F)
- Coughing up blood
- Severe throat pain

**⚠️ Important Notice:** This is not medical advice. For persistent symptoms, please consult a doctor.
"""
    
    elif any(word in symptoms for word in ['stomach', 'stomachache', 'stomach pain', 'nausea', 'diarrhea']):
        advice = """**Symptom Analysis: Stomach/Digestive Issues**

**Possible Causes:**
- Food intolerance or allergy
- Stomach/intestinal infection
- Stress and anxiety
- Irritable bowel syndrome (IBS)
- Food poisoning

**Recommended Medications:**
- **For nausea**: Gravol, Pepto-Bismol
- **For stomach pain**: Gaviscon, Maalox
- **For diarrhea**: Imodium
- **Electrolytes**: Pedialyte, Gatorade
- **Probiotics**: Various brands

**General Measures:**
- Light diet (BRAT: Bananas, Rice, Applesauce, Toast)
- Drink plenty (tea, water, clear broth)
- Avoid coffee, alcohol, fatty foods
- Rest and stay warm
- Peppermint or chamomile tea

**Warning Signs:**
- Severe abdominal pain
- Blood in stool or vomit
- Symptoms persisting over 3 days
- Signs of dehydration (dizziness, little urine)
- Unexplained weight loss

**⚠️ Important Notice:** This is not medical advice. For persistent symptoms, please consult a doctor.
"""
    
    else:
        advice = f"""**General Health Consultation**

**Your described symptoms:**
{symptoms}

**General Recommendations:**
1. **Rest and Recovery** - Body often needs simple rest
2. **Adequate Hydration** - 2-3 liters of water daily
3. **Healthy Nutrition** - Fresh fruits and vegetables
4. **Sufficient Sleep** - 7-8 hours per night
5. **Stress Reduction** - Relaxation and exercise

**Common OTC Medications:**
- Pain/Fever: Paracetamol (Tylenol), Ibuprofen (Advil)
- Cold: Decongestants, cough syrup
- Stomach issues: Antacids, Pepto-Bismol

**When to See a Doctor:**
- Symptoms lasting more than 1 week
- Severe pain
- High fever (>39°C/102°F)
- Sudden worsening
- Unexplained weight loss

**⚠️ Important Notice:** This is not medical advice. For unclear or serious symptoms, please consult a doctor.
"""
    
    return advice