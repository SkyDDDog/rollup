package com.lyd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSSClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.lyd.config.OssPath;
import com.lyd.controller.VO.BanUserVO;
import com.lyd.controller.VO.CommentsVO;
import com.lyd.controller.VO.MyPost;
import com.lyd.controller.VO.PrePostVO;
import com.lyd.entity.*;
import com.lyd.mapper.DocumentMapper;
import com.lyd.mapper.PostsMapper;
import com.lyd.mapper.UserInfoMapper;
import com.lyd.mapper.UserMapper;
import com.lyd.service.*;
import com.lyd.utils.ExcelUtil;
import com.lyd.utils.FileUtils;
import com.lyd.utils.OssUtils;
import com.lyd.utils.PdfUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

@Slf4j
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class DemoApplicationTests {

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private UserMapper userMapper;

    @Value("${token.expirationSeconds}")
    private int expirationSeconds;

    @Autowired
    UploadService uploadService;

    @Resource
    UserInfoMapper userInfoMapper;
    @Resource
    DocumentMapper documentMapper;
    @Resource
    PostsMapper postsMapper;

    @Autowired
    OSSClient ossClient;
    @Autowired
    OssPath ossPath;

    @Autowired
    CommentsService commentsService;
    @Autowired
    PostsService postsService;

    @Autowired
    VideoService videoService;
    @Autowired
    UserService userService;

    @Test
    void contextLoads() {
//        System.out.println(encoder.encode("123456"));
        System.out.println(expirationSeconds);
    }

    @Test
    void newUser() {
        User user = new User();
        user.setEmail("362664609@qq.com");
//        user.setPassword(encoder.encode("123456"));
        user.setPassword("123456");
        user.setRole("ROLE_admin");

        userMapper.insert(user);

    }

    @Test
    void word2pdf() {
        String doc = "C:\\Users\\天狗\\Desktop\\1.docx";
        String excel = "C:\\Users\\天狗\\Desktop\\27#.xlsx";
        String ppt = "C:\\Users\\天狗\\Desktop\\总复习.ppt";
        String pdf = "pdf/1.pdf";
        PdfUtils.word2pdf(doc,pdf);
//        PdfUtils.excel2pdf(excel,pdf);
//        PdfUtils.ppt2pdf(ppt,pdf);

        String water = "pdf/2.pdf";
        PdfUtils.wateMark(pdf,water,"卷吧");
//        PdfUtils.PDFFramer(pdf);
        PdfUtils.PDFFramer(water);

    }

    @Test
    void delFile() {
        String path = "tmp/";
        FileUtils.delFileInDir(path);
    }

    @Test
    void upload() throws IOException {
        String inPath = "C:\\Users\\天狗\\Desktop\\upload";
//        Long userId = 1550319589076406273L;
        List<User> users = userMapper.selectList(null);
        Random random = new Random();
        int i = random.nextInt(users.size());
        Long userId = users.get(i).getId();



        File folder = new File(inPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    log.info(file.getName());
                    FileInputStream in_file = new FileInputStream(file);
                    MultipartFile multipartFile = new MockMultipartFile(file.getName(), in_file);
                    Document document = uploadService.uploadDoc(multipartFile, userId, "论文模板");
                    uploadService.uploadInfo2DB(document);
                }
            }
        }
    }

    @Test
    void insertTestUser() {
        ExcelUtil data = new ExcelUtil("C:\\Users\\天狗\\Desktop\\暑假合作论\\userData.xlsx", "data");
        // 起始第几行
        final int row = 1;
        // 插入几条数据
        final int num = 10;
        // 学校清单
        final String[] schools = {"福州大学","华北电力大学","杭州电子科技大学","北京大学","清华大学","复旦大学","上海交通大学","西安电子科技大学","西安交通大学","厦门大学"};
        final String[] professions = {"计算机类","软件工程","人工智能","土木工程","机械工程"};

        for (int i = row; i < num+row ; i++) {
            User user = new User();
            UserInfo userInfo = new UserInfo();

            long id = IdWorker.getId(user);
            String email;
            String username;
            String password = "123456";
            password = encoder.encode(password);
            String role = "ROLE_USER";
            String nickname;
            String signature;
            Boolean gender;
            String school;
            String profession;
            Integer grade;

            username = data.getExcelDateByIndex(i,3);
            nickname = data.getExcelDateByIndex(i,2);
            email = data.getExcelDateByIndex(i,10);
            signature = data.getExcelDateByIndex(i,14);
            String tmp = data.getExcelDateByIndex(i,4);
            if ("男".equals(tmp)) {
                gender = false;
            } else if ("女".equals(tmp)) {
                gender = true;
            } else {
                gender = null;
            }
            school = schools[(int)(Math.random()*10)];
            grade = 2018 + (int)(Math.random()*4);
            profession = professions[(int)(Math.random()*5)];

            user.setId(id);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(role);

            userInfo.setId(id);
            userInfo.setNickname(nickname);
            userInfo.setSignature(signature);
            userInfo.setGender(gender);
            userInfo.setSchool(school);
            userInfo.setProfession(profession);
            userInfo.setGrade(grade);

            log.info(user.toString());
            log.info(userInfo.toString());
            userMapper.insert(user);
            userInfoMapper.insert(userInfo);
            log.info("插入成功");


        }
    }

    @Test
    void updateUserHead() throws Exception {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.ne("id",1550319589076406273L);
        List<UserInfo> userInfos = userInfoMapper.selectList(wrapper);
        for (UserInfo userInfo : userInfos) {
            Long id = userInfo.getId();
            String head = userInfo.getHead();
            org.jsoup.nodes.Document document = Jsoup.parse(new URL(head), 300000);
            Element download = document.getElementById("download");
            String attr = download.getElementsByTag("a").attr("href");
            log.info(attr);
            URL url = new URL(attr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 10000);
            InputStream inStream = conn.getInputStream();
            MultipartFile multipartFile = FileUtils.inputStreamToMultipartFile(id+".png", inStream);
            FileUtils.approvalFile(multipartFile,id+".png","tmp/");
            String newPhoto = OssUtils.saveImg(multipartFile, ossClient, id.toString(), ossPath.getUserHeadPath());

            userInfo.setHead(newPhoto);
            userInfoMapper.updateById(userInfo);
        }



    }

    @Test
    void crawler() throws IOException {
//        org.jsoup.nodes.Document document = Jsoup.parse(new URL("https://i.postimg.cc/QtGq2M69/106740190-196122765177606-5839687620378360321-n.jpg"), 30000);
        org.jsoup.nodes.Document document = Jsoup.parse(new URL("https://i.postimg.cc/63rhtNSH/106220989-293290228691886-4408161238520034314-n.jpg"), 300000);
        Element download = document.getElementById("download");
//        log.info(download.toString());
        String attr = download.getElementsByTag("a").attr("href");
        log.info(attr);
    }

    @Test
    void previewPdf() throws IOException {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        List<Document> documents = documentMapper.selectList(wrapper);
        for (Document document : documents) {
            // 定义各种路径/文件名
            Long id = document.getId();
            String oriUrl = document.getDoc_path();
            String suffix = oriUrl.substring(oriUrl.lastIndexOf('.'));
            String docFullName = id+suffix;
            String pdfFullName = id+".pdf";
            String tmpPath = "tmp/";
            String tmpFilePath = tmpPath + docFullName;
            String tmpOutPath = tmpPath + pdfFullName;
            String previewPdfPath = "previewPdf/"+pdfFullName;

            URL url = new URL(oriUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 10000);
            InputStream inStream = conn.getInputStream();
            MultipartFile multipartFile = FileUtils.inputStreamToMultipartFile(id+suffix, inStream);
            FileUtils.approvalFile(multipartFile,docFullName,"tmp/");

            // 水印字样
            final String mark = "卷吧";
            if (".doc".equals(suffix) || ".docx".equals(suffix)) {
                PdfUtils.word2pdf(tmpFilePath,tmpOutPath);
            } else if (".xls".equals(suffix) || ".xlsx".equals(suffix)) {
                PdfUtils.excel2pdf(tmpFilePath,tmpOutPath);
            } else if (".ppt".equals(suffix) || ".pptx".equals(suffix)) {
                PdfUtils.ppt2pdf(tmpFilePath,tmpOutPath);
            } else if (".pdf".equals(suffix)) {

            }
            PdfUtils.wateMark(tmpOutPath,previewPdfPath,mark);
            // 删除本地缓存文件
            FileUtils.delFileInDir(tmpPath);

        }


    }


    @Test
    void testPasswd() {
//        String pwd = "$2a$10$2RfQV9a45ZBoaLGfQstm.uWMaFsuJ/V09Y//eKECGUqVydhij7R4S";
        String pwd = "$2a$10$SY7Oa8TNAotwGyLd5qrRCuafSoo1v6MPc0y0QW2gJGfin/gdLru7y";
        boolean matches = encoder.matches("123456", pwd);
        System.out.println(matches);
        System.out.println(encoder.encode("123456"));
//        String encode = encoder.encode("123456");
//        System.out.println(encode);
//        log.info(encode);
    }


    @Test
    void insertPostData() {
        List<User> users = userMapper.selectList(null);
        Long userId = null;
        Random random = new Random();
        int p = random.nextInt(users.size());

        ExcelUtil postData = new ExcelUtil("C:\\Users\\天狗\\Desktop\\暑假合作论\\data1.xlsx", "posts");
        ExcelUtil commentData = new ExcelUtil("C:\\Users\\天狗\\Desktop\\暑假合作论\\data1.xlsx", "comments");
//        log.info(postData.getExcelDateByIndex(0, 0));
        for (int i = 0; i < postData.getColNums(); i++) {
            String pId = postData.getExcelDateByIndex(i, 0);
            String title = postData.getExcelDateByIndex(i, 1);
            Posts post = new Posts();
            Long id = IdWorker.getId(post);
            post.setId(id);
            post.setTitle(title);
            post.setUser_id(users.get(p).getId());
//            log.info(post.toString());
            postsMapper.insert(post);
//            postsService.newPost(title,userId,null);
            for (int j = 0; j < commentData.getColNums(); j++) {
                String rootId = commentData.getExcelDateByIndex(j, 1);
                if (rootId.equals(pId)) {
//                PostComments comment = new PostComments();
//                comment.setContent(commentData.getExcelDateByIndex(j,2));
//                comment.setPost_id(id);
                int ci = p;
                while (ci==p) {
                    ci = random.nextInt(users.size());
                }
//                comment.setUser_id(users.get(ci).getId());
//                log.info(comment.toString());

                    commentsService.newComment(users.get(ci).getId(),commentData.getExcelDateByIndex(j,2),id,
                            null,null);
                }
            }
        }



    }


    @Test
    void testRandon() {
        List<User> users = userMapper.selectList(null);
        Long userId = null;
        Random random = new Random();
        log.info("size:{}",users.size());
        int p = random.nextInt(users.size());
        log.info(p+"");
        int ci = p;
        while (ci==p) {
            ci = random.nextInt(users.size());
        }
        log.info(ci+"");
    }

    @Test
    void insertCc() {
        Long userId = 1550319589076406273L;
        String content = "测试用例测试用例测试用例";
        Long postId = 1554403290269827075L;
        Long answerId = 1554403290336935937L;
        Long commentId = null;
        List<CommentsVO> commentsByPostId = commentsService.getCommentsByPostId(null, postId, 1, 20);
        for (CommentsVO commentsVO : commentsByPostId) {
            answerId = Long.valueOf(commentsVO.getCommentId());
            commentsService.newComment(userId,content,postId,answerId,commentId);
        }
    }

    @Test
    void uploadVideo() {

//        String tmpTestVideo = "F:\\各类文档\\FZU\\学习资料\\大学物理\\2022-4-7.mp4";
        String tmpTestVideo = "video/";
//        FileUtils.grabberVideoFramer(tmpTestVideo,"C:\\Users\\天狗\\Desktop\\testPhoto","1.mp4","123.jpg");
    }

    @Test
    void testSql() {
        List<BanUserVO> banUserList = userService.getBanUserList(1, 5);
        log.info(banUserList.toString());

    }

    @Test
    void testDate() {
        LocalDate localDate = LocalDate.now();
        LocalDate startTime = localDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endTime = localDate.with(TemporalAdjusters.lastDayOfMonth());
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        log.info(df.format(startTime)+" 00:00:00");
        log.info(df.format(endTime)+" 23:59:59");
    }

    @Test
    void testHttpRequest() throws Exception {
        // 定义请求路径
        String requertUrl = "http://1.12.66.67:9091/posts/1/1/10";
        String userId = "1550319589076406273";
        String para = "?userId=" + URLEncoder.encode(userId, "UTF-8");
        requertUrl += para;
        // 创建连接并设置参数
        URL url = new URL(requertUrl);
        HttpURLConnection httpCoon = (HttpURLConnection) url.openConnection();
        httpCoon.setRequestMethod("GET");
        httpCoon.setRequestProperty("Charset","UTF-8");
//        httpCoon.setRequestProperty("Content-Type","application/json");
//        // 打开流
//        httpCoon.setDoOutput(true);
//        httpCoon.setDoInput(true);

//        // 获取输入流和写数据
//        OutputStream os = httpCoon.getOutputStream();
//        os.write(json.getBytes());
//        os.flush();

//         发起http请求 (getInputStream触发http请求)
        if (httpCoon.getResponseCode() != 200) {
            throw new Exception("调用请求异常,状态码:"+httpCoon.getResponseCode());
        }

        // 获取输入流和读数据
        BufferedReader br = new BufferedReader(new InputStreamReader(httpCoon.getInputStream()));
        String resultData = br.readLine();
//        log.info(resultData);
        JSONObject jsonObject = JSON.parseObject(resultData);
        String code = (String) jsonObject.get("code");
        String msg = (String) jsonObject.get("msg");
        JSONArray data = jsonObject.getJSONArray("data");
        log.info(code);
        log.info(msg);
        for (int i = 0; i < data.size(); i++) {
            JSONObject object = data.getJSONObject(i);
            PrePostVO prePostVO = JSON.toJavaObject(object, PrePostVO.class);
            log.info(prePostVO.getPostId());
        }
        // 关闭连接
        httpCoon.disconnect();
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        System.out.println(a-b);


        scanner.close();

    }


    @Test
    public void testPyRecommend() throws Exception {
        // 定义请求参数
//        String userId = "1550319589076406273";
        String userId = null;
        // 定义请求路径
        String prefix = "http://";
        String ip = "27.158.103.146";
        String port = "65000";
        String path = "/api/v1/recommend/";
        String requestUrl = prefix+ip+":"+port+path;
        log.info(requestUrl);
        if (userId!=null) {
            requestUrl += userId;
        }

        // 创建连接并设置参数
        URL url = new URL(requestUrl);
        HttpURLConnection httpCoon = (HttpURLConnection) url.openConnection();
        httpCoon.setRequestMethod("GET");
        httpCoon.setRequestProperty("Charset","UTF-8");

//         发起http请求 (getInputStream触发http请求)
        if (httpCoon.getResponseCode() != 200) {
            throw new Exception("调用请求异常,状态码:"+httpCoon.getResponseCode());
        }

        // 获取输入流和读数据
        BufferedReader br = new BufferedReader(new InputStreamReader(httpCoon.getInputStream()));
        String resultData = br.readLine();
//        log.info(resultData);
        JSONObject jsonObject = JSON.parseObject(resultData);
        Integer code = (Integer) jsonObject.get("statue");
        String msg = (String) jsonObject.get("message");
        JSONArray data = jsonObject.getJSONArray("data");
        log.info(code.toString());
        log.info(msg);
        for (int i = 0; i < data.size(); i++) {
            String id = (String) data.get(i);
            log.info(id);
        }
        // 关闭连接
        httpCoon.disconnect();
    }

    @Test
    public void testPyUrl() throws Exception {
        List<PrePostVO> recByUrl = postsService.getRecByUrl(null,1,10);
        log.info(recByUrl.toString());
    }



}
