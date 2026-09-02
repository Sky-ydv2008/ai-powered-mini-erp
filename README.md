# IntelliERP — AI-Powered Mini ERP & Decision Intelligence Platform

> **"Track your business. Understand your data. Predict problems. Make better decisions."**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Security](https://img.shields.io/badge/Security-JWT%20Bearer-blue.svg)](https://spring.io/projects/spring-security)
[![CI/CD](https://github.com/Sky-ydv2008/bharat/actions/workflows/ci.yml/badge.svg)](https://github.com/Sky-ydv2008/bharat/actions/workflows/ci.yml)
[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/Sky-ydv2008/bharat)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 🚀 Overview

**IntelliERP** is a full-stack, enterprise-grade Mini ERP and Decision Intelligence platform engineered for small and medium-sized enterprises.

Beyond standard CRUD operations, IntelliERP incorporates an **Explainable AI (XAI) & Anomaly Diagnostic Engine** that continuously analyzes sales ledger streams, inventory stock movements, supplier fulfillment times, and operational expenses to detect anomalies, quantify financial impact in ₹, diagnose root causes, and prescribe actionable decisions.

---

## ✨ Key Features

- **Executive Command Center**: Live KPIs, Today's Business Summary, and a composite 0–100 **Business Health Score** with 6 pillars (Profitability, Inventory, Sales, Supplier, Customer, Expenses).
- **Point of Sale (POS) Terminal**: Real-time barcode/SKU checkout, stock deduction, GST tax calculation, margin tracking, and printable tax invoices.
- **Explainable AI (XAI) Decision Engine**:
  - **Statistical Anomaly Detection**: 30-day baseline deviations ($Z > 2.0$) detecting sales drops and expense surges.
  - **Supplier Loss Detector**: Quantifies financial drag in ₹ from vendor delays, defect rates, and returns.
  - **Stockout & Lead-Time Predictor**: Predicts depletion dates based on velocity vs supplier lead times.
  - **BCG Product Matrix**: Classifies products into `STAR`, `CASH_COW`, `QUESTION_MARK`, `LOSS_MAKER`, and `DEAD_STOCK`.
- **"Ask Your Business Data" Natural Language Assistant**: Conversational AI business advisor providing structured diagnostic fact cards and recommendations.
- **Procurement & Inventory Ledger**: Complete immutable audit trail tracking all ledger transactions (`PURCHASE`, `SALE`, `RETURN`, `DAMAGE`, `ADJUSTMENT`, `TRANSFER`).
- **Profit & Loss Center**: Dynamic horizons (Today, This Week, This Month, This Year, Custom) with one-click CSV export generators.
- **Enterprise Security**: Spring Security 6 with stateless JJWT Bearer authentication and Role-Based Access Control (`ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_EMPLOYEE`).

---

## 🛠️ Technology Stack

- **Backend**: Java 21, Spring Boot 3.3.4, Spring Data JPA, Hibernate, Spring Security, JJWT 0.12.6, Springdoc OpenAPI 2.6.0 (Swagger 3).
- **Database**: H2 in MySQL Compatibility Mode (`./data/intellierp_db`) for zero-configuration, instant portability.
- **Frontend**: HTML5, Modern CSS3 (Glassmorphism design tokens), Vanilla JavaScript, Chart.js.

---

## ⚡ Quick Start & Installation

### 1. Prerequisites
- Java 21 (JDK 21)
- Apache Maven 3.9+

### 2. Clone the Repository
```bash
git clone <your-repo-url>
cd bharat2.0
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

The application will start on **http://localhost:3000** with pre-seeded realistic data and intentional benchmark anomalies.

---

## 🌐 Access Points & Documentation

| Portal | URL | Description |
| :--- | :--- | :--- |
| **Web Application** | `http://localhost:3000/login.html` | IntelliERP Web Application |
| **Executive Dashboard** | `http://localhost:3000/dashboard.html` | Real-time Business Command Center |
| **Interactive OpenAPI Docs** | `http://localhost:3000/swagger-ui.html` | Swagger UI with live API testing |
| **H2 Database Console** | `http://localhost:3000/h2-console` | Direct database inspection |

---

## 👥 Demo User Accounts

| Role | Username | Password | Access Rights |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Full System Control, Reseeder, Audit Logs |
| **Manager** | `manager` | `manager123` | Procurement, Suppliers, Expenses, Reports, Analytics |
| **Employee** | `employee` | `employee123` | POS Checkout, Catalog, Inventory |

---

## 🧪 Automated Testing

To run the comprehensive integration and AI engine test suite:
```bash
mvn clean test
```
All 9 automated integration tests covering authentication, stock deduction, profit/loss math, supplier loss calculations, Explainable AI insights, and the natural language assistant run out of the box.

---

## 📄 License
This project is licensed under the MIT License.
