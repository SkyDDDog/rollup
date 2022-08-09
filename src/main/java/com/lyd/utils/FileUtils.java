package com.lyd.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.imageio.ImageIO;
import org.bytedeco.javacv.Frame;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class FileUtils {

    /**
     * 将 multipartFile 转换成  临时 file 文件.（该临时文件使用完记得 delete掉.）
     *
     * @param multipartFile multipartFile
     * @return 临时的 file 文件.
     * @throws IOException io
     */
    public static File multipartFileToFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null) {
            return null;
        }

        InputStream inputStream = multipartFile.getInputStream();

        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead;
            byte[] buffer = new byte[8192];
            while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static void approvalFile( MultipartFile filecontent,String fileName,String path){
        OutputStream os = null;
        InputStream inputStream = null;
//        String fileName = null;
        try {
            inputStream = filecontent.getInputStream();
//            fileName = filecontent.getOriginalFilename();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
//            String path = "tmp/";
            // 2、保存到临时文件
            // 1K的数据缓冲
            byte[] bs = new byte[1024];
            // 读取到的数据长度
            int len;
            // 输出的文件流保存到本地文件
            File tempFile = new File(path);
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            os = new FileOutputStream(tempFile.getPath() + '/' + fileName);
            // 开始读取
            while ((len = inputStream.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 完毕，关闭所有链接
            try {
                os.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void delFileInDir(String path) {
        File index = new File(path);
        if (index.isDirectory()) {
            File[] files = index.listFiles();
            if (files != null) {
                for (File in : files) {
                    in.delete();
                }
            }
        }
    }

    /**
     * 将本地文件转成MultipartFile
     * @param path
     * @return
     */
    public static MultipartFile toMultipartFile(String path) {
        //先将本地文件转化成FileItem
        FileItem fileItem = createFileItem(path);
        return new CommonsMultipartFile(fileItem);
    }

    /**
     * 根据filePath将puppeteer截取到的图片转成FileItem
     * @param filePath 文件路径
     * @return
     */
    public static FileItem createFileItem(String filePath) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String fieldName = "example"; //文件名
        FileItem item = factory.createItem(fieldName, "image/jpg", true, UUID.randomUUID() + ".jpeg");
        File newfile = new File(filePath);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        try {
            FileInputStream fis = new FileInputStream(newfile);
            OutputStream os = item.getOutputStream();
            while ((bytesRead = fis.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item;
    }

    /**
     * @Description 字节输入流转换为包含二进制数据+文件名称的MultipartFile文件
     * @param fileName 文件名
     * @param inputStream 字节输入流
     * @return
     */
    public static MultipartFile inputStreamToMultipartFile(String fileName, InputStream inputStream) throws IOException {
        //该方法只能用于测试return new MockMultipartFile(fileName,fileName, MediaType.MULTIPART_FORM_DATA_VALUE,inputStream);
        FileItem fileItem = createFileItem(inputStream, fileName);
        //CommonsMultipartFile是feign对multipartFile的封装，但是要FileItem类对象
        return new CommonsMultipartFile(fileItem);
    }

    /**
     * FileItem类对象创建
     *
     * @param inputStream inputStream
     * @param fileName    fileName
     * @return FileItem
     */
    public static FileItem createFileItem(InputStream inputStream, String fileName) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = "file";
        FileItem item = factory.createItem(textFieldName, MediaType.MULTIPART_FORM_DATA_VALUE, true, fileName);
        int bytesRead = 0;
        byte[] buffer = new byte[10 * 1024 * 1024];
        OutputStream os = null;
        //使用输出流输出输入流的字节
        try {
            os = item.getOutputStream();
            while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            inputStream.close();
        } catch (IOException e) {
            log.error("Stream copy exception", e);
            throw new IllegalArgumentException("文件上传失败");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    log.error("Stream close exception", e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Stream close exception", e);
                }
            }
        }

        return item;
    }

    /**
     * 将视频文件帧处理并以“jpg”格式进行存储。
     * 依赖FrameToBufferedImage方法：将frame转换为bufferedImage对象
     */
    public static BufferedImage grabberVideoFramer(String videoPath,String videoFileName){
        //Frame对象
        Frame frame = null;
        //返回BufferedImage
        BufferedImage bufferedImage = null;
        //标识
        int flag = 0;
        try {
			 /*
            获取视频文件，路径最好使用绝对路径，
                File f =new File("");
                f.getAbsolutePath()
            */
            FFmpegFrameGrabber fFmpegFrameGrabber = new FFmpegFrameGrabber(videoPath + "/" + videoFileName);
            fFmpegFrameGrabber.setFormat("mp4");  //这里要加一句不然会报错
            fFmpegFrameGrabber.start();

            //获取视频总帧数
            int ftp = fFmpegFrameGrabber.getLengthInFrames();
            log.info("时长 " + ftp / fFmpegFrameGrabber.getFrameRate() / 60);

            while (flag <= ftp) {
                frame = fFmpegFrameGrabber.grabImage();
                if (frame != null) {
                    //文件储存对象
                    bufferedImage = FrameToBufferedImage(frame);
                    break;
                }
                flag++;
            }
            fFmpegFrameGrabber.stop();
            fFmpegFrameGrabber.close();
        } catch (Exception E) {
            E.printStackTrace();
        }
        return bufferedImage;
    }

    public static MultipartFile grabberVideoFramer(String videoFileName) throws IOException {
        BufferedImage bufferedImage = grabberVideoFramer("video/",videoFileName);
        //创建一个ByteArrayOutputStream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //把BufferedImage写入ByteArrayOutputStream
        ImageIO.write(bufferedImage, "jpg", os);
        //ByteArrayOutputStream转成InputStream
        InputStream input = new ByteArrayInputStream(os.toByteArray());
        return inputStreamToMultipartFile(videoFileName,input);
    }

    public static BufferedImage FrameToBufferedImage(Frame frame) {
        //创建BufferedImage对象
        Java2DFrameConverter converter = new Java2DFrameConverter();
        return converter.getBufferedImage(frame);
    }


}
