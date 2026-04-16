# Deploy Backend Lên Render Dùng MySQL CPanel

Tài liệu này dành cho trường hợp không dùng được Oracle Free Tier.

Mô hình:

- Frontend: cPanel domain `minlish.site`
- Backend: Render Free Web Service
- Database: MySQL trên iNET cPanel (`qdgjatryhosting_minlish`)

## 1. Điều kiện bắt buộc

Để Render kết nối được MySQL trên hosting iNET, cần đủ 2 điều kiện:

1. MySQL host cho phép remote connection từ internet.
2. Firewall/cPanel cho phép host bên ngoài truy cập port 3306.

Nếu không mở được remote MySQL thì backend trên Render sẽ không thể kết nối DB.

## 2. Chuẩn hóa thông tin DB

Bạn đã có:

- Database: `qdgjatryhosting_minlish`
- User: `qdgjatryhosting_minlish`

Khuyến nghị: đổi mật khẩu mới mạnh hơn, vì mật khẩu cũ đã từng được chia sẻ trong chat.

Bạn cần thêm:

- DB host public do iNET cung cấp (có thể là domain máy chủ hoặc IP)
- Port MySQL (thường 3306)

## 3. Mở Remote MySQL trong cPanel

Vào cPanel -> Remote MySQL.

Thêm host được phép truy cập. Với Render Free không có static outbound IP ổn định, nên thường phải cho phép rộng:

- Host: `%`

Lưu ý: nếu iNET không cho phép `%` hoặc chặn từ ngoài, phương án này sẽ fail.

## 4. Test kết nối DB từ máy local

Trước khi deploy Render, test từ máy local để xác nhận DB public được:

```powershell
mysql -h <DB_HOST_PUBLIC> -P 3306 -u qdgjatryhosting_minlish -p qdgjatryhosting_minlish
```

Nếu câu lệnh này không kết nối được thì Render cũng sẽ không kết nối được.

## 5. Deploy backend lên Render

### Bước 1: Push backend lên GitHub

Đảm bảo backend ở repo GitHub mà Render truy cập được.

### Bước 2: Tạo Web Service trên Render

- New + -> Web Service
- Chọn repo backend
- Runtime: Java
- Build Command: `./mvnw clean package -DskipTests`
- Start Command: `java -jar target/*.jar`

### Bước 3: Khai báo Environment Variables trên Render

Set các biến sau:

- `UI_URL=https://minlish.site`
- `PORT=10000`
- `DB_URL=jdbc:mysql://<DB_HOST_PUBLIC>:3306/qdgjatryhosting_minlish?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Bangkok`
- `DB_USERNAME=qdgjatryhosting_minlish`
- `DB_PASSWORD=<mat_khau_moi>`
- `JWT_SECRET=<chuoi_bi_mat_dai_it_nhat_64_ky_tu>`
- `JWT_EXPIRATION=604800000`
- `JWT_REFRESH_EXPIRATION=2592000000`
- `CORS_ALLOWED_ORIGINS=https://minlish.site,https://www.minlish.site`
- `SQL_INIT_MODE=never`

Nếu dùng OAuth hoặc mail thì thêm:

- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`
- `MAIL_USERNAME`, `MAIL_PASSWORD`

## 6. Gắn domain api.minlish.site vào Render

Sau khi service chạy và có URL dạng `https://xxx.onrender.com`:

1. Trong Render, Add Custom Domain: `api.minlish.site`
2. Trong iNET DNS, tạo bản ghi theo hướng dẫn Render (CNAME hoặc A/ALIAS tùy giao diện)
3. Chờ SSL tự cấp phát xong

## 7. Deploy frontend production

Trong frontend, build với env:

```env
VITE_API_BASE_URL=https://api.minlish.site
```

Upload `dist` lên thư mục domain `minlish.site` ở cPanel.

## 8. Checklist sau deploy

1. Mở `https://minlish.site`
2. Kiểm tra request API gọi đúng `https://api.minlish.site`
3. Thử đăng nhập/đăng ký
4. Nếu lỗi CORS, kiểm tra lại `CORS_ALLOWED_ORIGINS`
5. Nếu backend lỗi DB, kiểm tra Remote MySQL và `DB_URL`

## 9. Hạn chế của Render Free

- Service có thể sleep khi không có truy cập.
- Lần truy cập đầu sau sleep sẽ chậm vài chục giây.
- Không phù hợp production tải lớn, nhưng đủ cho demo/đồ án.
