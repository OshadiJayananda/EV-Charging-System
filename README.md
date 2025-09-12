# EV Charging Station Booking System

## Table of Contents

1. Project Overview
2. Features
3. System Architecture
4. Technology Stack
5. Database Design
6. Installation & Setup
7. Usage
8. References

## Project Overview

This project is an **end-to-end EV Charging Station Booking System** designed for **Sri Lanka**.
It provides both a **web application** for backoffice users and station operators, and a **mobile application** for EV owners.

The system uses a **client-server architecture** with a centralized web service that implements all business logic and interacts with a **NoSQL database**.

## Features

### Web Application (Backoffice & Station Operator)

- User Management
- Activate/Deactivate EV Owner accounts
- Charging Station Management
- Booking Management (via Station Operator)

### Mobile Application (EV Owner & Station Operator)

- EV Owner:
  - Create, update, and deactivate accounts
  - Book, modify, and cancel charging slots
  - View booking history and upcoming reservations
  - Access booking QR codes
  - View nearby charging stations on a map (Google Maps API)
  - Request account reactivation (via Admin)
- Station Operator:
  - Confirm bookings via QR code
  - Update slot availability
  - Finalize charging sessions

## System Architecture

[Web/Mobile Client] <---> [Web Service (C# Web API)] <---> [NoSQL Database]

## Technology Stack

- Web: React.js, Tailwind CSS / Bootstrap 5
- Mobile: Pure Android (Java/Kotlin), SQLite
- Backend: C# Web API (.NET Framework)
- Database: NoSQL (MongoDB or equivalent)
- Server: Windows IIS
- Map Integration: Google Maps API

## Database Design

### Database Schema Diagram

**Database Design Tool:** https://dbdiagram.io/d/EV-Charging-Station-Booking-System-68c3a356841b2935a62b7584

![EV Charging Station Booking System Database Schema](EV%20Charging%20Station%20Booking%20System.svg)

### Users

- Fields: \_id, nic, name, email, password_hash, role, status, created_at, updated_at

### Stations

- Fields: \_id, name, location, type, slots, status, operator_id

### Bookings

- Fields: \_id, owner_id, station_id, slot_id, reservation_time, status, qr_code

## Installation & Setup

1. Clone the repository
2. Setup Backend: configure MongoDB and run C# Web API on IIS
3. Setup Web App: npm install & npm start
4. Setup Mobile App: import into Android Studio and run

## Usage

- Backoffice: manage users, EV owners, and charging stations
- EV Owners: register, book slots, view history, access QR code, see nearby stations
- Station Operators: manage slots, confirm bookings, finalize sessions

## References

- MongoDB Documentation: https://www.mongodb.com/docs/
- React.js Documentation: https://reactjs.org/docs/getting-started.html
- Android Developer Guide: https://developer.android.com/guide
- Google Maps API: https://developers.google.com/maps/documentation
