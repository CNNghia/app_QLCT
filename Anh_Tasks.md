# 📋 Danh sách nhiệm vụ của Anh (Transaction Feature)

Dựa trên file `README.md` và hiện trạng của project, dưới đây là chi tiết các nhiệm vụ bạn cần thực hiện cho tính năng **Quản lý giao dịch (Transaction)**.

---

## 🎯 Mục tiêu chính
Thực hiện trọn vẹn chu trình CRUD (Thêm, Xem, Sửa, Xóa) cho các giao dịch tài chính, đảm bảo dữ liệu được lưu trữ chính xác vào Database và tuân thủ kiến trúc **MVVM + Clean Architecture**.

---

## 🛠️ Các bước thực hiện cụ thể

### 1. Xây dựng cấu trúc MVVM
Hiện tại `TransactionsActivity` đang xử lý Logic trực tiếp. Bạn cần tách biệt ra:
- **`TransactionViewModel`**: Quản lý State của UI (danh sách giao dịch, trạng thái loading, lỗi).
- **`TransactionRepository`**: Làm trung gian giữa ViewModel và Database (Room).
- **`Transaction` (Model)**: Đảm bảo class này đã được định nghĩa đúng với các trường: `id`, `amount`, `note`, `categoryId`, `walletId`, `date`, `type` (THU/CHI).

### 2. Triển khai CRUD (Tác vụ trọng tâm)
-  **Thêm (Create)**:
    - Hoàn thiện `AddTransactionActivity`.
    - Kiểm tra tính hợp lệ (Validate): Số tiền phải > 0, phải chọn hạng mục, ngày tháng.
    - Gọi qua ViewModel -> Repository -> Room để lưu.
- **Xem (Read)**:
    - Hiển thị danh sách giao dịch trong `TransactionsActivity`.
    - Lọc giao dịch theo **Tháng/Năm** và theo **Loại** (Thu nhập / Chi tiêu) như giao diện hiện có.
- **Sửa (Update)**:
    - Cho phép nhấn vào một giao dịch để mở màn hình chỉnh sửa.
    - Cập nhật lại thông tin trong Database.
- **Xóa (Delete)**:
    - Chức năng xóa giao dịch (có thể qua nút xóa hoặc vuốt để xóa).

### 3. Quy tắc Code (Bắt buộc)
Theo quy ước trong `README.md`, bạn **không được**:
- ❌ Gọi trực tiếp DAO từ Activity/Fragment.
- ❌ Viết logic xử lý dữ liệu trong Activity (Ví dụ: logic cộng trừ, chuyển đổi định dạng ngày tháng nên nằm trong ViewModel hoặc UseCase).

Bạn **phải**:
- ✅ Sử dụng `LiveData` hoặc `Flow` để quan sát dữ liệu từ ViewModel.
- ✅ Đảm bảo sau khi giao dịch được thêm/sửa/xóa, số dư ví tương ứng cũng phải được cập nhật (Phối hợp với Nghĩa - Data & Wallet).

### 4. Quy trình Git
- Làm việc trên nhánh: `feature/anh-transaction`.
- Commit chuẩn: `feat: implement create transaction`, `fix: validate amount input`, ...
- Khi xong, tạo **Pull Request** để Team Leader review.

---

## 📅 Lộ trình (Phase 2)
Bạn đang ở **Phase 2** của dự án. Đây là giai đoạn quan trọng nhất để ứng dụng có thể chạy thực tế.

> **Gợi ý**: Bạn nên bắt đầu bằng việc chuyển logic chuyển đổi tháng trong `TransactionsActivity` vào `TransactionViewModel` để làm quen với cách dữ liệu chảy trong ứng dụng.

---
*Tài liệu này được tạo tự động dựa trên yêu cầu dự án.*
