package com.lyd.utils;

import com.aspose.cells.Workbook;
import com.aspose.slides.Presentation;
import com.aspose.words.Document;
import com.aspose.words.License;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.python.bouncycastle.util.test.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

/**
 * @author 天狗
 * @desc 文件转PDF
 * Aspose下载地址：https://repository.aspose.com/repo/com/aspose/
 * @date 2022/7/25
 */
@Slf4j
public class PdfUtils {

    /**
     * @desc word 转为 pdf 输出
     * @param inPath  word文件
     * @param outPath pdf 输出文件目录
     */
    public static String word2pdf(String inPath, String outPath) {
        // 验证License
//        if (!isWordLicense()) {
//            return null;
//        }
        FileOutputStream os = null;
        try {
//            String path = outPath.substring(0, outPath.lastIndexOf(File.separator));
            String path = outPath.substring(0, outPath.lastIndexOf('/'));
            File file = new File(path);
            // 创建文件夹
            if (!file.exists()) {
                file.mkdirs();
            }
            // 新建一个空白pdf文档
            file = new File(outPath);
            os = new FileOutputStream(file);
            // Address是将要被转化的word文档
            Document doc = new Document(inPath);
            // 全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF,
            doc.save(os, com.aspose.words.SaveFormat.PDF);
            os.close();
        } catch (Exception e) {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        return outPath;
    }


    /**
     * @desc excel 转为 pdf 输出
     * @param inPath  excel 文件
     * @param outPath pdf 输出文件目录
     */
    public static String excel2pdf(String inPath, String outPath) {
        // 验证License
//        if (!isWordLicense()) {
//            return null;
//        }
        FileOutputStream os = null;
        try {
            String path = outPath.substring(0, outPath.lastIndexOf('/'));
            File file = new File(path);
            // 创建文件夹
            if (!file.exists()) {
                file.mkdirs();
            }
            // 新建一个空白pdf文档
            file = new File(outPath);
            os = new FileOutputStream(file);
            // Address是将要被转化的excel表格
//            Workbook workbook = new Workbook(new FileInputStream(getFile(inPath)));
            Workbook workbook = new Workbook(new FileInputStream(new File(inPath)));
            workbook.save(os, com.aspose.cells.SaveFormat.PDF);
            os.close();
        } catch (Exception e) {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        return outPath;
    }

    /**
     * @desc ppt 转为 pdf 输出
     * @param inPath  ppt 文件
     * @param outPath pdf 输出文件目录
     */
    public static String ppt2pdf(String inPath, String outPath) {
        // 验证License
//        if (!isWordLicense()) {
//            return null;
//        }
        FileOutputStream os = null;
        try {
            String path = outPath.substring(0, outPath.lastIndexOf('/'));
            File file = new File(path);
            // 创建文件夹
            if (!file.exists()) {
                file.mkdirs();
            }
            // 新建一个空白pdf文档
            file = new File(outPath);
            os = new FileOutputStream(file);
            // Address是将要被转化的PPT幻灯片
//            Presentation pres = new Presentation(new FileInputStream(getFile(inPath)));
            Presentation pres = new Presentation(new FileInputStream(new File(inPath)));
            pres.save(os, com.aspose.slides.SaveFormat.Pdf);
            os.close();
        } catch (Exception e) {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        return outPath;
    }

    /**
     * @desc 验证 Aspose.word 组件是否授权
     * 无授权的文件有水印和试用标记
     */
    public static boolean isWordLicense() {
        boolean result = false;
        try {
            InputStream is = Test.class.getClassLoader().getResourceAsStream("\\license.xml"); // license.xml应放在..\WebRoot\WEB-INF\classes路径下
            License aposeLic = new License();
            aposeLic.setLicense(is);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @desc OutputStream 转 InputStream
     */
    public static ByteArrayInputStream parse(OutputStream out) {
        ByteArrayOutputStream baos = (ByteArrayOutputStream) out;
        ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());
        return swapStream;
    }

    /**
     * @desc InputStream 转 File
     */
    public static File inputStreamToFile(InputStream ins, String name) throws Exception {
        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + name);
        if (file.exists()) {
            return file;
        }
        OutputStream os = new FileOutputStream(file);
        int bytesRead;
        int len = 8192;
        byte[] buffer = new byte[len];
        while ((bytesRead = ins.read(buffer, 0, len)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        ins.close();
        return file;
    }

    /**
     * @desc 根据网络地址获取 File 对象
     */
    public static File getFile(String url) throws Exception {
        String suffix = url.substring(url.lastIndexOf("."));
        HttpURLConnection httpUrl = (HttpURLConnection) new URL(url).openConnection();
        httpUrl.connect();
        return PdfUtils.inputStreamToFile(httpUrl.getInputStream(), UUID.randomUUID().toString() + suffix);
    }

    /**
     * 将 multipartFile 转换成  临时 file 文件.（该临时文件使用完记得 delete掉.）
     *
     * @param multipartFile multipartFile
     * @return 临时的 file 文件.
     * @throws IOException io
     */
    public static File getFile(MultipartFile multipartFile) throws IOException {
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


    private static int interval = -5;

    /**
     * @desc 给pdf加上自定义水印
     * @param inputFile   输入路径
     * @param outputFile    输出路径
     * @param waterMarkName 水印文字
     */
    public static void wateMark(String inputFile, String outputFile, String waterMarkName){
        try{
            File file = new File("previewPdf");
            // 创建文件夹
            if (!file.exists()) {
                file.mkdirs();
            }
            PdfReader reader = new PdfReader(new FileInputStream(inputFile));
            PdfStamper stamper = new PdfStamper(reader,new FileOutputStream(outputFile));
            BaseFont base = BaseFont.createFont("STSong-Light","UniGB-UCS2-H",BaseFont.EMBEDDED);
            Rectangle pageRect = null;
            PdfGState gs = new PdfGState();
            gs.setFillOpacity(0.3f);
            gs.setStrokeOpacity(0.4f);
            int total = reader.getNumberOfPages() + 1;

            JLabel label = new JLabel();
            FontMetrics metrics;
            int textH = 0;
            int textW = 0;
            label.setText(waterMarkName);
            metrics = label.getFontMetrics(label.getFont());
            textH = metrics.getHeight();
            textW = metrics.stringWidth(label.getText());

            PdfContentByte under;
            for (int i = 1; i < total; i++) {
                pageRect = reader.getPageSizeWithRotation(i);
                under = stamper.getOverContent(i);
                under.saveState();
                under.setGState(gs);
                under.beginText();
                under.setFontAndSize(base, 20);
                under.setRGBColorFill(145,145,145);

                // 水印文字成30度角倾斜
                //你可以随心所欲的改你自己想要的角度
                for (int height = interval + textH; height < pageRect.getHeight();
                     height = height + textH*3) {
                    for (int width = interval + textW; width < pageRect.getWidth() + textW;
                         width = width + textW*2) {
                        under.showTextAligned(Element.ALIGN_LEFT
                                , waterMarkName, width - textW,
                                height - textH, 30);
                    }
                }
                // 添加水印文字
                under.endText();
            }
            stamper.close();
            reader.close();


        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 截取PDF中的某一页作为缩略图，并上传（保存）
     * @param pdfFileName
     * @return
     */
    public static MultipartFile PDFFramer(String pdfFileName){

        File file = new File(pdfFileName);
        MultipartFile multipartFile = null;
        try  {
            // 打开来源 pdf
            log.info("开始截取PDF:");
            //PDDocument类的load()方法用于加载现有PDF文档
            PDDocument pdfDocument = PDDocument.load(file);
            //PDFRenderer的类将PDF文档呈现为AWT BufferedImage
            PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);

            // 提取的页码
            int pageNumber = 0;
            // 以300 dpi 读取存入 BufferedImage 对象
            int dpi = 300;
            //Renderer类的renderImage()方法在特定页面中渲染图像
            BufferedImage buffImage = pdfRenderer.renderImageWithDPI(pageNumber, dpi, ImageType.RGB);
            // 文件类型转换
//            MultipartFile multipartFile = fileCase(buffImage);
            //创建一个ByteArrayOutputStream
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            //把BufferedImage写入ByteArrayOutputStream
            ImageIO.write(buffImage, "jpg", os);
            //ByteArrayOutputStream转成InputStream
            InputStream input = new ByteArrayInputStream(os.toByteArray());
            //InputStream转成MultipartFile
//            multipartFile =new MockMultipartFile("file", "file.jpg", "text/plain", input);
            multipartFile = FileUtils.inputStreamToMultipartFile("file.jpg",input);

            // 临时存为File查看效果
//            FileConverUtils.approvalFile(multipartFile);

            // 关闭文档
            pdfDocument.close();
        }
        catch (InvalidPasswordException e)  {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return multipartFile;
    }

    public static Integer getPdfPageNum(String pdfPath) throws IOException {
        PdfReader pdfReader = new PdfReader(new FileInputStream(pdfPath));
        return pdfReader.getNumberOfPages();
    }




}
