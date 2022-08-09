package com.lyd;

import com.aliyun.oss.OSSClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.lyd.config.OssPath;
import com.lyd.controller.VO.CommentsVO;
import com.lyd.controller.VO.MyPost;
import com.lyd.entity.*;
import com.lyd.mapper.DocumentMapper;
import com.lyd.mapper.PostsMapper;
import com.lyd.mapper.UserInfoMapper;
import com.lyd.mapper.UserMapper;
import com.lyd.service.CommentsService;
import com.lyd.service.PostsService;
import com.lyd.service.UploadService;
import com.lyd.service.VideoService;
import com.lyd.utils.ExcelUtil;
import com.lyd.utils.FileUtils;
import com.lyd.utils.OssUtils;
import com.lyd.utils.PdfUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    @Test
    void contextLoads() {
//        System.out.println(encoder.encode("123456"));
        System.out.println(expirationSeconds);
    }
    @Test
    void pyTest1() {
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.exec("print 'hello world';");
    }
    @Test
    void pyTest2() {
        try {
            String[] arg = new String[] { "python", "C:\\Users\\天狗\\Desktop\\test.py", "123","456" };

            Process proc = Runtime.getRuntime().exec(arg);
            InputStreamReader reader = new InputStreamReader(proc.getInputStream(),"GBK");
            BufferedReader in = new BufferedReader(reader);
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            reader.close();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
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
        List<MyPost> myPost = userMapper.getMyPost(1550319589076406273L, 1, 5);
        log.info(myPost.toString());
    }

}
