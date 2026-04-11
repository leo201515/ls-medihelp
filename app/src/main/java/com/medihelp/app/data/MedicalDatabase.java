package com.medihelp.app.data;

import com.medihelp.app.model.MedicalCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MedicalDatabase {
    private static List<MedicalCondition> conditions;
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");

    private static final String[] PAIN_WORDS = {"pain", "ache", "schmerz", "hurts", "sore", "tender", "painful", "hurting", "schmerzen", "schmerzt"};
    private static final String[] BODY_PARTS = {"head", "back", "leg", "legs", "arm", "hand", "foot", "feet", "knee", "elbow", "shoulder", "neck", "stomach", "chest", "eye", "ear", "tooth", "toothache", "finger", "toe", "hip", "ankle", "wrist", "jaw", "throat", "belly", "abdomen", "muscle", "muscles", "joint", "joints", "bone", "bones", "skin", "lung", "heart", "gut", "bladder", "calf", "thigh", "shin", "heel", "toes", "fingers", "palm", "spine", "lumbar"};
    private static final String[] SYMPTOM_WORDS = {"fever", "cold", "cough", "sneezing", "rash", "swelling", "swollen", "nausea", "vomiting", "diarrhea", "headache", "dizziness", "fatigue", "tired", "exhausted", "runny nose", "sore throat", "chills", "sweating", "appetite loss", "weight loss", "insomnia", "anxiety", "depression", "weakness", "numbness", "tingling", "stiffness", "stiff", "bruise", "bruising", "cramp", "cramping"};

    private static final String[] TIME_WORDS = {"yesterday", "today", "tomorrow", "last week", "this week", "next week", "last month", "this month", "next month", "last year", "this year", "ago", "since", "until", "from", "to", "days", "day", "weeks", "week", "months", "month", "years", "year", "hours", "hour", "minutes", "minute", "morning", "afternoon", "evening", "night", "now", "later", "earlier", "before", "after", "during", "when", "time", "once", "twice", "times", "just", "still", "already", "yet", "recently", "suddenly", "immediately", "慢", "急诊", "立即", "马上", "赶快", "最近", "昨天", "今天", "明天", "上周", "下周", "时间"};

    private static String cleanInput(String input) {
        String cleaned = input.toLowerCase(Locale.ROOT);
        for (String time : TIME_WORDS) {
            cleaned = cleaned.replaceAll("(?i)\\b" + time + "\\b", " ");
        }
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        cleaned = cleaned.replace("i have ", "").replace("i feel ", "").replace("my ", "").replace("the ", "").replace("a ", "").replace("an ", "");
        return cleaned;
    }

    public static List<MedicalCondition> getConditions() {
        if (conditions == null) {
            conditions = new ArrayList<>();
            loadConditions();
        }
        return conditions;
    }

    public static MedicalCondition findMatch(String userInput) {
        String input = cleanInput(userInput);
        MedicalCondition bestMatch = null;
        int bestScore = 0;

        for (MedicalCondition condition : getConditions()) {
            int score = calculateMatchScore(input, condition);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = condition;
            }
        }
        return bestScore >= 3 ? bestMatch : null;
    }

    private static int calculateMatchScore(String input, MedicalCondition condition) {
        int score = 0;
        String nameLower = condition.getName().toLowerCase(Locale.ROOT);
        String nameDeLower = condition.getNameDe().toLowerCase(Locale.ROOT);
        String symptomsLower = condition.getSymptoms().toLowerCase(Locale.ROOT);

        if (input.contains(nameLower) || input.equals(nameLower)) score += 15;
        if (input.contains(nameDeLower) || input.equals(nameDeLower)) score += 15;

        boolean hasPain = false;
        boolean hasBodyPart = false;
        boolean hasSymptom = false;

        for (String pain : PAIN_WORDS) {
            if (input.contains(pain)) {
                hasPain = true;
                score += 5;
                break;
            }
        }

        for (String body : BODY_PARTS) {
            if (input.contains(body)) {
                hasBodyPart = true;
                score += 5;
                if (symptomsLower.contains(body)) {
                    score += 8;
                }
                break;
            }
        }

        for (String symptom : SYMPTOM_WORDS) {
            if (input.contains(symptom)) {
                hasSymptom = true;
                score += 4;
                if (symptomsLower.contains(symptom)) {
                    score += 6;
                }
                break;
            }
        }

        for (String word : WORD_PATTERN.split(input)) {
            if (word.length() > 3) {
                if (nameLower.contains(word)) score += 4;
                if (nameDeLower.contains(word)) score += 4;
                if (symptomsLower.contains(word)) score += 3;
            }
        }

        if (hasPain && hasBodyPart) score += 10;
        if (hasPain && hasSymptom) score += 8;

        String[] inputWords = input.split("\\s+");
        for (int i = 0; i < inputWords.length - 1; i++) {
            String twoWords = inputWords[i] + " " + inputWords[i + 1];
            if (symptomsLower.contains(twoWords)) {
                score += 8;
            }
        }

        return score;
    }

    public static String generateAdvice(String symptoms, int age, String language) {
        MedicalCondition match = findMatch(symptoms);
        String ageGroup;
        String medicines;
        String conditionName;
        String disclaimer;
        String noMatch;
        String generalAdvice;

        if (age < 18) {
            if (language.equals("de")) {
                ageGroup = "Kind/Teenager (" + age + " Jahre)";
            } else if (language.equals("nl")) {
                ageGroup = "Kind/Teenager (" + age + " jaar)";
            } else if (language.equals("fr")) {
                ageGroup = "Enfant/Ado (" + age + " ans)";
            } else if (language.equals("es")) {
                ageGroup = "Niño/Adolescente (" + age + " años)";
            } else if (language.equals("si")) {
                ageGroup = "ළමා/දියමන්ති (" + age + " වයස)";
            } else {
                ageGroup = "Child/Teenager (" + age + " years)";
            }
            medicines = match != null ? match.getMedicinesKids() : null;
        } else {
            if (language.equals("de")) {
                ageGroup = "Erwachsener (" + age + " Jahre)";
            } else if (language.equals("nl")) {
                ageGroup = "Volwassene (" + age + " jaar)";
            } else if (language.equals("fr")) {
                ageGroup = "Adulte (" + age + " ans)";
            } else if (language.equals("es")) {
                ageGroup = "Adulto (" + age + " años)";
            } else if (language.equals("si")) {
                ageGroup = "වැඩිහිටි (" + age + " වයස)";
            } else {
                ageGroup = "Adult (" + age + " years)";
            }
            medicines = match != null ? match.getMedicinesAdults() : null;
        }

        if (language.equals("de")) {
            conditionName = "Erkannte Bedingung";
            disclaimer = "⚠️ HAFTUNGSAUSSCHLUSS: Dies ist nur eine allgemeine Information, keine medizinische Beratung. Bitte konsultieren Sie einen Arzt.";
            noMatch = "Ich konnte keine spezifische Erkrankung basierend auf Ihren Symptomen identifizieren.";
            generalAdvice = "- Ruhen Sie sich aus und bleiben Sie hydratisiert\n- Beobachten Sie Ihre Symptome\n- Notieren Sie Veränderungen";
        } else if (language.equals("nl")) {
            conditionName = "Herkende aandoening";
            disclaimer = "⚠️ VRIJWARING: Dit is alleen algemene informatie, geen medisch advies. Raadpleeg altijd een arts.";
            noMatch = "Ik kon geen specifieke aandoening identificeren op basis van uw symptomen.";
            generalAdvice = "- Rust uit en blijf gehydrateerd\n- Observeer uw symptomen\n- Noteer veranderingen";
        } else if (language.equals("fr")) {
            conditionName = "Condition identifiée";
            disclaimer = "⚠️ AVERTISSEMENT: Ceci est uniquement une information générale, pas un conseil médical. Consultez toujours un médecin.";
            noMatch = "Je n'ai pas pu identifier une affection spécifique basée sur vos symptômes.";
            generalAdvice = "- Reposez-vous et restez hydraté\n- Observez vos symptômes\n- Notez les changements";
        } else if (language.equals("es")) {
            conditionName = "Condición identificada";
            disclaimer = "⚠️ ADVERTENCIA: Esto es solo información general, no consejo médico. Consulte siempre a un médico.";
            noMatch = "No pude identificar una condición específica basada en sus síntomas.";
            generalAdvice = "- Descanse y manténgase hidratado\n- Observe sus síntomas\n- Anote los cambios";
        } else if (language.equals("si")) {
            conditionName = "හඳුනාගත් තත්ත්වය";
            disclaimer = "⚠️ අනතුරු අඟවමින්: මෙය සාමාන්‍ය තොරතුරු පමණි, වෛද්‍ය උපදෙස් නොවේ. නිරන්තරයෙන් වෛද්‍යවරයෙකුගෙන් උපදෙස් ලබන්න.";
            noMatch = "ඔබේ රෝග ලක්ෂණ මත පදනම් වූ විශේෂිත තත්ත්වයක් මට හඳුනාගත නොහැක.";
            generalAdvice = "- නිදා ගෙන ජලය බොන්න\n- ඔබේ රෝග ලක්ෂණ නිරීක්ෂණය කරන්න\n- වෙනස්කම් නාටක කරන්න";
        } else {
            conditionName = "Recognized Condition";
            disclaimer = "⚠️ DISCLAIMER: This is only general information, not medical advice. Always consult a doctor.";
            noMatch = "I could not identify a specific condition based on your symptoms.";
            generalAdvice = "- Rest and stay hydrated\n- Monitor your symptoms\n- Note any changes";
        }

        if (match != null) {
            return "🔍 " + conditionName + ": " + match.getNameDe() + " (" + match.getName() + ")\n\n" +
                   "👤 " + (language.equals("de") ? "Altersgruppe" : language.equals("nl") ? "Leeftijdsgroep" : language.equals("fr") ? "Groupe d'âge" : language.equals("es") ? "Grupo de edad" : language.equals("si") ? "වයස කාණ්ඩය" : "Age Group") + ": " + ageGroup + "\n\n" +
                   "💊 " + (language.equals("de") ? "MEDIKAMENTE" : language.equals("nl") ? "MEDICIJNEN" : language.equals("fr") ? "MÉDICAMENTS" : language.equals("es") ? "MEDICAMENTOS" : language.equals("si") ? "ඖෂධ" : "MEDICINES") + ":\n" + medicines + "\n\n" +
                   "📋 " + (language.equals("de") ? "EMPFEHLUNGEN" : language.equals("nl") ? "AANBEVOLEN" : language.equals("fr") ? "RECOMMANDATIONS" : language.equals("es") ? "RECOMENDACIONES" : language.equals("si") ? "නිර්දේශ" : "RECOMMENDATIONS") + ":\n" + match.getAdvice() + "\n\n" +
                   disclaimer;
        }
        return "🤔 " + noMatch + "\n\n" +
               "📝 " + (language.equals("de") ? "IHRE EINGABE" : language.equals("nl") ? "UW INVOER" : language.equals("fr") ? "VOTRE SAISIE" : language.equals("es") ? "SU ENTRADA" : language.equals("si") ? "ඔබේ ආදේශය" : "YOUR INPUT") + ": " + symptoms + "\n" +
               "👤 " + (language.equals("de") ? "ALTER" : language.equals("nl") ? "LEEFTIJD" : language.equals("fr") ? "ÂGE" : language.equals("es") ? "EDAD" : language.equals("si") ? "වයස" : "AGE") + ": " + age + " " + (language.equals("de") ? "Jahre" : language.equals("nl") ? "jaar" : language.equals("fr") ? "ans" : language.equals("es") ? "años" : language.equals("si") ? "වසර" : "years") + "\n\n" +
               "📋 " + (language.equals("de") ? "ALLGEMEINE RATSCHLÄGE" : language.equals("nl") ? "ALGEMENE ADVIES" : language.equals("fr") ? "CONSEILS GÉNÉRAUX" : language.equals("es") ? "CONSEJOS GENERALES" : language.equals("si") ? "සාමාන්‍ය උපදෙස්" : "GENERAL ADVICE") + ":\n" + generalAdvice + "\n\n" +
               (language.equals("de") ? "⚠️ Bitte konsultieren Sie einen Arzt für eine proper Diagnose." : 
                 language.equals("nl") ? "⚠️ Raadpleeg een arts voor een juiste diagnose." :
                 language.equals("fr") ? "⚠️ Veuillez consulter un médecin pour un diagnostic approprié." :
                 language.equals("es") ? "⚠️ Por favor consulte a un médico para un diagnóstico adecuado." :
                 language.equals("si") ? "⚠️ නිවැරදි රෝග නිර්ණයක් සඳහා වෛද්‍යවරයෙකුගෙන් උපදෙස් ලබන්න." :
                 "⚠️ Please consult a doctor for proper diagnosis.");
    }

    private static void loadConditions() {
        conditions.add(new MedicalCondition("1", "Headache", "Kopfschmerzen",
            "headache, head pain, migraine, pressure in head, brain ache, hurts head, head hurts",
            "1. Ruhe in einem ruhigen, dunklen Raum\n2. Ausreichend Wasser trinken (2-3 Liter täglich)\n3. Kalte Kompresse auf die Stirn\n4. Paracetamol oder Ibuprofen bei Bedarf\n5. Bildschirmzeit und helles Licht vermeiden\n6. Bei Migräne: Reizquellen vermeiden\n7. Bei über 3 Tagen oder Verschlechterung zum Arzt",
            "Paracetamol 500mg-1g alle 4-6h (max 4g/Tag)\nIbuprofen 200-400mg alle 6-8h (max 1200mg/Tag)\nAspirin 500mg alle 4-6h (nur Erwachsene)",
            "Paracetamol Saft 10-15mg/kg alle 4-6h (max 60mg/kg/Tag)\nIbuprofen Saft 5-10mg/kg alle 6-8h (max 30mg/kg/Tag)\nKein Aspirin unter 16 Jahren!"));

        conditions.add(new MedicalCondition("2", "Common Cold", "Erkältung",
            "cold, runny nose, sneezing, sore throat, cough, flu-like, verstopfte nase, schnupfen, husten, grippaler infekt",
            "1. Ausruhen und zu Hause bleiben\n2. Viel warme Flüssigkeiten (Tee, Suppe)\n3. Luftbefeuchter verwenden\n4. Mit Salzwasser gurgeln bei Halsschmerzen\n5. Ausreichend Schlaf (7-8 Stunden)\n6. Mund beim Husten bedecken\n7. Meist innerhalb von 7-10 Tagen verbessert",
            "Paracetamol bei Fieber\nNasenspray (Otriven, Iliostin)\nHustensaft (Mucosolvan, Bronchipret)\nBonbons für Halsschmerzen (Dobendan)",
            "Paracetamol Saft nach Gewicht\nNasenspray für Kinder (Otriven Baby)\nHustensaft für Kinder (Mucosolvan Kinder)\nSalzwasser Nasentropfen"));

        conditions.add(new MedicalCondition("3", "Fever", "Fieber",
            "fever, high temperature, chills, body aches, hot skin, feverish, fieber, erhöhte temperatur",
            "1. Ausruhen und Temperatur überwachen\n2. Hydriert bleiben\n3. Bei Temperatur über 38.5°C fiebersenkende Mittel\n4. Leichte Kleidung verwenden\n5. Raumtemperatur angenehm halten\n6. Kalte Kompresse auf Stirn\n7. Bei Fieber über 39.5°C oder über 3 Tage zum Arzt",
            "Paracetamol 500mg-1g alle 4-6h (max 4g/Tag)\nIbuprofen 200-400mg alle 6-8h\nNovaminsulfon 500mg bei Bedarf",
            "Paracetamol Saft 10-15mg/kg alle 4-6h\nIbuprofen Saft 5-10mg/kg alle 6-8h\nWadenwickel bei Fieber über 39°C\nNOTFALL: Bei Säuglingen unter 3 Monaten sofort zum Arzt!"));

        conditions.add(new MedicalCondition("4", "Stomach Pain", "Bauchschmerzen",
            "stomach pain, abdominal pain, belly pain, nausea, stomach ache, bauchschmerzen, übelkeit, magenschmerzen",
            "1. Ausruhen und anfangs keine feste Nahrung\n2. Klare Flüssigkeiten schlückchenweise\n3. Ingwertee gegen Übelkeit\n4. Wärmflasche auf den Bauch\n5. Leichte Schonkost (Zwieback, Reis, Banane)\n6. Keine Milchprodukte, Fett und Koffein\n7. Bei starken Schmerzen sofort zum Arzt",
            "Buscopan bei Krämpfen\nIberogast (pflanzlich)\nMCP Tropfen bei Übelkeit\nOmeprazol bei Sodbrennen\nElektrolytlösung bei Durchfall",
            "Buscopan Dragees für Kinder ab 6 Jahren\nIberogast Tropfen für Kinder\nElektrolytpulver (Oralpädon)\nKeine Buscopan Zäpfchen unter 6 Jahren!\nBei Säuglingen: Arzt konsultieren!"));

        conditions.add(new MedicalCondition("5", "Back Pain", "Rückenschmerzen",
            "back pain, backache, lumbar pain, spine pain, hurts back, leg pain, pain in leg, hurts my leg, painful leg, lower back pain, upper back pain, rücken, kreuzschmerzen, beinschmerzen, schmerzen im bein, my leg hurts, pain leg, schmerz im rücken, schulterschmerzen",
            "1. Aktiv bleiben aber schweres Heben vermeiden\n2. Wärme anwenden (Warmbad, Heizkissen)\n3. Sanfte Dehnungsübungen\n4. Gute Haltung beibehalten\n5. Auf fester Matratze schlafen\n6. Schmerzmittel bei Bedarf\n7. Bei Schmerzausstrahlung in Beine oder Taubheit zum Arzt",
            "Voltarol Gel (Diclofenac)\nParacetamol\nIbuprofen 400mg\nWärmepflaster (Thermacare)\nMuskelrelaxantien bei Verspannungen",
            "Wärmepflaster für Kinder (ab 12 Jahren)\nParacetamol Saft nach Gewicht\nIbuprofen Saft 5-10mg/kg\nKeine Diclofenac/Gel unter 15 Jahren ohne Arzt!"));

        conditions.add(new MedicalCondition("6", "Allergies", "Allergien",
            "allergy, allergic reaction, itching, hives, rash, sneezing, pollen, allergies, allergien, juckreiz, hautausschlag",
            "1. Allergieauslöser identifizieren und vermeiden\n2. Fenster bei hoher Pollenbelastung geschlossen halten\n3. Luftreiniger verwenden\n4. Nach Draußen duschen und Kleidung wechseln\n5. Antihistaminika zur Linderung\n6. Bei schweren Reaktionen (Anaphylaxie) Notaufnahme",
            "Cetirizin (Zyrtec) 10mg 1x täglich\nLoratadin (Claritine) 10mg 1x täglich\nDimetinden (Fenistil) bei Juckreiz\nNasonex Spray bei verstopfter Nase",
            "Cetirizin Tropfen für Kinder ab 2 Jahren\nLoratadin Sirup für Kinder\nFenistil Gel für Kinder\nNasenspray für Kinder\nBEI ATEMNOT: Sofort 112 anrufen!"));

        conditions.add(new MedicalCondition("7", "Insect Bites", "Insektenstiche",
            "insect bite, mosquito bite, bee sting, tick bite, bug bite, sting, Mückenstich, Bienenstich, Zeckenbiss",
            "1. Stelle mit Wasser und Seife reinigen\n2. Kalte Kompresse gegen Schwellung\n3. Juckreizstillende Creme auftragen\n4. Nicht kratzen!\n5. Zecke vorsichtig mit Pinzette entfernen\n6. Stichstelle beobachten\n7. Bei Allergiesymptomen Arzt aufsuchen",
            "Fenistil Gel\nHydrocortison Creme 0.5%\nKühlsalbe\nAntihistaminikum Tabletten\nZeckenzange",
            "Fenistil Gel für Kinder\nHydrocortison Creme 0.5% für Kinder ab 6 Jahren\nKühlgel für Kinder\nAntihistaminikum Tropfen für Kinder\nZeckenkarte für Kinder"));

        conditions.add(new MedicalCondition("8", "Burns", "Verbrennungen",
            "burn, scald, skin burn, thermal burn, sunburn, brandwunde, verbrennung, sonnenbrand",
            "1. Brandstelle unter lauwarmem Wasser 10-20 Min kühlen\n2. KEIN Eis verwenden!\n3. Schmuck/Kleidung vor Schwellung entfernen\n4. Mit steriler, nicht haftender Binde abdecken\n5. KEINE Butter oder Öl auftragen\n6. Schmerzmittel bei Bedarf\n7. Bei schweren Verbrennungen sofort zum Arzt",
            "Brandwunden Gel (Burnshield)\nPanthenol Spray/Creme\nLavanid Salbe\nSchmerzlindernde Gele",
            "Panthenol Creme für Kinder\nBrandwunden Gel für Kinder\nKühlende Gele (kein Eis!)\nBei Säuglingen: Sofort zum Arzt!\nSonnenbrand: Mit kühlem Wasser kühlen, Panthenol"));

        conditions.add(new MedicalCondition("9", "Cuts and Wounds", "Schnittwunden",
            "cut, wound, laceration, scrape, abrasion, blutung, wunde, schnitt, schürfwunde",
            "1. Blutung durch Druck stoppen\n2. Wunde mit sauberem Wasser reinigen\n3. Fremdkörper vorsichtig entfernen\n4. Antiseptikum auftragen\n5. Mit Pflaster/Binde abdecken\n6. Täglich verbinden\n7. Bei tiefen Wunden oder Tetanus-Überprüfung zum Arzt",
            "Bepanthen Antiseptische Wundcreme\nOctenisept Lösung\nHansaplast Wundspray\nJodhaltige Lösungen\nPflaster und Verbände",
            "Bepanthen Wundsalbe für Kinder\nOctenisept Lösung für Kinder\nPflaster für Kinder (bunte)\nKeine Jodtinktur bei Kleinkindern!\nBei starken Blutungen: 112 rufen!"));

        conditions.add(new MedicalCondition("10", "Diarrhea", "Durchfall",
            "diarrhea, loose stools, stomach flu, gastroenteritis, dünndarm, durchfall",
            "1. Hydriert bleiben mit Elektrolytlösungen\n2. Schonkost (Banane, Reis, Zwieback)\n3. Keine Milchprodukte, Fett und Koffein\n4. Probiotika können helfen\n5. Zu Hause ausruhen\n6. Gute Hygiene beachten\n7. Bei Blut im Stuhl oder über 3 Tage zum Arzt",
            "Imodium (Loperamid) 2mg nach jedem Stuhl\nPerenterol (Saccharomyces)\nElektrolyt Pulver (Oralpädon)\nKohletabletten",
            "Imodium Saft für Kinder ab 2 Jahren\nElektrolytpulver für Kinder (Oralpädon)\nPerenterol für Kinder\nSchonkost: Banane, Reis, Zwieback\nBEI SÄUGLINGEN: Sofort zum Arzt!"));

        conditions.add(new MedicalCondition("11", "Sore Throat", "Halsschmerzen",
            "sore throat, throat pain, pain swallowing, pharyngitis, tonsillitis, halsschmerzen, mandelntzündung",
            "1. Mit warmem Salzwasser mehrmals täglich gurgeln\n2. Warme Flüssigkeiten trinken (Honigtee)\n3. Lutschtabletten verwenden\n4. Stimme schonen\n5. Luft befeuchten\n6. Nicht rauchen und Reizstoffe vermeiden\n7. Bei Atemnot oder hohem Fieber zum Arzt",
            "Dobendan Direkt (Dicloniclinch)\nLemocin Bonbons\nTantum Verde Spray\nIsla Moos Pastillen\nBei Bakterieller Mandelntzündung: Antibiotika vom Arzt",
            "Dobendan für Kinder ab 6 Jahren\nLemocin Junior für Kinder\nTantum Verde für Kinder ab 3 Jahren\nIsla Moos für Kinder ab 4 Jahren\nHonig für Kinder ab 1 Jahr\nBEI ATEMNOT: Sofort 112!"));

        conditions.add(new MedicalCondition("12", "Ear Pain", "Ohrenschmerzen",
            "ear pain, ear infection, hurts ear, earache, otitis, ohrenschmerzen, mittelohrentzündung",
            "1. Ohr vorsichtig untersuchen\n2. Warme Kompresse auf das Ohr\n3. Nasentropfen können helfen\n4. Schmerzmittel geben\n5. Nicht ins Ohr stecken\n6. Bei Fieber oder Eiterausfluß zum Arzt\n7. Bei Kindern besonders auf Ohrenschmerzen achten",
            "Otovowen Tropfen\nNasenspray\nIbuprofen bei Schmerzen\nParacetamol bei Fieber\nBEI VERMUTETER INFEKTION: Sofort zum Arzt!",
            "Otovowen für Kinder\nParacetamol/Ibuprofen Saft nach Gewicht\nNasentropfen für Babies\nNIEMALS Ohrstäbchen verwenden!\nBEI FIEBER + OHRENSCHMERZEN: Sofort zum Arzt!"));

        conditions.add(new MedicalCondition("13", "Dental Pain", "Zahnschmerzen",
            "tooth pain, toothache, dental, cavity, hurt tooth, zahnschmerzen, zahn, karies",
            "1. Mund mit warmem Wasser spülen\n2. Kühlkompresse auf Wange\n3. Zahnschmerztropfen verwenden\n4. Schmerzmittel\n5. Zucker vermeiden\n6. Zahnarzt aufsuchen bei: anhaltenden Schmerzen, geschwollenem Zahnfleisch, Eiter oder Fieber",
            "Dentanox Gel\nIbuprofen 400mg\nParacetamol 500mg\nZahnarzt Notdienst: 01805 986700",
            "Paracetamol/Ibuprofen Saft nach Gewicht\nKühlkompresse auf Wange\nSpülen mit lauwarmem Wasser\nZahnarzt aufsuchen!\nNIEMALS Aspirin direkt auf Zahnfleisch!"));

        conditions.add(new MedicalCondition("14", "Skin Rash", "Hautausschlag",
            "rash, skin rash, eczema, dermatitis, red skin, hives, eczema, hautausschlag, neurodermitis",
            "1. Auslöser identifizieren und vermeiden\n2. Betroffene Stelle sauber und trocken halten\n3. Milde, parfümfreie Seife verwenden\n4. Regelmäßig Feuchtigkeitscreme\n5. Kühle Kompressen gegen Juckreiz\n6. Hydrocortison kann helfen\n7. Bei großflächigem Ausschlag zum Dermatologen",
            "Hydrocortison Creme 0.5%\nFenistil Gel\nBepanthen\nLinola Fett Creme\nBei Ekzem: Elocumar Creme vom Arzt",
            "Hydrocortison 0.5% für Kinder ab 6 Jahren\nFenistil Gel für Kinder\nBepanthen für Kinder\nBei Säuglingen: Arzt konsultieren!\nWindeln-Ausschlag: Penaten Creme"));

        conditions.add(new MedicalCondition("15", "Joint Pain", "Gelenkschmerzen",
            "joint pain, arthralgia, knee pain, elbow pain, shoulder pain, swollen joint, leg pain, hip pain, ankle pain, pain in knee, hurts knee, hurts leg, painful joints, ache in joints, gelenkschmerzen, knieschmerzen, hüftschmerzen, fußschmerzen, schmerzen im gelenk, schmerzen im knie, schmerzen in den beinen",
            "1. Betroffenes Gelenk schonen\n2. Bei akuten Schmerzen: Eis auflegen\n3. Bei chronischer Steifheit: Wärme\n4. Entzündungshemmende Medikamente\n5. Gesundes Gewicht halten\n6. Sanfte Bewegungsübungen\n7. Bei Schwellung, Rötung oder Erwärmung zum Arzt",
            "Ibuprofen Gel\nVoltaren\nDiclofenac\nBei Bedarf: Physiotherapie",
            "Ibuprofen Gel ab 14 Jahren\nParacetamol/Ibuprofen Saft\nWärme für chronische Schmerzen\nKühlen für akute Schwellung\nBEI GELENKSCHWELLUNG + FIEBER: Sofort zum Arzt!"));

        conditions.add(new MedicalCondition("16", "Eye Irritation", "Augenreizung",
            "eye irritation, red eye, itchy eyes, conjunctivitis, pink eye, watery eye, augenreizung, bindehautentzündung",
            "1. Hände vor dem Berühren waschen\n2. Auge mit sauberem Wasser spülen\n3. Kontaktlinsen bis zur Heilung rausnehmen\n4. NICHT am Auge reiben!\n5. Künstliche Tränen bei Trockenheit\n6. Kühle Kompresse\n7. Bei Eiter, Sehstörungen oder Verschlechterung zum Augenarzt",
            "Systane Augentropfen\nArtelac EDO\nVisine\nAugentrost Drosinula",
            "Künstliche Tränen für Kinder\nArtelac EDO für Kinder\nWattebausch mit abgekochtem Wasser reinigen\nKEINE Augentropfen ohne Arzt bei Säuglingen!\nBEI GELBER EITER: Sofort zum Arzt!"));

        conditions.add(new MedicalCondition("17", "Cough", "Husten",
            "cough, dry cough, wet cough, chest cough, bronchitis, hacking, husten, bronchitis, reizhusten",
            "1. Ausreichend trinken\n2. Hustenstiller bei trockenem Husten\n3. Schleimlöser bei produktivem Husten\n4. Honig (ab 1 Jahr)\n5. Thymian- oder Eukalyptustee\n6. Luft befeuchten\n7. Bei Atemnot oder über 3 Wochen zum Arzt",
            "Silomat (Dextromethorphan) bei Reizhusten\nMucosolvan (Ambroxol) bei Verschleimung\nACC Akut (Acetylcystein)\nBronchipret Saft",
            "Monapax Saft für Kinder\nMucosolvan Kinderhustensaft\nACC Junior für Kinder\nBronchipret für Kinder\nHonig 1TL vor Schlaf (ab 1 Jahr)\nBEI ATEMNOT: Sofort 112!"));

        conditions.add(new MedicalCondition("18", "Anxiety", "Angststörungen",
            "anxiety, panic, worry, stress, nervousness, anxiety attack, insomnia from worry, angst, panik, stress",
            "1. Tiefe Atemübungen machen\n2. Regelmäßige körperliche Bewegung\n3. Regelmäßigen Schlafplan einhalten\n4. Koffein und Alkohol reduzieren\n5. Mindfulness und Meditation probieren\n6. Mit vertrauenswürdiger Person sprechen\n7. Bei Beeinträchtigung des Alltags: Therapeut suchen",
            "Baldriparan Stark\nRescue Tropfen\nL-Theanin (Teavigo)\nBaldrian + Passionsblume",
            "Rescue Tropfen für Kinder\nBaldrian Dragees für Kinder ab 6 Jahren\nBeruhigungstee (Kindertee)\nElterngespräch und Geborgenheit\nBEI SEVERER ANGST: Kinderpsychologe konsultieren!"));

        conditions.add(new MedicalCondition("19", "Vomiting", "Erbrechen",
            "vomiting, throwing up, nausea, retching, übergeben, erbrechen, übelkeit",
            "1. Ausruhen, Kopf hoch lagern\n2. Erbrechen NICHT unterdrücken\n3. Langsam kleine Mengen trinken\n4. Elektrolytlösung\n5. Schonkost wenn toleriert\n6. Nicht essen bis Magen sich beruhigt\n7. Bei Blut im Erbrochenem oder über 24h zum Arzt",
            "Vomex A Suppositorien\nMCP Tropfen\nElektrolyt Pulver\nIngwertee",
            "Vomex A Zäpfchen für Kinder\nMCP Tropfen für Kinder\nElektrolytpulver für Kinder\nTee mit Zwieback\nBEI SÄUGLINGEN: Sofort zum Arzt!"));

        conditions.add(new MedicalCondition("20", "Constipation", "Verstopfung",
            "constipation, can't poop, hard stool, difficulty bowel, verstopfung, darmträgheit",
            "1. Viel Wasser trinken (1.5-2L)\n2. Ballaststoffreiche Ernährung\n3. Pflaumensaft oder Feigensirup\n4. Regelmäßige Bewegung\n5. Stuhlweichmacher bei Bedarf\n6. Nicht zu lange warten\n7. Bei Blut im Stuhl oder über 1 Woche zum Arzt",
            "Microklist Einlauf\nLactulose Sirup\nMovicol Beutel\nFlohsamenschalen",
            "Lactulose für Kinder (Duphalac)\nGlycerin Zäpfchen für Kinder\nPflaumenmus\nBallaststoffe erhöhen\nBEI SÄUGLINGEN: Arzt konsultieren!"));

        conditions.add(new MedicalCondition("21", "Sunburn", "Sonnenbrand",
            "sunburn, sun burn, red skin from sun, sonnenbrand",
            "1. Sofort aus der Sonne gehen\n2. Kühlen mit lauwarmem Wasser (NICHT eiskalt!)\n3. Panthenol Creme auftragen\n4. Viel trinken\n5. Nicht die Blasen platzen lassen\n6. Lockere Kleidung tragen\n7. Bei schwerem Sonnenbrand zum Arzt",
            "Panthenol Creme/Spray\nAfter Sun Lotion\nKühlende Gele\nIbuprofen bei Schmerzen",
            "Panthenol Creme für Kinder\nAfter Sun für Kinder\nKühlen mit lauwarmem Wasser\nKein Eis!\nBei Säuglingen: SOFORT in Schatten!\nVorbeugung: Sonnencreme LSF 50+"));

        conditions.add(new MedicalCondition("22", "Heat Stroke", "Hitzschlag",
            "heat stroke, overheating, hot body, dehydration, hitzschlag, überhitzung, hitzeerschöpfung",
            "1. Sofort in kühle Umgebung bringen\n2. Kleidung lockern/entfernen\n3. Kühlen mit nassen Tüchern\n4. Wasser zu trinken geben\n5. Puls und Atmung prüfen\n6. NICHT eiskaltes Wasser verwenden\n7. Bei Bewusstlosigkeit: Notruf 112",
            "Elektrolytlösung\nWasser in kleinen Schlucken\nKühle Dusche\nBei Bewusstlosigkeit: 112",
            "Elektrolytpulver für Kinder\nWasser in kleinen Mengen\nKühle (nicht kalte!) Umschläge\nNIEMALS Kind allein im heißen Auto!\nBEI BEWUSSTLOSIGKEIT: 112 SOFORT!"));

        conditions.add(new MedicalCondition("23", "Allergic Reaction", "Allergische Reaktion",
            "allergic reaction, hives, swelling, face swelling, lip swelling, allergische reaktion, nesselsucht",
            "1. Allergieauslöser entfernen\n2. Antihistaminikum geben\n3. Bei Atemnot: Adrenalin-Autoinjektor\n4. Kühle Kompresse auf Schwellungen\n5. Atemwege frei halten\n6. Bei Lippen-/Zungenschwellung oder Atemnot: 112\n7. Leichte Reaktion: 24h beobachten",
            "Cetirizin 10mg\nDimetinden (Fenistil) Tropfen\nCortison bei starker Reaktion\nAdrenalin-Autoinjektor (Epipen) bei Anaphylaxie",
            "Cetirizin Tropfen für Kinder\nFenistil Tropfen für Kinder\nBEI ATEMNOT oder GESICHTSSCHWELLUNG: 112 SOFORT!"));

        conditions.add(new MedicalCondition("24", "Nosebleed", "Nasenbluten",
            "nosebleed, nose bleed, bloody nose, nasal bleeding, nasenbluten",
            "1. Aufrecht hinsetzen, Kopf leicht nach vorne\n2. Nasenflügel 10 Min zusammendrücken\n3. NICHT schneuzen!\n4. Kalte Kompresse im Nacken\n5. Nasenspray bei häufigem Nasenbluten\n6. Luftfeuchtigkeit erhöhen\n7. Bei über 20 Min or häufig: Arzt",
            "Nasivin Nasenspray\nEisen bei häufigem Nasenbluten\nNasentamponaden vom Arzt",
            "Nasenflügel zusammendrücken 10 Min\nKind aufrecht halten\nNICHT hinlegen!\nKalte Kompresse im Nacken\nBei häufigem Nasenbluten: Arzt"));

        conditions.add(new MedicalCondition("25", "Sprain", "Verstauchung",
            "sprain, twisted ankle, twisted wrist, ankle pain after fall, verstauchung, verstaucht, umgeknickt",
            "1. PECH-Regel: Pause, Eis, Compression, Hochlagern\n2. Betroffene Stelle nicht belasten\n3. Eis in Tuch gewickelt 20 Min auflegen\n4. Elastische Binde wickeln\n5. Hochlagern\n6. Schmerzmittel\n7. Bei Unfähigkeit zu belasten oder starker Schwellung: Röntgen",
            "Voltarol Gel\nIbu 400mg\nSport bandages\nElastische Binden",
            "Kühlen mit Eis (in Tuch!)\nElastische Binde\nHochlagern\nParacetamol/Ibuprofen Saft\nBEI UNFÄHIGKEIT ZU BELASTEN: Arzt/Röntgen!"));

        conditions.add(new MedicalCondition("26", "Foreign Body", "Fremdkörper",
            "foreign body, swallowed, choking, swallowed object, choking, verschluckt, fremdkörper, ersticken",
            "1. Bei Verschlucken: NICHT Erbrechen auslösen\n2. Bei Erstickung: 5x Rückenschläge, dann 5x Heimlich\n3. Bei Fremdkörper im Auge: NICHT reiben, mit Wasser spülen\n4. Ohr/Nase: Nicht selbst entfernen, zum Arzt\n5. Beobachten bei Verschlucken\n6. Bei Atemnot: 112\n7. Kleine Batterien/Zeug sind NOTFALL!",
            "BEI ERSTICKUNG: 112\nHeimlich-Manöver\nBei Augenfremdkörper: Augenspüllösung\nBei Ohr/Nase: HNO-Arzt",
            "BEI ERSTICKUNG: 112 SOFORT!\nRückenklopfer bei Baby\nHeimlich bei Kindern\nNIEMALS Finger oder Werkzeug benutzen!\nBEI VERSCHLUCKTEN GEGENSTÄNDEN: Röntgen und Arzt!"));

        conditions.add(new MedicalCondition("27", "Urinary Tract Infection", "Harnwegsinfekt",
            "urinary tract infection, uti, burning pee, pain when urinating, cystitis, harnwegsinfekt, blasenentzündung",
            "1. Viel Wasser trinken (2-3L)\n2. Keine koffeinhaltigen Getränke\n3. Blasen-/Nierentee\n4. Wärme auf Unterleib\n5. Nicht zurückhalten\n6. Bei Fieber oder Rückenschmerzen SOFORT zum Arzt\n7. Antibiotika oft notwendig",
            "Canephron\nCystorenal\nIbuprofen bei Schmerzen\nCranberry Kapseln\nWICHTIG: Bei Fieber + Rückenschmerzen = NIERENBECKENENTZÜNDUNG = NOTFALL!",
            "Viel Wasser trinken\nCranberry Saft ohne Zucker\nWärme auf Unterleib\nBEI FIEBER oder BABIES: Sofort zum Arzt!\nAntibiotika fast immer notwendig bei Kindern"));

        conditions.add(new MedicalCondition("28", "Hand Foot Mouth", "Hand-Fuß-Mund",
            "hand foot mouth, hfmd, bläschen, mouth sores, hand foot mouth disease, hand-fuß-mund",
            "1. Ansteckend! Hände waschen\n2. Schmerzmittel bei Mundgeschwüren\n3. Kühle Getränke und Speisen\n4. Nicht kratzen an Bläschen\n5. Isolation bis alle Bläschen verheilt\n6. Fieber beobachten\n7. Bei Austrocknung oder hohem Fieber zum Arzt",
            "Dolormin Saft\nMundgel (z.B. Oralieve)\nKühlschrankgetränke\nBei Austrocknung: Elektrolytlösung",
            "Paracetamol/Ibuprofen Saft\nKühle Getränke (kein Saft!)\nWeiche Schonkost\nMundgel für Kinder\nHände waschen!\nAnsteckend bis alle Bläschen verheilt!\nBEI FIEBER ÜBER 3 TAGE: Arzt!"));

        conditions.add(new MedicalCondition("29", "Scarlet Fever", "Scharlach",
            "scarlet fever, scharlach, strawberry tongue, red tongue, sandpaper rash, himbeerzunge",
            "1. SOFORT zum Arzt! (Antibiotika notwendig!)\n2. Penicillin oder Amoxicillin\n3. Bettruhe während Fieber\n4. Viel trinken\n5. Weiche Nahrung bei Halsschmerzen\n6. Kontrollieren nach 48h Antibiotika\n7. Kind sollte zu Hause bleiben bis 24h nach Antibiotika",
            "Penicillin V\nAmoxicillin\nIbuprofen bei Schmerzen\nFiebersaft\nScharlach ist meldepflichtig!",
            "Amoxicillin Saft für Kinder\nParacetamol/Ibuprofen Saft\nBEI SCHARLACH: IMMER zum Arzt!\nAntibiotika sind PFLICHT!\nAnsteckend bis 24h nach Antibiotikastart!\nFolgeerkrankungen ohne Behandlung möglich!"));

        conditions.add(new MedicalCondition("30", "Fifth Disease", "Erythema infectiosum",
            "fifth disease, slapped cheek, erythema infectiosum, wangenröte, ringelröteln",
            "1. Meist harmlos, kein Treatment nötig\n2. Bei Fieber: Fiebersaft\n3. Kind kann Kita besuchen wenn kein Fieber\n4. Bei Immunschwäche: Arzt informieren\n5. Warmes Wasser für Bläschen\n6. Feuchtigkeitscreme\n7. Bei Gelenkschmerzen: Ibuprofen",
            "Fiebersaft bei Bedarf\nFeuchtigkeitscreme\nIbuprofen bei Gelenkschmerzen\nBei Schwangerschaftskontakt: Arzt!",
            "Paracetamol/Ibuprofen bei Bedarf\nFeuchtigkeitscreme\nBei Schwangerschaftskontakt: SOFORT zum Frauenarzt!\nBei gesunden Kindern: Meist harmlos\nBei Anämie oder Immunschwäche: Arzt!"));

        conditions.add(new MedicalCondition("31", "Chickenpox", "Windpocken",
            "chickenpox, varicella, spots, juckreiz, windpocken, wasserpocken, juckende bläschen",
            "1. Nicht kratzen! (Narben!)\n2. Kühlende Gele auf Juckreiz\n3. Klammern bei Kleinkindern\n4. Fiebersaft wenn nötig\n5. Baden in Hafermehlwasser\n6. Lockere Baumwollkleidung\n7. Bei eitrigen Bläschen oder Atemnot: Arzt",
            "Fenistil Gel\nViranlos Sirup (Antihistaminikum)\nCotrim bei eitriger Infektion\nTanidrat Bad\nBei Atemnot: 112",
            "Fenistil Gel für Kinder\nViranlos Saft für Kinder\nParacetamol bei Fieber (KEIN Aspirin!)\nHände short halten!\nBei Säuglingen: Sofort zum Arzt!\nNarben möglich bei Kratzen!\nAnsteckend bis alle Bläschen verkrustet!"));

        conditions.add(new MedicalCondition("32", "Croup", "Krupp",
            "croup, barking cough, seal cough, stridor, krupp, husten, bellend",
            "1. SOFORT NOTFALL wenn Atemnot!\n2. 112 rufen bei Atemnot!\n3. Kalte Luft (Fenster auf)\n4. Aufrecht halten\n5. Beruhigen (Kind weinen verschlimmert!)\n6. Dampfbad im Badezimmer\n7. Bei Heiserkeit + Husten + pfeifendem Atem: Arzt",
            "BEI ATEMNOT: 112 SOFORT!\nDampf aus dem Wasser\nKühle Nachtluft\nInhalation mit Kochsalzlösung\nCortison Zäpfchen vom Arzt",
            "BEI ATEMNOT: 112!\nKühle Luft: Fenster auf, Kind raus\nDampf: Heiße Dusche an, Kind daneben setzen\nKind beruhigen!\nCortison vom Arzt\nEltern: Krupp kann nachts schlimmer werden!\nWIEDERHOLTER KRupp: Asthma abklären lassen!"));

        conditions.add(new MedicalCondition("33", "Meningitis", "Meningitis",
            "meningitis, stiff neck, light sensitivity, fontanelle bulging, meningitis, nackensteife, fontanelle",
            "1. NOTFALL! SOFORT 112!\n2. Bei Verdacht: Lumbalpunktion nötig\n3. Krankenhaus\n4. Antibiotika/Virostatika\n5. Intensivstation möglich\n6. Fieber + Nackensteife = 112!\n7. Bei Baby: Fontanelle wölben = NOTFALL!",
            "BEI VERDACHT: 112 SOFORT!\nMeningitis ist LEBENBEDROHLICH!\nLumbalpunktion zur Diagnose\nAntibiotika im Krankenhaus",
            "BEI VERDACHT: 112 SOFORT!\nKEINE VERSCHIEBUNG!\nSteifer Nacken + Fieber = Meningitis bis das Gegenteil bewiesen\nBei Babys: vorgewölbte Fontanelle = NOTFALL!\nErbrechen + Lichtscheu + Fieber = 112!"));

        conditions.add(new MedicalCondition("34", "Pneumonia", "Lungenentzündung",
            "pneumonia, lung infection, chest pain breathing, pneumonia, bronchitis, lunpgenentzündung, bronchopneumonie",
            "1. Sofort zum Arzt! (Röntgen oft nötig!)\n2. Bei schwer: Krankenhaus\n3. Antibiotika wenn bakteriell\n4. Viel trinken\n5. Ausruhen\n6. Fieber managen\n7. Bei Atemnot: 112",
            "Antibiotika (vom Arzt verordnet)\nIbuprofen bei Schmerzen\nACC wenn verschleimt\nInhalation mit Kochsalzlösung\nBei Atemnot: 112",
            "BEI VERDACHT: Sofort zum Arzt!\nRöntgen meist nötig\nAntibiotika oft notwendig\nAtemnot = 112 SOFORT!\nFieber + Husten + Kurzatmigkeit = Pneumonie\nBei Kleinkindern oft stationär!"));

        conditions.add(new MedicalCondition("35", "Flu", "Grippe",
            "flu, influenza, body aches, high fever, flu, influenza, grippaler infekt, echte grippt",
            "1. Bettruhe!\n2. Viel trinken\n3. Fieber managen\n4. Bei Risikogruppen: Tamiflu in 48h\n5. Gute Händehygiene\n6. Impfung jährlich empfohlen\n7. Bei Atemnot oder Verwirrtheit: 112",
            "Tamiflu (Oseltamivir) 75mg 2x täglich (innerhalb 48h)\nParacetamol bei Fieber\nViel Ruhe und Flüssigkeit\nBei Risikogruppen: Sofort zum Arzt!",
            "BEI VERDACHT: Sofort zum Arzt!\nTamiflu für Kinder (Säuglinge eingeschlossen)\nParacetamol/Ibuprofen\nViel trinken!\nBei Atemnot: 112\nGrippeimpfung für Kinder empfohlen!"));

        conditions.add(new MedicalCondition("36", "COVID-19", "COVID-19",
            "covid, coronavirus, corona, loss of taste, loss of smell, covid-19, SARS-CoV-2, omikron",
            "1. Testen (Schnelltest/PCR)\n2. Isolation bis 48h symptomfrei\n3. Bettruhe\n4. Symptome behandeln (Fieber, Husten)\n5. Bei Atemnot: 112\n6. Impfung schützt vor schwerem Verlauf\n7. Bei Risikopatienten: Arzt kontaktieren",
            "PCR Test\nIsolation\nParacetamol bei Fieber\nBei Atemnot: 112\nPaxlovid für Risikogruppen (vom Arzt)",
            "BEI POSITIVEM TEST:\n- Kinder meist mild\n- Bei Risiko: Sofort zum Arzt!\n- Fieber und Husten behandeln\n- Bei Atemnot: 112\n- Isolation einhalten\n- Ältere und Risikopatienten besonders schützen"));

        conditions.add(new MedicalCondition("37", "Measles", "Masern",
            "measles, masern, rash with fever, koplik spots, masern",
            "1. SOFORT zum Arzt! (Meldepflichtig!)\n2. Isolierung zu Hause\n3. Bettruhe\n4. Fieber behandeln\n5. Dunkler Raum (Licht empfindlich)\n6. Viel trinken\n7. Bei Komplikationen: Krankenhaus",
            "Fiebersaft\nVitamin A (nachgewiesenermaßen hilfreich)\nBei Komplikationen: Antibiotika\nMasern sind MELDEPFLICHTIG!",
            "BEI VERDACHT: Sofort zum Arzt!\nFiebersaft\nVitamin A für Kinder\nDunkler Raum wegen Lichtempfindlichkeit\nMasernimpfung schützt! (Impfpflicht in Deutschland!)\nKomplikationen: Mittelohrentzündung, Lungenentzündung, Gehirnhautentzündung"));

        conditions.add(new MedicalCondition("38", "Whooping Cough", "Keuchhusten",
            "whooping cough, pertussis, coughing fits, keuchhusten, pertussis, stickhusten",
            "1. SOFORT zum Arzt!\n2. Antibiotika (Makrolide)\n3. Isolation zu Hause\n4. Kontrolle der Hustenanfälle\n5. Viel trinken\n6. Kleine Mahlzeiten\n7. Bei Atemnot: 112",
            "Antibiotika (Azithromycin, Clarithromycin)\nSilomat gegen Reizhusten\nVitamin C\nKeuchhusten ist MELDEPFLICHTIG!",
            "BEI VERDACHT: Sofort zum Arzt!\nAntibiotika für Kinder\nSpritzen vom Arzt (Kinder unter 6 Monaten oft im Krankenhaus!)\nKeuchhustenimpfung schützt!\nSehr gefährlich für Säuglinge!\nEltern und enge Kontaktpersonen sollten auch behandelt werden!"));

        conditions.add(new MedicalCondition("39", "Impetigo", "Eiterflechte",
            "impetigo, honey crust, skin infection, bacterial, eiterflechte, bullous",
            "1. Zum Arzt! (Antibiotika meist nötig!)\n2. K切en nicht berühren\n3. Hände waschen\n4. Getrennte Handtücher\n5. Kleidung bei 60°C waschen\n6. Creme oder Antibiotika vom Arzt\n7. Kind zu Hause lassen bis Heilung",
            "Fucidinsäure Creme\nOctenisept Lösung\nBei ausgebreitet: Antibiotika Tabletten\nHände waschen!",
            "BEI VERDACHT: Zum Arzt!\nFucidinsäure für Kinder\nHände waschen!\nAnsteckend bis 24h nach Antibiotikastart oder bis alle Krusten abgeheilt!\nGetrennte Handtücher!\nKratzen verbreitet die Infektion!"));

        conditions.add(new MedicalCondition("40", "Ringworm", "Ringelflechte",
            "ringworm, tinea, fungal infection, circular rash, ringworm, kreisrunde flechte",
            "1. Zum Arzt zur Diagnose\n2. Antimykotische Creme\n3. Nicht kratzen\n4. Hände waschen\n5. Getrennte Handtücher\n6. Bei Haustieren: Tier behandeln lassen\n7. Bei ausgebreitet: Tabletten vom Arzt",
            "Canesten Creme (Clotrimazol)\nMycospor Creme\nBei hartnäckig: Antimykotika Tabletten",
            "BEI VERDACHT: Zum Arzt!\nCanesten für Kinder\nHände waschen!\nAnsteckend über Personen und Tiere!\nHandtücher und Kleidung bei 60°C waschen!\nBei Haustieren: Tierarzt!"));

        conditions.add(new MedicalCondition("41", "Tonsillitis", "Mandelntzündung",
            "tonsillitis, sore throat, swollen tonsils, white spots throat, mandelntzündung, tonsillen",
            "1. Zum Arzt für Diagnose\n2. Bei bakteriell: Antibiotika (Penicillin)\n3. Schonung\n4. Viel trinken\n5. Warme Getränke\n6. Bei Atemnot: 112\n7. Bei häufigen Entzündungen: OP besprechen",
            "Penicillin V 1Mio IE 3x tgl 10 Tage\nIbuprofen bei Schmerzen\nDobendan Lutschtabletten\nBei Allergie: Clindamycin",
            "BEI VERDACHT: Zum Arzt!\nAmoxicillin Saft für Kinder\nParacetamol/Ibuprofen\nBei Säuglingen: Sofort zum Arzt!\nWICHTIG: Antibiotika vollständig nehmen!"));

        conditions.add(new MedicalCondition("42", "Sinusitis", "Nasennebenhöhlenentzündung",
            "sinusitis, sinus infection, face pain, sinus pressure, nasal congestion, nasennebenhöhlen, stirnhöhle",
            "1. Nasenspray mit Kochsalzlösung\n2. Abschwellendes Nasenspray (max 7 Tage)\n3. Schmerzmittel\n4. Inhalation mit Dampf\n5. Wärme auf Gesicht\n6. Bei Fieber über 3 Tage oder Eiter: Arzt\n7. Bei chronisch: HNO-Arzt",
            "Otriven Nasenspray (max 7 Tage)\nIbuprofen 400mg\nParacetamol\nMucosolvan\nNasenspülung mit Kochsalz\nBei eitriger Sinusitis: Antibiotika vom Arzt",
            "Nasenspray für Kinder (Otriven Baby)\nParacetamol/Ibuprofen Saft\nInhalation mit Dampf (Vorsicht Verbrennung!)\nBei Säuglingen: Sofort zum Arzt!\nBei Fieber und eitrigem Nasensekret: Arzt!"));

        conditions.add(new MedicalCondition("43", "Bronchitis", "Bronchitis",
            "bronchitis, chest infection, productive cough, chest congestion, bronchitis, bronchialkatarrh",
            "1. Viel trinken\n2. Schleimlöser\n3. Bei Fieber: Fiebersenker\n4. Ausruhen\n5. Vermeiden: Rauchen, kalte Luft\n6. Bei Atemnot oder Blut im Auswurf: Arzt\n7. Bei Kindern unter 2 Jahren: Arzt",
            "Mucosolvan (Ambroxol) 30mg 3x tgl\nACC Akut 600mg 1x tgl\nBronchipret Saft\nBei Fieber: Paracetamol\nBei bakteriell: Antibiotika",
            "Mucosolvan Kinder Saft\nACC Junior\nBronchipret für Kinder\nParacetamol/Ibuprofen bei Fieber\nBEI ATEMNOT: 112!\nBei Säuglingen und Kleinkindern: Arzt!"));

        conditions.add(new MedicalCondition("44", "Asthma", "Asthma",
            "asthma, wheezing, shortness of breath, asthma attack, breathing difficulty, asthma, pfeifendes atmen",
            "1. Notfallplan kennen!\n2. Sofort Notfallspray anwenden\n3. Aufrecht hinsetzen\n4. Langsam tief atmen\n5. Bei keiner Besserung in 10 Min: 112\n6. Reserve-Inhalator\n7. Regelmäßige Kontrolle beim Lungenfacharzt",
            "Notfallspray: Ventolin (Salbutamol)\nLangzeitmedikation: Foster Inhalator\nMontelukast Tabletten\nKortison Inhalator\nBei Notfall: 112",
            "Notfallspray für Kinder\nBei Kindern unter 6: Inhalator mit Spacer\nBEI SCHWEREM ANFALL: 112 SOFORT!\nKortison Inhalator verschreibungspflichtig!\nEltern sollten einen Asthma-Plan vom Arzt haben!"));

        conditions.add(new MedicalCondition("45", "Gastroenteritis", "Magen-Darm-Grippe",
            "gastroenteritis, stomach flu, vomiting, diarrhea, nausea, magen-darm-grippe, brechdurchfall",
            "1. Viel trinken (Elektrolytlösung)\n2. Schonkost\n3. Nicht selbst Erbrechen unterdrücken\n4. Ausreichend Ruhe\n5. Hände waschen!\n6. Bei Säuglingen: Sofort zum Arzt\n7. Bei Blut im Stuhl: Arzt",
            "Elektrolyt Pulver (Oralpädon)\nVomex A bei Erbrechen\nImodium bei Durchfall\nPerenterol\nSchonkost: Zwieback, Reis, Banane",
            "Elektrolytpulver für Kinder (Oralpädon)\nVomex A Zäpfchen für Kinder\nImodium Saft ab 2 Jahren\nBEI SÄUGLINGEN: Sofort zum Arzt!\nNur abstillen wenn vom Arzt angeordnet!\nSTILLEN fortsetzen so viel wie möglich!"));

        conditions.add(new MedicalCondition("46", "Food Poisoning", "Lebensmittelvergiftung",
            "food poisoning, food infection, contaminated food, salmonella, lebensmittelvergiftung, salmonellen",
            "1. Erbrechen nicht unterdrücken\n2. Elektrolytlösung trinken\n3. Schonkost\n4. Bei starken Beschwerden: Arzt\n5. Gute Hygiene\n6. Verdorbene Lebensmittel entsorgen\n7. Bei Fieber oder Blut: Arzt",
            "Elektrolytlösung\nParacetamol bei Fieber\nVomex A wenn nötig\nBactrim bei Salmonellen (verschreibungspflichtig)\nBei Verdacht auf Botulismus: 112!",
            "Elektrolytpulver für Kinder\nParacetamol bei Fieber\nBEI FIEBER + DURCHFALL + VERDORBENES ESSEN: Arzt!\nBei Säuglingen: IMMER zum Arzt!\nMeldepflichtig bei Massenerkrankungen!"));

        conditions.add(new MedicalCondition("47", "Heartburn", "Sodbrennen",
            "heartburn, acid reflux, GERD, burning chest, sour stomach, sodbrennen, reflux",
            "1. Kleine Mahlzeiten\n2. Nicht hinlegen nach dem Essen\n3. Auslöser vermeiden (Kaffee, Fett, Alkohol)\n4. Gewicht reduzieren\n5. Kopf hochlagern beim Schlafen\n6. Sodbrennen Medikamente\n7. Bei anhaltend über 2 Wochen: Arzt",
            "Omeprazol 20mg 1x täglich\nPantoprazol 20mg 1x täglich\nRennie Kautabletten\nBei Reflux: Schlafen mit erhöhtem Kopfteil",
            "Omeprazol Mups für Kinder ab 1 Jahr\nRennie für Kinder\nBEI SCHLUCKBESCHWERDEN: Arzt!\nLangzeitbehandlung nur mit Arzt!\nBei Kindern: Arzt konsultieren!"));

        conditions.add(new MedicalCondition("48", "Hemorrhoids", "Hämorrhoiden",
            "hemorrhoids, piles, rectal bleeding, itching anus, hämorriden, analbeschwerden",
            "1. Stuhlweichmacher\n2. Sitzbad mit Kamille\n3. Hämorrhoiden Salbe/Zäpfchen\n4. Kein starkes Pressen\n5. Ballaststoffreiche Ernährung\n6. Viel trinken\n7. Bei starker Blutung: Arzt",
            "Faktu Salbe/Zäpfchen\nPosterisan Zäpfchen\nDoloprokt Salbe\nHametum Salbe\nBei starken Beschwerden: Verödung oder OP vom Proktologen",
            "Hämorrhoiden Zäpfchen für Kinder nicht üblich\nBEI BLUTUNGEN: Arzt!\nBallaststoffe und viel trinken\nKind sollte nicht stark pressen müssen\nBei Kindern selten, dann IMMER zum Arzt!"));

        conditions.add(new MedicalCondition("49", "Bladder Infection", "Blasenentzündung",
            "bladder infection, cystitis, burning urination, frequent urination, blase, blasenentzündung, zystitis",
            "1. Viel Wasser trinken (2-3L)\n2. Blasen-/Nierentee\n3. Wärme auf Unterleib\n4. Nicht zurückhalten\n5. Vermeiden: Koffein, Alkohol, scharfes Essen\n6. Bei Fieber oder Rückenschmerzen: SOFORT Arzt\n7. Antibiotika oft notwendig",
            "Canephron\nCystorenal\nIbuprofen bei Schmerzen\nCranberry Kapseln\nBEI FIEBER + RÜCKENSCHMERZEN = NIERENBECKENENTZÜNDUNG = NOTFALL!",
            "Viel Wasser trinken\nCranberry Saft (ungesüßt)\nWärme auf Unterleib\nBEI FIEBER oder BABIES: Sofort zum Arzt!\nAntibiotika fast immer notwendig!\nBei wiederholten Infekten: Arzt!"));

        conditions.add(new MedicalCondition("50", "Kidney Stones", "Nierensteine",
            "kidney stones, kidney pain, renal colic, back pain severe, nierensteine, nierenkolik",
            "1. SOFORT zum Arzt bei starken Schmerzen!\n2. Viel trinken\n3. Schmerzmittel\n4. Wärme\n5. Stein auffangen für Analyse\n6. Bei Fieber: 112\n7. Größe und Position per Ultraschall",
            "Buscopan bei Kolik\nIbuprofen bei Schmerzen\nNovaminsulfon bei starker Kolik\nBei Fieber: Antibiotika + Krankenhaus\nSteinanalayse nach Abgang",
            "BEI STARKEN SCHMERZEN: Sofort zum Arzt!\nParacetamol/Ibuprofen Saft\nViel trinken\nWärme\nBEI FIEBER: 112!\nKinder bekommen selten Nierensteine, dann aber sofort zum Arzt!"));

        conditions.add(new MedicalCondition("51", "Gout", "Gicht",
            "gout, big toe pain, joint swelling, uric acid, gicht, gichtanfall",
            "1. Sofort Arzt für Diagnose\n2. Entzündungshemmende Medikamente\n3. Kühlung\n4. Betroffenes Gelenk hochlagern\n5. Purinarme Ernährung\n6. Medikamente zur Senkung der Harnsäure\n7. Viel Wasser trinken",
            "Colchicin bei Gichtanfall\nIbuprofen 600mg\nCortison bei schwerem Anfall\nAllopurinol zur Langzeittherapie\nBei Erstdiagnose: Arzt!",
            "Gicht bei Kindern sehr selten\nBEI VERDACHT: Sofort zum Arzt!\nMedikamente für Kinder nicht gut erforscht\nKind braucht pädiatrische Betreuung!"));

        conditions.add(new MedicalCondition("52", "Arthritis", "Arthritis",
            "arthritis, joint swelling, joint pain, rheumatic, arthritis, gelenkentzündung, rheumatismus",
            "1. Sofort zum Arzt!\n2. Gelenk schonen\n3. Kühlung bei Entzündung\n4. Schmerzmittel\n5. Physiotherapie\n6. Medikamente vom Rheumatologen\n7. Regelmäßige Kontrollen",
            "Ibuprofen 400-600mg\nBei rheumatoider Arthritis: Biologika vom Rheumatologen\nKortison bei akuten Schüben\nPhysiotherapie\nBei Diagnose: Rheumatologe",
            "BEI VERDACHT: Sofort zum Arzt!\nKinder brauchen pädiatrischen Rheumatologen!\nFrüherkennung wichtig!\nSchmerzmittel altersgerecht\nPhysiotherapie für Kinder spezialisiert"));

        conditions.add(new MedicalCondition("53", "Osteoporosis", "Osteoporose",
            "osteoporosis, bone loss, fragile bones, bone density, osteoporose, knochenbrüchigkeit",
            "1. Knochendichtemessung beim Arzt\n2. Calcium und Vitamin D\n3. Regelmäßige Bewegung\n4. Sturzprävention\n5. Medikamente zur Knochenstärkung\n6. Gewichtstraining\n7. Regelmäßige Kontrollen",
            "Calcium + Vitamin D3 Supplement\nBisphosphonate (Alendronat)\nBei starker Osteoporose: Denosumab\nOrthopäde für Behandlung",
            "Osteoporose bei Kindern: Pädiatrischer Endokrinologe!\nAusreichend Calcium (Milchprodukte, grünes Gemüse)\nVitamin D3 im Winter\nSport und Bewegung\nKnochenbrüche bei Kindern immer abklären lassen!"));

        conditions.add(new MedicalCondition("54", "Sciatica", "Ischias",
            "sciatica, sciatic nerve pain, back pain radiating leg, ischias, hexenschuss",
            "1. Schonung aber nicht Bettruhe\n2. Wärme\n3. Schmerzmittel\n4. Sanfte Bewegung\n5. Bei Taubheit oder Blasen-/Darmproblemen: SOFORT Arzt\n6. Krankengymnastik\n7. Bei anhaltend über 6 Wochen: Arzt",
            "Ibuprofen 600mg\nVoltarol Gel\nWärmepflaster\nBei starker Ischias: Cortison vom Arzt\nKrankengymnastik",
            "BEI VERDACHT: Zum Arzt!\nKinder bekommen selten Ischias\nParacetamol/Ibuprofen kindgerecht\nWärme\nSanfte Bewegung\nBEI TAUBHEIT oder BLASENPROBLEMEN: 112!\nKinder brauchen pädiatrische Abklärung!"));

        conditions.add(new MedicalCondition("55", "Carpal Tunnel", "Karpaltunnelsyndrom",
            "carpal tunnel, hand numbness, tingling fingers, wrist pain, karpaltunnel, handschmerzen",
            "1. Handgelenk schonen\n2. Nachtschiene tragen\n3. Entzündungshemmende Medikamente\n4. Kühlung\n5. Bei Taubheit: Arzt\n6. Physiotherapie\n7. Bei schwer: OP vom Handchirurgen",
            "Ibuprofen Gel\nNachtschiene\nBei starker Entzündung: Cortison-Injektion vom Arzt\nHandchirurg für OP",
            "Karpaltunnel bei Kindern selten\nBEI VERDACHT: Zum Arzt!\nSchiene für Kinder ggf anpassbar\nParacetamol/Ibuprofen Saft\nBei Taubheit: Sofort zum Arzt!"));

        conditions.add(new MedicalCondition("56", "Tennis Elbow", "Tennisarm",
            "tennis elbow, elbow pain, arm pain, tennisarm, sehnenansatzentzündung",
            "1. Ellenbogen schonen\n2. Kühlung\n3. Schmerzgel\n4. Dehnübungen\n5. Physiotherapie\n6. Bei stark: Stoßwellentherapie\n7. OP als letzte Option",
            "Voltarol Gel\nIbuprofen 400mg\nEpicondilitis-Bandage\nKrankengymnastik\nBei hartnäckig: Stoßwellentherapie",
            "BEI VERDACHT: Zum Arzt!\nDehnübungen für Kinder anpassen\nKühlen mit Tuch!\nParacetamol/Ibuprofen Saft\nBei Kindern: Sportpause einlegen\nÜberlastung durch Handy/Tablet beachten!"));

        conditions.add(new MedicalCondition("57", "Plantar Fasciitis", "Plantarsehnenentzündung",
            "plantar fasciitis, heel pain, foot pain, fußschmerzen, plantarfasziitis, fußsohle",
            "1. Barfuß gehen vermeiden\n2. Dehnübungen für Fußsohle\n3. Kühlung\n4. Einlagen vom Orthopäden\n5. Schmerzmittel\n6. Bei anhaltend: Stoßwellentherapie\n7. Gewicht reduzieren",
            "Voltarol Gel\nEinlagen vom Orthopäden\nDehnübungen\nBei starker Entzündung: Cortison-Injektion",
            "BEI VERDACHT: Zum Arzt!\nDehnübungen für Kinder anpassen\nBequeme Schuhe\nParacetamol/Ibuprofen\nBei Kindern: Orthopäde!"));

        conditions.add(new MedicalCondition("58", "Varicose Veins", "Krampfadern",
            "varicose veins, spider veins, leg swelling, heavy legs, krampfadern, besenreiser",
            "1. Lange stehende/sitzende vermeiden\n2. Beine hochlegen\n3. Kompressionsstrümpfe\n4. Bewegung\n5. Bei Schwellung oder Schmerzen: Arzt\n6. Gewicht kontrollieren\n7. Bei schwer: Verödung oder OP",
            "Kompressionsstrümpfe\nVenotonika (Daflon)\nBei Schwellung: Entwässerungsmittel vom Arzt\nHautpflege\nBei Entzündung: Sofort zum Arzt",
            "Krampfadern bei Kindern selten\nBEI VERDACHT: Zum Kinderarzt!\nKompressionsstrümpfe für Kinder special angefertigt\nBei Kindern: Venenchirurg/Pädiatrischer Angiologe"));

        conditions.add(new MedicalCondition("59", "Shingles", "Gürtelrose",
            "shingles, herpes zoster, painful rash, blistered rash, gürtelrose, herpes zoster",
            "1. SOFORT zum Arzt! ( antiviral needed!)\n2. Ausschlag nicht kratzen\n3. Kühlung und Juckreiz lindern\n4. Schmerzmittel\n5. Isolierung (aneckend für Neugeborene!)\n6. Bei Gesichtsbeteiligung: SOFORT Augenarzt\n7. Antivirale Medikamente innerhalb 72h",
            "Valtrex (Valaciclovir) 1000mg 3x tgl\nZostrix Creme\nAciclovir Creme\nBei Schmerzen: Gabapentin\nBEI GESICHT/ÄUGEN: SOFORT 112/Augenarzt!",
            "BEI VERDACHT: Sofort zum Arzt!\nAntivirale Medikamente für Kinder\nSchmerzmittel kindgerecht\nWICHTIG: Neugeborene nicht anstecken!\nBei Augenschmerzen: Sofort Augenarzt!"));

        conditions.add(new MedicalCondition("60", "Cold Sores", "Lippenherpes",
            "cold sores, herpes simplex, lip blisters, fever blisters, lippenherpes, fieberbläschen",
            "1. Cremes mit Aciclovir\n2. Nicht berühren oder kratzen\n3. Hände waschen\n4. Lippenpflege\n5. Auslöser vermeiden (Stress, Sonne)\n6. Bei häufig: virustatische Tabletten\n7. Bei Augenbeteiligung: SOFORT Arzt",
            "Aciclovir Creme\nZovirax Creme\nBei häufig: Valtrex Tabletten\nLippenpflege mit Sonnenschutz\nBei immungeschwächten: Sofort zum Arzt",
            "Aciclovir Creme für Kinder\nBei Kindern meist erster Ausbruch schlimmer\nHände waschen!\nNicht mit Fingern in Augen fassen!\nBei Augenbeteiligung: Sofort zum Arzt!\nBaby unter 6 Wochen: SOFORT zum Arzt!"));

        conditions.add(new MedicalCondition("61", "Acne", "Akne",
            "acne, pimples, skin bumps, blackheads, akne, hautunreinheiten",
            "1. Gesicht 2x täglich waschen\n2. Nicht ausdrücken!\n3. Ölfreie Hautpflege\n4. Bei mild: Benzoylperoxid\n5. Bei mittelschwer: verschreibungspflichtige Medikamente\n6. Hautarzt bei schwerer Akne\n7. Geduld (Behandlung braucht Wochen)",
            "Benzoylperoxid Gel (2-5%)\nAcnefichthol\nBei mittelschwer: Differin Gel (verschreibungspflichtig)\nBei schwer: Roaccutane vom Hautarzt",
            "BEI VERDACHT: Zum Hautarzt!\nKinder sollten Pickel nicht ausdrücken!\nMilde Reinigung\nBenzoylperoxid 2.5% für Kinder ab 12\nHautarzt für stärkere Behandlungen\nMedikamente wie Roaccutane nur unter ärztlicher Kontrolle!"));

        conditions.add(new MedicalCondition("62", "Eczema", "Ekzem",
            "eczema, atopic dermatitis, dry itchy skin, neurodermitis, hautausschlag, ekzem",
            "1. Feuchtigkeitscreme täglich\n2. Kortisoncreme bei Schüben\n3. Nicht kratzen!\n4. Allergieauslöser meiden\n5. Baumwollkleidung\n6. Hautarzt für Behandlungsplan\n7. Bei Infektion: Antibiotika",
            "Linola Fett Creme\nHydrocortison 0.5% (kurzzeitig)\nElocumar Creme (verschreibungspflichtig)\nBei Infektion: Fucidinsäure Creme",
            "BEI VERDACHT: Zum Hautarzt!\nPflegecremes für Kinder\nHydrocortison 0.5% für Kinder\nKratzen verhindern (Händchen stoppen!)\nBei Infektion: Sofort zum Arzt!\nEltern sollten Handschuhe nachts anziehen!"));

        conditions.add(new MedicalCondition("63", "Psoriasis", "Schuppenflechte",
            "psoriasis, scaly skin, red patches, schuppenflechte, psoriasis",
            "1. Hautarzt für Diagnose\n2. Feuchtigkeitscreme\n3. Kortisoncreme\n4. Bei schwer: Biologika\n5. UV-Lichttherapie\n6. Stress reduzieren\n7. Regelmäßige Kontrollen",
            "Hydrocortison Creme\nDaivonex Creme (Vitamin D)\nBei schwer: Biologika (Humira, Stelara) vom Dermatologen\nLichttherapie",
            "BEI VERDACHT: Zum Hautarzt!\nKinder brauchen pädiatrischen Dermatologen!\nHautpflege wichtig\nMedikamente für Kinder speziell angepasst\nSchuppen nachts einweichen und ablösen"));

        conditions.add(new MedicalCondition("64", "Vitiligo", "Vitiligo",
            "vitiligo, white skin patches, depigmentation, vitiligo, pigmentstörung",
            "1. Hautarzt für Diagnose\n2. Hautuntersuchung\n3. UV-Lichttherapie\n4. Kortisoncreme\n5. Bei kosmetisch störpernd: Camouflage\n6. Sonnenschutz\n7. Selbsthilfegruppen",
            "Kortisoncreme\nCalcineurin-Inhibitoren (Protopic)\nUV-B Lichttherapie\nBei stabil: Melanotan-Transplantation (experimentell)",
            "BEI VERDACHT: Zum Hautarzt!\nKinder brauchen pädiatrischen Dermatologen\nSonnenschutz wichtig!\nStigmatisierung vermeiden\nSelbsthilfegruppe für Familien"));

        conditions.add(new MedicalCondition("65", "Cellulitis", "Cellulitis",
            "cellulitis, skin infection, red swollen skin, cellulitus, hautinfektion",
            "1. SOFORT zum Arzt! (Antibiotika needed!)\n2. Betroffene Stelle hochlagern\n3. Nicht kratzen\n4. Kühlung\n5. Bei Fieber: 112\n6. Ruhigstellen\n7. Penicillin oder Clindamycin",
            "Antibiotika (Penicillin V oder Clindamycin)\nBei schwer: Krankenhaus mit IV-Antibiotika\nKühlung\nRuhigstellung\nBEI FIEBER + ROTER STELLE: 112!",
            "BEI VERDACHT: Sofort zum Kinderarzt!\nAntibiotika für Kinder\nBEI FIEBER + GESCHWOLLENER HAUT: 112!\nStrukturierte Antibiotika nötig!\nKleinkinder oft mit Streptokokken\nRötung breitet sich aus = SOFORT 112!"));

        conditions.add(new MedicalCondition("66", "Lyme Disease", "Borreliose",
            "lyme disease, tick bite, erythema migrans, bull's eye rash, borreliose, zeckenbiss, wanderröte",
            "1. Bei Wanderröte: SOFORT Arzt!\n2. Zecke entfernen (nicht drehen!)\n3. Beobachten der Stichstelle 4-6 Wochen\n4. Bei Wanderröte: Bluttest + Antibiotika\n5. Bei Gelenkschmerzen: Arzt\n6. Bei Herzproblemen: 112\n7. Meldepflichtig",
            "Bei Wanderröte: Doxycyclin 100mg 2x tgl 14-21 Tage\nBei Unverträglichkeit: Amoxicillin\nBei schwer: Ceftriaxon IV\nBluttest nach 6 Wochen",
            "BEI WANDERRÖTE: Sofort zum Arzt!\nDoxycyclin ab 9 Jahre!\nUnter 9: Amoxicillin Saft\nZecke entfernen nicht mit Drehung!\nBEI HERZBETEILIGUNG: 112!\n4-6 Wochen Stichstelle beobachten\nFSME Impfung für Kinder in Risikogebieten!"));

        conditions.add(new MedicalCondition("67", "Anemia", "Blutarmut",
            "anemia, low iron, fatigue, pale skin, tiredness, blutarmut, anämie, müdigkeite",
            "1. Bluttest beim Arzt\n2. Eisentabletten\n3. Eisenreiche Ernährung (Fleisch, Hülsenfrüchte)\n4. Vitamin C für Eisenaufnahme\n5. Nicht mit Milch einnehmen\n6. Bei schwer: Eisentransfusion\n7. Ursache finden",
            "Ferro Sanol Kapseln (Eisen)\nBei schwerer Anämie: Eisengabe IV im Krankenhaus\nEisensäfte (Eisensaft)\nBluttest zur Kontrolle alle 3 Monate",
            "BEI VERDACHT: Zum Kinderarzt!\nEisensaft für Kinder (Ferrum Hausmann)\nEisenreiche Beikost einführen\nStillen wenn möglich\nBEI SEHR NIEDRIGEM Hämoglobin: Krankenhaus!\nBluttest macht keine Angst!"));

        conditions.add(new MedicalCondition("68", "Thyroid Problems", "Schilddrüsenprobleme",
            "thyroid, hypothyroidism, hyperthyroidism, goiter, thyroiditis, schilddrüse, überfunktion, unterfunktion",
            "1. Bluttest beim Hausarzt\n2. TSH, T3, T4 prüfen\n3. Bei Unterfunktion: Schilddrüsenhormone\n4. Bei Überfunktion: Medikamente oder Radiojod\n5. Ultraschall\n6. Regelmäßige Kontrollen\n7. Bei Knoten: Feinnadelbiopsie",
            "Bei Unterfunktion: L-Thyroxin 50-100µg\nBei Überfunktion: Thiamazol (Metizol)\nBei M. Basedow: Radiojod oder OP\nEndokrinologe",
            "BEI VERDACHT: Zum Kinderarzt!\nKinder brauchen pädiatrischen Endokrinologen!\nBluttest wichtig\nMedikamente für Kinder speziell dosiert\nWachstum und Entwicklung überwachen\nAnstrengung bei hyperaktiver Schilddrüse vermeiden!"));

        conditions.add(new MedicalCondition("69", "Diabetes Type 2", "Diabetes Typ 2",
            "diabetes type 2, blood sugar, insulin resistance, prediabetes, diabetes mellitus",
            "1. Bluttest (Nüchternblutzucker, HbA1c)\n2. Ernährungsumstellung\n3. Mehr Bewegung\n4. Bei Bedarf: Metformin\n5. Blutzucker selbst messen\n6. Bei Entgleisung: Sofort Arzt\n7. Diabetesschulung",
            "Metformin 500-1000mg 2x tgl\nBei Bedarf: Insulin\nBlutzuckermessgerät\nLangzeitblutzucker (HbA1c) alle 3 Monate",
            "BEI VERDACHT: Sofort zum Kinderarzt!\nDiabetes bei Kindern fast immer Typ 1!\nTyp 1 = SOFORT Insulin!\nBei Typ 2: Ernährungsumstellung + Bewegung\nKinder brauchen pädiatrischen Diabetologen!\nBEI BEWUSSTLOSIGKEIT WEGEN UNTERZUCKERUNG: 112!"));

        conditions.add(new MedicalCondition("70", "High Blood Pressure", "Bluthochdruck",
            "high blood pressure, hypertension, elevated blood pressure, bluthochdruck, hypertonie",
            "1. Blutdruckmessung beim Arzt\n2. Salzarme Ernährung\n3. Gewicht reduzieren\n4. Bewegung\n5. Stress reduzieren\n6. Bei Bedarf: Blutdrucksenker\n7. Regelmäßige Kontrollen",
            "Ramipril 5-10mg tgl\nBei Bedarf: Amlodipin\nThiaziddiuretikum\nBlutdruckmessgerät für zu Hause\nRegelmäßige EKG Kontrollen",
            "BEI VERDACHT: Zum Kinderarzt!\nBluthochdruck bei Kindern hat oft eine Ursache (Niere, Herz)!\nAbklären lassen!\nSalzreduktion\nBewegung\nMedikamente für Kinder speziell"));

        conditions.add(new MedicalCondition("71", "Low Blood Pressure", "Niedriger Blutdruck",
            "low blood pressure, hypotension, dizziness, fainting, niedriger blutdruck, kreislaufprobleme",
            "1. Langsam aufstehen\n2. Viel trinken\n3. Salzkonsum erhöhen\n4. Kompressionsstrümpfe\n5. Regelmäßige Bewegung\n6. Bei Ohnmacht: Arzt\n7. Ursache klären",
            "Etilefrin Tropfen (Effortil)\nBei starker Hypotonie: Dihydroergotamin\nViel Wasser trinken\nKochsalz erhöhen\nKreislauftraining",
            "BEI VERDACHT: Zum Kinderarzt!\nBei Kindern meist harmlos\nViel trinken\nLangsam aufstehen\nBEI WIEDERHOLTER OHMACHT: Sofort zum Arzt!\nUrsache klären (Herz? Anämie?!)"));

        conditions.add(new MedicalCondition("72", "Arrhythmia", "Herzrhythmusstörungen",
            "arrhythmia, irregular heartbeat, heart palpitations, herzrasen, herzstolpern, arrhythmie",
            "1. EKG beim Kardiologen\n2. Langzeit-EKG\n3. Herzultraschall\n4. Stress vermeiden\n5. Bei Schwindel oder Ohnmacht: 112\n6. Medikamente vom Kardiologen\n7. Bei Vorhofflimmern: Blutverdünner",
            "Bei Tachykardie: Metoprolol 25-50mg\nBei Vorhofflimmern: Marcoumar oder Xarelto\nKardiologe für Diagnose und Behandlung\nEKG + Langzeit-EKG",
            "BEI HERZRASEN + SCHWINDEL: 112!\nZum Kinderkardiologen!\nAngeborene Herzfehler ausschließen\nMedikamente für Kinder speziell\nBEI OHMACHT mit Herzrasen: 112!"));

        conditions.add(new MedicalCondition("73", "COPD", "COPD",
            "copd, chronic obstructive pulmonary disease, smoker's lung, lung emphysema, copd",
            "1. Sofort aufhören zu rauchen!\n2. Inhalatoren vom Lungenfacharzt\n3. Lungensonographie\n4. Bei Exazerbation: Antibiotika + Kortison\n5. Sauerstoffgabe bei schwer\n6. Lungensport\n7. Impfungen (Grippe, Pneumokokken)",
            "Bronchodilatatoren (Spiriva, Onbrez)\nKortison Inhalator\nBei Exazerbation: Antibiotika + Prednisolon\nLungenfunktionstest\nSauerstoff Langzeittherapie",
            "COPD bei Kindern: Fast nie!\n(Berufskrankheit erst nach Jahrzehnten)\nBei Kindern mit Atemnot: Asthma oder andere Ursache!\nZum Kinderpneumologen!\nBei Atemnot: 112!"));

        conditions.add(new MedicalCondition("74", "Depression", "Depression",
            "depression, depressed mood, loss of interest, sadness, not enjoying life, depression, traurigkeit",
            "1. Hausarzt für Erstdiagnose\n2. Psychotherapie\n3. Bei schwer: Antidepressiva\n4. Sport und Lichttherapie\n5. Schlaf regulieren\n6. Soziale Kontakte pflegen\n7. Bei Suizidgedanken: 112 / Telefonseelsorge",
            "Bei leichter-mittelschwer: Psychotherapie\nBei schwer: Escitalopram (Cipralex) oder Sertralin\nBei Suizidgedanken: 112 oder Telefonseelsorge 0800 111 0 111",
            "BEI VERDACHT: Zum Kinder- und Jugendarzt!\nKinder zeigen Depression anders als Erwachsene!\nAggression, Rückzug, Schulprobleme\nPsychotherapie für Kinder und Eltern\nBei Selbstverletzung oder Suizidgedanken: 112!\nKinder- und Jugendpsychiatrie!"));

        conditions.add(new MedicalCondition("75", "Insomnia", "Schlafstörungen",
            "insomnia, can't sleep, sleep problems, sleeping difficulty, schlafstörungen, einschlafprobleme",
            "1. Schlafhygiene verbessern\n2. Regelmäßige Schlafenszeiten\n3. Kein Koffein nach 14h\n4. Entspannungstechniken\n5. Baldrian oder Melatonin\n6. Bei anhaltend über 4 Wochen: Arzt\n7. Schlaflabor",
            "Melatonin 1-5mg\nBaldrian 600mg\nBei Depression: Mirtazapin\nSchlafhygiene\nSchlaflabor bei anhaltenden Problemen",
            "BEI VERDACHT: Zum Kinderarzt!\nSchlafprobleme bei Kindern oft durch Routinen!\nFeste Schlafenszeiten\nKein Bildschirm 1h vor Schlaf\nBaldriparan für Kinder ab 6 Jahren\nMelatonin nur kurzfristig vom Arzt!"));

        conditions.add(new MedicalCondition("76", "Migraine", "Migräne",
            "migraine, severe headache, headache with nausea, light sensitivity, migräne, starke kopfschmerzen",
            "1. Reizarme Umgebung\n2. Dunkler Raum\n3. Bei Übelkeit: Vomex A\n4. Schmerzmittel in early phase\n5. Bei starker Migräne: Triptane\n6. Prophylaxe mit Betablockern\n7. Migränetagebuch führen",
            "Bei Migräneanfall: Ibuprofen 400-600mg oder Sumatriptan 50mg\nBei Übelkeit: Vomex A\nZur Prophylaxe: Betablocker (Metoprolol) oder Topiramat\nMigränetagebuch",
            "BEI VERDACHT: Zum Kinderarzt!\nKinder können auch Migräne haben!\nDunkler Raum\nKühlkompresse\nParacetamol/Ibuprofen frühzeitig\nBei häufiger Migräne: Prophylaxe mit Arzt besprechen\nTriptane für Kinder oft nicht zugelassen!"));

        conditions.add(new MedicalCondition("77", "Tension Headache", "Spannungskopfschmerzen",
            "tension headache, stress headache, tight head, muscle tension, spannungskopfschmerzen, streß",
            "1. Entspannung\n2. Nacken- und Schultermassage\n3. Wärme\n4. Ausgleichende Bewegung\n5. Stress reduzieren\n6. Schmerzmittel bei Bedarf\n7. Bei chronisch: Arzt",
            "Paracetamol 500mg oder Ibuprofen 400mg\nBei Verspannungen: Ibuprofen Gel auf Nacken\nMassage\nEntspannungsübungen\nSport und Ausdauer",
            "BEI VERDACHT: Zum Kinderarzt!\nKinder durch Schulstress!\nEntspannung und Bewegung\nWärme auf Nacken\nParacetamol/Ibuprofen kindgerecht\nBei häufigen Kopfschmerzen abklären lassen!"));

        conditions.add(new MedicalCondition("78", "Cluster Headache", "Clusterkopfschmerzen",
            "cluster headache, severe one-sided headache, eye pain, clusterkopfschmerzen, trigeminusneuralgie",
            "1. SOFORT zum Arzt/Neurologen!\n2. Sauerstoffgabe\n3. Triptane als Nasenspray\n4. Prophylaxe mit Verapamil\n5. Kein Alkohol während Attacken\n6. Stress reduzieren\n7. Neurologe für Diagnose",
            "Sauerstoff 7-12L/min\nSumatriptan Nasenspray\nZur Prophylaxe: Verapamil 240-960mg\nLithium bei chronischer Form\nNeurologe!",
            "BEI VERDACHT: Sofort zum Kinderarzt!\nClusterkopfschmerzen bei Kindern sehr selten\nAber möglich!\nDiagnose durch Ausschluss und Kinderneurologe\nAndere Kopfschmerzen viel häufiger\nBEI WIEDERHOLTEN STARKS KOPFSCHMERZEN: Arzt!"));

        conditions.add(new MedicalCondition("79", "Concussion", "Gehirnerschütterung",
            "concussion, head injury, brain shake, knocked out, gehirnerschütterung, kopfverletzung",
            "1. SOFORT zum Arzt!\n2. Kinder nicht schlafen lassen nach Unfall?\n3. Kühlung bei Beule\n4. Bei Bewusstlosigkeit: 112\n5. 24-48h Bettruhe\n6. Kein Sport 1 Woche\n7. Bei Erbrechen oder Verwirrtheit: SOFORT 112",
            "Bei leichter Gehirnerschütterung: Bettruhe 24-48h\nParacetamol bei Kopfschmerzen\nKEIN Aspirin!\nCT Kopf bei Warnzeichen\nBei Bewusstlosigkeit: Krankenhaus",
            "BEI KOPFVERLETZUNG + BEWUSSTLOSIGKEIT: 112!\nZum Kinderarzt/Kinderchirurgie!\nKind 24h beobachten (alle 2h wecken!)\nBei Erbrechen, Verwirrtheit, ungleichen Pupillen: 112!\nKein Sport 1 Woche!\nCT nur bei Warnzeichen, nicht routinemäßig"));

        conditions.add(new MedicalCondition("80", "Whiplash", "Schleudertrauma",
            "whiplash, neck injury from car accident, neck pain after accident, schleudertrauma, Halswirbelsäule",
            "1. Sofort zum Arzt!\n2. Röntgen/CT/HWS Wirbelsäule\n3. Neck brace kurzzeitig\n4. Schmerzmittel\n5. Krankengymnastik\n6. Bei Nervenausfall: 112\n7. Langzeitrehabilitation",
            "Ibuprofen 400-600mg\nBei starken Schmerzen: Prednisolon\nNeck brace (Chirodynamic)\nPhysiotherapie\nRöntgen/CT",
            "BEI VERDACHT: Sofort zum Kinderarzt!\nKind muss medizinisch untersucht werden!\nRöntgen/Ultraschall\nSchmerzmittel kindgerecht\nNackenmuskulatur stärken\nLangzeitfolgen bei Kindern vermeiden!"));

        conditions.add(new MedicalCondition("81", "Fracture", "Knochenbruch",
            "fracture, broken bone, arm fracture, leg fracture, knochenbruch, gebrochen",
            "1. Unfallstelle nicht bewegen!\n2. Schiene anlegen falls möglich\n3. Kühlung\n4. Bei offenen Bruch: 112\n5. Sofort in Krankenhaus\n6. Röntgen\n7. Gips oder OP je nach Bruch",
            "Bei geschlossenem Bruch: Gips\nBei verschobenem: Reposition + Gips\nBei kompliziertem: OP\nSchmerzmittel im Krankenhaus\nCalcium und Vitamin D zur Heilung",
            "BEI VERDACHT: Sofort zum Kinderarzt/112!\nKind nicht bewegen!\nKnochen bei Kindern heilen schneller!\nWachstumsfugen beachten!\nEltern sollten ruhig bleiben!\nBei offenen Bruch: 112 SOFORT!"));

        conditions.add(new MedicalCondition("82", "Dislocation", "Ausrenkung",
            "dislocation, joint out of place, shoulder dislocation, elbow, ausrenkung, luxation",
            "1. Gelenk nicht bewegen!\n2. Schiene anlegen\n3. Nicht selbst einrenken!\n4. Sofort in Krankenhaus\n5. Röntgen\n6. Einrenken unter Narkose\n7. Schiene für 3-6 Wochen",
            "Im Krankenhaus: Reposition unter Narkose\nDanach: Schiene 3-6 Wochen\nBei Schultern: Pendelübungen nach Abnahme\nBei wiederholter Luxation: OP",
            "BEI VERDACHT: Sofort zum Kinderarzt/112!\nBEI SCHULTER: Kind nicht selbst einrenken!\nKind beruhigen\nSchiene anlegen\nKühlen mit Tuch\nOP bei wiederholter Luxation"));

        conditions.add(new MedicalCondition("83", "Bursitis", "Schleimbeutelentzündung",
            "bursitis, joint swelling, elbow bursitis, knee bursitis, schleimbeutelentzündung, bursitis",
            "1. Gelenk schonen\n2. Kühlung\n3. Entzündungshemmende Medikamente\n4. Bei Infektion: Antibiotika\n5. Bei chronisch: Schleimbeutelentfernung\n6. Physiotherapie\n7. Bei Rötung und Fieber: Arzt",
            "Ibuprofen 400-600mg\nBei Infektion: Antibiotika (Clindamycin)\nBei chronisch: OP zur Entfernung\nKühlung und Schonung\nPhysiotherapie",
            "BEI VERDACHT: Zum Kinderarzt!\nBei Kindern selten\nSchonung und Kühlung\nParacetamol/Ibuprofen\nBei Rötung + Fieber: SOFORT Arzt!\nInfektion nicht auszuschließen"));

        conditions.add(new MedicalCondition("84", "Tendinitis", "Sehnenentzündung",
            "tendinitis, tendon pain, tennis elbow, achilles tendon, sehnenentzündung, tendinitis",
            "1. Sehne schonen\n2. Kühlung\n3. Schmerzgel\n4. Dehnung und Kräftigung\n5. Bei hartnäckig: Stoßwellentherapie\n6. Bei Riss-Verdacht: SOFORT Arzt\n7. Bei Achilles: Fersenkeil",
            "Voltarol Gel\nIbuprofen 400mg\nBei Achilles: Fersenkeil\nPhysiotherapie\nBei Riss-Verdacht: MRT und ggf. OP",
            "BEI VERDACHT: Zum Kinderarzt!\nBei Kindern oft durch Sport!\nSchonung\nKühlen\nDehnung nach Akutphase\nBEI ACHILLESSEHNE + SCHMERZEN: Sofort zum Arzt!\nRiss ausschließen!"));

        conditions.add(new MedicalCondition("85", "Herniated Disc", "Bandscheibenvorfall",
            "herniated disc, slipped disc, back pain radiating leg, spinal disc, bandscheibenvorfall, hexenschuss",
            "1. Sofort zum Orthopäden!\n2. Röntgen/MRT\n3. Schmerzmittel\n4. Physiotherapie\n5. Bei Nervenausfall oder Lähmung: SOFORT 112\n6. Bei Blasen-/Darmstörung: SOFORT 112\n7. OP nur bei Versagen der konservativen Therapie",
            "Ibuprofen 600mg oder Diclofenac\nPrednisolon bei starken Schmerzen\nPhysiotherapie\nBei Lähmung: SOFORT 112 und Krankenhaus!\nMRT zur Diagnose",
            "Bandscheibenvorfall bei Kindern SEHR selten!\nAber möglich durch Unfälle!\nBEI RÜCKENSCHMERZEN + BEINSSCHMERZEN + TAUBHEIT: 112!\nZum Kinderorthopäden!\nSonst eher Muskelverspannung\nBEI BLASENSTÖRUNG + RÜCKENSCMERZEN: 112!"));

        conditions.add(new MedicalCondition("86", "Otitis Externa", "Außenohrentzündung",
            "swimmer's ear, outer ear infection, ear canal inflammation, außenohrentzündung, schwimmerohr",
            "1. Ohr trocken halten\n2. Keine Wattestäbchen!\n3. Ohrentropfen vom Arzt\n4. Schmerzmittel\n5. Bei Ausbreitung: Antibiotika\n6. Bei Diabetes: SOFORT Arzt (Risiko bösartige Form)\n7. HNO-Arzt",
            "Otolin Ohrentropfen\nBei Infektion: Antibiotika Ohrentropfen (Ofloxacin)\nBei schwer: Ciprofloxacin Tropfen\nSchmerzmittel\nTrockenhalten",
            "BEI VERDACHT: Zum Kinderarzt!\nOhrentropfen für Kinder\nKeine Wattestäbchen!\nSchwimmen vermeiden\nParacetamol bei Schmerzen\nBei Fieber oder Ausbreitung: Sofort Arzt!"));

        conditions.add(new MedicalCondition("87", "Otitis Media", "Mittelohrentzündung",
            "middle ear infection, ear pain, fever, ear infection baby, mittelohrentzündung, ohrenschmerzen kind",
            "1. Bei Ohrenschmerzen + Fieber: Arzt\n2. Schmerzmittel\n3. Abwarten oft möglich (virale Ursache)\n4. Bei eitriger: Antibiotika\n5. Bei wiederholend: Paukendrainage\n6. Nasentropfen\n7. Bei Säuglingen unter 6 Monaten: IMMER Arzt",
            "Paracetamol bei Schmerzen/Fieber\nNasentropfen (Otriven)\nBei bakteriell nach 48h Beobachtung: Amoxicillin\nBei wiederholten Infekten: HNO-Arzt für Drainage",
            "BEI OHRENSCHMERZEN + FIEBER: Zum Kinderarzt!\nSäuglinge unter 6 Monate: IMMER zum Arzt!\nParacetamol/Ibuprofen\nNasentropfen\nAbwarten oft möglich bei viral\nBEI FIEBER + ERBRECHEN + OHRENSCHMERZEN: Arzt!"));

        conditions.add(new MedicalCondition("88", "Labyrinthitis", "Labyrinthitis",
            "labyrinthitis, vertigo, dizziness, balance problems, hearing loss, labyrinthitis, schwindel",
            "1. Bei plötzlichem Schwindel + Übelkeit: Arzt\n2. Bettruhe\n3. Medikamente gegen Schwindel\n4. Bei Hörverlust: HNO-Arzt\n5. Ursache klären (viral, bakteriell)\n6. Bei Borreliose: Antibiotika\n7. Krankengymnastik für Gleichgewicht",
            "Bei Schwindel: Vomex A oder Dimenhydrinat\nBei Borreliose: Doxycyclin oder Ceftriaxon\nBetahistin zur Prophylaxe\nHNO-Arzt für Gehörprüfung",
            "BEI PLÖTZLICHEM SCHWINDEL: Sofort zum Kinderarzt!\nZum HNO-Arzt!\nKind nicht alleine lassen (Sturzgefahr!)\nBei Hörverlust: SOFORT HNO!\nLabyrinthitis bei Kindern selten\nAndere Ursachen häufiger (Ohren, Augen, Migräne)!"));

        conditions.add(new MedicalCondition("89", "Tinnitus", "Tinnitus",
            "tinnitus, ringing in ears, ear noise, summen im ohr, tinnitus, piepen",
            "1. HNO-Arzt für Untersuchung\n2. Gehör testen lassen\n3. Stress reduzieren\n4. Bei plötzlichem Tinnitus: SOFORT HNO (Hörsturz!)\n5. Weißes Rauschen\n6. Entspannungstechniken\n7. Wenn chronisch: Tinnitus-Retraining",
            "Bei akutem Hörsturz + Tinnitus: Sofort HNO!\nPrednisolon\nBei chronisch: Tinnitus-Retraining-Therapie\nWeißes Rauschen\nEntspannung",
            "BEI PÖTZLICHEM TINNITUS: Sofort zum Kinderarzt/HNO!\nKann auf Hörsturz hinweisen!\nAkut: Kortison wichtig!\nBei Kindern oft vorübergehend\nStressabbau\nHandy/Tablet Lautstärke begrenzen!"));

        conditions.add(new MedicalCondition("90", "Hearing Loss", "Hörverlust",
            "hearing loss, can't hear well, deafness, earwax, hörverlust, schwerhörigkeit",
            "1. HNO-Arzt für Hörtest\n2. Ohrenschmalz entfernen (vom Arzt!)\n3. Bei plötzlichem Hörverlust: SOFORT HNO\n4. Hörgeräte bei Bedarf\n5. CI (Cochlea Implantat) bei Taubheit\n6. Ursache finden\n7. Regelmäßige Kontrollen",
            "Bei Ohrenschmalz: Ohrentropfen oder Spülung durch HNO\nBei Infektion: Antibiotika\nBei Hörsturz: Kortison\nHörgeräte vom Akustiker",
            "BEI PLÖTZLICHEM HÖRVERLUST: Sofort zum Kinderarzt/HNO!\nKann Notfall sein!\nSäuglinge: Neugeborenen-Hörscreening beachten\nBei Kleinkindern: Paukenerguss häufig!\nHörteste beimKind regelmäßig\nZum HNO wenn Kind schlecht hört!"));

        conditions.add(new MedicalCondition("91", " epistaxis", "Nasenbluten",
            "nosebleed, bloody nose, nasal bleeding, nasenbluten, nasal",
            "1. Aufrecht hinsetzen, Kopf leicht nach vorne\n2. Nasenflügel 10 Min zusammendrücken\n3. NICHT schneuzen!\n4. Kalte Kompresse im Nacken\n5. Nasenspray bei häufigem Nasenbluten\n6. Luftfeuchtigkeit erhöhen\n7. Bei über 20 Min or häufig: HNO-Arzt",
            "Nasivin Nasenspray\nCottonpads\nEisen bei häufigem Nasenbluten\nNasentamponaden vom HNO\nBei Gerinnungsstörung: Gerinnungsambulanz",
            "Nasenflügel 10 Min zusammendrücken\nKind aufrecht halten\nNICHT hinlegen!\nKalte Kompresse im Nacken\nBei häufigem Nasenbluten: HNO-Arzt\nBei Säuglingen: IMMER zum Arzt!\nUrsache abklären!"));

        conditions.add(new MedicalCondition("92", "Allergic Rhinitis", "Allergischer Schnupfen",
            "allergic rhinitis, hay fever, pollen allergy, sneezing, sniffles, allergischer schnupfen, heuschnupfen",
            "1. Allergen vermeiden\n2. Antihistaminika\n3. Nasenspray mit Kortison\n4. Augentropfen\n5. Bei Asthma: Inhalator\n6. Allergietest beim Immunologen\n7. Spezifische Immuntherapie (Hypposensibilisierung)",
            "Cetirizin 10mg oder Loratadin 10mg\nNasonex Nasenspray\nLivocab Augentropfen\nBei Asthma: VentaAIR und inhaled Kortison",
            "BEI VERDACHT: Zum Kinderarzt/Allergologen!\nAllergietest bei Kindern möglich\nCetirizin Tropfen für kleine Kinder\nNasonex für Kinder ab 3 Jahren\nAugentropfen für Kinder\nBei Atemnot: 112!"));

        conditions.add(new MedicalCondition("93", "Anaphylaxis", "Anaphylaktischer Schock",
            "anaphylaxis, severe allergic reaction, throat swelling, shock, allergischer schock, anaphylaxie",
            "1. NOTFALL! SOFORT 112!\n2. Adrenalin-Autoinjektor anwenden\n3. Person hinlegen, Beine hoch\n4. Bei Atemnot: Adrenalin\n5. Antihistaminikum + Kortison\n6. Nach Erstversorgung: Krankenhaus 24h überwachen\n7. Allergiepass besorgen",
            "Adrenalin-Autoinjektor (Epipen, Jext)\nCetirizin\nPrednisolon 50mg\nSOFORT 112!\nIm Krankenhaus: Überwachung und ggf. Intubation",
            "BEI VERDACHT: 112 SOFORT!\nAdrenalin für Kinder (Epipen Jr ab 15kg)\nEltern müssen Adrenalin anwenden können!\nBei Bienen-/Wespenstich Allergie: Adrenalin + Notfallplan\nAllergiepass für Kind\nKita/Schule muss informiert werden!"));

        conditions.add(new MedicalCondition("94", "Strep Throat", "Streptokokken Angina",
            "strep throat, streptococcal pharyngitis, positive strep test, streptokokken, angina",
            "1. Rachenabstrich beim Arzt\n2. Bei positiv: Antibiotika (Penicillin!)\n3. Schonkost und Schmerzmittel\n4. Bei Penicillinallergie: Makrolide\n5. 24h nach Antibiotikastart nicht mehr ansteckend\n6. Bei Nichtbehandlung: Rheumatisches Fieber möglich\n7. Kontrolle nach Behandlung",
            "Penicillin V 1Mio IE 3x tgl 10 Tage\nBei Allergie: Azithromycin\nSchmerzmittel\nBei Wiederholung: Tonsillektomie mit HNO",
            "BEI VERDACHT: Sofort zum Kinderarzt!\nRachenabstrich!\nAntibiotika für 10 Tage vollständig!\nAnsteckend bis 24h nach Antibiotikastart!\nBEI GELENKSCHMERZEN NACH STREP: Sofort zum Arzt!\nRheumatisches Fieber als Komplikation!"));

        conditions.add(new MedicalCondition("95", "Mononucleosis", "Mononukleose",
            "mononucleosis, EBV, kissing disease, fatigue, swollen lymph nodes, Pfeiffersches Drüsenfieber",
            "1. Bluttest auf EBV\n2. Bettruhe 2-4 Wochen\n3. Schmerzmittel\n4. Bei Milzriss: SOFORT 112 (akuter Bauch + Linksschulter = NOTFALL!)\n5. Kein Sport 4-6 Wochen (Milzschonung)\n6. Kortison bei schwerem Verlauf\n7. Monate bis zur Erholung",
            "Bei schwerer Angina: Prednisolon\nSchmerzmittel (kein Aspirin bei Kindern!)\nBei Atemnot: Klinikeinweisung\nBei Milzriss: Notfall-OP\n Bluttest: EBV-Ak",
            "BEI VERDACHT: Zum Kinderarzt!\nBlutbild zeigt oft atypische Lymphozyten\nSchonung wichtig!\nBei Bauchschmerzen links + Fieber: 112!\nMilzriss-Gefahr bei Kontaktsport!\nSportpause 4-6 Wochen!\nMonate bis vollständige Erholung normal!"));

        conditions.add(new MedicalCondition("96", "Cytomegalovirus", "CMV",
            "cytomegalovirus, CMV, congenital infection, baby infection, zytomegalie, cmv",
            "1. Bei Schwangeren: SOFORT zum Frauenarzt!\n2. Bluttest auf CMV\n3. Bei Neugeborenen mit CMV: Ganciclovir\n4. Bei Immungeschwächten: Cangard\n5. Isolierung bei Hospitalisierung\n6. Hörtest bei Neugeborenen\n7. Regelmäßige Kontrollen",
            "Bei Neugeborenen: Ganciclovir 6mg/kg IV 2x tgl\nBei Immungeschwächten: Ganciclovir oder Foscarnet\nBei Schwangeren: Virostatika umstritten\nHörtest wichtig",
            "BEI CMV IM NEUGEBORENEN-ALTER: Sofort zum Kinderarzt!\nGanciclovir für Neugeborene!\nHörverlust als Spätfolge möglich!\nRegelmäßige Hörteste!\nBei Schwangerschaft: Frauenarzt informieren!\nImmungeschwächte Kinder besonders gefährdet!"));

        conditions.add(new MedicalCondition("97", "Toxoplasmosis", "Toxoplasmose",
            "toxoplasmosis, cat litter, parasite infection, schwangerschaft, toxoplasmose",
            "1. Bluttest auf Toxoplasmose\n2. Bei Schwangeren: SOFORT zum Frauenarzt!\n3. Antibiotika (Roxithromycin/Spiramycin)\n4. Kein Katzenklo reinigen in Schwangerschaft\n5. Fleisch gut durchgaren\n6. Gemüse waschen\n7. Bei Immungeschwächten: Pyrimethamin",
            "Bei Schwangeren: Spiramycin oder Roxithromycin\nBei Immungeschwächten: Pyrimethamin + Sulfadiazin\nAugenärztliche Kontrolle\nBei Erstinfektion in Schwangerschaft: Gynäkologe",
            "BEI VERDACHT IN DER SCHWANGERSCHAFT: Sofort zum Frauenarzt!\nKatzenhaltende Schwangere besonders gefährdet!\nBei Neugeborenen: Pädiatrische Abklärung\nAugenärztliche Kontrolle\nAntibiotika in Schwangerschaft möglich\nKeine Katzenstreu in der Schwangerschaft reinigen!"));

        conditions.add(new MedicalCondition("98", "Listeriosis", "Listeriose",
            "listeriosis, foodborne illness, unpasteurized cheese, listeria, listeriose, lebensmittelinfektion",
            "1. Bei Fieber + Nahrungsmitteln: Sofort Arzt!\n2. Blutkultur\n3. Antibiotika (Amoxicillin)\n4. Bei Schwangeren: Sofort Frauenarzt!\n5. Meldepflichtig\n6. Bettruhe\n7. Bei Neugeborenen: Krankenhaus",
            "Amoxicillin 3g IV 4x tgl 3 Wochen\nBei Allergie: Cotrimoxazol\nBei Sepsis: Gentamicin hinzufügen\nSchwangere: Sofort ins Krankenhaus",
            "BEI FIEBER + VERDACHT AUF LISTERIA: Sofort zum Arzt!\nSchwangere besonders gefährdet!\nRisikolebensmittel meiden (Weichkäse, Rohmilchprodukte)\nBei Infektion in Schwangerschaft: Sofort ins Krankenhaus!\nNeugeborene müssen abgeklärt werden!\nMeldepflichtig!"));

        conditions.add(new MedicalCondition("99", "Rotavirus", "Rotavirus",
            "rotavirus, baby diarrhea, vomiting baby, infant diarrhea, rotavirus, brechdurchfall baby",
            "1. Bei Säuglingen: Sofort zum Kinderarzt!\n2. Elektrolytlösung\n3. Stillen fortsetzen\n4. Bei schwer: Krankenhaus (IV Flüssigkeit)\n5. Impfung (Rotarix, Rotateq) schützt!\n6. Strenge Hygiene (hochansteckend!)\n7. Windeln entsorgen mit Handschuhen",
            "Oralpädon Elektrolytlösung\nBei schwerer Dehydration: IV Flüssigkeit im Krankenhaus\nRotavirusimpfung im Säuglingsalter\nHände waschen!",
            "BEI SÄUGLING MIT DURCHFALL UND ERBRECHEN: Sofort zum Kinderarzt!\nHÖCHST ANSTECKEND!\nElektrolytlösung für Säuglinge\nSTILLEN fortsetzen!\nBEI TROCKENER WINDELN ÜBER 4-6h: 112!\nWindeln entsorgen in Extra-Müllsack!"));

        conditions.add(new MedicalCondition("100", "Norovirus", "Norovirus",
            "norovirus, stomach flu, vomiting, diarrhea, norovirus, brechdurchfall",
            "1. Sofort zum Arzt bei Kleinkindern/Alter!\n2. Elektrolytlösung\n3. Schonkost\n4. Strenge Hygiene (DESINFIZIEREN!)\n5. Isolierung\n6. Bei Austrocknung: Krankenhaus\n7. 48h nach Erbrechen zu Hause bleiben",
            "Oralpädon Elektrolytlösung\nKeine Antidiarrhoeika!\nDESINFEKTIONSMITTEL (razepes!)!\nBei Austrocknung: IV im Krankenhaus\nHände waschen + Desinfektion",
            "BEI KLEINKIND/ÄLTEREN MENSCHEN MIT ERBRECHEN: Sofort zum Kinderarzt!\nEXTREM ANSTECKEND!\nElektrolytlösung\nHände DESINFIZIEREN nicht nur waschen!\nDESINFektionsmittel verwenden (Razepes)!\nWindeln in Extra-Müllsack!\nBei Trockenheit, Verwirrtheit: 112!"));

        conditions.add(new MedicalCondition("101", "Mumps", "Mumps",
            "mumps, parotitis, swollen cheeks, fever, schwellung der bäckspeicheldrüse, mumps",
            "1. SOFORT zum Arzt (Meldepflichtig!)\n2. Fieber und Schmerzmittel\n3. Kühlung der geschwollenen Drüsen\n4. Bettruhe\n5. Eiswürfel oder kalte Getränke\n6. Bei Hirnhautentzündung: Krankenhaus\n7. Impfstatus überprüfen",
            "Paracetamol bei Schmerzen/Fieber\nBei Hirnhautentzündung: Krankenhaus\nBei Mumps ohne Komplikationen: Bettruhe\nBei Kindern unter 2 Jahren: Besonderes Risiko",
            "BEI VERDACHT: Sofort zum Kinderarzt!\nMeldepflichtig!\nMumpsimpfung schützt (MMR-Impfung)!\nBei Hirnhautentzündung, Bauchschmerzen: Sofort 112!\nMumps bei Kindern meist harmlos\nKomplikationen wie Hodenentzündung bei Jugendlichen möglich\nHören prüfen lassen!"));

        conditions.add(new MedicalCondition("102", "Rubella", "Röteln",
            "rubella, german measles, rash, swollen lymph nodes, röteln, rotes unwohl",
            "1. SOFORT zum Arzt (Meldepflichtig!)\n2. Fiebermittel\n3. Isolierung Schwangerer verhindern!\n4. Bettruhe\n5. Bei Kindern meist harmlos\n6. Bei Schwangeren ohne Impfschutz: Sofort Gynäkologe!\n7. Impfstatus überprüfen",
            "Paracetamol bei Fieber\nBei Schwangeren nach Kontakt: Immunglobulin\nBei Kindern: Bettruhe\nBei Arthralgien: Ibuprofen\nRötelnimpfung schützt",
            "BEI VERDACHT: Sofort zum Kinderarzt!\nMeldepflichtig!\nRÖTELN BEI SCHWANGEREN = SEHR GEFÄHRLICH FÜR BABY!\nBei Schwangerenkontakt sofort zum Frauenarzt!\nRötelnimpfung schützt (MPR)!\nBei Kindern meist harmlos\nHautausschlag beginnt im Gesicht"));

        conditions.add(new MedicalCondition("103", "Diphtheria", "Diphtherie",
            "diphtheria, throat pseudomembrane, breathing difficulty, diphtherie, husten, kehlkopfentzündung",
            "1. SOFORT NOTFALL 112!\n2. Krankenhaus (Antitoxin + Antibiotika!)\n3. Atemwege sichern\n4. Isolation\n5. Penicillin oder Erythromycin\n6. Kontaktpersonen behandeln\n7. Impfstatus überprüfen",
            "Diphtherie-Antitoxin (im Krankenhaus)\nPenicillin G oder Erythromycin\nBei Atemnot: Intubation/Tracheotomie\nSofort Krankenhaus!\nMeldepflichtig",
            "BEI VERDACHT: 112 SOFORT!\nKRANKENHAUS SOFORT!\nDiphtherie ist LEBENSGEFÄHRLICH!\nKeim im Rachen bildet Membran\nKann Atemwege blockieren!\nDiphtherie-Imfpung schützt (im Rahmen 6-fach Impfung)!\nIn Deutschland sehr selten durch Impfung"));

        conditions.add(new MedicalCondition("104", "Tetanus", "Tetanus",
            "tetanus, lockjaw, muscle spasms, wound infection, wundstarrkrampf, tetanus",
            "1. SOFORT zum Arzt bei Wunde + nicht geimpft!\n2. Wundversorgung\n3. Tetanus-Antitoxin\n4. Penicillin oder Metronidazol\n5. Krankenhaus bei schweren Fällen\n6. Bei Krämpfen: Intensivstation\n7. Impfstatus überprüfen und auffrischen",
            "Tetanus-Antitoxin ( Humane Immunglobulin)\nPenicillin G oder Metronidazol\nWundversorgung\nBei Krämpfen: Intensivstation\nWunddebridement",
            "BEI WUNDE + FIEBER + KIEFERSPERRE: 112!\nTetanus kann tödlich sein!\nBei Kindern: Sofort zum Kinderarzt/112!\nImpfschutz prüfen (6-fach Impfung im Babyalter)!\nBei Nicht-Geimpften: Immunglobulin + Impfung\nWunde gründlich reinigen!"));

        conditions.add(new MedicalCondition("105", "Rabies", "Tollwut",
            "rabies, animal bite, dog bite, bat exposure, tollwut, bissverletzung",
            "1. SOFORT Wunde reinigen 15 Min mit Seife!\n2. SOFORT Arzt/Krankenhaus!\n3. Tollwut-Immunglobulin + Impfung\n4. Impfplan einhalten (Tag 0, 3, 7, 14, 28)\n5. Tier beobachten wenn möglich\n6. Bei Tollwutsymptomen: FAST IMMER TOD!\n7. Meldepflichtig",
            "Tollwut-Immunglobulin (RIG)\nTollwut-Impfstoff (HDCV oder PCECV)\nImpfung an Tag 0, 3, 7, 14, 28\nWundversorgung\nSofort ins Krankenhaus!",
            "BEI TIERBISS/ FLEDERMAUSKONTAKT: Sofort zum Arzt/112!\nTollwut ist IMMER TOD wenn Symptome!\nWunde 15 Min mit Seife ausspülen!\nTollwut-Immunglobulin + Impfung muss SOFORT beginnen!\nBei Wildtierbiss: Tollwut-Impfung\nIn Deutschland: Fledermäuse können Tollwut haben!"));

        conditions.add(new MedicalCondition("106", "Malaria", "Malaria",
            "malaria, tropical disease, fever after travel, mosquito bite, malaria, fieber nach reise",
            "1. Bei Fieber nach Tropenreise: SOFORT Arzt!\n2. Bluttest auf Malariaparasiten\n3. Je nach Art: Chloroquin, Artemether-Lumefantrin, Atovaquon-Proguanil\n4. Bei schwerer Malaria: IV Chinin im Krankenhaus\n5. Vorbeugung: Mückenschutz + Chemoprophylaxe\n6. Resistenzen beachten",
            "Bei unkomplizierter P. vivax/ovale: Chloroquin + Primaquin\nBei P. falciparum: Artemether-Lumefantrin (Riamet)\nBei schwerer: IV Chinin im Krankenhaus\nVorbeugung: Lariam, Malarone, Dovatrex",
            "BEI FIEBER NACH TROPENREISE: Sofort zum Kinderarzt!\nMalaria kann tödlich sein!\nBluttest notwendig!\nMedikamente für Kinder speziell dosiert\nInsektenstichschutz (Mückenspray, Bettnetz)!\nVor Reise in Malariagebiete: Tropenmediziner aufsuchen!"));

        conditions.add(new MedicalCondition("107", "Dengue Fever", "Dengue Fieber",
            "dengue, dengue fever, tropical fever, muscle pain, tropenfieber, knochenbrecherfieber",
            "1. Bei Fieber nach Tropenreise: Arzt!\n2. Bluttest\n3. Nur unterstützende Therapie (Schmerzmittel, Ruhe)\n4. KEIN Aspirin oder Ibuprofen (Blutungsgefahr!)\n5. Bei schwerem Dengue (DHS): Krankenhaus\n6. Paracetamol für Schmerzen/Fieber\n7. Mückenschutz während Genesung",
            "Bei Dengue: Nur Paracetamol!\nKEIN Aspirin, KEIN Ibuprofen (Gefahr Blutung!)\nBei schwerem Dengue: Infusionen, Bluttransfusion\nIm Krankenhaus: Überwachung",
            "BEI FIEBER NACH TROPENREISE: Sofort zum Kinderarzt!\nParacetamol für Kinder bei Dengue!\nKEINE Ibuprofene oder Aspirin!\nBei schwerem Verlauf: Krankenhaus\nMückenschutz wichtig\nZweite Infektion oft schlimmer!"));

        conditions.add(new MedicalCondition("108", "Zika Virus", "Zika Virus",
            "zika, zika virus, rash, fever, travel,Microcephaly, zika, schwangerschaft",
            "1. Bei Fieber + Ausschlag nach Tropenreise: Arzt\n2. Blut-/Urinest auf Zika\n3. Ruhe und Schmerzmittel\n4. Bei Schwangeren: SOFORT Gynäkologe!\n5. Mückenschutz (Schützt auch andere!)\n6. Bei Guillain-Barré: Krankenhaus\n7. 6 Monate nicht schwanger werden nach Infektion",
            "Nur symptomatische Therapie\nParacetamol\nBei Guillain-Barré: Krankenhaus\nKein Aspirin bei Kindern\nBei Schwangeren mit Zika: Gynäkologische Betreuung",
            "BEI SCHWANGERSCHAFT + TROPENREISE: Sofort zum Frauenarzt!\nZika kann Mikrozephalie beim Baby verursachen!\nBei Schwangeren: Engmaschige Ultraschallkontrollen\nInsektenstichschutz in der Schwangerschaft!\nBei Kindern meist harmlos\n6 Monate nicht schwanger nach Zika!"));

        conditions.add(new MedicalCondition("109", "Ebola", "Ebola",
            "ebola, viral hemorrhagic fever, africa travel, ebola, fieber, durchfall",
            "1. BEI VERDACHT NACH AFRIKAREISE: 112 SOFORT!\n2. Isolierung\n3. Spezialbehandlung im Krankenhaus\n4. Kontaktverfolgung\n5. Keine Selbstbehandlung\n6. Meldepflichtig\n7. Hohe Sterblichkeit",
            "Bei Verdacht: SOFORT 112 und Krankenhaus!\nSpezialisierte Behandlungszentren in Deutschland\nKeine spezifische Therapie\nRibavirin manchmal verwendet\nUnterstützende Intensivtherapie",
            "BEI FIEBER + DURCHFALL NACH AFRIKAREISE: 112 SOFORT!\nEBOLA IST SEHR TODLICH!\nWird in spezialisierten Zentren behandelt\nKrankenhaus in Germany: Bernhard-Nocht-Institut Hamburg\nBei Kindern besonders gefährlich\nHochansteckend!"));

        conditions.add(new MedicalCondition("110", "SARS-CoV-2 Variants", "COVID-19 Varianten",
            "covid, coronavirus, corona, omicron, delta, long covid, covid-19, SARS",
            "1. Schnelltest/PCR\n2. Isolation 5 Tage nach Symptombeginn\n3. Bei Risikogruppe: Paxlovid\n4. Symptomatische Behandlung\n5. Bei Atemnot: 112\n6. Impfung schützt vor schwerem Verlauf\n7. Bei Long COVID: Post-COVID-Ambulanz",
            "Bei leichter Erkrankung: Paracetamol\nBei Risikogruppe: Paxlovid (nur mit Rezept!)\nBei Atemnot: 112\nIsolation einhalten\nBei Long COVID: Post-COVID-Ambulanz",
            "BEI POSITIVEM TEST + ATEMNOT: 112!\nBei Kindern: Meist milder Verlauf\nParacetamol bei Fieber\nBei Risikokindern (Immunschwäche): Sofort zum Kinderarzt!\nIsolation einhalten\nImpfschutz für Risikopatienten\nLong COVID auch bei Kindern möglich"));

        conditions.add(new MedicalCondition("111", "Monkeypox", "Affenpocken",
            "monkeypox, mpox, smallpox related, rash, blisters, affenpocken, pockenvirus",
            "1. Bei typischem Ausschlag: Arzt\n2. Abstriche für PCR\n3. Isolation\n4. Bei schwer: Tecovirimat (TPOXX)\n5. Kontaktpersonen überwachen\n6. Bei Kindern und Schwangeren: Sofort Arzt\n7. Pockenimpfung kann schützen",
            "Bei leichtem Verlauf: Abwarten\nBei schwerem Verlauf: Tecovirimat (TPOXX) im Krankenhaus\nIsolation 3-4 Wochen\nKontaktpersonen: 21 Tage überwachen\nPockenimpfung bietet Teilschutz",
            "BEI VERDACHT: Sofort zum Kinderarzt!\nAffenpocken können Kinder betreffen\nBei Schwangeren besonders gefährlich\nTecovirimat für Kinder im Krankenhaus\nIsolation zu Hause möglich\nBei Kindern mit Immunschwäche: Besondere Vorsicht"));

        conditions.add(new MedicalCondition("112", "RS Virus", "RS-Virus",
            "rsv, respiratory syncytial virus, baby wheezing, cough baby, rs-virus, schnupfen baby",
            "1. Bei Säuglingen mit Atemnot: SOFORT Arzt!\n2. Inhalation mit Kochsalz\n3. Bei schwer: Krankenhaus (Sauerstoffgabe)\n4. Absaugen der Nase\n5. Viel trinken\n6. Fieber im Blick behalten\n7. Bei Frühgeborenen: Prophylaxe mit Palivizumab",
            "Bei schwerer RSV: Krankenhaus\nSauerstoffgabe\nInhalation mit Kochsalzlösung\nBei Apnoe: CPAP oder Intubation\nPalivizumab (Synagis) zur Prophylaxe bei Risikokindern",
            "BEI SÄUGLING MIT SCHNUPFEN + ATEMNOT: Sofort zum Kinderarzt/112!\nRS-Virus kann für Säuglinge gefährlich sein!\nNase absaugen mit Nasensauger\nBei Trinkverweigerung, Atemnot, blauen Lippen: 112!\nBei Frühgeborenen besonders gefährlich\nStationäre Aufnahme oft nötig"));

        conditions.add(new MedicalCondition("113", "SIDS", "Plötzlicher Kindstod",
            "sids, sudden infant death, baby stopped breathing, plötzlicher kindstod, säuglingstod",
            "1. NOTFALL: 112 SOFORT!\n2. Babys auf Rücken schlafen legen!\n3. Keine Kissen, Decken, Teddybären im Bettchen\n4. Raumtemperatur nicht über 18°C\n5. Nicht rauchen in der Umgebung\n6. Stillen schützt!\n7. Schnuller zum Schlafen geben",
            "PRÄVENTION IST WICHTIGST!\n- Baby auf den Rücken legen\n- Kein Überhitzen\n- Kein Rauchen\n- Fester Untergrund\n- Schnuller zum Schlafen\nBEI BEATMUNGSSTILLSTAND: 112 + Reanimation",
            "PRÄVENTION:\n- Baby immer auf den Rücken schlafen!\n- Kein Kopfkissen, keine Decke, kein Teddy\n- Raumtemperatur unter 18°C\n- Nicht rauchen!\n- Stillen schützt\n- Schnuller zum Schlafen\nBEI BEATMUNGSSTILLSTAND: 112 und baby reanimieren!\nEltern: Keine Panik bei Regelmäßigkeit - Vorbeugung rettet Leben!"));

        conditions.add(new MedicalCondition("114", "Crying Baby", "Schreibaby",
            "colic, crying baby, infant colic, excessive crying, schreibaby, kolik, übermäßiges schreien",
            "1. Bäuchlein massieren\n2. Wärmflasche (nicht zu heiß!)\n3. Beinchen nach Radfahren bewegen\n4. Tragetuch\n5. Weißes Rauschen\n6. Bei anhaltend über 3h: Arzt\n7. Bei Erbrechen, Fieber, Blut im Stuhl: SOFORT Arzt",
            "Bei Kolik: Sab Simplex, Lefax, Boneugh\nBäuchlein massieren\nWärme\nBei Unverträglichkeit: HA-Nahrung\nBei Stillkind: Kuhmilch der Mutter reduzieren",
            "BEI SCHREIENDEM BABY: Versuchen Sie es zu trösten!\n5-6h Schreien pro Tag kann normal sein (Fremdelphase)\nBei Fieber, Erbrechen, Blut im Stuhl: SOFORT Arzt!\nSchreien ist bei Babys normal\nAuszeit für Eltern wichtig!\nBei Erschöpfung: Familienhelfe, Kinderarzt kontaktieren!"));

        conditions.add(new MedicalCondition("115", "Teething", "Zahnen",
            "teething, baby teeth, gum swelling, drooling, zahnschmerzen baby, zahnen, sabbern",
            "1. Beißring kühlen\n2. Zahnungsgel auf die Kauflächen\n3. Bei Fieber: Zahnungstubliaden oder Paracetamol\n4. Kühlwaschlappen zum Kauen\n5. Zahnputzübungen mit Fingerzahnbürste\n6. Erster Zahnarztbesuch mit 1. Zahn",
            "Dentinox Zahnungsgel\nViburcol Zäpfchen (homöopathisch)\nBei Schmerzen/Fieber: Paracetamol Saft\nKühlender Beißring",
            "BEI ZAHNENDEM BABY: Kühlung hilft!\nBeißring in den Kühlschrank (nicht freezer!)\nZahnungsgel für Babys\nBei Fieber über 38.5°C: Arzt, nicht nur dem Zahnen zuschreiben!\nZahnung kann leichte Temperaturerhöhung, aber kein hohes Fieber\nErster Zahnarztbesuch: Mit Durchbruch des ersten Zahnes!"));

        conditions.add(new MedicalCondition("116", "Pneumothorax", "Pneumothorax",
            "pneumothorax, collapsed lung, spontaneous pneumothorax, lung collapse, pneumothorax, lung",
            "1. SOFORT 112! (Notfall!)\n2. Bei spontanem Pneumothorax: Ruhe bewahren\n3. Nicht rauchen\n4. Bei Atemnot: Sofort Arzt/112\n5. Thorax Röntgen\n6. Bei großem Pneumothorax: Drainage\n7. Bei wiederholtem: OP (Pleurodese)",
            "Bei spontanem Pneumothorax: Krankenhaus\nBei großem: Saugdrainage\nBei Spannungspneumothorax: Sofort Nadeldekompression + 112!\nSauerstoffgabe",
            "BEI PLÖTZLICHER ATEMNOT + BRUSTSCHMERZEN: 112 SOFORT!\nPneumothorax bei Kindern selten aber möglich\nNach Trauma: Sofort 112!\nBei rappeldünnem Brustkorb: Keine Nadel sondern Drainage\nKinder brauchen pädiatrische Intensivmedizin!"));

        conditions.add(new MedicalCondition("117", "Pulmonary Embolism", "Lungenembolie",
            "pulmonary embolism, pe, blood clot lung, sudden shortness of breath, lungenthrombus, lungenembolie",
            "1. SOFORT 112! (Lebensgefahr!)\n2. Sauerstoffgabe\n3. Blutverdünner (Heparin)\n4. CT mit Kontrastmittel\n5. Thrombolyse bei schweren Fällen\n6. Krankenhaus\n7. Langzeit Blutverdünner",
            "Im Krankenhaus: Heparin IV\nDann: Rivaroxaban (Xarelto) oder Marcumar\nBei massiver PE: Thrombolyse mit rtPA\nSauerstoff\nIntensivstation",
            "BEI PLÖTZLICHER ATEMNOT + BEINSSCHMERZEN: 112 SOFORT!\nLungenembolie kann tödlich sein!\nBei Kindern: Meist durch Katheter oder Krankenhaus\nAkute Diagnostik und Therapie im Krankenhaus\nLangzeitprophylaxe wichtig"));

        conditions.add(new MedicalCondition("118", "Heart Failure", "Herzinsuffizienz",
            "heart failure, chf, congestive heart failure, swollen legs, shortness of breath, herzinsuffizienz, herzschwäche",
            "1. SOFORT zum Kardiologen!\n2. Entwässerung (Diuretika)\n3. Herzmedikamente (ACE-Hemmer, Betablocker)\n4. Salzrestriktion\n5. Gewicht täglich kontrollieren\n6. Bei Verschlechterung: sofort Arzt\n7. Bei akuter Verschlechterung: 112",
            "Furosemid (Lasix) 40-80mg\nRamipril (Delix) oder Candesartan\nMetoprolol\nBei akuter: Nitro + Sauerstoff + Furosemid IV\nKardiologe!",
            "BEI ATEMNOT IM LIEGEN + GESCHWOLLENE BEINE: Sofort zum Kinderkardiologen!\nHerzinsuffizienz bei Kindern: Angeborene Herzfehler!\nMedikamente kinderspezifisch dosiert\nBei akuter Verschlechterung: 112!\nPädiatrischer Kardiologe notwendig"));

        conditions.add(new MedicalCondition("119", "Heart Attack", "Herzinfarkt",
            "heart attack, myocardial infarction, mi, chest pain, heart pain, herzinfarkt, myocardial infarction",
            "1. SOFORT 112! (Jede Minute zählt!)\n2. Kauen von Aspirin 300mg\n3. Hinlegen mit erhöhtem Oberkörper\n4. Bei Bewusstlosigkeit: Reanimation\n5. Nicht alleine lassen\n6. Defibrillator wenn vorhanden\n7. Krankenhaus mit Herzkatheter",
            "SOFORT 112!\nAspirin 300mg kauen\nNitrospray unter die Zunge\nBei Bewusstlosigkeit: Reanimation\nIm Krankenhaus: Herzkatheter + Stent",
            "BEI BRUSTSCHMERZEN + SCHWEISSAUSBRUCH + ÜBELKEIT: 112 SOFORT!\nHerzinfarkt bei Kindern: Sehr selten (meist angeboren)\nAber möglich bei Kawasaki oder Familiäre Hypercholesterinämie\nBei Verdacht auf Kind: Sofort pädiatrische Intensivstation!\nReanimation vorbereiten"));

        conditions.add(new MedicalCondition("120", "Stroke", "Schlaganfall",
            "stroke, cerebral infarction, apoplexy, face drooping, arm weakness, speech difficulty, schlaganfall, hirnschlag",
            "1. SOFORT 112! (Zeitfenster für Lyse!)\n2. F-A-S-T Test: Face, Arm, Speech, Time\n3. Bei Bewusstlosigkeit: Stabile Seitenlage\n4. Nicht essen/trinken\n5. Blutdruck nicht selbst senken\n6. Lyse-Therapie innerhalb 4.5h\n7. Thrombektomie möglich",
            "SOFORT 112!\nIm Krankenhaus: CT + Lyse oder Thrombektomie\nAspirin nach Ausschluss Blutung\nBlutdruck senken falls >185/110\nZeitfenster: 4.5h für Lyse",
            "BEI VERDACHT: 112 SOFORT!\nSchlaganfall bei Kindern: Andere Ursachen als bei Erwachsenen!\n- Angeborene Herzfehler\n- Gefäßmissbildungen\n- Sichelzellanämie\nSofort pädiatrische Schlaganfall-Spezialisten!\nZeitfenster beachten!"));

        conditions.add(new MedicalCondition("121", "Angina Pectoris", "Angina Pectoris",
            "angina, chest pain, heart chest tightness, coronary heart disease, angina pectoris, herzkranzgefäße",
            "1. Bei Brustenge: Sofort hinlegen\n2. Nitrospray anwenden\n3. Bei Ruhe nicht besser in 15 Min: 112\n4. Aspirin 300mg\n5. Stress vermeiden\n6. Kardiologe für Diagnostik\n7. Langzeittherapie mit nitrates/Betablocker",
            "Nitrospray (Nitrolingual)\nAspirin 300mg\nBei Ruhe-Schmerz nicht besser: 112!\nIm Krankenhaus: Herzkatheter\nLangzeit: Betablocker + Nitrate",
            "BEI BRUSTENGE BEI ANSTRENGUNG: Zum Kinderkardiologen!\nAngina bei Kindern: Angeborene Herzfehler oder Kawasaki-Folgen\nNitrospray für Kinder nicht getestet\nDiagnostik notwendig\nKinder brauchen spezialisierte Betreuung"));

        conditions.add(new MedicalCondition("122", "Pericarditis", "Herzbeutelentzündung",
            "pericarditis, chest pain, heart sac inflammation, fever, perikarditis, herdsackentzündung",
            "1. Sofort zum Kardiologen!\n2. EKG und Herzultraschall\n3. Entzündungshemmende Medikamente\n4. Bei tamponade: Sofort Drainage\n5. Ruhe\n6. Bei virale: Meist selbstlimitierend\n7. Bei bakterieller: Antibiotika",
            "Ibuprofen 600mg 3x tgl\nBei schwer: Prednisolon\nBei Perikarditiserguss: Perikardpunktion\nBei Tamponade: Sofort Drainage im Krankenhaus",
            "BEI BRUSTSCHMERZEN + LIEGEN BESSER: Sofort zum Kinderkardiologen!\nPerikarditis bei Kindern: Meist viral\nHerzultraschall notwendig\nMedikamente kinderspezifisch\nBei Atemnot: 112!\nKinder brauchen pädiatrische Kardiologen"));

        conditions.add(new MedicalCondition("123", "Endocarditis", "Herzinnenhautentzündung",
            "endocarditis, infection heart valve, fever, heart murmur, endokarditis, infektiöse endokarditis",
            "1. SOFORT zum Kardiologen!\n2. Blutkulturen abnehmen\n3. Antibiotika IV über 4-6 Wochen\n4. Herzultraschall (TEE)\n5. Bei Klappenschaden: OP\n6. Endokarditisprophylaxe bei Risikopatienten\n7. Bei Fieber + Herzgeräusch: Arzt",
            "Im Krankenhaus: Ampicillin + Gentamicin\nOder Vancomycin + Gentamicin\nNach Blutkultur: gezielte Antibiotika\nBei Klappenschaden: Herzchirurgie",
            "BEI FIEBER + HERZGERÄUSCH: Sofort zum Kinderkardiologen!\nEndokarditis bei Kindern: Oft bei angeborenen Herzfehlern\nBlutkulturen vor Antibiotika!\nLange Antibiotikatherapie 4-6 Wochen\nBei Klappenbefall: Herzchirurgie\nEndokarditisprophylaxe wichtig!"));

        conditions.add(new MedicalCondition("124", "Aortic Aneurysm", "Aortaneurysma",
            "aortic aneurysm, dilated aorta, back pain, abdominal aortic aneurysm, aortenaneurysma",
            "1. Bei plötzlichen starken Rücken/Bauchschmerzen: 112!\n2. CT oder MRT zur Diagnose\n3. Bei kleinem <5cm: Kontrollen\n4. Bei großem >5.5cm oder schnell wachsend: OP\n5. Blutdrucksenkung\n6. Gefäßchirurg\n7. Bei Ruptur: Sofort OP/112",
            "Bei hypertonie: Betablocker (Metoprolol)\nRamipril oder Losartan\nBei Ruptur: Notfall-OP\nCT/MRT zur Diagnose\nGefäßchirurg",
            "BEI PLÖTZLICHEN STARKEN RÜCKENSCHMERZEN: 112 SOFORT!\nAortenaneurysma bei Kindern: Selten (Marfan-Syndrom, Ehlers-Danlos)\nSofort in spezialisierte Kinderklinik!\nGenetische Beratung bei Marfan\nRegelmäßige CT/MRT Kontrollen\nBei Dissektion: Notfall-OP!"));

        conditions.add(new MedicalCondition("125", "Deep Vein Thrombosis", "Beinvenenthrombose",
            "dvt, deep vein thrombosis, leg swelling, leg pain, blood clot leg, beinvenenthrombose, thrombose",
            "1. Sofort zum Angiologen!\n2. Kompressionsstrumpf\n3. Blutverdünner (NOAK oder Heparin)\n4. Bei Verdacht auf Lungenembolie: 112\n5. Ursache abklären\n6. Bewegun beginning\n7. Langzeittherapie",
            "Rivaroxaban 15mg 2x tgl 3 Wochen, dann 20mg 1x tgl\nOder: niedermolekulares Heparin (Clexane)\nKompressionsstrumpf Klasse II\nBei PE-Verdacht: 112",
            "BEI GESCHWOLLENEM BEIN + ATEMNOT: 112 SOFORT!\nDVT bei Kindern: Selten aber möglich (Katheter, PICC-Line)\nSofort zum pädiatrischen Angiologen!\nUltraschall zur Diagnose\nBlutverdünner für Kinder speziell dosiert\nBei Lungenembolie-Symptomen: 112!"));

        conditions.add(new MedicalCondition("126", "Raynaud's Phenomenon", "Raynaud-Syndrom",
            "raynaud, cold fingers, color changes fingers, white blue red fingers, raynaud, durchblutungsstörung finger",
            "1. Finger warmen (nicht direkt erhitzen!)\n2. Stress reduzieren\n3. Nicht rauchen\n4. Bei häufigen Attacken: Kalziumantagonisten\n5. Bei offenen Stellen: Wundversorgung\n6. Kardiologe/Rheumatologe\n7. Bei Infektion: Sofort Arzt",
            "Nifedipin (Adalat) 10-20mg 3x tgl\nBei schwer: Bosentan (Tracleer) für pulmonale Hypertonie\nHandwärmer bei Kälte\nNicht rauchen!",
            "BEI WEISSEN/BLAUEN FINGERN BEI KÄLTE: Zum Kinderarzt!\nRaynaud bei Kindern: Meist primär (gutartig)\nBei sekundärem Raynaud: Grunderkrankung abklären\nWärme hilft!\nMedikamente für Kinder evtl. nötig\nBei Geschwüren: Wundversorgung"));

        conditions.add(new MedicalCondition("127", "Lupus", "Systemischer Lupus Erythematodes",
            "lupus, sle, autoimmune disease, butterfly rash, joint pain, lupus, schmetterlingserythem",
            "1. Sofort zum Rheumatologen!\n2. Bluttest (ANA, Anti-DNS, Complement)\n3. Hautschutz vor Sonne\n4. Medikamente: Hydroxychloroquin, Kortison\n5. Bei Organbeteiligung: Immunsuppressiva\n6. Regelmäßige Kontrollen\n7. Nephrologie bei Nierenbeteiligung",
            "Hydroxychloroquin (Quentiax) 200-400mg tgl\nPrednisolon bei Schub\nBei Organbeteiligung: Mycophenolat oder Azathioprin\nBei Nephritis: Cyclophosphamid\nSonnenschutz LSF 50+",
            "BEI VERDACHT: Sofort zum Kinderarzt/Rheumatologen!\nLupus bei Kindern: Oft schwererer Verlauf als bei Erwachsenen!\nSofortige Diagnostik notwendig\nMedikamente für Kinder speziell angepasst\nBei Nierenbeteiligung: Nephrologe\nMultidisziplinäre Betreuung nötig\nSonneschutz extrem wichtig!"));

        conditions.add(new MedicalCondition("128", "Rheumatoid Arthritis", "Rheumatoide Arthritis",
            "rheumatoid arthritis, ra, chronic joint inflammation, morning stiffness, rheumatoide arthritis, chronische gelenkentzündung",
            "1. Sofort zum Rheumatologen!\n2. Bluttest (RF, Anti-CCP, ESR, CRP)\n3. Physiotherapie\n4. Medikamente: Basistherapeutika, Biologika\n5. Gelenkschutz\n6. Regelmäßige Kontrollen\n7. Bei Kindern: Still's Disease",
            "Methotrexat 15-25mg 1x Woche + Folsäure\nBei unzureichend: Biologika (Humira, Enbrel)\nBei Schub: niedrig dosiertes Kortison\nNSAR für Schmerzen\nPhysiotherapie",
            "BEI VERDACHT: Sofort zum Kinderarzt/Rheumatologen!\nJuvenile idiopathische Arthritis (JIA) bei Kindern!\nFrühe Diagnose und Therapie wichtig!\nMedikamente für Kinder speziell\nPhysiotherapie für Kinder\nGelenkschutz + Bewegung\nRegelmäßige Augenuntersuchungen (Uveitis!)"));

        conditions.add(new MedicalCondition("129", "Scleroderma", "Sklerodermie",
            "scleroderma, hardened skin, raynaud, tight skin, sklerodermie, systemische sklerose",
            "1. Sofort zum Rheumatologen!\n2. Hautbiopsie\n3. Bei diffuser Form: aggressive Therapie\n4. Bei limitierter Form: Monitor\n5. Physiotherapie\n6. Nierenfunktion überwachen (Crisis!)\n7. Hautpflege",
            "Bei diffuser systemische: Methotrexat + Prednisolon\nFür Raynaud: Nifedipin\nBei renaler Krise: ACE-Hemmer hochdosiert\nPhysiotherapie\nHautpflege mit ureahaltigen Cremes",
            "BEI VERDACHT: Sofort zum Kinderarzt/Rheumatologen!\nSklerodermie bei Kindern: Selten aber möglich\nHautverhärtung + Raynaud = verdächtig\nSofort in spezialisierte Klinik!\nBei Nierenkrise: Sofort ACE-Hemmer!\nHautpflege wichtig\nPhysiotherapie für Kinder"));

        conditions.add(new MedicalCondition("130", "Sjögren's Syndrome", "Sjögren-Syndrom",
            "sjogren, dry eyes, dry mouth, autoimmune, sjögren, sicca-syndrom",
            "1. Sofort zum Rheumatologen!\n2. Bluttest (Anti-SSA, Anti-SSB)\n3. Tränenersatzmittel für Augen\n4. Künstlicher Speichel\n5. Bei Organbeteiligung: Immunsuppressiva\n6. Zahnarzt für Prophylaxe\n7. Regelmäßige Kontrollen",
            "Tränenersatzmittel (Artelac, Siccafluid)\nKünstlicher Speichel (Biotene)\nBei Gelenkschmerzen: Hydroxychloroquin\nBei Organbeteiligung: Prednisolon oder Mycophenolat",
            "BEI TROCKENE AUGEN + TROCKENER MUND: Zum Kinderarzt!\nSjögren bei Kindern: Selten\nTrockenheit kann auch andere Ursachen haben\nZahnarztliche Kontrollen wichtig\nBei Blutbildveränderungen: Hämatologe\nInterdisziplinäre Betreuung"));

        conditions.add(new MedicalCondition("131", "Polymyositis", "Polymyositis",
            "polymyositis, muscle inflammation, weakness, muscle pain, polymyositis, muskelentzündung",
            "1. Sofort zum Rheumatologen!\n2. Muskelenzyme (CK, LDH)\n3. MRT der Muskeln\n4. Muskelbiopsie\n5. Hochdosiertes Kortison\n6. Bei Lungenbeteiligung: Immunsuppressiva\n7. Physiotherapie",
            "Prednisolon 1mg/kg起始\nMethotrexat als Steroid-sparend\nBei schwer: IVIG oder Rituximab\nPhysiotherapie\nBei interstitieller Lungenerkrankung: Mycophenolat",
            "BEI MUSKELSCHWÄCHE + HAUTAUSSCHLAG: Sofort zum Kinderarzt!\nBei Kindern: Juvenile Dermatomyositis!\nHaut + Muskel = verdächtig\nSofort Therapie beginnen!\nKortison für Kinder hochdosiert nötig\nPhysiotherapie für Kinder\nTumor ausschließen!"));

        conditions.add(new MedicalCondition("132", "Vasculitis", "Vaskulitis",
            "vasculitis, blood vessel inflammation, purpura, vasculitis, gefäßentzündung",
            "1. Sofort zum Rheumatologen!\n2. Biopsie oder Angiographie\n3. Bluttest (ANCA, CRP, ESR)\n4. Je nach Typ: Kortison + Immunsuppressiva\n5. Bei Kawasaki: Sofort IVIG + Aspirin\n6. Bei GPA/MPA: Rituximab oder Cyclophosphamid\n7. Organfunktionen überwachen",
            "Bei Kawasaki: IVIG 2g/kg über 10h + Aspirin 30mg/kg\nBei ANCA-Vaskitis: Cyclophosphamid + Prednisolon\nBei IgA-Vaskitis (Henoch-Schönlein): Oft selbstlimitierend\nBei HSP: Nephrologe bei Nierenbeteiligung",
            "BEI HAUTBLUTUNGEN + GELENKSCHMERZEN + BAUCHSCHMERZEN: Sofort zum Kinderarzt!\nVaskulitis bei Kindern: Verschiedene Typen!\n- Kawasaki (Säuglinge!)\n- IgA-Vaskitis (Henoch-Schönlein)\n- ANCA-Vaskitis\nSofort in spezialisierte Klinik!\nFrühe Therapie wichtig!"));

        conditions.add(new MedicalCondition("133", "Sarcoidosis", "Sarkoidose",
            "sarcoidosis, lung inflammation, granulomas, sarcoid, lungenfibrose, sarkoidose",
            "1. Sofort zum Pneumologen!\n2. Röntgen/CT der Lunge\n3. Biopsie (Klinik oder Lymphknoten)\n4. Bei Stadium II-IV: Therapie nötig\n5. Kortison first-line\n6. Bei schwer: Methotrexat, Biologika\n7. Augenarzt (Uveitis!), EKG, Lungenfunktion",
            "Prednisolon 20-40mg tgl起始\nBei Nichtansprechen: Methotrexat\nBei schwerer Lungenerkrankung: Infliximab\nAugenuntersuchung, EKG, Lungenfunktion alle 3-6 Monate",
            "BEI VERDACHT: Sofort zum Kinderarzt/Pneumologen!\nSarkoidose bei Kindern: Selten\nDiagnose durch Biopsie\nStadieneinteilung wichtig\nKortison für Kinder oft nötig\nAugen, Herz, Nieren überwachen\nMultidisziplinäre Betreuung"));

        conditions.add(new MedicalCondition("134", "Cystic Fibrosis", "Mukoviszidose",
            "cystic fibrosis, cf, lung disease, pancreatic insufficiency, mukoviszidose, cystische fibrose",
            "1. Sofort zum Mukoviszidose-Zentrum!\n2. Schweißtest (Chlorid >60)\n3. Mutationsanalyse\n4. Inhalationstherapie (DNAase, Bronchodilatator)\n5. Physiotherapie (Klopfen)\n6. Pankreasenzyme ersetzen\n7. Kalorienreiche Ernährung",
            "Im Mukoviszidose-Zentrum:\nDNAase (Pulmozyme) Inhalation\nBronchodilatatoren\nBei Infektion: Antibiotika (oft Ciprofloxacin)\nPankreasenzyme (Creon)\nFettlösliche Vitamine (A, D, E, K)\nKalorienreiche Diät",
            "BEI VERDACHT: Sofort zum Kinderarzt!\nMukoviszidose: Häufigste angeborene Stoffwechselkrankheit!\nNeugeborenenscreening in Deutschland!\nBei positivem Screening: Sofort ins Mukoviszidose-Zentrum!\n lebenslange Betreuung im Zentrum\nModerne Therapien verbessern Prognose deutlich\nBei Atemnot: Sofort ins Zentrum!"));

        conditions.add(new MedicalCondition("135", "Celiac Disease", "Zöliakie",
            "celiac disease, gluten intolerance, diarrhea, failure to thrive, zöliakie, glutensensitivität",
            "1. Sofort zum Gastroenterologen!\n2. Anti-Transglutaminase Antikörper (tTG-IgA)\n3. Dünndarmbiopsie\n4. Lebenslange glutenfreie Diät!\n5. Bei Dermatitis herpetiformis: Dapsone\n6. Ernährungsberatung\n7. Kontrollen (Knochendichte, Vitamine)",
            "Lebenslange glutenfreie Diät!\n- Kein Weizen, Roggen, Gerste\n- Hafer eventuell vertragen\nBei Dermatitis herpetiformis: Dapsone\nCalcium + Vitamin D Supplement\nBei Anämie: Eisentpönung",
            "BEI DURCHFALL + GEWICHTSVERLUST + AUFGEBLÄHTEM Bauch: Sofort zum Kinderarzt!\nZöliakie bei Kindern: Häufig!\nAntikörpertest (tTG-IgA) Screening\nBiopsie zur Bestätigung\nStrenge glutenfreie Diät für immer!\nErnährungsberatung für Eltern\nBei Kindern: Wachstum überwachen\nKnochenentwicklung kontrollieren"));

        conditions.add(new MedicalCondition("136", "Crohn's Disease", "Crohn-Krankheit",
            "crohn's disease, crohn, ibd, inflammatory bowel disease, abdominal pain, crohn, chronische darmentzündung",
            "1. Sofort zum Gastroenterologen!\n2. Darmspiegelung mit Biopsie\n3. MRT oder CT (Strikuren, Fisteln)\n4. Medikamente: Kortison, Immunsuppressiva, Biologika\n5. Bei Komplikationen: OP\n6. Ernährungstherapie (Trinknahrung)\n7. Regelmäßige Kontrollen",
            "Bei Schub: Prednisolon 40-60mg tgl\nAls Erhaltung: Azathioprin (Imurek) 2-2.5mg/kg\nBei Versagen: Infliximab (Remicade) oder Adalimumab\nBei Mangel: Eisen, Vitamin B12, Vitamin D\nErnährungstherapie",
            "BEI BLUTIGEM DURCHFALL + BAUCHSCHMERZEN + GEWICHTSVERLUST: Sofort zum Kinderarzt!\nMorbus Crohn bei Kindern: Immer häufiger!\nDarmspiegelung zur Diagnose\nModerne Biologika helfen Kindern!\nErnährungstherapie oft notwendig\nWachstum und Pubertät überwachen\nBei Kindern oft ausgedehntere Erkrankung als bei Erwachsenen"));

        conditions.add(new MedicalCondition("137", "Ulcerative Colitis", "Colitis Ulcerosa",
            "ulcerative colitis, uc, bloody diarrhea, ibd, colitis ulcerosa, ulceröse kolitis",
            "1. Sofort zum Gastroenterologen!\n2. Coloskopie mit Biopsie\n3. Blut- und Stuhlproben\n4. Medikamente: 5-ASA, Kortison, Immunsuppressiva\n5. Bei Versagen: Biologika oder JAK-Inhibitoren\n6. Bei Kolektomie: Stoma-Pouch\n7. Regelmäßige Kontrollen (Krebsvorsorge)",
            "Mesalazin (Salofalk) 2-4g tgl als 5-ASA\nBei schwerem Schub: Prednisolon 40-60mg tgl\nAzathioprin als Erhaltung\nBei Versagen: Infliximab, Adalimumab oder Tofacitinib\nBei toxischem Megakolon: Sofort Kolektomie",
            "BEI BLUTIGEM DURCHFALL + DRANG: Sofort zum Kinderarzt!\nColitis ulcerosa bei Kindern: Möglich\nColoskopie zur Diagnose\nMedikamente für Kinder angepasst\nBiologika helfen Kindern\nBei Kindern oft schwerer Verlauf\nLebenslange Überwachung wegen Krebsrisiko\nErnährungsberatung"));

        conditions.add(new MedicalCondition("138", "Irritable Bowel Syndrome", "Reizdarmsyndrom",
            "ibs, irritable bowel, abdominal pain, bloating, irregular bowel, reizdarm, nervöser darm",
            "1. Zum Gastroenterologen!\n2. Ausschluss von CED!\n3. Symptomlinderung\n4. Ballaststoffe oder Flohsamenschalen\n5. Stressreduktion, Psychotherapie\n6. Bei Verstopfung: Macrogol\n7. Bei Durchfall: Loperamid",
            "Bei Verstopfung: Macrogol (Movicol) 1-3 Beutel tgl\nBei Durchfall: Loperamid (Imodium) 2mg nach Stuhl\nBei Blähungen: Simeticon (Lefax)\nFlohsamenschalen (Metamucil) 1-3x tgl\nPsychotherapie (CBT)",
            "BEI WIEDERKEHRENDEN BAUCHSCHMERZEN + AUFBLÄHUNG: Zum Kinderarzt!\nReizdarmsyndrom bei Kindern: Häufig!\nAusschluss von CED wichtig!\nBallaststoffe helfen!\nStress bei Kindern beachten\nPsychotherapie für Kinder\nErnährungstagebuch führen\nBei Kindern: Schule und Psyche einbeziehen"));

        conditions.add(new MedicalCondition("139", "Hepatitis A", "Hepatitis A",
            "hepatitis a, liver inflammation, jaundice, fecal-oral, hepatitis a, gelbsucht",
            "1. Zum Infektiologen!\n2. Bluttest (Anti-HAV-IgM)\n3. Ruhe und Schonkost\n4. Kein Alkohol!\n5. Bei Kindern oft mild\n6. Bei Erwachsenen schwerer Verlauf\n7. Hygiene maßnahmen (kein Kindergarten/Schule 2 Wochen)",
            "Nur symptomatische Therapie!\n- Ruhe\n- Leichte Ernährung\n- Kein Alkohol\n- Paracetamol nur wenn nötig und niedrig dosiert (Leber schonen!)\nBei Ikterus: Bilirubin und Leberwerte kontrollieren\nBei schwerer Hepatitis: Krankenhaus",
            "BEI GELBSUCHT + MÜDIGKEIT + ÜBELKEIT: Sofort zum Kinderarzt!\nHepatitis A bei Kindern: Meist mild oder asymptomatisch!\nAnsteckend! Gute Händehygiene\nBei Kindern meist selbstlimitierend\nHepatitis A Impfung schützt!\nRückfall möglich (bis 20% haben cholestatische Phase)\nLeberwerte überwachen"));

        conditions.add(new MedicalCondition("140", "Hepatitis B", "Hepatitis B",
            "hepatitis b, liver disease, sexually transmitted, hepatitis b, chronische leberentzündung",
            "1. Sofort zum Gastroenterologen/Infektiologen!\n2. Bluttest (HBsAg, Anti-HBs, HBeAg)\n3. Bei chronischer Hepatitis: Therapie!\n4. Entecavir oder Tenofovir first-line\n5. Bei Akut: Meist selbstlimitierend\n6. Kontaktpersonen impfen\n7. Regelmäßige Kontrollen (Leberkrebs!)",
            "Bei chronischer Hepatitis B:\n- Entecavir (Baraclude) 0.5mg tgl\n- Oder Tenofovir (Viread) 245mg tgl\nBei HBeAg-positiv: Serokonversion anstreben\nKontrolle alle 6 Monate: Leberwerte, HBV-DNA, Ultrasound\nImpfung schützt!",
            "BEI GELBSUCHT + MÜDIGKEIT + RISIKOFAKTOREN: Sofort zum Kinderarzt!\nHepatitis B bei Kindern: Oft chronisch!\nÜbertragung: Geburt (Mutter) oder Blut/Körperflüssigkeiten\nBei Neugeborenen: Sofort Impfung + HBIG!\nChronische Hepatitis: Therapie mit Entecavir/Tenofovir\nLeberkrebsrisiko erhöht - Überwachung nötig\nImpfung für alle Kinder empfohlen!"));

        conditions.add(new MedicalCondition("141", "Hepatitis C", "Hepatitis C",
            "hepatitis c, liver disease, blood transmission, hepatitis c, chronische leberentzündung c",
            "1. Sofort zum Gastroenterologen/Infektiologen!\n2. Bluttest (Anti-HCV, HCV-RNA)\n3. Genotyp bestimmen\n4. Bei chronisch: Direkt antivirale (DAA)!\n5. 8-12 Wochen Therapie\n6. Erfolgskontrolle (SVR)\n7. Regelmäßige Leberkontrollen",
            "Moderne DAA (direkt antivirale Mittel):\n- Sofosbuvir/Velpatasvir (Epclusa) 12 Wochen\n- Glecaprevir/Pibrentasvir (Maviret) 8 Wochen\nBei Genotyp 1,2,3,4,5,6 wirksam!\nSVR >95%!\nBei Leberzirrhose: angepasste Therapie",
            "BEI VERDACHT: Sofort zum Kinderarzt!\nHepatitis C bei Kindern: Übertragung oft bei Geburt oder Blutprodukte\nModerne DAA heilen fast alle!\nBei Kindern ab 3 Jahren: Maviret zugelassen\nTherapie für Kinder wichtig wegen Langzeitfolgen\nLeber überwachen\nBei Neugeborenen von HCV-positiven Müttern: Testen mit 2-4 Jahren"));

        conditions.add(new MedicalCondition("142", "Fatty Liver Disease", "Fettleber",
            "fatty liver, nafld, nash, liver fat, overweight, fettleber, nicht-alkoholische fettleber",
            "1. Sofort zum Gastroenterologen!\n2. Ultraschall der Leber\n3. Bluttest (Leberwerte, Blutzucker, Cholesterin)\n4. Gewichtsreduktion!\n5. Sport und Bewegung\n6. Bei NASH: Vitamin E, Pioglitazon\n7. Bei Zirrhose: Lebertransplantation",
            "Gewichtsreduktion 5-10%!\n- Bewegung 150min/Woche moderat\n- Kalorienreduktion\nBei NASH: Vitamin E 800IE oder Pioglitazon 15-30mg\nStatine bei Hyperlipidämie\nBei Diabetes: gute Einstellung\nBei Zirrhose: Transplantationszentrum",
            "BEI ERHÖHTEN LEBERWERTEN + ÜBERGEWICHT: Sofort zum Kinderarzt!\nFettleber bei Kindern: Immer häufiger durch Adipositas!\nUltraschall zur Diagnose\nBei Kindern: Gewichtsreduktion durch Ernährung + Sport\nFructose reduzieren!\nLeberwerte und Stoffwechsel überwachen\nBei Kindern oft reversibel durch Lebensstiländerung!\nBei NASH: Pädiatrischer Gastroenterologe"));

        conditions.add(new MedicalCondition("143", "Gallstones", "Gallensteine",
            "gallstones, gallbladder stones, biliary colic, right upper abdomen pain, gallensteine, gallerenblase",
            "1. Sofort zum Chirurgen!\n2. Ultraschall\n3. Bei Beschwerden: Gallenblase entfernen (Laparoskopie)\n4. Medikamente (Ursodeoxycholsäure) nur bei kleinen Steinen\n5. Fettarme Ernährung\n6. Bei Gelbsucht oder Entzündung: SOFORT OP\n7. Diät nach OP: Fett reducieren",
            "Bei symptomatischen Gallensteinen: Cholezystektomie (Laparoskopie)\nUrsodeoxycholsäure (Ursofalk) nur bei kleinen Cholesteringsteinen\nBei akuter Cholezystitis: Sofort Antibiose + OP\nBei Choledocholithiasis: ERCP + Sphinkterotomie",
            "BEI RECHTEN OBERBAUCHSCHMERZEN NACH FETTESSEN: Sofort zum Kinderarzt!\nGallensteine bei Kindern: Früher selten, jetzt durch Adipositas häufiger!\nUltraschall zur Diagnose\nBei Kindern: Oft mit Adipositas oder Sichelzellanämie\nGallenblase entfernen (Laparoskopie) wenn nötig\nFettreduzierte Ernährung\nMedikamente für Kinder nur eingeschränkt wirksam"));

        conditions.add(new MedicalCondition("144", "Pancreatitis", "Bauchspeicheldrüsenentzündung",
            "pancreatitis, pancreatic inflammation, epigastric pain, pancreatitis, pankreatitis",
            "1. SOFORT zum Krankenhaus!\n2. Nahrungskarenz (NPO)\n3. IV Flüssigkeit\n4. Schmerzmittel\n5. Bei nekrotisierender: Antibiotika, ggf. OP\n6. Ursache finden (Gallensteine, Alkohol, Medikamente)\n7. Bei rezidivierend: Vorsorge",
            "Im Krankenhaus:\n- NPO (nichts per os)\n- IV Flüssigkeit (Ringer-Laktat)\n- Schmerzmittel (Buprenorphin oder Fentanyl)\n- Bei infizierter Nekrose: Imipenem + ggf. Drainage/OP\nUrsache abklären!",
            "BEI STARKEN OBERBAUCHSCHMERZEN + ERBRECHEN: 112 SOFORT!\nPankreatitis bei Kindern: Meist durch Medikamente, Infektionen oder Trauma!\nSofort ins Krankenhaus!\nKind braucht pädiatrische Gastroenterologie/Intensivstation\nNahrungskarenz für Kinder manchmal schwierig\nUrsache bei Kindern anders als bei Erwachsenen\nBei rezidivierender: Stoffwechseluntersuchungen"));

        conditions.add(new MedicalCondition("145", "Diabetes Type 1", "Diabetes Typ 1",
            "diabetes type 1, t1dm, insulin dependent, juvenile diabetes, polydipsia, polyuria, diabetes typ 1",
            "1. SOFORT zum Kinderarzt/Diabetesambulanz!\n2. Blutzucker und Ketone messen\n3. Insulin therapy!\n4. Schulung (BE zählen, Korrektur)\n5. HbA1c alle 3 Monate\n6. Augenarzt, Nierenfunktion, Füße\n7. Bei DKA: Sofort Krankenhaus/112",
            "Intensivierte Insulintherapie:\n- Basis-Bolus (Levemir + NovoRapid)\n- Oder Insulinpumpe\nBE-Faktor und Korrekturfaktor lernen\nHbA1c Ziel: <7.5% (53 mmol/mol)\nBei DKA: Sofort Krankenhaus mit Insulin- und Flüssigkeitstherapie",
            "BEI VIEL TRINKEN + VIEL URINieren + GEWICHTSVERLUST: Sofort zum Kinderarzt!\nDiabetes Typ 1 bei Kindern: Häufigste Stoffwechselkrankheit im Kindesalter!\nSofort Blutzucker + Ketone messen!\nBei DKA (Ketoazidose): SOFORT Krankenhaus Lebensgefahr!\nInsulin muss sofort beginnen!\nKinderdiabetologie und Schulung für Eltern und Kind\nLebenslange Insulintherapie\nTechnologie (Sensoren, Pumpen) hilft Kindern!"));

        conditions.add(new MedicalCondition("146", "Hypoglycemia", "Unterzuckerung",
            "hypoglycemia, low blood sugar, shakiness, sweating, confusion, hypoglykämie, unterzuckerung",
            "1. Bei Bewusstlosigkeit: 112 + Stabile Seitenlage\n2. Bei ansprechbar: Traubenzucker 4-5 Täfelchen\n3. Fruchtsaft 200ml\n4. Bei Glucagon: Spritzen/nasenspray\n5. Ursache finden\n6. Mahlzeitenregelmäßigkeit\n7. Bei repeat episodes: Diabetologe",
            "Bei mild: 4-5 Traubenzuckerplättchen oder 200ml Fruchtsaft\nBei schwer (bewusstlos): Glucagon 0.5-1mg s.c./i.m. oder nasal (Baqsimi)\nIm Krankenhaus: 20-30% Glukose IV\nUrsache bei Diabetikern: Überdosis Insulin, versäumte Mahlzeit, Sport",
            "BEI BEWUSSTLOSIGKEIT + SCHWITZEN + ZITTERN: 112 SOFORT!\nUnterzuckerung bei Kindern: Notfall!\nBei Diabetikern: Schnell Kohlenhydrate geben!\nGlucagon für Kinder wichtet!\nBaqsimi (nasales Glucagon) für Kinder ab 4 Jahren\nUrsache bei Kindern: Insulinom, Stoffwechselerkrankungen, Medikamente\nBei repeatierter Hypoglykämie: Pädiatrische Abklärung!"));

        conditions.add(new MedicalCondition("147", "Hyperthyroidism", "Schilddrüsenüberfunktion",
            "hyperthyroidism, graves disease, weight loss, palpitations, heat intolerance, schilddrüsenüberfunktion",
            "1. Sofort zum Endokrinologen!\n2. Bluttest (TSH, fT3, fT4)\n3. Bei Graves: Thiamazol (Metizol)\n4. Bei Sturm: Lugol's Lösung + Betablocker + Kortison\n5. Radiojodtherapie nur außerhalb Schwangerschaft\n6. Bei Kindern: Thiamazol first-line\n7. OP (Thyreoidektomie) bei Versagen/Nebenwirkungen",
            "Thiamazol (Metizol) 10-40mg tgl je nach Schwere\nBei tyreotoxischer Krise:\n- Lugol's Lösung (Kaliumjodid) 5 Trpf 3x tgl\n- Propranolol 40-80mg 4x tgl\n- Prednisolon 50mg 4x tgl\n- Sofort Krankenhaus!",
            "BEI HERZRASEN + GEWICHTSVERLUST + SCHWITZEN: Sofort zum Kinderarzt!\nBei Kindern: Meist Morbus Basedow (autoimmun)!\nThiamazol für Kinder zugelassen\nBei tyreotoxischer Krise: Sofort 112!\nMedikamente für Kinder speziell dosiert\nRadiojod bei Kindern vermieden\nBei Versagen: OP durch erfahrenen Chirurgen\nBetablocker für schnelle Symptomkontrolle"));

        conditions.add(new MedicalCondition("148", "Hypothyroidism", "Schilddrüsenunterfunktion",
            "hypothyroidism, hashimoto, fatigue, weight gain, cold intolerance, schilddrüsenunterfunktion, hashimoto",
            "1. Sofort zum Endokrinologen!\n2. Bluttest (TSH, fT4)\n3. Bei primär: Levothyroxin (L-Thyroxin) 1.5-2µg/kg\n4. Morgens nüchtern einnehmen\n5. TSH-Kontrolle alle 6-8 Wochen\n6. Bei Hashimoto: Selten OP nötig\n7. Bei angeboren: Sofort Therapie für Entwicklung!",
            "Levothyroxin (L-Thyroxin Henning) 50-100µg起始\nStartdosis: 1.5-2µg/kg bei Kindern\nBei Kindern: 3-5µg/kg möglich\nNüchtern 30min vor Frühstück einnehmen\nTSH alle 6-8 Wochen kontrollieren\nZiel: TSH 0.5-2.5 (altersabhängig)",
            "BEI MÜDIGKEIT + GEWICHTSZUNAHME + TROCKENER HAUT: Sofort zum Kinderarzt!\nAngeborene Hypothyreose: Neugeborenenscreening in Deutschland!\nBei positivem Screening: Sofort L-Thyroxin beginnen!\nNormale Entwicklung nur bei früher Behandlung!\nBei erworben: Hashimoto bei Kindern häufig!\nLevothyroxin für Kinder flüssig verfügbar\nTSH-Zielwerte bei Kindern anders als bei Erwachsenen\nWachstum und Entwicklung überwachen!"));

        conditions.add(new MedicalCondition("149", "Adrenal Insufficiency", "Nebenniereninsuffizienz",
            "adrenal insufficiency, addison disease, cortisol deficiency, fatigue, hypotension, nebenniereninsuffizienz",
            "1. SOFORT zum Endokrinologen!\n2. Bluttest (Cortisol, ACTH, Elektrolyte)\n3. Sofort Hydrocortison ersetzen!\n4. Bei Krise: Sofort 100mg Hydrocortison IV + Kochsalz!\n5. Stressdosis bei Stress/Infektion\n6. Notfallausweis und Armband\n7. Patient und Angehörige schulen",
            "Hydrocortison (Cortef) 15-25mg tgl in 2-3 Dosen\nBei Nebennierenkrise:\n- 100mg Hydrocortison IV sofort\n- 1000ml NaCl 0.9% über 30min\n- 2.Dosis Hydrocortison 100mg nach 2h\nNotfallausweis!\nBei Morbus Addison: lebenslange Substitution",
            "BEI SCHWERE MÜDIGKEIT + ERBRECHEN + KREISLAUFZUSAMMENBRUCH: 112 SOFORT!\nNebenniereninsuffizienz bei Kindern: Notfall!\nAngeborene (AGS) oder erworben (autoimmun)!\nSofort Hydrocortison im Notfall!\nEltern müssen Notfallmanagement kennen!\nBei Kindern mit AGS: Genetische Beratung\nStressdosis bei Fieber über 38°C, Erbrechen, Impfungen\nNotfallarmband!"));

        conditions.add(new MedicalCondition("150", "Cushing's Syndrome", "Cushing-Syndrom",
            "cushing syndrome, excess cortisol, moon face, obesity, hypertension, cushing-syndrom, hypercortisolismus",
            "1. Sofort zum Endokrinologen!\n2. 24h-Urin Cortisol, Speichelcortisol nachts\n3. Dexamethason-Hemmtest\n4. MRT der Hypophyse (Hypophysenadenom!)\n5. Bei Hypophysen-CT: OP (transsphenoidal)\n6. Bei Nebenniere: Adrenalektomie\n7. Bei iatrogen: Steroidreduktion",
            "Bei Morbus Cushing: Transsphenoidale OP durch erfahrenen Neurochirurgen\nBei Nebennierenadenom: Einseitige Adrenalektomie\nBei Cushing-Syndrom durch Medikamente: Dosisreduktion wenn möglich\nNach OP: Kortisonsubstitution temporär nötig\nBei Nebennierenkarzinom: Ketoconazol + OP + ggf. Mitotan",
            "BEI RUNDLICHEM GESICHT + STREIFEN AM BAUCH + HOCHDRUCK: Sofort zum Kinderarzt!\nCushing bei Kindern: Selten aber möglich!\nMeist durch Hypophysenadenom oder Nebennierenadenom!\nOP oft heilend!\nVor Diagnose: Keine Steroidgaben absetzen!\nMedikamente für Kinder speziell\nNach OP: Vorübergehende Nebenniereninsuffizienz möglich\nLangzeitüberwachung wichtig"));

        conditions.add(new MedicalCondition("151", "Growth Hormone Deficiency", "Wachstumshormonmangel",
            "growth hormone deficiency, ghd, short stature, growth failure, wachstumshormonmangel, kleinwuchs",
            "1. Sofort zum Pädiatrischen Endokrinologen!\n2. Röntgen Alter (Bone Age)\n3. Wachstumskurven überprüfen\n4. IGF-1 und IGFBP-3 im Blut\n5. Stimulations test (Arginin, Insulin)\n6. Bei Mangel: Wachstumshormon (Somatropin)\n7. Regelmäßige Kontrollen (Wachstum, TSH, Stoffwechsel)",
            "Somatropin (Genotropin, Norditropin) 0.025-0.05mg/kg tgl\nTägliche subkutane Injektionen\nZiel: Wachstum 2-3cm/Jahr über normale Rate\nBei Turner-Syndrom, SGA, Prader-Willi: Höhere Dosen\nKontrolle alle 3-6 Monate: Größe, IGF-1, Schilddrüse",
            "BEI KLEINEM WACHSTUM: Sofort zum Kinderarzt!\nWachstumshormonmangel bei Kindern: Verschiedene Ursachen!\nDiagnose durch Stimulationstest!\nTherapie mit Somatropin für Kinder zugelassen\nInjektionen täglich - Schulung für Eltern und Kind\nBei frühem Beginn: Bessere Ergebnisse\nÜberwachung: Wachstum, Schilddrüse, Stoffwechsel\nGenetische Syndrome ausschließen!"));

        conditions.add(new MedicalCondition("152", "Precocious Puberty", "Vorzeitige Pubertät",
            "precocious puberty, early puberty, pubarche, thelarche, Pubertätsentwicklung zu früh",
            "1. Sofort zum Pädiatrischen Endokrinologen!\n2. Röntgen Alter (Bone Age)\n3. LH, FSH, Östradiol/Testosteron\n4. Bei Zentraler Pubertät: GnRH-Analoga\n5. Bei peripherer: Ursache behandeln\n6. Psychologische Betreuung\n7. Ziel: Normal adult height",
            "Bei zentraler Pubertät: Leuprolid (Enantone) 3.75mg alle 4 Wochen i.m.\nOder Triptorelin (Decapeptyl) 3.75mg alle 4 Wochen\nZiel: Pubertät pausieren bis normales Alter\nBone Age Fortschritt verlangsamen\nBei peripherer: Ursache behandeln",
            "BEI BRUSTENTWICKLUNG VOR 8.LEBENSJAHR (MÄDCHEN): Sofort zum Kinderarzt!\nBEI HODENVOLUMEN >4ml VOR 9.LEBENSJAHR (JUNGEN): Sofort zum Kinderarzt!\nVorzeitige Pubertät bei Kindern: Immer häufiger!\nGnRH-Analoga für Kinder zugelassen\nTherapie verhindert Kleinwuchs\nBone Age bestimmen!\nBei Mädchen: Erhöhtes Risiko für PCOS, Adipositas\nBei Jungen: Ursache (Hypothalam Hamartom?!) abklären\nPsychologische Betreuung für Kind und Eltern"));

        conditions.add(new MedicalCondition("153", "Delayed Puberty", "Pubertas tarda",
            "delayed puberty, late puberty, no development, primäre amenorrhoe, verzögerte pubertät",
            "1. Sofort zum Pädiatrischen Endokrinologen!\n2. Bluttest (LH, FSH, Östradiol, Testosteron, Schilddrüse)\n3. Chromosomenanalyse (Karyotyp!)\n4. MRT der Hypophyse\n5. Bei Hypogonadismus: Geschlechtshormone\n6. Bei Turner/Klinefelter: Spezifische Therapie\n7. Psychologische Betreuung",
            "Bei Hypergonadotropem Hypogonadismus:\n- Mädchen: Östradiol 2mg tgl, dann steigern\n- Jungen: Testosteron Enantat 50mg/Monat i.m., dann steigern\nBei Kallmann-Syndrom: GnRH-Pumpe oder Gonadotropine\nZiel: Pubertät einleiten und Knochenreifung fördern",
            "BEI KEINER PUBERTÄTSENTWICKLUNG BIS 13 (MÄDCHEN) / 14 (JUNGEN): Sofort zum Kinderarzt!\nBei Jungen: Keine Hodenvergrößerung bis 14 Jahre = Abklärung!\nUrsachen bei Kindern: Turner-Syndrom (Mädchen), Klinefelter (Jungen), Hypophyseninsuffizienz!\nHormonersatz für Kinder wichtig\nKnochenalter bestimmen\nGenetische Abklärung bei Mädchen: Karyotyp!\nPsychologische Betreuung wichtig\nBei Hypophysenproblemen: MRT!"));

        conditions.add(new MedicalCondition("154", "Polycystic Ovary Syndrome", "PCOS",
            "pcos, polycystic ovaries, irregular periods, hirsutism, acne, polyzystisches ovarsyndrom",
            "1. Sofort zum Gynäkologen/Endokrinologen!\n2. Bluttest (LH, FSH, Testosteron, AMH, Stoffwechsel)\n3. vaginaler Ultraschall\n4. Lebensstiländerung (Gewicht, Sport)\n5. Bei Kinderwunsch: Clomifen\n6. Bei nicht: Hormonpille\n7. Metformin bei Stoffwechselproblemen",
            "Lebensstiländerung: Gewichtsreduktion 5-10%\nBei Zyklusstörungen: Hormonpille (Yasmin, Belara)\nBei Hirsutismus: Diane 35 oder Spironolacton\nBei Kinderwunsch: Clomifen 50mg tgl Tag 5-9\nMetformin 500-1500mg tgl bei Stoffwechselproblemen",
            "BEI UNREGELMÄSSIGEN PERIODEN + HIRSUTISMUS + AKNE: Sofort zum Kinderarzt/Frauenarzt!\nPCOS bei Jugendlichen: Immer häufiger!\nDiagnose bei Jugendlichen: Schwieriger als bei Erwachsenen\nLebensstiländerung für Kinder wichtig!\nHormonpille für Jugendliche möglich\nMetformin für Kinder ab 12 Jahren möglich\nBei Kinderwunsch: Clomifen für Jugendliche möglich\nLangzeitfolgen: Diabetes, Herz-Kreislauf-Risiko\nPsychologische Betreuung!"));

        conditions.add(new MedicalCondition("155", "Turner Syndrome", "Turner-Syndrom",
            "turner syndrome, 45xo, short stature, webbed neck, ovarian failure, turner-syndrom",
            "1. Sofort zum Pädiatrischen Endokrinologen!\n2. Chromosomenanalyse (Karyotyp)\n3. Herz MRT (Aortenisthmusstenose!)\n4. Nierensonographie\n5. Wachstumshormon (Somatropin)\n6. Bei Aortenisthmusstenose: OP\n7. Östrogenersatz bei Turner-Frauen",
            "Somatropin (Genotropin) 0.05mg/kg tgl bei Kleinwuchs\nÖstradiol substitution ab 12-14 Jahre: 2mg tlg start, dann steigern\nBei Herzfehler: Betablocker oder OP\nBei Östrogenmangel: Hormonersatztherapie (Östrogen + Gestagen)\nRegelmäßige Audiogramm (Mittelohr!)\nBei Kinderwunsch: Eizellspende möglich",
            "BEI KLEINEM WACHSTUM + MÄDCHEN OHNE PUBERTÄTSENTWICKLUNG: Sofort zum Kinderarzt!\nTurner-Syndrom bei Mädchen: Chromosomenanalyse diagnostisch!\nKardiovaskuläre Malformationen (Aortenisthmusstenose) ausschließen!\nNiere: Hufeisenniere häufig\nWachstumshormon für Kinder hilft!\nÖstrogenersatz für Pubertät und Knochen wichtig\nLebenslange Überwachung: Herz, Niere, Schilddrüse, Hören\nBei Kinderwunsch: Reproduktionsmedizin!"));

        conditions.add(new MedicalCondition("156", "Klinefelter Syndrome", "Klinefelter-Syndrom",
            "klinefelter syndrome, 47xxy, small testes, tall stature, gynecomastia, klinefelter-syndrom",
            "1. Sofort zum Endokrinologen/Urologen!\n2. Chromosomenanalyse (Karyotyp: 47,XXY)\n3. Hormonstatus (Testosteron, LH, FSH)\n4. Bei Hypogonadismus: Testosteronersatz\n5. Bei Gynäkomastie: Gynäkomastie-OP\n6. Spermaentnahme ggf. (ICSI)\n7. Psychologische Betreuung",
            "Testosteron Enantat 250mg alle 3 Wochen i.m. oder tägliches Gel\nStart: 12-14 Jahre je nach Pubertätsentwicklung\nBei Kinderwunsch: Mikro-TESE + ICSI\nBei Gynäkomastie: Operation wenn kosmetisch störend\nSprachtherapie bei Kindern oft nötig\nLernschwierigkeiten bei Kindern möglich",
            "BEI KLEINEN HODEN + LANGEM WUCHS + GYNÄKOMASTIE (JUGENDLICHER): Sofort zum Kinderarzt!\nKlinefelter bei Jungen: Häufigste chromosomale Störung (1:500)!\nOft Diagnose erst in Pubertät oder bei Infertilität!\nTestosteronersatz für Jungen wichtig für Pubertät!\nBei Lernschwierigkeiten: Förderung\nBei Kinderwunsch: Reproduktionsmedizin (ICSI)!\n lebenslange Überwachung: Metabolisches Syndrom, Osteoporose, Hodenkrebsrisiko!"));

        conditions.add(new MedicalCondition("157", "Marfan Syndrome", "Marfan-Syndrom",
            "marfan syndrome, tall stature, long limbs, aortic aneurysm, lens dislocation, marfan-syndrom",
            "1. Sofort zum Genetiker/Kardiologen!\n2. Ghent Kriterien erfüllt?\n3. Echokardiographie (Aorta!)\n4. Augenärztliche Untersuchung\n5. Bei Aortenektasie: Betablocker oder Losartan\n6. Bei Aneurysma >4.5cm: OP\n7. Sport einschränken (Kontaktsport, Schwerathletik)",
            "Betablocker (Metoprolol oder Atenolol) oder Losartan 50-100mg tgl\nZiel: Aortawachstum verlangsamen <0.5cm/Jahr\nBei Aortendurchmesser >4.5-5cm (bei Kindern <4cm): OP (Bentall oder David)\nVermeiden: Isometrische Belastung, Kontaktsport\nEngmaschige echokardiographische Kontrollen alle 6-12 Monate",
            "BEI GROSSEM WACHSTUM + LANGEN GLIEDMAßEN + HERZGERÄUSCH: Sofort zum Kinderarzt!\nMarfan bei Kindern: Diagnose oft schwierig!\nGhent Kriterien anwenden\nAortendurchmesser bei Kindern: Z-Score wichtiger als Absolutwert!\nBetablocker für Kinder wichtig\nBei Sport: Einschränkungen (Basketball, Volleyball, Gewichtheben)!\nAortendissektion bei Kindern: Lebensgefahr!\nGenetische Beratung für Familie\nBei Schwangerschaft: Hohes Dissektionsrisiko!"));

        conditions.add(new MedicalCondition("158", "Neurofibromatosis", "Neurofibromatose",
            "neurofibromatosis, nf1, cafe-au-lait spots, neurofibromas, nf1, neurofibromatose",
            "1. Sofort zum Neurologen/Genetiker!\n2. Hautuntersuchung (Café-au-lait Flecken, Neurofibrome)\n3. Augenärztliche Untersuchung (Lisch-Knötchen!)\n4. MRT bei neurologischen Symptomen\n5. Bei Optikusgliom: Chemotherapie\n6. Bei Plexiform: OP\n7. Regelmäßige Kontrollen (Tumorüberwachung)",
            "Bei Optikusgliom: Carboplatin + Vincristin\nBei plexiformen Neurofibromen: Selumetinib (Koselugo) seit 2022\nBei Schmerzen: Gabapentin oder Pregabalin\nBei Aufmerksamkeitsdefizit: Methylphenidat\nOperative Entfernung bei funktioneller Beeinträchtigung\nJährliche Kontrollen: Augen, Haut, Wachstum, Lernen",
            "BEI CAFÉ-AU-LAIT FLECKEN + NEUROFIBROME: Sofort zum Kinderarzt!\nNF1 bei Kindern: Häufigste Tumorprädispositionssyndrom!\nDiagnose klinisch (NIH-Kriterien)!\nOptikusgliom bei Kindern: Häufigster Hirntumor bei NF1\nMedikamente (Selumetinib) für Kinder mit plexiformen Neurofibromen!\nLernschwierigkeiten bei 30-60%!\nRegelmäßige Überwachung lebenslang\nTumorüberwachung: Risiko für Optikusgliom, Astrozytom, Phäochromozytom!"));

        conditions.add(new MedicalCondition("159", "Tuberous Sclerosis", "Tuberöse Sklerose",
            "tuberous sclerosis, tsc, seizures, ashleaf spots, angiofibromas, tuberosklerose",
            "1. Sofort zum Neurologen/Genetiker!\n2. MRT des Schädels (Cortical Tubers, Subependymale Knötchen!)\n3. Hautuntersuchung (Ashleaf, Angiofibrome)\n4. Augenuntersuchung\n5. Bei Anfällen: Antiepileptika\n6. Bei SEGA: Everolimus (Rapamune)\n7. Bei renalen Angiomyolipomen: Everolimus",
            "Bei Krampfanfällen: Vigabatrin (Säuglinge) oder Sirolimus/Everolimus\nBei SEGA (Subependymales Riesenzellastrozytom): Everolimus (Rapamune) 5mg/m²\nBei renalen Angiomyolipomen: Everolimus\nBei Angiofibromen: Everolimus\nLebenslange Überwachung: Nieren, Gehirn, Haut",
            "BEI KRÄMpfEN + HELLEN HAUTFLECKEN + RETARDIERUNG: Sofort zum Kinderarzt!\nTuberöse Sklerose bei Kindern: Genetisch!\nHamartome in vielen Organen!\nNeugeborenenscreening in manchen Ländern\nEverolimus für Kinder zugelassen!\nBei kindlichen Krampfanfällen: Vigabatrin first-line\nBei SEGA: Everolimus kann Größe reduzieren!\nRegelmäßige MRT-Kontrollen (alle 1-3 Jahre)\nBei Entwicklungsverzögerung: Förderung\nGenetische Beratung!"));

        conditions.add(new MedicalCondition("160", "Epilepsy", "Epilepsie",
            "epilepsy, seizures, convulsions, seizure disorder, epilepsy, krampfanfälle",
            "1. SOFORT 112 bei erstem Anfall oder anhaltend!\n2. Bei Kindern: Pädiatrischer Neurologe\n3. EEG (Elektroenzephalogramm)\n4. MRT bei fokalen Anfällen\n5. Antiepileptika (AED)\n6. Bei Status epilepticus: Midazolam oder Diazepam rektal\n7. Lebensstil anpassen (Schlaf, Flackerlicht)",
            "Bei generalized tonisch-klonisch: Valproat (Ergenyl) 20-30mg/kg\nBei Absence: Ethosuximid (Petnidan) oder Valproat\nBei fokalen: Carbamazepin (Tegretal) oder Levetiracetam (Keppra)\nBei Status: Midazolam buccal (Dormicum) 0.3mg/kg\nFreiwilliger Führerschein nach 2.anfallsfrei Jahr",
            "BEI KRÄMPFEN + BEWUSSTLOSIGKEIT: 112 SOFORT!\nEpilepsie bei Kindern: Häufigste neurologische Erkrankung!\nErster Anfall = nicht immer Epilepsie!\nEEG für Diagnose unverzichtbar!\nAntiepileptika für Kinder je nach Anfallstyp\nMedikamente wirken bei 70%!\nBei Therapieresistenz: Keto-Diät oder Vagusstimulation\nLeben mit Epilepsie: Sport erlaubt (Schwimmen mit Aufsicht!)\nBei Schulkindern: Epilepsie-Beratung!"));

        conditions.add(new MedicalCondition("161", "Febrile Seizures", "Fieberkrämpfe",
            "febrile seizure, febrile convulsion, seizure with fever, fieberkrampf",
            "1. Kind hinlegen, stabile Seitenlage\n2. Fieber senken (Zäpfchen)\n3. Zeit stoppen (Dauer?)\n4. Bei >5min: Midazolam rektal/nasal oder Diazepam rektal\n5. Nach Anfall: Kind beruhigen\n6. Bei komplexem Fieberkrampf: EEG, LP\n7. Ursache des Fiebers finden",
            "Zur Fiebersenkung: Paracetamol 10-15mg/kg oder Ibuprofen 5-10mg/kg\nBei prolongiertem Krampf (>5min):\n- Midazolam buccal (Dormicum) 0.3mg/kg oder\n- Diazepam rektal (Valium) 0.5mg/kg\nBei komplexen Fieberkrämpfen: Neurologische Abklärung",
            "BEI KRÄMPFEN + FIEBER: Kind beruhigen, Zeit stoppen, 112 bei >5min!\nFieberkrämpfe bei Kindern: Häufig (2-5%)!\nMeist harmlos und selbstlimitierend!\nEinfache Fieberkrämpfe: <15min, generalisiert, einmalig\nKomplexe Fieberkrämpfe: >15min, fokal, wiederholt = EEG + LP!\nFieberursache finden (Mittelohrentzündung, Viren!)\nBei Fieber >38.5°C: Fiebersenkung wichtig\nFieberkrampf ist kein Epilepsierisiko erhöhend!\nBei Kindern unter 1 Jahr oder komplex: Pädiatrische Abklärung"));

        conditions.add(new MedicalCondition("162", "Meningitis", "Meningitis",
            "meningitis, stiff neck, fever, headache, light sensitivity, meningitis, nackensteife",
            "1. SOFORT 112! (Notfall!)\n2. Lumbalpunktion zur Diagnose!\n3. Sofort Antibiotika/Virostatika\n4. Bei erhöhtem ICP: Mannitol\n5. Intensivstation\n6. Dexamethason vor/mit Antibiotika\n7. Kontaktpersonen prophylaxe (Chemoprophylaxe)",
            "Bevor Lumbalpunktion: Blutkulturen + Ceftriaxon!\nBei bakterieller: Ceftriaxon 2g + Ampicillin (bei >50J) + Dexamethason 10mg\nBei Listerien: Ampicillin + Gentamicin\nBei HSV: Aciclovir 10mg/kg\nBei Kindern: Sofort pädiatrische Intensivstation!",
            "BEI FIEBER + NACKENSTEIFE + KOPFSCHMERZEN: 112 SOFORT!\nMeningitis bei Kindern: NOTFALL!\nFrühsymptome oft unspezifisch bei Kleinkindern!\nBei Säuglingen: Erbrechen, Schreien, Fontanelle wölben!\nLumbalpunktion: Eitrige (bakterielle) vs lymphozytäre (virale)\nBei eitriger: Sofort Antibiose vor LP!\nBakterielle Meningitis ohne Behandlung: Tod in Stunden!\nHäufigste Erreger bei Kindern: Neugeborene (B-Strep, E.coli), >1 Monat (Pneumokokken, Meningokokken)\nKontaktprophylaxe für enge Kontaktpersonen!"));

        conditions.add(new MedicalCondition("163", "Encephalitis", "Gehirnentzündung",
            "encephalitis, brain inflammation, confusion, fever, seizures, herpes simplex encephalitis, enzephalitis",
            "1. SOFORT 112!\n2. MRT + EEG\n3. Lumbalpunktion\n4. Sofort Aciclovir bei HSV!\n5. Bei autoimmun: Kortison oder Plasmapherese\n6. Intensivstation\n7. Rehabilitaiton",
            "Bei HSV: Aciclovir 10mg/kg alle 8h über 14-21 Tage\nBei bacterial: Ceftriaxon + Ampicillin\nBei autoimmun (Anti-NMDA): Methylprednisolon + Rituximab oder Plasmapherese\nBei Fokus: Antiepileptika\nRehabilitation: Physiotherapie, Ergotherapie, Logopädie",
            "BEI VERWIRRTHEIT + FIEBER + KRÄMPFEN: 112 SOFORT!\nEnzephalitis bei Kindern: Selten aber lebensbedrohlich!\nHSV-Enzephalitis: Häufigste behandelbare viral bedingte!\nAciclovir SOFORT bei Verdacht - keine Zeit verlieren!\nAutoimmune Enzephalitis (Anti-NMDA): Erkennung wichtig!\nFolgen: Kognitive Einschränkungen, Epilepsie, Verhaltensänderungen\nLangzeitrehabilitation oft nötig\nBei Verdacht: MRT, LP, EEG sofort!"));

        conditions.add(new MedicalCondition("164", "Guillain-Barré Syndrome", "Guillain-Barré-Syndrom",
            "guillain-barre, ascending paralysis, areflexia, gbs, muscle weakness, guillain-barré-syndrom",
            "1. SOFORT zum Neurologen/Krankenhaus!\n2. Lumbalpunktion (Eiweiß erhöht, Zel arm!)\n3. EMG/NLG (verzögerte Leitgeschwindigkeit!)\n4. Bei atemlähmung: Intensivstation + Beatmung!\n5. Plasmaaustausch oder IVIG\n6. Physiotherapie früh beginnen\n7. Langzeitrehabilitation",
            "Sofort Therapie beginnen:\n- IVIG 0.4g/kg über 5 Tage (Gamunex)\n- Oder Plasmapherese 4-6 Austausche\nBei Ateminsuffizienz: Intensivstation, Intubation, Beatmung\nÜberwachung: Atemfunktion (FVC, MIP), Herzrhythmus\nFrühe Physiotherapie: Lagerung, passive Bewegung",
            "BEI AUFSTEIGENDER LÄHMUNG + KEINER REFLEXE: 112 SOFORT!\nGuillain-Barré bei Kindern: Selten aber möglich!\nMeist nach Infekt (CMV, EBV, Campylobacter)!\nAtemfunktion überwachen!\nBei Kindern: IVIG oft First-line (besser verträglich als Plasmapherese)\nIntensivstation bei Ateminsuffizienz!\nDie meisten Kinder erholen sich gut!\nLange Rekonvaleszenz (Monate!)"));

        conditions.add(new MedicalCondition("165", "Multiple Sclerosis", "Multiple Sklerose",
            "multiple sclerosis, ms, demyelination, optic neuritis, diplopia, ms, multiple sklerose",
            "1. Sofort zum Neurologen!\n2. MRT (periventrikuläre Läsionen, Kontrastmittel)\n3. Lumbalpunktion (oligoklonale Banden!)\n4. Bei Schub: Hochdosis Kortison\n5. Basistherapie (Interferon, Glatirameracetat, Fingolimod)\n6. Bei Kindern: Sofort therapy\n7. Vitamin D substituieren",
            "Bei Schub: Methylprednisolon 500-1000mg iv 3-5 Tage\nBasistherapie für Kinder:\n- Interferon beta-1a (Avonex) oder beta-1b (Betaferon)\n- Glatirameracetat (Copaxone)\n- Fingolimod (Gilenya) ab 10 Jahren\nBei hoher Krankheitsaktivität: Natalizumab oder Alemtuzumab\nVitamin D3 4000-5000IE tgl!",
            "BEI SEHSTÖRUNG + DOPPELTSEHEN + GLIEDMAßENSCHWÄCHE: Sofort zum Kinderarzt!\nMS bei Kindern: Selten (2-5% aller MS), aber zunehmend erkannt!\nDiagnose: McDonald-Kriterien (MRT + LP)\nKinder haben oft mehr entzündliche активность than Erwachsene\nFrühtherapie wichtig für Outcome!\nInterferone für Kinder zugelassen\nFingolimod für Kinder ab 10 Jahren!\nVitamin D wichtig als Modulator\nBei Optikusneuritis: Sofort augenärztliche + neurologische Abklärung!"));

        conditions.add(new MedicalCondition("166", "Bell's Palsy", "Facialisparese",
            "bell's palsy, facial nerve paralysis, face drooping, bell's palsy, gesichtslähmung",
            "1. Sofort zum Neurologen/HNO!\n2. Bluttest (Borrelien, VZV, HSV)\n3. EMG bei schweren Fällen\n4. Bei Borreliose: Antibiotika\n5. Bei idiopathisch: Kortison innerhalb 72h\n6. Augenschutz (Tränenersatz, Pflaster)\n7. Physiotherapie Gesicht",
            "Bei idiopathisch: Prednisolon 1mg/kg (max 60mg) für 7-10 Tage, start innerhalb 72h\nBei Borreliose: Doxycyclin 200mg tgl (ab 9 Jahren) oder Amoxicillin (Kinder)\nBei VZV: Aciclovir + Prednisolon\nAugenschutz: Kunstlinsen (Siccafluid), Uhrglasverband nachts",
            "BEI PLÖTZLICHER GESICHTSLÄHMUNG: Sofort zum Kinderarzt!\nFacialisparese bei Kindern: Oft durch Borrelien (Zeckenbiss!)\nBorreliose ausschließen: Labor, ggf. LP!\nBei Borreliose: Antibiotika heilen oft!\nBei idiopathisch: Kortison hilft wenn früh gegeben\nAuge nicht schließen möglich = Augenschutz wichtig!\nPhysiotherapie für Gesicht bei Kindern\nBei 70-80%: Vollständige Erholung innerhalb 3-6 Monaten!\nBei unvollständiger Erholung: Botulinumtoxin für Synkinesien!"));

        conditions.add(new MedicalCondition("167", "Carpal Tunnel Syndrome", "Karpaltunnelsyndrom",
            "carpal tunnel, hand numbness, tingling, wrist pain at night, karpaltunnelsyndrom",
            "1. Zum Orthopäden/Neurologen!\n2. Neurologische Untersuchung (Tinel-Zeichen, Phalen-Test)\n3. NLG (verzögerte palmare Latenz!)\n4. Nachtschiene\n5. Bei schwer: Kortisoninjektion\n6. Bei Versagen: OP (Spaltung Retinakulum)\n7. Ursache abklären (Schilddrüse, Diabetes, Schwangerschaft)",
            "Nachtschiene in Neutralstellung\nBei Schmerzen/Parästhesien: Ibuprofen\nBei schweren/fail konservativ: Kortisoninjektion 1ml Triamcinolon\nBei Op: Retinakulumspaltung (offen oder endoskopisch)\nBei Rezidiv: Revision-OP\nRLS Tinel-Zeichen: Parästhesien bei Beklopfen des Karpaltunnels",
            "BEI TAUBEN FINGERN + BRENNENDEN HAND/NACHTS: Zum Kinderarzt!\nKarpaltunnelsyndrom bei Kindern: Selten!\nOft durch Überlastung (Handy, Tablet!)\nNachtschiene für Kinder anpassbar\nBei Kindern: OP nur bei Versagen der konservativen Therapie\nBei Kleinwuchs/Syndromen: Grunderkrankung behandeln\nPhysiotherapie für Kinder\nHandy-/Tablet-Nutzung bei Kindern begrenzen!"));

        conditions.add(new MedicalCondition("168", "Restless Legs Syndrome", "Restless Legs Syndrom",
            "restless legs, rls, uncomfortable legs, urge to move, zappeln in den Beinen, unruhige Beine",
            "1. Sofort zum Neurologen/Pneumologen!\n2. Eisenstatus prüfen (Ferritin, Transferrin-Sättigung!)\n3. Schlaflabor bei Kindern\n4. Bei Eisenmangel: Eisentherapie\n5. Bei schwer: Dopaminagonisten (Ropinirol, Rotigotin)\n6. Lebensstil (Abendliche routine, Schlafhygiene)\n7. Magnesium",
            "Bei Ferritin <50µg/L: Eisentherapie (Eisensuccinat 100mg tgl bis Ferritin >75µg/L)\nBei schwerem RLS: Ropinirol 0.5-2mg tgl oder Rotigotin Pflaster 1-3mg/24h\nBei Kindern: Dopaminagonisten off-label\nGute Schlafhygiene\nKoffein und Alkohol meiden",
            "BEI ZAPPELIGEN BEINEN + DRUCK ZU BEWEGEN + ABENDS SCHLIMMER: Sofort zum Kinderarzt!\nRLS bei Kindern: Zunehmend erkannt (2-4%)!\nOft mit Eisenmangel oder ADHS assoziiert!\nFerritin bei Kindern prüfen!\nBei Eisenmangel: Eisentherapie hilft oft!\nLebensstil für Kinder wichtig (Bildschirmzeit abends reduzieren!)\nDopaminagonisten für Kinder: Ropinirol möglich\nBei Schlafstörungen: Schlaflabor\nBei ADHS: Abklärung sinnvoll"));

        conditions.add(new MedicalCondition("169", "Sleep Apnea", "Schlafapnoe",
            "sleep apnea, snoring, breathing pauses, daytime sleepiness, schlafapnoe, schnarchen",
            "1. Sofort zum Pneumologen/Schlaflabor!\n2. Polysomnographie (PSG)\n3. Bei Adenoide/Tonsillen: HNO\n4. Bei OSA: CPAP-Therapie\n5. Gewichtsreduktion\n6. Bei Kindern: OP (Adenotonsillektomie) oft heilend\n7. Residual: Positiv atemwegsdruck (CPAP/BiPAP)",
            "Bei obstruktiver Schlafapnoe (OSA) durch Adenoide/Tonsillen:\n- Adenotonsillektomie (HNO-OP) = First-line bei Kindern!\nBei CPAP-Bedürftigkeit:\n- CPAP (AirSense 10) oder BiPAP\n- Druck: 5-12 mbar (altersabhängig)\nGewichtsreduktion bei Adipositas\nBei zentraler Schlafapnoe: Ursache (Herz, Neurologie!)",
            "BEI SCHNARCHEN + ATMUNGSAUSSETZER + TAGESMÜDIGKEIT BEI KINDERN: Sofort zum Kinderarzt!\nSchlafapnoe bei Kindern: Häufig durch vergrößerte Adenoide/Tonsillen!\nPolysomnographie (PSG) Goldstandard!\nBei Kindern: OP (Adenotonsillektomie) oft heilend!\nBei Adipositas: Gewichtsreduktion!\nUnbehandelt: Lernschwierigkeiten, Wachstumsstörungen, Herzbelastung\nCPAP für Kinder möglich (angepasste Masken)\nSchlaflabor für Kinder spezialisiert"));

        conditions.add(new MedicalCondition("170", "Narcolepsy", "Narkolepsie",
            "narcolepsy, excessive daytime sleepiness, cataplexy, sleep attacks, narkolepsie, schlafsucht",
            "1. Sofort zum Neurologen/Schlaflabor!\n2. Multiple Sleep Latency Test (MSLT)\n3. Polysomnographie (PSG) Nacht zuvor\n4. HLA-Bindung (DR15, DQ0602)\n5. Bei Kataplexie: Modafinil oder Natriumoxybat\n6. Geplante Schlafenszeiten\n7. Lebensstilanpassungen",
            "Bei starker Tagesschläfrigkeit: Modafinil 100-400mg tgl oder Methylphenidat (Ritalin) 10-20mg\nBei Kataplexie: Natriumoxybat (Xyrem) 4.5-9g tgl (aufgeteilt auf 2 Dosen) oder Venlafaxin\nSchlafhygiene und geplante Nickerchen (15-20min) 1-2x täglich\nVermeiden von Alkohol und Schlafmitteln",
            "BEI UNWIDERSTEHLICHER MÜDIGKEIT + MUSKELKRAFTVERLUST BEI EMOTIONEN: Sofort zum Kinderarzt!\nNarkolepsie bei Kindern: Zunehmend erkannt!\nKataplexie (Verlust Muskeltonus bei Emotionen) = pathognomonisch!\nDiagnose: PSG + MSLT\nModafinil für Kinder off-label, Methylphenidat möglich\nBei Kataplexie: Natriumoxybat für Kinder ab 7 Jahren (in USA zugelassen)\nSchulische Anpassungen wichtig!\nNarkolepsie nach H1N1-Impfung (Pandemrix): Kausal möglich!\nLebenslange Erkrankung!"));

        conditions.add(new MedicalCondition("171", "Chronic Fatigue Syndrome", "Chronisches Erschöpfungssyndrom",
            "chronic fatigue syndrome, cfs, me, long lasting fatigue, exhaustion, chronisches erschöpfungssyndrom, myalgische enzephalomyelitis",
            "1. Sofort zum Pädiatrischen Gastroenterologen!\n2. Ausschluss anderer Ursachen\n3. Pacing (Energiemanagement)\n4. Schrittweise Steigerung (GET)\n5. Bei Schmerzen: Schmerzmittel\n6. Psychotherapie\n7. Bei Kindern: Pädiatrische Spezialisten",
            "Keine kausale Therapie!\nPacing: Aktivitätsmanagement, nicht überfordern\nBei Schmerzen: Ibuprofen oder Amitriptylin\nBei Schlafstörungen: Schlafhygiene\nStufenweise Exercise Therapy (GET) umstritten\nPsychotherapie (CBT) kann helfen\nBei Kindern: Sofort ins Kinderspezialisten!",
            "BEI LANGFRISTIGER ERCHÖPFUNG + LEISTUNGSMINDERUNG: Sofort zum Kinderarzt!\nChronisches Erschöpfungssyndrom (ME/CFS) bei Kindern: Selten aber zunehmend!\nDiagnose durch Ausschluss!\nPost-Exertional Malaise (PEM) = Kernsymptom!\nPacing für Kinder sehr wichtig (nicht überfordern)!\nStufenweise Exercise Therapy (GET) bei Kindern umstritten\nBei Schulabwesenheit: Pädagogische Unterstützung\nPsychologische Betreuung\nBei Viralem Beginn (EBV): Sofort Diagnostik!"));

        conditions.add(new MedicalCondition("172", "Tourette Syndrome", "Tourette-Syndrom",
            "tourette syndrome, tics, motor tics, vocal tics, choppy, tourette, tiks",
            "1. Sofort zum Neurologen/Psychiater!\n2. Yale Global Tic Severity Scale (YGTSS)\n3. Verhaltensbeobachtung\n4. Bei leichten Tics: Watchful Waiting\n5. Bei moderate: Alpha-2-Agonisten (Clonidin)\n6. Bei schwer: Antipsychotika (Risperidon, Aripiprazol)\n7. CBIT Therapie",
            "Bei leichten Tics: Aufklärung und Monitoring\nBei mittelgradigen Tics: Clonidin 0.025-0.1mg tgl oder Guanfacin\nBei schweren Tics: Risperidon 0.5-2mg tgl oder Aripiprazol 2-10mg tgl\nTopiramat oder Botulinumtoxin bei fokalen Tics\nCBIT (Comprehensive Behavioral Intervention for Tics) für Kinder",
            "BEI WIEDERKEHRENDEN TICS + LAUTEN TICS: Sofort zum Kinderarzt!\nTourette bei Kindern: Häufig (1-3%)!\nTics: Motorisch (Blinzeln, Kopfbewegungen) + Vokal (Räuspern, Hüsteln)!\nCBIT Therapie für Kinder am besten untersucht!\nBei Kindern: Kombinierte Verhaltens- + Medikamententherapie\nAntipsychotika für Kinder: Aripiprazol besser verträglich als Haloperidol\nBei Schulproblemen: Pädagogische Unterstützung\nBei ADHS/OCD: Komorbide Behandlung wichtig\nTics können bis ins Erwachsenenalter persistieren!"));

        conditions.add(new MedicalCondition("173", "Autism Spectrum Disorder", "Autismus-Spektrum-Störung",
            "autism, asd, autistic spectrum, social communication, repetitive behaviors, autismus, autismus spektrum störung",
            "1. Sofort zum Kinderpsychater!\n2. ADOS-2 Test (Goldstandard)\n3. ADI-R Interview\n4. Intelligenztest (IQ)\n5. Frühförderung so früh wie möglich!\n6. Verhaltenstherapie (ABA)\n7. Bei Sprachstörungen: Logopädie",
            "Frühförderung ab Diagnoseverdacht!\nVerhaltenstherapie: ABA (Applied Behavior Analysis), TEACCH\nBei ADHS-Komorbidität: Methylphenidat\nBei Reizbarkeit: Risperidon oder Aripiprazol\nBei Schlafstörungen: Melatonin\nBei Ängsten: SSRI (Fluoxetin) für Kinder",
            "BEI WENIGER SOZIALKONTAKT + WIEDERKEHRENDE HANDLUNGEN: Sofort zum Kinderarzt!\nAutismus bei Kindern: Häufigkeit 1-2%!\nFrüherkennung: 18-24 Monate Screening!\nWichtige Warnsignale: Kein Zeigen, keine Blickteilung, kein Gestaltengebrauch bis 12 Monate\nFrühförderung entscheidend für Prognose!\nBei Kindern: Verhaltenstherapie (ABA), TEACCH\nBei Schulproblemen: Integration oder Förderschule\nSprachtherapie für Kinder\nBei herausforderndem Verhalten: Funktionale Verhaltensanalyse\nElterntraining wichtig!"));

        conditions.add(new MedicalCondition("174", "Attention Deficit Hyperactivity Disorder", "ADHS",
            "adhd, attention deficit, hyperactivity, impulsivity, add, adhs, aufmerksamkeitsdefizit",
            "1. Sofort zum Kinderpsychiater!\n2. ADHD Rating Scale (ADHD-RS)\n3. Intelligenztest\n4. Ausschluss von Seh-/Hörstörungen\n5. Medikamente: Methylphenidat oder Atomoxetin\n6. Verhaltenstherapie (Elterntraining)\n7. Schulische Anpassungen",
            "Stimulanzien:\n- Methylphenidat (Ritalin, Concerta) 10-60mg tgl\n- Lisdexamfetamin (Elvanse) 30-70mg tgl\n\nNicht-Stimulanzien:\n- Atomoxetin (Strattera) 40-80mg tgl\n\nVerhaltenstherapie für Kinder + Eltern!\nBei Opposition: Guanfacin oder Clonidin",
            "BEI AUFMERKSAMKEITSPROBLEMEN + UNRUHIGKEIT + IMPULSIVITÄT: Sofort zum Kinderarzt!\nADHS bei Kindern: Häufigkeit 3-7%!\nDiagnose nach DSM-5 Kriterien!\nMedikamente für Kinder: Methylphenidat ab 6 Jahren zugelassen!\nStimulanzien wirken bei 70-80%!\nVerhaltenstherapie + Medikation = beste Strategie!\nElterntraining (Triple P, Starke Eltern, Starke Kinder)\nSchulische Anpassungen (extra Zeit, Nachteilsausgleich)\nBei Kindern: Non-Stimulanzien wenn Stimulanzien kontraindiziert\nCo-Morbiditäten: Ticstörungen, Lernbehinderungen, Angststörungen"));

        conditions.add(new MedicalCondition("175", "Oppositional Defiant Disorder", "Trotzstörung",
            "odd, oppositional defiant, defiance, anger outbursts, trotzstörung, oppositionelles verhalten",
            "1. Sofort zum Kinderpsychiater!\n2. Verhaltensbeurteilungsskalen (CBCL)\n3. Ausschluss von anderen Störungen\n4. Elterntraining (Triple P, Starke Eltern)\n5. Soziales Kompetenztraining für Kinder\n6. Bei schwer: Fluoxetin oder Risperidon\n7. Schule einbeziehen",
            "Elterntraining: Triple P (Positive Parenting Program)!\nKinder-Verhaltenstraining: Aggressionsmanagement\nBei externalen Problemen: Risperidon 0.5-2mg tgl\nBei depressiver Symptomatik: Fluoxetin (Fluctin) 10-20mg tgl\nBei Impulsivität: Methylphenidat wenn ADHS-Komorbidität\nSchulische Intervention: Klassenraummanagement",
            "BEI WIDERSPENSTIGEM VERHALTEN + TROTZANFÄLLEN + AGGRESSION: Sofort zum Kinderarzt!\nTrotzstörung bei Kindern: Häufig (3-10%)!\nOft komorbid mit ADHS, Lernstörungen, Angst!\nFrühe Intervention wichtig!\nElterntraining für Kinder am wirksamsten!\nBei Kindern: Verhaltenstherapie first-line!\nMedikamente nur bei Komorbiditäten\nBei Schulproblemen: Schulpsychologischer Dienst\nPrävention: Positive Erziehungsstrategien!"));

        conditions.add(new MedicalCondition("176", "Conduct Disorder", "Störung des Sozialverhaltens",
            "conduct disorder, cd, aggression, delinquency, rule breaking, verstöße gegen soziale normen",
            "1. SOFORT zum Kinderpsychiater!\n2. Verhaltensbeurteilung\n3. Familienanamnese\n4. Multifamilientherapie\n5. Sozialkompetenztraining\n6. Bei schwer: Antipsychotika\n7. Bei Straffälligkeit: Jugendgerichtshilfe",
            "Multifaktorielle Intervention:\n- Familientherapie (MTFC)\n- Sozialkompetenztraining\n- Schulisches Training\nBei schwerer aggressiver Form: Risperidon 0.5-3mg tgl oder Aripiprazol\nBei Impulsivität: Methylphenidat wenn ADHS\nBei depressiver Symptomatik: Fluoxetin\nBei Kindern: Langzeittherapie oft nötig!",
            "BEI SCHWERER AGGRESSION + REGELBRUCH + STRAFTATEN: Sofort zum Kinderpsychiater!\nStörung des Sozialverhaltens bei Kindern: 2-10%!\nGefahr der Entwicklung zur antisozialen Persönlichkeitsstörung!\nFrüher Beginn (vor 10 Jahren) = schlechtere Prognose!\nBei Kindern: Intensive Familientherapie!\nMultisystemische Therapie (MST) für Kinder bewährt\nBei Straffälligkeit: Jugendgericht + Jugendhilfe\nPrävention: Frühe Hilfen für Familien!"));

        conditions.add(new MedicalCondition("177", "Separation Anxiety Disorder", "Trennungsangst",
            "separation anxiety, clingy child, school refusal, separation anxiety disorder, trennungsangst",
            "1. Sofort zum Kinderpsychiater!\n2. Angstfragebögen (SCARED)\n3. Ausschluss von somatischen Ursachen\n4. Kognitive Verhaltenstherapie (KVT)\n5. Bei schwer: Sertralin oder Fluvoxamin\n6. School-based Intervention\n7. Elternberatung",
            "Kognitive Verhaltenstherapie (KVT) für Kinder first-line!\nBei schwerer Angst: SSRI\n- Sertralin (Gladem) 25-100mg tgl oder\n- Fluvoxamin (Fevarin) 50-100mg tgl\nElterntraining: Angst reduzieren, Kind ermutigen schrittweise zu trennen\nBei Schulverweigerung: School refusal Programm",
            "BEI STARKEM FESTHALTEN + SCHULVERWEIGERUNG + ÜBERTRIEBENER ANGST: Sofort zum Kinderarzt!\nTrennungsangst bei Kindern: Normal bis 3-4 Jahre!\nWenn abnormal heftig und anhaltend: Trennungsangststörung!\nBei Kindern: KVT für Kinder bewährt!\nSSRI für Kinder mit schwerer Trennungsangst möglich!\nSchule einbeziehen (stufenweise Eingliederung)\nBei Kindern: Eltern sollten Konsequenz zeigen, aber auch Sicherheit vermitteln\nSport und Aktivitäten fördern Selbstvertrauen\nBei teenage:区分 Trennungsangst von anderen Angststörungen!"));

        conditions.add(new MedicalCondition("178", "Social Anxiety Disorder", "Soziale Phobie",
            "social anxiety, social phobia, fear of social situations, speaking in front of people, soziale phobie",
            "1. Sofort zum Kinderpsychiater!\n2. Angstfragebögen (SAD, SCARED)\n3. Kognitive Verhaltenstherapie (KVT)\n4. Bei schwer: SSRI (Sertralin, Fluoxetin)\n5. Soziales Kompetenztraining\n6. Graduierte Exposition\n7. Schulische Intervention",
            "KVT für Kinder: Erstlinientherapie!\n- Kognitive Umstrukturierung\n- Rollenspiele\n- Graduierte Exposition\nBei schwerer sozialer Angst: SSRI\n- Sertralin 50-150mg tgl oder\n- Fluoxetin 20-40mg tgl\nBei Performancesangst: Propranolol 10-40mg 1h vor Event",
            "BEI ANGST IN SOZIALEN SITUATIONEN + REDEN VOR GRUPPEN: Sofort zum Kinderarzt!\nSoziale Phobie bei Kindern: 5-10%!\nKinder fallen durch Stillstand, Clownerie oder Rückzogenheit auf!\nBei Kindern: KVT für Kinder bewährt!\nSSRI für Kinder möglich (Sertralin, Fluoxetin)!\nPropranolol für Prüfungsangst (Kinder ab 8 Jahren möglich)!\nSchulische Intervention wichtig\nSportliche Aktivitäten können Selbstbewusstsein stärken\nBei Kindern: Früherkennung wichtig für Schulkarriere!"));

        conditions.add(new MedicalCondition("179", "Specific Phobia", "Spezifische Phobie",
            "specific phobia, fear of spiders, dogs, heights, blood, spezifische phobie, plötzliche angst",
            "1. Sofort zum Kinderpsychiater!\n2. Angstfragebögen\n3. Kognitive Verhaltenstherapie (KVT)\n4. Expositionstherapie\n5. Bei Tieren: Tiere-Therapie\n6. Bei Kindern: Spieltherapie\n7. Bei schwer: SSRI",
            "Expositionstherapie (Verhaltenstherapie) für Kinder Goldstandard!\n- Systematische Desensibilisierung\n- In-vivo Exposition\nBei Kindern: Spieltherapie + Exposition\nBei Blut-/Verletzungsphobie: Applied Tension (Spannungstechnik)\nBei schwerer spezifischer Phobie: Sertralin oder Fluoxetin\nHypnotherapie für Kinder möglich",
            "BEI ÜBERTRIEBENER ANGST VOR BESTIMMTEN DINGEN/SITUATIONEN: Sofort zum Kinderarzt!\nSpezifische Phobien bei Kindern: Häufig (5-10%)!\nBei Kindern: KVT + Expositionstherapie sehr wirksam!\nBlut-/Verletzungsphobie: Applied Tension für Kinder!\nBei Tierphobien: Tiergestützte Therapie für Kinder\nBei Kindern: Spontanremission häufig!\nWenn anhaltend oder schwer: Therapie für Kinder\nVermeidungsverhalten durchbrechen!"));

        conditions.add(new MedicalCondition("180", "Panic Disorder", "Panikstörung",
            "panic disorder, panic attacks, agoraphobia, herzrasen, atemnot, anxiety, panikstörung",
            "1. Sofort zum Kinderpsychiater!\n2. Panic Disorder Scale\n3. Ausschluss von somatischen Ursachen (Herz, Schilddrüse)\n4. Kognitive Verhaltenstherapie (KVT)\n5. Bei schwer: SSRI (Paroxetin, Escitalopram)\n6. Bei Akut: Alprazolam oder Clonazepam (kurzfristig)\n7. Exposition mit Vermeidungsverhalten",
            "KVT für Kinder: Erstlinientherapie!\n- Panik-Management-Techniken\n- Kognitive Umstrukturierung\n- Expositionstherapie\nBei schwerer Panikstörung: SSRI\n- Paroxetin (Tagonis) 20-40mg tgl oder\n- Escitalopram (Cipralex) 10-20mg tgl\nBei Akut: Alprazolam 0.5mg bei Bedarf (kurzfristig)",
            "BEI WIEDERKEHRENDEN PANIKATTACKEN: Sofort zum Kinderarzt!\nPanikstörung bei Kindern: Selten aber möglich!\nDiagnose bei Kindern: Schwierig (körperliche Symptome berichten anders)\nBei Kindern: KVT für Kinder first-line!\nSSRI für Kinder möglich!\nPanikattacken können mit Agoraphobie kompliziert werden\nBei Kindern: Familien einbeziehen\nKörperliche Ursachen ausschließen (Herz, Schilddrüse)!\nAtemübungen für Kinder beibringen!"));

        conditions.add(new MedicalCondition("181", "Generalized Anxiety Disorder", "Generalisierte Angststörung",
            "gad, generalized anxiety, worry, restlessness, generalisierte angststörung, dauerangst",
            "1. Sofort zum Kinderpsychiater!\n2. Angstfragebögen (GAD-7, SCARED)\n3. Kognitive Verhaltenstherapie (KVT)\n4. Bei schwer: SSRI (Escitalopram, Sertralin)\n5. Entspannungsverfahren\n6. Sorgen nicht kontrolieren wollen\n7. Schule und Familie einbeziehen",
            "KVT für Kinder: Erstlinientherapie!\n- Sorgenidentifikation und -management\n- Kognitive Umstrukturierung\n- Entspannungsverfahren (PMR)\nBei schwerer GAD: SSRI\n- Escitalopram (Cipralex) 10-20mg tgl oder\n- Sertralin (Zoloft) 50-150mg tgl\nBei Schlafstörungen: Oryzinol oder Baldrian",
            "BEI DAUERHAFTER ANGST + VIELE SORGEN + KÖRPERLICHE BESCHWERDEN: Sofort zum Kinderarzt!\nGeneralisierte Angststörung bei Kindern: 2-5%!\nKinder mit GAD: Sorgen um Schule, Familie, Gesundheit!\nBei Kindern: KVT für Kinder sehr wirksam!\nSSRI für Kinder möglich (Escitalopram, Sertralin)!\nKörperliche Symptome: Bauchschmerzen, Kopfschmerzen, Schlafstörungen\nElterntraining: Nicht vor Sorgen warnen, aber Realismus fördern!\nBei Kindern: Regelmäßige Bewegung und Sport hilft Angst zu reduzieren\nZeit für entspannende Aktivitäten schaffen!"));

        conditions.add(new MedicalCondition("182", "Obsessive Compulsive Disorder", "Zwangsstörung",
            "ocd, obsessive compulsive, washing hands, checking, counting, zwangsstörung, zwanghaftes verhalten",
            "1. Sofort zum Kinderpsychiater!\n2. Y-BOCS Kinder (Kinder Yale-Brown Obsessive Compulsive Scale)\n3. Kognitive Verhaltenstherapie (KVT) mit Exposition und Reaktionsverhinderung\n4. Bei schwer: SSRI + KVT\n5. Bei Tic-Komorbidität: Clonidin oder Risperidon\n6. Family Based Treatment (FBT)\n7. Bei Kindern: PANDAS abklären",
            "KVT mit Exposition und Reaktionsverhinderung (ERP) für Kinder Goldstandard!\nBei unzureichender KVT: SSRI\n- Fluoxetin 20-60mg tgl oder\n- Sertralin 50-200mg tgl oder\n- Escitalopram 10-30mg tgl\nBei Treatment-resistent: Aripiprazol oder Risperidon\nBei PANDAS: Antibiotika (Amoxicillin) + KVT",
            "BEI ZWANGHHANDLUNGEN (WÄSCHEN, ZÄHLEN) + ZWANGGEDANKEN: Sofort zum Kinderarzt!\nZwangsstörung bei Kindern: 1-3%!\nKinder beschreiben Zwänge oft nicht spontan - nachfragen!\nBei Kindern: KVT mit ERP für Kinder sehr wirksam!\nSSRI für Kinder möglich - aber höhere Dosen als bei Depression!\nPANDAS: Nach Streptokokkeninfektion (ASL-Titer)!\nBei Kindern: Family Based Treatment (FBT) wichtig!\nZwänge können小时候 bis ins Erwachsenenalter persistieren\nMitbehandlung von komorbiden Störungen (ADHS, Tic)!\nBei Schulproblemen: Nachteilsausgleich!"));

        conditions.add(new MedicalCondition("183", "Post Traumatic Stress Disorder", "PTBS",
            "ptsd, post traumatic stress, trauma, nightmares, flashbacks, ptss,创伤后应激障碍",
            "1. SOFORT zum Kinderpsychiater!\n2. Trauma-Exposition und Symptomklärung\n3. Kognitive Verarbeitungstherapie (CPT) oder EMDR\n4. Bei Kindern: Trauma-fokussierte CBT (TF-CBT)\n5. Bei schwer: Sertralin oder Paroxetin\n6. Play Therapy für kleine Kinder\n7. Bei akuter Belastungsstörung: Prazosin für Alpträume",
            "Trauma-fokussierte CBT (TF-CBT) für Kinder Goldstandard!\n- Psychoedukation\n- Relaxation\n- Kognitive Verarbeitung\n- Graduierte Exposition\nBei PTBS mit Alpträumen: Prazosin 2-10mg zur Nacht (Kinder ab 6 Jahren möglich)\nBei schwerer PTBS: Sertralin 50-200mg tgl oder Paroxetin 20-40mg tgl\nBei akuter Belastungsstörung: Selbstlimitierend, aber Betreuung wichtig",
            "BEI TRAUMA + ALPTRAUMEN + FLASHBACKS: Sofort zum Kinderarzt!\nPTBS bei Kindern: Nach Missbrauch, Unfällen, Gewalt, Flucht!\nKinder zeigen PTBS anders als Erwachsene!\n- Alpträume, Rückzug, Spielstörungen, Ängstlichkeit\nBei Kindern: Trauma-fokussierte CBT (TF-CBT) sehr wirksam!\nEMDR für Kinder möglich!\nPrazosin für PTBS-Alpträume bei Kindern!\nBei Flüchtlingskindern: Dolmetscher + kultursensible Therapie!\nLangzeitfolgen: Depression, Angststörungen, Substanzmissbrauch!\nFrühe Intervention wichtig!" ));

        conditions.add(new MedicalCondition("184", "Anorexia Nervosa", "Magersucht",
            "anorexia, eating disorder, restrictive eating, low weight, amenorrhea, magersucht, essstörung",
            "1. SOFORT zum Kinderpsychiater/Gastroenterologen!\n2. BMI und Wachstum dokumentieren\n3. EKG, Blutbild, Elektrolyte\n4. Stationäre Aufnahme bei BMI <15 oder Herzrhythmusstörungen\n5. Refeeding-Protokoll\n6. Psychotherapy (FPT für Kinder)\n7. Bei Kindern: Familienbasierte Therapie",
            "Stationär wenn BMI <15 oder Elektrolytstörungen oder Bradykardie <50/min\nRefeeding: 20-30 kcal/kg起始, langsam steigern\nBei Kindern: Familienbasierte Therapie (Maudsley Modell) Goldstandard!\n- Eltern übernehmen Ernährung\n- Kind wird nicht zur Verantwortung gezogen\nBei Therapieresistenz: Fluoxetin oder Olanzapin\nZiel: Normalgewicht erreichen",
            "BEI UNTERGEWICHT + VERMEIDUNG VON ESSEN + VERSTELLTER NAHRUNGSAUFNAHME: Sofort zum Kinderarzt!\nMagersucht bei Kindern: Zunehmend, besonders Mädchen 12-18 Jahre!\nDiagnose: BMI <17.5 (Erwachsene), bei Kindern <3. Perzentile!\nKörperliche Komplikationen: Bradykardie, Hypotonie, Elektrolytentgleisung, Osteoporose\nBei Kindern: Familienbasierte Therapie (Maudsley) sehr wirksam!\nStationäre Aufnahme oft lebensrettend!\nRefeeding-Syndrom bei zu schneller Nahrungszufuhr Gefahr!\nLangzeitprognose: 50% vollständige Remission, 25% chronisch\nBei Knochenentwicklung: Osteoporose auch bei jungen Frauen möglich!"));

        conditions.add(new MedicalCondition("185", "Bulimia Nervosa", "Ess-Brechsucht",
            "bulimia, binge eating, vomiting, laxative abuse, purging, bulimie, essbrechsucht",
            "1. Sofort zum Kinderpsychiater!\n2. Essverhalten dokumentieren\n3. Blutbild, Elektrolyte (Hypokaliämie!)\n4. EKG\n5. Psychotherapy (CBT für Kinder)\n6. Bei schwer: Fluoxetin 60mg tgl\n7. Bei Hypokaliämie: Kaliumersatz",
            "Kognitive Verhaltenstherapie (CBT) für Kinder Goldstandard!\nBei Kindern: CBT-E (Enhanced CBT) für Jugendliche\nFluoxetin 60mg tgl: Einziges für Bulimie zugelassenes Antidepressivum\nBei Hypokaliämie: Kaliumchlorid 20-40mval tgl\nBei Laxanzienabhängigkeit: Ausschleichen + Elektrolytersatz\nZahnarztliche Kontrolle (Säureerosion!)",
            "BEI ESSBRECHANFÄLLEN + LAKTIEREN + ÜBERGEWICHT: Sofort zum Kinderarzt!\nBulimie bei Kindern: Zunehmend, oft normale oder leichtes Übergewicht!\nKörperliche Komplikationen: Hypokaliämie, metabolische Alkalose, Zahnschäden (Säureerosion), Speicheldrüsenvergrößerung\nBei Kindern: CBT für Kinder wirksam!\nFluoxetin 60mg für Jugendliche zugelassen!\nSelten: Laxanzien- oder Diuretikaabusus\nMagersucht und Bulimie können ineinander übergehen\nBei Familienkonflikten: Familienbasierte Therapie\nLangzeitrisiken: Osteoporose, Herzerkrankungen!"));

        conditions.add(new MedicalCondition("186", "Binge Eating Disorder", "Binge-Eating-Störung",
            "binge eating, overeating, loss of control eating, adipositas, binge eating disorder, esssucht",
            "1. Sofort zum Kinderpsychiater!\n2. Essverhalten dokumentieren\n3. BMI und Gewichtsverlauf\n4. Psychotherapy (CBT für Kinder, Interpersonal Therapy)\n5. Bei Adipositas: Gewichtsreduktion\n6. Bei Depression: SSRI\n7. Selbsthilfegruppen",
            "Psychotherapie für Kinder: CBT oder Interpersonale Therapie (IPT)\nBei Adipositas: Interdisziplinäres Programm (Ernährung, Bewegung, Verhaltenstherapie)\nBei Kindern: Familienbasierte Gewichtsreduktion!\nLisdexamfetamin (Vyvanse) in USA für BED ab 18 Jahren zugelassen\nTopiramat oder Zonisamid können helfen (off-label)\nOrlistat bei Adipositas (ab 12 Jahren)",
            "BEI WIEDERKEHRENDEN ESSBRECHANFÄLLEN OHNE KOMPENSATORISCHEM VERHALTEN: Sofort zum Kinderarzt!\nBinge-Eating-Störung bei Kindern: Häufig!\nKinder fallen durch Übergewicht oder Gewichtsschwankungen auf\nBei Adipositas: Interdisziplinäres Programm!\nBei Kindern: Familienbasierte Intervention am wirksamsten!\nPsychotherapie für Kinder: CBT oder IPT\nBei Kindern: Keine Medikamente first-line!\nLangzeitfolgen: Adipositas, Diabetes, Herz-Kreislauf-Erkrankungen\nSelbsthilfegruppen für Kinder und Familien\nPrävention: Familiäre Mahlzeitenzeiten!"));

        conditions.add(new MedicalCondition("187", "Enuresis", "Einnässen",
            "enuresis, bedwetting, nocturnal enuresis,尿床, bettnässen, einnässen nachts",
            "1. Sofort zum Kinderarzt!\n2. Blasentraining und Pipi-Tagebuch\n3. Ausschluss von Harnwegsinfekt, Diabetes, Fehlbildungen\n4. Bei Kindern >5 Jahre: Alarmtherapie (Weckgerät)\n5. Bei Versagen: Desmopressin (Minirin)\n6. Flüssigkeitskarenz ab Nachmittag\n7. Motivationstherapie",
            "Alarmtherapie für Kinder: Enuresis-Alarm (z.B. Wet-Alarm)!\n- Erfolgsrate 60-70%\n- 2-3 Monate konsequent anwenden\nDesmopressin (Minurin) Melt 120-240µg zur Nacht\nBei Kindern: Weckgerät first-line!\nFlüssigkeitskarenz nach 16:00 Uhr\nVermeiden: Koffein, Kohlensäure, Zitrusgetränke",
            "BEI NÄCHTLICHEM EINNÄSSEN AB 5. LEBENGSJAHR: Sofort zum Kinderarzt!\nEinnässen bei Kindern: Normal bis 5 Jahre!\nDiagnose: Erst ab 5 Jahre bei 2x/Woche über mindestens 3 Monate\nPrimär (nie trocken) vs. Sekundär (wieder Einnässen nach Trockenphase)!\nBei Kindern: Alarmtherapie für nErin für Kinder sehr wirksam!\nDesmopressin für Kinder wenn Alarm nicht möglich oder zu langsam\nTiefschlaf + kleine Blase + ADH-Mangel = häufige Ursachen!\nHarnwegsinfekt und Diabetes ausschließen!\nStufenweise Therapie: Blasentraining → Alarm → Desmopressin!\nPositiv verstärken (Belohnung für trockene Nächte)!"));

        conditions.add(new MedicalCondition("188", "Encopresis", "Einkoten",
            "encopresis, fecal incontinence, soiling, constipation, einkoten, stuhlinkontinenz",
            "1. Sofort zum Kinderarzt/Gastroenterologen!\n2. Abdominale Röntgen (Stuhlbrei)\n3. Ausschluss von Morbus Hirschsprung\n4. Enddarm Entleerung (Disimpaction)\n5. Erhaltungstherapie (Macrogol)\n6. Toilettentraining\n7. Bei psychogener Ursache: Psychotherapie",
            "Disimpaction (Darmentleerung): Macrogol 1-1.5g/kg tgl über 3-6 Tage\nOder: Einlauf (Klean-Prep) über 2-3 Tage\nErhaltungstherapie: Macrogol 0.3-0.5g/kg tgl\nToilettentraining: 5-10min nach jeder Mahlzeit auf Toilette sitzen\nVerhaltenstherapie für Kinder bei psychogener Ursache\nBei chronischer Obstipation: Biofeedback-Training",
            "BEI WIEDERKEHRENDEM STUHLVERLUST AB 4. LEBENSJAHR: Sofort zum Kinderarzt!\nEinkoten bei Kindern: Häufig (1-3%)!\nFast immer durch chronische Obstipation mit Überlaufenkopresis!\nBei Kindern: Röntgen zur Diagnostik (Stuhlbrei im Colon)!\nMacrogol für Kinder sehr wirksam!\nToilettentraining für Kinder essentiell!\nPsychologische Ursachen abklären (Schulangst, Familienstreit, Missbrauch)!\nPositiv reinforcement (Belohnung) für Toilettenerfolg!\nBei Kindern: Keine Strafen für Einkoten!\nLange Therapie nötig (Monate bis Jahre)!\nZusammenarbeit mit Psychologen bei Bedarf!"));

        conditions.add(new MedicalCondition("189", "Pica", "Pica-Syndrom",
            "pica, eating non-food items, dirt, hair, ice, chalk, pica, essen von ungenießbaren dingen",
            "1. Sofort zum Kinderarzt!\n2. Blutbild (Anämie? Eisenmangel? Zinkmangel?)\n3. Ausschluss von Psychosen\n4. Eisen- und Zinksubstitution\n5. Verhaltenstherapie (ABC-Analyse)\n6. Überwachung der Umgebung\n7. Bei Bleivergiftung: Ausleitung",
            "Bei Eisen- oder Zinkmangel: Substitution!\n- Eisen (Ferro Sanol) nach Gewicht\n- Zink (Unizink) 15-30mg tgl\nVerhaltenstherapie: Identifikation von Auslösern und Ersatzverhalten\nBei Bleichs Wasser: Bleivergiftung ausschließen\nBei anhaltendem Pica: Pica ist oft Zeichen von Autismus oder geistiger Behinderung\nSensorisches Integrations training für Kinder",
            "BEI ESSEN VON ERDE, KREIDE, HAAREN: Sofort zum Kinderarzt!\nPica bei Kindern: Häufig bei Autismus, geistiger Behinderung, Eisenmangel!\nKinder unter 2 Jahre: Normales Explorationsverhalten!\nGefahr: Bleivergiftung (Abtropfen von bleihaltiger Farbe), Parasiten, Darmverschluss\nBei Kindern: Eisen- und Zinkmangel ausschließen und substituieren!\nVerhaltenstherapie für Kinder!\nÜberwachung der Umgebung (Sauberkeit)!\nBei Autismus: Sensorisches Training\nErnährungsberatung\nBei anhaltendem Pica: Abklärung auf Autismus oder geistige Behinderung"));

        conditions.add(new MedicalCondition("190", "Rumination Syndrome", "Ruminationsstörung",
            "rumination, regurgitation, effortless vomiting, regurgitation disorder, ruminationsstörung",
            "1. Sofort zum Kinderarzt/Gastroenterologen!\n2. pH-Metrie und Manometrie\n3. Ausschluss von GERD\n4. Diaphragmatische Atmung als Counter\n5. Achtsamkeitstraining\n6. Magersucht ausschließen\n7. Psychotherapie",
            "Verhaltenstherapie für Kinder: Habit Reversal Training!\n- Zwerchfellatmung (bewusst gegenregulieren)\n- Achtsamkeitsbasierte Techniken\nBei Kindern: Biofeedback kann helfen\nMagensäureblocker können begleitend helfen (PPI)\nBei zugrundeliegender Angst oder Depression: Behandlung\nSchlucktraining",
            "BEI MÜHELOSEM WIEDERHOLEMTEM ERBRECHEN VON NAHRUNG: Sofort zum Kinderarzt!\nRuminationsstörung bei Kindern: Selten, aber oft nicht erkannt!\nKinder würgen Nahrung hoch und schlucken oder spucken sie wieder aus - ohne Übelkeit!\nVerhaltenstherapie für Kinder: Zwerchfellatmung als Counter\nBei Kindern: Wird oft mit GERD verwechselt!\nDiagnose: pH-Metrie/Manometrie zum Ausschluss von GERD\nBei Autismus: Häufiger!\nPsychotherapie für Kinder oft notwendig\nBei Schulkindern: Soziale Situationen können Auslöser sein\nLangfristige Psychotherapie nötig!"));

        conditions.add(new MedicalCondition("191", "Conversion Disorder", "Funktionelle neurologische Störung",
            "conversion disorder, functional neurological disorder, weakness, paralysis, seizures, dissociation, dissoziative störung",
            "1. Sofort zum Neurologen!\n2. Ausschluss von neurologischen Erkrankungen\n3. Neurologische Untersuchung (positive Zeichen)\n4. Psychoedukation\n5. Physiotherapie bei motorischen Symptomen\n6. Psychotherapie\n7. Bei dissociation: Grounding-Techniken",
            "Physiotherapie für Kinder: Wichtigste Intervention bei Lähmungen!\n- Wiederholende aktive Bewegung\n- Constraint-induced movement therapy\nPsychotherapie: Konfrontative Exposition, DBT für Kinder\nBei dissociation: Grounding-Techniken (5-4-3-2-1 Übung)\nBei Kindern: Familienintervention wichtig\nBei Schulabstinenz: Schulreintegration",
            "BEI PLÖTZLICHEN NEUROLOGISCHEN SYMPTOMEN (LÄHMUNG, KRÄMPFE) OHNE ORGANISCHE URSACHE: Sofort zum Kinderarzt!\nFunktionelle neurologische Störung bei Kindern: Zunehmend!\nKinder zeigen: Lähmungen, Zittern, Krämpfe, Gangstörungen, Taubheit, Blindheit!\nDiagnose: Ausschluss von neurologischen Erkrankungen + positive klinische Zeichen!\nBei Kindern: Psychoedukation - Krankheit ist REAL, nicht eingebildet!\nPhysiotherapie für Kinder sehr wichtig!\nPsychotherapie für Kinder: Emotionen in körperliche Symptome\nAuslöser: Trauma, Stress, Schulprobleme, familiäre Konflikte\nGute Prognose bei Kindern wenn frühe Intervention!\nEltern sollten nicht verstärken, aber auch nicht dramatisieren!"));

        conditions.add(new MedicalCondition("192", "Dissociative Identity Disorder", "Dissoziative Identitätsstörung",
            "did, multiple personality, alter personalities, dissociation, dissoziative identitätsstörung",
            "1. SOFORT zum Kinderpsychiater!\n2. Strukturiertes klinisches Interview (SCID-D)\n3. Traumafokussierte Psychotherapie\n4. Stabilisierungstechniken\n5. Bei Kindern: Kindgerechte Therapie (Play Therapy, EMDR)\n6. Arbeit mit Identitäten\n7. Familienarbeit",
            "Traumafokussierte Psychotherapie für Kinder: TF-CBT!\nEMDR für Kinder mit Trauma!\nStabilisierungsarbeit: Grounding, Sicherheitserleben\nBei Identitäten: Therapeutische Beziehung zu allen Identitäten\nBei schwerer Dissoziation: Langzeit-Psychotherapie\nKrisenplan erstellen\nSchool-based support",
            "BEI WECHSELNDEN IDENTITÄTEN + ERINNERUNGSLÜCKEN + TRAUMA: Sofort zum Kinderpsychiater!\nDissoziative Identitätsstörung bei Kindern: Selten, aber oft unterdiagnostiziert!\nFast immer mit schwerem Trauma in der Kindheit verbunden (Missbrauch, Misshandlung, Vernachlässigung)!\nBei Kindern: Trauma-Therapie first-line!\nIdentitäten können unterschiedliche Altersstufen, Namen, Verhaltensweisen haben\nEltern müssen einbezogen werden (Schutzfaktoren stärken)!\nBei Schulproblemen: Edukation des Umfelds\nLangzeittherapie nötig!\nGefahr: Selbstverletzung, Suizidalität!"));

        conditions.add(new MedicalCondition("193", "Body Dysmorphic Disorder", "Körpers dysmorphische Störung",
            "bdd, body dysmorphic disorder, appearance concerns, imagined ugliness, perceived defect, körpers dysmorphische störung",
            "1. Sofort zum Kinderpsychiater!\n2. Yale-Brown Obsessive Compulsive Scale for BDD (BDD-YBOCS)\n3. Ausschluss von realen körperlichen Problemen\n4. CBT für Kinder\n5. Bei schwer: SSRI (Fluoxetin, Sertralin)\n6. Bei Kindern: Familienarbeit\n7. Stopping reassurance seeking",
            "Kognitive Verhaltenstherapie (CBT) für Kinder:\n- Exposition mit Vermeidungsverhalten\n- Kognitive Umstrukturierung\n- Response Prevention (zwanghaftes Überprüfen)\nBei schwerer BDD: SSRI\n- Fluoxetin 40-80mg tgl oder\n- Sertralin 100-200mg tgl\nBei Jugendlichen: Meist Kombination CBT + SSRI\nStoppen des: Rückversicherungsverhalten (immer in den Spiegel schauen, um Bestätigung bitten)",
            "BEI ÜBERTRIEBENER BESORGE UM KÖRPERLICHE ERSCHEINUNG + VIEL SPIEGELN + VERGLEICHEN: Sofort zum Kinderarzt!\nKörpers dysmorphische Störung bei Kindern: Zunehmend!\nKinder/F Jugedliche besessen von wahrgenommenen Makeln (Haut, Haare, Nase, Gewicht)!\nBei Kindern: CBT für Kinder wirksam!\nSSRI für Kinder möglich!\nOft komorbid mit Essstörungen, sozialer Angst, Depression\nBei teenage Mädchen besonders häufig!\nHandy-/Social Media-Nutzung verschlechtert oft Symptome!\nSelten allein durch plastische Chirurgie zu lösen (kann verschlimmern)!\nPsychotherapie für Kinder essentiell!"));

        conditions.add(new MedicalCondition("194", "Hoarding Disorder", "Hamsterstörung",
            "hoarding, collecting, clutter, excessive acquisition, hamstersucht, horten von gegenständen",
            "1. Sofort zum Kinderpsychiater!\n2. hoarding Scale\n3. CBT für Kinder\n4. Graduierte Entrümpelung\n5. Bei Tieren: Tier-/Vermehrungskontrolle\n6. Familienarbeit\n7. Bei ADHS-Komorbidität: Methylphenidat",
            "Kognitive Verhaltenstherapie (CBT) für Kinder:\n- Kognitive Umstrukturierung\n- Graduierte Exposition (Entrümpelung)\n- Motivationsarbeit\nBei ADHS: Methylphenidat wenn komorbid\nBei Tiersammlungen: Gesundheitsamt einschalten (Tierschutz!)\nFamily-based Intervention für Kinder\nBei Kindern: Geringe Evidenz, da DSM-5 Diagnose erst seit 2013\nGemeindesozialarbeit",
            "BEI EXZESSIVEM SAMMELN + NICHT LOSLASSEN KÖNNEN + ÜBERFÜLLTE RÄUME: Sofort zum Kinderarzt!\nHamsterstörung bei Kindern: Teilweise mit Autismus, ADHS, OC Spectrum assoziiert!\nKinder horten: Schulhefte, Kuscheltiere, Müll!\nUnterschied zu normalem kindlichem Sammeln: Leidensdruck, Funktionsbeeinträchtigung!\nBei Kindern: Familienarbeit + CBT!\nHygieneprobleme, soziale Isolation, Schulprobleme!\nBei Tiersammlungen: Gesundheitsamt für Kindesschutz einschalten!\nLangfristige Therapie nötig\nPrävention: Nicht zu viel schenken, regelmäßig ausmisten!"));

        conditions.add(new MedicalCondition("195", "Trichotillomania", "Haareausreißen",
            "trichotillomania, hair pulling, hair loss, hair pulling disorder, trichotillomanie, haareausreißen",
            "1. Sofort zum Kinderpsychiater!\n2. Trichotillomania Scalp Examination\n3. Habit Reversal Training (HRT) für Kinder\n4. CBT für Kinder\n5. Bei Kindern: N-Acetylcystein (NAC)\n6. Bei schwer: Clomipramin oder SSRIs\n7. Familienarbeit",
            "Habit Reversal Training (HRT) für Kinder: Goldstandard!\n- Awareness Training\n-Competing Response (Faust in Tasche)\n-Social Support\nBei Kindern: N-Acetylcystein (NAC) 1200-2400mg tgl (off-label)!\nBei schwerer oder komorbider Depression: Clomipramin 50-150mg tgl oder Fluoxetin 40-80mg tgl\nBiofeedback für Kinder möglich\nNagelpflege (代替行为)",
            "BEI SICHTERLICHEM HAARAUSREISSEN + KAHLEN STELLEN: Sofort zum Kinderarzt!\nTrichotillomanie bei Kindern: 1-3%!\nKinder (besonders Mädchen) ziehen Haare aus bei Stress/Langeweile!\nBei Kindern: Habit Reversal Training für Kinder sehr wirksam!\nNAC für Kinder off-label möglich (1200-2400mg)!\nOft komorbid mit Angst, ADHS, OC-spectrum\nNagelpflege (代替行为) hilfreich!\nBei Kindern: Keine Strafen für Haareausreißen!\nLangfristige Therapie nötig\nAls Ersatz: Fidget-Spinner, Kneten!"));

        conditions.add(new MedicalCondition("196", "Excoriation Disorder", "Hautdrücken",
            "excoriation disorder, skin picking, dermatillomania, acne picking, picking disorder, Hautdrücken",
            "1. Sofort zum Kinderpsychiater!\n2. Hautuntersuchung\n3. Habit Reversal Training (HRT)\n4. CBT für Kinder\n5. Bei Kindern: NAC (N-Acetylcystein)\n6. Hautpflege (reduziert Pickel, damit weniger Drängel)\n7. Bei Infektion: Antibiotika lokal",
            "Habit Reversal Training (HRT) für Kinder: Goldstandard!\n- Awareness Training\n-Competing Response\nBei Hautproblemen: Gute Hautpflege (reduziert Drang zu picken)\nBei Kindern: N-Acetylcystein (NAC) 1200-2400mg tgl (off-label)!\nBei Anxiety-Komorbidität: SSRI\nBei Infektion: Antiseptische Wundversorgung\nNagelbeißen: Bitternail-Lack",
            "BEI WIEDERKEHRENDEM HAUTDRÜCKEN + OFFENE STELLEN + NARBEN: Sofort zum Kinderarzt!\nHautdrücken bei Kindern: Zunehmend, besonders Mädchen/teenager!\nKinder picken an Pickeln, Krusten, Unebenheiten bis blutig!\nBei Kindern: Habit Reversal Training für Kinder!\nNAC für Kinder off-label!\nHautpflege: Reduziert Anlass zu picken\nInfektionsgefahr: Antiseptische Wundversorgung!\nOft komorbid mit Angst, OC-Spektrum, Depression\nBei teenage Mädchen besonders häufig!\nAls Ersatz: Fidget-Spinner, Kneten, Handbeschäftigung!\nLangfristige Therapie nötig!"));

        conditions.add(new MedicalCondition("197", "Transient Tic Disorder", "Vorübergehende Tic-Störung",
            "transient tic disorder, temporary tics, simple tics, vorübergehende tics, vorübergehende tic-störung",
            "1. Zum Kinderarzt!\n2. Yale Global Tic Severity Scale (YGTSS)\n3. Beobachtung und Monitoring\n4. Bei milden Tics: Aufklärung\n5. Stressreduktion\n6. Bei Persistenz >1 Jahr: Chronische Tic-Störung\n7. Bei schweren Tics: Medikation",
            "Transient Tic Disorder: Häufig bei Kindern, selbstlimitierend!\nBei leichten Tics: Watchful Waiting + Aufklärung (Tics sind nicht bewusst!)\nStressreduktion: Ausreichend Schlaf, wenig Bildschirmzeit, Entspannung\nBei schweren Tics oder Persistenz: Siehe Tourette-Syndrom\nAlpha-2-Agonisten: Clonidin 0.025-0.1mg tgl oder Guanfacin\nBei Persistenz >1 Jahr: Diagnose Tourette-Syndrom",
            "BEI VORÜBERGEHENDEN TICS (BLINKEN, RÄUSPERN): Zum Kinderarzt!\nVorübergehende Tic-Störung bei Kindern: Sehr häufig (bis 20%)!\nEinfache motorische Tics: Blinzeln, Gesichtszucken, Kopfbewegungen\nEinfache vokale Tics: Räuspern, Hüsteln, Grunzen\nBei Kindern: Meist selbstlimitierend (Wochen bis Monate)!\nTics sind UNWILLKÜRLICH (nicht bewusst)!\nStigmatisierung und Bloßstellung vermeiden!\nBei Persistenz >1 Jahr: Chronische Tic-Störung/Tourette!\nSport und Ablenkung können helfen\nEltern sollten NICHT darauf ansprechen (kann verschlimmern)!\nZeit und Stressabbau helfen oft!"));

        conditions.add(new MedicalCondition("198", "Chronic Motor or Vocal Tic Disorder", "Chronische motorische oder vokale Tic-Störung",
            "chronic motor tic disorder, chronic vocal tic disorder, persistent tics, chronische tic-störung",
            "1. Sofort zum Neurologen/Psychiater!\n2. YGTSS (Yale Global Tic Severity Scale)\n3. Abgrenzung zu Tourette\n4. Bei schweren Tics: Medikation\n5. Habit Reversal Training\n6. CBIT Therapie\n7. Psychoedukation",
            "Wenn motorische ODER vokale Tics (aber nicht beide) >1 Jahr: Chronische Tic-Störung!\nAlpha-2-Agonisten für Kinder: Clonidin 0.025-0.2mg tgl oder Guanfacin 1-3mg tgl\nBei schweren Tics: Antipsychotika\n- Risperidon 0.5-3mg tgl oder\n- Aripiprazol 2-15mg tgl\nCBIT (Comprehensive Behavioral Intervention for Tics) für Kinder: Wirksam!\nPsychoedukation für Kind und Familie",
            "BEI PERSISTIERENDEN TICs ÜBER 1 JAHR: Sofort zum Kinderarzt!\nChronische Tic-Störung bei Kindern: 1-2%!\nUnterschied zu Tourette: Nur motorische ODER nur vokale Tics (nicht beide)!\nBei Kindern: Genauso behandlungsbedürftig wie Tourette!\nAlpha-2-Agonisten für Kinder first-line!\nBei Kindern: CBIT für Kinder bewährt!\nAntipsychotika für Kinder bei schweren Tics (Aripiprazol oft besser verträglich als Haloperidol)!\nPrognose: 50% entwickeln zusätzliche Tic-Formen = Tourette!\nBei Schulproblemen: Nachteilsausgleich!\nBei Komorbidität (ADHS, OC): Behandlung beider wichtig!"));

        conditions.add(new MedicalCondition("199", "Stereotypic Movement Disorder", "Stereotype Bewegungsstörung",
            "stereotypic movement disorder, rocking, hand flapping, head banging, stereotypien, selbststimulierendes verhalten",
            "1. Sofort zum Kinderarzt!\n2. Beobachtung und Beschreibung der Bewegungen\n3. Sicherheit gewährleisten (bei selbstverletzendem Verhalten)\n4. Bei Autismus: Identifikation von Auslösern\n5. Alternative Verhaltensweisen anbieten\n6. Bei Selbstverletzung: Schutzmaßnahmen\n7. Bei Kindern ohne Autismus: Abklärung ADHS, Geistige Behinderung",
            "Bei selbstverletzendem Verhalten: Schutzmaßnahmen (Helm bei Kopfschlagen)\nBei Autismus: Identifikation von Triggern (sensorisch, emotional)\nReplacement Behavior (代替verhalten): Fidget-Spinner, Kneten, Gewichtstuch\nBei Kindern: Keine Unterbrechung der Bewegungen (verstärkt sie oft!)\nBei ADHS-Komorbidität: Methylphenidat\nGgf. Risperidon 0.5-2mg tgl bei schwerer Selbstverletzung",
            "BEI WIEDERKEHRENDEN BEWEGUNGEN (SCHAUkeln, HÄNDEFLATTERN, KOPFSCHLAGEN): Sofort zum Kinderarzt!\nStereotype Bewegungsstörung bei Kindern: Häufig bei Autismus und Geistiger Behinderung!\nSchaukeln, Händeflattern, Drehen, Beißen, Kopfschlagen!\nBei Kindern: Oft selbstberuhigend (sensorisch)! \nWichtig: Bei Autismus nicht unterdrücken, ersetzen!\nBei Selbstverletzung: Helm, Polsterung, Alternativen!\nWenn keine geistige Behinderung/Autismus: ADHS abklären!\nKopfschlagen kann bei Kleinkindern normale Entwicklung sein (bis 3 Jahre)!\nBei anhaltendem Kopfschlagen nach 3 Jahren: Abklärung!\nGewichtstuch (Weighted Blanket) kann helfen!"));

        conditions.add(new MedicalCondition("200", "Rett Syndrome", "Rett-Syndrom",
            "rett syndrome, regression, hand wringing, microcephaly, MECP2 mutation, rett-syndrom",
            "1. SOFORT zum Kinderarzt/Neuropädiater!\n2. Gentest (MECP2-Mutation)\n3. Klinische Kriterien\n4. Multidisziplinäre Betreuung\n5. Physiotherapie (Erhaltung Motorik)\n6. Bei Epilepsie: Antiepileptika\n7. Ernährung und Wachstum überwachen",
            "Keine kausale Therapie!\nMultidisziplinäre Betreuung:\n- Physiotherapie (verzögert Motorikverlust)\n- Ergotherapie\n- Logopädie\nBei Epilepsie: Rufinamid (Inofron), Levetiracetam (Keppra)\nBei Spastik: Baclofen oder Botulinumtoxin\nBei Atemstörungen: Beta-Agonisten\nGastrostomie bei Schluckstörungen",
            "BEI MÄDCHEN MIT ENTWICKLUNGSRÜCKGRIFF + HANDKRÄMPFEN: 112 SOFORT!\nRett-Syndrom bei Mädchen: Häufigste Ursache für schwere geistige Behinderung bei Mädchen!\nGenetische Störung (MECP2-Mutation auf X-Chromosom)!\nTypischer Verlauf: Normal bis 6-18 Monate → Regression (Sprache, Motorik) → Handkrämpfe (Waschen, Pressen) → Spätphase\nBei Mädchen: Sofortige genetische Testung!\nEpilepsie bei 80%!\nAtemstörungen (Apnoen, Hyperventilation)!\nLebenserwartung eingeschränkt aber viele werden erwachsen!\nFrühe Diagnose und Therapie wichtig!\nElternberatung und genetische Beratung!"));

        conditions.add(new MedicalCondition("201", "Tension Headache", "Spannungskopfschmerzen",
            "tension headache, stress headache, band-like headache, pressure headache, neck tension, spannungskopfschmerzen",
            "1. Entspannungstechniken\n2. Massage des Nackens\n3. Wärme auf den Nacken\n4. Pausen bei Bildschirmarbeit\n5. Ausreichend Schlaf\n6. Stressmanagement\n7. Bei chronischen Kopfschmerzen zum Arzt",
            "Paracetamol 500mg-1g\nIbuprofen 400mg\nDiclofenac 25mg\nKoffein kann helfen",
            "Paracetamol Saft 10-15mg/kg\nIbuprofen Saft 5-10mg/kg\nWärme auf Nacken\nMassage für Kinder"));

        conditions.add(new MedicalCondition("202", "Migraine", "Migräne",
            "migraine, severe headache, throbbing pain, aura, light sensitivity, migräne",
            "1. Ruhe in dunklem Raum\n2. Kühle Kompresse auf Stirn\n3. Ausreichend Wasser trinken\n4. Schlaf nachholen\n5. Auslöser identifizieren\n6. Prophylaxe mit Magnesium\n7. Bei mehr als 3 Attacken/Monat zum Arzt",
            "Sumatriptan 50-100mg\nRizatriptan 10mg\nParacetamol 1g\nIbuprofen 400mg",
            "Bei Kindern: Paracetamol/Ibuprofen Saft\nDunkler, ruhiger Raum\nKühle Kompresse\nSchlaf oft hilfreich"));

        conditions.add(new MedicalCondition("203", "Sinusitis", "Nasennebenhöhlenentzündung",
            "sinusitis, sinus infection, facial pain, nasal congestion, sinus pressure, nasennebenhöhlen",
            "1. Nasenspülung mit Kochsalzlösung\n2. Inhalieren mit Dampf\n3. Nasenschleim abschwellende Mittel\n4. Schmerzmittel\n5. Ausreichend trinken\n6. Kopf hoch lagern\n7. Bei Fieber oder über 10 Tage zum Arzt",
            "Nasenspray mit Dexpanthenol\nXylometazolin Nasentropfen (max 7 Tage)\nIbuprofen bei Schmerzen\nParacetamol",
            "Nasenspray für Kinder\nInhalieren mit Kochsalzlösung\nNasentropfen für Kinder\nSchmerzmittel nach Gewicht"));

        conditions.add(new MedicalCondition("204", "Tonsillitis", "Mandelentzündung",
            "tonsillitis, sore throat, white spots on tonsils, difficulty swallowing, mandelentzündung",
            "1. Warmes Wasser trinken\n2. Gurgeln mit Salzwasser\n3. Ruhe\n4. Schmerzmittel\n5. Bei Atemnot sofort zum Arzt\n6. Ausschluss Streptokokken\n7. Bei häufigen Infektionen: OP erwägen",
            "Bei bakteriell: Penicillin V 1 Mio IE 3x tgl\nBei Penicillinallergie: Clindamycin\nSchmerzmittel: Ibuprofen",
            "Bei Kindern: Sofort zum Kinderarzt\nKeine Selbstmedikation bei Kindern unter 3 Jahren\nGurgeln mit Salzwasser (ab 6 Jahren)"));

        conditions.add(new MedicalCondition("205", "Bronchitis", "Bronchitis",
            "bronchitis, chest infection, cough with mucus, bronchial infection, bronchitis, husten",
            "1. Ausreichend trinken\n2. Schleimlöser\n3. Inhalieren\n4. Ruhe\n5. Bei Fieber: fiebersenkende Mittel\n6. Vermeiden: Rauchen, Staub\n7. Bei über 3 Wochen zum Arzt",
            "ACC 200mg 3x tgl\nMucosolvan 30mg 3x tgl\nBronchipret Filmtabletten\nIbuprofen bei Schmerzen",
            "ACC Saft für Kinder\nMucosolvan Kinderhustensaft\nInhalieren mit Kochsalzlösung\nViel trinken"));

        conditions.add(new MedicalCondition("206", "Pneumonia", "Lungenentzündung",
            "pneumonia, lung infection, fever, cough, chest pain, breathing difficulty, lungentzdauer",
            "1. SOFORT zum Arzt\n2. Röntgen zur Diagnose\n3. Antibiotika wenn bakteriell\n4. Ausreichend trinken\n5. Ruhe\n6. Bei Atemnot: 112\n7. Krankenhaus bei schwerem Verlauf",
            "Bei bakteriell: Amoxicillin 1g 3x tgl\nBei atypisch: Makrolide (Azithromycin)\nBei schwer: Krankenhaus\nSauerstoffgabe",
            "BEI VERDACHT AUF LUNGENENTZÜNDUNG: Sofort zum Kinderarzt\nBei Säuglingen: Oft Krankenhaus\nAntibiotika nach Gewicht"));

        conditions.add(new MedicalCondition("207", "Asthma", "Asthma",
            "asthma, wheezing, shortness of breath, chest tightness, atemnot, pfeifend",
            "1. Inhaler (Reliever) anwenden\n2. Notfallplan befolgen\n3. Auslöser vermeiden\n4. Controller Inhaler regelmäßig\n5. Peak-Flow messen\n6. Bei Verschlechterung zum Arzt\n7. Bei Atemnot: 112",
            "Reliever: Salbutamol 100mcg\nController: Budesonid 200mcg\nMontelukast 10mg\nBei Anfall: Salbutamol alle 5 Min",
            "Bei Kindern: Inhaler mit Spacer\nSalbutamol Saft für Kleinkinder\nBudesonid Inhaler für Kinder\nNotfallplan für Kinder"));

        conditions.add(new MedicalCondition("208", "Gastritis", "Magenschleimautzündung",
            "gastritis, stomach inflammation, upper abdominal pain, nausea, burning stomach, magenschleimhautentzündung",
            "1. Meidung: Alkohol, Kaffee, NSAR\n2. Kleine, häufige Mahlzeiten\n3. Nicht liegend essen\n4. Stress reduzieren\n5. Magenschutzmittel\n6. Bei Blutungen sofort zum Arzt\n7. HP-Test beim Arzt",
            "Pantoprazol 40mg morgens\nOmeprazol 20mg\nMisoprostol bei NSAR\nSucralfat bei Ulcus",
            "Bei Kindern: Magenspiegelung\nSchonkost\nKeine IBS bei Kindern\nBei blutigem Erbrechen: Sofort zum Arzt"));

        conditions.add(new MedicalCondition("209", "Peptic Ulcer", "Magengeschwür",
            "peptic ulcer, stomach ulcer, duodenal ulcer, burning epigastric pain, magengeschwür",
            "1. Magenspiegelung zur Diagnose\n2. HP-Eradikation wenn positiv\n3. PPI-Therapie 8 Wochen\n4. Meidung: NSAR, Alkohol, Rauchen\n5. Kleine Mahlzeiten\n6. Bei Blutung: SOFORT zum Arzt\n7. Kontrolle nach Therapie",
            "Bei HP-positiv: Esomeprazol 40mg 2x + Amoxicillin 1g 2x + Clarithromycin 500mg 2x\nBei HP-negativ: PPI 8 Wochen\nBei Blutung: Hospitalisierung",
            "Bei Kindern: Selten\nMagenspiegelung unter Narkose\nBei blutigem Stuhl/Erbrechen: Sofort Arzt"));

        conditions.add(new MedicalCondition("210", "GERD", "Sodbrennen",
            "gerd, acid reflux, heartburn, regurgitation, chest burning, sodbrennen, reflux",
            "1. Kopf hoch lagern\n2. Kleinere Mahlzeiten\n3. Meidung: Fett, Scharf, Sauer, Kaffee\n4. 3 Stunden vor dem Schlafen nicht essen\n5. Gewicht reduzieren\n6. Magensäurehemmer\n7. Bei Alarmzeichen zum Arzt",
            "Omeprazol 20mg morgens\nPantoprazol 40mg\nEsomeprazol 40mg\nBei Bedarf: Gaviscon",
            "Bei Säuglingen: 3wöchige Therapie üblich\nNahrungsmittelverdünnung\nBei Gedeihstörung: Kinderarzt"));

        conditions.add(new MedicalCondition("211", "Hepatitis A", "Hepatitis A",
            "hepatitis a, liver inflammation, jaundice, fatigue, nausea, hepatitis a, gelbsucht",
            "1. Sofort zum Infektiologen\n2. Bluttest auf Antikörper\n3. Hygiene beachten\n4. Alkoholvermeidung\n5. Ruhe\n6. Kontrolle der Leberwerte\n7. Impfschutz prüfen",
            "Keine spezifische Therapie\nSupportiv: Ruhe, Flüssigkeit\nBei schwerem Verlauf: Krankenhaus\nLeberwerte überwachen",
            "BEI HEPATITIS A: Meldepflicht\nAnsteckend für 2 Wochen vor und 1 Woche nach Ikterus\nBei Kindern oft asymptomatisch"));

        conditions.add(new MedicalCondition("212", "Hepatitis B", "Hepatitis B",
            "hepatitis b, liver disease, jaundice, fatigue, sexual transmission, hepatitis b, leberentzündung",
            "1. Bluttest auf HBsAg, Anti-HBs, Anti-HBc\n2. Leberwerte prüfen\n3.Bei chronisch: antiviral Therapie\n4. Sexualpartner informieren\n5. Kein Alkohol\n6. Impfung für Kontaktpersonen\n7. Regelmäßige Kontrollen",
            "Bei chronischer Hepatitis B:\n- Entecavir 0.5mg tgl\n- Tenofovir 245mg tgl\nBei Zirrhose: Sofort Therapie",
            "BEI HEPATITIS B: Sofort zum Infektiologen\nBei Neugeborenen: Sofortige passive und aktive Immunisierung\nMutter HBsAg-positiv: Simultanimpfung + HBIG"));

        conditions.add(new MedicalCondition("213", "Gallstones", "Gallensteine",
            "gallstones, biliary colic, right upper quadrant pain, fatty food intolerance, gallensteine, gallenkolik",
            "1. Ultraschall zur Diagnose\n2. Fettarme Ernährung\n3. Bei Kolik: Schmerzmittel\n4. Eventuell ESWL (Steinzertrümmerung)\n5. laparoskopische Cholezystektomie\n6. Bei Gelbsucht/Entzündung: Sofort-OP\n7. Rückfallprophylaxe",
            "Bei Kolik: Butylscopolamin 20mg i.v./i.m.\nParacetamol 1g\nBei Entzündung: Ceftriaxon 2g i.v.",
            "Bei Kindern: Selten\nBei hämolytischer Anämie: Erhöhtes Risiko\nBei Gelbsucht + Bauchschmerzen: Sofort zum Kinderarzt"));

        conditions.add(new MedicalCondition("214", "Pancreatitis", "Bauchspeicheldrüsenentzündung",
            "pancreatitis, pancreatic inflammation, severe upper abdominal pain, nausea, vomiting, bauchspeicheldrüsenentzündung",
            "1. SOFORT zum Arzt\n2. Nüchtern lassen\n3. Flüssigkeit parenteral\n4. Schmerztherapie\n5. Bei akuter: Krankenhaus\n6. Ursache klären\n7. Bei rezidivierender: OP erwägen",
            "Im Krankenhaus:\n- NPO (nichts per os)\n- IV-Flüssigkeit\n- Schmerzmittel\nBei infizierter Nekrose: Antibiotika",
            "BEI VERDACHT AUF PANKREATITIS: Sofort zum Kinderarzt/Notaufnahme\nBei Kindern: Oft medikamenteninduziert\nAmylase und Lipase erhöht"));

        conditions.add(new MedicalCondition("215", "Urinary Tract Infection", "Harnwegsinfekt",
            "urinary tract infection, uti, burning urination, frequent urination, cloudy urine, harnswegsinfekt, blasenentzündung",
            "1. Urinprobe zum Arzt\n2. Antibiotika\n3. Viel trinken\n4. Bei Fieber: sofort zum Arzt\n5. Bei Schwangeren: unbedingt behandeln\n6. Blasenentzündung nicht verschleppen\n7. Bei rezidivierenden: Abklärung",
            "Bei unkompliziert: Nitrofurantoin 100mg 2x 5 Tage\nFosfomycin 3g Einmaldosis\nBei Fieber/Sepsis: Ciprofloxacin 500mg 2x",
            "Bei Kindern: Sofort zum Kinderarzt\nUrinuntersuchung\nAntibiotika nach Gewicht\nBei Säuglingen: Blasenpunktion"));

        conditions.add(new MedicalCondition("216", "Kidney Stones", "Nierensteine",
            "kidney stones, renal colic, flank pain, blood in urine, back pain, nierensteine, nierenkolik",
            "1. Schmerzmittel\n2. Viel trinken\n3. Stein abpassen\n4. Ultraschall/CT\n5. Bei Stein >5mm: Lithotripsie\n6. Bei Infektion: Antibiotika\n7. Metaphylaxe (Ernährung)",
            "Bei Kolik: Tamsulosin 0.4mg\nParacetamol 1g\nMetamizol 1g\nBei Infektion: Ciprofloxacin",
            "Bei Kindern: Pädiatrische Urologie\nSelten aber möglich\nAngeborene Fehlbildungen\nUltraschall"));

        conditions.add(new MedicalCondition("217", "Glomerulonephritis", "Nierenentzündung",
            "glomerulonephritis, blood in urine, swollen feet, high blood pressure, kidney inflammation, nierenzündung",
            "1. Sofort zum Nephrologen\n2. Bluttest (Kreatinin, Harnstoff)\n3. Urinprobe\n4. Nierenbiopsie\n5. Immunsuppression\n6. Blutdruckkontrolle\n7. Dialyse bei Bedarf",
            "Bei rapid progressiver GN:\n- Methylprednisolon 500mg i.v. 3 Tage\n- Prednisolon 1mg/kg absteigend",
            "BEI HÄMaturie + ÖDEM + HOCHDRUCK: Sofort zum Kinderarzt\nPoststreptokokken-GN: Häufigste GN bei Kindern\nPlötzliche Ödeme, dunkler Urin"));

        conditions.add(new MedicalCondition("218", "Anemia", "Blutarmut",
            "anemia, low hemoglobin, fatigue, pale skin, dizziness, shortness of breath, blutarmut, niedriges hämoglobin",
            "1. Blutbild prüfen\n2. Ursache klären (Eisenmangel, B12, Folsäure, chronische Krankheit)\n3. Eisensubstitution wenn Eisenmangel\n4. Transfusion bei schwerer Anämie\n5. Ernährung optimieren\n6. Bei chronischer Krankheit: Grundkrankheit behandeln\n7. Regelmäßige Kontrollen",
            "Bei Eisenmangel: Eisen fumarat 100mg 2x tgl\nVitamin B12 1000mcg i.m. wöchentlich\nFolsäure 5mg tgl",
            "Bei Kindern: Gewichtsadaptierte Eisentherapie\nFerronat Sirup 2-6mg/kg/d\nVitamin C verbessert Eisenaufnahme"));

        conditions.add(new MedicalCondition("219", "Leukemia", "Leukämie",
            "leukemia, blood cancer, fatigue, bleeding, infections, pale, weight loss, leukämie, blutkrebs",
            "1. SOFORT zum Hämatologen\n2. Blutbild mit Differenzialblutbild\n3. Knochenmarkbiopsie\n4. Lumbalpunktion\n5. Chemotherapie\n6. Stammzelltransplantation\n7. Supportivtherapie",
            "ALL: Vincristin + Prednison + Asparaginase\nAML: Cytarabin + Daunorubicin\nKMT bei High-Risk",
            "BEI VERDACHT AUF LEUKÄMIE: Sofort in die Kinderklinik\nHäufigste Krebserkrankung bei Kindern\nBlässe, Müdigkeit, Infektionen, Blutergüsse"));

        conditions.add(new MedicalCondition("220", "Lymphoma", "Lymphom",
            "lymphoma, swollen lymph nodes, night sweats, weight loss, fatigue, lymphoma, lymphknotenkrebs",
            "1. Lymphknotenbiopsie\n2. Staging (CT, PET, Knochenmark)\n3. Chemotherapie oder Bestrahlung\n4. Immuntherapie\n5. Stammzelltransplantation\n6. Nachsorge\n7. Bei Rezidiv: Salvage-Therapie",
            "Hodgkin-Lymphom: ABVD\nNon-Hodgkin: R-CHOP\nKMT bei Rezidiv",
            "BEI PERSISTIERENDEN LYMPHKNOTENSCHWELLUNG: Sofort zum Kinderarzt\nBei Kindern: Non-Hodgkin häufiger\nB-Symptome (Nachtschweiß, Gewichtsverlust, Fieber)"));

        conditions.add(new MedicalCondition("221", "Deep Vein Thrombosis", "Beinvenenthrombose",
            "deep vein thrombosis, dvt, swollen leg, leg pain, red leg, blood clot, beinvenenthrombose",
            "1. Sofort zum Arzt\n2. Kompressionsultraschall\n3. Antikoagulation\n4. Kompressionsstrümpfe\n5. Mobilisierung\n6. Thrombolyse bei schwer\n7. Filter bei Kontraindikation",
            "Apixaban 10mg 2x 7 Tage, dann 5mg 2x\nRivaroxaban 15mg 2x 3 Wochen, dann 20mg tgl\nLMWH (Enoxaparin 1mg/kg 2x)",
            "BEI GESCHWOLLENEM BEIN + SCHMERZ: Sofort zum Arzt\nBei Kindern: Selten\nBei Kathetern, Immobilisation, Thrombophilie"));

        conditions.add(new MedicalCondition("222", "Cellulitis", "Cellulitis",
            "cellulitis, skin infection, red swollen skin, warmth, fever, zellulitis, hautinfektion",
            "1. SOFORT zum Arzt\n2. Antibiotika\n3. Ruhigstellung\n4. Kühlung\n5. Bei Abszess: Drainage\n6. Bei schwerer: IV-Antiobiotika\n7. Bei Gesichtsbeteiligung: Sofort-Antiobiotika",
            "Amoxicillin/Clavulansäure 1g 3x tgl\nBei Penicillinallergie: Clindamycin 600mg 3x tgl\nAbszess: I&D",
            "BEI ROTER, HEISSER, GESCHWOLLENER HAUT: Sofort zum Kinderarzt\nBei Kindern: Oft periorbital (Gesicht)\nI.v. Antibiotika bei Kindern oft nötig"));

        conditions.add(new MedicalCondition("223", "Eczema", "Ekzem",
            "eczema, atopic dermatitis, itchy skin, dry skin, red skin, ekzem, atopische dermatitis, juckreiz",
            "1. Feuchtigkeitscreme 2-3x tgl\n2. Kortikoidcreme bei Schüben\n3. Juckreiz stillende Mittel\n4. Allergenvermeidung\n5. Kurze, lauwarme Duschen\n6. Baumwollkleidung\n7. Bei Infektion: Antibiotikum",
            "Tacrolimus 0.1% oder Pimecrolimus bei empfindlichen Stellen\nKortikoid: Hydrocortison 1%\nAntihistaminikum: Cetirizin 10mg tgl",
            "Bei Babys: Atopisches Ekzem (Milchschorf)\nFrühe Therapie wichtig\nHautpflege 2x tgl\nKortikoid nur kurz"));

        conditions.add(new MedicalCondition("224", "Psoriasis", "Schuppenflechte",
            "psoriasis, scaly skin, red plaques, silvery scales, joint pain, schuppenflechte, psoriasis",
            "1. Dermatologe\n2. Topische Therapie\n3. Lichttherapie\n4. Systemische Therapie bei schwer\n5. Biologika\n6. Stressreduktion\n7. Gelenkcheck",
            "Topisch: Kortikosteroide + Vitamin D3 Analoga\nLichttherapie: PUVA, UVB\nSystemisch: Methotrexat 15-25mg/Woche\nBiologika: Adalimumab",
            "Bei Kindern: juvenile Psoriasis möglich\nKinderdermatologe\nTopische Therapie bei Kindern first-line\nBiologika nur bei schwerer Psoriasis"));

        conditions.add(new MedicalCondition("225", "Acne", "Akne",
            "acne, pimples, blackheads, oily skin, face breakouts, akne, pickel, hautunreinheiten",
            "1. Milde Reinigung 2x tgl\n2. Nicht ausdrücken\n3. Benzoylperoxid\n4. Retinoide\n5. Bei schwerer: Dermatologe\n6. Hormontherapie bei Frauen\n7. Narbenprophylaxe",
            "Benzoylperoxid 5%\nAdapalen 0.1% Gel\nTretinoin 0.025% Creme\nBei schwer: Isotretinoin 0.5-1mg/kg",
            "Bei Jugendlichen: Häufig\nMilde Akne: Benzoeperoxid 5%\nBei Entzündung: Erythromycin Lösung\nKeine systemischen Retinoide bei Kindern unter 12"));

        conditions.add(new MedicalCondition("226", "Vitiligo", "Vitiligo",
            "vitiligo, white patches on skin, depigmentation, vitiligo, pigmentverlust",
            "1. Dermatologe\n2. Topische Therapie\n3. PUVA-Lichttherapie\n4. Depigmentierung bei ausgedehnter Vitiligo\n5. Camouflage\n6. Bei aktiver: Immunmodulatoren\n7. Kosmetische Abdeckung",
            "Topisch: Kortikosteroide oder Calcineurinhemmer\nPUVA: 8-MOP + UVA\nBei hartnäckig: Tacrolimus 0.1%",
            "Bei Kindern: Möglich\nFrühe Therapie besser\nSonnencreme auf helle Flecken\nPsychologische Betreuung"));

        conditions.add(new MedicalCondition("227", "Ringworm", "Ringelflechte",
            "ringworm, tinea, fungal skin infection, circular rash, jock itch, athlete's foot, ringelflechte, fußpilz",
            "1. Pilztest (KOH)\n2. Antimykotikum Creme\n3. Bei ausgedehnt: orale Therapie\n4. Hygiene\n5. Kontaktpersonen behandeln\n6. Bei Kopfpilz: Shampoo\n7. Wäsche bei 60°C waschen",
            "Clotrimazol 1% Creme 2x tgl 4 Wochen\nMiconazol 2% Creme\nBei Kopfpilz: Terbinafin 250mg tgl 4 Wochen",
            "Bei Kindern: Häufig\nKopfpilz bei Schulkindern\nTerbinafin Saft für Kinder\nKontaktpersonen mitbehandeln"));

        conditions.add(new MedicalCondition("228", "Herpes Zoster", "Gürtelrose",
            "herpes zoster, shingles, painful rash, blisters on back, nerve pain, gürtelrose, herpes",
            "1. Sofort zum Arzt\n2. Virostatika innerhalb 72h\n3. Schmerztherapie\n4. Austrocknung der Blasen\n5. Bei Gesichtsbeteiligung: sofort zum Augenarzt\n6. postherpetische Neuralgie verhindern\n7. Impfung ab 60",
            "Valaciclovir 1g 3x tgl 7 Tage\nBei Schmerzen: Pregabalin 75-150mg 2x\nBei Neuralgie: Amitriptylin 25-75mg",
            "BEI GÜRTELROSE IM GESICHT: Sofort zum Arzt\nBei Kindern: Selten\nBei Immunschwäche häufiger\nWindpockenimpfung schützt"));

        conditions.add(new MedicalCondition("229", "Chickenpox", "Windpocken",
            "chickenpox, varicella, itchy blisters, fever, spots, windpocken, varicellen",
            "1. Juckreizstillende Mittel\n2. Kühlende Lotionen\n3. Fiebersenkung (kein Aspirin)\n4. Baden mit Haferstroh\n5. Isolierung bis alle Blasen verkrustet\n6. Bei Immunschwäche oder Erwachsenen: Virostatika\n7. Impfstatus prüfen",
            "Symptomatisch:\n- Juckreiz: Polidocanol Creme\n- Fieber: Paracetamol (KEIN Aspirin - Reye)\nBei Risikopatienten: Aciclovir 800mg 5x tgl 7 Tage",
            "Bei Kindern: Meist harmlos\nAber Säuglinge und Immunschwache gefährdet\nJuckreiz: Fenistil Gel\nKEIN Aspirin bei Kindern"));

        conditions.add(new MedicalCondition("230", "Measles", "Masern",
            "measles, rubeola, high fever, cough, rash, red spots, masern, rötheln",
            "1. Sofort zum Arzt (Meldepflicht)\n2. Isolation\n3. Fiebersenkung\n4. Vitamin A\n5. Hustenstillung\n6. Bei Komplikationen: Krankenhaus\n7. Impfstatus",
            "Symptomatisch:\n- Fieber: Paracetamol\n- Husten: Codein 20mg bei Bedarf\n- Vitamin A 200.000 IE für 2 Tage",
            "BEI MASERN: Meldepflicht\nBei Kindern: Gefährlich\nHohes Fieber, Husten, Bindehautentzündung, fleckiger Ausschlag\nVitamin A reduziert Komplikationen"));

        conditions.add(new MedicalCondition("231", "Rubella", "Röteln",
            "rubella, german measles, mild fever, rash, swollen lymph nodes, rötheln",
            "1. Arztbesuch\n2. Isolation\n3. Fiebersenkung\n4. Bei Gelenkschmerzen: NSAR\n5. Bei Schwangeren: IG-Prophylaxe\n6. Impfstatus\n7. Kontaktpersonen informieren",
            "Symptomatisch:\n- Fieber: Paracetamol\n- Gelenkschmerzen: Ibuprofen 400mg",
            "BEI RÖTELN: Meldepflicht\nBei Kindern: Meist mild\nAber Röteln in Schwangerschaft katastrophal\nBei Schwangeren: Sofort IG"));

        conditions.add(new MedicalCondition("232", "Mumps", "Mumps",
            "mumps, swollen parotid glands, jaw pain, fever, orchitis, mumps, ohrspeicheldrüsenentzündung",
            "1. Arztbesuch\n2. Symptomatische Therapie\n3. Kühlung der geschwollenen Drüsen\n4. Flüssige Ernährung\n5. Bei Orchitis: Bettruhe, Kühlung\n6. Bei Hirnhautentzündung: Krankenhaus\n7. Impfstatus",
            "Symptomatisch:\n- Schmerz: Paracetamol 1g\n- Kühlung\nBei Orchitis: Prednison 50mg absteigend",
            "BEI MUMPS: Meldepflicht\nBei Kindern: Meist harmlos\nImpfschutz beachten (MMR)\nOrchitis bei 20-30% der postpubertären Männer"));

        conditions.add(new MedicalCondition("233", "Whooping Cough", "Keuchhusten",
            "whooping cough, pertussis, severe coughing fits, vomiting after coughing, keuchhusten, pertussis",
            "1. Sofort zum Arzt (Meldepflicht)\n2. Antibiotika (Makrolide)\n3. Isolation 5 Tage nach Antibiotika\n4. Husten stillende Mittel\n5. Bei Säuglingen: Krankenhaus\n6. Impfstatus\n7. Postexpositionsprophylaxe",
            "Azithromycin 500mg Tag 1, 250mg Tag 2-5\nOder Clarithromycin 500mg 2x 7 Tage",
            "BEI KEUCHHUSTEN: Meldepflicht\nBei Säuglingen: Lebensgefährlich\nParoxysmaler Husten, Posttussives Erbrechen\nBei Säuglingen: Stationäre Aufnahme"));

        conditions.add(new MedicalCondition("234", "Meningitis", "Hirnhautentzündung",
            "meningitis, stiff neck, severe headache, fever, sensitivity to light, hirnhautentzündung, meningitis",
            "1. SOFORT 112\n2. Lumbalpunktion\n3. Antibiotika/Virostatika\n4. Intensivstation\n5. Bei erhöhtem ICP: Mannitol\n6. Kortikosteroide\n7. Supportivtherapie",
            "BEI VERDACHT: SOFORT 112\nEmpirisch: Ceftriaxon 2g 2x i.v. + Ampicillin 2g 4x i.v. + Dexamethason",
            "BEI VERDACHT AUF HIRNHÄUTENTZÜNDUNG: SOFORT 112\nNackensteife, Fieber, Kopfschmerzen, Lichtscheu\nBei Kindern: Oft unspezifisch"));

        conditions.add(new MedicalCondition("235", "Encephalitis", "Hirnuentzündung",
            "encephalitis, brain inflammation, confusion, fever, seizures, headache, hirnentzündung, encephalitis",
            "1. SOFORT zum Arzt\n2. MRT und Lumbalpunktur\n3. Virostatika (Aciclovir)\n4. Bei HSV: sofort Aciclovir\n5. Antiepileptika\n6. Intensivtherapie\n7. Rehabilitation",
            "Bei HSV: Aciclovir 10mg/kg 3x i.v. 14-21 Tage\nBei CMV: Ganciclovir 5mg/kg 2x i.v.",
            "BEI VERDACHT AUF HIRNENTZÜNDUNG: Sofort 112\nBei Kindern: Meist viral\nHSV-Enzephalitis ist Notfall"));

        conditions.add(new MedicalCondition("236", "Sepsis", "Blutvergiftung",
            "sepsis, blood poisoning, high fever, rapid breathing, confusion, low blood pressure, blutvergiftung, sepsis",
            "1. SOFORT 112\n2. Blutkulturen\n3. Breitspektrum-Antibiotika\n4. Volumentherapie\n5. Vasopressoren\n6. Intensivstation\n7. Quelle kontrollieren",
            "SOFORT 112\nIm Krankenhaus:\n- Breitspektrum AB: Piperacillin/Tazobactam 4.5g 4x i.v.\n- Volumen: 30mL/kg Kristalloide",
            "BEI VERDACHT AUF BLUTVERGIFTUNG: SOFORT 112\nBei Kindern: Fieber + Petechien + Apathie\nPetekhien am Körper sind Warnsignal\nSofort in Klinik"));

        conditions.add(new MedicalCondition("237", "Tetanus", "Wundstarrkrampf",
            "tetanus, lockjaw, muscle spasms, difficulty swallowing, stiff jaw, wundstarrkrampf, tetanus",
            "1. SOFORT zum Arzt\n2. Wundversorgung\n3. Humanes Tetanusimmunglobulin\n4. Aktivimpfung\n5. Muskelrelaxantien\n6. Beatmung bei Bedarf\n7. Impfstatus auffrischen",
            "Im Krankenhaus:\n- TIG 500 IE i.m.\n- Tetanusimpfstoff 0.5mL i.m.\n- Metronidazol 500mg 4x i.v.",
            "BEI TETANUS: Sofort zum Arzt\nBei Kindern: Impfschutz essentiell\nWundversorgung + TIG + Impfung\nWundstarrkrampf ist NOTFALL"));

        conditions.add(new MedicalCondition("238", "Rabies", "Tollwut",
            "rabies, hydrophobia, fear of water, agitation, animal bite, confusion, tollwut, bissex",
            "1. SOFORT Wundversorgung und Impfung\n2. Humanes Rabies Immunglobulin (HRIG)\n3. Impfstoff an Tag 0, 3, 7, 14, 28\n4. Bei Biss: 112\n5. Tier beobachten\n6. Kein Alkohol\n7. Bei Symptomen: Fast immer tödlich",
            "SOFORT nach Biss:\n- Wunde reinigen mit Seife 15 Min\n- HRIG 20 IE/kg\n- Impfstoff 0.5mL i.m. Tag 0, 3, 7, 14, 28",
            "BEI TOLLWUTVERDACHT: Sofort zum Arzt\nTollwut ist IMMER tödlich nach Symptombeginn\nJeder Biss muss behandelt werden"));

        conditions.add(new MedicalCondition("239", "Lyme Disease", "Borreliose",
            "lyme disease, tick bite, erythema migrans, bull's eye rash, joint pain, borreliose, zeckenbiss",
            "1. Bei Zeckenbiss: beobachten\n2. Bei Erythema migrans: Sofort zum Arzt\n3. Antibiotika\n4. Bei Lyme-Arthritis: längere Therapie\n5. Bei Neuroborreliose: IV-Antibiose\n6. Kontrolle nach Therapie\n7. Zeckenschutz",
            "Bei Erythema migrans: Doxycyclin 100mg 2x 10-21 Tage\nBei Unverträglichkeit: Amoxicillin 1g 3x 14 Tage",
            "BEI ZECKENBISS: Zecke entfernen, beobachten\nBei Kindern unter 9: Doxycyclin kontraindiziert\nAmoxicillin für Kinder"));

        conditions.add(new MedicalCondition("240", "Malaria", "Malaria",
            "malaria, tropical disease, fever, chills, sweats, headache, travel to africa, malaria",
            "1. Sofort zum Tropenmediziner\n2. Bluttest (Dicke Tropfen)\n3. Art确定\n4. Je nach Art: Chloroquin, ACT oder Atovaquon\n5. Bei schwerer: IV-Artesunat\n6. Supportivtherapie\n7. Prophylaxe bei Reisen",
            "Bei unkomplizierter P. falciparum: Artemether/Lumefantrin 4 Dosen über 48h\nBei schwerer P. falciparum: Artesunat IV",
            "BEI MALARIAVERDACHT NACH REISE: Sofort zum Tropenmediziner\nBei Kindern: Oft milder Verlauf bei P. falciparum\nSchnelltest"));

        conditions.add(new MedicalCondition("241", "Dengue Fever", "Dengue Fieber",
            "dengue fever, tropical fever, severe headache, retro-orbital pain, rash, muscle pain, dengue",
            "1. Sofort zum Tropenmediziner\n2. Blutbild kontrollieren (Thrombozyten)\n3. Flüssigkeit\n4. Paracetamol (kein NSAR)\n5. Bei Warnzeichen: sofort in Klinik\n6. Bei schwerer: Intensivstation\n7. Erneute Infektion gefährlicher",
            "Symptomatisch:\n- Paracetamol 1g 4x tgl (KEIN Aspirin/NSAR)\n- Flüssigkeit 3L tgl",
            "BEI DENGUE: Sofort zum Tropenmediziner\nBei Kindern: Häufig\nÜbertragung durch Aedes-Mücke\nWarnzeichen: Bauchschmerzen, Erbrechen"));

        conditions.add(new MedicalCondition("242", "COVID-19", "COVID-19",
            "covid, coronavirus, fever, cough, loss of taste, shortness of breath, covid, SARS-CoV-2",
            "1. Testen (Schnelltest/PCR)\n2. Isolation 5-10 Tage\n3. Symptomatische Therapie\n4. Bei Atemnot: sofort zum Arzt\n5. Paxlovid bei Risikopatienten\n6. Bei schwerer: Remdesivir, Dexamethason\n7. Impfung",
            "Bei leichten Symptomen: Abwarten, Paracetamol\nBei Risikopatienten: Paxlovid 300/100mg 2x 5 Tage",
            "BEI COVID-19: Isolation zu Hause möglich bei milden Symptomen\nBei Atemnot, Brustschmerzen, Bewusstseinstrübung: Sofort 112"));

        conditions.add(new MedicalCondition("243", "Influenza", "Grippe",
            "influenza, flu, high fever, body aches, cough, fatigue, influenza, echte grippe",
            "1. Testen (Schnelltest)\n2. Ruhe\n3. Flüssigkeit\n4. Fiebersenkung\n5. Bei Risikopatienten: Oseltamivir 75mg 2x 5 Tage\n6. Bei Atemnot: zum Arzt\n7. Jährliche Impfung",
            "Oseltamivir (Tamiflu) 75mg 2x 5 Tage\nZanamivir 10mg 2x 5 Tage (inhalativ)",
            "BEI GRIPPE: Kinderarzt bei Kleinkindern\nBei Kindern unter 5: Höheres Risiko für Komplikationen\nOseltamivir Sirup für Kinder"));

        conditions.add(new MedicalCondition("244", "Strep Throat", "Streptokokken Angina",
            "strep throat, strep, painful throat, white patches, fever, scharlach, streptokokken angina",
            "1. Rachenabstrich\n2. Bei positiv: Penicillin V\n3. Vollständige Therapie (10 Tage)\n4. Bei Penicillinallergie: Makrolide\n5. Schulkinder: 24h nach Therapiebeginn\n6. Bei Scharlach: Exanthem beachten\n7. Bei Rezidiv: Tonsillektomie",
            "Penicillin V 1 Mio IE 3x tgl 10 Tage\nBei Penicillinallergie: Azithromycin 500mg Tag1, 250mg Tag2-5",
            "BEI STREPTOKOKKEN: Antibiotika 10 Tage\nBei Kindern: Scharlach möglich (Hautausschlag, Erdbeerzunge)\nRachenabstrich vor Therapie"));

        conditions.add(new MedicalCondition("245", "Scarlet Fever", "Scharlach",
            "scarlet fever, strep scarlet, sandpaper rash, strawberry tongue, scharlach",
            "1. Rachenabstrich\n2. Penicillin 10 Tage\n3. Bettruhe\n4. Hautpflege\n5. Bei Schuppung: Creme\n6. Isolation 24h nach Antibiotika\n7. Komplikationen beachten",
            "Penicillin V 1,5 Mio IE 3x tgl 10 Tage\nBei Allergie: Clarithromycin 250mg 2x 10 Tage",
            "BEI SCHARLACH: Meldepflicht\nBei Kindern: Häufig\nTypisch: feuerroter Rachen, Himbeerzunge, Sandpapierekzem\nPenicillin 10 Tage"));

        conditions.add(new MedicalCondition("246", "Impetigo", "Eiterflechte",
            "impetigo, honey-colored crusts, bacterial skin infection, eiterflechte, börner",
            "1. Arzt\n2. Lokale Antibiotikum\n3. Crust entfernen\n4. Nicht kratzen\n5. Hygiene\n6. Bei ausgedehnt: orale Antibiotika\n7. Kontrolle",
            "Lokal: Fusidinsäure 2% Creme 5 Tage\nMupirocin 2% Salbe\nBei ausgedehnt: Flucloxacillin 1g 3x tgl",
            "Bei Kindern: Sehr häufig\nTypisch: honiggelbe Krusten\nLokale Antibiotika first-line\nKratzen vermeiden - Ausbreitung"));

        conditions.add(new MedicalCondition("247", "Conjunctivitis", "Bindehautentzündung",
            "conjunctivitis, pink eye, red eye, discharge, watery eyes, bindehautentzündung, augentripper",
            "1. Arzt\n2. Tränenersatz\n3. Antibiotische Augentropfen\n4. Hände waschen\n5. Nicht reiben\n6. Kontaktlinsen raus\n7. Bei Neugeborenen: sofort zum Arzt",
            "Bei bakteriell: Tobramycin Augentropfen 0.3% 3x tgl\nBei viral: Tränenersatz, kühle Kompressen",
            "BEI NEUGEBORENEN MIT EITER: Sofort zum Kinderarzt\nGonokokken-Blenorrhoe ist Notfall\nBei Kindern: Meist viral"));

        conditions.add(new MedicalCondition("248", "Stye", "Gerstenkorn",
            "stye, hordeolum, painful lump on eyelid, eye pain, gerstenkorn",
            "1. Warme Kompresse 10min 4x tgl\n2. Nicht ausdrücken\n3. Bei Infektion: Antibiotikum Augentropfen\n4. Bei persistierend: Inzision\n5. Kontaktlinsen raus\n6. Make-up vermeiden\n7. Hände weg",
            "Warme Kompresse 10min 4x tgl 5-7 Tage\nBei nicht remitieren: Fusidinsäure Augentropfen/-salbe",
            "Bei Kindern: Häufig\nWarme Kompresse 4x täglich\nNicht selbst ausdrücken\nBei Kindern: Augenarzt wenn nach 48h keine Besserung"));

        conditions.add(new MedicalCondition("249", "Otitis Externa", "Außenohrentzündung",
            "otitis externa, swimmer's ear, ear canal infection, ear pain, außenohrentzündung",
            "1. Ohr trocken halten\n2. Saure Ohrentropfen\n3. Antibiotikum Ohrentropfen\n4. Schmerzmittel\n5. Keine Wattestäbchen\n6. Bei Pilz: Antimykotikum\n7. Schwimmer nicht schwimmen",
            "Locacortenvital Ohrentropfen (Hydrocortison + Cliochinol)\nBei bakteriell: Ofloxacin 0.3% Ohrentropfen",
            "Bei Kindern: Häufig nach Schwimmen\nOhrentropfen 3x täglich\nOhr trocken halten\nKeine Wattestäbchen"));

        conditions.add(new MedicalCondition("250", "Otitis Media", "Mittelohrentzündung",
            "otitis media, middle ear infection, ear pain, fever, hearing loss, mittelohrentzündung",
            "1. Arzt\n2. Abwarten 48-72h bei Kindern >2 Jahren\n3. Schmerzmittel\n4. Bei Indikation: Antibiotika\n5. abschwellende Nasentropfen\n6. Paukenröhrchen bei rezidiv\n7. HNO-Arzt",
            "Abwarten 48-72h bei Kindern >2 Jahren mit milden Symptomen\nBei Indikation: Amoxicillin 500mg 3x tgl 5-7 Tage",
            "BEI MITTELOHENTZÜNDUNG: Kinderarzt\nBei Säuglingen: Häufigste Infektion\nAbwarten 48-72h bei Kindern >2\nSchmerzmittel\nBei Fieber, Schmerzen: Antibiotika"));

        conditions.add(new MedicalCondition("251", "Labyrinthitis", "Gleichgewichtsstörung",
            "labyrinthitis, vertigo, dizziness, spinning sensation, balance problems, schwindel",
            "1. Bettruhe\n2. Bei Übelkeit: Antivertiginosa\n3. Bei viral: Virostatika\n4. Kortikosteroide\n5. Physiotherapie\n6. Langsam aufstehen\n7. Bei persistierend: HNO-Arzt",
            "Bei akutem Schwindel: Dimenhydrinat 50-100mg 4x tgl\nBei viraler Labyrinthitis: Prednisolon 50mg absteigend",
            "Bei Kindern: Selten\nOft nach viraler Infektion\nBettruhe\nMedikamente für Kinder anpassen"));

        conditions.add(new MedicalCondition("252", "Tinnitus", "Tinnitus",
            "tinnitus, ringing in ears, buzzing, hissing, ears ringing, tinnitus, ohrensausen",
            "1. HNO-Arzt\n2. Hörtest\n3. Ursachenabklärung\n4. Sound Therapy\n5. CBT\n6. Bei plötzlich: sofort zum Arzt\n7. Stressreduktion",
            "Bei akutem Tinnitus: Prednisolon 50mg 5 Tage absteigend\nBei subakut: Infusionen (Pentoxifyllin 400mg 2x)",
            "BEI PLÖTZLICHEM TINNITUS: Sofort zum HNO-Arzt\nBei Kindern: Selten\nAkuter Tinnitus = Hörsturz möglich"));

        conditions.add(new MedicalCondition("253", "Hearing Loss", "Hörverlust",
            "hearing loss, deafness, reduced hearing, hearing impaired, schwerhörigkeit, taubheit",
            "1. HNO-Arzt\n2. Hörtest\n3. Ursache klären\n4. Hörgerät\n5. Cochlea-Implantat\n6. Logopädie\n7. Bei plötzlich: Notfall",
            "Bei plötzlichem Hörverlust: Sofort Prednisolon 250mg i.v. 3 Tage\nHörgeräte Anpassung\nCochlea-Implantat bei Gehörlosigkeit",
            "BEI PLÖTZLICHEM HÖRVERLUST: Sofort zum HNO-Arzt\nBei Kindern: Hörscreenning nach Geburt\nBei Entwicklungsverzögerung: Hörtest"));

        conditions.add(new MedicalCondition("254", "Sinusitis", "Nasennebenhöhlenentzündung",
            "sinusitis, sinus infection, facial pain, congestion, headache, sinus, nasennebenhöhlen",
            "1. Nasenspülung\n2. abschwellende Nasentropfen\n3. Schmerzmittel\n4. Bei bakteriell: Antibiotika\n5. Inhalation\n6. Bei chronisch: OP\n7. Allergietest",
            "Bei viral: Nasenspülung, abschwellende Mittel\nBei bakteriell (>10 Tage): Amoxicillin 500mg 3x 7 Tage",
            "Bei Kindern: Häufig\nNasenspülung mit Kochsalzlösung\nNasentropfen für Kinder\nBei Fieber >3 Tage: Antibiotika"));

        conditions.add(new MedicalCondition("255", "Nosebleed", "Nasenbluten",
            "nosebleed, epistaxis, bleeding from nose, nasal bleeding, nasenbluten",
            "1. Aufrecht setzen\n2. Kopf nach vorne\n3. Nasenflügel zusammen drücken 10min\n4. Kühl auf Nase\n5. Bei Persistenz: Tamponade\n6. Ursache abklären\n7. Bei rezidivierend: HNO-Arzt",
            "Konservativ: Nasenflügel komprimieren 10-15min\nBei Persistenz: Nasentamponade\nBei rezidivierend: Kauterisation",
            "Bei Kindern: Sehr häufig\nKopf nach vorne (nicht zurück)\nNasenflügel 10 Minuten drücken\nBei anhaltend: Kinderarzt/HNO"));

        conditions.add(new MedicalCondition("256", "Pharyngitis", "Rachenentzündung",
            "pharyngitis, sore throat, throat pain, difficulty swallowing, rachenentzündung",
            "1. Gurgeln mit Salzwasser\n2. Lutschtabletten\n3. Schmerzmittel\n4. Bei Strep: Antibiotika\n5. Feuchtigkeit\n6. Bei Atemnot: 112\n7. Rachenabstrich",
            "Bei viral: Symptomatisch\nBei bakteriell (Strep): Penicillin V 1 Mio IE 3x 10 Tage",
            "Bei Kindern: Häufig\nGurgeln mit Salzwasser (ab 6 Jahren)\nLutschtabletten für Kinder\nBei Fieber, Schluckbeschwerden: Kinderarzt"));

        conditions.add(new MedicalCondition("257", "Laryngitis", "Kehlkopfentzündung",
            "laryngitis, hoarseness, loss of voice, sore throat, cough, kehlkopfentzündung",
            "1. Stimme schonen\n2. Nicht flüstern\n3. Feuchtigkeit\n4. Bei Husten: Sirup\n5. Bei viral: abwarten\n6. Bei bakteriell: Antibiotika\n7. Bei Atemnot: 112",
            "Stimmruhe (nicht Flüstern) 1-2 Wochen\nInhalation mit Kochsalzlösung\nBei bakteriell: Amoxicillin 500mg 3x tgl",
            "BEI KEHLKOPFENTZÜNDUNG: Kinderarzt\nBei Kindern: Pseudokrupp möglich\nBellender Husten, Atemnot, Pfeifen beim Einatmen\nBei Atemnot: Sofort 112"));

        conditions.add(new MedicalCondition("258", "Epiglottitis", "Kehldeckelentzündung",
            "epiglottitis, drooling, difficulty swallowing, high fever, muffled voice, kehldeckelentzündung",
            "1. SOFORT 112\n2. Nicht den Mund öffnen\n3. Keine Untersuchung\n4. Krankenwagen\n5. Intubation\n6. Antibiotika\n7. Impfstatus",
            "SOFORT 112\nIm Krankenhaus:\n- Keine orale Untersuchung\n- Intubation/tracheostomie\n- i.v. Ceftriaxon 2g",
            "BEI EPIGLOTTITIS: SOFORT 112\nBei Kindern: Selten durch Impfung (Hib)\nDrohende Atemwegsobstruktion\nNicht flach liegen lassen"));

        conditions.add(new MedicalCondition("259", "Pneumothorax", "Lungenkollaps",
            "pneumothorax, collapsed lung, sudden chest pain, shortness of breath, lungercollaps",
            "1. SOFORT 112\n2. Sauerstoff\n3. Ruhe bewahren\n4. Nicht schlucken\n5. Bei Spannung: Nadeldekompression\n6. Thoraxdrainage\n7. OP bei Bedarf",
            "SOFORT 112\nDiagnose: Röntgen\nBei Spannungspneumothorax: Nadeldekompression\nThorakozentese",
            "BEI PLÖTZLICHER ATEMNOT + BRUSTSCHMERZ: Sofort 112\nBei Kindern: Primärer Spontanpneumothorax selten\nSauerstoffgabe\nBei Spannungspneumothorax: Nadeldekompression"));

        conditions.add(new MedicalCondition("260", "Pleurisy", "Rippenfellentzündung",
            "pleurisy, pleuritis, chest pain with breathing, sharp pain, rippenfellentzündung",
            "1. Arzt\n2. Röntgen\n3. Ursache behandeln\n4. Schmerzmittel\n5. Bei bakteriell: Antibiotika\n6. Bei Tumor: Onkologe\n7. Pleurapunktion",
            "Bei bakterieller Pleuritis: Ceftriaxon 2g i.v. + Metronidazol 500mg i.v.\nSchmerz: Ibuprofen 600mg 3x",
            "BEI BRUSTSCHMERZEN BEIM ATMEN: Sofort zum Kinderarzt\nBei Kindern: Nach Infektionen möglich\nRöntgen\nBei Fieber + Brustschmerz: Sofort"));

        conditions.add(new MedicalCondition("261", "Pulmonary Fibrosis", "Lungenfibrose",
            "pulmonary fibrosis, scarring of lung tissue, shortness of breath, dry cough, lung fibrosis",
            "1. Lungenfunktionstest\n2. CT\n3. Biopsie\n4. Ursache finden\n5. antifibrotische Therapie\n6. Sauerstoff bei Bedarf\n7. Lungentransplantation",
            "Bei idiopathisch: Pirfenidon 801mg 3x tgl oder Nintedanib 150mg 2x\nSauerstoffgabe bei Ruhehypoxie",
            "BEI LUNGENFIBROSE: Sofort zum Pneumologen\nBei Kindern: Selten\nAngeborene Formen möglich\nKinderpneumologie"));

        conditions.add(new MedicalCondition("262", "Cystic Fibrosis", "Mukoviszidose",
            "cystic fibrosis, cf, thick mucus, lung infections, pancreatic insufficiency, mukoviszidose",
            "1. Sofort zum Pneumologen\n2. Schweißtest\n3. Genetik\n4. Mukolytika\n5. Physiotherapie\n6. Pankreasenzyme\n7. CF-Zentrum",
            "Mukolytika: Dornase alfa 2.5mg Inhalation täglich\nBei Infektion: Tobramycin Inhalation 300mg 2x\nPankreasenzyme: Creon 25000",
            "BEI MUKOVISZIDOSE: Sofort in CF-Zentrum\nBei Neugeborenen: Neugeborenenscreening\nBei Kindern: Lebenslang Pneumologie + Gastroenterologie"));

        conditions.add(new MedicalCondition("263", "Sarcoidosis", "Sarkoidose",
            "sarcoidosis, lung nodules, hilar lymphadenopathy, skin lesions, sarcoidose",
            "1. Pneumologe/Dermatologe\n2. Röntgen/CT\n3. Biopsie\n4. Stadium bestimmen\n5. Bei Symptomen: Kortikosteroide\n6. Bei Stadium II-IV: Immunsuppression\n7. Monitoring",
            "Bei leichter Sarkoidose: Often Spontanremission, abwarten\nBei Stadium II-IV: Prednisolon 20-40mg tgl 6-12 Monate",
            "BEI SARKOIDOSE: Pneumologe\nBei Kindern: Selten\nLöfgren-Syndrom: Bilaterale Hiluslymphadenopathie + Erythema nodosum\nMeist selbstlimitierend"));

        conditions.add(new MedicalCondition("264", "Sleep Apnea", "Schlafapnoe",
            "sleep apnea, snoring, breathing pauses during sleep, daytime sleepiness, schlafrapnoe",
            "1. Schlaflabor\n2. Polysomnographie\n3. CPAP-Therapie\n4. Gewichtsreduktion\n5. Bei Kindern: Adenoide\n6. Zahnärztliche Schiene\n7. OP bei Indikation",
            "Bei obstruktiver Schlafapnoe: CPAP 5-15 mbar\nBei zentraler: Adaptive Servoventilation\nGewichtsreduktion",
            "BEI SCHLAFAPNOE: Schlaflabor\nBei Kindern: Adenoide oft Ursache\nAdenoidektomie kann heilend sein\nSchnarchen + Tagesmüdigkeit = Schlaflabor"));

        conditions.add(new MedicalCondition("265", "Insomnia", "Schlafstörung",
            "insomnia, sleeplessness, cannot fall asleep, wake up at night, schlafstörung",
            "1. Schlafhygiene\n2. CBT-I\n3. Entspannungstechniken\n4. Bei akut: Z-Substanzen\n5. Bei chronisch: niedrig dosierte Antidepressiva\n6. Melatonin\n7. Ursache behandeln",
            "Kurzzeitig: Zolpidem 10mg zur Nacht oder Temazepam 20mg\nLangzeitig: CBT-I\nBei Depression: Mirtazapin 15-45mg zur Nacht",
            "Bei Kindern: Schlafhygiene wichtig\nFeste Schlafenszeiten\nBildschirmzeit vor Schlaf vermeiden\nKeine Medikamente ohne ärztliche Anweisung"));

        conditions.add(new MedicalCondition("266", "Restless Legs", "Restless Legs Syndrom",
            "restless legs, rls, urge to move legs, uncomfortable sensation, worse at night, unruhige beine",
            "1. Eisenlevel prüfen\n2. Dopaminagonisten\n3. Gabapentin\n4. Eisenersatz wenn deficient\n5. Lebensstiländerungen\n6. Beinmassage\n7. Koffein reduzieren",
            "Bei Eisenmangel: Eisen fumarat 100mg 2x tgl\nBei primärem RLS: Pramipexol 0.18-0.54mg oder Ropinirol 0.5-4mg zur Nacht",
            "Bei Kindern: Möglich\nEisenstatus prüfen\nSymptome oft als Wachstumsschmerzen fehlgedeutet\nBei anhaltenden Beschwerden: Kinderneurologe"));

        conditions.add(new MedicalCondition("267", "Narcolepsy", "Narkolepsie",
            "narcolepsy, excessive daytime sleepiness, sudden sleep attacks, cataplexy, schlafsucht",
            "1. Schlaflabor\n2. Multiple Sleep Latency Test\n3. Modafinil\n4. Natrium Oxybate\n5. Antidepressiva bei Kataplexie\n6. Schlafhygiene\n7. Nickerchen planen",
            "Erste Wahl: Modafinil 100-400mg morgens\nZweite Wahl: Methylphenidat 10-40mg 2-3x tgl\nBei Kataplexie: Venlafaxin 75-225mg",
            "BEI NARKOLEPSIE: Schlaflabor\nBei Kindern: Selten aber möglich\nDiagnose oft verzögert\nKataplexie ist pathognomonisch\nLebenslange Therapie"));

        conditions.add(new MedicalCondition("268", "Migraine with Aura", "Migräne mit Aura",
            "migraine with aura, visual disturbances, zigzag lines, blind spots, hemiplegic migraine, migräne mit aura",
            "1.Neurologe\n2. Trigger vermeiden\n3. Prophylaxe\n4. Bei Aura: Triptane\n5. Magnesium\n6. Akuttherapie\n7. Mydriatikum bei retinaler Aura",
            "Prophylaxe: Propranolol 40-240mg oder Topiramat 50-100mg oder Amitriptylin 25-75mg\nAkut: Sumatriptan 50-100mg bei Auraende",
            "BEI MIGRÄNE MIT AURA: Neurologe\nBei Kindern: Möglich\nVisuelle Aura: Flimmern, Zickzack-Linien, blinde Flecken\nKeine Triptane während Aura"));

        conditions.add(new MedicalCondition("269", "Cluster Headache", "Clusterkopfschmerz",
            "cluster headache, severe one-sided headache, eye tearing, nasal congestion, clusterkopfschmerz",
            "1. Neurologe\n2. Sauerstoffgabe\n3. Triptane sc\n4. Verapamil Prophylaxe\n5. Lithium bei chronisch\n6. Kapsaicin Nasenspray\n7. Nervenstimulation",
            "Akut: Sauerstoff 12L/min 15min oder Sumatriptan 6mg sc\nProphylaxe: Verapamil 240-960mg tgl",
            "BEI CLUSTERKOPFSCHMERZ: Neurologe\nBei Kindern: Sehr selten\nStärkste Kopfschmerzen\nMänner häufiger\nSauerstoff zu Hause"));

        conditions.add(new MedicalCondition("270", "Trigeminal Neuralgia", "Trigeminusneuralgie",
            "trigeminal neuralgia, face pain, electric shock pain, jaw pain, trigeminusneuralgie",
            "1. Neurologe\n2. MRT\n3. Carbamazepin\n4. Oxcarbazepin\n5. Bei Versagen: OP\n6. Bei MS: andere Therapien\n7. Schmerzmanagement",
            "Erste Wahl: Carbamazepin 200-1200mg tgl\nAlternativ: Oxcarbazepin 300-1800mg tgl\nBei Therapieresistenz: Mikrovasculäre Dekompression",
            "BEI GESICHTSSCHMERZEN: Neurologe\nBei Kindern: Selten\nElektrischer Schlag-Charakteristik\nCarbamazepin ist Diagnostisch und therapeutisch"));

        conditions.add(new MedicalCondition("271", "Bell's Palsy", "Gesichtslähmung",
            "bell's palsy, facial nerve palsy, one-sided face drooping, cannot close eye, Gesichtslähmung",
            "1. Sofort zum Arzt\n2. Prednisolon\n3. Augenschutz\n4. Physiotherapie\n5. Bei Herpes: Virostatika\n6. Augenoperateur\n7. Monitoring",
            "Prednisolon 60mg tgl 5 Tage, dann absteigend\nBei Herpes Zoster: + Aciclovir 800mg 5x 5 Tage",
            "BEI GESICHTSLÄHMUNG: Sofort zum Kinderarzt\nBei Kindern: Möglich\nPlötzliche einseitige Gesichtsschwäche\nPrednisolon innerhalb 72h\nAuge schützen"));

        conditions.add(new MedicalCondition("272", "Multiple Sclerosis", "Multiple Sklerose",
            "multiple sclerosis, ms, vision problems, numbness, weakness, fatigue, multiple sklerose",
            "1. Neurologe\n2. MRT\n3. Lumbalpunktion\n4. Immuntherapie\n5. Schubtherapie\n6. Verlaufstherapie\n7. Rehabilitation",
            "Bei Schub: Methylprednisolon 1g i.v. 3-5 Tage\nImmuntherapie: Interferon beta-1a oder Glatirameracetat",
            "BEI MULTIPLER SKLEROSE: Neurologe\nBei Kindern: Möglich (pädiatrische MS)\nFrühzeitige Immuntherapie wichtig\nMRT + Lumbalpunktion"));

        conditions.add(new MedicalCondition("273", "Parkinson's Disease", "Parkinson",
            "parkinson, tremor, rigidity, bradykinesia, shuffling gait, parkinson",
            "1. Neurologe\n2. DaT-Scan\n3. Levodopa\n4. Dopaminagonisten\n5. MAO-B-Hemmer\n6. Physiotherapie\n7. Bei Fluktuationen: Pumpen",
            "Levodopa/Carbidopa 100/25mg 3-4x tgl\nDopaminagonisten: Pramipexol 1.05-3.15mg tgl\nMAO-B-Hemmer: Rasagilin 1mg tgl",
            "BEI PARKINSON: Neurologe\nBei Kindern: Sehr selten\nJuveniler Morbus Parkinson möglich\nBei Kindern mit Tremor: Kinderneurologe"));

        conditions.add(new MedicalCondition("274", "Epilepsy", "Epilepsie",
            "epilepsy, seizures, convulsions, fits, seizure, epilepsie, krampfanfall",
            "1. Neurologe\n2. EEG\n3. MRT\n4. Antiepileptika\n5. Status epilepticus: Notfallprotokoll\n6. Bei remittiert: Auslassversuch\n7. Fahrtauglichkeit",
            "Valproat 500-2000mg tgl\nLevetiracetam 1000-3000mg tgl\nLamotrigin 100-400mg tgl",
            "BEI ERSTEM KRAMPFANFALL: Sofort zum Kinderarzt\nBei Kindern: Häufig\nFieberkrämpfe bei Kleinkindern oft harmlos\nEEG\nAntiepileptika nach Gewicht"));

        conditions.add(new MedicalCondition("275", "Febrile Seizures", "Fieberkrämpfe",
            "febrile seizures, convulsions with fever, seizure in child, fahrenheit, fieberkrampf",
            "1. Kind auf Seite legen\n2. Ruhe bewahren\n3. Nicht festhalten\n4. Fieber senken\n5. Bei >5min: Notfallmedikament\n6. Nach Anfall: Arzt\n7. Aufzeichnung",
            "Akuttherapie bei prolongiertem Fieberkrampf (>5min):\n- Midazolam 5mg nasal oder Bukkal\n-oder Diazepam 5mg rektal",
            "BEI FIEBERKRAMPF: Ruhe bewahren, Kind auf Seite\nBei Kindern 6 Monate bis 5 Jahre\nEinfache Fieberkrämpfe: Meist harmlos\nNotfallmedikament verschreiben lassen"));

        conditions.add(new MedicalCondition("276", "Status Epilepticus", "Status Epilepticus",
            "status epilepticus, prolonged seizure, seizure lasting more than 5 minutes, dauerhafter krampfanfall",
            "1. SOFORT 112\n2. Kind auf Seite\n3. Nicht festhalten\n4. Zeit messen\n5. Notfallmedikament\n6. Krankenwagen\n7. Intensivstation",
            "SOFORT 112\nPrähospital: Midazolam 5mg nasal/bukkal oder Diazepam 10mg rektal\nIm Krankenhaus: Lorazepam 4mg i.v.",
            "BEI STATUS EPILEPTICUS: SOFORT 112\nBei Kindern: Notfall\nJede Minute zählt für Outcome\nPrähospitale Notfallmedikation\nIntensivstation"));

        conditions.add(new MedicalCondition("277", "Stroke", "Schlaganfall",
            "stroke, brain attack, paralysis, slurred speech, facial drooping, sudden numbness, schlaganfall",
            "1. SOFORT 112\n2. Zeit notieren\n3. Liegen lassen\n4. Keine Nahrung\n5. Thrombolyse/Thrombektomie\n6. Stroke Unit\n7. Rehabilitation",
            "SOFORT 112\nIschämisch: Alteplase 0.9mg/kg i.v. innerhalb 4.5h\nMechanische Thrombektomie bis 6h",
            "BEI SCHLAGANFALL: SOFORT 112\nBei Kindern: Möglich\nJede Minute zählt\nTime is Brain\nPrähospitale Thrombolyse/Thrombektomie"));

        conditions.add(new MedicalCondition("278", "Transient Ischemic Attack", "Transitorische Ischämische Attacke",
            "tia, ministroke, brief paralysis, temporary vision loss, speech problems, vorübergehende durchblutungsstörung",
            "1. SOFORT zum Arzt\n2. Ursache klären\n3. Doppler/Duplex\n4. Echokardiographie\n5. Antithrombotische Therapie\n6. Risikofaktoren behandeln\n7. Prävention",
            "Duplev-Ultraschall der hirnversorgenden Arterien\nEchokardiographie\nAntithrombozytäre Therapie: ASS 100mg tgl oder Clopidogrel 75mg tgl",
            "BEI TIA: Sofort zum Kinderarzt/Notaufnahme\nBei Kindern: Selten\nSchlaganfall-Risiko erhöht nach TIA\nSofortige Abklärung"));

        conditions.add(new MedicalCondition("279", "Brain Aneurysm", "Hirnschlagader-Aneurysma",
            "brain aneurysm, cerebral aneurysm, berry aneurysm, headaches, rupture, subarachnoidalblutung",
            "1. Neurologe/Neurochirurg\n2. CTA/MRA/DSA\n3. Bei Ruptur: SOFORT 112\n4. Clipping oder Coiling\n5. Bei unrupturiert: Risiko-Nutzen Abwägung\n6. Blutdruckkontrolle\n7. Kontrolle",
            "Bei Ruptur: Sofort 112\nIntensivstation\nNimodipin 60mg 4x p.o./i.v. 21 Tage\nAneurysma-Clipping oder endovaskuläres Coiling",
            "BEI HIRNANEURYSMA: Neurochirurg\nBei Kindern: Selten\nAngeborene Aneurysmen oder bei Marfan/Ehlers-Danlos\nRuptur = Subarachnoidalblutung = Katastrophe"));

        conditions.add(new MedicalCondition("280", "Hydrocephalus", "Wasserkopf",
            "hydrocephalus, enlarged ventricles, head circumference increase, pressure on brain, wasserkopf",
            "1. Sofort zum Neurochirurgen\n2. MRT\n3. Shunt-Implantation\n4. Bei akut: Notfall-Drainage\n5. Shunt-Kontrolle\n6. Entwicklungsüberwachung\n7. Bei Versagen: Revision",
            "Ventrikuloperitonealer Shunt (VP-Shunt)\nProgrammierbarer Shunt\nBei akutem Hydrozephalus: Externer Ventrikulärer Drain (EVD)",
            "BEI WASSERKOPF: Sofort zum Neurochirurgen\nBei Kindern: Angeboren oder erworben\nVergrößerter Kopfumfang bei Säuglingen\nSpannungsfontanelle"));

        conditions.add(new MedicalCondition("281", "Spina Bifida", "Offener Rücken",
            "spina bifida, neural tube defect, myelomeningocele, back lesion, paralysis, offener rücken",
            "1. Sofort zum Neurochirurgen/Urologen\n2. MRT\n3. OP innerhalb 48h\n4. Shunt bei Hydrozephalus\n5. Urologische Überwachung\n6. Orthopädische Versorgung\n7. Rehabilitation",
            "Chirurgisch: Verschluss des Defekts innerhalb 48h\nBei Hydrozephalus: VP-Shunt",
            "BEI SPINA BIFIDA: Sofort in Kinderklinik\nBei Kindern: Offener Rücken bei Geburt\nSofortige OP\nLebenslange Betreuung\nFolsäureprophylaxe in Schwangerschaft"));

        conditions.add(new MedicalCondition("282", "Cerebral Palsy", "Cerebralparese",
            "cerebral palsy, cp, movement disorder, muscle stiffness, coordination problems, zerebralparese",
            "1. Kinderneurologe\n2. Physio- und Ergotherapie\n3. Medikamente für Spastik\n4. Orthesen\n5. Bei Schluckstörung: Logopädie\n6. Botulinumtoxin\n7. Bei schwer: Rollstuhl",
            "Bei Spastik: Baclofen 30-150mg tgl oder Tizanidin 12-36mg tgl\nBotulinumtoxin A Injektionen in betroffene Muskeln",
            "BEI ZEREBRALPARESE: Kinderneurologe und Frühförderung\nBei Kindern: Häufigste körperliche Behinderung\nFrühzeitige Therapie entscheidend"));

        conditions.add(new MedicalCondition("283", "Muscular Dystrophy", "Muskeldystrophie",
            "muscular dystrophy, muscle weakness, difficulty walking, progressive, duchenne, muskeldystrophie",
            "1. Kinderneurologe\n2. CK erhöht\n3. Genetik\n4. Physiotherapie\n5. Kortikosteroide\n6. Kardiologische Überwachung\n7. Bei Duchenne: Rollstuhl",
            "Bei Duchenne: Prednison 0.75mg/kg tgl oder Deflazacort 0.9mg/kg\nKardiologische Überwachung",
            "BEI MUSKELDYSTROPHIE: Kinderneurologe\nBei Duchenne: X-chromosomal-rezessiv, Jungen betroffen\nCK stark erhöht\nMuskelschwäche ab 2-3 Jahren"));

        conditions.add(new MedicalCondition("284", "Myasthenia Gravis", "Myasthenia Gravis",
            "myasthenia gravis, mg, muscle weakness, ptosis, double vision, fatigue, myasthenia gravis",
            "1. Neurologe\n2. Repetitive Nervenstimulation\n3. Acetylcholinrezeptor-Antikörper\n4. Cholinesterasehemmer\n5. Immunsuppression\n6. Bei Krise: ICU\n7. Thymektomie",
            "Symptomatisch: Pyridostigmin 30-60mg 4x tgl\nImmunsuppression: Prednison 1mg/kg tgl",
            "BEI MYASTHENIA GRAVIS: Neurologe\nBei Kindern: Möglich, aber selten\nTägliche Fluktuation der Symptome\nBelastungsabhängige Verschlechterung"));

        conditions.add(new MedicalCondition("285", "Guillain-Barré Syndrome", "Guillain-Barré Syndrom",
            "guillain-barré, ascending paralysis, tingling, cannot walk, areflexia, guillain-barré",
            "1. SOFORT zum Arzt\n2. LP (Eiweißvermehrung)\n3. Plasmapherese oder IVIG\n4. Überwachung (Atemfunktion)\n5. Physiotherapie\n6. Rehabilitation\n7. Bei Atemstillstand: Beatmung",
            "Sofort: Plasmapherese 4-6 Zyklen oder IVIG 0.4g/kg 5 Tage\nMonitoring: Vitalkapazität, Atemmuskulatur",
            "BEI GUILLAIN-BARRÉ: Sofort in Klinik\nBei Kindern: Möglich\nNach Infekt (Campylobacter, CMV, EBV)\nAufsteigende Lähmung\nAreflexie"));

        conditions.add(new MedicalCondition("286", "Poliomyelitis", "Kinderlähmung",
            "poliomyelitis, polio, paralysis, muscle weakness, polio, schluckauf",
            "1. Meldepflicht\n2. Sofort zum Infektiologen\n3. Supportivtherapie\n4. Physiotherapie\n5. Bei Atemlähmung: Beatmung\n6. Orthopädische Versorgung\n7. Impfstatus",
            "Keine kausale Therapie\nSupportiv: Schmerztherapie, Physiotherapie\nBei respiratorischer Insuffizienz: Maskenbeatmung",
            "BEI POLIOVERDACHT: Meldepflicht und sofort zum Infektiologen\nDank Impfung selten in entwickelten Ländern\nImpfung schützt (IPV)"));

        conditions.add(new MedicalCondition("287", "Neuropathy", "Nervenerkrankung",
            "neuropathy, peripheral neuropathy, numbness, tingling, burning feet, nervenerkrankung",
            "1. Neurologe\n2. NCV/EMG\n3. Ursache finden\n4. Bei diabetisch: Blutzucker optimieren\n5. Medikamente für neuropathische Schmerzen\n6. Physiotherapie\n7. Fußpflege",
            "Bei diabetischer Neuropathie: Duloxetin 60-120mg tgl oder Pregabalin 300-600mg tgl\nTopisch: Capsaicin 0.075%",
            "BEI NEUROPATHIE: Neurologe\nBei Kindern: Selten\nBei Diabetes, B12-Mangel, Medikamenten\nKribbeln, Taubheit, Brennen\nUrsache finden"));

        conditions.add(new MedicalCondition("288", "Carpal Tunnel Syndrome", "Karpaltunnelsyndrom",
            "carpal tunnel, wrist pain, numbness in fingers, tingling, hand weakness, karpaltunnelsyndrom",
            "1. Orthopäde/Neurologe\n2. EMG\n3. Konservativ: Schiene\n4. Bei schwer: OP\n5. Ergotherapie\n6. Kortikosteroid Injektion\n7. Ursache behandeln",
            "Konservativ: Handgelenksschiene in Neutralstellung nachts\nLokale Kortikosteroid-Injektion\nBei schwer: Karpaltunnel-Release",
            "BEI KARPALTUNNEL: Orthopäde\nBei Kindern: Selten\nBei repetitiver Belastung, Schwangerschaft, Hypothyreose\nNächtliche Beschwerden typisch"));

        conditions.add(new MedicalCondition("289", "Sciatica", "Ischias",
            "sciatica, back pain radiating to leg, leg numbness, pirifom syndrome, ischias",
            "1. Orthopäde\n2. MRT bei neurologischen Ausfällen\n3. Konservativ: Physiotherapie\n4. Schmerzmittel\n5. Bei Bandscheibenvorfall: OP\n6. Infiltrationen\n7. Rückenschule",
            "Konservativ: Physiotherapie, NSAR (Ibuprofen 600mg 3x), Wärme\nBei schwer: Prednisolon 50mg 10 Tage",
            "BEI ISCHIAS: Orthopäde\nBei Kindern: Selten\nBei Bandscheibenvorfall, Spondylolisthese\nPhysiotherapie first-line"));

        conditions.add(new MedicalCondition("290", "Fibromyalgia", "Fibromyalgia",
            "fibromyalgia, widespread pain, tender points, fatigue, sleep disturbance, fibromyalgie",
            "1. Rheumatologe\n2. Diagnose (ACR-Kriterien)\n3. Ausdauertraining\n4. CBT\n5. Medikamente\n6. Stressmanagement\n7. Schlafhygiene",
            "Ausdauertraining (Kardio) 30min 3x Woche\nMedikamente: Duloxetin 60mg tgl oder Pregabalin 300-450mg tgl",
            "BEI FIBROMYALGIE: Rheumatologe\nBei Kindern: Möglich (juvenile Fibromylagie)\nChronische Schmerzen an typischen Punkten\nMeist bei Mädchen 13-15 Jahre"));

        conditions.add(new MedicalCondition("291", "Lupus", "Systemischer Lupus Erythematodes",
            "lupus, sle, joint pain, butterfly rash, fatigue, photosensitivity, lupus",
            "1. Rheumatologe\n2. Bluttests (ANA, Anti-dsDNA)\n3. Organdiagnostik\n4. Immunsuppression\n5. Sonnenschutz\n6. Bei Nephritis: aggressive Therapie\n7. Monitoring",
            "Leichte Form: Hydrochloroquin 200-400mg tgl + ggf. niedrig dosiert Prednison\nBei Organbeteiligung: Prednison + Mycophenolat Mofetil",
            "BEI LUPUS: Rheumatologe\nBei Kindern: Möglich, oft schwererer Verlauf als bei Erwachsenen\nSchmetterlingserythem, Gelenkschmerzen, Fatigue"));

        conditions.add(new MedicalCondition("292", "Rheumatoid Arthritis", "Rheumatoide Arthritis",
            "rheumatoid arthritis, ra, joint swelling, morning stiffness, autoimmune, rheumatoide arthritis",
            "1. Rheumatologe\n2. Bluttests (RF, Anti-CCP)\n3. Bildgebung\n4. DMARDs\n5. Biologika\n6. Physiotherapie\n7. Monitoring",
            "DMARD: Methotrexat 15-25mg/Woche (Folsäure)\nBiologika: Adalimumab 40mg s.c. 2-wöchentlich",
            "BEI RHEUMATOIDER ARTHRITIS: Rheumatologe\nBei Kindern: Juvenile idiopathische Arthritis (JIA)\nMorgensteifigkeit, Gelenkschwellung\nFrühzeitige DMARD-Therapie"));

        conditions.add(new MedicalCondition("293", "Osteoarthritis", "Arthrose",
            "osteoarthritis, oa, joint pain, stiffness, cartilage wear, degenerative, arthrose",
            "1. Orthopäde\n2. Röntgen\n3. Gewichtsreduktion\n4. Physiotherapie\n5. Schmerzmittel\n6. Bei schwer: Gelenkersatz\n7. Hyaluronsäure",
            "Nicht-medikamentös: Gewichtsreduktion, Physiotherapie\nMedikamentös: Paracetamol 1g 3x, NSAR (Ibuprofen 600mg 3x)",
            "BEI ARTHROSE: Orthopäde\nBei Kindern: Selten (sekundär bei Fehlbildung, nach Trauma)\nGewichtsreduktion, Physiotherapie"));

        conditions.add(new MedicalCondition("294", "Gout", "Gicht",
            "gout, uric acid, painful big toe, joint inflammation, tophi, gicht",
            "1. Internist/Rheumatologe\n2. Harnsäure im Blut\n3. Gelenkpunktion (Nadeleinschlüsse)\n4. Akuttherapie\n5. Langzeittherapie\n6. Diät\n7. Bei Tophi: Therapie intensivieren",
            "Akut: Colchicin 0.5mg 3x tgl oder NSAR (Naproxen 500mg 2x) oder Prednisolon 35mg 5 Tage\nLangzeit: Allopurinol 100-900mg tgl",
            "BEI GICHT: Internist/Rheumatologe\nBei Kindern: Selten\nOft bei Stoffwechselstörungen, Leukämiebehandlung\nHyperurikämie"));

        conditions.add(new MedicalCondition("295", "Osteoporosis", "Osteoporose",
            "osteoporosis, bone loss, fractures, low bone density, brittle bones, osteoporose",
            "1. Internist/Gynäkologe\n2. DXA-Scan\n3. Bluttests (Ca, Vit D, PTH)\n4. Bisphosphonate\n5. Vitamin D + Calcium\n6. Bewegung\n7. Sturzprophylaxe",
            "Bisphosphonate: Alendronat 70mg 1x/Woche oder Zoledronat 5mg i.v. 1x/Jahr\nCalcium 1000mg + Vitamin D3 800-2000IE tgl",
            "BEI OSTEOPOROSE: Internist\nBei Kindern: Sekundär (Cortisontherapie, Immobilisation, Anorexia)\nBMD-Messung mit pQCT"));

        conditions.add(new MedicalCondition("296", "Osteomyelitis", "Knocheninfektion",
            "osteomyelitis, bone infection, fever, local pain, swelling, joint infection, knocheninfektion",
            "1. SOFORT zum Ortopäden\n2. MRT\n3. Biopsie/Kultur\n4. Langzeit-Antibiotika\n5. Bei Sequester: OP\n6. IV-Antibiose\n7. Monitoring",
            "Empirisch: Flucloxacillin 2g 4x i.v. + Ceftriaxon 2g tgl\nBei MRSA: Vancomycin 1g 2x i.v.\nDauer: 4-6 Wochen i.v.",
            "BEI KNOCHENINFEKTION: Sofort zum Orthopäden\nBei Kindern: Hämatogen oder nach Trauma\nStaphylokokken häufig\nMRT zur Ausdehnung"));

        conditions.add(new MedicalCondition("297", "Bursitis", "Schleimbeutelentzündung",
            "bursitis, joint swelling, pain, olecranon, prepatellar, trochanteric, schleimbeutelentzündung",
            "1. Orthopäde\n2. Kühlen\n3. NSAR\n4. Bei infiziert: Punktion + Antibiotika\n5. Physiotherapie\n6. Bei rezidivierend: OP/Botenox\n7. Druck vermeiden",
            "Konservativ: Ruhe, Kälte, NSAR (Ibuprofen 600mg 3x)\nBei infektiös: Punktion, Kultur, Flucloxacillin 2g 4x i.v.",
            "BEI SCHLEIMBEUTELENTZÜNDUNG: Orthopäde\nBei Kindern: Selten\nOlecranon- und Patellarbursitis am häufigsten\nKühlen, NSAR"));

        conditions.add(new MedicalCondition("298", "Tendinitis", "Sehnenentzündung",
            "tendinitis, tendon pain, overuse, tennis elbow, golfer's elbow, achilles tendinitis, sehnenentzündung",
            "1. Orthopäde\n2. Ruhe\n3. Eis\n4. Dehnung/Physiotherapie\n5. NSAR\n6. Bei chronisch: Stoßwellentherapie\n7. Bei Ruptur: OP",
            "Konservativ: Ruhe, Eis, NSAR (Diclofenac 50mg 3x), Physiotherapie\nBei chronisch: Stoßwellentherapie (ESWT)",
            "BEI SEHNENENTZÜNDUNG: Orthopäde\nBei Kindern: Möglich bei Überbelastung (Sport)\nExzentrische Dehnungsübungen\nNSAR lokal oder oral"));

        conditions.add(new MedicalCondition("299", "Plantar Fasciitis", "Plantare Fasziitis",
            "plantar fasciitis, heel pain, morning stiffness, foot pain, sehnenfascitis, fußschmerzen",
            "1. Orthopäde\n2. Dehnungsübungen\n3. Einlagen\n4. Kälte-/Wärmetherapie\n5. NSAR\n6. Kortikoidinjektion\n7. Bei chronisch: Stoßwelle",
            "Dehnungsübungen für Achillessehne und Plantarfaszie 3x tgl\nEinlagen/Fersenpolster\nNSAR (Ibuprofen 600mg 3x)",
            "BEI PLANTARER FASZIITIS: Orthopäde\nBei Kindern: Möglich, besonders bei Sport\nDehnungsübungen 3x täglich\nFußgymnastik"));

        conditions.add(new MedicalCondition("300", "Rheumatic Fever", "Rheumatisches Fieber",
            "rheumatic fever, strep complications, joint pain, carditis, chorea, rheumatic fever, gelenkschmerzen",
            "1. Sofort zum Kinderarzt\n2. Jones-Kriterien\n3. Penicillintherapie\n4. ASS\n5. Bei Karditis: Prednison\n6. Prophylaxe\n7. Kardiologische Überwachung",
            "Akut: Penicillin G 1-2 Mio IE 4x i.v. 10-14 Tage\nBei Karditis: Prednison 2mg/kg tgl 2-4 Wochen\nSekundärprophylaxe: Benzathin-Penicillin",
            "BEI RHEUMATISCHEM FIEBER: Sofort zum Kinderarzt\nNach Streptokokken-Infektion\nBei Kindern: Hauptursache für Herzerkrankungen in Entwicklungsländern"));

        conditions.add(new MedicalCondition("301", "Scleroderma", "Sklerodermie",
            "scleroderma, skin hardening, raynaud's, tight skin, sklerodermie",
            "1. Rheumatologe/Dermatologe\n2. Autoantikörper (Scl-70, CENP-B)\n3. Organscreening\n4. Immunsuppression\n5. Raynaud's-Schutz\n6. Physiotherapie\n7. Monitoring",
            "Bei limitiert (CREST): Calcipotriol lokal, Hände schützen\nBei diffus: Methotrexat 15-25mg/Woche\nRaynaud: Calciumantagonisten (Nifedipin 30-60mg tgl)",
            "BEI SKLERODERMIE: Rheumatologe\nBei Kindern: Sehr selten\nJuvenile systemische Sklerodermie\nHautverhärtung, Raynaud, Organsymptome"));

        conditions.add(new MedicalCondition("302", "Dermatomyositis", "Dermatomyositis",
            "dermatomyositis, muscle weakness, skin rash, heliotrope rash, gottron papules, dermatomyositis",
            "1. Rheumatologe/Neurologe\n2. CK, EMG, MRT\n3. Hautbiopsie/Muskelbiopsie\n4. Immunsuppression\n5. Physiotherapie\n6. Sonnenschutz\n7. Monitoring",
            "Prednison 1mg/kg tgl + Methotrexat 15-25mg/Woche\nBei schwer: IVIG 2g/kg über 2-5 Tage\nSonnenschutz ( SPF 50+)",
            "BEI DERMATOMYOSITIS: Rheumatologe\nBei Kindern: Möglich, oft mit malignom-Assoziation\nHeliotropes Exanthem, Gottron-Papeln\nCK erhöht"));

        conditions.add(new MedicalCondition("303", "Sjögren's Syndrome", "Sjögren-Syndrom",
            "sjogren's, dry eyes, dry mouth, autoimmune, sjogren, sicca",
            "1. Rheumatologe\n2. Schirmer-Test, Speicheltest\n3. Autoantikörper (Ro/SSA, La/SSB)\n4. Symptomatisch\n5. Immunsuppression bei viszeral\n6. Zahnärztliche Betreuung\n7. Augenärztliche Betreuung",
            "Symptomatisch: Tränenersatz, Speichelersatz, Pilocarpin 5-10mg 3x tgl\nBei viszeraler Beteiligung: Hydroxychloroquin, Methotrexat",
            "BEI SjöGREN-SYNDROM: Rheumatologe\nBei Kindern: Möglich\nTrockene Augen und Mund\nSymptomatische Therapie"));

        conditions.add(new MedicalCondition("304", "Vasculitis", "Gefäßentzündung",
            "vasculitis, blood vessel inflammation, purpura, leg pain, fatigue, gefäßentzündung",
            "1. Rheumatologe\n2. Biopsie\n3. ANCA, Complement\n4. Immunsuppression\n5. Je nach Typ unterschiedlich\n6. Organmonitoring\n7. Bei Kindern: oft IgA-Vaskulitis",
            "Bei IgA-Vaskulitis: Prednison 1mg/kg 1-2 Wochen\nBei ANCA-assoziierter Vaskulitis: Rituximab oder Cyclophosphamid + Prednison",
            "BEI VASKULITIS: Rheumatologe\nBei Kindern: Häufig IgA-Vaskulitis (Henoch-Schönlein)\nPalpable Purpura, Bauchschmerzen, Arthralgien, Nephritis"));

        conditions.add(new MedicalCondition("305", "Polycystic Kidney Disease", "Polyzystische Nierenerkrankung",
            "polycystic kidney disease, pckd, cysts in kidneys, high blood pressure, enlarged kidneys, polyzystische nieren",
            "1. Nephrologe\n2. Ultraschall/CT\n3. Blutdruckkontrolle\n4. ACE-Hemmer\n5. Bei PKD1: Tolvaptan\n6. Bei Niereninsuffizienz: Dialyse\n7. Leberzysten überwachen",
            "Blutdruck: Ramipril 5-10mg tgl\nBei rapid progresiver PKD1: Tolvaptan 60-120mg tgl\nBei terminaler Niereninsuffizienz: Dialyse",
            "BEI POLYZYSTISCHER NIERENERKRANKUNG: Nephrologe\nBei Kindern: Autosomal-rezessiv (ARPKD) oder autosomal-dominant (ADPKD)\nBlutdruckkontrolle"));

        conditions.add(new MedicalCondition("306", "Nephrotic Syndrome", "Nephrotisches Syndrom",
            "nephrotic syndrome, proteinuria, edema, low albumin, high cholesterol, nephrotisches syndrom",
            "1. Sofort zum Nephrologen\n2. Urinprotein/Kreatinin-Quotient\n3. Albumin im Blut\n4. Nierenbiopsie bei Indikation\n5. Kortikosteroide\n6. Bei steroidresistent: Cyclophosphamid\n7. Diät",
            "Erste Therapie: Prednison 60mg/m² tgl 4-6 Wochen, dann 40mg/m² 4-6 Wochen\nBei steroidresistent: Cyclophosphamid 2mg/kg tgl 8-12 Wochen",
            "BEI NEPHROTISCHEM SYNDROM: Sofort zum Nephrologen\nBei Kindern: Häufig (MCD häufigste Ursache)\nMassive Proteinurie, Ödeme, Hypalbuminämie"));

        conditions.add(new MedicalCondition("307", "Kidney Failure", "Nierenversagen",
            "kidney failure, renal failure, high creatinine, dialysis, kidney dysfunction, nierenversagen",
            "1. Sofort zum Nephrologen\n2. Kreatinin, Harnstoff, GFR\n3. Ursache finden\n4. Bei akut: Behandlung + Dialyse\n5. Bei chronisch: Vorbereitung zur Dialyse/Transplantation\n6. Diät\n7. Erythropoetin",
            "Bei akutem Nierenversagen: Ursache behandeln, Flüssigkeit, bei Bedarf Dialyse\nBei chronischer Niereninsuffizienz: ACE-I/ARB, Erythropoetin",
            "BEI NIERENVERSAGEN: Sofort zum Nephrologen\nBei Kindern: Akut (nach Trauma, Infektion) oder chronisch (Fehlbildungen)\nDialyse bei akuter Indikation"));

        conditions.add(new MedicalCondition("308", "Urinary Stones", "Harnsteine",
            "urinary stones, kidney stones, ureteral stones, renal colic, blood in urine, harnsteine",
            "1. Ultraschall\n2. CT bei Bedarf\n3. Schmerztherapie\n4. Spontanabgang abwarten\n5. Bei gross >5mm: Lithotripsie\n6. Metaphylaxe\n7. Steinanalyse",
            "Bei Kolik: Tamsulosin 0.4mg tgl + Alfuzosin 10mg tgl\nSchmerz: Metamizol 1g 4x oder Diclofenac 75mg 2x\nESWL bei Steinen 5-20mm",
            "BEI HARNSTEINEN: Urologe\nBei Kindern: Metabolische Ursachen (Hyperkalziurie, Zystinurie)\nViel trinken (>2L/Tag)"));

        conditions.add(new MedicalCondition("309", "Benign Prostatic Hyperplasia", "Prostatavergrößerung",
            "bph, enlarged prostate, urinary obstruction, frequent urination, weak stream, prostatavergrößerung",
            "1. Urologe\n2. PSA\n3. Ultraschall\n4. Medikamentös\n5. Bei schwer: OP\n6. TUR-P\n7. Monitoring",
            "Medikamentös: Tamsulosin 0.4mg tgl + Dutasterid 0.5mg tgl\nBei akutem Harnverhalt: Katheterisierung, dann Alpha-Blocker",
            "BEI PROSTATAVERGRÖSSERUNG: Urologe\nBei Kindern: Nicht relevant (benigne Prostatahyperplasie ist Erkrankung des älteren Mannes)"));

        conditions.add(new MedicalCondition("310", "Prostatitis", "Prostataentzündung",
            "prostatitis, prostate inflammation, pelvic pain, burning urination, prostatitis",
            "1. Urologe\n2. Urinkultur\n3. Antibiotika\n4. Alpha-Blocker\n5. Bei chronisch: Physiotherapie\n6. Entzündungshemmer\n7. Wärme",
            "Bei akuter bakterieller Prostatitis: Ciprofloxacin 500mg 2x 14-28 Tage\nBei chronischem Beckenschmerzsyndrom: Alpha-Blocker + Antibiotika 4-6 Wochen",
            "BEI PROSTATITIS: Urologe\nBei Kindern: Selten\nAkute Prostatitis mit Fieber und Dysurie\nAntibiotika"));

        conditions.add(new MedicalCondition("311", "Testicular Torsion", "Hodentorsion",
            "testicular torsion, twisted testicle, sudden severe testicular pain, swelling, hodentorsion",
            "1. SOFORT 112\n2. Urologe\n3. NOTFALL-OP\n4. Innerhalb 6h retten\n5. Fixation des anderen Hodens\n6. Bei Infarzierung: Abtragung\n7. Monitoring",
            "SOFORT 112\nDiagnose: Klinische Untersuchung, Doppler-Ultraschall\nNotfall-OP: Innerhalb 6 Stunden Hoden retten",
            "BEI HODENTORSION: SOFORT 112, NOTFALL-OP\nBei Kindern: Häufiger Notfall\nPlötzlicher Hodenschmerz, Übelkeit, Erbrechen\nZeitfenster 6 Stunden = Hoden retten"));

        conditions.add(new MedicalCondition("312", "Epididymitis", "Nebenhodenentzündung",
            "epididymitis, scrotal pain, swelling, fever, epididymitis, nebenhodenentzündung",
            "1. Urologe\n2. Urinkultur\n3. Antibiotika\n4. Kühlen\n5. Bettruhe\n6. Bei Abszess: Drainage\n7. Sexualpartnertherapie",
            "Bei sexuell aktiven <35: Ceftriaxon 500mg i.m. + Doxycyclin 100mg 2x 10 Tage\nBei >35 oder D-Katheter: Fluorchinolone",
            "BEI NEBENHODENENTZÜNDUNG: Urologe\nBei Kindern: Möglich, oft nach Infektion\nSchmerzhafte Hodenschwellung\nAntibiotika"));

        conditions.add(new MedicalCondition("313", "Varicocele", "Varikozele",
            "varicocele, dilated veins in scrotum, heavy feeling, infertility, varikozele",
            "1. Urologe\n2. Ultraschall\n3. Bei Symptomen: OP\n4. Bei Unfruchtbarkeit: IVF\n5. Sklerotherapie\n6. Monitoring\n7. Bei Kindern: Abwarten bis Wachstum abgeschlossen",
            "Bei Indikation: Mikrochirurgische Varikozelektomie\noder radiologische Embolisation\nBei Unfruchtbarkeit: ART",
            "BEI VARIKOZELE: Urologe\nBei Kindern: Häufig, oft asymptomatisch\nBei Hodenatrophie oder Unfruchtbarkeit: OP\nMikrochirurgische Technik bevorzugt"));

        conditions.add(new MedicalCondition("314", "Hydrocele", "Wasserbruch",
            "hydrocele, fluid around testicle, scrotal swelling, wasserbruch",
            "1. Urologe\n2. Ultraschall\n3. Bei symptomatisch: OP\n4. Bei Kindern: Often Spontanheilung bis 1 Jahr\n5. Sklerotherapie\n6. Monitoring\n7. Notfall bei Inkarzeration",
            "Bei symptomatisch: Vannas-OP oder Sklerotherapie mit Aethoxysklerol\nBei Kindern: Meist konservativ bis 1 Jahr",
            "BEI WASSERBRUCH: Urologe\nBei Kindern: Häufig bei Neugeborenen\nOft spontane Resolution bis 1. Lebensjahr\nBei anhaltend oder groß: OP"));

        conditions.add(new MedicalCondition("315", "Erectile Dysfunction", "Erektile Dysfunktion",
            "erectile dysfunction, ed, impotence, cannot get erection, erection problems, erektile dysfunktion",
            "1. Urologe/Androloge\n2. Hormonstatus\n3. Gefäßuntersuchung\n4. Psychologische Abklärung\n5. PDE-5-Hemmer\n6. Vakuumtherapie\n7. Bei Ursache: Behandlung",
            "PDE-5-Hemmer: Sildenafil 50-100mg bei Bedarf oder Tadalafil 10-20mg\nBei Hormonmangel: Testosteron-Gel oder -Injektion\nVakuumtherapie",
            "BEIER EREKTILER DYSFUNKTION: Urologe/Androloge\nBei jungen Männern: Oft psychogene Ursache\nPDE-5-Hemmer first-line\nUrsache abklären"));

        conditions.add(new MedicalCondition("316", "Premature Ejaculation", "Vorzeitiger Samenerguss",
            "premature ejaculation, pe, coming too fast, early ejaculation, samenerguss",
            "1. Urologe\n2. Psychologische Abklärung\n3. Verhaltenstherapie\n4. Topische Anästhetika\n5. PDE-5-Hemmer\n6. Antidepressiva\n7. Stop-Start-Technik",
            "Topisch: Lidocain/Prilocain Spray 5-10min vor dem Sex\nMedikamentös: Dapoxetin 30-60mg 1-3h vor Sex\nAntidepressiva: Paroxetin 20mg tgl",
            "BEI VORZEITIGEM SAMENERGUSS: Urologe\nBei jungen Männern: Oft psychogen\nVerhaltenstherapie (Stop-Start, Squeeze)\nTopische Anästhetika"));

        conditions.add(new MedicalCondition("317", "Balanitis", "Vorhautentzündung",
            "balanitis, foreskin inflammation, red penis, discharge, pain, vorhautentzündung",
            "1. Urologe\n2. Hygiene verbessern\n3. Antimykotische Creme\n4. Bei bakteriell: Antibiotikum\n5. Bei Phimose: OP\n6. Vorsicht bei Diabetes\n7. Zirkumzision",
            "Bei Pilz: Clotrimazol 1% Creme 2x tgl\nBei bakteriell: Fusidinsäure 2% Creme\nBei rezidivierend: Zirkumzision",
            "BEI VORHAUTENTZÜNDUNG: Urologe\nBei Kindern: Häufig\nVerbesserte Hygiene\nAntimykotische oder antibiotische Creme\nBei Phimose: OP"));

        conditions.add(new MedicalCondition("318", "Gynecomastia", "Gynäkomastie",
            "gynecomastia, enlarged breast tissue in men, man boob, brustvergrößerung mann",
            "1. Internist/Urologe\n2. Hormonstatus\n3. Ultraschall\n4. Bei Pubertät: Abwarten\n5. Bei persistierend: OP\n6. Bei Tumor: Abklärung\n7. Medikamente überprüfen",
            "Bei pubertärer Gynäkomastie: Abwarten 1-2 Jahre\nBei persistierend/ störend: Tamoxifen 10-20mg tgl oder OP (Liposuction+Mastektomie)",
            "BEI GYNÄKOMASTIE: Internist/Urologe\nBei Jugendlichen: Häufig in Pubertät\nOft spontane Regression\nBei persistierend: OP oder Tamoxifen"));

        conditions.add(new MedicalCondition("319", "Endometriosis", "Endometriose",
            "endometriosis, endometrial tissue outside uterus, pelvic pain, painful periods, endometriose",
            "1. Gynäkologe\n2. MRT/Ultraschall\n3. Laparoskopie\n4. Hormontherapie\n5. Bei Kinderwunsch: IVF\n6. Schmerztherapie\n7. Bei schwer: OP",
            "Hormontherapie: Dienogest 2mg tgl oder GnRH-Analoga (Leuprorelin 3.75mg/Monat)\nBei Kinderwunsch: IVF\nBei schwer: laparoskopische Exzision",
            "BEI ENDOMETRIOSE: Gynäkologe\nBei Jugendlichen: Möglich\nDysmenorrhoe, chronische Beckenschmerzen\nHormontherapie\nBei Kinderwunsch: IVF"));

        conditions.add(new MedicalCondition("320", "Polycystic Ovary Syndrome", "PCOS",
            "pcos, polycystic ovaries, irregular periods, excess hair, acne, obesity, eierstockzysten",
            "1. Gynäkologe/Endokrinologe\n2. Hormonstatus (Testosteron, LH/FSH)\n3. Ultraschall\n4. Lebensstiländerung\n5. Hormontherapie\n6. Bei Kinderwunsch: Clomifen\n7. Metformin",
            "Lebensstiländerung: Gewichtsreduktion 5-10%\nHormonell: Ethinylestradiol/Cyproteronacetat (Diane 35) oder Levonorgestrel-IUP\nBei Kinderwunsch: Clomifen 50-150mg",
            "BEI PCOS: Gynäkologe/Endokrinologe\nBei Jugendlichen: Häufig\nIrreguläre Zyklen, Hirsutismus, Akne\nGewichtsreduktion\nHormontherapie"));

        conditions.add(new MedicalCondition("321", "Pelvic Inflammatory Disease", "Beckenentzündung",
            "pid, pelvic inflammatory disease, lower abdominal pain, vaginal discharge, fever, beckenentzündung",
            "1. Gynäkologe\n2. Abstriche\n3. Antibiotika\n4. Partnerbehandlung\n5. Bei Abszess: Drainage\n6. Bei chronisch: OP\n7. Monitoring",
            "Ambulant: Ceftriaxon 500mg i.m. + Doxycyclin 100mg 2x 14 Tage + Metronidazol 500mg 2x 14 Tage\nStationär: Clindamycin + Gentamicin i.v.",
            "BEI BECKENENTZÜNDUNG: Gynäkologe\nBei jungen Frauen: Sexuell übertragbare Infektionen\nAntibiotika 14 Tage\nPartnerbehandlung"));

        conditions.add(new MedicalCondition("322", "Ovarian Cyst", "Eierstockzyste",
            "ovarian cyst, ovarian mass, abdominal pain, bloating, eierstockzyste",
            "1. Gynäkologe\n2. Ultraschall\n3. CA-125 bei >40\n4. Bei funktionell: Abwarten\n5. Bei persistierend >6cm: OP\n6. Bei Tumor: onkologische Abklärung\n7. Monitoring",
            "Bei funktionellen Zysten: Abwarten 6-8 Wochen\nBei >5cm oder persistierend: Laparoskopische Zystektomie oder Ovarektomie",
            "BEI EIERSTOCKZYSTE: Gynäkologe\nBei Jugendlichen: Häufig funktionelle Zysten\nMeist spontane Regression\nBei >5cm oder Schmerzen: OP"));

        conditions.add(new MedicalCondition("323", "Uterine Fibroids", "Gebärmuttermyom",
            "uterine fibroids, leiomyoma, heavy bleeding, pelvic pressure, myoma, myome",
            "1. Gynäkologe\n2. Ultraschall\n3. MRT bei Bedarf\n4. Bei symptomatisch: Hormontherapie\n5. Embolisation\n6. Myomektomie\n7. Bei schwer: Hysterektomie",
            "Bei symptomatisch: Ulipristalacetat 5mg tgl oder GnRH-Analoga\nEmbolisation (UAE)\nMyomektomie: laparoskopisch oder abdominal",
            "BEI GEBÄRMUTTERMYOM: Gynäkologe\nBei Frauen im gebärfähigen Alter: Häufig\nStarke Blutungen, Beckendruck\nUlipristalacetat oder GnRH-Analoga"));

        conditions.add(new MedicalCondition("324", "Cervical Cancer", "Gebärmutterhalskrebs",
            "cervical cancer, cervical carcinoma, abnormal bleeding, hpv, gebärmutterhalskrebs",
            "1. Gynäkologe\n2. Pap-Abstrich\n3. Kolposkopie + Biopsie\n4. HPV-Test\n5. Konisation\n6. Bei invasiv: OP + Strahlen\n7. Nachsorge",
            "Bei CIN: Konisation oder LLETZ\nBei invasiv: Radikale Hysterektomie + pelvine Lymphadenektomie\nStrahlentherapie + Chemotherapie",
            "BEI GEBÄRMUTTERHALSKREBS: Gynäkologe/Onkologe\nHPV-Impfung schützt!\nRegelmäßige Vorsorge (Pap-Abstrich)\nBei auffälligem Abstrich: Kolposkopie"));

        conditions.add(new MedicalCondition("325", "Breast Cancer", "Brustkrebs",
            "breast cancer, breast tumor, lump in breast, breast cancer, brustkrebs",
            "1. Gynäkologe/Brustzentrum\n2. Mammographie + Sonographie\n3. Biopsie\n4. Staging\n5. OP + Chemotherapie\n6. Strahlentherapie\n7. Hormontherapie",
            "OP: Brusterhaltende OP (BET) + Sentinellymphknotenbiopsie oder Mastektomie\nChemotherapie: Anthrazykline + Taxane\nHormontherapie: Tamoxifen oder Aromatasehemmer",
            "BEI BRUSTKREBS: Brustzentrum\nFamiliäre Belastung: Gentest (BRCA1/2)\nSelbstuntersuchung\nMammographie-Screening ab 50"));

        conditions.add(new MedicalCondition("326", "Prostate Cancer", "Prostatakrebs",
            "prostate cancer, elevated psa, prostate tumor, difficulty urinating, prostatakrebs",
            "1. Urologe\n2. PSA + digitale RE\n3. MRT\n4. Biopsie\n5. OP oder Strahlentherapie\n6. Bei metastasiert: Hormontherapie\n7. Nachsorge",
            "Bei lokalisiert: Radikale Prostatektomie oder Strahlentherapie\nBei metastasiert: Androgenentzugstherapie (Leuprorelin, Bicalutamid)\nChemotherapie bei CRPC",
            "BEI PROSTATAKREBS: Urologe\nPSA-Screening umstritten\nBei familiärer Belastung: Regelmäßige Kontrollen\nFrüherkennung wichtig"));

        conditions.add(new MedicalCondition("327", "Testicular Cancer", "Hodenkrebs",
            "testicular cancer, testicular tumor, lump in testicle, hodenkrebs",
            "1. Urologe\n2. Ultraschall\n3. Tumor marker (AFP, β-hCG, AFP)\n4. OP (inguinale Semikastration)\n5. Bei metastasiert: Chemotherapie\n6. Strahlentherapie\n7. Nachsorge",
            "OP: Radikale inguinale Semikastration + Hodenprothese\nBei Seminom: Carboplatin oder Strahlentherapie\nBei Nicht-Seminom: BEP-Chemotherapie (Bleomycin, Etoposid, Cisplatin)",
            "BEI HODENKREBS: Urologe/Onkologe\nHäufigster Krebs bei jungen Männern (15-35 Jahre)\nSelbstuntersuchung\nFrüh erkannt: Heilbar"));

        conditions.add(new MedicalCondition("328", "Skin Cancer Melanoma", "Schwarzer Hautkrebs",
            "melanoma, skin cancer, mole changing, abcde rule, black skin cancer, schwarzer hautkrebs",
            "1. Dermatologe\n2. Auflichtmikroskopie\n3. Biopsie\n4. Wächterlymphknotenbiopsie\n5. Staging\n6. OP\n7. Bei metastasiert: Immuntherapie",
            "OP: Weite Exzision mit 1-2cm Sicherheitsabstand\nBei metastasiert: Immuntherapie (Pembrolizumab, Nivolumab) oder zielgerichtete Therapie (BRAF-Hemmer)",
            "BEI MELANOM: Dermatologe\nABCDE-Regel beachten\nAsymmetrie, Begrenzung, Color, Durchmesser, Evolution\nSonnenschutz!\nFrüherkennung entscheidend"));

        conditions.add(new MedicalCondition("329", "Basal Cell Carcinoma", "Basalzellkarzinom",
            "basal cell carcinoma, bcc, skin cancer, rod ulcer, pearly nodule, hautkrebs",
            "1. Dermatologe\n2. Biopsie\n3. OP oder Strahlentherapie\n4. Bei klein: Kryotherapie\n5. Bei metastasiert: Hedgehog-Hemmer\n6. Monitoring\n7. Sonnenschutz",
            "OP: Mikroskopisch kontrollierte Chirurgie (MKC) oder Standard-OP\nBei klein und oberflächlich: Imiquimod Creme oder 5-FU\nBei metastasiert: Vismodegib 150mg tgl",
            "BEI BASALZELLKARZINOM: Dermatologe\nHäufigster Hautkrebs\nLangsam wachsend, selten metastasierend\nOP mit Mikroskopisch kontrollierter Chirurgie"));

        conditions.add(new MedicalCondition("330", "Squamous Cell Carcinoma", "Plattenepithelkarzinom",
            "squamous cell carcinoma, scc, skin cancer, scaling lesion, hornhautkrebs",
            "1. Dermatologe\n2. Biopsie\n3. OP\n4. Strahlentherapie\n5. Bei metastasiert: Chemotherapie\n6. Monitoring\n7. Sonnenschutz",
            "OP: Weite Exzision mit Sicherheitsabstand\nBei Hochrisiko: Sentinel-Node-Biopsie\nBei metastasiert: Cisplatin + 5-FU oder Immuntherapie",
            "BEI PLAT'TENEPITHELKARZINOM: Dermatologe\nZweithäufigster Hautkrebs\nChronische Sonnenschäden\nFrüherkennung wichtig\nMetastasen selten aber möglich"));

        conditions.add(new MedicalCondition("331", "Colorectal Cancer", "Darmkrebs",
            "colorectal cancer, colon cancer, rectal cancer, blood in stool, bowel cancer, darmkrebs",
            "1. Gastroenterologe\n2. Koloskopie + Biopsie\n3. CT/MRT Staging\n4. OP\n5. Chemotherapie\n6. Strahlentherapie (Rektum)\n7. Nachsorge",
            "OP: Hemikolektomie oder tiefe anteriore Resektion\nChemotherapie: FOLFOX (5-FU, Folinsäure, Oxaliplatin) oder FOLFIRI\nBei Rektumkarzinom: neoadjuvante Radiochemotherapie",
            "BEI DARMKREBS: Gastroenterologe/Onkologe\nKoloskopie ab 50 (oder früher bei familiärer Belastung)\nBlut im Stuhl als Warnsignal\nFrüherkennung durch Koloskopie"));

        conditions.add(new MedicalCondition("332", "Gastric Cancer", "Magenkrebs",
            "gastric cancer, stomach cancer, epigastric pain, weight loss, magenkrebs",
            "1. Gastroenterologe\n2. Gastroskopie + Biopsie\n3. CT/Staging\n4. OP\n5. Chemotherapie\n6. Strahlentherapie\n7. Nachsorge",
            "OP: Totale oder subtotale Gastrektomie\nChemotherapie: FLOT (5-FU, Leukovorin, Oxaliplatin, Docetaxel)\nBei HER2-positiv: Trastuzumab",
            "BEI MAGENKREBS: Gastroenterologe/Onkologe\nHpylori-Infektion als Risikofaktor\nChronische Gastritis\nFrüherkennung bei familiärer Belastung"));

        conditions.add(new MedicalCondition("333", "Pancreatic Cancer", "Bauchspeicheldrüsenkrebs",
            "pancreatic cancer, pancreas cancer, jaundice, weight loss, back pain, bauchspeicheldrüsenkrebs",
            "1. Gastroenterologe\n2. CT/MRT\n3. Biopsie (EUS)\n4. OP wenn resektabel\n5. Chemotherapie\n6. Palliative Therapie\n7. Schmerztherapie",
            "Wenn resektabel: Whipple-OP (pankreatikoduodenale Resektion) oder distale Pankreatektomie\nChemotherapie: FOLFIRINOX oder Gemcitabin + Nab-Paclitaxel\nPalliativ: Stents, Schmerztherapie",
            "BEI BAUCHSPEICHELDRÜSENKREBS: Gastroenterologe/Onkologe\nOft spät diagnostiziert\nIkterus, Gewichtsverlust, Rückenschmerzen\nCA 19-9 als Tumormarker"));

        conditions.add(new MedicalCondition("334", "Liver Cancer", "Leberkrebs",
            "liver cancer, hepatocellular carcinoma, hcc, liver tumor, leberkrebs",
            "1. Hepatologe\n2. Ultraschall + CT/MRT\n3. Biopsie\n4. OP oder Transplantation\n5. TACE\n6. Systemtherapie\n7. Palliativ",
            "Bei resektabel: Leberresektion oder Transplantation (Milan-Kriterien)\nTACE (Transarterielle Chemoembolisation)\nBei fortgeschritten: Sorafenib oder Atezolizumab + Bevacizumab",
            "BEI LEBERKREBS: Hepatologe/Onkologe\nRisikofaktoren: HBV, HCV, Leberzirrhose, Alkohol\nÜberwachung bei Risikopatienten\nFrüherkennung durch Ultraschall"));

        conditions.add(new MedicalCondition("335", "Lung Cancer", "Lungenkrebs",
            "lung cancer, lung tumor, cough, hemoptysis, weight loss, lungentumor",
            "1. Pneumologe\n2. CT\n3. Biopsie (Bronchoskopie, CT-gestützt)\n4. Staging\n5. OP, Chemotherapie, Strahlentherapie\n6. Immuntherapie\n7. Palliativ",
            "NSCLC: OP (wenn Stadium I-II) oder Radiochemotherapie (Stadium III)\nBei metastasiert: Immuntherapie (Pembrolizumab) + Chemotherapie\nSCLC: Chemotherapie + Strahlentherapie",
            "BEI LUNGENKREBS: Pneumologe/Onkologe\nHauptrisiko: Rauchen\nChronischer Husten, Bluthusten, Gewichtsverlust\nLow-Dose-CT für Risikopersonen"));

        conditions.add(new MedicalCondition("336", "Brain Tumor", "Hirntumor",
            "brain tumor, brain cancer, headache, seizures, neurological deficits, gehirntumor",
            "1. Neurologe/Neurochirurg\n2. MRT\n3. Biopsie/OP\n4. Histologie\n5. Strahlentherapie\n6. Chemotherapie\n7. Rehabilitation",
            "OP: Resektion wenn möglich\nStrahlentherapie: Ganzhirn (Hirnmetastasen) oder fokussiert\nChemotherapie: Temozolomid bei Glioblastom\nTargeted Therapy bei bestimmten Mutationen",
            "BEI HIRNTUMOR: Neurologe/Neurochirurg\nMRT mit Kontrastmittel\nKopfschmerzen, Krampfanfälle, neurologische Ausfälle\nFrühzeitige Diagnose wichtig"));

        conditions.add(new MedicalCondition("337", "Thyroid Cancer", "Schilddrüsenkrebs",
            "thyroid cancer, thyroid nodule, neck mass, thyroid carcinoma, schilddrüsenkrebs",
            "1. Endokrinologe\n2. Ultraschall\n3. Feinnadelpunktion\n4. OP\n5. Radiojodtherapie\n6. TSH-suppressive Therapie\n7. Monitoring",
            "OP: Totale Thyreoidektomie + ggf. Lymphknotendissektion\nRadiojodtherapie (I-131) bei papillärem und follikulärem Karzinom\nLevothyroxin TSH-suppressiv",
            "BEI SCHILDDRÜSENKREBS: Endokrinologe/ Chirurg\nSchilddrüsenknoten abklären\nFeinnadelpunktion ( Bethesda-Klassifikation)\nGute Prognose bei papillärem Schilddrüsenkrebs"));

        conditions.add(new MedicalCondition("338", "Bladder Cancer", "Blasenkrebs",
            "bladder cancer, blood in urine, bladder tumor, urinating problems, blasenkrebs",
            "1. Urologe\n2. Urinzytologie\n3. Zystoskopie\n4. TUR-B\n5. Bei muskelinvasiv: Zystektomie\n6. Chemotherapie\n7. BCG-Instillation",
            "Nicht-muskelinvasiv: TUR-B + adjuvante BCG-Instillation oder Chemotherapie\nMuskelinvasiv: Radikale Zystektomie + Chemotherapie oder Radiochemotherapie",
            "BEI BLASENKREBS: Urologe\nRisikofaktoren: Rauchen, aromatische Amine\nBlut im Urin als Warnsignal\nZystoskopie + Biopsie"));

        conditions.add(new MedicalCondition("339", "Kidney Cancer", "Nierenkrebs",
            "kidney cancer, renal cell carcinoma, blood in urine, flank pain, nierenkrebs",
            "1. Urologe\n2. Ultraschall + CT\n3. Biopsie bei Bedarf\n4. OP\n5. Bei metastasiert: Immuntherapie\n6. Target-Therapie\n7. Monitoring",
            "OP: Radikale Nephrektomie oder Teilresektion (Nierenteilentfernung)\nBei metastasiert: Immuntherapie (Nivolumab + Ipilimumab) oder Target-Therapie (Sunitinib, Pazopanib)",
            "BEI NIERENKREBS: Urologe\nRisikofaktoren: Rauchen, Adipositas\nBlut im Urin, Flankenschmerz\nCT + MRT"));

        conditions.add(new MedicalCondition("340", "Lymphoma Non-Hodgkin", "Non-Hodgkin Lymphom",
            "non-hodgkin lymphoma, nhl, swollen lymph nodes, lymphoma, lymphknotenkrebs",
            "1. Hämatologe/Onkologe\n2. Lymphknotenbiopsie\n3. Staging (CT, PET, Knochenmark)\n4. Chemotherapie\n5. Immuntherapie\n6. Strahlentherapie\n7. KMT bei Bedarf",
            "CHOP + Rituximab (R-CHOP) bei B-Zell-Lymphomen\nBei aggressiv: R-CHOP 6 Zyklen\nBei rezidiv: Salvage-Chemotherapie + KMT",
            "BEI NON-HODGKIN LYMPHOM: Hämatologe/Onkologe\nB-Symptome: Fieber, Nachtschweiß, Gewichtsverlust\nStadieneinteilung durch CT/PET\nKinderonkologie konsultieren"));

        conditions.add(new MedicalCondition("341", "Leukemia ALL", "Akute Lymphatische Leukämie",
            "all, acute lymphoblastic leukemia, blood cancer, fatigue, bleeding, leukämie",
            "1. SOFORT zum Hämatologen\n2. Blutbild\n3. Knochenmarkbiopsie\n4. Lumbalpunktion\n5. Chemotherapie\n6. Bestrahlung\n7. KMT",
            "Induktion: Vincristin + Prednison + Asparaginase + Daunorubicin\nKonsolidierung: Methotrexat + 6-Mercaptopurin\nErhaltungstherapie 2-3 Jahre",
            "BEI ALL: Sofort in die Kinderklinik\nHäufigste Krebserkrankung bei Kindern\nBlässe, Müdigkeit, Infektionen, Blutergüsse\nFrühzeitige Therapie entscheidend"));

        conditions.add(new MedicalCondition("342", "Leukemia AML", "Akute Myeloische Leukämie",
            "aml, acute myeloid leukemia, blood cancer, fatigue, infections, leukämie",
            "1. SOFORT zum Hämatologen\n2. Blutbild + Diff\n3. Knochenmarkbiopsie\n4. Zytogenetik\n5. Chemotherapie\n6. KMT\n7. Supportiv",
            "Induktion: Cytarabin + Daunorubicin (7+3)\nBei APL: ATRA + Arsenik\nKonsolidierung: Cytarabin hochdosis\nKMT in CR1 bei High-Risk",
            "BEI AML: Sofort zum Hämatologen\nBei Kindern: Möglich\nMüdigkeit, Infektionen, Blutungen\nZytogenetik bestimmt Prognose"));

        conditions.add(new MedicalCondition("343", "Chronic Myeloid Leukemia", "Chronische Myeloische Leukämie",
            "cml, chronic myeloid leukemia, high white blood cell count, fatigue, leukämie",
            "1. Hämatologe\n2. Blutbild\n3. Knochenmarkbiopsie\n4. BCR-ABL Test\n5. Tyrosinkinasehemmer\n6. Monitoring\n7. KMT bei Resistenz",
            "Tyrosinkinasehemmer: Imatinib 400mg tgl oder Dasatinib 100mg tgl oder Nilotinib 300mg 2x tgl\nBei Versagen: Zweite Generation TKI\nKMT nur bei TKI-Versagen",
            "BEI CML: Hämatologe\nBCR-ABL positiv\nChronische Phase: TKI\nBeschwerdefreiheit oft langjährig\nRegelmäßige Monitoring"));

        conditions.add(new MedicalCondition("344", "Hodgkin Lymphoma", "Hodgkin Lymphom",
            "hodgkin lymphoma, lymph node enlargement, reed-sternberg cells, lymphoma, lymphknotenkrebs",
            "1. Hämatologe/Onkologe\n2. Lymphknotenbiopsie\n3. Staging\n4. Chemotherapie\n5. Strahlentherapie\n6. Monitoring\n7. Bei Rezidiv: salvage",
            "Stadium I-II: ABVD (Adriamycin + Bleomycin + Vinblastin + Dacarbazin) + Bestrahlung\nStadium III-IV: ABVD 6-8 Zyklen\nBei Rezidiv: ICE oder Brentuximab Vedotin",
            "BEI HODGKIN LYMPHOM: Hämatologe/Onkologe\nGutes Ansprechen auf Chemotherapie\nB-Symptome: Fieber, Nachtschweiß, Gewichtsverlust\nHohe Heilungsrate"));

        conditions.add(new MedicalCondition("345", "Multiple Myeloma", "Multiples Myelom",
            "multiple myeloma, plasma cell cancer, bone pain, anemia, hypercalcemia, myeloma",
            "1. Internist/Hämatologe\n2. Protein im Serum/Urin\n3. Knochenmarkbiopsie\n4. Bildgebung\n5. Chemotherapie\n6. KMT\n7. Maintenance",
            "Induktion: Bortezomib + Lenalidomid + Dexamethason (VRd)\nKMT: Hochdosis-Melphalan + autologe KMT\nMaintenance: Lenalidomid\nBei Rezidiv: Daratumumab",
            "BEI MULTIPLEM MYELOM: Hämatologe\nBei Kindern: Sehr selten\nKnochenschmerzen, Anämie, Niereninsuffizienz\nMGUS als Vorstufe\nRegelmäßige Kontrollen"));

        conditions.add(new MedicalCondition("346", "Myelodysplastic Syndrome", "Myelodysplastisches Syndrom",
            "mds, myelodysplastic syndrome, low blood counts, anemia, dsyplasie",
            "1. Hämatologe\n2. Blutbild\n3. Knochenmarkbiopsie\n4. Zytogenetik\n5. Supportiv\n6. Hypomethylierende Agenzien\n7. KMT",
            "Supportiv: Transfusionen, Erythropoetin, G-CSF\nBei niedrig-Risk: Luspatercept oder ESAs\nBei hoch-Risk: Azacitidin oder Decitabin (Hypomethylierende)\nKMT bei geeigneten Patienten",
            "BEI MDS: Hämatologe\nBei Kindern: Selten\nZytopenien, Transfusionsbedarf\nZytogenetik wichtig\nRisiko stratification"));

        conditions.add(new MedicalCondition("347", "Aplastic Anemia", "Aplastische Anämie",
            "aplastic anemia, bone marrow failure, pancytopenia, fatigue, blutarmut",
            "1. Sofort zum Hämatologen\n2. Blutbild\n3. Knochenmarkbiopsie\n4. Supportiv\n5. Immunsuppression\n6. KMT\n7. Monitoring",
            "Bei <40 Jahre mit Spender: Allogene KMT (Sib-MUD)\nBei >40 oder kein Spender: Immunsuppression (Antithymozytenglobulin + Cyclosporin)\nSupportiv: Transfusionen, Antibiotika",
            "BEI APLASTISCHER ANÄMIE: Sofort zum Hämatologen\nPanzytopenie\nKnochenmarkversagen\nImmunsuppression oder KMT"));

        conditions.add(new MedicalCondition("348", "Sickle Cell Disease", "Sichelzellanämie",
            "sickle cell disease, sickle cell anemia, pain crisis, anemia, schmerzkrisen",
            "1. Hämatologe\n2. Hämoglobin-Elektrophorese\n3. Schmerztherapie\n4. Hydroxycarbamid\n5. Transfusionen\n6. Bei Komplikationen: Sofort-OP\n7. KMT als kurative Option",
            "Hydroxycarbamid (Hydroxyurea) 15-35mg/kg tgl\nSchmerztherapie: Opioide bei Vaso-okklusiven Krisen\nBei akutem Thoraxsyndrom: Austauschtransfusion",
            "BEI SICHELZELLANÄMIE: Hämatologe\nBei Kindern: Neonatales Screening\nSchmerzkrisen, akutes Thoraxsyndrom\nHydroxycarbamid reduziert Krisen"));

        conditions.add(new MedicalCondition("349", "Thalassemia", "Thalassämie",
            "thalassemia, microcytic anemia, low hemoglobin, thalassämie",
            "1. Hämatologe\n2. Blutbild + Hämoglobin-Elektrophorese\n3. Transfusionen\n4. Eisenchelation\n5. Folsäure\n6. Bei schwer: KMT\n7. Monitoring",
            "Regelmäßige Transfusionen bei β-Thalassemia major\nEisenchelation: Deferasirox 20-40mg/kg tgl oder Deferoxamin\nFolsäure 1mg tgl",
            "BEI THALASSÄMIE: Hämatologe\nBei Kindern: Neonatales Screening möglich\nRegelmäßige Transfusionen\nEisenchelationstherapie\nKMT als kurative Option"));

        conditions.add(new MedicalCondition("350", "Hemophilia", "Hämophilie",
            "hemophilia, bleeding disorder, easy bruising, joint bleeds, hämophilie",
            "1. Hämatologe\n2. Gerinnungstests (Faktor VIII/IX)\n3. Faktorenersatz\n4. Bei Gelenkblutung: Ruhigstellung + Faktoren\n5. Prophylaxe\n6. Bei Inhibitor: bypassing agents\n7. Physiotherapie",
            "Bei Hämophilie A: Faktor VIII Konzentrat 25-50 IE/kg 2-3x/Woche (Prophylaxe) oder bei Bedarf\nBei Hämophilie B: Faktor IX 25-50 IE/kg 2x/Woche\nBei Inhibitor: rFVIIa oder aPCC",
            "BEI HÄMOPHILIE: Hämatologe\nX-chromosomal-rezessiv (Jungen)\nGelenkblutungen, Muskelblutungen\nFaktorenersatz prophylaktisch\nBei Kindern: Frühzeitige Prophylaxe"));
    }
}
