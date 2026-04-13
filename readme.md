# 💰 Expense Manager App

<p align="center">
  <b>Ứng dụng quản lý thu chi cá nhân trên Android</b><br>
  Demo đơn giản • SQLite (Room) • MVVM • Feature-based team workflow
</p>

---

# 📌 Giới thiệu

**Expense Manager App** là ứng dụng Android giúp người dùng:

- Quản lý thu chi cá nhân
- Theo dõi số dư ví
- Thống kê tài chính cơ bản

👉 Dự án được thiết kế theo hướng **demo học tập**:
- Kiến trúc rõ ràng
- Mỗi thành viên làm **1 feature end-to-end**
- Dễ demo & dễ chấm điểm

---

# 🏗️ Kiến trúc sử dụng

> **MVVM (Model - View - ViewModel)** + **SQLite (Room)**

## 🔄 Data Flow

```
UI → ViewModel → Repository → Room (SQLite)
```

---

# 📂 Cấu trúc thư mục

```bash
app/
├── data/
│   ├── local/        # Room Database, DAO
│   └── repository/   # Repository
│
├── model/            # Entity
├── ui/               # Activity / Fragment
├── viewmodel/        # ViewModel
└── utils/
```

---

# 💾 Database (SQLite - Room)

Ứng dụng sử dụng **Room Database** để lưu trữ dữ liệu cục bộ.

## 📌 Các bảng chính

- **transactions**: lưu giao dịch
- **wallets**: quản lý ví
- **categories**: phân loại

---

# 🚀 Tính năng chính

### 💸 Transaction
- Thêm / sửa / xóa giao dịch
- Validate dữ liệu

### 👛 Wallet
- Quản lý ví
- Tính toán số dư

### 📊 Dashboard
- Tổng thu / chi
- Hiển thị nhanh

### 📈 Report
- Thống kê theo thời gian
- Biểu đồ

### 🔍 Search & Filter
- Tìm theo note
- Lọc theo ngày / danh mục

---

# 👨‍💻 Phân công (Feature-based)

## 🔹 Nguyên tắc
- Mỗi người làm **1 feature hoàn chỉnh (UI + ViewModel + Logic)**
- Không chia theo layer

---

### 👤 Nghĩa – Core Data + Wallet
- Setup Room (Entity, DAO)
- Repository
- Feature: Wallet (CRUD + balance)

### 👤 Anh – Transaction
- Danh sách giao dịch
- Thêm / sửa / xóa
- Validate input

### 👤 Đức – UI + Dashboard
- Thiết kế UI
- Dashboard (tổng thu/chi)

### 👤 Thịnh – Report
- Thống kê
- Biểu đồ
- Filter theo thời gian

### 👤 Đạt – Search + Integration
- Search & Filter
- Navigation
- Integration toàn app
- Test + Fix bug

---

# 📱 Danh sách màn hình

| Screen | Owner |
|--------|------|
| Wallet | Nghĩa |
| Transaction | Anh |
| Dashboard | Đức |
| Report | Thịnh |
| Search/Filter | Đạt |

---

# 🔄 Quy trình phát triển

## Phase 1
- Setup database
- UI mock
- Fake data

## Phase 2
- Mỗi người code feature riêng

## Phase 3
- Integration (navigation + data flow)
- Fix bug

---

# 🌿 Git Workflow (Main Only)

## 📌 Nguyên tắc

- Sử dụng **duy nhất 1 branch: `main`**
- Tất cả thành viên làm việc trực tiếp trên `main`

---

## 🔁 Quy trình làm việc

```bash
# 1. Luôn pull trước khi code
git pull origin main

# 2. Code

# 3. Commit
git add .
git commit -m "feat: feature name"

# 4. Push
git push origin main
```

---

## ⚠️ Quy tắc


### ❌ Không được
- Push khi chưa pull
- Sửa code người khác tùy ý
- Code chồng feature

### ✅ Bắt buộc
- Mỗi người 1 feature
- Test trước khi push
- Commit rõ ràng

---

# 🎯 Kết quả đạt được

- 5/5 thành viên đều code
- Mỗi người có feature riêng
- App chạy hoàn chỉnh (end-to-end)

---

# 📄 License

MIT License

---

# ❤️ Ghi chú

Dự án phục vụ mục đích học tập:

- Android (Kotlin)
- MVVM
- SQLite (Room)
- Teamwork Git

---

⭐ Star nếu thấy hữu ích!

