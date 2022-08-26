# 卷吧后端开发项目
## 基于springboot进行开发

> 使用の技术
* 数据库mysql
* 小型缓存数据库redis
* 数据源druid
* 持久层mybatis-plus
* 安全spring-securiy
* jwt签名
* 文档支持swagger2
* json序列化fastjson
* office全家桶处理 aspose
* pdf操作
  * itextpdf
  * thumbnailator
  * pdfbox
* 视频截取 ffmpeg

> 亮点
* 使用对象存储来存储图片、文档等
* 继承了上次邮箱验证码，并新增了计算型的图形验证码
* 文档处理
  * 文档上传后 原文档上传至oss对象存储
  * 文档处理为pdf储存在本地 (便于统一转化为pdf文件流全文预览用)
  * 处理出的pdf有个性化水印
  * 截取pdf第一页 并存储至oss对象存储 (作为预览图展示)
* 视频截取前几帧作为预览图存至oss
* **项目采用docker部署交付，能够一键启动并部署**