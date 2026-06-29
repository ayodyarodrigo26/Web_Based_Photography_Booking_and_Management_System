<h1 align="center">📸 Web-Based Photography Booking and Management System</h1>



> A comprehensive web-based platform developed to streamline photography service booking, customer management, payment processing, marketing campaigns, and photographer scheduling through an integrated management system.

<p align="center">

<a href="https://github.com/ayodyarodrigo26/Web_Based_Photography_Booking_and_Management_System">Repository</a> • <a href="#-installation">Installation</a> • <a href="#-license">License</a>

</p>

<p align="center">
  <img src="screenshots/img_12.png" width="100%">
</p>

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen)
![Spring Security](https://img.shields.io/badge/Spring_Security-6-success)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![Hibernate](https://img.shields.io/badge/Hibernate-JPA-brown)
![Thymeleaf](https://img.shields.io/badge/Frontend-Thymeleaf-green)
![Bootstrap](https://img.shields.io/badge/UI-Bootstrap-purple)
![Status](https://img.shields.io/badge/Project-Completed-success)

---
## 📑 Table of Contents

* [📖 Project Overview](#-project-overview)
* [✨ Key Features](#-key-features)
* [🏗️ System Architecture](#-system-architecture)
* [🏛️ Modules](#️-modules)
* [🖼️ System Screenshots](#️-system-screenshots)
* [⭐ Special Business Features](#-special-business-features)
* [🛠️ Technology Stack](#️-technology-stack)
* [📂 Project Structure](#-project-structure)
* [🗄️ Database Tables](#️-database-tables)
* [🚀 Installation](#-installation)
* [👥 Team Members](#-team-members)
* [🎓 Academic Information](#-academic-information)
* [🏆 Project Highlights](#-project-highlights)
* [📜 License](#-license)

---
# 📖 Project Overview

The **Web-Based Photography Booking and Management System** is a full-stack enterprise web application developed to digitalize photography service management.

The system enables customers to browse photography packages, make reservations, apply promotional discounts, complete online payments, and track bookings, while providing administrators with powerful tools to manage users, photographers, packages, promotions, loyalty discounts, payments, and reports.

This project was developed as the **Year 2 Semester 2 Information Systems Project (IE2091)** for the **BSc (Hons) in Information Technology – Information Systems Engineering** degree program at the **Sri Lanka Institute of Information Technology (SLIIT)**.

---

# ✨ Key Features

## 👤 User Management

* Customer Registration & Login
* Photographer Management
* Administrator Dashboard
* Role-Based Authentication
* User Profile Management
* Account Activation / Deactivation
* Secure Password Encryption

---

## 📦 Photography Package Management

* Photography Category Management
* Package Management
* Package Filtering
* Shopping Cart
* Add-on Selection
* Cart Summary
* Automatic Price Calculation

---

## 📅 Booking Management

* Online Reservation System
* Photographer Assignment
* Photographer Availability Validation
* Double Booking Prevention
* Booking Approval & Rejection
* Booking Tracking
* Booking Calendar

---

## 💳 Payment Management

* Full Payment
* Advance Payment
* Remaining Balance Payment
* PDF Receipt Generation
* Payment History
* Revenue Reports
* Payment Status Tracking

---

## 🎯 Marketing Management

* Promotion Management
* Coupon Management
* Loyalty Discount System
* Promotion Countdown
* Automatic Discount Calculation
* Promotional Image Upload

---

## ⭐ Loyalty Discount Feature

One of the unique business features of this project.

Customers who complete **three or more paid bookings** are automatically recognized as **Loyal Customers**.

The system:

* Detects loyalty automatically
* Applies configurable loyalty discounts
* Combines loyalty discounts with promotions and coupons
* Calculates the final payable amount automatically
* Allows administrators to modify loyalty percentages without changing the source code

---

## 📝 Feedback Management

* Customer Feedback Submission
* Rating System
* Feedback Moderation
* Feedback Display

---

## 🔒 Security Features

* Spring Security Authentication
* Role-Based Authorization
* Password Encryption (BCrypt)
* CSRF Protection
* Session Management
* Input Validation
* Secure Route Protection

---

# 🏗 System Architecture

```
Presentation Layer
│
├── Thymeleaf Templates
├── Bootstrap UI
│
Controller Layer
│
├── User Controller
├── Booking Controller
├── Package Controller
├── Payment Controller
├── Marketing Controller
│
Service Layer
│
├── Business Logic
├── Validation
├── Discount Calculations
│
Repository Layer
│
├── Spring Data JPA
│
Database
│
└── MySQL
```

---

# 🏛 Modules

* User Management
* Photography Package Management
* Booking Management
* Payment Management
* Marketing Management
* Feedback Management

---
# 🖼️ System Screenshots

## Home Page

<p align="center">
  <img src="screenshots/img.png" width="100%">
</p>

---
## Customer Registration

<p align="center">
  <img src="screenshots/img_1.png" width="100%">
</p>

<p align="center">
  <img src="screenshots/img_2.png" width="100%">
</p>

---

## Photography Packages

<p align="center">
  <img src="screenshots/img_4.png" width="100%">
</p>

---

## Shopping Cart

<p align="center">
  <img src="screenshots/img_5.png" width="100%">
</p>

---

## Marketing Management

<p align="center">
  <img src="screenshots/img_6.png" width="100%">
</p>

<p align="center">
  <img src="screenshots/img_7.png" width="100%">
</p>

---

## Booking Management

<p align="center">
  <img src="screenshots/img_9.png" width="100%">
</p>

---

## Payment Management

<p align="center">
  <img src="screenshots/img_10.png" width="100%">
</p>

---
# ⭐ Special Business Features

## Loyalty Discount System

Automatically rewards returning customers by detecting completed bookings and applying configurable loyalty discounts.

---

## Promotion Engine

* Category-based promotions
* Countdown campaigns
* Automatic promotion calculation

---

## Coupon System

* Percentage Discounts
* Fixed Amount Discounts
* Coupon Validation
* Coupon Expiration
* Active/Inactive Coupons

---

## Photographer Availability Validation

Automatically prevents:

* Double bookings
* Schedule conflicts
* Invalid photographer assignments

---

# 🛠 Technology Stack

| Technology      | Description                    |
| --------------- | ------------------------------ |
| Java 17         | Programming Language           |
| Spring Boot     | Backend Framework              |
| Spring Security | Authentication & Authorization |
| Hibernate / JPA | ORM Framework                  |
| Thymeleaf       | Server-side Templating         |
| Bootstrap 5     | Frontend UI                    |
| HTML5           | Frontend                       |
| CSS3            | Styling                        |
| JavaScript      | Client-side Interactions       |
| MySQL           | Database                       |
| Maven           | Dependency Management          |
| IntelliJ IDEA   | Development Environment        |

---

# 📂 Project Structure

```
src
├── booking_management
├── feedback_management
├── marketing_management
│   ├── coupon
│   ├── promotion
│   └── discounts
├── package_management
├── payment_management
├── user_management
├── config
├── security
├── repository
├── service
└── templates
```

---

# 🗄 Database Tables

### User Management

* users
* roles

### Photography Package Management

* category
* package
* cart
* add_on

### Booking Management

* booking
* booking_summary
* photographer_assignment

### Marketing Management

* promotion
* coupon
* loyalty_discount

### Payment Management

* payment

### Feedback Management

* feedback

---

# 🚀 Installation

### Clone the repository

```bash
git clone https://github.com/ayodyarodrigo26/Web_Based_Photography_Booking_and_Management_System.git
```

### Navigate to the project

```bash
cd Web_Based_Photography_Booking_and_Management_System
```

### Configure the database

Update the database configuration in:

```text
src/main/resources/application.properties
```

### Build and run

```bash
mvn clean install
mvn spring-boot:run
```

### Open in your browser

```text
http://localhost:8080
```


---

# 👥 Team Members

| Student ID        | Name               |
|-------------------|--------------------|
| IT24103653  (Me)  | Rodrigo H.A.D.A    |
| IT24100636        | Fernando T.M.I.U   |
| IT24100151        | Ruth R.W.N         |
| IT24101144        | Jayaweera S.R.S.H  |
| IT24101905        | Chamya J.A         |

---

# 🎓 Academic Information

**Module:** Information Systems Project (IE2091)

**Institution:** Sri Lanka Institute of Information Technology (SLIIT)

**Academic Year:** Year 2 Semester 2 (2026)

---

# 🏆 Project Highlights

* ✅ Complete Enterprise Web Application
* ✅ Six Fully Integrated Modules
* ✅ Spring Security Authentication
* ✅ Role-Based Authorization
* ✅ Loyalty Discount System
* ✅ Promotion & Coupon Engine
* ✅ Photographer Availability Validation
* ✅ Secure Payment Management
* ✅ Responsive User Interface
* ✅ MVC Architecture
* ✅ Hibernate/JPA Integration
* ✅ MySQL Database
* ✅ Real-world Business Workflow

---
## 📜 License

This project is licensed under the **MIT License**.

See the [LICENSE](LICENSE) file for more information.

---

<p align="center">

Developed using Java, Spring Boot, Thymeleaf, and MySQL.

© 2026 Ayodya Rodrigo & Team

</p>



