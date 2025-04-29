# 🏗️ CarportKonfigurator – Full Stack Webapplikation (Eksamensprojekt)

## 📌 Projektbeskrivelse

**CarportKonfigurator** er en full-stack webapplikation udviklet i Java med [Javalin](https://javalin.io/) og [Thymeleaf](https://www.thymeleaf.org/), hvor kunder kan konfigurere en carport med egne ønsker og sende en ordre.

Admin kan efterfølgende tilføje de nødvendige mål, hvorefter systemet automatisk genererer en **Bill of Material** (stykliste), udregner prisen ud fra en materialedatabase og sender et tilbud til kunden via e-mail (SendGrid).

Dette projekt er udviklet som vores eksamensprojekt på Copenhagen business.

---

## ⚙️ Funktionalitet

### 🔹 Kunde-side:
- Udfyld formular med:
  - Ønsker til trapeztag og redskabsrum
  - Kontaktoplysninger
- Indsend carport-forespørgsel

### 🔹 Admin-panel:
- Vis alle indsendte ordrer
- Indtast præcise mål (længde, bredde, skur)
- Systemet:
  - Genererer **stykliste** (Bill of Material)
  - Udregner **pris**
  - Sender **tilbud via SendGrid**

### 🧾 Bill of Material inkluderer:
- Materialenavn
- Længde
- Antal
- Enhed
- Anvendelse
