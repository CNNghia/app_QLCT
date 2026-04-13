###### \# 📂 HƯỚNG DẪN QUẢN LÝ NHÁNH \& COMMIT (TERMINAL)

###### 

###### \## 🌿 1. Danh sách các nhánh (Branches)

###### 

###### Dựa trên bảng phân công, mỗi thành viên \*\*chỉ làm việc trên nhánh cá nhân của mình\*\*:

###### 

###### | Thành viên | Tên nhánh                 | Nhiệm vụ chính                                              |

###### | ---------- | ------------------------- | ----------------------------------------------------------- |

###### | Nghĩa      | `feature/nghia-wallet`    | Setup Room Database, Entity, DAO, Repository, Màn hình Ví   |

###### | Anh        | `feature/anh-transaction` | Màn hình Giao dịch, Validate input, Logic lưu dữ liệu       |

###### | Đức        | `feature/duc-dashboard`   | UI Home, Dashboard, Component Card thống kê                 |

###### | Thịnh      | `feature/thinh-report`    | Màn hình thống kê, Biểu đồ Pie/Bar, Logic Filter ngày/tháng |

###### | Đạt        | `feature/dat-integration` | Tìm kiếm, Filter, Navigation, Kết nối toàn bộ App           |

###### 

###### \---

###### 

###### \## 🚀 2. Cách làm việc đúng nhánh bằng Terminal

###### 

###### \### 🔹 Bước 1: Lấy danh sách nhánh mới nhất từ Server

###### 

###### ```bash

###### git fetch origin

###### ```

###### 

###### \---

###### 

###### \### 🔹 Bước 2: Chuyển vào đúng nhánh của mình

###### 

###### ```bash

###### git checkout <tên-nhánh>

###### ```

###### 

###### \*\*Ví dụ:\*\*

###### 

###### ```bash

###### git checkout feature/anh-transaction

###### ```

###### 

###### \---

###### 

###### \### 🔹 Bước 3: Kiểm tra nhánh hiện tại (RẤT QUAN TRỌNG)

###### 

###### ```bash

###### git branch

###### ```

###### 

###### 👉 Nhánh hiện tại sẽ có dấu `\*`

###### 

###### \---

###### 

###### \## 🛠️ 3. Quy trình Commit chuẩn (Tránh xung đột)

###### 

###### \### 🔸 Bước 1: Add code

###### 

###### ```bash

###### git add .

###### ```

###### 

###### \---

###### 

###### \### 🔸 Bước 2: Commit (viết message rõ ràng)

###### 

###### ```bash

###### git commit -m "feat: <mô tả những gì bạn đã làm>"

###### ```

###### 

###### \*\*Ví dụ:\*\*

###### 

###### ```bash

###### git commit -m "feat: hoàn thành UI thêm giao dịch"

###### ```

###### 

###### \---

###### 

###### \### 🔸 Bước 3: Pull code mới nhất từ nhánh develop

###### 

###### ```bash

###### git pull origin develop

###### ```

###### 

###### \---

###### 

###### \### 🔸 Bước 4: Push code lên GitHub

###### 

###### ```bash

###### git push origin <tên-nhánh-của-bạn>

###### ```

###### 

###### \---

###### 

###### \## ⚠️ Nguyên tắc vàng cho Team

###### 

###### ❌ \*\*KHÔNG BAO GIỜ:\*\*

###### 

###### ```bash

###### git push origin develop

###### git push origin main

###### ```

###### 

###### \---

###### 

###### ✅ \*\*PHẢI TUÂN THỦ:\*\*

###### 

###### \* Mỗi người chỉ làm việc trên \*\*nhánh cá nhân\*\*

###### \* Mọi thay đổi lớn phải \*\*thông báo trước\*\*, đặc biệt:

###### 

###### &#x20; \* Database (Nghĩa)

###### &#x20; \* Navigation / Integration (Đạt)

###### 

###### \---

###### 

###### \## 🔥 Phase 3 - Integration

###### 

###### \* Đạt sẽ thực hiện merge toàn bộ hệ thống

###### \* Các thành viên cần:

###### 

###### &#x20; \* Chủ động \*\*fix bug trên nhánh của mình\*\*

###### &#x20; \* Hỗ trợ khi có conflict

###### &#x20; \* Đảm bảo code \*\*chạy ổn định trước khi merge\*\*

###### 

###### \---

###### 

###### \## 💡 Tips chuyên nghiệp

###### 

###### \* Luôn pull trước khi push

###### \* Commit nhỏ, rõ ràng → dễ debug

###### \* Không commit file rác (`.log`, `.env`, `build/`)

###### \* Dùng `.gitignore` chuẩn

###### 

