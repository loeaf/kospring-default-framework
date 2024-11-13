package com.service.frame.rstmeet.service.impl;

import com.service.frame.common.misc.ServiceImpl;
import com.service.frame.datacreator.mapper.MediaMapper;
import com.service.frame.datacreator.mapper.ReViewMapper;
import com.service.frame.datacreator.mapper.RestaurantMapper;
import com.service.frame.datacreator.model.Media;
import com.service.frame.datacreator.model.ReView;
import com.service.frame.rstmeet.dto.RstMeetFile;
import com.service.frame.rstmeet.dto.params.RestaurantParam;
import com.service.frame.rstmeet.mapper.RestaurantProcMapper;
import com.service.frame.rstmeet.model.Restaurant;
import com.service.frame.rstmeet.model.RestaurantDto;
import com.service.frame.rstmeet.repository.RestaurantRepository;
import com.service.frame.rstmeet.service.RestaurantService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl
        extends ServiceImpl<RestaurantRepository, Restaurant, String>
        implements RestaurantService {
    private final RestaurantRepository jpaRepo;
    @Autowired
    private final RestaurantProcMapper restaurantProcMapper;
    @Autowired
    private final RestaurantMapper restaurantMapper;

    @Autowired
    private final MediaMapper mediaMapper;
    @Autowired
    private final ReViewMapper reViewMapper;

    @PostConstruct
    private void init() {
        super.set(jpaRepo, new Restaurant());
    }

    @Override
    @Transactional
    public void registBulkByCSV() throws FileNotFoundException {
        List<RstMeetFile> rstMeetFileList = this.readCSVByClassPath();
        rstMeetFileList.forEach(p -> {
            System.out.println(p.toString());
            List<Restaurant> restaurantList =  this.jpaRepo.findByRoadAddressAndName(p.getKoreanRoadAddress(), p.getRestaurant());
            if(restaurantList.size() > 0) {
                System.out.println("Already Exist" + p.toString());
                return;
            }
            var rst = new Restaurant();
            rst.setId(UUID.randomUUID().toString());
            rst.setRestaurantNumber(p.getRstMeetFileNumber());
            rst.setName(p.getRestaurant());
            rst.setRoadAddress(p.getKoreanRoadAddress());
            rst.setJibunAddress(p.getKoreanJibunAddress());
            rst.setEnglishAddress(p.getEnglishAddress());
            rst.setMiniAddress(p.getArea());
            rst.setLatitude(Double.parseDouble(p.getLat()));
            rst.setLongitude(Double.parseDouble(p.getLog()));
            rst.setGeoInfo(null);
            rst.setRegDate(new Date());
            rst.setPhoneNumber(p.getPhoneNumber());
            rst.setHoliday(p.getHoliday());
            rst.setRefinedGeoLocation(0);
            rst.setRepresentativeMenu(p.getRepresentativeMenu());
            this.jpaRepo.save(rst);
        });
    }

    @Override
    public List<RestaurantDto> findRestaurant(RestaurantParam restaurant) {
        List<RestaurantDto> restaurantList = restaurantProcMapper.findRestaurant2(restaurant);
        restaurantList.forEach(p -> {
            List<Media> mediaList = mediaMapper.findByRestaurantId(p.getId());
            p.setMediaList(mediaList);
        });
        return restaurantList;
    }

    @Override
    public List<Restaurant> findRestaurantByRoadAddress(String roadAddress, String name) {
        List<Restaurant> restaurantList = jpaRepo.findByRoadAddressAndName(roadAddress, name);
        return null;
    }

    @Override
    public List<RestaurantDto> findRestaurantByInstaInfoId(String instaInfoId) {
        List<RestaurantDto> restaurantList = restaurantProcMapper.findRestaurantByInstaInfoId(instaInfoId);
        return restaurantList;
    }

    @Override
    @Transactional
    public void fetch(RestaurantParam dto) {
        var restaurant = new com.service.frame.datacreator.model.Restaurant();
        restaurant.setId(dto.getId());
        restaurant.setName(dto.getName());
        restaurant.setRoadAddress(dto.getRoadAddress());
        restaurant.setJibunAddress(dto.getJibunAddress());
        restaurant.setDataType(dto.getDataType());
        restaurant.setUpdateDate(new Date());
        var result = this.restaurantMapper.updateByPrimaryKeySelective(restaurant);
        if(dto.getMedia() != null) {
            Media media = new Media();
            media.setId(UUID.randomUUID().toString());
            media.setRestaurantId(dto.getId());
            media.setName(dto.getMedia().getName());
            media.setPath(dto.getMedia().getPath());
            media.setNormalFileName(dto.getMedia().getNormalFileName());
            media.setSmallFileName(dto.getMedia().getSmallFileName());
            media.setInstagramId(dto.getInstagramId());
            media.setShortCode(dto.getShortCode());
            media.setRegDate(new Date());
            media.setRestaurantId(dto.getId());
            this.mediaMapper.insertSelective(media);
        }
        if(dto.getReViewContent() != null) {
            ReView reView = new ReView();
            reView.setId(UUID.randomUUID().toString());
            reView.setRestaurantId(dto.getId());
            reView.setContent(dto.getReViewContent());
            reView.setIsMain("Y");
            reView.setRegDate(new Date());
            reView.setRestaurantId(dto.getId());
            this.reViewMapper.insertSelective(reView);
        }
    }

    private List<RstMeetFile> readCSVByClassPath() throws FileNotFoundException {
        List<RstMeetFile> rstMeetFileList = new ArrayList<>();
        BufferedReader br = null;
//        File file = ResourceUtils.getFile("classpath:/static/file/sejong_rst_meet.csv");
        File file = ResourceUtils.getFile("src/main/resources/static/file/sejong_rst_meet.csv");
        if (file.exists()) {
            System.out.println("file exists");
        } else {
            System.out.println("file not exists");
        }
        InputStream targetStream = new DataInputStream(new FileInputStream(file));
        try{
            br = new BufferedReader(new InputStreamReader(targetStream, "UTF-8"));
            //Charset.forName("UTF-8");
            String line = "";
            int idx = 0;
            while((line = br.readLine()) != null){
                if(idx == 0){
                    idx++;
                    continue;
                }
                //CSV 1행을 저장하는 리스트
                List<String> tmpList = new ArrayList<String>();
                String array[] = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                System.out.println(line);
                var rstMeetFile = new RstMeetFile();
                rstMeetFile.setRstMeetFileNumber(Integer.parseInt(array[0]));
                rstMeetFile.setCategory(array[1]);
                rstMeetFile.setArea(array[2]);
                rstMeetFile.setRestaurant(array[3]);
                rstMeetFile.setRepresentativeMenu(array[4]);
                rstMeetFile.setHoliday(array[5]);
                rstMeetFile.setPhoneNumber(array[6]);
                rstMeetFile.setKoreanRoadAddress(array[7]);
                rstMeetFile.setKoreanJibunAddress(array[8]);
                rstMeetFile.setEnglishAddress(array[9]);
                rstMeetFile.setSpecAddr(array[10]);
                if (array.length > 12) {
                    rstMeetFile.setLog(array[11]);
                    rstMeetFile.setLat(array[12]);
                } else {
                    rstMeetFile.setLog("0");
                    rstMeetFile.setLat("0");
                }
                System.out.println(rstMeetFile.toString());
                rstMeetFileList.add(rstMeetFile);
            }
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            try{
                if(br != null){
                    br.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return rstMeetFileList;
    }
}
