# 🤝 CONTRIBUTING GUIDE

Tài liệu này mô tả quy trình làm việc Git của team theo hướng **đơn giản – đồng bộ – sử dụng 1 branch duy nhất (`main`)**.

---

# 🎯 Mục tiêu

- Làm việc trên **1 branch duy nhất: main**
- Đồng bộ code liên tục
- Hạn chế conflict
- Đảm bảo tiến độ team

---

# 🌿 Git Workflow (Main Only)

## 📌 Branch sử dụng

- `main`: branch duy nhất để tất cả thành viên làm việc

👉 Không sử dụng branch phụ

---

# 🔁 Quy trình làm việc

## 1. Luôn pull trước khi code

```bash
git pull origin main
```

⛔ Bắt buộc trước mọi thay đổi

---

## 2. Thực hiện code

- Làm đúng feature được phân công
- Không sửa code người khác nếu chưa trao đổi
- Tuân thủ kiến trúc: MVVM

---

## 3. Commit code

```bash
git add .
git commit -m "feat: add feature"
```

### 📌 Quy tắc commit

| Type     | Ý nghĩa           |
| -------- | ----------------- |
| feat     | Thêm tính năng    |
| fix      | Sửa lỗi           |
| refactor | Cải thiện code    |
| docs     | Cập nhật tài liệu |
| chore    | Việc vặt          |

---

## 4. Push ngay sau khi hoàn thành task nhỏ

```bash
git push origin main
```

👉 Không giữ code lâu trong máy

---

# ⚠️ Quy tắc bắt buộc

## ❌ Không được

- Push khi chưa pull
- Commit nhiều tính năng trong 1 lần
- Sửa file người khác tùy ý
- Push code đang lỗi
- Giữ code lâu rồi mới push

## ✅ Bắt buộc

- Pull trước khi code
- Push thường xuyên
- Test app trước khi push
- Viết commit rõ ràng
- Mỗi người làm 1 feature riêng

---

# 🧠 Phân chia công việc

Để tránh conflict, mỗi thành viên làm việc theo feature:

| Thành viên | Feature |
| ---------- |--------|
| Nghĩa      | Wallet + Database |
| Anh        | Transaction |
| Đức        | UI + Dashboard |
| Thịnh      | Report |
| Đạt        | Search + Integration |

---

# 🔧 Xử lý conflict

## Khi gặp conflict:

```bash
git pull origin main
```

### Bước xử lý:

1. Mở file bị conflict
2. Tìm đoạn:
```
<<<<<<< HEAD
code của bạn
=======
code người khác
>>>>>>>
```
3. Chỉnh sửa lại cho đúng
4. Commit lại:

```bash
git add .
git commit -m "fix: resolve conflict"
git push
```

---

# ⚡ Best Practices

- Commit nhỏ, rõ ràng
- Push nhiều lần trong ngày
- Không để code local quá lâu
- Luôn build/run trước khi push

---

# 🚀 Quy trình chuẩn mỗi ngày

```bash
# 1. Lấy code mới nhất
git pull origin main

# 2. Code

# 3. Commit
git add .
git commit -m "feat: something"

# 4. Push
git push origin main
```

---

# 📌 Ghi chú

Workflow này phù hợp cho:

- Team nhỏ (3–5 người)
- Dự án học tập / deadline ngắn

👉 Yêu cầu quan trọng nhất: **kỷ luật khi dùng Git**

---

# ❤️ Đóng góp

- Tuân thủ đúng quy trình
- Hỗ trợ team khi có conflict
- Test kỹ trước khi push

---

🔥 Happy Coding!

