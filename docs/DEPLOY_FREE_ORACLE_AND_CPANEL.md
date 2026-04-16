# Deploy Miễn Phí Cho MinLish

Tài liệu này chọn phương án miễn phí phù hợp với dự án hiện tại:

- Frontend: cPanel của `minlish.site`
- Backend: Oracle Cloud Free Tier

Lý do chọn cách này:

- Backend của dự án là Spring Boot + MySQL.
- Shared hosting cPanel thường không chạy được Java server lâu dài.
- Render free có thể chạy Java web service, nhưng không có MySQL free gắn sẵn cho bài toán này.
- Oracle Free Tier có compute miễn phí đủ để chạy cả backend và MySQL trên cùng một VM.

## 1. Mục tiêu cuối cùng

Sau khi xong, bạn sẽ có:

- `https://minlish.site` chạy frontend
- `https://api.minlish.site` chạy backend
- Frontend gọi API qua `https://api.minlish.site`
- Backend cho phép CORS từ `https://minlish.site`

## 2. Chuẩn bị DNS

Bạn đang có domain `minlish.site` trỏ về hosting `kesat.vn`.

Giữ nguyên hướng đó cho frontend.

Thêm một subdomain mới:

- `api.minlish.site`

Trỏ `A record` của `api.minlish.site` tới IP public của máy Oracle sau khi tạo xong VM.

## 3. Deploy frontend lên cPanel

### Bước 1: build frontend

Trong thư mục `D:\HocTap\Java\minlish-websites`:

```powershell
npm install
npm run build
```

### Bước 2: đặt biến môi trường production

Tạo file `.env` cho frontend với nội dung:

```env
VITE_API_BASE_URL=https://api.minlish.site
```

### Bước 3: upload thư mục `dist`

Upload toàn bộ nội dung trong `dist/` lên thư mục web root của domain `minlish.site` trong cPanel.

Nếu domain đang trỏ vào `public_html/minlish.site`, thì copy toàn bộ file trong `dist/` vào đó.

### Bước 4: kiểm tra

- Mở `https://minlish.site`
- Kiểm tra frontend có load được
- Kiểm tra API requests đang gọi `https://api.minlish.site`

## 4. Deploy backend lên Oracle Free Tier

### Bước 1: tạo Oracle Cloud Free Tier

Đăng ký Oracle Free Tier.

Oracle hiện có Always Free compute cho ứng dụng nhỏ, đủ để chạy backend Java và MySQL.

### Bước 2: tạo VM

Chọn một VM Always Free.

Khuyến nghị:

- Nếu có thể chọn Ampere A1, ưu tiên loại này vì RAM thoải mái hơn.
- Nếu chỉ có AMD free thì vẫn dùng được cho test/low traffic.

### Bước 3: cài môi trường

Trên VM, cài:

- JDK 21
- MySQL 8 hoặc MariaDB tương thích
- Git

### Bước 4: clone source backend

```bash
git clone <repo-backend-url>
cd minlish-servers
```

### Bước 5: tạo database

Tạo database `minlish` và user MySQL riêng.

Ví dụ:

```sql
CREATE DATABASE minlish CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'minlish'@'localhost' IDENTIFIED BY 'your_strong_password';
GRANT ALL PRIVILEGES ON minlish.* TO 'minlish'@'localhost';
FLUSH PRIVILEGES;
```

### Bước 6: cấu hình `.env`

Tạo file `.env` trong backend:

```env
UI_URL=https://minlish.site
PORT=8080
DB_URL=jdbc:mysql://localhost:3306/minlish?useSSL=false&serverTimezone=Asia/Bangkok
DB_USERNAME=minlish
DB_PASSWORD=your_strong_password
JWT_SECRET=your_long_random_secret
JWT_EXPIRATION=604800000
JWT_REFRESH_EXPIRATION=2592000000
CORS_ALLOWED_ORIGINS=https://minlish.site,https://www.minlish.site
```

Nếu dùng Google/GitHub login thì thêm các biến OAuth tương ứng.

### Bước 7: build và chạy thử

```bash
./mvnw clean package -DskipTests
java -jar target/minlish-0.0.1-SNAPSHOT.jar
```

Mở thử:

- `http://localhost:8080`

### Bước 8: chạy như service

Sau khi chạy thử ổn, tạo `systemd service` để backend tự bật lại sau reboot.

Ý tưởng service:

- Working directory: thư mục backend
- Start command: `java -jar target/minlish-0.0.1-SNAPSHOT.jar`

### Bước 9: public qua Nginx

Nên đặt Nginx reverse proxy:

- `api.minlish.site` -> `localhost:8080`

Như vậy bạn không cần mở trực tiếp port 8080 ra internet.

## 5. Trường hợp không muốn tự quản VM

Nếu bạn không muốn tự cài Linux, Nginx, MySQL và service, thì có 2 hướng khác:

### Hướng A: dùng Oracle Free Tier

Phù hợp nhất với dự án hiện tại vì giữ nguyên MySQL.

### Hướng B: đổi backend sang nền tảng khác và đổi database

Ví dụ chuyển sang PostgreSQL free trên service khác.

Hướng này phải sửa code nhiều hơn nên không khuyến nghị lúc này.

## 6. Cách kiểm tra sau deploy

1. Mở `https://minlish.site`
2. Đăng ký hoặc đăng nhập
3. Kiểm tra request API đi tới `https://api.minlish.site`
4. Kiểm tra OAuth redirect quay về `https://minlish.site/auth`
5. Kiểm tra backend logs nếu đăng nhập không thành công

## 7. Lỗi hay gặp

- CORS lỗi: quên set `CORS_ALLOWED_ORIGINS`
- 502/Bad Gateway: Nginx chưa trỏ đúng sang port backend
- Login Google lỗi: `UI_URL` hoặc redirect URI chưa đúng domain thật
- Frontend gọi `localhost`: quên set `VITE_API_BASE_URL`
