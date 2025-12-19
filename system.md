# Quản Lý Thu - Chi Trung Tâm Dạy Học Thêm Song Lâm Edu

### I. Các chức năng chính

#### 1. Thông tin chung về Hộ kinh doanh: Các thông tin này dùng để hiển thị lên phiếu thu hoặc hóa đơn
- Tên đầy đủ: HỘ KINH DOANH CƠ SỞ GIÁO DỤC SONG LÂM EDU
- Tên giao dịch/viết tắt: HỘ KINH DOANH CƠ SỞ GIÁO DỤC SONG LÂM EDU
- Loại hình kinh doanh: Hộ kinh doanh
- CCCD Chủ hộ: 033169011971
- Mã số thuế: 8083456655-001
- Mã công ty: kb94guy4
- Ngày thành lập: 06/03/2025
- Mã số đăng ký kinh doanh: 40A8062989
- Ngày cấp: 06/03/2025
- Nơi cấp: Phường Tân Lập
- Người đại diện pháp luật: An Thị Thanh
- Chức danh: Chủ cơ sở
- Địa chỉ: 193/5B Nguyễn Thái Bình, Tân Lập, Đắk Lắk
- Điện thoại: 0835100699
- Fax:
- Email: mrlong07.11@gmail.com
- Website:

#### 2. Quản lý người dùng: Có 2 loại người dùng là Admin và Cashier
- Admin sẽ được tạo bằng script SQL
- Cashier có thể tự Register và cần Admin active account
    - Sau khi Register, tài khoản Cashier ở trạng thái chưa active
    - Cashier không thể login cho đến khi Admin active tài khoản

#### 3. Quản lý học sinh:
- Sử dụng CCCD để làm mã học sinh
- **LƯU Ý**: Đối với học sinh chưa có CCCD, sử dụng CCCD của phụ huynh với format CCCD_phụ huynh + số thứ tự con (ví dụ: 033169011971-01, 033169011971-02)
- Có nhập hàng loạt từ excel

#### 4. Quản lý thu: xuất phiếu như mẫu [receipt_template.html](src/main/resources/templates/receipt_template.html)

#### 5. Quản lý chi: phát triển sau

#### 6. Thông kê doanh thu: thống kê doanh thu theo tháng, quý, năm theo mẫu [So_quy_tien_mat.xlsx](src/main/resources/templates/So_quy_tien_mat.xlsx)

### II. Kiến trúc hệ thống
- Build bằng gradle
- Project Name: SongLamEdu
- Package Name: com.songlam.edu
- Database: PostgreSQL (Các [entity](src/main/java/com/songlam/edu/entity) đã được tạo sẵn trong project)
- Backend: Spring Boot
- Frontend: Thymeleaf
- Repository: [SongLamEdu](https://github.com/GiaMinh0802/SongLamEdu.git)

### III. Giao diện hệ thống
- Hệ thống gồm các url
    - /login
    - /logout
    - /register
    - /forgot-password
    - /reset-password
    - /change-password
    - /me						# Thông tin cá nhân
    - /info 					# Thông tin Hộ kinh doanh
    - /users					# Quản lý người dùng
    - /students				    # Quản lý học sinh
    - /transactions			    # Quản lý thu
    - /dashboard				# Thống kê doanh thu
- Sau khi login các trang hiển thị ở từng người dùng
    - Admin:
        - Thông tin cá nhân (hiển thị khi click vào vào ảnh đại diện ở góc phải trên cùng)
        - Thông tin chung về Hộ kinh doanh
        - Quản lý người dùng
        - Quản lý học sinh
        - Quản lý thu
        - Thống kê doanh thu
    - Cashier:
        - Thông tin cá nhân (hiển thị khi click vào vào ảnh đại diện ở góc phải trên cùng)
        - Thông tin chung về Hộ kinh doanh
        - Quản lý học sinh
        - Quản lý thu

### IV. Phân quyền chức năng của từng người dùng

#### 1. Login/Logout/Register/Forgot password/Reset password/Change password
- Admin:
    - Login
    - Logout
    - Forgot password
    - Reset password
    - Change password
- Cashier:
    - Login
    - Logout
    - Register
    - Forgot password
    - Reset password
    - Change password
#### 2. Thông tin chung về Hộ kinh doanh:
- Admin:
    - Xem thông tin
    - Chỉnh sửa thông tin
- Cashier:
    - Xem thông tin
#### 3. Quản lý người dùng: chỉ Admin
- Tìm kiếm: theo CCCD, họ tên, số điện thoại
- Phân trang: 20 người/trang
- Xem thông tin (chỉ hiện thị các Cashier, không hiển thị các Admin khác)
- Chỉnh sửa thông tin
- Active tài khoản cho Cashier
#### 4. Quản lý học sinh:
- Admin:
    - Tìm kiếm: theo CCCD, họ tên, số điện thoại
    - Phân trang: 20 học sinh/trang
    - Xem thông tin học sinh
    - Chỉnh sửa thông tin học sinh
    - Thêm học sinh
    - Thêm học sinh hàng loạt từ excel
    - Tải file excel mẫu nhập hàng loạt
- Cashier:
    - Tìm kiếm: theo CCCD, họ tên, số điện thoại
    - Phân trang: 20 học sinh/trang
    - Xem thông tin học sinh
    - Tạo phiếu thu
#### 5. Quản lý thu: cả Admin và Cashier
- Tìm kiếm: theo mã phiếu thu, CCCD học sinh, ngày thu
- Lọc: theo khoảng thời gian, người thu (Cashier)
- Phân trang: 20 phiếu thu/trang
- Xem chi tiết phiếu thu
- Chỉnh sửa thông tin phiếu thu
- Hủy phiếu thu
- Tải phiếu thu
- Tải phiếu thu hàng loạt theo điều kiện lọc
#### 6. Thống kê doanh thu: chỉ Admin
- Xem dashboard doanh thu
    - Tổng thu trong tháng/quý/năm
    - Phần trăm tăng trưởng của tháng/quý/năm hiện tại so với kỳ trước
    - Biểu đồ cột: doanh thu theo tháng
- Xuất thông kê theo tháng/quý/năm

### V. Quy trình nghiệp vụ chi tiết

#### 1. Luồng điều hướng sau login
- Khi truy cập vào website:
    - Nếu chưa đăng nhập → chuyển đến /login
    - Sau khi login thành công:
        + Admin → chuyển đến /dashboard (Thống kê doanh thu)
        + Cashier → chuyển đến /students (Quản lý học sinh)
    - Nếu user đang login cố truy cập trang không có quyền → hiển thị lỗi 403 Forbidden

#### 2. Quy trình quên mật khẩu
- Nhập email → Hệ thống gửi link reset password (hết hạn trong 30 phút)
- Click link → Nhập mật khẩu mới → Xác nhận

#### 3. Quy trình đóng học phí
- Cashier chọn học sinh → Click button [Thu học phí] → Nhập thông tin thu phí (Lý do nộp, Số tiền) → Tạo phiếu thu
- Hệ thống tự động sinh mã phiếu thu (format: PT + 6 chữ số tự tăng bằng SEQUENCE, ví dụ: PT000001)
- Phiếu thu được lưu vào hệ thống và hiển thị lên browser ở 1 tab khác

#### 4. File Excel mẫu nhập học sinh
- Cấu trúc: CCCD | Họ và tên | Ngày sinh | Giới tính | Địa chỉ | Số điện thoại
- Số dòng tối đa: 100 học sinh/lần
- Validate: tất cả đều không được để trống
    - CCCD: maxlength 15 ký tự chữ số, không được trùng với học sinh đã có trong hệ thống và trong cùng file
    - Họ và tên: chỉ chứa chữ cái và khoảng trắng
    - Ngày sinh: format dd/MM/yyyy
    - Giới tính: chỉ chấp nhận "Nam" hoặc "Nữ"
    - Số điện thoại: cố định 10 ký tự chữ số
- Xử lý lỗi:
    - Nếu 1 dòng lỗi: bỏ qua dòng đó, tiếp tục import các dòng hợp lệ
    - Sau khi import: hiển thị thông báo
        - "Import thành công X học sinh"
        - "Lỗi Y học sinh: [Danh sách lỗi chi tiết]"
    - Xuất file Excel chứa các dòng lỗi để người dùng sửa và import lại

### VI. Yêu cầu bảo mật
- Mật khẩu: tối thiểu 8 ký tự, bao gồm chữ hoa, chữ thường, số
- Mã hóa mật khẩu: BCrypt
- Session timeout: 30 phút không hoạt động
- Remember-me: 30 ngày
    - Logout → Xóa remember-me cookie
    - Đổi mật khẩu → Invalidate remember-me
    - Admin deactivate Cashier → buộc logout tất cả session
- Token reset password: hết hạn sau 30 phút
- HTTPS + HttpOnly cookie: deploy production