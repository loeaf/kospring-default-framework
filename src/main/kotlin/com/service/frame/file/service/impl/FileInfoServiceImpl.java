package com.service.frame.file.service.impl;

import com.service.frame.common.misc.FileUtiles;
import com.service.frame.common.misc.ServiceImpl;
import com.service.frame.config.S3Config;
import com.service.frame.file.domain.FileInfo;
import com.service.frame.file.persistence.FileInfoRepository;
import com.service.frame.file.service.FileInfoService;
import com.service.frame.rstmeet.dto.params.RestaurantParam;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 파일 처리 서비스
 * @author break8524
 */
@Service
@RequiredArgsConstructor
public class FileInfoServiceImpl
        extends ServiceImpl<FileInfoRepository, FileInfo, Long>
        implements FileInfoService {
    private final FileInfoRepository jpaRepo;
    private final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${app.file.upload.img-path}")
    private String fileUploadPath = "";
    @Value("${app.file.upload.root-path}")
    private String rootPath = "";
    @Value("${app.file.tmp.path}")
    private String tmpPath = "";


    // S3 Config
    @Autowired
    private S3Config s3Config;

    @PostConstruct
    private void init() {
        super.set(jpaRepo, new FileInfo());
    }

    /**
     * MultipartStream을 파일로 저장후 리스트를 리턴합니다
     * @param files
     * @return
     */
    private List<FileInfo> getCPFilesByMultipart(MultipartFile[] files) {
        String Path = "";
        List<FileInfo> result = new ArrayList<>();
        for(MultipartFile mtf : files) {
            result.add(this.multipartToFileInfo(mtf));
        }
        return result;
    }

    /**
     * Multipart Stream을 파일로 변환 후 지구계획 객체로 리턴합니다
     * 파일명, 확장자, 파일경로 등이 확정됩니다.
     * @param multipartFile
     * @return
     */
    private FileInfo multipartToFileInfo(MultipartFile multipartFile) {
        FileInfo fi = new FileInfo();
        // 파일 정보
        String uploadDir = fileUploadPath;
        String originFilename = "";
        String extName = "";
        if(multipartFile.getOriginalFilename().equals("blob")) {
            originFilename = multipartFile.getOriginalFilename();
            extName = "png";
        } else {
            var splitInfo = multipartFile.getOriginalFilename().split("[.]");
            originFilename = splitInfo[0];
            extName = splitInfo[1];
        }
        Long size = multipartFile.getSize();

        // 서버에서 저장 할 파일 이름
        String saveFileName = UUID.randomUUID().toString();

        System.out.println("originFilename : " + originFilename);
        System.out.println("extensionName : " + extName);
        System.out.println("saveFileName : " + saveFileName);

        fi.setFileName(saveFileName + "." + extName);
        String filePath = uploadDir + "/" + saveFileName + "/";
        // exsist folder
        File folder = new File(filePath);
        if(!folder.exists())
            folder.mkdirs();
        // tmp folder
        File tmpFolder = new File(tmpPath);
        if(!tmpFolder.exists())
            tmpFolder.mkdirs();
        fi.setFilePath(saveFileName);
        fi.setTmpPath(tmpPath);
        fi.setLocalPath(filePath);
        fi.setOriginFileName(originFilename);
        fi.setFileExtention(extName);
        return fi;
    }


    /**
     *  Multipart Stream 전체를 파일로 저장 후 List 파일 정보를 리턴합니다
     * @param multipartFiles
     * @return
     */
    @Override
    public List<FileInfo> procCPFiles(MultipartFile[] multipartFiles) {
        // multipart로 부터 메타데이터를 추출하여 listfileInfo를 구성한다.
        List<FileInfo> files = getCPFilesByMultipart(multipartFiles);
        // for문을 돌면서 해당 썸네일을 만들고 해당 파일을 저장한다.
        for(int i = 0; i < multipartFiles.length; i++) {
            FileInfo file = files.get(i);
            try {
                var fullPath = FileUtiles.writeStreamFileByFullPath(multipartFiles[i], file.getTmpPath() + file.getFileName());
                File originFile = new File(fullPath);
                String smallFileName = createThumbNailImage(file, originFile, ThumbnailType.THUMBNAIL_100);
                file.setSmallFileName(smallFileName);
                String normalFileName = createThumbNailImage(file, originFile, ThumbnailType.THUMBNAIL_512);
                file.setNormalFileName(normalFileName);
                // move file to upload path
                FileUtiles.moveFile(fullPath, file.getLocalPath() + file.getFileName());
                FileUtiles.moveFile(file.getTmpPath() +"/"+ smallFileName, file.getLocalPath() + file.getSmallFileName());
                FileUtiles.moveFile(file.getTmpPath() +"/"+ normalFileName, file.getLocalPath() + file.getNormalFileName());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return files;
    }

    @Override
    public List<FileInfo> procCPFilesByRestparam(RestaurantParam requestParam) throws IOException {
        String imageFullPath = this.rootPath + "/enjoy.seoul" + "/" + requestParam.getSelectedSumnail();
        var files = FileUtiles.convertToMultipartFile(imageFullPath);
        MultipartFile[] multipartFiles = new MultipartFile[1];
        multipartFiles[0] = files;
        return this.procCPFiles(multipartFiles);
    }

    /**
     * 썸네일 생성하는 코드
     * @param saveFile
     * @throws IOException
     */
    private String createThumbNailImage(FileInfo fileInfo, File saveFile, ThumbnailType thumbnailType) throws IOException {
        int width = 0;
        int height = 0;
        String prefix = "";
        if (thumbnailType == ThumbnailType.THUMBNAIL_512) {
            width = 512;
            height = 512;
            prefix = "m_";
        } else if (thumbnailType == ThumbnailType.THUMBNAIL_100) {
            width = 100;
            height = 100;
            prefix = "s_";
        }
        // Create thumbnail
        BufferedImage thumbnailImage = Thumbnails.of(saveFile)
                .size(width, height)
                .asBufferedImage();

        // Convert thumbnail to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnailImage, "png", baos);
        byte[] thumbnailBytes = baos.toByteArray();
        // save thumbnail

        String thumbnailFilename = prefix + fileInfo.getFileName();
        File thumbnailFile = new File(fileInfo.getTmpPath(), thumbnailFilename);
        FileOutputStream fos = new FileOutputStream(thumbnailFile);
        fos.write(thumbnailBytes);
        fos.close();
        return thumbnailFilename;
    }


    @Override
    public void sendS3Files() throws IOException {
        if(true) {

        } else {
            System.getProperty("user.home");
        }
        File file = new File("C:\\Users\\break8524\\Desktop\\test.txt");
        file.createNewFile();
//        AmazonS3Client s3Client = s3Config.amazonS3Client();
//        s3Client.putObject("bucket", file.getName(), file);
    }

}

enum ThumbnailType {
    THUMBNAIL,
    THUMBNAIL_512,
    THUMBNAIL_100
}