# 💰 Expense Manager App

<p align="center">
  <b>Ứng dụng quản lý thu chi cá nhân trên Android</b><br>
  Demo đơn giản • SQLite (Room) • MVVM • Clean Architecture • Feature-based team workflow
</p>

---

# 📌 Giới thiệu

**Expense Manager App** là ứng dụng Android giúp người dùng:

* Quản lý thu chi cá nhân.
* Theo dõi số dư ví và Lập ngân sách (Budget).
* Thống kê tài chính nâng cao (Pie Chart, Lịch giao dịch).
* Bảo mật riêng tư (Mã PIN, Sinh trắc học).

👉 Dự án được thiết kế theo hướng **demo học tập** và vừa trải qua đợt tái cấu trúc (Refactor & Audit) chuẩn hóa 100% Kiến trúc và Tối ưu Memory Leak.

* Kiến trúc Clean Architecture chuẩn mực.
* Mỗi thành viên chịu trách nhiệm **1 feature end-to-end**.
* Rành mạch UI/Logic, dễ demo & chấm điểm.

---

# 🏗️ Kiến trúc sử dụng

> **MVVM (Model - View - ViewModel)** + **SQLite (Room)**

## 🔄 Data Flow

```
UI (View) ↔ StateFlow/LiveData ↔ ViewModel ↔ Repository ↔ Room DAO (SQLite)
```

---

# 📂 Cấu trúc thư mục

```bash
app/
├── data/
│   ├── entity/       # Các bảng Database (Transaction, Wallet, Category)
│   ├── local/        # DAO interface tương tác Room SQLite
│   ├── Repository    # Trung gian giao tiếp Database
│   └── Constants.kt  # Hằng số tập trung trị Magic String
├── presentation/
│   └── viewmodel/    # Nơi chứa toàn bộ ViewModel xử lý logic tách biệt với UI
├── ui/               # Layout, Activity, Fragment, Dialog, Compose Components
└── utils/            # FormatUtils (Tiền tệ, Ngày tháng) dùng chung
```

---

# 💾 Database (SQLite - Room)

Ứng dụng sử dụng **Room Database** v2 với khả năng tối ưu hóa truy vấn qua **Database Indexes**.
Bao gồm 3 bảng chính:
* **transactions**: Quản lý lịch sử giao dịch (Đã Index: `date`, `type`, `walletName`).
* **wallets**: Quản lý ví (Đã Index: `name`).
* **categories**: Danh mục giao dịch.

---

# 🚀 Tính năng nổi bật

### 💸 Transaction & Wallet
* Thêm / sửa / xóa giao dịch, tự động trích/cộng trừ số dư trực tiếp trên Ví.
* Cảnh báo an toàn dữ liệu: Ngăn xóa ví nếu còn giao dịch ràng buộc.

### 📊 Thống kê & Dashboard
* **Pie Chart**: Biểu đồ hình tròn thời gian thực của tháng hiện tại.
* **Smart Budget**: Thiết lập và giám sát ngân sách. Thanh ProgressBar cảnh báo tự động chuyển màu Cam(80%) và Đỏ(100%) khi lố hạn mức.
* Lịch giao dịch trực quan theo từng ngày.

### 🔍 Search & Advanced Filter
* Công cụ Filter nổi theo Ngày, Tên danh mục, Số tiền, Loại giao dịch (Thu/Chi).
* Tìm kiếm Native realtime theo ghi chú.

### 🔐 Security & Bảo mật
* Database vô hiệu hóa rủi ro lộ lọt qua Android Backup (`allowBackup="false"`).
* Hệ thống Khóa bảo vệ vòng ngoài bằng **Mã PIN 4 số** (Khóa 30 giây khi sai 5 lần) & **Sinh trắc học vân tay**.
* Cấu hình an toàn bằng EncryptedSharedPreferences (AES-256).

### 💅 Khác
* Dọn dẹp Code rác, dùng Helper Format tiền tệ tái sử dụng.
* Sẵn sàng tùy biến Đóng/Mở Chế Độ Tối (Dark Mode).

---

# 👨‍💻 Phân công (Feature-based)

## 🔹 Nguyên tắc
* Mỗi người làm **1 feature hoàn chỉnh (UI + ViewModel + Logic)**.

### 👤 Nghĩa – Core Data + Wallet
* Setup Room Database (Entity, DAO, Repository).
* CRUD tính năng quản lý Wallet. 
* 🛠️ *Refactor gần nhất: Phủ Null-Safety cho getWallet, di dời dứt điểm Entity sai thư mục, fix query DB bị duplicate.*

### 👤 Anh – Transaction
* Code danh sách RecyclerView giao dịch.
* Module Form Nhập/Sửa.
* 🛠️ *Lưu ý (Tech Debt): Đang xử lý Memory Leak tại khu vực Lọc dữ liệu SearchView bị gắn Event Observer chồng chéo.*

### 👤 Đức – UI + Dashboard
* Chịu trách nhiệm UX giao diện Dashboard (Biểu đồ).
* 🛠️ *Refactor gần nhất: Loại bỏ filter tính toán chậm chạp tại onResume, thay bằng cơ chế Memory Cache.*

### 👤 Thịnh – Report
* Xây dựng báo cáo Summary Report theo tháng.
* 🛠️ *Lưu ý (Tech Debt): Đang xử lý lỗi Space Complexity (RAM) do gọi list thủ công từ DB lên nhồi vào List thay vì lệnh SQL Group By.*

### 👤 Đạt – Search, Integration & Utilities
* Filter bộ lọc, Search bar, Điều hướng Navigation.
* 🛠️ *Refactor gần nhất: Hệ thống hóa Class tính toán tiền tệ `FormatUtils`, bứng logic % Ngân Sách khỏi Activity vào ViewModel.*

---

# 🔄 Quy trình phát triển (Tiến độ)

## Phase 1 -> 3
* Set up, Chia tính năng End-to-end, Tích hợp chung và fix Bug sơ bộ.

## Pha 4: Audit & Cải tiến Kiến trúc
* **Code Quality**: Áp dụng triệt để Clean Architecture, xóa 100% Magic Strings.
* **Performance**: Index database tăng tốc tìm kiếm ví và ngày giao dịch, áp caching chống tính toán UI.
* **Security**: Triển khai Biometric, AppPrefs Encrypted, App Backup Guard, Chống Brute-force PIN.
* **Đạt mức hoàn thiện dự án MỚI NHẤT: 90% Readiness.**

---

# 🌿 Git Workflow (Main Only)

Đội ngũ đang áp dụng phương pháp Single-branch Workflow.

## 🔁 Quy trình làm việc
```bash
# 1. Luôn pull mới nhất để tránh conflict
git pull origin main

# 2. Add và Commit theo từng mẻ tính năng cụ thể
git add .
git commit -m "feat/fix/perf/refactor: Mô tả rõ ràng nội dung"

# 3. Đẩy lên server
git push origin main
```

---

# 📄 License
MIT License

# ❤️ Ghi chú
Dự án phục vụ mục đích học tập chuyên sâu:
* Android (Kotlin)
* Cơ chế Data Caching, MVVM, LiveData/StateFlow
* SQLite Indexing (Room)
* Teamwork Git
* Tái cấu trúc chuẩn Clean Architecture (Dependency Separation)
