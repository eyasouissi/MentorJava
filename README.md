<p align="center">
  <img alt="symfony logo" src="https://symfony.com/images/logos/header-logo.svg" width="300">
</p>

<h1 align="center">Mentor - Symfony Application</h1>

# 🎓 Mentor - Educational Platform

Welcome to the GitHub repository of **Mentor**, an interactive educational platform passionately created by the **Brainiacs** team 🧠.

---

## 📚 Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Technologies](#technologies)
- [Team](#team-brainiacs)
- [Project Structure](#project-structure)
- [Contributions](#contributions)
- [License](#license)

---

## Project Overview

> **Mentor** is a JavaFX application dedicated to interactive learning. It includes several powerful modules ranging from course management to the creation of collaborative study rooms with modern tools like artificial intelligence and facial recognition.

---

## GitHub Repository Link

🔗 [https://github.com/eyasouissi/MentorJava.git](https://github.com/eyasouissi/MentorJava.git)

---

## Technologies

![PHP](https://img.shields.io/badge/PHP-777BB4?style=for-the-badge&logo=php&logoColor=white)
![Symfony](https://img.shields.io/badge/Symfony-000000?style=for-the-badge&logo=symfony&logoColor=white)
![Composer](https://img.shields.io/badge/Composer-885630?style=for-the-badge&logo=composer&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Doctrine](https://img.shields.io/badge/Doctrine-FF6C37?style=for-the-badge)
![Twig](https://img.shields.io/badge/Twig-009E5B?style=for-the-badge&logo=twig&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![Bootstrap](https://img.shields.io/badge/Bootstrap-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)
![GitHub](https://img.shields.io/badge/Hosted_on-GitHub-181717?style=for-the-badge&logo=github)


---

## Team Brainiacs

| Name               | Role             |
|--------------------|------------------|
| 👩‍💻 Eya Souissi     | Developer         |
| 👩‍💻 Islem Belhadj   | Developer         |
| 👩‍💻 Maram Abidi     | Developer         |
| 👩‍💻 Emna Naffeti    | Developer         |
| 👩‍💻 Sarra Amami     | Developer         |
| 👨‍💻 Yassine Hammouda | Developer         |

---

## Project Structure

```bash
MentorJava/
├── src/
│   ├── controllers/       # JavaFX Controllers
│   ├── models/            # Domain Models
│   ├── Entities/          # Entity Views
│   ├── services/          # Services
│   ├── tests/             # Test Classes
│   ├── tools/             # Tools           
│   └── utils/             # Helpers and Utilities
├── resources/
│   ├── assets/            # Assets
│   ├── css/               # Stylesheets
│   ├── fonts/             # Fonts
│   ├── interfaces/        # FXML Interfaces 
│   ├── images/            # Logos, Icons
│   └── styles/            # CSS Files
├── Main.java              # JavaFX Entry Point
└── README.md              # Documentation

```

---

## Installation
Read the steps to install and run this Symfony project in your region:

### 1. Clone the repository
git clone https://github.com/eyasouissi/MentorSymf.git
cd my_project

### 2. Install PHP dependencies
composer install

### 3. Create and configure `.env.local`
cp .env .env.local
# Edit the .env.local file:
# DATABASE_URL="mysql://username:password@127.0.0.1:3306/mentordb"
# Replace `username`, `password`, and `mentordb` with your actual DB credentials

### 4. Create the database
php bin/console doctrine:database:create

### 5. Generate the database schema
php bin/console doctrine:schema:update --force

### 6. Load test data (fixtures)
php bin/console doctrine:fixtures:load

### 7. Start the server
# Option 1 (Symfony CLI):
symfony serve
# Then visit: https://localhost:8000

# Option 2 (PHP built-in server):
php -S localhost:8000 -t public/

## Prerequisites & Tools

# Recommended Editor: VS Code
# Download: https://code.visualstudio.com/

# PHP (v8.0+ required)
# Windows: Install from https://www.php.net/downloads.php or via XAMPP
# macOS:
brew install php
# Linux (Ubuntu/Debian):
sudo apt update
sudo apt install php php-cli php-mbstring php-xml php-curl php-zip php-mysql

# Local Server: XAMPP
# Windows/macOS/Linux: https://www.apachefriends.org/

# Composer (Dependency manager)
# Windows: https://getcomposer.org/
# macOS/Linux:
curl -sS https://getcomposer.org/installer | php
sudo mv composer.phar /usr/local/bin/composer

# MySQL Database
# Included with XAMPP
# macOS:
brew install mysql
# Linux (Ubuntu/Debian):
sudo apt update
sudo apt install mysql-server

## Required APIs & Bundles

# 1. Google API Client
composer require google/apiclient
composer require google-gemini-php/symfony

# 2. Google Cloud Vision
composer require google/cloud-vision

# 3. Google Gemini (again)
composer require google-gemini-php/symfony

# 4. Stripe (payments)
composer require stripe/stripe-php

# 5. Twilio (messaging & communication)
composer require twilio/sdk

# 6. Doctrine ORM
composer require doctrine/orm

# 7. Doctrine Bundle
composer require doctrine/doctrine-bundle

# 8. Doctrine Migrations Bundle
composer require doctrine/doctrine-migrations-bundle

# 9. Symfony Security Bundle
composer require symfony/security-bundle

# 10. Lexik JWT Authentication
composer require lexik/jwt-authentication-bundle

# 11. Symfony Form
composer require symfony/form

# 12. Symfony Validator
composer require symfony/validator

# 13. ReCaptcha Bundle
composer require victor-prdh/recaptcha-bundle

# 14. VichUploaderBundle (file uploads)
composer require vich/uploader-bundle

# 15. Omnipay Stripe (advanced Stripe integration)
composer require omnipay/stripe

# 16. KnpSnappyBundle (PDF/HTML to image generator)
composer require knplabs/knp-snappy-bundle

---


## Usage

- 👤 **Advanced Authentication:** :Log in via facial recognition, Captcha, and password.
- 🖼️ **Personal Profile:** : Add an AI-generated avatar and view your achievements.
- 📘 **Smooth Navigation** : Browse available courses, organized by locked levels.
- 🧠 **Smart Creation** :Create courses using the AI generator and share them with a QR Code.
- 📬 **Automatic Notifications** : New courses are notified to the admin in real time via EMAIL.
- 💬 **Interactive Forum** : Communicate in a forum enriched with emojis, automatic moderation, and media content.
- 🏠 **Collaborative Study Rooms** : Join or create a study room (public or private) with:
  - an AI assistant,
  - an interactive whiteboard,
  - a to-do list,
  - a timer,
  - and relaxing background sounds.
- 💳 **Premium Subscription** : Unlock more content via a secure payment system with **Stripe**.
- 📅 **Educational Events** : View upcoming events, scan the QR code, and add them to your calendar.
- 👥 ** Project Management** :Organize your projects with integrated video conferencing and team calendars.


---


---

## Contributions

We sincerely thank everyone who contributed to this project!

### Contributors

The following people have contributed by adding key features, fixing bugs, and enhancing the user experience:

- **[Maram Abidi](https://github.com/lilaine777)**: Developed the user module, including profile management, advanced authentication, and personalization.
- **[Eya Souissi](https://github.com/eyasouissi)**: Fully implemented courses and categories management, with a progression system based on levels and locked files.
- **[Islem Belhadj](https://github.com/MissFlawless)**: Created and integrated the interactive forum and collaborative study rooms with built-in tools (AI, to-do list, timer...).
- **[Emna Naffeti](https://github.com/EmNaNaF)**: Implemented the premium subscription system and secure payments via Stripe.
- **[Sarra Amami](https://github.com/SAmami3)**: Designed the event management and announcements modules with calendar and QR code integration.
- **[Yassine Hammouda](https://github.com/yassine-hammouda)**: Developed the project and group management features, including video conferencing and collaboration tools.

---

💡 **Want to contribute? Here's how:**

1. Fork the project  
2. Create a branch `feature/your-feature`  
3. Make your changes  
4. Commit & push  
5. Open a Pull Request 🚀

---
---

## Acknowledgements

I would like to sincerely thank my professors [Mrs. Sana Fayechi](mailto:sana.fayechi@esprit.tn) and [Mr. Mohamed Ali Bellili](mailto:mohammedali.bellili@esprit.tn) for their guidance, availability, and valuable advice throughout this project.

Thank you for your support, dedication, and encouragement!

This project was carried out as part of my training at **ESPRIT – Private Higher School of Engineering and Technology**.



## License

Project under the **MIT** license.

```
MIT License

Copyright (c) 2025

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...

```

---

> 🏁 This project was developed as part of our training at **ESPRIT**.
